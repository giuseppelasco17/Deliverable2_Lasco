package project_package;

public class Ticket {
	
	private String key;
	private int fixedVersion;
	private int injVersion;
	private int openVersion;
	private int lastAv;
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public int getFixedVersion() {
		return fixedVersion;
	}
	public void setFixedVersion(int fixedVersion) {
		this.fixedVersion = fixedVersion;
	}
	public int getInjVersion() {
		return injVersion;
	}
	public void setInjVersion(int injVersion) {
		this.injVersion = injVersion;
	}
	public int getOpenVersion() {
		return openVersion;
	}
	public void setOpenVersion(int openVersion) {
		this.openVersion = openVersion;
	}
	public int getLastAv() {
		return lastAv;
	}
	public void setLastAv(int lastAv) {
		this.lastAv = lastAv;
	}
}
