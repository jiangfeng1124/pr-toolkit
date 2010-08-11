package model.distribution.trainer;


import gnu.trove.TIntArrayList;
import model.distribution.AbstractMultinomial;
import optimization.gradientBasedMethods.Objective;
import optimization.gradientBasedMethods.Optimizer;
import optimization.gradientBasedMethods.stats.OptimizerStats;
import optimization.linesearch.LineSearchMethod;
import optimization.stopCriteria.StopingCriteria;
import util.ArrayMath;
import util.SparseVector;
import util.StaticUtils;

/**
 * Trains a ME regularized with the sigmaL2Norm of the gradient for a particular
 * hidden state. Receives the weights for that particular hidden state and the intial
 * parameters to start the optimization. 
 * When terminated the parameters are the ones 
 * 
 * @param counts
 */

public class MaxEntClassifier {
	
	
	double gaussianPriorVariance;
	
	/**
	 * The features to be used
	 */
	MultinomialFeatureFunction fxy;
	/**
	 * Saves the optimization parameters
	 */
	double[] initialParameters;

	
	/**
	 * Use the parameters from the last update as initial parameters (true) or
	 * start from zero parameters(true).
	 */
	public boolean warmStart = false;
	
	//Optimization particular methods to be used
	LineSearchMethod lineSearch;
	Optimizer optimizer;
	OptimizerStats stats;
	StopingCriteria stoppingCriteria;
	
	public MaxEntClassifier(MultinomialFeatureFunction featFunct,
			LineSearchMethod ls, Optimizer op, OptimizerStats stats, 
			StopingCriteria stop,
			double gaussianPriorVariance, 
			boolean warmStart){
		fxy = featFunct;
		initialParameters = new double[fxy.nrFeatures()];
		lineSearch = ls;
		optimizer = op;
		this.stats = stats;
		stoppingCriteria = stop;
		this.gaussianPriorVariance = gaussianPriorVariance;
		this.warmStart = warmStart;
	}
	
	/**
	 * Clean the optimization classes
	 */
	public void reset(){
		stoppingCriteria.reset();
		stats.reset();
		lineSearch.reset();
		optimizer.reset();
		
	}
	
	public MaxEntMinimizationObjective getObjective(AbstractMultinomial counts, int variable){
		return new MaxEntMinimizationObjective(initialParameters, counts, variable,gaussianPriorVariance);		
	}
	
	public OptimizerStats batchTrain(AbstractMultinomial counts, int variable){	

		reset();
		if(warmStart == false){
			System.out.println("Restarting ME weights");
			java.util.Arrays.fill(initialParameters, 0);
		}
		MaxEntMinimizationObjective obj = getObjective(counts, variable);
		boolean succed = optimizer.optimize(obj,stats,stoppingCriteria);
		return stats;
		//
		//		System.out.println("Suceess " + succed + "/n"+stats.prettyPrint(0));
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
		private AbstractMultinomial counts;
		double gaussianPriorVariance;
		//Current function value and gradient
		double objective;

		
		int variable;

		
		MaxEntMinimizationObjective(
				double[] initialParameters, 
				AbstractMultinomial counts, 
				int variable,
				double gaussianPriorVariance) {
			this.counts = counts;
			this.variable = variable;
			this.gaussianPriorVariance = gaussianPriorVariance;
			// compute empirical expectations...
			empiricalExpectations = new double[fxy.nrFeatures()];
			gradient = new double[fxy.nrFeatures()];
			computerEmpiricalExpectations();
			parameters = initialParameters;
			setParameters(initialParameters);

		}
		
		/**
		 * set the counts from which the empirical expectations are computed. 
		 */
		public void setCounts(AbstractMultinomial counts){
			this.counts = counts;
			computerEmpiricalExpectations();
		}
		
		/**
		 * For each variable X and each value Y compute the empirical counts
		 * "from the count table" of each feature
		 */
		public void computerEmpiricalExpectations(){
			ArrayMath.set(empiricalExpectations, 0);
			TIntArrayList valuesIds = counts.getAvailableStates(variable);
			for (int i = 0; i < valuesIds.size(); i++) {
				int state = valuesIds.getQuick(i);
				StaticUtils.plusEquals(empiricalExpectations, fxy.apply(variable,state),counts.getCounts(variable, state));
			}
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
			
			TIntArrayList values = counts.getAvailableStates(variable);					
			SparseVector scores = computeScores(variable, values,parameters);
			double max = scores.max();
			scores.plusEquals(-max);
			SparseVector expScores = scores.expEntries();	
			double Z = expScores.sum();
			double logZ = Math.log(Z);
			double totalWeight = counts.sum(variable);
			for(int y = 0; y < values.size(); y++){
				int yValue = values.getQuick(y);
				double count = counts.getCounts(variable,yValue);
				objective+=(-scores.getValue(yValue) + logZ)*count;
				StaticUtils.plusEquals(expectations, fxy.apply(variable, yValue),
				totalWeight*(expScores.getValue(yValue)/Z));
			}			
			for (int i = 0; i < gradient.length; i++) {
				gradient[i] += expectations[i];
			}
						
			double squareWeightNorm = StaticUtils.twoNormSquared(parameters);
			objective += 1/gaussianPriorVariance*squareWeightNorm;
			
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
			updateObjectiveAndGradient();
		}

		@Override
		public String toString() {
			return "MaxEntropy to String not implemented";
		}


	}

}
