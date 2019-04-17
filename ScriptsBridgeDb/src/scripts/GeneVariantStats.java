package scripts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Set;

import org.bridgedb.BridgeDb;
import org.bridgedb.DataSource;
import org.bridgedb.IDMapper;
import org.bridgedb.Xref;
import org.bridgedb.XrefIterator;
import org.bridgedb.bio.DataSourceTxt;

public class GeneVariantStats {

	public static void main(String[] args) throws Exception {
		File bridgedb = new File("C:\\Users\\martina.kutmon\\Downloads\\gene-variant\\Missense_95_BridgeDb.bridge");
		
		Set<Xref> genes = new HashSet<Xref>();
		Set<Xref> snps = new HashSet<Xref>();
		
		DataSourceTxt.init();

		// BridgeDb setup
		Class.forName("org.bridgedb.rdb.IDMapperRdb");
		IDMapper mapper = BridgeDb.connect("idmapper-pgdb:" + bridgedb.getAbsolutePath());
		
	
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File("genes.txt")));
		BufferedWriter writer2 = new BufferedWriter(new FileWriter(new File("snps.txt")));
		BufferedWriter writer3 = new BufferedWriter(new FileWriter(new File("associations.txt")));
		int count = 0;
		if(mapper instanceof XrefIterator) {
			for (Xref x : ((XrefIterator) mapper).getIterator()) {
				if(!genes.contains(x) && !snps.contains(x)) {
					if(x.getDataSource().equals(DataSource.getExistingBySystemCode("En"))) {
						genes.add(x);
						writer.write(x + "\n");
						for(Xref x2 : mapper.mapID(x, DataSource.getExistingBySystemCode("Sn"))) {
							count++;
							writer3.write(x + "\t" + x2 + "\n");
						}
						System.out.println(x + "\t" + count);
					} else if (x.getDataSource().equals(DataSource.getExistingBySystemCode("Sn"))) {
						snps.add(x);
						writer2.write(x + "\n");
					} else {
						System.err.println("ERROR");
					}
				}
			}
		}	
		writer.close();
		writer2.close();
		writer3.close();
		
		System.out.println(genes.size());
		System.out.println(snps.size());
	}

}
