package model.chain;

import util.Printing;




/**
 * Computes forward backward for a chain model
 * Uses the caches passed as arguments 
 * and fills the posterior receive as arguments.
 * Return the likelihood of the given sentence
 * @author javg
 *
 */
public class ForwardBackwardInference {

	/**
	 * Index by hidden state / position
	 */
	public double [][] forward;
	public double [][] backward;
	
	/** Scalors indexed by position */
	public double[] _inverseLikelihoodScalors;
	public double _scaledLikelihood;
	
	/** LogLikelihood of this particular instance */
	public double _logLikelihood;
	
	int nrHiddenStates;
	int nrPositions;
	
	/** Particular instance */
	ChainSentenceDist sd;
	
	public ForwardBackwardInference(ChainSentenceDist sd){
		nrHiddenStates = sd.getNumberOfHiddenStates();
		nrPositions = sd.getNumberOfPositions();
		forward = new double[nrHiddenStates][nrPositions];
		
		backward = new double[nrHiddenStates][nrPositions];
		_inverseLikelihoodScalors=new double[nrPositions];
//		for(int i = 0; i < nrPositions; i++){
//			java.util.Arrays.fill(forward[i],0);
//			java.util.Arrays.fill(backward[i],0);
//		}
//		java.util.Arrays.fill(_inverseLikelihoodScalors,0);
		this.sd = sd;
	}
	
	public void makeInference(){
//		System.out.println("calling FB make inference");
		makeForwardTables();
		makeBacwardTables();
//		sanityCheckMakeForwardTables();
//		sanityCheckMakeBacwardTables();
//		makeStatePosteriors(sd);
//		makeTransitionPosteriors(sd);
		sd.setLogLikelihood(_logLikelihood);
		//util.Printing.printDoubleArray(((HMMSentenceDist)sd).observationPosterior ,null,null,"posteriors");
	}
	
	/**
	 * Scaled backward tables. Rabineri 97
	 */
	public void makeBacwardTables() {
		for (int state = 0; state < nrHiddenStates; state++) {
			backward[state][nrPositions-1]= 1/_inverseLikelihoodScalors[nrPositions-1];
		}
		for (int pos = nrPositions - 2; pos >= 0; pos--) {
			for (int state = 0; state < nrHiddenStates; state++) {
				double prob = 0;
				for (int nextState = 0; nextState < nrHiddenStates; nextState++) {
					double transition = sd.getTransitionProbability(pos,state,nextState);
					double observation = sd.getObservationProbability(pos + 1,nextState);
					double back = backward[nextState][pos + 1];
					prob += transition * observation * back;
				//	System.out.println(" state " + state + " nextState " + nextState + " state size " + _tagSetSize
				//			+ "pos " + pos + "current sentence size" + _currentSentenceSize +   " trans " + transition + " obs " + observation + " back " + back + " prob " + prob);
				}
				backward[state][pos]= prob/_inverseLikelihoodScalors[pos];		
			}
		}
//		sanityCheckMakeBacwardTables();
	}
	
	/**
	 * Scaled forward table.
	 * Rabineri92
	 * 
	 * 
	 */
	public void makeForwardTables() {
		java.util.Arrays.fill(_inverseLikelihoodScalors, 0);
		_scaledLikelihood =1.0;	
		_logLikelihood=0;
		double likelihoodScalor = 0;
		for (int state = 0; state < nrHiddenStates; state++) {
			double prob = sd.getInitProb(state)
					* sd.getObservationProbability(0, state);

			forward[state][0]= prob;
			likelihoodScalor+=prob;	
		}
		for (int state = 0; state < nrHiddenStates; state++) {
			forward[state][0]=  forward[state][0]/likelihoodScalor;	
		}
		_inverseLikelihoodScalors[0] =likelihoodScalor;
		_scaledLikelihood *= likelihoodScalor;
		_logLikelihood += Math.log(likelihoodScalor);
		for (int pos = 1; pos < nrPositions; pos++) {
			likelihoodScalor=0;
			for (int state = 0; state < nrHiddenStates; state++) {
				double observation = sd.getObservationProbability(pos, state);
				double prob = 0;
				for (int prevState = 0; prevState < nrHiddenStates; prevState++) {
					double alpha = forward[prevState][pos - 1];
					double transition = sd.getTransitionProbability(pos-1,prevState,state);
					prob += alpha * transition;
//					System.out.println("At position " + pos +
//							"going from state " + prevState +"-" +state+
//							" obs " + observation +
//							" alpha " + alpha +
//							" transition " + transition +
//							" prob " + prob);
				}
				prob = prob * observation;
				
				forward[state][pos]=prob;
				likelihoodScalor+=prob;
			}
			if(likelihoodScalor == 0){
				System.out.println("Likelihood scallor is zero for position " + pos);
				System.out.println(sd.toString());
				printForwardBackwardTables();
				throw new RuntimeException("Likelihood scallor is zero for position "+pos);
			}
			for (int state = 0; state < nrHiddenStates; state++) {
				forward[state][pos] =  forward[state][pos]/likelihoodScalor;
			}
			_inverseLikelihoodScalors[pos] = likelihoodScalor;
			_scaledLikelihood *= likelihoodScalor;
			_logLikelihood += Math.log(likelihoodScalor);
		}
		//sanityCheckMakeForwardTables();
	}	

	public void sanityCheckMakeForwardTables(){
		//Sum for all states must be one
		for (int pos = 1; pos < nrPositions; pos++) {
			double sum =0;
			for (int state = 0; state < nrHiddenStates; state++) {
				sum+= forward [state][pos];
			}
			if(!util.MathUtil.closeToOne(sum)){
				System.out.println("Scalling is not well done " + sum);
				printForwardBackwardTables();
				System.out.println(sd.toString());
				throw new RuntimeException("Scalling is not well done" + sum);
			}
		}
	}

	public void printForwardBackwardTables(){
		util.Printing.printDoubleArray(forward, null,null,"Forward table");
		util.Printing.printDoubleArray(backward,null,null, "Backward table");
	}
	
	public void sanityCheckMakeBacwardTables(){
		
		//Likelihood per position should be the same for all positions
		double[] _likelihoodPerPosition = new double[nrPositions];
		java.util.Arrays.fill(_likelihoodPerPosition,0);
		for (int pos = 0; pos < nrPositions; pos++) {
			for (int state = 0; state < nrHiddenStates; state++) {
				//Since the scallor is 1/c
				_likelihoodPerPosition[pos]+=backward[state][pos]*forward[state][pos]*
				_scaledLikelihood*_inverseLikelihoodScalors[pos];
			}
		}		
		//Likelihood per position should all be the same
		for (int pos = 1; pos < nrPositions; pos++) {
			double diff = (_likelihoodPerPosition[pos] - _likelihoodPerPosition[pos-1]);
			if(!util.MathUtil.closeToZero(diff)){
//				
				System.out.println("Likelihood per position are not all the same "
						+ " size "+ nrPositions + " "
						+ " sentence number " + pos + " " 
						+ _likelihoodPerPosition[pos] + " " 
						+ " " + (pos-1) + " " 
						+ _likelihoodPerPosition[pos-1] + " diff " + diff);
				printForwardBackwardTables();
				util.Printing.printDoubleArray(_inverseLikelihoodScalors,null,"Likelihood scallors");
				util.Printing.printDoubleArray(_likelihoodPerPosition,null,"Likelihood per position");
				System.out.println("Sclaed likelihood" + _scaledLikelihood);
				System.out.println("SD" + sd.toString());
				System.exit(-1);
			}
		}
	}

	
//	public void makeStatePosteriors(ChainSentenceDist sd){		
//		//Calculating the gamma
//		for (int observation = 0; observation < nrPositions; observation++) {
//			for (int state = 0; state < nrHiddenStates; state++) {
//				//Must multiply by the likelihood scallor since its overcounting it.
//				double prob = forward[state][observation]*
//				backward[state][observation]*_inverseLikelihoodScalors[observation];
//				if (prob < 0) {
//					sd.setStatePosterior(observation, state, 0);
//				} else {
//					sd.setStatePosterior(observation, state, prob);
//				}
//			}
//		}	
//	}
	
	/**
	 * This is the rescaled version so there is no need to divide by the likelihood since this is already
	 * incorporated on the scallors. Just as in the state posteriors
	 *
	 */
//	public void makeTransitionPosteriors(ChainSentenceDist sd){
//		for (int pos = 0; pos < nrPositions - 1; pos++) {
//			for (int currentState = 0; currentState < nrHiddenStates; currentState++) {
//				for (int nextState = 0; nextState < nrHiddenStates; nextState++) {
//					double transition = sd.getTransitionProbability(pos,currentState,nextState);                                                                                                        					
//					 double observation = sd.getObservationProbability(pos + 1,  nextState);                                                                                                                
//					 double alpha = forward[currentState][pos];                                                                                         
//					 double beta = backward[nextState][pos + 1];                                                                                        
//					 double epsilon = alpha * transition * observation * beta;						  				                                
//					 sd.setTransitionPosterior(pos, currentState, nextState, epsilon);			
//				}	 
//			}
//		}
//	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(Printing.doubleArrayToString(forward, null, null, "forward"));
		sb.append(Printing.doubleArrayToString(backward, null, null, "backward"));
		return sb.toString();
	}
	
}
