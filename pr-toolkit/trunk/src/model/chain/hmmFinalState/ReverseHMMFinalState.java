package model.chain.hmmFinalState;

import model.AbstractCountTable;
import model.chain.HMMCountTable;
import model.chain.hmm.HMM;
import model.chain.hmm.HMMSentenceDist;
import model.distribution.Multinomial;
import data.Corpus;
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
public abstract class ReverseHMMFinalState extends HMMFinalState{

	
	/**
	 * Initialize multinomial tables
	 * @param nrWordTypes
	 * @param nrHiddenStates
	 */
	public ReverseHMMFinalState(Corpus c,int nrWordTypes, int nrHiddenStates){
		super(c, nrWordTypes, nrHiddenStates);
	}
	
	public HMMSentenceDist getSentenceDist(HMM model, WordInstance inst){
		int[] revWords = util.Array.reverseIntArray(inst.words);
		WordInstance revInst = new WordInstance(revWords,inst.getInstanceNumber());
		return new HMMFinalStateSentenceDist(model,revInst,nrStates);
	}
		
	public void proceddDecodingOutput(int[][] output){
		for (int i = 0; i < output.length; i++) {
			int[] predictedShort = new int[output[i].length-1];
			System.arraycopy(output[i], 0, predictedShort, 0, predictedShort.length);
			output[i] = output[i] = util.Array.reverseIntArray(predictedShort);
		}
	}
}
