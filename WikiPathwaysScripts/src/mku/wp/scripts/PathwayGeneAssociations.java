// WikiPathways Scripts
// Copyright 2015 Martina Kutmon
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package mku.wp.scripts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bridgedb.BridgeDb;
import org.bridgedb.DataSource;
import org.bridgedb.IDMapper;
import org.bridgedb.Xref;
import org.bridgedb.bio.DataSourceTxt;
import org.bridgedb.bio.Organism;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.wikipathways.webservice.WSCurationTag;
import org.pathvisio.wikipathways.webservice.WSPathway;
import org.wikipathways.client.WikiPathwaysClient;

/**
 * 
 * Simple script that generates a file with pathway-gene associations
 * that can then e.g. be imported as a network in Cytoscape
 * 
 * All curated pathways for a species are downloaded from 
 * WikiPathways. 
 * 
 * The identifiers are unified to the defined system
 * code using a BridgeDb database file.
 * 
 * @author mku
 *
 */
public class PathwayGeneAssociations {

	public static void main(String[] args) throws Exception {
		Organism org = Organism.HomoSapiens;
		
		// use Ensembl identifiers
		String sysCode = "En";
		
		// get labels from HGNC
		String label = "H";
		
		File bridgeDb = new File("/home/martina/Data/BridgeDb/Hs_Derby_20130701.bridge");
		
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		Date date = new Date();
		File outputNetwork = new File("associations_" + org.shortName() + "_" + dateFormat.format(date) + ".txt");
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputNetwork));
		
		// load libs for derby files
		Class.forName("org.bridgedb.rdb.IDMapperRdb");  
		DataSourceTxt.init();
		IDMapper mapper = BridgeDb.connect("idmapper-pgdb:" + bridgeDb.getAbsolutePath());
		
		DataSource dsEn = DataSource.getExistingBySystemCode(sysCode);
		DataSource dsHGNC = DataSource.getExistingBySystemCode(label);
		
		// id, label
		Map<String, String> map = new HashMap<String, String>();
		
		WikiPathwaysClient client = new WikiPathwaysClient(new URL("http://webservice.wikipathways.org"));
		WSCurationTag [] tags = client.getCurationTagsByName("Curation:AnalysisCollection");
		for(WSCurationTag t : tags) {
			if(t.getPathway().getSpecies().equals(org.latinName())) {
				WSPathway wsp = client.getPathway(t.getPathway().getId());
				Pathway p = WikiPathwaysClient.toPathway(wsp);
				Set<String> ids = new HashSet<String>();
				for(Xref x : p.getDataNodeXrefs()) {
					Set<Xref> res = mapper.mapID(x, dsEn);
					for(Xref r : res) {
						ids.add(r.getId());
						String la = "";
						if(!map.containsKey(r.getId())) {
							Set<Xref> res2 = mapper.mapID(r, dsHGNC);
							if(res2.size() > 0) {
								la = res2.iterator().next().getId();
								map.put(r.getId(), la);
							}
						} else {
							la = map.get(r.getId());
						}
					}
				}
				for(String id : ids) {
					writer.write(t.getPathway().getId() + "\t" + t.getPathway().getName() + "\t" + id + "\t" + ((map.get(id) == null) ? "" : map.get(id)) + "\n");
				}
			}
		}
		writer.close();
	}
}
