package model.chain.mrf;


import util.Array;
import model.chain.ChainSentenceDist;


/**
 * distribution that is used to compute MRF normalizer; the emission factors are just the sums over all words; 
 *
 */
public class NoObsSentenceDist extends ChainSentenceDist{

	double[] initProbs;
	double[][] transitionProbs;
	double[] emissionProbs;
	double[] finalProbs;
	int numStates;
	
	double[][] statePosterior;
	double[][][] transitionPosterior; 
	double logLikelihood;
	
	int numPositions;
	
	public NoObsSentenceDist(double[] init, double[][] transition, double[] emission, double[] finalP, int numPositions){
		this.initProbs = init;
		numStates = initProbs.length;
		this.transitionProbs = transition;
		this.emissionProbs = emission;
		this.finalProbs = finalP;
		this.numPositions = numPositions;
		// have state and transition posteriors at positions+1 because that corresponds to the final state. 
		statePosterior = new double[numPositions+1][getNumberOfHiddenStates()];
		transitionPosterior = new double[numPositions+1][getNumberOfHiddenStates()][getNumberOfHiddenStates()];
	}
	
	@Override
	public void clearCaches() {
	}

	@Override
	public void clearPosteriors() {
	}

	@Override
	public double getLogLikelihood() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void initSentenceDist() {}

	@Override
	public double getInitProb(int state) {
		if(state == numStates) return 0;
		return initProbs[state];
	}

	@Override
	public int getNumberOfHiddenStates() {
		return initProbs.length+1;
	}

	@Override
	public int getNumberOfPositions() {
		return numPositions+1;
	}

	@Override
	public double getObservationProbability(int position, int state) {
		if (position == numPositions){
			if (state == numStates) return 1;
			return 0;
		}
		if (state == numStates) return 0;
		return emissionProbs[state];
	}

	@Override
	public double getStatePosterior(int position, int state) {
		return statePosterior[position][state];
	}

	@Override
	public double getTransitionPosterior(int position, int prevState, int state) {
		double v=transitionPosterior[position][prevState][state];
		return v;
	}

	@Override
	public double getTransitionProbability(int position, int state,
			int nextState) {
		double val = getTransitionHelp(position, state, nextState);
		return val;
	}
	
	public double getTransitionHelp(int position, int state,
			int nextState) {
		if (position == numPositions -1 ){ // we're going from the last 
			// actual position to the dummy "final" position
			if (state == numStates) return 0;// can't transition from final anywhere
			if(nextState == numStates) return finalProbs[state];
			return 0;
		}
		if(nextState == numStates || state == numStates) return 0;
		return transitionProbs[state][nextState];
	}

	@Override
	public int getWordId(int position) {
		// TODO Auto-generated method stub
		return -1;
	}

	@Override
	public void setStatePosterior(int position, int state, double prob) {
		statePosterior[position][state] = prob;
	}

	@Override
	public void setTransitionPosterior(int position, int prevState, int state,
			double prob) {
		transitionPosterior[position][prevState][state] = prob;
	}

	@Override
	public void setLogLikelihood(double l) {
		logLikelihood = l;
	}

	public void checkPosteriors() {
		for (int position = 0; position < statePosterior.length; position++) {
			for (int state = 0; state < statePosterior[position].length; state++) {
				if (Double.isInfinite(statePosterior[position][state])) throw new RuntimeException();
				if (Double.isNaN(statePosterior[position][state])) throw new RuntimeException();
			}
		}
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("NoObsSentenceDist:\n");
		sb.append(util.Printing.doubleArrayToString(initProbs, null, "init-Probs"));
		sb.append(util.Printing.doubleArrayToString(finalProbs, null, "final-Probs"));
		sb.append(util.Printing.doubleArrayToString(emissionProbs, null, "emission-Probs"));
		sb.append(util.Printing.doubleArrayToString(transitionProbs, null ,null, "transition-Probs"));		
		return sb.toString();
	}

}
