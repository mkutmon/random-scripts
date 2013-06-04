import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.pathvisio.wikipathways.webservice.WSPathway;
import org.wikipathways.client.WikiPathwaysClient;


public class OnttagUpdater {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		try { 
			//Create a client to the WikiPathways web service
			WikiPathwaysClient client = new WikiPathwaysClient(
					new URL("http://www.wikipathways.org/wpi/webservice/webservice.php")	
			);
			
			// xml file reader
			SAXBuilder builder  = new SAXBuilder(false);
			
			// current pathway and term
			// TODO: read from spreadsheet
			String term = "ACE inhibitor drug pathway";
			String id = "PW:0001228";
			String ontology = "Pathway Ontology";
			String pwId = "WP468";

			// TODO: loop over all pathways to add ontology term
			
			System.out.println("Downloading pathway " + pwId);
			
			// get current pathway from wikipathways
			WSPathway wsPathway = client.getPathway(pwId);
			
			// read jdom tree
			InputStream is = new ByteArrayInputStream(wsPathway.getGpml().getBytes());
			System.out.println("Opening pathway GPML file");
			Document doc = builder.build(is);
			
			Element root = doc.getRootElement();
			if (root.getName().equals("Pathway")) {
				
				// check if Biopax Element is present
				Element biopax = null;
				for(Object o : root.getChildren()) {
					Element e = (Element)o;
					if(e.getName().equals("Biopax")) {
						System.out.println("Biopax element exists.");
						biopax = e;
					}
				}
				
				// if Biopax Element is missing add new element
				if(biopax == null) {
					System.out.println("New Biopax element was created.");
					biopax = new Element("Biopax", Namespace.getNamespace("http://pathvisio.org/GPML/2013a"));
					root.addContent(biopax);
				}
				
				// check if ontology term is already present in pathway
				Namespace bp = Namespace.getNamespace("bp", "http://www.biopax.org/release/biopax-level3.owl#");
				List<Element> vocabularies = biopax.getChildren("openControlledVocabulary", bp);
				
				boolean found = false;
				for(Element voc : vocabularies) {
					Element t = voc.getChild("TERM", bp);
					if(t.getValue().equals(term)) {
						found = true;
						break;
					}
				}
				
				if(found) {
					// 
					System.out.println("Pathway is already annotated with term " + term + ".");
				} else {
					Element voc = new Element("openControlledVocabulary", bp);
					Element eterm = new Element("TERM", bp);
					eterm.setText(term);
					Element eID = new Element("ID", bp);
					eID.setText(id);
					Element eOnt = new Element("Ontology", bp);
					eOnt.setText(ontology);
					voc.addContent(eterm);
					voc.addContent(eID);
					voc.addContent(eOnt);
					biopax.addContent(voc);
					System.out.println("Update pathway with annotation: " + term + ".");
				}
			
				
				File output = new File("output.gpml");
				// writing out
				XMLOutputter xmlcode = new XMLOutputter(Format.getPrettyFormat());

				OutputStream out = new FileOutputStream(output);
				//Send XML code to the outputstream
				xmlcode.output(doc, out);
			} else {
				System.out.println("Invalid GPML file");
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
