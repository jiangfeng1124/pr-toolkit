package model.chain.mrf;

import data.Corpus;
import model.chain.ForwardBackwardInference;
import model.chain.GenerativeFeatureFunction;
import model.chain.hmm.HMMCountTable;
import optimization.gradientBasedMethods.Objective;
import util.ArrayMath;
import util.LogSummer;
import util.SparseVector;
import util.StaticUtils;

public class MRFObjective extends Objective {
	
	//ChainSentenceDist[] empirical;
	HMMCountTable counts;
	Corpus c;
	GenerativeFeatureFunction fxy;
	int numStates;
	int numFeatures;
	double[] empiricalExpectations;
	int[]  numSentencesPerLen;
	MRFCache cache;
	double gaussianPriorVariance;
	
	private class MRFCache{
		public double[] emissionNoObs;
		double emissionSumOffset; // emission sum is actually emissionNoObs * exp(emissionSumOffset)
		public NoObsSentenceDist[] distsNoObs;
		double objective;
		double[] gradient;
		
		public MRFCache(){
			emissionNoObs = computeEmissionSum();
			emissionSumOffset = ArrayMath.max(emissionNoObs);
			ArrayMath.plusEquals(emissionNoObs, -emissionSumOffset);
			ArrayMath.exponentiate(emissionNoObs);
			distsNoObs = doForwardBackwardNoObs(numSentencesPerLen, emissionNoObs);
			objective = computeObjective();
			gradient = computeGradient();
		}
		
		/**
		 * computes the sum of emission factors for each observation.  If we don't observe X, 
		 * this is the effective emission factor. 
		 * @return
		 */
		private double[] computeEmissionSum(){
			// we store emissionSum in log space for now... 
			double[] emissionSum = new double[numStates];
			for (int i = 0; i < emissionSum.length; i++) {
				emissionSum[i] = Double.NEGATIVE_INFINITY;
			}
			for (int y = 0; y < emissionSum.length; y++) {
				for (int word = 0; word < c.getNrWordTypes(); word++) {
					LogSummer.sum(emissionSum, y, getEmissionDot(y, word));
//					emissionSum[y] += Math.exp(getEmissionDot(y, word));
				}
//				if(Double.isInfinite(emissionSum[y])) throw new RuntimeException("infinite emissions sum:"+y+" "+emissionSum[y]);
				if(Double.isNaN(emissionSum[y])) throw new RuntimeException("nan emissions sum:"+y);
			}
			return emissionSum;
		}

		/**
		 * run forward-backward for the case when we don't observe anything. 
		 * @param maxLen
		 * @return
		 */
		public NoObsSentenceDist[] doForwardBackwardNoObs(int[] lengths, double[] emissionSum){
			NoObsSentenceDist[] res = new NoObsSentenceDist[lengths.length];
			double[] init = new double[numStates];
			double initOffset; 
			double[] finalP = new double[numStates];
			double finalOffset;
			for (int state = 0; state < init.length; state++) {
				init[state] = getInitial(state);
				finalP[state] = getFinal(state);
			}
			initOffset = ArrayMath.max(init);
			finalOffset = ArrayMath.max(finalP);
			ArrayMath.plusEquals(init, -initOffset);
			ArrayMath.plusEquals(finalP, -finalOffset);
			ArrayMath.exponentiate(init);
			ArrayMath.exponentiate(finalP);
			double[][] transitions = new double[numStates][numStates];
			double transOffset = 0;
			for (int state = 0; state < init.length; state++) {
				for (int next = 0; next < transitions.length; next++) {
					transitions[state][next] = getTransition(state, next);
				}
				transOffset = Math.max(transOffset, ArrayMath.max(transitions[state]));
			}
			for (int state = 0; state < init.length; state++) {
				ArrayMath.plusEquals(transitions[state], -transOffset);
				ArrayMath.exponentiate(transitions[state]);
			}

			for (int length = 0; length < res.length; length++) {
				if (lengths[length] == 0) continue;
				NoObsSentenceDist d = new NoObsSentenceDist(init, transitions, emissionSum, finalP,length);
				ForwardBackwardInference inference  = new ForwardBackwardInference(d);
				inference.makeInference();
				// FIXME -- check me
				d.logLikelihood += emissionSumOffset*length;
				d.logLikelihood += initOffset;
				d.logLikelihood += finalOffset;
				d.logLikelihood += transOffset*(length -1);
				res[length] = d;
			}
			return res;
		}
		
		/**
		 * the value is the expectation according to the corpus labeling "corpusAfterEStep"
		 * of the log probability of that labeling: 
		 * sum_x E_{y}[log p_theta(x,y)]
		 *  = sum_x E_{y}[f(x,y).theta] - log Z(len(x))
		 * to compute the value of E_{y}[f(x,y).theta] we just need posteriors of the position 
		 * and transitions. 
		 */ 
		public double computeObjective(){
			double logZ = 0;
			for (int i = 1; i < distsNoObs.length; i++) {
				if (numSentencesPerLen[i] == 0) continue;
				logZ += numSentencesPerLen[i]*distsNoObs[i].logLikelihood; 
				if (Double.isNaN(logZ)) throw new RuntimeException("normalizer is NaN ");
			}
			double fdottheta = ArrayMath.dotProduct(empiricalExpectations, parameters);
			double logPrior = (0.5/gaussianPriorVariance) * StaticUtils.twoNormSquared(parameters);
			double res = fdottheta- logZ - logPrior;
			if (Double.isNaN(res)) throw new RuntimeException("value is NaN ");
			return res;
		}
		
		public double[] computeGradient(){
			double[] res = new double[parameters.length];
			// set res to model expectations
			
			// compute state expectations as well as initial/transition.  
			// we compute expectations of the emission features just below the loop over lengths
			double[] stateExpectations = new double[numStates];
			int numWords = c.wordAlphabet.size();
			
			for (int length = 1; length < distsNoObs.length; length++) {
				int repeat = numSentencesPerLen[length];
				if (repeat == 0) continue;
				NoObsSentenceDist inst = distsNoObs[length];
				inst.checkPosteriors();
				for (int state = 0; state < numStates; state++) {
					addInitial(state, res, repeat*inst.getStatePosterior(0, state));
					addFinal(state, res, repeat*inst.getStatePosterior(inst.numPositions-1, state));
				}
				for (int position = 0; position < length; position++) {
					for (int state = 0; state < numStates; state++) {
						stateExpectations[state] += repeat*inst.getStatePosterior(position, state);
						if(position == inst.getNumberOfPositions() -1) continue;
						for (int nextState = 0; nextState < numStates; nextState++) {
							addTransition(state, nextState, res, repeat*inst.getTransitionPosterior(position, state, nextState));
						}
					}
				}
			}
			// compute expectations of emission features
			for (int word = 0; word < numWords; word++) {
				SparseVector sv = fxy.apply(null, word);
				for (int state = 0; state < numStates; state++) {
					// expectation of word is: 
					//    expectation of the state(summed over all words) 
					//       * emission factor for the word
					//       / emission factor for the sum over all words
					double logEmission = getEmissionDot(state, word)-emissionSumOffset - Math.log(emissionNoObs[state]); // was wordProbMult
					double wordProb = stateExpectations[state] * Math.exp(logEmission);
					for (int j = 0; j < sv.numEntries(); j++) {
						addEmission(state, sv.getIndexAt(j), res, wordProb*sv.getValueAt(j));
					}
				}
			}

			
			// set res to empirical - res
			for (int i = 0; i < res.length; i++) {
				res[i] = empiricalExpectations[i] - res[i] - (1 / gaussianPriorVariance) * parameters[i];
			}
			return res;
			
		}
		
	}
	
	
	// the parameters in theta are arranged as follows... 
	// first #labels = log-initial probs
	// next #labels = log-final probs
	// next #labels*#labels = log-transition probs
	// next #labels*#features = log-emission factors
	public final double getInitial(int state){ return parameters[state]; }
	public final  void  addInitial(int state, double[] a, double d){a[state]+=d;}
	public final double getFinal(int state){ return parameters[numStates+state];}
	public final  void  addFinal(int state, double[] a, double d){a[numStates+state]+=d;}
	public final double getTransition(int state, int nextState){ return parameters[2*numStates+numStates*state+nextState];}
	public final  void  addTransition(int state, int nextState,double[] a, double d){ a[2*numStates+numStates*state+nextState]+=d;}
	public final double getEmission(int state, int feature){ return parameters[2*numStates+numStates*numStates+state*numFeatures+feature];}
	public final  void  addEmission(int state, int feature,double[] a, double d){a[2*numStates+numStates*numStates+state*numFeatures+feature]+=d;}
	
	
	public double getEmissionDot(int state, int w){
		return getEmissionDot(state, fxy.apply(null, w));
	}
	
	public double getEmissionDot(int state, SparseVector sv){
		double res = 0;
		for (int i = 0; i < sv.numEntries(); i++) {
			res+= getEmission(state, sv.getIndexAt(i))*sv.getValueAt(i);
		}
		return res;
	}
	
	public MRFObjective(HMMCountTable counts, int[] lengthCounts, Corpus c, GenerativeFeatureFunction fxy, double variance){
		this.c = c;
		this.fxy = fxy;
		this.gaussianPriorVariance = variance;
		numStates = counts.initialCounts.numStates() - 1;
		numFeatures = fxy.wSize(); 
		this.parameters = new double[2*numStates+numStates*numStates+numStates*numFeatures];
		numSentencesPerLen = lengthCounts;
		empiricalExpectations = new double[parameters.length];
		// compute empirical expectations
		for (int state = 0; state < numStates; state++) {
			addInitial(state, empiricalExpectations, counts.initialCounts.getCounts(0, state));
			addFinal(state, empiricalExpectations, counts.transitionCounts.getCounts(state, numStates)); 
			for (int word = 0; word < c.getNrWordTypes(); word++) {
				SparseVector sv = fxy.apply(null, word);
				double wordCount = counts.observationCounts.getCounts(state, word);
				for (int i = 0; i < sv.numEntries(); i++) {
					addEmission(state, sv.getIndexAt(i), empiricalExpectations, wordCount);					
				}
			}
			for (int nextState = 0; nextState < numStates; nextState++) {
				addTransition(state, nextState, empiricalExpectations, counts.transitionCounts.getCounts(state, nextState));
			}
		}
		gradient = new double[getNumParameters()];
		testGradient();
	}

	public void setParameters(double[] params) {
		updateCalls++;
		System.arraycopy(params, 0, parameters, 0, parameters.length);
		cache = null;
		testGradient();
	}

	@SuppressWarnings("unused")
	private void testGradient(){
		// check that gradient and value correspond to eachother... 
		double val = getValue();
		double[] gradient = new double[parameters.length];
		gradient = getGradient();
		double[] numgradient = new double[parameters.length];
		double epsilon = 0.001;
		System.out.println("value = "+val);
		System.out.println("parameters norm = "+ArrayMath.twoNormSquared(parameters));
		System.out.println("gradient norm = "+ArrayMath.twoNormSquared(gradient));
		System.out.flush();
		if(true) return;
		for (int i = 0; i < numgradient.length; i++) {
			if (Double.isNaN(parameters[i])) throw new RuntimeException("parameters["+i+"] is not a number");
			if (Double.isInfinite(parameters[i])) throw new RuntimeException("parameters["+i+"] is infinite: "+parameters[i]);
			if (Double.isNaN(gradient[i])) throw new RuntimeException("gradient["+i+"] is not a number");
			if (Double.isInfinite(gradient[i])) throw new RuntimeException("gradient["+i+"] is infinite: "+parameters[i]);
			cache = null;
			parameters[i] += epsilon;
			numgradient[i] = (getValue()-val)/epsilon;
			parameters[i] -= epsilon;
			if (Double.isNaN(numgradient[i])) throw new RuntimeException("numgradient["+i+"] is not a number");
			if (Double.isInfinite(numgradient[i])) throw new RuntimeException("numgradient["+i+"] is infinite: "+parameters[i]);
		}
		cache = null;
		System.out.println("Cosine between grad and numgrad is "+ArrayMath.cosine(numgradient, gradient));		
	}
	
	@Override
	public double[] getGradient() {
		if (cache==null) cache = new MRFCache();
		double scale = ArrayMath.sum(numSentencesPerLen)/100.0;
		for (int i = 0; i < gradient.length; i++) {
			gradient[i] = -cache.gradient[i]/scale;   // minimize, not maximize
		}
		return gradient;
	}

	@Override
	public double getValue() {
		if (cache==null) cache = new MRFCache();
		double scale = ArrayMath.sum(numSentencesPerLen)/100.0;
		return -cache.objective/scale; // minimize, not maximize
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

}
