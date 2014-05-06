package scripts;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.bridgedb.BridgeDb;
import org.bridgedb.IDMapper;
import org.bridgedb.IDMapperException;
import org.bridgedb.Xref;
import org.bridgedb.XrefIterator;


/**
 * This main class returns all Ensembl identifiers present in
 * a bridgedb database file.
 * First asks the user to select a file in a dialog.
 * @author martina
 *
 */
public class AllEnsemblIds {

	public static void main(String[] args) throws ClassNotFoundException, IDMapperException, IOException {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("BridgeDb database", "bridge", "bridge");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) { 
			File file = chooser.getSelectedFile();
	
			File output = new File(file.getParentFile(),"ensembl-ids.txt");
			BufferedWriter writer = new BufferedWriter(new FileWriter(output));
			// load libs for derby files
			Class.forName("org.bridgedb.rdb.IDMapperRdb");  
			IDMapper mapper = BridgeDb.connect("idmapper-pgdb:" + file.getAbsolutePath());
			if(mapper instanceof XrefIterator) {
				for (Xref x : ((XrefIterator) mapper).getIterator()) {
					if(x.getDataSource().getSystemCode().equals("En")) {
						writer.write(x.getId() + "\n"); 
					}
				}
			}
			writer.close();
			JOptionPane.showMessageDialog(null,"Output file has been created:\n" + output.getAbsolutePath());
		}
	}
}
