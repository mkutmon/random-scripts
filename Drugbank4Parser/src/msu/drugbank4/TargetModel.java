package msu.drugbank4;


import java.util.HashSet;
import java.util.Set;

public class TargetModel {

	private String uniprotId = "";
	private String name = "";
	private String geneName = "";
	private String drugbankId = "";
	private String organism = "";
	private Set<String> xrefs;
	
	public TargetModel() {
		xrefs = new HashSet<String>();
	}

	public String getUniprotId() {
		return uniprotId;
	}

	public void setUniprotId(String uniprotId) {
		this.uniprotId = uniprotId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGeneName() {
		return geneName;
	}

	public void setGeneName(String geneName) {
		this.geneName = geneName;
	}

	public String getDrugbankId() {
		return drugbankId;
	}

	public void setDrugbankId(String drugbankId) {
		this.drugbankId = drugbankId;
	}

	public String getOrganism() {
		return organism;
	}

	public void setOrganism(String organism) {
		this.organism = organism;
	}

	public Set<String> getXrefs() {
		return xrefs;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("[target:").append(" id=")
				.append(uniprotId).append(", name=").append(name)
				.append(", organism=").append(organism)
				.append(", geneName=").append(geneName).append("]");
		return sb.toString();
	}
}
