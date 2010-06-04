package model.chain.hmm;

import gnu.trove.TIntArrayList;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

import learning.EM;
import learning.stats.LikelihoodStats;
import model.AbstractCountTable;
import model.AbstractModel;
import model.AbstractSentenceDist;
import model.chain.ChainDecoder;
import model.chain.ForwardBackwardInference;
import model.chain.GenerativeFeatureFunction;
import model.chain.PosteriorDecoder;
import model.chain.mrf.MRFObjective;
import model.distribution.AbstractMultinomial;
import model.distribution.Multinomial;
import optimization.gradientBasedMethods.stats.OptimizerStats;
import optimization.linesearch.InterpolationPickFirstStep;
import optimization.linesearch.WolfRuleLineSearch;
import optimization.stopCriteria.CompositeStopingCriteria;
import optimization.stopCriteria.GradientL2Norm;
import optimization.stopCriteria.NormalizedGradientL2Norm;
import optimization.stopCriteria.ProjectedGradientL2Norm;
import optimization.stopCriteria.StopingCriteria;
import optimization.stopCriteria.ValueDifference;
import util.ArrayMath;
import util.InputOutput;
import data.Corpus;
import data.InstanceList;
import data.WordInstance;

/** Implements an HMM without Final State.
 * 
 * @author javg
 *
 */
public  class HMM extends AbstractModel{

	
	/**
	 * TABLE_UP - All parameters table are updated by normalizing the counts
	 * VM - Variational Bayes with Dirichlet Prior on the parameterts
	 * OBS_MAX_ENT - Train a Max-Ent model for the observation parameters
	 * TRANS_MAX_ENT - Train a Max-Ent model for the transition parameters
	 * MAX_ENT - Train a Max-Ent for both observations and transition parameters
	 * MRF - Train a MRF TODO - This should be a different model
	 */
	public enum Update_Parameters  {TABLE_UP, OBS_MAX_ENT, TRANS_MAX_ENT, MAX_ENT,VB, MRF};
	
	public Update_Parameters updateType = Update_Parameters.TABLE_UP;
	public AbstractMultinomial initialProbabilities;
	public AbstractMultinomial transitionProbabilities;
	public AbstractMultinomial observationProbabilities;
	
	protected int nrStates;
	protected int nrWordTypes;
	
	protected boolean initialized;

	public Corpus corpus;
	
	//Optimization and maxent options
	public double gradientConvergenceValue;
	public double valueConvergenceValue;
	public int maxIter = 1000;
	public double gaussianPrior = 1;
	//Feature function used for Max Ent
	public model.chain.GenerativeFeatureFunction fxy = null; 
	public boolean warmStart=true;
	
	
	
	//Variational Baeys parameters
	public double stateToStatePrior = 000.1;
	public double stateToObservationPrior = 000.1;
	
	
	
	protected 	HMM(){
		
	}
	
	
	
	
	
	/**
	 * Initialize multinomial tables
	 * @param nrWordTypes
	 * @param nrHiddenStates
	 */
	public HMM(Corpus c,int nrWordTypes, int nrHiddenStates){
		corpus = c;
		this.nrWordTypes = nrWordTypes;
		//Note does not have to be the same as the number of true tags
		this.nrStates = nrHiddenStates;
		initialProbabilities = new Multinomial(1,nrStates);
		//Model P(S_t|S_t-1)
		transitionProbabilities = new Multinomial(nrStates,nrStates);
		//Models p(obs|state)
		observationProbabilities = new Multinomial(nrStates,nrWordTypes);
	}
	

	public int getNrRealStates(){
		return nrStates;
	}

	@Override
	public void addToCounts(AbstractSentenceDist sd, AbstractCountTable counts) {
		HMMSentenceDist dist = (HMMSentenceDist)sd;
		dist.updateInitCounts((HMMCountTable) counts);
		dist.updateTransitionCounts((HMMCountTable) counts);
		dist.updateObservationCounts((HMMCountTable) counts);
		//dist.clearPosteriors();		
	}

	@Override
	public void computePosteriors(AbstractSentenceDist dist) {
		HMMSentenceDist d = (HMMSentenceDist)dist;
		d.makeInference();
	}
	
	@Override
	public AbstractSentenceDist[] getSentenceDists() {
		return getSentenceDists(corpus.trainInstances);
	}

	
	@Override
	public AbstractSentenceDist[] getSentenceDists(InstanceList inst) {
		HMMSentenceDist[] sentences = new HMMSentenceDist[inst.instanceList.size()];
		for (int i = 0; i < sentences.length; i++) {
			sentences[i] = getSentenceDist(this, inst.instanceList.get(i));
		}
		return sentences;
	}
	
	// FIXME: Why does this method take a model as input?
	public HMMSentenceDist getSentenceDist(HMM model, WordInstance inst){
		return new HMMSentenceDist(model,inst,nrStates);	
	}
	
	@Override
	public AbstractCountTable getCountTable() {
		return new HMMCountTable(nrWordTypes,nrStates);
	}

	/**
	 * Checks if the counts tables make sense:
	 * Initial Counts - Should sum to the number of sentences
	 * Transition Counts - Should sum to the number of tokens - number of sentences
	 * State Counts - Should sum to the number of tokens
	 * @param counts
	 */
	public void checkCountsTable(AbstractCountTable counts){
		System.out.println("DEBUG:::Checking if counts are correct");
		checkInitialCounts(((HMMCountTable)counts).initialCounts);
		checkObservationCounts(((HMMCountTable)counts).observationCounts);
		checkTransitionCounts(((HMMCountTable)counts).transitionCounts);
	}
	
	public void checkObservationCounts(Multinomial table){
		double sum = 0;
		for(int i = 0; i < getNrRealStates(); i++){
			for(int j = 0; j < corpus.getNrWordTypes(); j++){
				sum += table.getCounts(i, j);
			}
		}
		if(Math.abs(sum - corpus.getNumberOfTokens()) > 1.E-5){
			System.out.println("Transition counts do not sum to the number of tokens");
			throw new RuntimeException();
		}
	}
	
	//InitialCounts should sum to the number of sentences
	public void checkInitialCounts(Multinomial table){
		double sum = 0;
		for(int i = 0; i < getNrRealStates(); i++){
			sum += table.getCounts(0, i);
		}
		if(Math.abs(sum - corpus.getNrOfTrainingSentences()) > 1.E-5){
			System.out.println("Initial counts do not sum to the number of sentences got: " + sum + " truth: " + corpus.getNrOfTrainingSentences());
			throw new RuntimeException();
		}
	}
	
	/**
	 * Should sum to the number of tokens minus the number of sentences
	 * @param table
	 */
	public void checkTransitionCounts(Multinomial table){
		double sum = 0;
		for(int i = 0; i < nrStates; i++){
			for(int j = 0; j < nrStates; j++){
				sum += table.getCounts(i, j);
			}
		}
		if(Math.abs(sum - corpus.getNumberOfTokens() + corpus.getNrOfTrainingSentences()) > 1.E-5){
			System.out.println("Transition counts do not sum to the number of tokens minus number of sentences got: "+
					sum + " true: " + (corpus.getNumberOfTokens() - corpus.getNrOfTrainingSentences()));
			throw new RuntimeException();
		}
	}
	
	
	/**
	 * For now just normalize but this should be a class
	 * so we can switch between variational, maxEntropy, or
	 * tabular
	 * TODO: Just this be a part of EM???
	 */
	@Override
	public void updateParameters(AbstractCountTable counts) {
//		((HMMCountTable) counts).print();
		//Debug Check Counts
//		checkCountsTable(counts);
//		((HMMCountTable)counts).initialCounts.print("Initial Counts", null,null);
//		((HMMCountTable)counts).transitionCounts.print("Transition Counts", null,null);
//		((HMMCountTable)counts).observationCounts.print("Observation Counts", null,null);
		
		if(updateType == Update_Parameters.TABLE_UP){
			initialProbabilities.copyAndNormalize(((HMMCountTable)counts).initialCounts);
			transitionProbabilities.copyAndNormalize(((HMMCountTable)counts).transitionCounts);
			observationProbabilities.copyAndNormalize(((HMMCountTable)counts).observationCounts);
		}else if(updateType == Update_Parameters.OBS_MAX_ENT){
			initialProbabilities.copyAndNormalize(((HMMCountTable)counts).initialCounts);
			transitionProbabilities.copyAndNormalize(((HMMCountTable)counts).transitionCounts);
			
			maxEntRetrain(observationProbabilities, ((HMMCountTable)counts).observationCounts, 
					corpus,fxy, 
					gaussianPrior, 
					gradientConvergenceValue, 
					valueConvergenceValue,
					maxIter);
		}else if(updateType == Update_Parameters.MAX_ENT){
			throw new UnsupportedOperationException("Update method not implemented");	
		}else if(updateType == Update_Parameters.TRANS_MAX_ENT){
			throw new UnsupportedOperationException("Update method not implemented");	
		}else if(updateType == Update_Parameters.VB){
			variationalBaeysParameterUpdates(counts);
		}else if(updateType == Update_Parameters.MRF){
			mrfRetrain(counts);

		}else{
			System.out.println("Update Counts method not implemented: " + updateType);
			System.exit(-1);
		}
		//printModelParameters();
	}
	


	public void initializeRandom(Random r,double jitter){
		initialProbabilities.initializeRandom(r,jitter);
		transitionProbabilities.initializeRandom(r,jitter);
		observationProbabilities.initializeRandom(r,jitter);
		initialized = true;
//		printModelParameters();
//		System.exit(-1);
	}

	
	/**
	 * Initialize from data
	 * @param smoothing
	 * @param nrSentences
	 * @param nrHiddenStates
	 * @param wordSentences
	 * @param tagSentences
	 */
	public void initializeSupervised(double smoothing, int nrHiddenStates, InstanceList list){
		System.out.println("Initializing supervised");
		if(getNrRealStates() != nrHiddenStates){
			System.out.println("Number of hidden states from supervised data does not match HMM number of hidden states");
			System.exit(-1);
		}
		HMMCountTable count = (HMMCountTable) getCountTable();
		count.fill(smoothing);
		for (WordInstance inst : list.instanceList) {
			int len = inst.getNrWords();
			count.initialCounts.addCounts(0, inst.getTagId(0), 1);
			count.observationCounts.addCounts(inst.getTagId(0),inst.getWordId(0), 1);
			for(int pos = 1; pos < len; pos++){
				count.transitionCounts.addCounts(inst.getTagId(pos-1),inst.getTagId(pos), 1);
				count.observationCounts.addCounts(inst.getTagId(pos),inst.getWordId(pos), 1);
			}
		}
		updateParameters(count);
		initialized = true;
	//	printModelParameters();
		System.out.println("Finished supervised");
	}
	
	
	public void initFromPosteriors(HMMSentenceDist[] sd){
		HMMCountTable counts = (HMMCountTable)getCountTable();
		for (int i = 0; i < sd.length; i++) {
			addToCounts(sd[i], counts);
		}
		updateParameters(counts);
	}
	
	
	public AbstractSentenceDist[] getRandomPosteriors(Random r,double jitter){
		AbstractSentenceDist[] sentences = getSentenceDists(corpus.trainInstances);
		for (int i = 0; i < sentences.length; i++) {
			HMMSentenceDist sd = (HMMSentenceDist) sentences[i];
			sd.createRandomPosteriors();
		}
		
		return sentences;
	}
	
	public void printModelParameters(){
		initialProbabilities.print("Initial Parameters",null,null);
		transitionProbabilities.print("Transition Parameters",null,null);
		//observationProbabilities.print("Observatiob Parameters",null,null);
	}
	
	private void mrfRetrain(AbstractCountTable counts) {
		TIntArrayList lengths = new TIntArrayList();
		for (int i = 0; i < corpus.trainInstances.instanceList.size(); i++) {
			int len = corpus.trainInstances.instanceList.get(i).words.length;
			while(len >= lengths.size()) 
				lengths.add(0);
			//System.out.println(lengths.size()+" "+len);
			lengths.set(len, lengths.get(len)+1);
		}
		MRFObjective mrf = new MRFObjective((HMMCountTable)counts, lengths.toNativeArray(), corpus, fxy, gaussianPrior);
		// potentially the following should really go somewhere else... 
		WolfRuleLineSearch wolfe = new WolfRuleLineSearch(new InterpolationPickFirstStep(1),0.001,0.9);
		
		wolfe.setDebugLevel(0);
		// LineSearchMethod ls = new ArmijoLineSearchMinimization();
		optimization.gradientBasedMethods.LBFGS optimizer = 
			new optimization.gradientBasedMethods.LBFGS(wolfe,30);
		optimization.gradientBasedMethods.stats.OptimizerStats stats = new OptimizerStats();
		StopingCriteria stopGrad = new NormalizedGradientL2Norm(gradientConvergenceValue);
		StopingCriteria stopValue = new ValueDifference(valueConvergenceValue);
		CompositeStopingCriteria stop = new CompositeStopingCriteria();
		stop.add(stopGrad);
		stop.add(stopValue);
		optimizer.setMaxIterations(maxIter);
		boolean succed = optimizer.optimize(mrf,stats,stop);
		System.out.println("Suceess " + succed + "/n"+stats.prettyPrint(0));
		if(nrStates == getNrRealStates()){
			System.out.println("WARNING!!! MRF NOT SET UP TO DEAL WITH non-final state HMM!!!");
		}
		// save the resulting values...
		double[] initials = new double[getNrRealStates()];
		double[] finals = new double[getNrRealStates()];
		for (int state = 0; state < getNrRealStates(); state++) { 
			initials[state] = mrf.getInitial(state);
			finals[state] = mrf.getFinal(state);
		}
		ArrayMath.plusEquals(initials, -ArrayMath.max(initials));
		ArrayMath.plusEquals(finals, -ArrayMath.max(finals));
		ArrayMath.exponentiate(initials);
		ArrayMath.exponentiate(finals);
		for (int state = 0; state < initials.length; state++) { 
			initialProbabilities.setCounts(0, state, initials[state]);
			transitionProbabilities.setCounts(state, getNrRealStates(), finals[state]);
		}
		initialProbabilities.setCounts(0, getNrRealStates(), 0);
		// transition parameters... 
		double max = 0;
		double[][] transitions = new double[getNrRealStates()][getNrRealStates()];
		for (int state = 0; state < getNrRealStates(); state++) {
			for (int nextState = 0; nextState < getNrRealStates(); nextState++) {
				transitions[state][nextState] = mrf.getTransition(state, nextState);
				max = Math.max(max , transitions[state][nextState]);
			}
		}
		for (int state = 0; state < getNrRealStates(); state++) {
			for (int nextState = 0; nextState < getNrRealStates(); nextState++) {
				transitions[state][nextState] -= max;
				transitionProbabilities.setCounts(state, nextState, Math.exp(transitions[state][nextState]));
			}
			// final probabilities set above (i.e. transitions[state][#states])
		}
		for (int nextState = 0; nextState < getNrRealStates(); nextState++) {
			// final state can't transition to anything. 
			transitionProbabilities.setCounts(getNrRealStates(), nextState, 0);
		}
		// Observation params.. 
		double[][] observations = new double[getNrRealStates()][corpus.getNrWordTypes()];
		max = 0;
		for (int state = 0; state < observations.length; state++) {
			for (int w = 0; w < observations[state].length; w++) {
				observations[state][w] = mrf.getEmissionDot(state, w);
				max = Math.max(max, observations[state][w]);
			}
		}
		for (int state = 0; state < observations.length; state++) {
			for (int w = 0; w < observations[state].length; w++) {
				observations[state][w] -= max;
				observationProbabilities.setCounts(state, w, Math.exp(observations[state][w]));
			}
		}
		for (int w = 0; w < corpus.getNrWordTypes(); w++) {
			observationProbabilities.setCounts(getNrRealStates(), w, 0);
		}
	}
	
	/**
	 * This method updates the observation counts using a maximum entropy model instead of just 
	 * normalizing the counts. This way we can add features about morphology of the words.
	 * 
	 */
	public util.LinearClassifier[] maxEntModels;	
	//Weigths of the observations 
	double counts[];
	
	public void maxEntRetrain(AbstractMultinomial toUpdate, AbstractMultinomial other,Corpus c,GenerativeFeatureFunction fxy, double gaussianPrior
			,double gradientPrecision,double valuePrecision,int maxIterations){
		if(other instanceof Multinomial){
			maxEntRetrain((Multinomial)toUpdate, (Multinomial)other, c, fxy, gaussianPrior, gradientPrecision, valuePrecision, maxIterations);
		}		
	}
	
	public void maxEntRetrain(Multinomial toUpdate, Multinomial other,Corpus c,GenerativeFeatureFunction fxy, double gaussianPrior
			,double gradientPrecision,double valuePrecision,int maxIterations){
		util.DummyAlphabet dummyAlphabet = new util.DummyAlphabet(c.getNrWordTypes());
		model.chain.GenerativeMaxEntropy maxEnt = new model.chain.GenerativeMaxEntropy(gaussianPrior,dummyAlphabet,dummyAlphabet,fxy);
		util.SparseVector x = new util.SparseVector();
		if (maxEntModels == null){
			maxEntModels = new util.LinearClassifier[nrStates];
			for(int state = 0; state < nrStates; state++){
				maxEntModels[state] = new util.LinearClassifier(dummyAlphabet,dummyAlphabet,fxy);
			}
		}
		else if(!warmStart){
			System.out.println("Restarting ME weights");
			//Fill parameters with zero
			for(int state = 0; state < nrStates; state++){
				java.util.Arrays.fill(maxEntModels[state].w, 0);
			}
		}
		//For each hidden state train a maximum entropy model
		for(int state = 0; state < getNrRealStates(); state++){
			//Get the weights from the count table for each word
			if(counts == null){
				counts = new double[c.getNrWordTypes()];
			}
			for(int i = 0; i < c.getNrWordTypes(); i++){
				counts[i]=other.getCounts(state, i);
			}
			//Train the classifier 
			util.LinearClassifier classifier = maxEnt.batchTrain(counts,maxEntModels[state],gradientPrecision,valuePrecision,maxIterations);	
			//Predict
			classifier.scores(x);
			double[] scores = classifier.scores(x);
			
			//Update the counts table
			double[] probs = util.StaticUtils.exp(scores);
			double Z = util.StaticUtils.sum(probs);
			for(int i = 0; i < c.getNrWordTypes(); i++){
				
				double prob = probs[i]/Z;
				if(Double.isNaN(prob) || Double.isInfinite(prob)){
					System.out.println("Error probabililty of max-ent is nan");
					throw new RuntimeException();
				}
				toUpdate.setCounts(state, i, prob);
			}
			
			//Debug compute the kl between the probs of doing ME vs the probs of doing ML
			double MLsum = util.StaticUtils.sum(counts);
			double kl = 0;
			for(int i = 0; i < c.getNrWordTypes(); i++){
				double prob = probs[i]/Z;
				double mlProb = counts[i]/MLsum;
				double logP = Math.log(prob/mlProb);
//				System.out.println("prob: " + prob + " mlProb " + mlProb + " kl " + prob*logP);
				kl+= prob*logP;
			}
			System.out.println("Kl between ME and ML estimates for state "+state+": " + kl);	
		}	
	}
	
	public int[][] decode(ChainDecoder decoder, InstanceList list){
		int[][] output = decoder.decodeSet(this, list);
		proceddDecodingOutput(output);
		return output;
	}
	
	public void proceddDecodingOutput(int[][] output){
	}
	
	public void variationalBaeysParameterUpdates(AbstractCountTable countsTable){
		//Observation counts
		for(int state = 0; state < getNrRealStates(); state++){
			double sum=0;
			for(int observation = 0; observation < corpus.getNrWordTypes(); observation++){
				double counts = ((HMMCountTable)countsTable).observationCounts.getCounts(state, observation);
				sum += counts;
			}
			double newSum  = util.DigammaFunction.expDigamma(sum+corpus.getNrWordTypes()*stateToObservationPrior);
			if(newSum < 1.E-100 || Double.isNaN(newSum) || Double.isInfinite(newSum)){
				System.out.println("Probem normalizing Observation table newSum is NAN or zero.... Keeping old version");
			}else{
				for(int observation = 0; observation <corpus.getNrWordTypes(); observation++){
					double counts = ((HMMCountTable)countsTable).observationCounts.getCounts(state, observation);
					double newCounts = util.DigammaFunction.expDigamma(counts+stateToObservationPrior);
					observationProbabilities.setCounts(state, observation, newCounts/newSum);
				}
			}
		}
		
		//Transition counts -- In this case must all the states since in case of a fake 
		// parameter we have a position for it
		for(int currentState = 0; currentState < nrStates; currentState++){
			double sum=0;
			for(int nextState = 0; nextState < nrStates; nextState++){
				double counts = ((HMMCountTable)countsTable).transitionCounts.getCounts(currentState, nextState);
				sum += counts;
			}
			double newSum  = util.DigammaFunction.expDigamma(sum+nrStates*stateToStatePrior);
			if(newSum < 1.E-100 || Double.isNaN(newSum) || Double.isInfinite(newSum)){
				System.out.println("Probem normalizing transition table newSum is NAN or zero.... Keeping old version");
			}else{
				for(int nextState = 0; nextState < nrStates; nextState++){
					double counts = ((HMMCountTable)countsTable).transitionCounts.getCounts(currentState, nextState);
					double newCounts = util.DigammaFunction.expDigamma(counts+stateToStatePrior);
					transitionProbabilities.setCounts(currentState, nextState, newCounts/newSum);
				}
			}
		}
		
		double sum=0;
		for(int state = 0; state < getNrRealStates(); state++){
			sum += ((HMMCountTable)countsTable).initialCounts.getCounts(0,state);
		}
		sum = util.DigammaFunction.expDigamma(sum+getNrRealStates()*stateToStatePrior);

		for(int state = 0; state < getNrRealStates(); state++){
			double counts = ((HMMCountTable)countsTable).initialCounts.getCounts(0,state);
			double newCounts = util.DigammaFunction.expDigamma(counts+stateToStatePrior);
			initialProbabilities.setCounts(0,state, newCounts/sum);
		}
	}
	
	public void printStamp(PrintStream file){
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        java.util.Date date = new Date();
        System.out.println("Current Date Time : " + dateFormat.format(date));
		
	}
	
	public void saveProperties(PrintStream file) throws IOException{
        Properties props = new Properties(); 
        props.put("nrStates", nrStates);
        props.put("nrWordTypes", nrWordTypes);
        props.store(file, "");
		
	}
	
	public void saveModel(String directory) throws IOException{
		if(!util.FileSystem.createDir(directory)){
			System.out.println("Failed to create directory not saving");
			return;
		}
		corpus.saveDescription(directory);
		try {
			PrintStream file = new PrintStream(new FileOutputStream(directory+ "stamp"));
			printStamp(file);
		} catch (FileNotFoundException e) {
			System.out.println("Could not save model");
			System.exit(-1);
		}
		PrintStream properties = new PrintStream(new FileOutputStream(directory+ "properties"));
		saveProperties(properties);
		initialProbabilities.saveTable(directory+"/initProb");
		observationProbabilities.saveTable(directory+"/obsProb");
		transitionProbabilities.saveTable(directory+"/transProb");
	}
	
	public static void main(String[] args) throws UnsupportedEncodingException, IOException, IllegalArgumentException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Corpus c = new Corpus(args[0]);
		HMM hmm = new HMM(c,c.getNrWordTypes(),10);
//		hmm.updateType  = Update_Parameters.OBS_MAX_ENT;
//		hmm.warmStart = true;
//		hmm.gaussianPrior = 1;
//		//Create and add feature function
//		hmm.fxy = new model.chain.GenerativeFeatureFunction(c,args[1]);
		hmm.initializeRandom(new Random(), 1);
		System.out.println("Initialized HMM");
		EM em = new EM(hmm);
		LikelihoodStats stats = new LikelihoodStats();
		em.em(5	, stats);
		hmm.printModelParameters();
		PosteriorDecoder decoding = new PosteriorDecoder();
		decoding.decodeSet(hmm, c.trainInstances);
	}
	
}
