package model.chain.hmm;


import model.chain.ChainSentenceDist;
import model.chain.ForwardBackwardInference;
import data.WordInstance;


/**
 * Represents a final sentence distribution for an HMM.
 * The particular sentence as sentence of the instance, 
 * we add an extra position to reflect the end 
 * position. Note that there are no parameters for this position.
 * @author javg
 *
 */
public class HMMSentenceDist extends ChainSentenceDist{	
	
	
	ForwardBackwardInference inference;
	
	//Caches
	//Index per position/state
	public double observationCache[][];
	//Index per prevState/nextState
	public double transitionCache[][];
	//Index per state
	public double initialCache[];
	
	//Posterior distributions
	//position, state
	public double observationPosterior[][];
	//position, prevState,state
	//public double transitionPosterior[][][];

	public double logLikelihood;
	
	protected int sentenceSize;	

	public int nrHiddenStates;
	
	public HMM model;
	public WordInstance instance;
	
	public String toString() {
		StringBuffer res = new StringBuffer();
		res.append("HMM sentence distr");
		res.append("Sentence size" + sentenceSize);
		res.append("nrHiddenStates " + nrHiddenStates);
		res.append(instance.toString());
		res.append(util.Printing.doubleArrayToString(initialCache,null, "init cache"));
		double[][] observ = new double[observationCache.length][observationCache[0].length];
		for (int i = 0; i < observ.length; i++) {
			for (int j = 0; j < observ[i].length; j++) {
				observ[i][j] = getObservationProbability(i, j);
			}
		}
//		res.append(util.Printing.doubleArrayToString(observationCache, null,null,"observation cache"));
		res.append(util.Printing.doubleArrayToString(observ, null,null,"observation cache"));
		double[][] trans = new double[transitionCache.length][transitionCache[0].length];
		for (int i = 0; i < trans.length; i++) {
			for (int j = 0; j < trans[0].length; j++) {
				trans[i][j] = getTransitionProbability(getNumberOfPositions()-1, i, j);
			}
		}
		res.append(util.Printing.doubleArrayToString(trans, null,null,"end transition Probs"));
		res.append(util.Printing.doubleArrayToString(transitionCache, null,null,"transition cache"));
		return res.toString();
	}

	
	protected HMMSentenceDist(){
		
	}
	
	/**
	 * 
	 * @param model
	 */
	public HMMSentenceDist(HMM model, WordInstance inst, int nrHiddenStates) {
		this.model = model;
		instance = inst;
		sentenceSize = inst.getNrWords();
		this.nrHiddenStates = nrHiddenStates;

	}
	@Override
	public void initSentenceDist(){
//		System.out.println("Calling init caches");
		makeInitCache();
		makeTransitionCache();
		makeObservationCahce();
		//Create Posterior Objects
		//observationPosterior = new double[sentenceSize][nrHiddenStates];
		//transitionPosterior=new double[sentenceSize-1][nrHiddenStates][nrHiddenStates];
	}
	
	public void clearCaches(){
		initialCache = null;
		observationCache = null;
		transitionCache = null;
	}
	
	public void clearPosteriors(){
		observationPosterior = null;
		inference.forward = null;
		inference.backward = null;
		//transitionPosterior = null;
	}
	
	public void makeObservationCahce(){
		observationCache=new double[sentenceSize][nrHiddenStates];
		for(int wordPos = 0;  wordPos < sentenceSize; wordPos++){
			for(int tagID = 0;  tagID < nrHiddenStates; tagID++){
				double prob = model.observationProbabilities.getCounts(tagID,instance.getWordId(wordPos));
				observationCache[wordPos][tagID]=prob;
			}
		}
	}
	
	public void makeInitCache(){
		initialCache=new double[nrHiddenStates];
		for(int tagID = 0;  tagID < model.getNrRealStates(); tagID++){
			double prob = model.initialProbabilities.getCounts(0,tagID);
			initialCache[tagID]=prob;
		}
	}
	
	public void makeTransitionCache(){
		transitionCache=new double[nrHiddenStates][nrHiddenStates];
		for(int previousTagId = 0;  previousTagId < nrHiddenStates; previousTagId++){
			for(int tagID = 0;  tagID < nrHiddenStates; tagID++){
				double prob = model.transitionProbabilities.getCounts(previousTagId,tagID);
				transitionCache[previousTagId][tagID]=prob;
			}
		}
	}
	
	
	/**
	 * If state is bigger than true hiddenState then we are in the non existing state and 
	 * we return zero
	 */
	public double getTransitionProbability(int position,int prevState, int state) {
		if(transitionCache !=  null){
			return transitionCache[prevState][state];
		}else{
			return model.transitionProbabilities.getCounts(prevState,state);
		}
	}

	public double getObservationProbability(int position, int state) {
		if(observationCache != null){
			return observationCache[position][state];
		}else{
			return model.observationProbabilities.getCounts(state,instance.getWordId(position));
		}
	}

	public double getInitProb(int state) {
		if(initialCache != null){
			return initialCache[state];
		}else{
			return model.initialProbabilities.getCounts(0,state);
		}
	}

	@Override
	public double getStatePosterior(int position, int state) {
		return observationPosterior[position][state];
	}

	
	@Override
	public void makeInference() {
//		System.out.println("calling hmm make inference");
		inference = new ForwardBackwardInference(this);
		inference.makeInference();	
		makeStatePosteriors();
		//debug
		//checkStatePosteriors();
	}
	
	public void makeStatePosteriors(){
//		System.out.println("Making state posteriors");
		if(observationPosterior == null){
//			System.out.println("Building new array for  state posteriors");
			observationPosterior = new double[sentenceSize][nrHiddenStates];
		}
		for (int observation = 0; observation < sentenceSize; observation++) {
			for (int state = 0; state < nrHiddenStates; state++) {
				//Must multiply by the likelihood scallor since its overcounting it.
				double prob = inference.forward[state][observation]*
				inference.backward[state][observation]*inference._inverseLikelihoodScalors[observation];
				if (prob < 0) {
					observationPosterior[observation][state] = 0;
				} else {
					observationPosterior[observation][state] = prob;
				}
			}
		}	
	}
	
	//State posteriors should sum to one for each position when summed all over states
	public void checkStatePosteriors(){
		System.out.println("checking state posteriors");
		
		for (int observation = 0; observation < sentenceSize; observation++) {
			double sum = 0;
			for (int state = 0; state < nrHiddenStates; state++) {
				sum+= observationPosterior[observation][state];
			}
			//debugging code
			if(Math.abs(1-sum) > 1.E-5){
				System.out.println("State posteriors are not correct");
				throw new RuntimeException();
			}
		}
	}
	
	@Override
	public double getTransitionPosterior(int position, int prevState, int state) {
		double transition = getTransitionProbability(position,prevState,state);                                                                                                        					
		 double observation = getObservationProbability(position + 1,  state);                                                                                                                
		 double alpha = inference.forward[prevState][position];                                                                                         
		 double beta = inference.backward[state][position + 1];                                                                                        
		 double epsilon = alpha * transition * observation * beta;
		 return epsilon;
	}

//	@Override
//	public void setStatePosterior(int position, int state, double prob) {
//			observationPosterior[position][state] = prob;
//	}

//	@Override
//	public void setTransitionPosterior(int position, int prevState, int state,
//			double prob) {
//			transitionPosterior[position][prevState][state] = prob;
//	}

	@Override
	public int getNumberOfHiddenStates() {
		return nrHiddenStates;
	}

	@Override
	public int getNumberOfPositions() {
		return sentenceSize;
	}

	

	@Override
	public int getWordId(int position) {
			return instance.getWordId(position);
	}

	@Override
	public double getLogLikelihood() {
		return logLikelihood;
	}
	
	
	public void updateObservationCounts(HMMCountTable counts) {
		for (int state = 0; state < getNumberOfHiddenStates(); state++) {
			for (int pos = 0; pos < getNumberOfPositions(); pos++) {
				double prob = getStatePosterior(pos, state);
				if(Double.isInfinite(prob) || Double.isNaN(prob)){
					System.out.println("Updating counts for translation prob not a number");
					prob=0;
				}
				counts.observationCounts.addCounts(state,getWordId(pos),prob);
			}
		}		
	}
	
	public void updateTransitionCounts(HMMCountTable counts) {
		if (getNumberOfPositions() == 1)
			return;	
		for (int currentState = 0; currentState <getNumberOfHiddenStates(); currentState++) {
			for (int nextState = 0; nextState < getNumberOfHiddenStates(); nextState++) {
				double epsilonSum = 0;
				for (int pos = 0; pos < getNumberOfPositions() - 1; pos++) {
					epsilonSum += getTransitionPosterior(pos, currentState, nextState);
				}
				double prob = epsilonSum;
				if(Double.isNaN(prob) || Double.isInfinite(prob)){
					System.out.println("Updating counts for transition prob not a number epsilon" + epsilonSum );
					prob =0;
					model.printModelParameters();
					printStatePosteriors();
					System.exit(-1);
				}
					counts.transitionCounts.addCounts(currentState, nextState, prob);
			}
		}
		
	}
	
	public void updateInitCounts(HMMCountTable counts) {
		for (int state = 0; state < model.getNrRealStates(); state++) {
			double prob =  getStatePosterior(0, state);
			if(Double.isInfinite(prob) || Double.isNaN(prob)){
				System.out.println("Update init counts not a number");
				prob=0;
				model.printModelParameters();
				printStatePosteriors();
				System.exit(-1);
			}
			counts.initialCounts.addCounts(0, state, prob);
		}
		
	}

	public void printStatePosteriors(){
		System.out.println(statePosteriorToString());
		
	}
	
	public String statePosteriorToString(){
		return util.Printing.doubleArrayToString(observationPosterior, null,null,"observation posteriors");
		
	}
	
//	public String transitionPosteriorToString(){
//		StringBuffer sb = new StringBuffer();
//		for (int i = 0; i < sentenceSize-1; i++) {
//			sb.append(util.Printing.doubleArrayToString(transitionPosterior[i], null,null,"transition Posteriors pos " +i));
//		}
//		return sb.toString();	
//	}
	
//	public void printTransitionPosteriors(){
//		System.out.println(transitionPosteriorToString());
//	}	
	public void createRandomPosteriors(){
		System.out.println("CreateRandomPosteriors Not Implemented");
		System.exit(-1);
	}


	@Override
	public void setLogLikelihood(double l) {
		logLikelihood = l;
	}
}
