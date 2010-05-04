package model.chain.hmmFinalState;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;

import learning.EM;
import learning.stats.LikelihoodStats;
import model.AbstractCountTable;
import model.chain.PosteriorDecoder;
import model.chain.hmm.HMM;
import model.chain.hmm.HMMCountTable;
import model.chain.hmm.HMMSentenceDist;
import model.chain.hmm.HMM.Update_Parameters;
import model.distribution.Multinomial;
import data.Corpus;
import data.InstanceList;
import data.WordInstance;

/** Implements an HMM
 * which explicitly models a initial state and a final .
 * The final state correspond to the nrStates +1;
 * state.
 * We need to have one parameters to this non existing state.
 * However, we only allow transition to that state 
 * and no transition from that state. And we can only 
 * transit to that state in the case 
 * we are in the last position of the sentence. This is achieved
 * by the HMMFinalState sentence distribution.
 * @author javg
 *
 */
public  class HMMFinalState extends HMM{

	
	
	
	 int trueNumberOfStates;
	/**
	 * Initialize multinomial tables
	 * @param nrWordTypes
	 * @param nrHiddenStates
	 */
	public HMMFinalState(Corpus c,int nrWordTypes, int nrHiddenStates){
		corpus = c;
		//Add the extra state
		trueNumberOfStates = nrHiddenStates;
		nrStates = nrHiddenStates+1;
		this.nrWordTypes = nrWordTypes;
		System.out.println("Creating HMMFInal state with " + nrStates);
		initialProbabilities = new Multinomial(1,trueNumberOfStates);
		transitionProbabilities = new Multinomial(nrStates,nrStates);
		observationProbabilities = new Multinomial(trueNumberOfStates,nrWordTypes);
		
		
	}
	
	
	@Override
	public AbstractCountTable getCountTable() {
		return new HMMFinalStateCountTable(nrWordTypes,nrStates);
	}
	
	public HMMSentenceDist getSentenceDist(HMM model, WordInstance inst){
		return new HMMFinalStateSentenceDist(model,inst,nrStates);	
	}

	
	
	public void proceddDecodingOutput(int[][] output){
		for (int i = 0; i < output.length; i++) {
			int[] predictedShort = new int[output[i].length-1];
			System.arraycopy(output[i], 0, predictedShort, 0, predictedShort.length);
			output[i] = predictedShort;
		}
	}
	public int getNrRealStates(){
		return trueNumberOfStates;
	}
	
	
	/**
	 * Should sum to the number of tokens minus the number of sentences,
	 * since in the final state we are adding a word to every sentence and this is not 
	 * accounted on the number of tokens we do not need to subtract the number of tokens.
	 * @param table
	 */
	public void checkTransitionCounts(Multinomial table){
		double sum = 0;
		for(int i = 0; i < nrStates; i++){
			for(int j = 0; j < nrStates; j++){
				sum += table.getCounts(i, j);
			}
		}
		if(Math.abs(sum - corpus.getNumberOfTokens()) > 1.E-5){
			System.out.println("Transition counts do not sum to the number of tokens minus number of sentences got: "+
					sum + " true: " + (corpus.getNumberOfTokens()));
			throw new RuntimeException();
		}
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
			//Transition to imaginary last state
			count.transitionCounts.addCounts(inst.getTagId(len-1),getNrRealStates(), 1);
		}
		updateParameters(count);
		initialized = true;
		System.out.println("Finished supervised");
	}
	
	
	
	public static void main(String[] args) throws UnsupportedEncodingException, IOException, IllegalArgumentException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Corpus c = new Corpus(args[0]);
		HMMFinalState hmm =new HMMFinalState(c,c.getNrWordTypes(),5);
		hmm.updateType  = Update_Parameters.OBS_MAX_ENT;
		
		hmm.gaussianPrior = 10;
		hmm.gradientConvergenceValue = 0.00001;
		hmm.valueConvergenceValue = 0.0001;
		hmm.maxIter = 1000;
		//Create and add feature function
		hmm.fxy = new model.chain.GenerativeFeatureFunction(c,args[1]);
		hmm.warmStart = false;
		hmm.initializeRandom(new Random(1), 1);
		hmm.printModelParameters();
		System.out.println("Initialized HMM");
		EM em = new EM(hmm);
		LikelihoodStats stats = new LikelihoodStats();
		em.em(2	, stats);
		hmm.printModelParameters();
		PosteriorDecoder decoding = new PosteriorDecoder();
		decoding.decodeSet(hmm, c.trainInstances);
	}
}
