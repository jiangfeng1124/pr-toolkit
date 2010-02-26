package postagging.programs;


//import java.io.IOException;
//import java.io.PrintStream;
//import java.lang.reflect.InvocationTargetException;
//import java.util.ArrayList;
//import java.util.Random;
//
//
//import learning.JointCorpusPR;
//import learning.JointEM;
//import learning.stats.JointCompositeTrainStats;
//import learning.stats.JointTrainStats;
//import model.AbstractSentenceDist;
//import model.chain.ChainDecoder;
//import model.chain.PosteriorDecoder;
//import model.chain.ViterbiDecoder;
//import model.chain.hmm.HMM;
//import model.chain.hmm.HMMSentenceDist;
//
//import org.kohsuke.args4j.Argument;
//import org.kohsuke.args4j.CmdLineException;
//import org.kohsuke.args4j.CmdLineParser;
//import org.kohsuke.args4j.Option;
//
//import constraints.AgreementConstraints;
//
//import postagging.data.PosCorpus;
//import postagging.data.PosInstance;
//import postagging.model.PosHMM;
//import postagging.model.PosHMMFinalState;
//import postagging.model.PosReverseHMM;
//import postagging.model.PosReverseHMMFinalState;
//import data.InstanceList;


public class RunAgreementModel {
	
//	// Train, dev, test files and output files
//	@Option(name="-corpus-params", usage = "<filename> = Corpus parameters file")
//	private String corpusParams;
//	//Min and max sentence sizes to include for training
//	@Option(name="-min-sentence-size", usage = "Minimum sentence size to use for training")
//	private int minSentenceSize = 0;
//	@Option(name="-max-sentence-size", usage = "Maximum sentence size to use for training")
//	private int maxSentenceSize = Integer.MAX_VALUE;
//	@Option(name="-max-number-sentences", usage = "Maximum number of sentences to load")
//	private int maxNumberOfSentences = Integer.MAX_VALUE;
//	
//	
//	// Model Selection
//	private enum ModelType {HMM, HMMFinalState}
//	@Option(name="-model-type", usage = "HMM - Regular HMM for both directions" +
//			"HMMFinalState - Regular HMM with explicit modelling of the final state for both directions")
//	private ModelType modelType = ModelType.HMMFinalState;
//	
//	// Model initialization
//	private enum ModelInit {RANDOM,SUPERVISED,SAVED,RANDOM_SUPERVISED,RANDOM_POSTERIORS};
//	@Option(name="-model-init", usage = "RANDOM = Random, SUPERVISED = Max likelihood, " +
//			"or SAVED = Load saved model" +
//			"RANDOM_SUPERVISED = randomly choose n sentences and " +
//					"initialize supervised (n is set with the option)" +
//			"Random Posteriors = create a random posteriors and initalize both models from same posteriors")
//	private ModelInit initType = ModelInit.RANDOM_POSTERIORS;
//	
//	@Option(name="-seed", usage="Seed for random number generator for random start (default = 1)")
//	private Long seed;  // Note: Default value is set in main method instead of here, to allow for more error checking
//	@Option(name="-jitter", usage="Jitter used for each random number.... JOAO describe better")
//	private double jitter = 1; 
//	
//	//Model update options
//	@Option(name="-parameter_update_type", usage="Type of parameter update: TABLE_UP - Normal HMM just update" +
//			"counts" + " OBS_MAX_ENT  use generative Max-Ent model on observation" +
//					"VB Variational Bayes with Dirichelt prior")
//	private HMM.Update_Parameters updateParams = HMM.Update_Parameters.TABLE_UP; 
//	
//	// Basic EM options
//	@Option(name="-num-em-iters", usage="Number of EM iterations (default = 0)")
//	private int numEMIters = 0;
//	@Option(name="-stats-file", usage="Training statistics file")
//	private String statsFile = "";
//	private enum TrainingType {EM,PR};
//	@Option(name="-trainingType", usage="Training Type: EM EM Training; " +	"PR use agremeent constraint during training;")
//	private TrainingType trainingType = TrainingType.EM;
//	@Option(name="-warmup-iters", usage="Number of warmup normal EM iterations (default = 0)")
//	private int warmupIter = 0;
//	
//	
//	private void printOptions(PrintStream out){
//		out.println("-corpus-params " + corpusParams);
//		out.println("-min-sentence-size " + minSentenceSize);
//		out.println("-max-sentence-size " + maxSentenceSize);
//		out.println("-max-number-of-sentences " + maxNumberOfSentences);
//		out.println("-model-type " + modelType);
//		out.println("-model-init " + initType);
//		out.println("-seed " + seed);
//		out.println("-jitter " + jitter);
//		out.println("-num-em-iters " + numEMIters);
//		out.println("-stats-file " + statsFile);
//		out.println("-trainingType " + trainingType);
//	}
//	@Argument
//	private final ArrayList<String> arguments = new ArrayList<String>();
//	
//	public static void main (String[] args)
//	throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
//		new RunAgreementModel().parseCommandLineArguments(args);
//	}
//	
//	
//	
//	
//
//	public void parseCommandLineArguments(String[] args)
//	throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
//
//		CmdLineParser parser = new CmdLineParser(this);
//		try {
//			 parser.setUsageWidth(120);
//			
//			// Read options
//			parser.parseArgument(args);
//
//			// If any additional arguments were given, throw exception
//			if(!arguments.isEmpty())
//				throw new CmdLineException("Unrecognized command line arguments: " + arguments.toString());
//
//			printOptions(System.out);
//			
//			// Read corpus params, the only option that's always required
//			PosCorpus corpus;
//			if(corpusParams == null) throw new CmdLineException("Must always specify a corpus params file");
//			else corpus = new PosCorpus(corpusParams,minSentenceSize,maxSentenceSize);
//
//			//Initialize model
//			HMM models[] = createModels(modelType,corpus);
//			initializeModel(models);
//
//			
//
//			// Train model
//			if(numEMIters > 0) {
////				CompositeTrainStats stats = (CompositeTrainStats) CompositeTrainStats.buildTrainStats(statsFile);
////				stats.addStats(new GlobalEMTimeCounter());
////				stats.addStats(new LikelihoodStats());
//				JointTrainStats stats = new JointCompositeTrainStats();
//				trainModel(models,numEMIters,stats);
//			} 
//			
//
//		// Test model (will test for all test sets available in corpus)
//			testModel(models);
//		} catch(CmdLineException e) {
//			// Print exception's message
//			System.err.println(e.getMessage() + "\n");
//
//			// Print usage message
//			System.err.println("Full set of available options:\njava programs.Train [options...]");
//			parser.printUsage(System.err);
//
//			return;
//		}
//	}
//
//
//	private void trainModel(HMM[] models, int numEMIters, JointTrainStats stats) throws CmdLineException {
//		if(TrainingType.EM== trainingType) {
//			JointEM em = new JointEM(models);
//			em.em(numEMIters, stats);
//		}else if(TrainingType.PR== trainingType) {
//			
//			JointEM em = new JointEM(models);
//			em.em(warmupIter, stats);
//			System.out.println("Test after warmup\n\n");
//			testModel(models);
//			initFromCommonPosteriors(models);
//			JointCorpusPR pr = new JointCorpusPR(models, new AgreementConstraints());
//			pr.em(numEMIters-warmupIter, stats);
//		} else {
//			throw new CmdLineException("Not a valid training type");
//		}
//	}
//
//	private void initFromCommonPosteriors(HMM[] models){
//		AgreementConstraints constraints = new AgreementConstraints();
//		AbstractSentenceDist[][] posteriors = new AbstractSentenceDist[2][];
//		posteriors[0]=models[0].getSentenceDists();
//		posteriors[1]=models[1].getSentenceDists();
//		for (int i = 0; i < posteriors[0].length; i++) {
//			posteriors[0][i].initSentenceDist();
//			posteriors[1][i].initSentenceDist();
//			models[0].computePosteriors(posteriors[0][i]);
//			models[1].computePosteriors(posteriors[1][i]);
//		}
//		constraints.project(posteriors);
//		models[0].initFromPosteriors((HMMSentenceDist[])posteriors[0]);
//		models[1].initFromPosteriors((HMMSentenceDist[])posteriors[1]);
//	}
//	
//	private void initializeModel(HMM[] models) {
//		switch(initType) {
//		case RANDOM: seed = (seed == null? 1 : seed);
//					 System.out.println("Random initialization with seed " + seed);
//					 models[0].initializeRandom(new Random(seed), jitter); 
//					 models[1].initializeRandom(new Random(seed), jitter); 
//					 break;
//		case RANDOM_POSTERIORS: seed = (seed == null? 1 : seed);
//			System.out.println("Random Posterios initialization with seed " + seed);
//			models[0].initializeRandom(new Random(seed), jitter); 
//			models[1].initializeRandom(new Random(seed), jitter); 
//			initFromCommonPosteriors(models);
//			break;
//		default: assert(false) : "Why didn't args4j catch this model-init options error?"; break;
//		}
//		System.out.println();
//	}
//	
//	
//	public HMM[] createModels(ModelType modelType, PosCorpus c){
//		HMM[] models = new HMM[2];
//		if(ModelType.HMM == modelType){
//			models[0] = new PosHMM(c);
//			models[1] = new PosReverseHMM(c);
//		}else if(ModelType.HMMFinalState == modelType){
//			models[0] = new PosHMMFinalState(c);
//			models[1] = new PosReverseHMMFinalState(c);
//		}
//		else{
//			System.out.println("Model is non existing");
//			System.exit(-1);
//		}
//		models[0].updateType = updateParams;
//		models[1].updateType = updateParams;
//		return models;
//	}
//	
//	/**
//	 * 
//	 * @param model
//	 */
//	public void testModel(HMM[] models){
//		InstanceList trainingLR = models[0].corpus.trainInstances;
//		int[][]  gold = new int[trainingLR.instanceList.size()][];
//		for (int i = 0; i < gold.length; i++) {
//			PosInstance inst = (PosInstance) trainingLR.instanceList.get(i);
//			gold[i] = inst.getTags();
//		}
//		PosCorpus c = (PosCorpus) models[0].corpus;
//		int nrHiddenStates = models[0].getNrStates();
//		int nrTags = c.getNrTags();
//		testModelsForType(models, "Viterbi", new ViterbiDecoder(), gold, c, nrHiddenStates, nrTags);
//		testModelsForType(models, "Posterior", new PosteriorDecoder(), gold, c, nrHiddenStates, nrTags);
//		
//	}
//	
//	private void testModelsForType(HMM[] models, String decodeType, ChainDecoder decoder, int[][] gold,
//			PosCorpus corpus, int nrStates, int nrTags){
//		InstanceList trainingLR = models[0].corpus.trainInstances;
//		InstanceList trainingRL = models[1].corpus.trainInstances;
//		System.out.println(decodeType);	
//		int[][] resultsLR = models[0].decode(decoder, trainingLR);
//		printResults(resultsLR, gold, "Final LR " + decodeType, corpus, nrStates, nrTags);
//		int[][] resultsRL = models[1].decode(decoder, trainingRL);
//		printResults(resultsRL, gold, "Final RL " + decodeType, corpus, nrStates, nrTags);
//
//	}
//	
//	private void printResults(int[][] results, int[][] gold, String type, PosCorpus corpus, int nrStates, int nrTags){
//		int[][]  decoding1ToMany =  
//			postagging.evaluation.PosMapping.manyTo1Mapping(corpus, nrStates,
//					results, nrTags,gold);
//		System.out.println(type + " UnSupervised 1 to many" + postagging.evaluation.Evaluator.eval(decoding1ToMany, gold));	
//		int[][]  decoding1to1 =  
//			postagging.evaluation.PosMapping.oneToOnemapping(corpus, nrStates,results, nrTags,gold);
//		System.out.println(type+" UnSupervised 1 to 1" + postagging.evaluation.Evaluator.eval(decoding1to1, gold));
//	}
//	
}
