package milestone_one;

public class CSVEntry {

	private Integer version;
	private String fileName;
	private Double locAdded = Double.valueOf(0);
	private Double maxLocAdded = Double.valueOf(0);
	private Double avgLocAdded = Double.valueOf(0);
	private Double churn = Double.valueOf(0);
	private Double maxChurn = Double.valueOf(0);
	private Double avgChurn = Double.valueOf(0);
	private Double nR = Double.valueOf(0);
	private Double chgSet = Double.valueOf(0);
	private Double maxChgSet = Double.valueOf(0);
	private Double avgChgSet = Double.valueOf(0);
	private String buggy = "NO";

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getBuggy() {
		return buggy;
	}

	public void setBuggy(String buggy) {
		this.buggy = buggy;
	}

	public Double getLocAdded() {
		return locAdded;
	}

	public Double getAvgLocAdded() {
		return avgLocAdded;
	}

	public Double getNR() {
		return nR;
	}

	public Double getMaxLocAdded() {
		return maxLocAdded;
	}

	public void setAvgLocAdded(Double avgLocAdded) {
		this.avgLocAdded = avgLocAdded;

	}

	public void setLocAdded(Double locAdded) {
		this.locAdded = locAdded;
	}

	public void setMaxLocAdded(Double maxLocAdded) {
		this.maxLocAdded = maxLocAdded;
	}

	public void setNR(Double nR) {
		this.nR = nR;
	}

	public Double getChurn() {
		return churn;
	}

	public Double getAvgChurn() {
		return avgChurn;
	}

	public Double getMaxChurn() {
		return maxChurn;
	}

	public void setAvgChurn(Double avgChurn) {
		this.avgChurn = avgChurn;
	}

	public void setChurn(Double churn) {
		this.churn = churn;
	}

	public void setMaxChurn(Double maxChurn) {
		this.maxChurn = maxChurn;
	}

	public Double getAvgChgSet() {
		return avgChgSet;
	}

	public Double getChgSet() {
		return chgSet;
	}

	public Double getMaxChgSet() {
		return maxChgSet;
	}

	public void setChgSet(Double chgSet) {
		this.chgSet = chgSet;
	}

	public void setAvgChgSet(Double avgChgSet) {
		this.avgChgSet = avgChgSet;
	}

	public void setMaxChgSet(Double maxChgSet) {
		this.maxChgSet = maxChgSet;
	}
}
