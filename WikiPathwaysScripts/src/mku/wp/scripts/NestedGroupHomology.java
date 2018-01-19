package mku.wp.scripts;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement.Comment;
import org.pathvisio.wikipathways.webservice.WSPathway;
import org.wikipathways.client.WikiPathwaysClient;

public class NestedGroupHomology {

	public static void main(String[] args) throws Exception {
		WikiPathwaysClient client = new WikiPathwaysClient(new URL("https://webservice.wikipathways.org"));
		
		File f = new File("resources/pathways-nested-groups.txt");
		BufferedReader reader = new BufferedReader(new FileReader(f));
		String line;
		
		String output = "";
		while((line = reader.readLine()) != null) {
			String id = line;
			WSPathway wsp = client.getPathway(id);
			String pathwayId = wsp.getId();
			String pathwayRev = wsp.getRevision();
			String species = wsp.getSpecies();
			
			Pathway p = new Pathway();
			p.readFromXml(new ByteArrayInputStream(wsp.getGpml().getBytes("UTF-8")), false);
			String sourcePathway = "";
			for(Comment c : p.getMappInfo().getComments()) {
				if(sourcePathway.equals("")) {
					if(c.getSource() != null && c.getSource().contains("Homology")) {
						String comment = c.getComment();
						{
							// check for new URL pattern
							Pattern pattern = Pattern.compile("(WP[0-9]+_r[0-9]+)");
							Matcher matcher = pattern.matcher(comment);
							while (matcher.find()) {
								sourcePathway = matcher.group();
							}
						}
						if(sourcePathway.equals("")) {
							// check for old URL pattern
							Pattern pattern = Pattern.compile("(WP[0-9]+[(]r[0-9]+)");
							Matcher matcher = pattern.matcher(comment);
							while (matcher.find()) {
								sourcePathway = matcher.group().replace("(", "_");
							}
						}
					}
				}
			}
			
			String orgId = "";
			String orgRev = "";
			String orgSpecies = "";
			if(!sourcePathway.equals("")) {
				String [] buffer = sourcePathway.split("_");
				WSPathway i = client.getPathway(buffer[0]);
				orgId = i.getId();
				orgRev = i.getRevision();
				orgSpecies = i.getSpecies();
				Pathway orgPathway = new Pathway();
				orgPathway.readFromXml(new ByteArrayInputStream(i.getGpml().getBytes("UTF-8")), false);
				orgPathway.writeToXml(new File("C:/Users/martina.kutmon/owncloud/Projects/WikiPathways/2018-01-19-nestedGroupFix/" + species.replace(" ", "_") + "/" + orgId + "_" + orgRev + ".gpml"), false);
				
			}
			output = output + "\n" + pathwayId + "\t" + pathwayRev + "\t" + species + "\t" + orgId + "\t" + orgRev + "\t" + orgSpecies;
		}
		System.out.println(output);
		reader.close();
	}

}
