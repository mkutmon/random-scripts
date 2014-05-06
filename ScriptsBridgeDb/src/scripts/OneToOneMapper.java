package scripts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import org.bridgedb.BridgeDb;
import org.bridgedb.DataSource;
import org.bridgedb.IDMapper;
import org.bridgedb.IDMapperException;
import org.bridgedb.Xref;

public class OneToOneMapper {

	public static void main(String[] args) throws ClassNotFoundException, IOException, IDMapperException {
		// define input sys code
		String sysCodeIn = "S";
		// define output sys code
		String sysCodeOut = "En";
		
		// file containing identifiers to be mapped
		File file = new File("/home/martina/input.txt");
		// bridgedb database file
		File bridgedb = new File("/home/martina/Hs_Derby_20130701.bridge");
	
		// define output file (contains all mapped identifiers)
		File output = new File(file.getParentFile(),"output-mapping.txt");
		BufferedWriter writer = new BufferedWriter(new FileWriter(output));

		
		// BridgeDb setup
		Class.forName("org.bridgedb.rdb.IDMapperRdb");  
		IDMapper mapper = BridgeDb.connect("idmapper-pgdb:" + bridgedb.getAbsolutePath());
		
		// read input file line by line (no header, every line contains one identifier)
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		while((line = reader.readLine()) != null) {
			// map each identifier (one identifier per line) to the output data source
			Set<Xref> result = mapper.mapID(new Xref(line, DataSource.getBySystemCode(sysCodeIn)), DataSource.getBySystemCode(sysCodeOut));
			for(Xref x : result) {
				// write out mapped identifier in output file
				writer.write(x.getId() + "\n");
			}
		}
		reader.close();
		writer.close();
	}

}
