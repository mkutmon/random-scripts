//WikiPathways Scripts
//Copyright 2015 Martina Kutmon
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package mku.wp.scripts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.bridgedb.BridgeDb;
import org.bridgedb.DataSource;
import org.bridgedb.IDMapper;
import org.bridgedb.IDMapperException;
import org.bridgedb.Xref;
import org.bridgedb.bio.DataSourceTxt;
import org.pathvisio.core.model.ConverterException;
import org.pathvisio.core.model.Pathway;

/**
 * 
 * This class takes two collections from WikiPathways
 * and compares the data node coverage.
 * Is it possible to run it for genes (select gene
 * mapping db + gene db system code) or metabolites
 * (select metabolite mapping db + metabolite db system
 * code).
 * @author mku
 *
 */
public class WPCollectionComparator {

	private IDMapper mapper;
	private Set<Xref> col1Ids;
	private Set<Xref> col2Ids;
	private Set<Xref> col1Unique;
	private Set<Xref> col2Unique;
	private String dsSystemCode = "En";
	
	
	public static void main(String[] args) {
		if(args.length < 3) {
			System.out.println("Invalid number of arguments\n[1] BridgeDb file path\n"
					+ "[2] Collection 1 directory (GPML files)\n"
					+ "[3] Collection 2 directory (GPML files)\n"
					+ "Optional: [4] system code for mapping (default = En)");
		} else {
			// argument 1 = bridgedb mapping file
			File bridgedb = new File(args[0]);
			// argument 2 = collection 1 dir
			File col1Dir = new File(args[1]);
			// argument 3 = collection 2 dir
			File col2Dir = new File(args[2]);
			
			try {
				WPCollectionComparator comparator = new WPCollectionComparator(bridgedb);
				if(args.length == 4) {
					// optional argument 4 = system code
					comparator.setDsSystemCode(args[3]);
				}
				
				comparator.compare(col1Dir, col2Dir);
				
				File out1 = new File("collection1_ids.txt");
				File out2 = new File("collection2_ids.txt");
				try {
					BufferedWriter writer1 = new BufferedWriter(new FileWriter(out1));
					BufferedWriter writer2 = new BufferedWriter(new FileWriter(out2));
					
					for(Xref x : comparator.getCol1Ids()) {
						writer1.write(x.getId() + "\n");
					}
					for(Xref x : comparator.getCol2Ids()) {
						writer2.write(x.getId() + "\n");
					}
					
					writer1.close();
					writer2.close();
					
					System.out.println("Result:");
					System.out.println("Collection 1: " + comparator.getCol1Ids().size() + " (" + comparator.getCol1Unique().size() + " unique)");
					System.out.println("Collection 2: " + comparator.getCol2Ids().size() + " (" + comparator.getCol2Unique().size() + " unique)");

				} catch (IOException e) {
					System.err.println("Cannot write output files.");
				}
				
				
			} catch (ClassNotFoundException e) {
				System.err.println("Could not find BridgeDb libraries.\n" + e.getMessage());
			} catch (IDMapperException e) {
				System.err.println("Identifier mapping error.\n" + e.getMessage());
			} catch (ConverterException e) {
				System.err.println("Error occurred when reading pathway file.\n" + e.getMessage());
			}
		}
	}

	
	public WPCollectionComparator(File bridgedb) throws ClassNotFoundException, IDMapperException {
		DataSourceTxt.init();
		Class.forName("org.bridgedb.rdb.IDMapperRdb");
		mapper = BridgeDb.connect("idmapper-pgdb:" + bridgedb.getAbsolutePath());
		col1Ids = new HashSet<Xref>();
		col2Ids = new HashSet<Xref>();
		col1Unique = new HashSet<Xref>();
		col2Unique = new HashSet<Xref>();
	}
	
	public void compare(File wpDir, File reactomeDir) throws ConverterException, IDMapperException {
		for(File f : wpDir.listFiles()) {
			Pathway p = new Pathway();
			p.readFromXml(f, true);
			
			for(Xref x : p.getDataNodeXrefs()) {
				Set<Xref> res = mapper.mapID(x, DataSource.getExistingBySystemCode(dsSystemCode));
				col1Ids.addAll(res);
			}
		}
		
		for(File f : reactomeDir.listFiles()) {
			Pathway p = new Pathway();
			p.readFromXml(f, true);
			
			for(Xref x : p.getDataNodeXrefs()) {
				Set<Xref> res = mapper.mapID(x, DataSource.getExistingBySystemCode(dsSystemCode));
				col2Ids.addAll(res);
			}
		}
		
		col1Unique.addAll(col1Ids);
		col1Unique.removeAll(col2Ids);
		col2Unique.addAll(col2Ids);
		col2Unique.removeAll(col1Ids);
	}
	
	
	
	public String getDsSystemCode() {
		return dsSystemCode;
	}

	public void setDsSystemCode(String dsSystemCode) {
		this.dsSystemCode = dsSystemCode;
	}

	public Set<Xref> getCol1Ids() {
		return col1Ids;
	}

	public Set<Xref> getCol2Ids() {
		return col2Ids;
	}

	public Set<Xref> getCol1Unique() {
		return col1Unique;
	}

	public Set<Xref> getCol2Unique() {
		return col2Unique;
	}
}
