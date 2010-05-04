package model.chain;

import model.AbstractSentenceDist;


public abstract class ChainSentenceDist extends AbstractSentenceDist{
	
	
	
	
	public abstract int getNumberOfHiddenStates();
	public abstract int getNumberOfPositions();
	public abstract int getWordId(int position);
	

	
	
	/**Access to Parameters*/
	public abstract double getTransitionProbability(int position,int prevState, int state);
	public abstract double getObservationProbability(int position, int state);
	public abstract double getInitProb(int state);
	
	
	/**Access to Posterior*/
	public abstract void makeInference();
//	public abstract void setStatePosterior(int position, int state, double prob);
	public abstract double getStatePosterior(int position, int state);
//	public abstract void setTransitionPosterior(int position, int prevState, int state,double prob);
	public abstract double getTransitionPosterior(int position, int prevState, int state);
	
	/**Cleanup methods*/
	public abstract void clearCaches();
	
	public abstract void clearPosteriors();

	
	@Override
	public String toString() {
		return super.toString();
	}
	public abstract void setLogLikelihood(double l);

}
