package milestone_two;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

public class Util {
	
	private Util() {
	    throw new IllegalStateException("Utility class");
	  }
	
	private static final String EXCEPTION_THROWN = "exception thrown";
	private static final Logger logger = Logger.getAnonymousLogger();

	public static void buildResultCsv(List<ResultEntry> entryList, String projName) {
		String outname = projName.toUpperCase() + "Results.csv";
		try (
			// Name of CSV for output
			FileWriter fileWriter = new FileWriter(outname)){
			fileWriter.append("dataset,#trainingRelease,%training,%defectiveInTraining,%defectiveInTesting,classifier,"
					+ "balancing,featureSelection,TP,FP,TN,FN,precision,recall,ROC_Area,kappa");
			fileWriter.append("\n");
			for (ResultEntry entry : entryList) {
				fileWriter.append(entry.getDataset());
				fileWriter.append(",");
				fileWriter.append(entry.getNumTrainRelease());
				fileWriter.append(",");
				fileWriter.append(entry.getPercTraining());
				fileWriter.append(",");
				fileWriter.append(entry.getPercDefectiveTrain());
				fileWriter.append(",");
				fileWriter.append(entry.getPercDefectiveTest());
				fileWriter.append(",");
				fileWriter.append(entry.getClassifier());
				fileWriter.append(",");
				fileWriter.append(entry.getBalancing());
				fileWriter.append(",");
				fileWriter.append(entry.getFeatureSelection());
				fileWriter.append(",");
				fileWriter.append(entry.getTruePositive());
				fileWriter.append(",");
				fileWriter.append(entry.getFalsePositive());
				fileWriter.append(",");
				fileWriter.append(entry.getTrueNegative());
				fileWriter.append(",");
				fileWriter.append(entry.getFalseNegative());
				fileWriter.append(",");
				fileWriter.append(entry.getPrecision());
				fileWriter.append(",");
				fileWriter.append(entry.getRecall());
				fileWriter.append(",");
				fileWriter.append(entry.getRocArea());
				fileWriter.append(",");
				fileWriter.append(entry.getKappa());
				fileWriter.append("\n");
			}

		} catch (Exception e) {
			logger.log(Level.SEVERE, EXCEPTION_THROWN, e);
		}
	}
	
	public static int[] countClassesOccurrences(Instances inst) {
		int countYes = 0;
		int countNo = 0;
		for (int i = 0; i < inst.numInstances(); i++) {
			if (inst.instance(i).stringValue(inst.numAttributes() - 1).equals("YES")) {
				countYes++;
			} else if (inst.instance(i).stringValue(inst.numAttributes() - 1).equals("NO")) {
				countNo++;
			}
		}
		return new int[]{countYes, countNo};
	}
	
	public static void csvToArff(String filePath) {
		// load CSV
		CSVLoader loader = new CSVLoader();
		try {
			loader.setSource(new File(filePath));
			Instances data = loader.getDataSet();// get instances object
			if (filePath != null && filePath.length() > 0) {
				filePath = filePath.substring(0, filePath.length() - 4) + ".arff";
			}
			// save ARFF
			ArffSaver saver = new ArffSaver();
			saver.setInstances(data);// set the dataset we want to convert
			// and save as ARFF
			saver.setFile(new File(filePath));
			saver.writeBatch();
		} catch (IOException e) {
			logger.log(Level.SEVERE, EXCEPTION_THROWN, e);
		}
	}
}
