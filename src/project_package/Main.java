package project_package;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;

public class Main {
	
	private String path;
	private String repository;
	private String firstDate;
	private String lastDate;
	private String midDate;
	private String subPath;
	private int firstVersion = 1;
	private int midVersion;
	private int lastVersion;
	
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
	
	private void loadPath(){		
		try (
			FileReader f = new FileReader("config.txt");
			BufferedReader buff = new BufferedReader(f);
			){
			this.path = buff.readLine();
			this.repository = buff.readLine();
			this.subPath = buff.readLine();
		} catch (IOException e) {
			logger.log(Level.SEVERE, EXCEPTION_THROWN, e);
		} 
	}
	
	private String calcMidDate(String firstDate, String lastDate) {
		Date startdate = null;
		Date enddate = null;
		try {
			startdate = new SimpleDateFormat("yyyy-MM-dd").parse(firstDate);
			enddate = new SimpleDateFormat("yyyy-MM-dd").parse(lastDate);
		} catch (ParseException e) {
			logger.log(Level.SEVERE, EXCEPTION_THROWN, e);
		}
		Date middate = new Date((startdate.getTime() + enddate.getTime()) / 2);
		return new SimpleDateFormat("yyyy-MM-dd").format(middate);
	}
	
	private void addInfoToFileList(List<Ticket> tckList, List<File> allFiles){
		for(Ticket tkt : tckList) {
			try {
				List<String> commits = GitQuery.GetInstance().listCommits(tkt.getKey());
				for(String commit : commits) {
					List<String> files = GitQuery.GetInstance().listFiles(commit);
					for(String file : files) {//files referred to a ticket 
						//System.out.println("Aff fileName: " + file);
						for(File allFile : allFiles) {//map AV on file in allFiles list
							if(allFile.getFileName().equals(file)) {//searching the matching between files referred to a ticket and files in allFiles list
								allFile.setInjVersion(tkt.getInjVersion());
								allFile.setLastAv(tkt.getLastAv());
								//System.out.println("Name: " + allFile.getFileName() + " add: " + allFile.getAddVersion() + " inj: " + allFile.getInjVersion() + " lav: " +
								//allFile.getLastAv() + " rem: " + allFile.getRemVersion());
								break;
							}
						}
						
					}
				}
			} catch (InstantiationException e) {
				logger.log(Level.SEVERE, EXCEPTION_THROWN, e);
			}
		}	
	}
	
	private List<File> retrieveAllFiles(){
		List<File> fileList = new ArrayList<>();
		try {
			List<ProjFile> pFiles = GitQuery.GetInstance().listAllFiles();
			for(ProjFile pFile : pFiles) {
				int addVersion, remVersion;
				String addDate, remDate, file;
				addDate = pFile.getAddDate();
				remDate = pFile.getRemDate();
				file = pFile.getFileName();
				File f = new File();
				f.setFileName(file);
				if(!remDate.equals("-1")) {
					remVersion = Utilities.getIdVersionFromDate(getSubPath().toUpperCase(), remDate);
					f.setRemVersion(remVersion);
				}
				if(!addDate.equals("-1")) {
					addVersion = Utilities.getIdVersionFromDate(getSubPath().toUpperCase(), addDate);
					f.setAddVersion(addVersion);					
				}
				if(f.getAddVersion() != -1) {//add to the list only the files with addDate
					if(f.getRemVersion() == -1) {
						f.setRemVersion(getLastVersion());//if remVersion does not exist i set ideally it to last current version
					}
					fileList.add(f);
					//System.out.println("tutti i file: " + f.getFileName());
				}
			}
		} catch (InstantiationException e) {
			logger.log(Level.SEVERE, EXCEPTION_THROWN, e);
		}
		return fileList;
		
	}
	
	private List<CSVEntry> createCSVEntryList(List<File> fileList) {
		List<CSVEntry> csvEntryList = new ArrayList<>();
		String no = "NO";
		String yes = "YES";
		for(File file : fileList) {
			int addV = file.getAddVersion();
			int remV = file.getRemVersion();
			int injV = file.getInjVersion();
			int lastAV = file.getLastAv();
			String fileName = file.getFileName();
			for(int i = addV; i <= remV; i++) {
				if(injV == -1 && lastAV == -1) {//class is never buggy
					CSVEntry entry = new CSVEntry();
					entry.setBuggy(no);
					entry.setFileName(fileName);
					entry.setVersion((Integer)i);
					csvEntryList.add(entry);
				}else if(i >= Math.max(addV, injV) && i <= Math.min(remV, lastAV)) {//class is buggy range(IV, FV-1)
					CSVEntry entry = new CSVEntry();
					entry.setBuggy(yes);
					entry.setFileName(fileName);
					entry.setVersion((Integer)i);
					csvEntryList.add(entry);
				}else {//not buggy in range([addV, remV] - [IV, FV-1])
					CSVEntry entry = new CSVEntry();
					entry.setBuggy(no);
					entry.setFileName(fileName);
					entry.setVersion((Integer)i);
					csvEntryList.add(entry);
				}
			}
		}
		Collections.sort(csvEntryList, new Comparator<CSVEntry>() {
		    @Override
		    public int compare(CSVEntry o1, CSVEntry o2) {
		        return o1.getVersion().compareTo(o2.getVersion());
		    }
		});
		return csvEntryList;
	}

	private void mergeMetrics(List<CSVEntry> entryList, List<CSVEntry> metricsEntryList) {
		for(CSVEntry entry : entryList) {
			CSVEntry metricsEntry = Metrics.findObjInList(entry.getVersion(), entry.getFileName(), metricsEntryList);
			if(metricsEntry != null) {//if there is matching i merge info
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
		for(CSVEntry metricsEntry : metricsEntryList) {
			CSVEntry entry = Metrics.findObjInList(metricsEntry.getVersion(), metricsEntry.getFileName(), entryList);
			if(entry == null) {//if there is no matching i add the metricsEntry to the final list 
				entryList.add(metricsEntry);
			}
		}
		Collections.sort(entryList, new Comparator<CSVEntry>() {
		    @Override
		    public int compare(CSVEntry o1, CSVEntry o2) {
		        return o1.getVersion().compareTo(o2.getVersion());
		    }
		});
	}

	public static void main(String[] args) {
		/*
		 * args[1] represent the source of all files retrieving, if is "file" from file, else from git
		 * first date -> final date, mean date -> mean version, JIRA query on all tickets (closed, bug),
		 * analyze JSON file response (VERSION[list of AV], CREATED VERSION[OV], ), */
		System.out.println( args.length);
		if(args.length > 1) {
			logger.log(Level.SEVERE,"Insert 'file' or nothing");
			return;
		}
		if(args.length == 1 && !args[0].equals("file")) {
			logger.log(Level.SEVERE,"Insert 'file' or nothing");
			return;
		}
		String retrMode = null;
		if(args.length == 1) {
			retrMode = args[0];
		}
		Main main = new Main();
		//Utilities util = new Utilities();
		RetrieveTickets rtvTkt = new RetrieveTickets();
		main.loadPath();
		GitQuery gitQuery = null;
		try {
			gitQuery = GitQuery.Create(main.getPath(), main.getSubPath(), main.getRepository(), retrMode);
		} catch (InstantiationException e1) {
			logger.log(Level.SEVERE, EXCEPTION_THROWN, e1);
		}
		gitQuery.gitClone();
		gitQuery.gitPull();
		main.setFirstDate(gitQuery.firstCommit());
		main.setLastDate(gitQuery.lastCommit());
		main.setMidDate(main.calcMidDate(main.getFirstDate(), main.getLastDate()));
		main.setLastVersion(Utilities.getLastVersion(main.getSubPath().toUpperCase()));
		//System.out.println(main.getFirstDate()+ " " + main.getMidDate());

		try {
			GetReleaseInfo.getRelease(main.getSubPath().toUpperCase());//create CSV with releases info
		} catch (JSONException | IOException e) {
			logger.log(Level.SEVERE, EXCEPTION_THROWN, e);
		}
		List<Ticket> ticketList = null;
		try {
			main.setMidVersion(Utilities.getIdVersionFromDate(main.getSubPath().toUpperCase(), main.getMidDate()));
		} catch (Exception e1) {
			logger.log(Level.SEVERE, EXCEPTION_THROWN, e1);
		}
		try {//tickets list 
			ticketList = rtvTkt.retrieveTicketsIDs(main.getSubPath().toUpperCase(), main.getMidVersion());
		} catch (Exception e) {
			logger.log(Level.SEVERE, EXCEPTION_THROWN, e);
		}
		List<File> fileList = main.retrieveAllFiles();
		main.addInfoToFileList(ticketList, fileList);
		List<CSVEntry> entryList = main.createCSVEntryList(fileList);
		//compute the metrics
		TreeMap<Integer,List<String>> versionCommitsMap = Metrics.listAllCommitsPerVersion(main.getSubPath().toUpperCase());
		List<CSVEntry> metricsEntryList = Metrics.computeMetrics(versionCommitsMap);
		//merge with metrics
		main.mergeMetrics(entryList, metricsEntryList);
		Utilities.buildCsv(entryList, main.getSubPath().toUpperCase());
		/*for(CSVEntry entry : entryList) {
			if(entry.getBuggy() == "YES")
				System.out.println(entry.getVersion() + " File: " + entry.getFileName() + " Buggy: " + entry.getBuggy());
		}*/
	}
}
