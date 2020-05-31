package milestone_one;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;

public class FirstMileController {

	private String path;
	private String repository;
	private String firstDate;
	private String lastDate;
	private String midDate;
	private String subPath;
	private int firstVersion = 1;
	private int midVersion;
	private int lastVersion;
	private String retrMode;

	static Logger logger = Logger.getAnonymousLogger();

	private static final String EXCEPTION_THROWN = "an exception was thrown";

	public String getPath() {
		return path;
	}

	public String getSubPath() {
		return subPath;
	}

	public String getRepository() {
		return repository;
	}

	public String getFirstDate() {
		return firstDate;
	}

	public void setFirstDate(String firstDate) {
		this.firstDate = firstDate;
	}

	public String getLastDate() {
		return lastDate;
	}

	public void setLastDate(String lastDate) {
		this.lastDate = lastDate;
	}

	public void setMidDate(String midDate) {
		this.midDate = midDate;
	}

	public String getMidDate() {
		return this.midDate;
	}

	public int getMidVersion() {
		return midVersion;
	}

	public void setMidVersion(int midVersion) {
		this.midVersion = midVersion;
	}

	public int getFirstVersion() {
		return firstVersion;
	}

	public int getLastVersion() {
		return lastVersion;
	}

	public void setLastVersion(int lastVersion) {
		this.lastVersion = lastVersion;
	}

	public String getRetrMode() {
		return retrMode;
	}

	public void setRetrMode(String retrMode) {
		this.retrMode = retrMode;
	}

	private void loadPath() {
		try (FileReader f = new FileReader("config.txt"); BufferedReader buff = new BufferedReader(f);) {
			this.path = buff.readLine();
			this.repository = buff.readLine();
			this.subPath = buff.readLine();
			this.setRetrMode(buff.readLine());
		} catch (IOException e) {
			logger.log(Level.SEVERE, EXCEPTION_THROWN, e);
		}
	}

	private void addInfoToFileList(List<Ticket> tckList, List<FileWithInfo> allFiles) {
		for (Ticket tkt : tckList) {
			try {
				List<String> commits = GitQuery.getInstance().listCommits(tkt.getKey());//closed tickets referred to some commits
				for (String commit : commits) {
					List<String> files = GitQuery.getInstance().listFiles(commit);//files changed in this commit
					iterateFilesAndAddInfo(files, allFiles, tkt);
				}
			} catch (InstantiationException e) {
				logger.log(Level.SEVERE, EXCEPTION_THROWN, e);
			}
		}
	}
	
	private void iterateFilesAndAddInfo(List<String> files, List<FileWithInfo> allFiles, Ticket tkt) {
		for (String file : files) {// files referred to a ticket
			for (FileWithInfo allFile : allFiles) {// map AV on file in allFiles list
				if (allFile.getFileName().equals(file)) {// searching the matching between files referred to
															// a ticket and files in allFiles list
					allFile.setInjVersion(tkt.getInjVersion());
					allFile.setLastAv(tkt.getLastAv());
					break;
				}
			}

		}
	}

	private List<FileWithInfo> retrieveAllFiles() {
		List<FileWithInfo> fileList = new ArrayList<>();
		try {
			List<ProjFile> pFiles = GitQuery.getInstance().listAllFiles();
			for (ProjFile pFile : pFiles) {
				int addVersion;
				int remVersion;
				String addDate;
				String remDate;
				String file;
				addDate = pFile.getAddDate();
				remDate = pFile.getRemDate();
				file = pFile.getFileName();
				FileWithInfo f = new FileWithInfo();
				f.setFileName(file);
				if (!remDate.equals("-1")) {
					remVersion = Utilities.getIdVersionFromDate(getSubPath().toUpperCase(), remDate);
					f.setRemVersion(remVersion);
				}
				if (!addDate.equals("-1")) {
					addVersion = Utilities.getIdVersionFromDate(getSubPath().toUpperCase(), addDate);
					f.setAddVersion(addVersion);
				}
				if (f.getAddVersion() != -1) {// add to the list only the files with addDate
					if (f.getRemVersion() == -1) {
						f.setRemVersion(getLastVersion());// if remVersion does not exist i set ideally it to last
															// current version
					}
					fileList.add(f);
				}
			}
		} catch (InstantiationException e) {
			logger.log(Level.SEVERE, EXCEPTION_THROWN, e);
		}
		return fileList;

	}

	private List<CSVEntry> createCSVEntryList(List<FileWithInfo> fileList, FirstMileController main) {
		List<CSVEntry> csvEntryList = new ArrayList<>();
		String no = "NO";
		String yes = "YES";
		for (FileWithInfo file : fileList) {
			int addV = file.getAddVersion();
			int remV = file.getRemVersion();
			int injV = file.getInjVersion();
			int lastAV = file.getLastAv();
			String fileName = file.getFileName();
			for (int i = addV; i <= Math.min(remV, main.getMidVersion()); i++) {//I discard all version major of midVersion
				if (!(injV == -1 && lastAV == -1) && i >= Math.max(addV, injV) && i <= Math.min(remV, lastAV)) {// class is never buggy
					CSVEntry entry = new CSVEntry();
					entry.setBuggy(yes);
					entry.setFileName(fileName);
					entry.setVersion((Integer) i);
					csvEntryList.add(entry);
				} else {// not buggy in range([addV, remV] - [IV, FV-1])
					CSVEntry entry = new CSVEntry();
					entry.setBuggy(no);
					entry.setFileName(fileName);
					entry.setVersion((Integer) i);
					csvEntryList.add(entry);
				}
			}
		}
		Collections.sort(csvEntryList, (CSVEntry o1, CSVEntry o2) -> o1.getVersion().compareTo(o2.getVersion()));
		return csvEntryList;
	}

	private void mergeMetrics(List<CSVEntry> entryList, List<CSVEntry> metricsEntryList) {
		for (CSVEntry entry : entryList) {
			CSVEntry metricsEntry = Metrics.findObjInList(entry.getVersion(), entry.getFileName(), metricsEntryList);
			if (metricsEntry != null) {// if there is matching I merge info
				entry.setLocAdded(metricsEntry.getLocAdded());
				entry.setMaxLocAdded(metricsEntry.getMaxLocAdded());
				entry.setAvgLocAdded(metricsEntry.getAvgLocAdded());
				entry.setChurn(metricsEntry.getChurn());
				entry.setMaxChurn(metricsEntry.getMaxChurn());
				entry.setAvgChurn(metricsEntry.getAvgChurn());
				entry.setNR(metricsEntry.getNR());
				entry.setChgSet(metricsEntry.getChgSet());
				entry.setMaxChgSet(metricsEntry.getMaxChgSet());
				entry.setAvgChgSet(metricsEntry.getAvgChgSet());
			}
		}
		for (CSVEntry metricsEntry : metricsEntryList) {
			CSVEntry entry = Metrics.findObjInList(metricsEntry.getVersion(), metricsEntry.getFileName(), entryList);
			if (entry == null) {// if there is no matching i add the metricsEntry to the final list
				entryList.add(metricsEntry);
			}
		}
		Collections.sort(entryList, (CSVEntry o1, CSVEntry o2) -> o1.getVersion().compareTo(o2.getVersion()));
	}

	public static void main(String[] args) throws FileNotFoundException {
		/*
		 * args[1] represent the source of all files retrieving, if is "file" from file,
		 * else from git first date -> final date, mean date -> mean version, JIRA query
		 * on all tickets (closed, bug), analyze JSON file response (VERSION[list of
		 * AV], CREATED VERSION[OV], ),
		 */
		FirstMileController firstMileController = new FirstMileController();
		firstMileController.loadPath();
		String retrMode = firstMileController.getRetrMode();
		if (retrMode != null && !retrMode.equals("file")) {// if retrMode is null the file list is computed using git CLI, else it uses the
			logger.log(Level.SEVERE, "Insert 'file' or nothing in config.txt");// cache file
			return;				
		}
		File cachedFiles = new File(firstMileController.getSubPath().toUpperCase() + "Files.csv");
		if (retrMode != null && (retrMode.equals("file") && !(cachedFiles.exists() && cachedFiles.isFile()))) {// check if the cachedFile exist
			throw new FileNotFoundException();
		}
		RetrieveTickets rtvTkt = new RetrieveTickets();

		GitQuery gitQuery = null;
		try {
			gitQuery = GitQuery.create(firstMileController.getPath(), firstMileController.getSubPath(), firstMileController.getRepository(), retrMode);
		} catch (InstantiationException e1) {
			logger.log(Level.SEVERE, EXCEPTION_THROWN, e1);
		}
		if(gitQuery == null) {
			throw new NullPointerException();
		}
		gitQuery.gitClone();
		gitQuery.gitPull();
		firstMileController.setFirstDate(gitQuery.firstCommit());
		firstMileController.setLastDate(gitQuery.lastCommit());
		try {
			GetReleaseInfo.getRelease(firstMileController.getSubPath().toUpperCase());// create CSV with releases info
		} catch (JSONException | IOException e) {
			logger.log(Level.SEVERE, EXCEPTION_THROWN, e);
		}
		firstMileController.setMidDate(Utilities.calcMidDate(firstMileController.getFirstDate(), firstMileController.getLastDate()));
		firstMileController.setLastVersion(Utilities.getLastVersion(firstMileController.getSubPath().toUpperCase()));
		List<Ticket> ticketList = null;
		try {
			firstMileController.setMidVersion(Utilities.getIdVersionFromDate(firstMileController.getSubPath().toUpperCase(), firstMileController.getMidDate()));
		} catch (Exception e1) {
			logger.log(Level.SEVERE, EXCEPTION_THROWN, e1);
		}
		try {// tickets list
			ticketList = rtvTkt.retrieveTicketsIDs(firstMileController.getSubPath().toUpperCase(), firstMileController.getMidVersion());
		} catch (Exception e) {
			logger.log(Level.SEVERE, EXCEPTION_THROWN, e);
		}
		List<FileWithInfo> fileList = firstMileController.retrieveAllFiles();
		firstMileController.addInfoToFileList(ticketList, fileList);
		List<CSVEntry> entryList = firstMileController.createCSVEntryList(fileList, firstMileController);
		// compute the metrics
		SortedMap<Integer, List<String>> versionCommitsMap = Metrics
				.listAllCommitsPerVersion(firstMileController.getSubPath().toUpperCase(), firstMileController.getMidVersion());
		List<CSVEntry> metricsEntryList = Metrics.computeMetrics(versionCommitsMap);
		// merge with metrics
		firstMileController.mergeMetrics(entryList, metricsEntryList);
		Utilities.buildCsv(entryList, firstMileController.getSubPath().toUpperCase());
	}

}
