package mku.wp.scripts;

import java.io.File;
import java.net.URL;

import org.pathvisio.core.model.Pathway;
import org.pathvisio.wikipathways.webservice.WSCurationTag;
import org.pathvisio.wikipathways.webservice.WSPathwayInfo;
import org.wikipathways.client.WikiPathwaysClient;

public class UpdatePathway {

	public static void main(String[] args) throws Exception {
		WikiPathwaysClient client = new WikiPathwaysClient(new URL("https://webservice.wikipathways.org"));
		client.login("", "");
		File dir = new File("C:/Users/martina.kutmon/owncloud/Projects/WikiPathways/2018-01-19-nestedGroupFix/output/checked");
		for(File f : dir.listFiles()) {
			String id = f.getName().replace(".gpml", "");
			System.out.println(id);
			WSPathwayInfo i = client.getPathwayInfo(id);
			Pathway p = new Pathway();
			p.readFromXml(f, true);
			String name = p.getMappInfo().getMapInfoName();
			if(i.getName().equals(name)) {
				WSCurationTag [] tags = client.getCurationTags(id);
				boolean curated = false;
				boolean featured = false;
				for(WSCurationTag t : tags) {
					if(t.getName().equals("Curation:AnalysisCollection")) {
						curated = true;
					}
					if(t.getName().equals("Curation:FeaturedPathway")) {
						featured = true;
					}
				}
				
				client.updatePathway(i.getId(), p, "Homology conversion update", Integer.parseInt(i.getRevision()));
				WSPathwayInfo info = client.getPathwayInfo(i.getId());
				System.out.println(curated + "\t" + featured);
				if(curated) {
					client.saveCurationTag(info.getId(), "Curation:AnalysisCollection", "", Integer.parseInt(info.getRevision()));
				}
				if(featured) {
					client.saveCurationTag(info.getId(), "Curation:FeaturedPathway", "", Integer.parseInt(info.getRevision()));
				}
			} else {
				System.out.println("different name!\t" + name + "\t" + i.getName());
			}
		}

	}

}
