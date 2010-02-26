package postagging.programs;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Random;

import learning.CorpusPR;
import learning.EM;
import learning.stats.CompositeTrainStats;
import learning.stats.GlobalEMTimeCounter;
import learning.stats.LikelihoodStats;
import learning.stats.TrainStats;
import model.chain.PosteriorDecoder;
import model.chain.hmm.HMM;
import model.chain.hmm.HMM.Update_Parameters;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import postagging.constraints.L1LMax;

import postagging.data.PosCorpus;
import postagging.data.PosInstance;
import postagging.evaluation.PosMapping;
import postagging.learning.stats.AccuracyStats;
import postagging.learning.stats.L1LMaxStats;
import postagging.learning.stats.MeParametersStats;
import postagging.model.PosHMM;
import postagging.model.PosHMMFinalState;
import postagging.model.PosHMMFinalState2;
import postagging.model.PosReverseHMM;
import postagging.model.PosReverseHMMFinalState;
import util.InputOutput;
import data.InstanceList;


public class RunModel {
	
	// Train, dev, test files and output files
	@Option(name="-corpus-params", usage = "<filename> = Corpus parameters file")
	private String corpusParams;
	//Min and max sentence sizes to include for training
	@Option(name="-min-sentence-size", usage = "Minimum sentence size to use for training")
	private int minSentenceSize = 0;
	@Option(name="-max-sentence-size", usage = "Maximum sentence size to use for training")
	private int maxSentenceSize = Integer.MAX_VALUE;
	@Option(name="-max-number-sentences", usage = "Maximum number of sentences to load")
	private int maxNumberOfSentences = Integer.MAX_VALUE;
	
	public void printCorpusOptions(PrintStream out){
		out.println("-corpus-params " + corpusParams);
		out.println("-min-sentence-size " + minSentenceSize);
		out.println("-max-sentence-size " + maxSentenceSize);
		out.println("-max-number-of-sentences " + maxNumberOfSentences);
	}
	
	// Model Selection
	private enum ModelType {HMM, HMMFinalState,HMMFinalState2, ReverseHMM, ReverseHMMFinalState}
	@Option(name="-model-type", usage = "HMM - Regular HMM" +
			"HMMFinalState - Regular HMM with explicit modelling of the final state" +
			"HMMFinalState2 - Bg Free Regular HMM with explicit modelling of the final state" +
			"ReverseHMM - Regular HMM reading the sentence from right to left" +
			"ReverseHMMFinalState - Regular HMM with explicit modelling of the final state reading the " +
			"sentence from right to right")
	private ModelType modelType = ModelType.HMMFinalState;
	
	@Option(name="-number-states", usage = "Number of hidden states of the HMM")
	private int nrStates = -1;
	
	
	public void printModelSelectionOptions(PrintStream out){
		out.println("-model-type " + modelType);
		out.println("-number-states " + nrStates);
	}
	
	// Model initialization
	private enum ModelInit {RANDOM,SUPERVISED,SAVED,RANDOM_SUPERVISED};
	@Option(name="-model-init", usage = "RANDOM = Random, SUPERVISED = Max likelihood, " +
			"or SAVED = Load saved model" +
			"RANDOM_SUPERVISED = randomly choose n sentences and " +
					"initialize supervised (n is set with the option)")
	private ModelInit initType = ModelInit.RANDOM;
	
	@Option(name="-seed", usage="Seed for random number generator for random start (default = 1)")
	private Long seed;  // Note: Default value is set in main method instead of here, to allow for more error checking
	@Option(name="-jitter", usage="Jitter used for each random number.... JOAO describe better")
	private double jitter = 1; 
	@Option(name="-model-to-load", usage="<filename> = Model to load")
	private String modelFile;
	
	public void printModelInitOptions(PrintStream out){
		out.println("-model-init " + initType);
		if(initType == ModelInit.RANDOM){
			out.println("-seed " + seed);
			out.println("-jitter " + jitter);
		} else if(initType == ModelInit.SAVED){
			out.println("-model-to-load " + modelFile);
		}
	}
	
	//Model update options
	@Option(name="-parameter_update_type", usage="Type of parameter update: TABLE_UP - Normal HMM just update" +
			"counts" + " OBS_MAX_ENT  use generative Max-Ent model on observation" +
					" VB Variational Bayes with Dirichelt prior"+"   MRF use an MRF to update")
	private HMM.Update_Parameters updateParams = HMM.Update_Parameters.TABLE_UP; 
	@Option(name="-max-ent-features-file", usage="<filename> = Description of what max-ent features to use")
	String maxEntFeaturesFile="";
	
	@Option(name="-max-ent-gaussian-prior", usage="gaussian prior to use on MaxEnt")
	double gaussianPrior = 10;
	@Option(name="-max-ent-warm-start", usage="use warm start on max-ent optimization")
	boolean maxEntWarmStart = false;
	
	@Option(name="-vb-state-state-prior", usage="Variational bayes transition prior")
	double stateToStatePrior = 000.1;
	@Option(name="-vb-state-observation-prior", usage="Variational bayes emission prior")
	double stateToObservationPrior = 000.1;
	
	public void printModelUpdateOptions(PrintStream out){
		out.println("-parameter_update_type " + updateParams);
		if(updateParams == HMM.Update_Parameters.OBS_MAX_ENT){
			out.println("-max-ent-features-file " + maxEntFeaturesFile);
			out.println("-max-ent-gaussian-prior " + gaussianPrior);
			out.println("-max-ent-warm-start " + maxEntWarmStart);
			
		} else if(updateParams == HMM.Update_Parameters.VB){
			out.println("-vb-state-state-prior " + stateToStatePrior);
			out.println("-vb-state-observation-prior" + stateToObservationPrior);
		}
		
	}
	
	//Training type
	
	// Basic EM options
	@Option(name="-num-em-iters", usage="Number of EM iterations (default = 0)")
	private int numEMIters = 0;
	@Option(name="-stats-file", usage="Training statistics file")
	private String statsFile = "";
	private enum TrainingType {EM,L1LMax};
	@Option(name="-trainingType", usage="Training Type: EM EM Training; " +	"L1LMax PR with L1LMax;")
	private TrainingType trainingType = TrainingType.EM;
	
	@Option(name="-warmup-iters", usage="Number of warmupIter before starting using constraints")
	private int warmupIter = 30;
	
	@Option(name="-min-word-occurs-L1LMax", usage="Minimum Occurences of words to use L1LMax")
	private int minWordOccursL1LMax = 10;
	
	@Option(name="-c-str", usage="Corpus constraints strengh")
	private double cstr = 32;
	
	public void printModelTrainingOptions(PrintStream out){
		out.println("-num-em-iters " + numEMIters);
		out.println("-stats-file " + statsFile);
		out.println("-trainingType " + trainingType);
		if(trainingType == TrainingType.L1LMax){
			out.println("-warmup-iters " + warmupIter);
			out.println("-min-word-occurs-L1LMax " + minWordOccursL1LMax);
			out.println("-c-str " + cstr);
		} 
	}
	
	private enum TestSet {Train,Dev,Test,All};
	//Testing options
	@Option(name="-test-set", usage="Where to test at the end of training: Train, Dev, Test, All")
	TestSet testSet = TestSet.Test;
	
	//Testing options
	@Option(name="-training-test-set", usage="Where to test during training of training: Train, Dev, Test, All")
	TestSet trainingTestSet = TestSet.Dev;
	
	@Option(name="-save-predictions-end", usage="Save the predictions after training")
	boolean savePredictionsEnd = true;
	
	@Option(name="-save-predictions-training", usage="Save the predictions during training")
	boolean savePredictionsTraining = false;
	
	@Option(name="-save-predictions-training-iter", usage="Number of iterations to save the predictions at training")
	int savePredictionsTrainingIter = 50;
	
	public void printModelTestOptions(PrintStream out){
		out.println("-test-set" + testSet);
		out.println("-training-test-set" + trainingTestSet);
		out.println("-save-predictions-end" + savePredictionsEnd);
		out.println("-save-predictions-training" + savePredictionsTraining);
		out.println("-save-predictions-training-iter" + savePredictionsTrainingIter);
	}
	
	//Output Options
	@Option(name="-base-output-dir", usage="Directory where outpus will be added")
	private String baseOutputString;
	
	@Option(name="-save-model", usage="Save the model at the end of training or not")
	private boolean saveModel = false;
	
	
	
	public void printModelOutputOptions(PrintStream out){
		out.println("-base-output-dir" + baseOutputString);
		out.println("-save-model" + saveModel);
	}
	
	private void printOptions(PrintStream out){
		printCorpusOptions(out);
		printModelSelectionOptions(out);
		printModelInitOptions(out);
		printModelUpdateOptions(out);
		printModelTrainingOptions(out);	
		printModelTestOptions(out);
		printModelOutputOptions(out);
	}
	
	@Argument
	private final ArrayList<String> arguments = new ArrayList<String>();
	
	public static void main (String[] args)
	throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
		new RunModel().parseCommandLineArguments(args);
	}
	
	
	
	

	public void parseCommandLineArguments(String[] args)
	throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, InstantiationException, IOException {

		CmdLineParser parser = new CmdLineParser(this);
		try {
			 parser.setUsageWidth(120);
			
			// Read options
			parser.parseArgument(args);

			// If any additional arguments were given, throw exception
			if(!arguments.isEmpty())
				throw new CmdLineException("Unrecognized command line arguments: " + arguments.toString());

			printOptions(System.out);
			
			// Read corpus params, the only option that's always required
			PosCorpus corpus;
			if(corpusParams == null) throw new CmdLineException("Must always specify a corpus params file");
			else corpus = new PosCorpus(corpusParams,minSentenceSize,maxSentenceSize);

			//Initialize model
			HMM model = createModel(modelType,corpus,nrStates);
			initializeModel(model);
			
			CompositeTrainStats stats = (CompositeTrainStats) CompositeTrainStats.buildTrainStats(statsFile);
			stats.addStats(new GlobalEMTimeCounter());
			stats.addStats(new LikelihoodStats());
	//		stats.addStats(new MemoryStats());
			//stats.addStats(new ComulativeMemoryStats());
			if(trainingTestSet == TestSet.Train){
				stats.addStats(new AccuracyStats(5,savePredictionsTrainingIter,baseOutputString+"/predictions/",savePredictionsTraining,corpus.trainInstances));
			}else if(trainingTestSet == TestSet.Dev){
				if(corpus.devInstances == null){
					System.out.println("No Dev instances available for evaluation during training");
					System.out.println("Defaulting to training instances");
					stats.addStats(new AccuracyStats(5,savePredictionsTrainingIter,baseOutputString+"/predictions/",savePredictionsTraining,corpus.trainInstances));

				}else{
					stats.addStats(new AccuracyStats(5,savePredictionsTrainingIter,baseOutputString+"/predictions/",savePredictionsTraining,corpus.devInstances));
					stats.addStats(new AccuracyStats("5","101",baseOutputString+"/predictions/",corpus.devInstances));
				}
			}else if(trainingTestSet == TestSet.Test){
				if(corpus.testInstances == null || corpus.testInstances.size() == 0){
					System.out.println("No Test instances available for evaluation during training");
					System.out.println("Defaulting to training instances");
					stats.addStats(new AccuracyStats(5,savePredictionsTrainingIter,baseOutputString+"/predictions/",savePredictionsTraining,corpus.trainInstances));

				}else{
					for(int i = 0; i < corpus.testInstances.size(); i++){
						stats.addStats(new AccuracyStats(5,savePredictionsTrainingIter,baseOutputString+"/predictions/",savePredictionsTraining,corpus.testInstances.get(i)));
					}
				}
			}else if (trainingTestSet == TestSet.All){
				stats.addStats(new AccuracyStats(5,savePredictionsTrainingIter,baseOutputString+"/predictions/",savePredictionsTraining,corpus.trainInstances));

				if(corpus.devInstances != null){
					stats.addStats(new AccuracyStats(5,savePredictionsTrainingIter,baseOutputString+"/predictions/",savePredictionsTraining,corpus.devInstances));

				}
				if(corpus.testInstances != null && corpus.testInstances.size() > 0){
					for(int i = 0; i < corpus.testInstances.size(); i++){
						stats.addStats(new AccuracyStats(5,savePredictionsTrainingIter,baseOutputString+"/predictions/",savePredictionsTraining,corpus.testInstances.get(i)));
					}
				}
			}
			
			
			stats.addStats(new L1LMaxStats(corpus,model,"2",minWordOccursL1LMax,baseOutputString+"/l1LMaxClusters/",50));
			stats.addStats(new MeParametersStats(10,baseOutputString+"/me-parameters/"));
			trainModel(corpus,model,stats);
			
			if(saveModel){
				System.out.println("Saving model not yet implement");
			}

			testModel(model, corpus);
		} catch(CmdLineException e) {
			// Print exception's message
			System.err.println(e.getMessage() + "\n");

			// Print usage message
			System.err.println("Full set of available options:\njava programs.Train [options...]");
			parser.printUsage(System.err);

			return;
		}
	}

	public void testModel(HMM model, PosCorpus corpus) throws UnsupportedEncodingException, IOException{
		if(testSet == TestSet.Train){
			System.out.println(testModel(model, corpus.trainInstances, "Final",savePredictionsEnd,baseOutputString+"/predictions/final/"));
		}else if(testSet == TestSet.Dev){
			if(corpus.devInstances == null){
				System.out.println("No Dev instances available for evaluation at end");
				System.out.println("Defaulting to training instances");
				System.out.println(testModel(model, corpus.trainInstances, "Final",savePredictionsEnd,baseOutputString+"/predictions/final/"));
			}else{
				System.out.println(testModel(model, corpus.devInstances, "Final",savePredictionsEnd,baseOutputString+"/predictions/final/"));
				
			}
		}else if(testSet == TestSet.Test){
			if(corpus.testInstances == null || corpus.testInstances.size() == 0){
				System.out.println("No Test instances available for evaluation at the end");
				System.out.println("Defaulting to training instances");
				System.out.println(testModel(model, corpus.trainInstances, "Final",savePredictionsEnd,baseOutputString+"/predictions/final/"));
			}else{
				for(int i = 0; i < corpus.testInstances.size(); i++){
					System.out.println(testModel(model, corpus.testInstances.get(i), "Final",savePredictionsEnd,baseOutputString+"/predictions/final/"));
				}
			}
		}else if (testSet == TestSet.All){
			System.out.println(testModel(model, corpus.trainInstances, "Final",savePredictionsEnd,baseOutputString+"/predictions/final/"));
			if(corpus.devInstances != null){
				System.out.println(testModel(model, corpus.devInstances, "Final",savePredictionsEnd,baseOutputString+"/predictions/final/"));
			}
			if(corpus.testInstances != null && corpus.testInstances.size() > 0){
				for(int i = 0; i < corpus.testInstances.size(); i++){
					System.out.println(testModel(model, corpus.testInstances.get(i), "Final",savePredictionsEnd,baseOutputString+"/predictions/final/"));
				}
			}
		}
	}
	
	
	private void trainModel(PosCorpus c, HMM model, TrainStats stats) throws CmdLineException, UnsupportedEncodingException, IOException {
		if(TrainingType.EM== trainingType) {
			EM em = new EM(model);
			em.em(numEMIters, stats);
		} else if(TrainingType.L1LMax== trainingType){
			EM em = new EM(model);
			em.em(warmupIter, stats);
			L1LMax l1lmax = new L1LMax(c,model,minWordOccursL1LMax,cstr);
			CorpusPR learning = new CorpusPR(model,l1lmax);
			learning.em(numEMIters, stats);
		}
		else {
			throw new CmdLineException("Not a valid training type");
		}
	}

	private void initializeModel(HMM model) {
		switch(initType) {
		case RANDOM: seed = (seed == null? 1 : seed);
					 System.out.println("Random initialization with seed " + seed);
					 model.initializeRandom(new Random(seed), jitter); break;
		case SUPERVISED: 
		 System.out.println("Supervised initialization");
		 model.initializeSupervised(1e-200, ((PosCorpus)model.corpus).getNrTags(), model.corpus.trainInstances);
		 break;
		default: assert(false) : "Why didn't args4j catch this model-init options error?"; break;
		}
		System.out.println();
	}
	
	
	public HMM createModel(ModelType modelType, PosCorpus c, int nrTags) throws IllegalArgumentException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException{
		if(nrTags == -1){
			nrTags = c.getNrTags();
		}
		HMM model = null;
		if(ModelType.HMM == modelType){
			model = new PosHMM(c,nrTags);
		}else if(ModelType.HMMFinalState == modelType){
			model = new PosHMMFinalState(c,nrTags);
		}else if(ModelType.HMMFinalState2 == modelType){
			model = new PosHMMFinalState2(c,nrTags);
		}else if(ModelType.ReverseHMM == modelType){
			model = new PosReverseHMM(c,nrTags);
		}else if(ModelType.ReverseHMMFinalState == modelType){
			model = new PosReverseHMMFinalState(c,nrTags);
		}else{
			System.out.println("Model is non existing");
			System.exit(-1);
		}
		model.updateType = updateParams;
		if(model.updateType == Update_Parameters.OBS_MAX_ENT || model.updateType == Update_Parameters.MRF){
			if(maxEntFeaturesFile.equals("")){
				System.out.println("Must specify -max-ent-features-file to use max ent or MRF");
				System.exit(-1);
			}
			model.warmStart = maxEntWarmStart;
			model.gaussianPrior = gaussianPrior;
			//Create and add feature function
			model.fxy = new model.chain.GenerativeFeatureFunction(c,maxEntFeaturesFile);
			System.out.println("Using max Ent training");
			System.out.println("Gaussian Prior:" +model.gaussianPrior );
			System.out.println("Gradient Precision:" +model.gradientConvergenceValue);
			System.out.println("Value Precision:" +model.valueConvergenceValue);
			System.out.println("Max Iterations:" +model.maxIter);
		}else if(model.updateType == Update_Parameters.VB){
			model.stateToObservationPrior = stateToObservationPrior;
			model.stateToStatePrior = stateToStatePrior;
		}
		return model;
	}
	
	/**
	 * 
	 * @param model
	 * @throws IOException 
	 * @throws UnsupportedEncodingException 
	 */
	public static String testModel(HMM model, InstanceList list, String description, boolean savePredictions, String saveDir) throws UnsupportedEncodingException, IOException{
		
		StringBuffer res = new StringBuffer();        	
		int[][]  gold = new int[list.instanceList.size()][];
		for (int i = 0; i < gold.length; i++) {
			PosInstance inst = (PosInstance) list.instanceList.get(i);
			gold[i] = inst.getTags();
		}

		PosteriorDecoder posterior = new PosteriorDecoder();
		int[][] posteriorResults = model.decode(posterior, list);
		int[][] mappingCounts = 
			PosMapping.statePosCounts((PosCorpus)model.corpus, Integer.MAX_VALUE, model.getNrRealStates(), posteriorResults, ((PosCorpus)model.corpus).getNrTags(),gold);
		res.append(PosMapping.printMappingCounts((PosCorpus)model.corpus,mappingCounts, model.getNrRealStates(),((PosCorpus)model.corpus).getNrTags())+"\n");
		int[]  posteriorDecoding1ToManyMapping =  
			postagging.evaluation.PosMapping.manyTo1Mapping((PosCorpus)model.corpus,mappingCounts,Integer.MAX_VALUE, model.getNrRealStates(), posteriorResults, ((PosCorpus)model.corpus).getNrTags(),gold);
		res.append(PosMapping.printMapping((PosCorpus)model.corpus,posteriorDecoding1ToManyMapping));
		int[][] posteriorDecoding1ToMany = PosMapping.mapToTags(posteriorDecoding1ToManyMapping, posteriorResults, Integer.MAX_VALUE);
		res.append(description + "-"+list.name+" " + "Posterior UnSupervised 1 to many" + postagging.evaluation.Evaluator.eval(posteriorDecoding1ToMany, gold)+"\n");	
		int[]  posteriorDecoding1to1Mapping =  
			postagging.evaluation.PosMapping.oneToOnemapping((PosCorpus)model.corpus, mappingCounts,Integer.MAX_VALUE,model.getNrRealStates(), posteriorResults, ((PosCorpus)model.corpus).getNrTags(),gold);;
			res.append(PosMapping.printMapping((PosCorpus)model.corpus,posteriorDecoding1to1Mapping));
		int[][] posteriorDecoding1to1 =  PosMapping.mapToTags(posteriorDecoding1to1Mapping, posteriorResults, Integer.MAX_VALUE); 
			res.append(description + "-"+list.name+" " + "Posterior UnSupervised 1 to 1" + postagging.evaluation.Evaluator.eval(posteriorDecoding1to1, gold)+"\n");	
		
		
    	
    	double[] infometric = PosMapping.informationTheorethicMeasures(mappingCounts,model.getNrRealStates(),((PosCorpus)model.corpus).getNrTags());
		res.append(description + "-"+list.name+" " 
				+ " Posterior E(Tag) " + infometric[0]+
				" E(Gold) " + infometric[1 ] +
				" MI " + infometric[2] +
				" H(Gold |Tag) " + infometric[3] +
				" H(Tag |Gold) " + infometric[4] +
				" VI " + infometric[5] +
				"\n");
		
		if(savePredictions){
			util.FileSystem.createDir(saveDir);
			PrintStream predictions = InputOutput.openWriter(saveDir+description + "-"+list.name+".posteriorDec");
	    	predictions.print(((PosCorpus)model.corpus).printClusters(list, posteriorResults));
	    	predictions.close();
	    	PrintStream predictions1ToMany = InputOutput.openWriter(saveDir+description + "-"+list.name+".posteriorDec.1toMany");
	    	predictions1ToMany.print(((PosCorpus)model.corpus).printTags(list, posteriorDecoding1ToMany));
	    	predictions1ToMany.close();
	    	PrintStream predictions1To1 = InputOutput.openWriter(saveDir+description + "-"+list.name+".posteriorDec.1to1");
	    	predictions1To1.print(((PosCorpus)model.corpus).printTags(list, posteriorDecoding1to1));
	    	predictions1To1.close();	
		}
		
		return res.toString();
    	
	}
	
	public void savePredictions(HMM model, InstanceList list, String description) throws FileNotFoundException{
    	
    	
    	
    }
	
	
	
}
