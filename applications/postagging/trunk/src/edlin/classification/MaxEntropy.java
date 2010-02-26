package edlin.classification;

import java.util.ArrayList;
import java.util.Arrays;

import edlin.algo.ConjugateGradient;
import edlin.algo.GradientAscent;
import edlin.types.Alphabet;
import edlin.types.ClassificationInstance;
import edlin.types.DifferentiableObjective;
import edlin.types.FeatureFunction;
import edlin.types.LinearClassifier;
import edlin.types.StaticUtils;


public class MaxEntropy {

	double gaussianPriorVariance;
	double numObservations;
	Alphabet xAlphabet;
	Alphabet yAlphabet;
	FeatureFunction fxy;

	public MaxEntropy(double gaussianPriorVariance, Alphabet xAlphabet,
			Alphabet yAlphabet, FeatureFunction fxy) {
		this.gaussianPriorVariance = gaussianPriorVariance;
		this.xAlphabet = xAlphabet;
		this.yAlphabet = yAlphabet;
		this.fxy = fxy;
	}

	public LinearClassifier batchTrain(
			ArrayList<ClassificationInstance> trainingData) {
		double[] weights = new double[trainingData.size()];
		Arrays.fill(weights,1);
		return batchTrain(trainingData,weights);
	}
	
	public LinearClassifier batchTrain(
			ArrayList<ClassificationInstance> trainingData, double[] weights) {
		Objective obj = new Objective(trainingData, weights);
		// perform gradient descent
		@SuppressWarnings("unused")
		GradientAscent gaoptimizer;
		ConjugateGradient optimizer = new ConjugateGradient(obj
				.getNumParameters());
		@SuppressWarnings("unused")
		boolean success = optimizer.maximize(obj);
		System.out.println("valCalls = " + obj.numValueCalls
				+ "   gradientCalls=" + obj.numGradientCalls);
		return obj.classifier;
	}

	/**
	 * An objective for our max-ent model. That is: max_\lambda sum_i log
	 * Pr(y_i|x_i) - 1/var * ||\lambda||^2 where var is the Gaussian prior
	 * variance, and p(y|x) = exp(f(x,y)*lambda)/Z(x).
	 * 
	 * @author kuzman
	 * 
	 */
	class Objective implements DifferentiableObjective {
		double[] empiricalExpectations;
		LinearClassifier classifier;
		ArrayList<ClassificationInstance> trainingData;
		int numValueCalls = 0;
		int numGradientCalls = 0;
		double[] trainingInstanceWeights;

		Objective(ArrayList<ClassificationInstance> trainingData, double[] trainingInstanceWeights) {
			this.trainingData = trainingData;
			this.trainingInstanceWeights = trainingInstanceWeights;
			// compute empirical expectations...
			empiricalExpectations = new double[fxy.wSize()];
			for (int i = 0; i < trainingData.size(); i++) {
				ClassificationInstance inst = trainingData.get(i);
				StaticUtils.plusEquals(empiricalExpectations, fxy.apply(inst.x,
						inst.y),trainingInstanceWeights[i]);
			}
			classifier = new LinearClassifier(xAlphabet, yAlphabet, fxy);
		}

		public double getValue() {
			numValueCalls++;
			// value = log(prob(data)) - 1/gaussianPriorVariance * ||lambda||^2
			double val = 0;
			for (int i = 0; i < trainingData.size(); i++) {
				ClassificationInstance inst = trainingData.get(i);
				double[] scores = classifier.scores(inst.x);
				double[] probs = StaticUtils.exp(scores);
				double Z = StaticUtils.sum(probs);
				val += trainingInstanceWeights[i]*(scores[inst.y] - Math.log(Z));
			}
			val -= 1 / (2 * gaussianPriorVariance)
					* StaticUtils.twoNormSquared(classifier.w);
			return val;
		}

		public void getGradient(double[] gradient) {
			numGradientCalls++;
			// gradient = empiricalExpectations - modelExpectations
			// -2/gaussianPriorVariance * params
			double[] modelExpectations = new double[gradient.length];
			for (int i = 0; i < gradient.length; i++) {
				gradient[i] = empiricalExpectations[i];
				modelExpectations[i] = 0;
			}
			for (int i = 0; i < trainingData.size(); i++) {
				ClassificationInstance inst = trainingData.get(i);
				double[] scores = classifier.scores(inst.x);
				double[] probs = StaticUtils.exp(scores);
				double Z = StaticUtils.sum(probs);
				for (int y = 0; y < yAlphabet.size(); y++) {
					StaticUtils.plusEquals(modelExpectations, fxy.apply(inst.x,
							y), trainingInstanceWeights[i]*(probs[y] / Z));
				}
			}
			for (int i = 0; i < gradient.length; i++) {
				gradient[i] -= modelExpectations[i];
				gradient[i] -= 1 / gaussianPriorVariance * classifier.w[i];
			}

		}

		public void setParameters(double[] newParameters) {
			System.arraycopy(newParameters, 0, classifier.w, 0,
					newParameters.length);
		}

		public void getParameters(double[] params) {
			System.arraycopy(classifier.w, 0, params, 0, params.length);
		}

		public int getNumParameters() {
			return classifier.w.length;
		}

	}

}
