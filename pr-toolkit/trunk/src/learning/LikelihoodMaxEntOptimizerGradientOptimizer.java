package learning;


import gnu.trove.TIntArrayList;
import model.AbstractModel;
import model.AbstractSentenceDist;
import model.chain.hmmFinalState.HMMFinalState;
import model.chain.hmmFinalState.HMMFinalStateCountTable;
import model.distribution.AbstractMultinomial;
import model.distribution.Multinomial;
import model.distribution.trainer.MultinomialFeatureFunction;
import optimization.gradientBasedMethods.Objective;
import optimization.gradientBasedMethods.Optimizer;
import optimization.gradientBasedMethods.stats.OptimizerStats;
import optimization.linesearch.LineSearchMethod;
import optimization.stopCriteria.StopingCriteria;
import util.SparseVector;
import util.StaticUtils;

/**
 * Optimizes the likelihood of a maximum entropy HMM with ME models 
 * for every click using a direct gradient base method.
 * 
 * @author javg
 *
 */
public class LikelihoodMaxEntOptimizerGradientOptimizer {

	double gaussianPriorVariance;
	/**
	 * The features to be used
	 */
	MultinomialFeatureFunction fxy;
	
	//Optimization particular methods to be used
	LineSearchMethod lineSearch;
	Optimizer optimizer;
	OptimizerStats stats;
	StopingCriteria stoppingCriteria;
	
	AbstractModel model;

	public LikelihoodMaxEntOptimizerGradientOptimizer(AbstractModel model,
			double gaussianPriorVariance,
			MultinomialFeatureFunction fxy,
			LineSearchMethod lineSearch,
			Optimizer optimizer,
			OptimizerStats stats,
			StopingCriteria stop){
			
			this.model = model;
			this.gaussianPriorVariance = gaussianPriorVariance;
			this.fxy = fxy;
			this.lineSearch = lineSearch;
			this.optimizer = optimizer;
			this.stats = stats;
			this.stoppingCriteria = stop;
	}
	
	
	public void train(int maxIterations){
//		MaxEntMinimizationObjective obj = 
//			new MaxEntMinimizationObjective(initialParameters, counts, variable,gaussianPriorVariance);
//			boolean succed = optimizer.optimize(obj,stats,stoppingCriteria);
//		
			//Do something with stats
		
	}
	
		
	
	
	/**
	 * A Maximum entropy objective for the counts table max-entropy model.
	 * The observations are all words pairs (including null aligned words) wp_i:
	 * wp_i = targetWord_j|sourceWord_k
	 * 
	 * Note that we are doing minimization so we are minimizing the negative entropy:
	 * 
	 * The objective is: 
	 * min_w = sum_wp - log Pr(targetWord|sourceWourd) + 1/var*||w||^2 
	 * w are the features weights
	 * var is the Gaussian prior variance: 
	 * log Pr(tWord|sourceWourd) = log(exp(f(targetWord,sourceWord)*w)/Z(targetWord))
	 *                           = f(targetWord,sourceWord)*w - log(Z(targetWord))
	 * log(Z(targetWord)) = sum_sourceWords log(exp(f(targetWord,sourceWord)*w)
	 * 
	 * The gradient is:
	 * modelExpectations +2/gaussianPriorVariance * w - empiricalExpectations
	 * empiricalExpectations - In this case for each entry in the count table is the features for a particular entry weighted by the respective counts.
	 * modelExpectation = E_w[f(targetWord,sourceWord)] = Pr(targetWord|sourceWourd)*f(targetWord,sourceWord)
	 * 
	 */
	class MaxEntMinimizationObjective extends Objective {
		double[] empiricalExpectations;
		HMMFinalStateCountTable counts;
		double gaussianPriorVariance;
		//Current function value and gradient
		double objective;

		

		HMMFinalState model;
		
		MaxEntMinimizationObjective(HMMFinalState model,
				double[] initialParameters,  
				double gaussianPriorVariance) {
			this.model =model;
			this.counts = (HMMFinalStateCountTable) model.getCountTable();
			this.gaussianPriorVariance = gaussianPriorVariance;
			// compute empirical expectations...
			empiricalExpectations = new double[fxy.nrFeatures()];
			gradient = new double[fxy.nrFeatures()];
			computerEmpiricalExpectations();
			parameters = initialParameters;
			setParameters(initialParameters);
		}
		
		public void updateCounts(){
			// Clear model for accumulating E step counts
			counts.fill(0);
			// Corpus E step
			AbstractSentenceDist[] sentenceDists = model.getSentenceDists();
			for(AbstractSentenceDist sd : sentenceDists){			
				sd.initSentenceDist();
				model.computePosteriors(sd);
				sd.clearCaches();
				model.addToCounts(sd,counts);	
				sd.clearPosteriors();
			}
		}

		
		/**
		 * Computes the dot product between the features and the parameters.
		 * @param x
		 * @param yList
		 * @return
		 */
		public SparseVector computeScores(){
			Multinomial observations = counts.observationCounts;
			Multinomial init = counts.initialCounts;
			
			
			SparseVector res = new SparseVector();			
			
			//Collect Initial counts
			TIntArrayList initValuesIds = init.getAvailableStates(0);
			for (int i = 0; i < initValuesIds.size(); i++) {
				int yValue =(Integer) initValuesIds.getQuick(i);
				res.add(yValue, StaticUtils.dotProduct(fxy.apply(0,yValue), parameters));
			}
			
			//Go for each position
			for(int variable = 0; variable < model.getNrStates(); variable ++){
				//Collect from observations
				TIntArrayList observationValuesIds = observations.getAvailableStates(variable);
				for (int i = 0; i < observationValuesIds.size(); i++) {
					int yValue =(Integer) observationValuesIds.getQuick(i);
					res.add(yValue, StaticUtils.dotProduct(fxy.apply(variable,yValue), parameters));
				}
		
				//Collect transition counts
				for(int variable2 = 0; variable2 < model.getNrStates(); variable2 ++){
					res.add(variable2, StaticUtils.dotProduct(fxy.apply(variable,variable2), parameters));
				}
			}
			return res;
		}

		
		
		/**
		 * For each variable X and each value Y compute the empirical counts
		 * "from the count table" of each feature
		 */
		public void computerEmpiricalExpectations(){
			Multinomial observations = counts.observationCounts;
			Multinomial init = counts.initialCounts;
			Multinomial transitions = counts.transitionCounts;
		
			
			//Collect Initial counts
			TIntArrayList initValuesIds = observations.getAvailableStates(0);
			for (int i = 0; i < initValuesIds.size(); i++) {
				int state = initValuesIds.getQuick(i);
				StaticUtils.plusEquals(empiricalExpectations, fxy.apply(0,state),init.getCounts(0, state));
			}
			
			//Go for each position
			for(int variable = 0; variable < model.getNrStates(); variable ++){
				//Collect from observations
				TIntArrayList observationValuesIds = observations.getAvailableStates(variable);
				for (int i = 0; i < observationValuesIds.size(); i++) {
					int state = observationValuesIds.getQuick(i);
					StaticUtils.plusEquals(empiricalExpectations, fxy.apply(variable,state),observations.getCounts(variable, state));
				}
		
				
				//Collect transition counts
				for(int variable2 = 0; variable2 < model.getNrStates(); variable2 ++){
					StaticUtils.plusEquals(empiricalExpectations, fxy.apply(variable,variable2),transitions.getCounts(variable, variable2));
				}
			}
		}
		
		/**
		 * Given a variable and a list of values for that variable computes
		 * the dot product between the parameters and the features. The features come
		 * from the feature function for each variable value.
		 * @param x
		 * @param yList
		 * @return
		 */
		public SparseVector computeScores(int x, TIntArrayList yList, double[] parameters){
			SparseVector res = new SparseVector();
			for (int y=0; y<yList.size(); y++){
				int yValue =(Integer) yList.getQuick(y);
				res.add(yValue, StaticUtils.dotProduct(fxy.apply(x,yValue), parameters));
			}
			return res;
		}
		
		/**
		 * Updates the objective and the gradient using the current parameters.
		 * This method should be called whenever the parameters are changed.
		 * Value = 1/var*||w||^2 + sum_targetW sum_sourceW  (log(Z_targetW)-w*f(targetW,sourceW))
		 * Gradient[targetWord,sourceWord] =  
		 * Pr(targetWord|sourceWourd)*f(targetWord,sourceWord) +2/gaussianPriorVariance * w - empiricalExpectations(targetWord, sourceWord)
		 */
		public void updateObjectiveAndGradient(){
			
			//Debug code
			double[] expectations = new double[gradient.length];
			
			objective = 0;
			//Fill gradient with constant 2/gaussianPriorVariance * w - empiricalExpectations(targetWord, sourceWord)
			for (int i = 0; i < gradient.length; i++) {	
				gradient[i]=2/gaussianPriorVariance * parameters[i] - empiricalExpectations[i];
				expectations[i]=0;
			}
			
			//Compute objective and expectation for each pair of hidden variables
			computeObjectiveExpectations(expectations);
			for (int i = 0; i < gradient.length; i++) {
				gradient[i] += expectations[i];
			}
			double squareWeightNorm = StaticUtils.twoNormSquared(parameters);
			objective += 1/gaussianPriorVariance*squareWeightNorm;
			
		}
		
		/**
		 * Computes:
		 * sum_targetW sum_sourceW  (log(Z_targetW)-w*f(targetW,sourceW))
		 * @return
		 */
		double computeObjectiveExpectations(double[] expectations){
			Multinomial observations = counts.observationCounts;
			Multinomial init = counts.initialCounts;
			Multinomial transitions = counts.transitionCounts;

			
			
			//Collect Initial counts
			TIntArrayList initValuesIds = observations.getAvailableStates(0);
			for (int i = 0; i < initValuesIds.size(); i++) {
				int state = initValuesIds.getQuick(i);
				computeObjectiveAndExpectationsForPairOfObservtions(0, state, init, expectations);
			}
			
			for(int variable = 0; variable < model.getNrStates(); variable ++){
				//Collect from observations
				TIntArrayList observationValuesIds = observations.getAvailableStates(variable);
				for (int i = 0; i < observationValuesIds.size(); i++) {
					int state = observationValuesIds.getQuick(i);
					computeObjectiveAndExpectationsForPairOfObservtions(variable, state, observations, expectations);
				}
			
				
				//Collect transition counts
				for(int variable2 = 0; variable2 < model.getNrStates(); variable2 ++){
					computeObjectiveAndExpectationsForPairOfObservtions(variable, variable2, transitions, expectations);
				}
			}
			
			double squareWeightNorm = StaticUtils.twoNormSquared(parameters);
			objective += 1/gaussianPriorVariance*squareWeightNorm;
			return objective;
		}
		
		/**
		 * (log(Z_targetW)-w*f(targetW,sourceW)
		 * @return
		 */
		void computeObjectiveAndExpectationsForPairOfObservtions(int observed, int hidden, 
				AbstractMultinomial counts,
				double[] expectations){
			TIntArrayList values = counts.getAvailableStates(observed);					
			SparseVector scores = computeScores(observed, values,parameters);
			double max = scores.max();
			scores.plusEquals(-max);
			SparseVector expScores = scores.expEntries();	
			double Z = expScores.sum();
			double logZ = Math.log(Z);
			double totalWeight = counts.sum(observed);
			for(int y = 0; y < values.size(); y++){
				int yValue = values.getQuick(y);
				double count = counts.getCounts(observed,yValue);
				objective+=(-scores.getValue(yValue) + logZ)*count;
				StaticUtils.plusEquals(expectations, fxy.apply(observed, yValue),
				totalWeight*(expScores.getValue(yValue)/Z));
			}
			
		}
		
		public double getValue() {
//			System.out.println("Value " + value);
			functionCalls++;
			return objective;

		}
		
		public double[] getGradient(){
			gradientCalls++;
			return gradient;
		}
		

		public void setParameters(double[] newParameters) {
			super.setParameters(newParameters);
			//set hmm parameters
			setHMMParameters();
			updateCounts();
			computerEmpiricalExpectations();
			updateObjectiveAndGradient();
		}

		/**Takes the gradient base parameters and converts it into the parameter table
		* of the HMM to be able to call the normal inference procedures
		*/
		public void setHMMParameters(){
			Multinomial observations = (Multinomial) model.observationProbabilities;
			Multinomial init = (Multinomial) model.initialProbabilities;
			Multinomial transitions = (Multinomial) model.transitionProbabilities;
			
			//Go for each position
			for(int variable = 0; variable < model.getNrStates(); variable ++){
				double observationSum = observations.sum(variable);
				TIntArrayList observationValuesIds = observations.getAvailableStates(variable);
				for (int i = 0; i < observationValuesIds.size(); i++) {
					int state = observationValuesIds.getQuick(i);
					SparseVector scores = computeScores(variable, observationValuesIds, parameters);
					observations.setCounts(variable, state, scores.getValue(state)/observationSum);
				}
				//Collect transition counts
				double transitionSum = transitions.sum(variable);
				TIntArrayList transitionValuesIds = transitions.getAvailableStates(variable);
				for (int i = 0; i < transitionValuesIds.size(); i++) {
					int state = transitionValuesIds.getQuick(i);
					SparseVector scores = computeScores(variable, transitionValuesIds, parameters);
					transitions.setCounts(variable, state, scores.getValue(state)/transitionSum);
				}
			}
			//Collect Initial counts
			double initSum = init.sum(0);
			TIntArrayList initValuesIds = init.getAvailableStates(0);
			for (int i = 0; i < initValuesIds.size(); i++) {
				int state = initValuesIds.getQuick(i);
				SparseVector scores = computeScores(0, initValuesIds, parameters);
				init.setCounts(0, state, scores.getValue(state)/initSum);
			}
		}
		
		@Override
		public String toString() {
			return "Direct Gradient MaxEntropy to String not implemented";
		}


	}

}
