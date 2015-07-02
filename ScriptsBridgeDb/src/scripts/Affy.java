package scripts;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.bridgedb.BridgeDb;
import org.bridgedb.DataSource;
import org.bridgedb.IDMapper;
import org.bridgedb.Xref;
import org.bridgedb.XrefIterator;
import org.bridgedb.bio.BioDataSource;

public class Affy {

	public static void main(String[] args) throws Exception {
//		File f1 = new File("C:\\Users\\martina.kutmon\\Data\\BridgeDb\\Hs_Derby_Ensembl_77_v0.3.bridge");
		File f1 = new File("C:\\Users\\martina.kutmon\\Data\\BridgeDb\\Hs_Derby_20130701.bridge");
		File f2 = new File("C:\\Users\\martina.kutmon\\Data\\BridgeDb\\Hs_Derby_Ensembl_79_v.01.bridge");
		
		Class.forName("org.bridgedb.rdb.IDMapperRdb");
		BioDataSource.init();
		IDMapper mapper1 = BridgeDb.connect("idmapper-pgdb:" + f1.getAbsolutePath());
		IDMapper mapper2 = BridgeDb.connect("idmapper-pgdb:" + f2.getAbsolutePath());
		
		Set<DataSource> ds = mapper1.getCapabilities().getSupportedSrcDataSources();
	
		for(DataSource d : ds) {
			System.out.println(d.getSystemCode() + "\t" + d.getFullName());
			Set<String> oldIds = new HashSet<String>();
			for (Xref x : ((XrefIterator) mapper1).getIterator()) {
				if(x.getDataSource().equals(d)) {
					oldIds.add(x.getId());
				}
			}
			System.out.println("old ids: " + oldIds.size());
			Set<String> newIds = new HashSet<String>();
			for (Xref x : ((XrefIterator) mapper2).getIterator()) {
				if(x.getDataSource().equals(d)) {
					newIds.add(x.getId());
				}
			}
			System.out.println("new ids: " + newIds.size());
			
			int countOnlyOld = 0;
			int countBoth = 0;
			for(String id : oldIds) {
				if(!newIds.contains(id)) {
					countOnlyOld++;
				}
				if(newIds.contains(id)) {
					countBoth++;
				}
			}
			
			int countOnlyNew = 0;
			for(String id : newIds) {
				if(!oldIds.contains(id)) {
					countOnlyNew++;
				}
			}
			
			System.out.println("in both dbs: " + countBoth);
			System.out.println("only old: " + countOnlyOld);
			System.out.println("only new: " + countOnlyNew);
			double per1 = (double)((double)countOnlyOld/(double)oldIds.size())*100.0;
			double per2 = (double)((double)countOnlyNew/(double)oldIds.size())*100.0;
			
			System.out.println("percentage ids lost\t-" + per1);
			System.out.println("percentage ids gained\t" + per2);
			
			System.out.println("absolute diff " + (countOnlyNew + countOnlyOld));

			System.out.println("\n");
		}
		
		
	}

}
