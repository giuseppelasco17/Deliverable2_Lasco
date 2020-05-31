package milestone_one;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GitQuery {// Singleton

	private static GitQuery instance = null;

	private static final String CMD = "git -C ";

	private static final String EXCEPTION_THROWN = "an exception was thrown";
	
	private static final String PATH_DEL = "\\";

	static Logger logger = Logger.getAnonymousLogger();

	private String path;

	private String subPath;

	private String repository;

	private String retrMode;

	private GitQuery(String path, String subPath, String repository, String retrMode) {
		this.path = path;
		this.subPath = subPath;
		this.repository = repository;
		this.retrMode = retrMode;
	}

	public static GitQuery getInstance() throws InstantiationException {
		if (instance == null)
			throw new InstantiationException("GitQuery instance not created !");
		return instance;
	}

	public static GitQuery create(String path, String subPath, String repository, String retrMode)
			throws InstantiationException {
		if (instance != null)
			throw new InstantiationException("GitQuery instance already created !");

		instance = new GitQuery(path, subPath, repository, retrMode);
		return instance;
	}

	public String logFilter(String ticket) {
		String tkt = null;
		try {
			// First line represent the last commit date related to the ticket
			String pathComplete = this.path + PATH_DEL + this.subPath;
			Process p = Runtime.getRuntime()
					.exec(CMD + pathComplete + " log -1 --pretty=format:\"%cs\" --grep=" + ticket);
			p.waitFor();
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			tkt = stdInput.readLine();
		} catch (IOException | InterruptedException e) {
			logger.log(Level.SEVERE, EXCEPTION_THROWN, e);
			// Restore interrupted state...
			Thread.currentThread().interrupt();
			System.exit(-1);
		}
		return tkt;
	}

	public List<String> listCommits(String key) {
		List<String> tktList = new ArrayList<>();
		try {
			// First line represent the last commit date related to the ticket
			String pathComplete = this.path + PATH_DEL + this.subPath;
			Process p = Runtime.getRuntime()
					.exec(CMD + pathComplete + " --no-pager log --pretty=format:\"%H\" --grep=" + key);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = stdInput.readLine()) != null) {
				tktList.add(line);
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, EXCEPTION_THROWN, e);
			// Restore interrupted state...
			Thread.currentThread().interrupt();
			System.exit(-1);
		}

		return tktList;
	}

	public List<String> listFiles(String commit) {
		List<String> fileList = new ArrayList<>();
		try {
			// First line represent the last commit date related to the ticket
			String pathComplete = this.path + PATH_DEL + this.subPath;
			Process p = Runtime.getRuntime().exec(
					CMD + pathComplete + " --no-pager diff-tree --no-commit-id --name-only -r " + commit + " *.java");
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = stdInput.readLine()) != null) {
				fileList.add(line);
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, EXCEPTION_THROWN, e);
			
			// Restore interrupted state...
			Thread.currentThread().interrupt();
			System.exit(-1);
		}
		return fileList;
	}

	public List<ProjFile> listAllFiles() {
		List<ProjFile> fileList = new ArrayList<>();
		if (retrMode == null) {// while retrieving from git, it builds the file
			File file = new File(this.subPath.toUpperCase() + "Files.csv");
			List<String> rawFiles = new ArrayList<>();
			try {
				if (!file.createNewFile()) {
					logger.info("file exist");
				}
			} catch (IOException e1) {
				logger.log(Level.SEVERE, EXCEPTION_THROWN, e1);
			}
			try (PrintWriter buff = new PrintWriter(new FileWriter(file))) {
				// First line represent the last commit date related to the ticket
				String pathComplete = this.path + PATH_DEL + this.subPath;
				Process p = Runtime.getRuntime()
						.exec(CMD + pathComplete + " --no-pager log --pretty=format:\"\" --name-only *.java");
				BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line;
				
				String prevLine = "";
				while ((line = stdInput.readLine()) != null || prevLine != null) {
					if (line != null && !line.equals(""))
						rawFiles.add(line);
					prevLine = line;
				}
				List<String> filteredFiles = new ArrayList<>(new LinkedHashSet<>(rawFiles));// remove the
																										// duplicated
																										// lines
				addDatesAndPrintOnFile(filteredFiles, fileList, buff);
			} catch (IOException e) {
				logger.log(Level.SEVERE, EXCEPTION_THROWN, e);
				// Restore interrupted state...
				Thread.currentThread().interrupt();
				System.exit(-1);
			} 
		} else {// if the reading is from file
			readFromFile(fileList);
		}
		return fileList;
	}
	
	private void readFromFile(List<ProjFile> fileList) {
		try (BufferedReader buff = new BufferedReader(new FileReader(this.subPath.toUpperCase() + "Files.csv"))) {
			String line;
			String[] fileByLine;
			while ((line = buff.readLine()) != null) {
				fileByLine = line.split(",");
				ProjFile projFile = new ProjFile(fileByLine[0], fileByLine[1], fileByLine[2]);
				fileList.add(projFile);
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, EXCEPTION_THROWN, e);
		} 
	}

	private void addDatesAndPrintOnFile(List<String> filteredFiles, List<ProjFile> fileList, PrintWriter buff) {
		for (String filtLine : filteredFiles) {
			String addDate;
			String remDate;
			if ((addDate = retrieveDate(filtLine, "A")) == null)// if addDate does not exist
				addDate = "-1";// useful when the reading is from file
			if ((remDate = retrieveDate(filtLine, "D")) == null)// if remDate does not exist
				remDate = "-1";
			ProjFile projFile = new ProjFile(filtLine, addDate, remDate);
			fileList.add(projFile);
			buff.println(filtLine + "," + addDate + "," + remDate);
		}
	}

	public String retrieveDate(String file, String type) {
		String date = null;
		try {
			// First line represent the last commit date related to the ticket
			String pathComplete = this.path + PATH_DEL + this.subPath;
			Process p = Runtime.getRuntime()
					.exec(CMD + pathComplete + " log --diff-filter=" + type + " --pretty=format:\"%as\" -- " + file);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			date = stdInput.readLine();
		} catch (IOException e) {
			logger.log(Level.SEVERE, EXCEPTION_THROWN, e);
			// Restore interrupted state...
			Thread.currentThread().interrupt();
			System.exit(-1);
		}
		return date;
	}

	public String firstCommit() {
		String date = null;
		try {
			// First line represent the first commit date
			String pathComplete = this.path + PATH_DEL + this.subPath;
			Process p = Runtime.getRuntime().exec(CMD + pathComplete + " log --reverse --pretty=format:%cs");
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			date = stdInput.readLine();
		} catch (IOException e) {
			logger.log(Level.SEVERE, EXCEPTION_THROWN, e);
			// Restore interrupted state...
			Thread.currentThread().interrupt();
			System.exit(-1);
		}
		return date;
	}

	public List<String> listAllCommitsWithDates() {
		List<String> commitList = new ArrayList<>();
		try {
			// First line represent the last commit date related to the ticket
			String pathComplete = this.path + PATH_DEL + this.subPath;
			Process p = Runtime.getRuntime()
					.exec(CMD + pathComplete + " --no-pager log --pretty=format:\"%cs,%H\" --reverse");
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = stdInput.readLine()) != null) {
				commitList.add(line);
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, EXCEPTION_THROWN, e);
			// Restore interrupted state...
			Thread.currentThread().interrupt();
			System.exit(-1);
		}
		return commitList;
	}

	public List<String> gitDiff(String commitBefore, String commitAfter) {
		List<String> filesPlusAttrList = new ArrayList<>();
		try {
			// First line represent the last commit date related to the ticket
			String pathComplete = this.path + PATH_DEL + this.subPath;
			Process p = Runtime.getRuntime().exec(
					CMD + pathComplete + " --no-pager diff --numstat " + commitBefore + " " + commitAfter + " *.java");
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = stdInput.readLine()) != null) {
				filesPlusAttrList.add(line);
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, EXCEPTION_THROWN, e);
			// Restore interrupted state...
			Thread.currentThread().interrupt();
			System.exit(-1);
		}
		return filesPlusAttrList;
	}

	public String lastCommit() {
		String date = null;
		try {
			// First line represent the last commit date
			String pathComplete = this.path + PATH_DEL + this.subPath;
			Process p = Runtime.getRuntime().exec(CMD + pathComplete + " log --pretty=format:%cs -1");
			p.waitFor();
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			date = stdInput.readLine();
		} catch (IOException | InterruptedException e) {
			logger.log(Level.SEVERE, EXCEPTION_THROWN, e);
			// Restore interrupted state...
			Thread.currentThread().interrupt();
			System.exit(-1);
		}
		return date;
	}

	public void gitClone() {
		Process p;
		try {
			new File(this.path).mkdir();
			p = Runtime.getRuntime().exec(CMD + this.path + " clone " + repository);
			p.waitFor();
		} catch (IOException e) {
			logger.log(Level.SEVERE, EXCEPTION_THROWN, e);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, EXCEPTION_THROWN, e);
			Thread.currentThread().interrupt();
		}

	}

	public void gitPull() {
		Process p;
		try {
			p = Runtime.getRuntime().exec(CMD + this.path + " pull " + repository);
			p.waitFor();

		} catch (IOException e) {
			logger.log(Level.SEVERE, EXCEPTION_THROWN, e);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, EXCEPTION_THROWN, e);
			Thread.currentThread().interrupt();
		}

	}

//	public static void main(String[] args) {
//		try {
//			System.out.println(GitQuery.Create("C:\\Users\\Giuseppe\\Desktop\\bookkeeper", "bookkeeper", "https://github.com/apache/bookkeeper.git",null).logFilter("BOOKKEEPER-142:"));
//			String comm = GitQuery.GetInstance().listCommits("BOOKKEEPER-142:").get(0);
//			String file = GitQuery.GetInstance().listFiles(comm).get(0);
//			String date = GitQuery.GetInstance().retrieveDate(file, "A");
//			System.out.println(comm + " " + file + " " + date);		
//		} catch (InstantiationException e) {
//			e.printStackTrace();
//		}
//	}
}
