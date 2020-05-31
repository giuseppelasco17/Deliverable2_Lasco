package milestone_two;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.supervised.instance.SpreadSubsample;

public class SecondMileController {

	private static final String[] FEATURE_SELECTION = { "noFeatureSel", "bestFirst" };
	private static final String[] BALANCING = { "noSampling", "oversampling", "undersampling", "SMOTE" };
	private static final String[] CLASSIFIERS = { "randomForest", "naiveBayes", "ibk" };
	private String project;

	private static final String EXCEPTION_THROWN = "exception thrown";
	private static final Logger logger = Logger.getAnonymousLogger();
	private List<Integer> versionIndexes;
	private Instances dataset;

	private void selectProject() {
		try (FileReader f = new FileReader("config.txt"); BufferedReader buff = new BufferedReader(f);) {
			this.project = buff.readLine();
			this.project = buff.readLine();
			this.project = buff.readLine();
		} catch (IOException e) {
			logger.log(Level.SEVERE, EXCEPTION_THROWN, e);
		}
	}

	private void computeVersionIndexes(String dataset) {
		String nextLine = null;
		versionIndexes = new ArrayList<>();
		int lines = 0;
		int index = 1;
		versionIndexes.add(lines);// first line
		try (FileReader f = new FileReader(dataset); BufferedReader buff = new BufferedReader(f);) {
			// Reading Records One by One in a String array

			nextLine = buff.readLine();
			while ((nextLine = buff.readLine()) != null) {
				String[] line = nextLine.split(",");
				if (line != null && Integer.parseInt(line[0]) > index) {
					index = Integer.parseInt(line[0]);
					versionIndexes.add(lines);// first line of next version
				}
				lines++;
			}
			versionIndexes.add(lines);// last line
		} catch (IOException e) {
			logger.log(Level.SEVERE, EXCEPTION_THROWN, e);
		}
	}

	private ResultEntry setEntry(int k, Instances training, Instances testing, String samTecnique, String fSTecnique,
			String classifier, Evaluation evaluation) {
		ResultEntry entry = new ResultEntry();
		entry.setDataset(project.substring(0, 1).toUpperCase() + project.substring(1));
		entry.setNumTrainRelease(String.valueOf(k + 1));
		int trainInstances = training.numInstances();
		int testInstances = testing.numInstances();
		int totalInstances = dataset.numInstances();
		double percTraining = ((float) trainInstances / (float) totalInstances) * 100.0;
		entry.setPercTraining(String.valueOf(percTraining));
		
		int[] occurrencesTrain = Util.countClassesOccurrences(training);
		int numYesTrain = occurrencesTrain[0];
		double percDefTraining = ((float) numYesTrain / (float) trainInstances) * 100.0;
		entry.setPercDefectiveTrain((String.valueOf(percDefTraining)));
		
		int[] occurrencesTest = Util.countClassesOccurrences(testing);
		int numYesTest = occurrencesTest[0];
		double percDefTesting = ((float) numYesTest / (float) testInstances) * 100.0;
		entry.setPercDefectiveTest((String.valueOf(percDefTesting)));
		entry.setClassifier(classifier);
		entry.setBalancing(samTecnique);
		entry.setFeatureSelection(fSTecnique);
		entry.setTruePositive(String.valueOf(evaluation.numTruePositives(1)));
		entry.setTrueNegative(String.valueOf(evaluation.numTrueNegatives(1)));
		entry.setFalsePositive(String.valueOf(evaluation.numFalsePositives(1)));
		entry.setFalseNegative(String.valueOf(evaluation.numFalseNegatives(1)));
		entry.setPrecision(String.valueOf(evaluation.weightedPrecision()));//TODO:controllare che sia weighted oppure no
		entry.setRecall(String.valueOf(evaluation.weightedRecall()));
		entry.setRocArea(String.valueOf(evaluation.weightedAreaUnderROC()));
		entry.setKappa(String.valueOf(evaluation.kappa()));
		return entry;
	}

	private ResultEntry runEvaluation(int k, String fSTecnique, String samTecnique, String classifier)
			throws InvalidWekaTecniqueException {
		int beginTrain = 0;
		int beginTest = versionIndexes.get(k + 1);
		int trainAmount = beginTest;
		int testAmount = versionIndexes.get(k + 2) - trainAmount;
		Instances training = new Instances(dataset, beginTrain, trainAmount);
		Instances testing = new Instances(dataset, beginTest, testAmount);
		int numAttr = training.numAttributes();
		training.setClassIndex(numAttr - 1);
		testing.setClassIndex(numAttr - 1);
		Filter sampler = null;
		FilteredClassifier fc = new FilteredClassifier();
		RandomForest randomForest;
		IBk ibk;
		NaiveBayes naiveBayes;
		// Balancing
		switch (samTecnique) {
		case "noSampling":
			break;
		case "oversampling":
			int[] occurrences = Util.countClassesOccurrences(training);
			int numYes = occurrences[0];
			int numNo = occurrences[1];
			int majority = Math.max(numYes, numNo);
			double majorPerc = ((float) majority / (float) training.numInstances()) * 100.0 * 2;// Y, the option to pass
																								// to the filter. It is
																								// the
																								// majority class
																								// occurrence * 2
			sampler = new Resample();// sampling tecnique
			String[] optsOver = new String[] { "-B", "1.0", "-Z", (String.valueOf(majorPerc))};
			try {
				sampler.setOptions(optsOver);
				sampler.setInputFormat(training);
			} catch (Exception e) {
				logger.log(Level.SEVERE, EXCEPTION_THROWN, e);
			}
			
			break;
		case "undersampling":
			sampler = new SpreadSubsample();
			String[] opts = new String[] { "-M", "1.0" };
			try {
				sampler.setOptions(opts);
			} catch (Exception e) {
				logger.log(Level.SEVERE, EXCEPTION_THROWN, e);
			}
			break;
		case "SMOTE":
			sampler = new SMOTE();
			try {
				sampler.setInputFormat(training);
			} catch (Exception e) {
				logger.log(Level.SEVERE, EXCEPTION_THROWN, e);
			}
			break;
		default:
			throw new InvalidWekaTecniqueException("Invalid balancing tecnique");
		}
		// FS
		switch (fSTecnique) {
		case "bestFirst":
			// create AttributeSelection object
			AttributeSelection filter = new AttributeSelection();
			// create evaluator and search algorithm objects
			CfsSubsetEval eval = new CfsSubsetEval();
			GreedyStepwise search = new GreedyStepwise();
			// set the algorithm to search backward
			search.setSearchBackwards(true);
			// set the filter to use the evaluator and search algorithm
			filter.setEvaluator(eval);
			filter.setSearch(search);
			// specify the dataset
			try {
				filter.setInputFormat(training);
				// apply
				training = Filter.useFilter(training, filter);
				int numAttrFiltered = training.numAttributes();
				training.setClassIndex(numAttrFiltered - 1);
				testing = Filter.useFilter(testing, filter);
				testing.setClassIndex(numAttrFiltered - 1);
			} catch (Exception e) {
				logger.log(Level.SEVERE, EXCEPTION_THROWN, e);
			}
			break;
		case "noFeatureSel":
			break;
		default:
			throw new InvalidWekaTecniqueException("Invalid feat. sel. tecnique");
		}
		// classifier
		switch (classifier) {
		case "randomForest":
			randomForest = new RandomForest();
			fc.setClassifier(randomForest);
			break;
		case "naiveBayes":
			naiveBayes = new NaiveBayes();
			fc.setClassifier(naiveBayes);
			break;
		case "ibk":
			ibk = new IBk();
			fc.setClassifier(ibk);
			break;
		default:
			throw new InvalidWekaTecniqueException("Invalid classifier");
		}
		if (sampler != null)
			fc.setFilter(sampler);
		Evaluation evaluation = null;
		try {
			fc.buildClassifier(training);
			evaluation = new Evaluation(testing);
			evaluation.evaluateModel(fc, testing);
		} catch (Exception e) {
			logger.log(Level.SEVERE, EXCEPTION_THROWN, e);
		}
		if (evaluation == null) {
			throw new InvalidWekaTecniqueException("Evaluation is null");
		}
		return setEntry(k, training, testing, samTecnique, fSTecnique, classifier, evaluation);// TODO: ha
																											// senso
																											// computare
																											// percentuali
																											// dopo
																											// sampling??
	}

	public List<ResultEntry> analyzeDataset() throws InvalidWekaTecniqueException {
		List<ResultEntry> resultEntries = new ArrayList<>();
		selectProject();
		Util.csvToArff(project.toUpperCase() + "Dataset.csv");
		DataSource source;
		try {
			source = new DataSource(project.toUpperCase() + "Dataset.arff");
			dataset = source.getDataSet();
		} catch (Exception e) {
			logger.log(Level.SEVERE, EXCEPTION_THROWN, e);
		}
		
		// load the dataset
		computeVersionIndexes(project.toUpperCase() + "Dataset.csv");
		int numOfRuns = versionIndexes.size() - 3;
		for (String fSTecnique : FEATURE_SELECTION) {
			for (String samTecnique : BALANCING) {
				for (String classifier : CLASSIFIERS) {
					for (int k = 0; k < numOfRuns; k++) {
						ResultEntry entry = runEvaluation(k, fSTecnique, samTecnique, classifier);
						resultEntries.add(entry);
					}
				}
			}
		}
		return resultEntries;
	}

	public static void main(String[] args) {
		SecondMileController secondMileController = new SecondMileController();
		List<ResultEntry> resultEntries = null;
		try {
			resultEntries = secondMileController.analyzeDataset();
		} catch (InvalidWekaTecniqueException e) {
			logger.log(Level.SEVERE, EXCEPTION_THROWN, e);
		}
		Util.buildResultCsv(resultEntries, secondMileController.project);
	}
}
