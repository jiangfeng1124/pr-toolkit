package model.chain;



public class ViterbiDecoder extends ChainDecoder{

	
	public int[] decodeWithoutInit(ChainSentenceDist sentence) {
		int nrPositions = sentence.getNumberOfPositions();
		int nrTags = sentence.getNumberOfHiddenStates();
		
		//Viterbi trellis keeps the maximum of getting to a state at each position
		double[][] viterbi = new double[nrPositions][nrTags];
		// Backtrack pointers for each position, for each
		//state where was the state where it came before
		int[][] states = new int[nrPositions][nrTags];
		
		//Fill the backpointers to -1 to signal a error
		for (int i = 1; i < nrPositions; i++) {
			java.util.Arrays.fill(states[i], -1);
		}
		
		for (int state = 0; state < nrTags; state++) {
			double prob = sentence.getInitProb(state)
					* sentence.getObservationProbability(0, state);
			viterbi[0][state]=prob;
			states[0][state] = 0;
		}
		
		for (int pos = 1; pos < nrPositions; pos++) {
			for (int state = 0; state < nrTags; state++) {
				double observation = sentence.getObservationProbability(pos, state);
				double prob = 0;
				double max = -1;
				int maxState = -2;
				for (int prevState = 0; prevState < nrTags; prevState++) {
					double viter = viterbi[pos - 1][prevState];
					double dist = sentence.getTransitionProbability(pos,prevState,state);
					prob = viter * dist;
					if (prob > max) {
						max = prob;
						maxState = prevState;
					}

				}
				if(maxState == -2){
					throw new RuntimeException("Viterbi decoding, adding a state of -2");
				}
				viterbi[pos][state]= max * observation;
				states[pos][state] = maxState;
			}
		}

		int[] viterbiPath = new int[nrPositions];
		java.util.Arrays.fill(viterbiPath, -2);

		double max = -1;
		for (int state = 0; state < nrTags; state++) {
			double prob = viterbi[nrPositions - 1][state];
			if (prob > max) {
				max = prob;
				viterbiPath[nrPositions - 1] = state;
			}
		}

		// System.out.println(fSize);
		for (int pos = nrPositions - 2; pos >= 0; pos--) {
			int currentState = viterbiPath[pos + 1];
			//Debug code
			if(currentState == -2){
				throw new RuntimeException("Viterbi decoding, path as a -2 value");
			}
			viterbiPath[pos] = states[pos + 1][currentState];
		}
		return viterbiPath;
	}
	
	@Override
	public int[] decode(ChainSentenceDist sentence) {
		sentence.initSentenceDist();
		int[] decode =  decodeWithoutInit(sentence);
		return decode;
	}
}
