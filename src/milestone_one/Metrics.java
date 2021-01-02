package milestone_one;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Metrics {

	private static final String EXCEPTION_THROWN = "an exception was thrown";
	private static final Logger logger = Logger.getAnonymousLogger();
	
	private Metrics() {
	    throw new IllegalStateException("Metrics class");
	  }

	public static SortedMap<Integer, List<String>> listAllCommitsPerVersion(String projName, int midVersion) {
		/*
		 * it retrieves all the commits of the project and collect them for version in a
		 * map
		 */
		List<String> commits = null;
		try {
			commits = GitQuery.getInstance().listAllCommitsWithDates();
		} catch (InstantiationException e) {
			logger.log(Level.SEVERE, EXCEPTION_THROWN, e);
		}
		TreeMap<Integer, List<String>> versionCommitsMap = new TreeMap<>();
		for (String commit : commits) {// for each commit I want to collect it in a map with the correct version id
			String[] dateAndCommit = commit.split(",");
			int version = Utilities.getIdVersionFromDate(projName, dateAndCommit[0]);// i take the version from the date
			if (version != -1 && version <= midVersion) {// if version is not valid or >= midVersion i discard them
				List<String> commitsList = new ArrayList<>();
				if ((versionCommitsMap.get(version)) != null) {// if entry in map exist
					commitsList = versionCommitsMap.get(version);
					commitsList.add(dateAndCommit[1]);
					versionCommitsMap.put(version, commitsList);
				} else {
					commitsList.add(dateAndCommit[1]);
					versionCommitsMap.put(version, commitsList);
				}
			}

		}
		return versionCommitsMap;
	}

	public static CSVEntry findObjInList(Integer version, String fileName, List<CSVEntry> csvEntryList) {
		CSVEntry entryFound = null;

		for (CSVEntry entry : csvEntryList) {
			if (entry.getFileName().equals(fileName) && entry.getVersion().equals(version)) {
				entryFound = entry;
				break;
			}
		}
		return entryFound;
	}

	public static List<CSVEntry> computeMetrics(SortedMap<Integer, List<String>> versionCommitsMap) {
		/**
		 * it analyzes the differences between the i and i-1 commit with the purpose of
		 * compute all the metrics the project needs
		 */
		List<CSVEntry> csvEntryList = new ArrayList<>();
		for (Entry<Integer, List<String>> entryMap : versionCommitsMap.entrySet()) {// for version
			List<String> commitList = entryMap.getValue();
			for (int i = 1; i < commitList.size(); i++) {// for commits in version k
				List<String> filesPlusAttrList = null;
				try {
					filesPlusAttrList = GitQuery.getInstance().gitDiff(commitList.get(i - 1), commitList.get(i));
				} catch (InstantiationException e) {
					logger.log(Level.SEVERE, EXCEPTION_THROWN, e);
				}
				if(filesPlusAttrList == null) {
					throw new NullPointerException();
				}
				Double noOfFiles = Double.valueOf(filesPlusAttrList.size() - Double.valueOf(1));
				for (String filePlusAttr : filesPlusAttrList) {// for file in difference between commit i and i-1
					calcMetricsOfFile(noOfFiles, filePlusAttr, csvEntryList, entryMap);
				}

			}
		}
		return csvEntryList;
	}

	private static void calcMetricsOfFile(Double noOfFiles, String filePlusAttr, List<CSVEntry> csvEntryList, Entry<Integer, List<String>> entryMap) {
		String[] fPAStrings = filePlusAttr.split("\t");
		Integer version = entryMap.getKey();
		String fileName = fPAStrings[2];
		Double addedLines = Double.valueOf(fPAStrings[0]);
		Double removedLines = Double.valueOf(fPAStrings[1]);
		CSVEntry entry = findObjInList(version, fileName, csvEntryList);
		if (entry == null) {// if entry doesn't exist in the list, it'll be created
			entry = new CSVEntry();
			entry.setFileName(fileName);
			entry.setVersion(version);
			csvEntryList.add(entry);
		} // else i work on the entry i've found
			// compute LOC_added
		Double actualLocAdded = entry.getLocAdded();
		Double actualAvgLocAdded = entry.getAvgLocAdded();
		Double actualNR = entry.getNR();
		Double actualMaxLocAdded = entry.getMaxLocAdded();
		entry.setLocAdded(actualLocAdded + addedLines);
		entry.setAvgLocAdded(actualAvgLocAdded * ((actualNR) / (actualNR + Double.valueOf(1)))
				+ addedLines * (Double.valueOf(1) / (actualNR + Double.valueOf(1))));
		if (actualMaxLocAdded < addedLines) {
			entry.setMaxLocAdded(addedLines);
		}
		// compute NR
		entry.setNR(actualNR + Double.valueOf(1));
		// compute CHURN(added - deleted)
		Double actualChurn = entry.getChurn();
		Double actualAvgChurn = entry.getAvgChurn();
		Double actualMaxChurn = entry.getMaxChurn();
		entry.setChurn(actualChurn + (addedLines - removedLines));
		entry.setAvgChurn(actualAvgChurn * ((actualNR) / (actualNR + Double.valueOf(1)))
				+ (addedLines - removedLines) * (Double.valueOf(1) / (actualNR + Double.valueOf(1))));
		if (actualMaxChurn < (addedLines - removedLines)) {
			entry.setMaxChurn((addedLines - removedLines));
		}
		// compute ChgSet (# of files committed together the actual file)
		Double actualChgSet = entry.getChgSet();
		Double actualAvgChgSet = entry.getAvgChgSet();
		Double actualMaxChgSet = entry.getMaxChgSet();
		entry.setChgSet(actualChgSet + noOfFiles);
		entry.setAvgChgSet(actualAvgChgSet * ((actualNR) / (actualNR + Double.valueOf(1)))
				+ noOfFiles * (Double.valueOf(1) / (actualNR + Double.valueOf(1))));
		if (actualMaxChgSet < noOfFiles) {
			entry.setMaxChgSet(noOfFiles);
		}
	}
}
