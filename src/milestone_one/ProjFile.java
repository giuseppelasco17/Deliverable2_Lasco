package milestone_one;

public class ProjFile {
	private String fileName;
	private String addDate;
	private String remDate;

	public ProjFile(String fileName, String addDate, String remDate) {
		this.fileName = fileName;
		this.remDate = remDate;
		this.addDate = addDate;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getRemDate() {
		return remDate;
	}

	public void setRemDate(String remDate) {
		this.remDate = remDate;
	}

	public String getAddDate() {
		return addDate;
	}

	public void setAddDate(String addDate) {
		this.addDate = addDate;
	}

}
