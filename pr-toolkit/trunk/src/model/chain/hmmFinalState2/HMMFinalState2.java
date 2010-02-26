package model.chain.hmmFinalState2;

import model.AbstractCountTable;
import model.chain.HMMCountTable;
import model.chain.hmm.HMM;
import model.chain.hmm.HMMSentenceDist;
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
public abstract class HMMFinalState2 extends HMM{

	
	/**
	 * Initialize multinomial tables
	 * @param nrWordTypes
	 * @param nrHiddenStates
	 */
	public HMMFinalState2(Corpus c,int nrWordTypes, int nrHiddenStates){
		corpus = c;
		//Add the extra state
		nrStates = nrHiddenStates+1;

		this.nrWordTypes = nrWordTypes;
		System.out.println("Creating HMMFInal state with " + nrStates);
		initialProbabilities = new Multinomial(1,nrStates);
		transitionProbabilities = new Multinomial(nrStates,nrStates);
		observationProbabilities = new Multinomial(nrStates,nrWordTypes);
		
		
	}
	
	public HMMSentenceDist getSentenceDist(HMM model, WordInstance inst){
		return new HMMFinalStateSentenceDist2(model,inst,nrStates);	
	}

	@Override
	public AbstractCountTable getCountTable() {
		return new HMMCountTable(nrWordTypes,nrStates);
	}
	
	public void proceddDecodingOutput(int[][] output){
		for (int i = 0; i < output.length; i++) {
			int[] predictedShort = new int[output[i].length-1];
			System.arraycopy(output[i], 0, predictedShort, 0, predictedShort.length);
			output[i] = predictedShort;
		}
	}
	public int getNrRealStates(){
		return nrStates-1;
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
}
