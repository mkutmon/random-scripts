package mku.wp.scripts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bridgedb.BridgeDb;
import org.bridgedb.DataSource;
import org.bridgedb.IDMapper;
import org.bridgedb.Xref;
import org.bridgedb.bio.DataSourceTxt;
import org.pathvisio.wikipathways.webservice.WSSearchResult;
import org.wikipathways.client.WikiPathwaysClient;

public class PathwaysForGeneList {

	public static void main(String[] args) throws Exception {
		
		// change settings here
		String syscode = "L"; // L for Entrez Gene, En for Ensembl
		// file with gene ids - one per line
		File input = new File("test.txt");
		File bridgeDb = new File("C:/Users/martina.kutmon/Data/BridgeDb/Hs_Derby_Ensembl_85.bridge");
		File output = new File("output.txt");
		
		DataSourceTxt.init();
		Class.forName("org.bridgedb.rdb.IDMapperRdb");  
		DataSourceTxt.init();
		IDMapper mapper = BridgeDb.connect("idmapper-pgdb:" + bridgeDb.getAbsolutePath());

		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		Set<Xref> set = new HashSet<Xref>();
		while((line = reader.readLine()) != null) {
			Xref x = new Xref(line, DataSource.getExistingBySystemCode(syscode));
			set.add(x);
		}
		reader.close();
		WikiPathwaysClient client = new WikiPathwaysClient(new URL("http://webservice.wikipathways.org"));
		Map<String, String> pathwayName = new HashMap<>();
		Map<String, List<String>> pathwayGenes = new HashMap<>();
		Map<String, String> geneName = new HashMap<>();

		for(Xref x : set) {
			Set<Xref> res1 = mapper.mapID(x, DataSource.getExistingBySystemCode("H"));
			if(!res1.isEmpty()) {
				Xref gName = res1.iterator().next();
				geneName.put(x.getId(), gName.getId());
			} else {
				geneName.put(x.getId(), "");
			}
			WSSearchResult [] res = client.findPathwaysByXref(x);
			for (WSSearchResult r : res) {
				if(!pathwayName.containsKey(r.getId())) {
					pathwayName.put(r.getId(), r.getName());
				}
				if(!pathwayGenes.containsKey(r.getId())) {
					pathwayGenes.put(r.getId(), new ArrayList<>());
				}
				if(!pathwayGenes.get(r.getId()).contains(x.getId())) {
					pathwayGenes.get(r.getId()).add(x.getId());
				}
			}
		}
		
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(output));
		for(String s : pathwayGenes.keySet()) {
			for(String g : pathwayGenes.get(s)) {
				writer.write(s + "\t" + pathwayName.get(s) + "\t" + g + "\t" + geneName.get(g) + "\n");
			}
		}
		writer.close();
	}

}
