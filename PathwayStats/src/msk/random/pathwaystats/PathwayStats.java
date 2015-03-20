package msk.random.pathwaystats;

import java.io.File;
import java.io.IOException;

import org.bridgedb.BridgeDb;
import org.bridgedb.DataSource;
import org.bridgedb.IDMapper;
import org.bridgedb.IDMapperException;
import org.bridgedb.bio.BioDataSource;
import org.bridgedb.rdb.construct.DataDerby;
import org.pathvisio.core.Engine;
import org.pathvisio.core.preferences.PreferenceManager;
import org.pathvisio.data.DataException;
import org.pathvisio.desktop.PvDesktop;
import org.pathvisio.desktop.gex.GexManager;
import org.pathvisio.desktop.gex.SimpleGex;
import org.pathvisio.desktop.visualization.Criterion;
import org.pathvisio.gexplugin.GexPlugin;
import org.pathvisio.gexplugin.GexTxtImporter;
import org.pathvisio.gexplugin.ImportInformation;
import org.pathvisio.gui.SwingEngine;
import org.pathvisio.statistics.StatisticsPathwayResult;
import org.pathvisio.statistics.StatisticsPlugin;
import org.pathvisio.statistics.StatisticsResult;
import org.pathvisio.statistics.ZScoreCalculator;
import org.pathvisio.visualization.plugins.VisualizationPlugin;

/**
 * perform pathway statistics (PathVisio)
 * @author msk
 *
 */
public class PathwayStats {

	// repeat main method for all files in a directory if that's what you want
	public static void main(String[] args) throws IOException, IDMapperException, DataException, ClassNotFoundException {
		String input = "/home/msk/example.txt";
		
		// number of column which contains identifier - starts with 0
		int idCol = 0;
		
		// bridgedb system code: http://www.pathvisio.org/documentation/system-codes/
		String syscode = "L";
		
		File bridgeDbFile = new File("/home/msk/Data/BridgeDb/Hs_Derby_20130701.bridge");
		File pwCol = new File("/home/msk/Data/WikiPathways/2015-03-20-wikipathways-human");
		
		String crit = "[up] = 1";
		
		PathwayStats stats = new PathwayStats();
		StatisticsResult res = stats.runStatistics(input, idCol, syscode, bridgeDbFile, pwCol, crit);
		for(StatisticsPathwayResult r : res.getPathwayResults()) {
			
			// hides all pathways were none of the genes are measured (remove if you want results for all pathways!
			if(!Double.isNaN(r.getZScore())) {
				
				// write this in a file or save it in a map!
				System.out.println(r.getFile().getName() + "\t" + r.getZScore());
			}
		}
	}
	
	private String inputFile;
	private int idColNum;
	private String syscode;
	private GexManager gexManager;
	private File gexFile;
	private File pwCol;
	private IDMapper gdb;
	
	public PathwayStats() {
		PreferenceManager.init();
		BioDataSource.init();
	}
	
	public StatisticsResult runStatistics(String input, int idCol, String syscode, File bridgeDbFile, File pwCol, String crit) throws IOException, IDMapperException, DataException, ClassNotFoundException {
		this.inputFile = input;
		this.idColNum = idCol;
		this.syscode = syscode;
		this.pwCol = pwCol;
		
		loadIdMappers(bridgeDbFile);
		
		importData();
		
		return calcStats(crit);
	}
	
	private StatisticsResult calcStats(String expression) throws DataException, IDMapperException {
		Engine engine = new Engine();
		PvDesktop pvDesktop = new PvDesktop(new SwingEngine(engine), null);

		GexPlugin gexPlugin = new GexPlugin();
		gexPlugin.init(pvDesktop);

		VisualizationPlugin visPlugin = new VisualizationPlugin();
		visPlugin.init(pvDesktop);

		StatisticsPlugin plugin = new StatisticsPlugin();
		plugin.init(pvDesktop);

		SimpleGex gex = new SimpleGex("" + gexFile, false, new DataDerby());
		pvDesktop.getGexManager().setCurrentGex(gex);

		Criterion criteria = new Criterion();
		criteria.setExpression(expression, gex.getSampleNames());
		ZScoreCalculator zsc = new ZScoreCalculator(criteria, pwCol,
				pvDesktop.getGexManager().getCachedData(),
				gdb, null);
		StatisticsResult result = zsc.calculateMappFinder();
		return result;
	}
	
	private void importData() throws IOException, IDMapperException {
		gexManager = new GexManager();
		ImportInformation info = new ImportInformation();
		info.setTxtFile(new File(inputFile));
		info.setGexName(inputFile + ".pgex");
		info.setIdColumn(idColNum);
		info.setSyscodeFixed(true);
		info.setDataSource(DataSource.getBySystemCode(syscode));
		gexFile = new File(info.getGexName());

		GexTxtImporter.importFromTxt(info, null, gdb, gexManager);
	}
	
	private IDMapper loadIdMappers(File bridgeDbFile) throws IDMapperException, ClassNotFoundException {
		Class.forName("org.bridgedb.rdb.IDMapperRdb");
		gdb = BridgeDb.connect("idmapper-pgdb:" + bridgeDbFile.getAbsolutePath());
		return gdb;
	}

}
