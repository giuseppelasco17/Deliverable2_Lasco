package milestone_one;

public class FileWithInfo {

	private String fileName;
	private int addVersion = -1;// if addVersion == -1 the file will be discarded
	private int remVersion = -1;// if file is not deleted yet remVersion is set to -1
	private int injVersion = -1;// if injVersion and lasvAv are -1 the file is never buggy
	private int lastAv = -1;

	public int getAddVersion() {
		return addVersion;
	}

	public void setAddVersion(int addVersion) {
		this.addVersion = addVersion;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getRemVersion() {
		return remVersion;
	}

	public void setRemVersion(int remVersion) {
		this.remVersion = remVersion;
	}

	public int getInjVersion() {
		return injVersion;
	}

	public void setInjVersion(int injVersion) {
		this.injVersion = injVersion;
	}

	public int getLastAv() {
		return lastAv;
	}

	public void setLastAv(int lastAv) {
		this.lastAv = lastAv;
	}
}
