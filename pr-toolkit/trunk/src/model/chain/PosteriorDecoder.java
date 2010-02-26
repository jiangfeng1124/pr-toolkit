package model.chain;


import model.chain.hmm.HMMSentenceDist;
import model.chain.hmmFinalState.HMMFinalStateSentenceDist;


public class PosteriorDecoder extends ChainDecoder{

	
	public int[] decodeWithoutInit(ChainSentenceDist sentence) {
		int[] posteriorPath = new int[sentence.getNumberOfPositions()];
		for (int pos = 0; pos < sentence.getNumberOfPositions(); pos++) {
			int maxState = -1;
			double maxProb = 0;
			for (int state = 0; state < sentence.getNumberOfHiddenStates(); state++) {
				double prob = sentence.getStatePosterior(pos,state);
				if(prob > maxProb){
					maxProb = prob;
					maxState = state;
				}
			}
			posteriorPath[pos]=maxState;
		}
		return posteriorPath;
	}


	@Override
	public int[] decode(ChainSentenceDist sentence) {
		sentence.initSentenceDist();
		ForwardBackwardInference inference  = new ForwardBackwardInference((HMMSentenceDist)sentence);
		inference.makeInference();
		return decodeWithoutInit(sentence);
	}

}
