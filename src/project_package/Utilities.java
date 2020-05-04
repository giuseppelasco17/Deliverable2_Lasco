package project_package;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;



public class Utilities {
	
	private static final Logger logger = Logger.getAnonymousLogger();

	public static int getIdVersionFromDate(String projName,String _date) {
		Date date = null;
		try {
			date = new SimpleDateFormat("yyyy-MM-dd").parse(_date);
		} catch (ParseException e1) {
			logger.log(Level.SEVERE, "an exception was thrown", e1);
		}
		Date prevDate = null;
		String nextLine = null;
        String[] nextRecord = null;
        String[] prevRecord = null;
		try (
				FileReader f = new FileReader(projName + "VersionInfo.csv");
				BufferedReader buff = new BufferedReader(f);
	        ) {
	            // Reading Records One by One in a String array

	            buff.readLine();
	            while ((nextLine = buff.readLine()) != null) {
	            	nextRecord = nextLine.split(",");
	            	prevDate = new SimpleDateFormat("yyyy-MM-dd").parse(nextRecord[3]);
	            	if (date.before(prevDate) && prevRecord != null) {
	            		return Integer.parseInt(prevRecord[0]);
	            	}
	            	if (date.before(prevDate) && prevRecord == null){
	            		return -1;
	            	}
	            	prevRecord = nextRecord.clone();
	            } 
			} catch (IOException | ParseException e) {
				logger.log(Level.SEVERE, "an exception was thrown", e);
			}
		return Integer.parseInt(prevRecord[0]);
	}
	
	public static int getIdVersionFromVersion(String projName, String fixVersion) {
		int intFixV = Integer.parseInt(fixVersion.replaceAll("\\p{Punct}", ""));
		int prevV = 0;
		String nextLine = null;
		String[] nextRecord = null;
        String[] prevRecord = null;
		try (
				FileReader f = new FileReader(projName + "VersionInfo.csv");
				BufferedReader buff = new BufferedReader(f);
	        ) {
	            // Reading Records One by One in a String array

	            buff.readLine();
	            while ((nextLine = buff.readLine()) != null) {
	            	nextRecord = nextLine.split(",");
	            	prevV = Integer.parseInt(nextRecord[2].replaceAll("\\p{Punct}", ""));
	            	if (intFixV < prevV && prevRecord != null) {
	            		return Integer.parseInt(prevRecord[0]);
	            	}
	            	if (intFixV < prevV && prevRecord == null){
	            		return -1;
	            	}
	            	prevRecord = nextRecord.clone();
	            } 
			} catch (IOException e) {
				logger.log(Level.SEVERE, "an exception was thrown", e);
			}
		return Integer.parseInt(prevRecord[0]);
	}
	
	public static int getLastVersion(String projName) {
		String[] nextRecord = null;
		String nextLine = null;
		try (
				FileReader f = new FileReader(projName + "VersionInfo.csv");
				BufferedReader buff = new BufferedReader(f);
	        ) {
	            // Reading Records One by One in a String array
	            while ((nextLine = buff.readLine()) != null) {
	            	nextRecord = nextLine.split(",");
	            } 
			} catch (IOException e) {
				logger.log(Level.SEVERE, "an exception was thrown", e);
			}
		return Integer.parseInt(nextRecord[0]);
	}
	
	public static void buildCsv(List<CSVEntry> entryList, String projName) {
		FileWriter fileWriter = null;
        try {
           String outname = projName + "Dataset.csv";
				    //Name of CSV for output
           fileWriter = new FileWriter(outname);
           fileWriter.append("Version,File Name,LOC_added,MAX_LOC_added,AVG_LOC_added,Churn,MAX_Churn,"
           		+ "AVG_Churn,NR,ChgSetSize,MAX_ChgSet,AVG_ChgSet,Buggy");
           fileWriter.append("\n");
           for (CSVEntry entry : entryList) {
              fileWriter.append(entry.getVersion().toString());
              fileWriter.append(",");
              fileWriter.append(entry.getFileName());
              fileWriter.append(",");
              fileWriter.append(entry.getLocAdded().toString());
              fileWriter.append(",");
              fileWriter.append(entry.getMaxLocAdded().toString());
              fileWriter.append(",");
              fileWriter.append(entry.getAvgLocAdded().toString());
              fileWriter.append(",");
              fileWriter.append(entry.getChurn().toString());
              fileWriter.append(",");
              fileWriter.append(entry.getMaxChurn().toString());
              fileWriter.append(",");
              fileWriter.append(entry.getAvgChurn().toString());
              fileWriter.append(",");
              fileWriter.append(entry.getNR().toString());
              fileWriter.append(",");
              fileWriter.append(entry.getChgSet().toString());
              fileWriter.append(",");
              fileWriter.append(entry.getMaxChgSet().toString());
              fileWriter.append(",");
              fileWriter.append(entry.getAvgChgSet().toString());
              fileWriter.append(",");
              fileWriter.append(entry.getBuggy());
              fileWriter.append("\n");
           }

        } catch (Exception e) {
           System.out.println("Error in csv writer");
           e.printStackTrace();
        } finally {
           try {
              fileWriter.flush();
              fileWriter.close();
           } catch (IOException e) {
              System.out.println("Error while flushing/closing fileWriter !!!");
              e.printStackTrace();
           }
        }
	}
		
	public static void main(String[] args) {
		System.out.println(Utilities.getIdVersionFromDate("BOOKKEEPER", "2011-12-08"));
		System.out.println(Utilities.getIdVersionFromVersion("BOOKKEEPER", "4.3.3"));
		System.out.println(Utilities.getLastVersion("BOOKKEEPER"));
	}

}
