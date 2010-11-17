package model.chain.hmm;

import data.Corpus;
import data.WordInstance;


/**
 * Implements an HMM that reads sentences from right to left.
 * The only changed is when creating HMMSentenceDist we reverse
 * the order of the words, and on preprocessing after decoding 
 * where we put them correctly in the same order.
 * @author javg
 *
 */
public class ReverseHMM extends HMM{
	public ReverseHMM(Corpus c,int nrWordTypes, int nrHiddenStates){
		super(c,nrWordTypes,nrHiddenStates);
	}
	

	
	
	public HMMSentenceDist getSentenceDist(HMM model, WordInstance inst){
		int[] revWords = util.Array.reverseIntArray(inst.words);
		WordInstance revInst = new WordInstance(revWords,inst.getInstanceNumber());
		return new HMMSentenceDist(model,revInst,nrStates);	
	}
	
	public void proceddDecodingOutput(int[][] output){
		for (int i = 0; i < output.length; i++) {
			output[i] = util.Array.reverseIntArray(output[i]);
		}
	}
	
	
}
