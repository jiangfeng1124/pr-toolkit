package model.chain;

import optimization.gradientBasedMethods.Objective;
import optimization.gradientBasedMethods.stats.OptimizerStats;
import optimization.linesearch.InterpolationPickFirstStep;
import optimization.linesearch.WolfRuleLineSearch;
import optimization.stopCriteria.CompositeStopingCriteria;
import optimization.stopCriteria.NormalizedGradientL2Norm;
import optimization.stopCriteria.StopingCriteria;
import optimization.stopCriteria.ValueDifference;
import util.Alphabet;
import util.FeatureFunction;
import util.LinearClassifier;
import util.SparseVector;
import util.StaticUtils;


/**
 * a generative max ent model; 
 * @author javg
 *
 */
public class GenerativeMaxEntropy {

	double gaussianPriorVariance;
	double numObservations;
	Alphabet yAlphabet, xAlphabet;
	FeatureFunction fxy;

	public GenerativeMaxEntropy(double gaussianPriorVariance, Alphabet xAlphabet, Alphabet yAlphabet, FeatureFunction fxy) {
		this.gaussianPriorVariance = gaussianPriorVariance;
		this.yAlphabet = yAlphabet;
		this.xAlphabet = xAlphabet;
		this.fxy = fxy;
	}
	
	public LinearClassifier batchTrain(double[] weights,LinearClassifier initialClassifier,double gradientPrecision,double valuePrecision, int maxIterations) {
		
		MaxEntMinimizationObjective obj = new MaxEntMinimizationObjective(weights,initialClassifier);
		
		// perform gradient descent
		WolfRuleLineSearch wolfe = 
			new WolfRuleLineSearch(new InterpolationPickFirstStep(1),0.001,0.9);
		
		wolfe.setDebugLevel(0);
//		LineSearchMethod ls = new ArmijoLineSearchMinimization();
		optimization.gradientBasedMethods.LBFGS optimizer = 
			new optimization.gradientBasedMethods.LBFGS(wolfe,30);
		optimization.gradientBasedMethods.stats.OptimizerStats stats = new OptimizerStats();
		StopingCriteria stopGrad = new NormalizedGradientL2Norm(gradientPrecision);
		StopingCriteria stopValue = new ValueDifference(valuePrecision);
		CompositeStopingCriteria stop = new CompositeStopingCriteria();
		stop.add(stopGrad);
		stop.add(stopValue);
		optimizer.setMaxIterations(maxIterations);
		boolean succed = optimizer.optimize(obj,stats,stop);
		System.out.println("Suceess " + succed + "/n"+stats.prettyPrint(0));
		
		return obj.classifier;
	}

	/**
	 * An objective for our max-ent model. That is: max_\lambda sum_i log
	 * Pr(y_i|x_i) - 1/var * ||\lambda||^2 where var is the Gaussian prior
	 * variance, and p(y|x) = exp(f(x,y)*lambda)/Z(x).
	 * 
	 * @author kuzman
	 * 
	 *JOAO: Note that since this is a minimization we are multiplying the value and the gradient by -1
	 * 
	 */
	class MaxEntMinimizationObjective extends Objective {
		double[] empiricalExpectations;
		LinearClassifier classifier;
		SparseVector x = new SparseVector(); // dummy
		double[] labelWeights;

		MaxEntMinimizationObjective(double[] labelWeights,LinearClassifier initialClassifier) {
			this.labelWeights = labelWeights;
			// compute empirical expectations...
			empiricalExpectations = new double[fxy.wSize()];
			for (int y = 0; y < yAlphabet.size(); y++) {
				StaticUtils.plusEquals(empiricalExpectations, fxy.apply(x,y),
						labelWeights[y]);
			}
			classifier = initialClassifier;
			gradient = new double[getNumParameters()];
			//System.out.println("Classifier scores"+util.Printing.doubleArrayToString(initialClassifier.w, null, "w"));
		}

		boolean cacheValid;
		double[] expScores;
		double Z;
		double logZ;
		double[] scores;
		public double getValue() {
			functionCalls++;
			// value = log(prob(data)) - 1/gaussianPriorVariance * ||lambda||^2
			//since we want it to be a minimization we need to multiply at the end by -1	
			double val = 0;
			//Might need to do a max subtraction in case scores are very low...
			
			if(!cacheValid){		
				scores = classifier.scores(x);
				double max = StaticUtils.max(scores);
				StaticUtils.plusEquals(scores, -max);	
				expScores = StaticUtils.exp(scores);
				Z = StaticUtils.sum(expScores);
				logZ = Math.log(Z);
				cacheValid=true;
			}
			for (int y = 0; y < yAlphabet.size(); y++) {
				
				val += labelWeights[y]*(scores[y] - logZ);
			}
			val -= 1 / (2 * gaussianPriorVariance)
					* StaticUtils.twoNormSquared(classifier.w);
			if(Double.isNaN(val)){
				throw new RuntimeException("Objective Value nan: norm=" + Z);
			}
			return val*-1;
		}

		public double[] getGradient() {
			
			gradientCalls++;
			// gradient = empiricalExpectations - modelExpectations
			// -2/gaussianPriorVariance * params
			//Since we want it to be a minimation multiply each entry of the
			//gradient by -1
			double[] modelExpectations = new double[gradient.length];
			for (int i = 0; i < gradient.length; i++) {
				gradient[i] = empiricalExpectations[i] - 1 / gaussianPriorVariance * classifier.w[i];
				modelExpectations[i] = 0;
			}
			
			if(!cacheValid){		
				scores = classifier.scores(x);
				double max = StaticUtils.max(scores);
				StaticUtils.plusEquals(scores, -max);	
				expScores = StaticUtils.exp(scores);
				Z = StaticUtils.sum(expScores);
				logZ = Math.log(Z);
				cacheValid=true;
			}
			
			double totalWeight = StaticUtils.sum(labelWeights);
			for (int y = 0; y < yAlphabet.size(); y++) {
				StaticUtils.plusEquals(modelExpectations, fxy.apply(x,
						y), totalWeight*(expScores[y] / Z));
			}
			for (int i = 0; i < gradient.length; i++) {
				gradient[i] -= modelExpectations[i];
			}

			//Get the negative of the gradient for minimization
			for(int i =0; i < gradient.length; i++){
				gradient[i]=-gradient[i];
			}
			return gradient;
		}

		public void setParameters(double[] newParameters) {
			System.arraycopy(newParameters, 0, classifier.w, 0,
					newParameters.length);
			updateCalls++;
			cacheValid = false;
			
		}

		@Override
		public double[] getParameters() {
			//System.out.println("Getting correct getParameters");
			return classifier.w;
			//System.arraycopy(classifier.w, 0, params, 0, params.length);
		}

		public int getNumParameters() {
			return classifier.w.length;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return null;
		}

	}


	
	
}
