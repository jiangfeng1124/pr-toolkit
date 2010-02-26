package edlin.sequence;

import java.util.ArrayList;

import edlin.algo.ConjugateGradient;
import edlin.algo.GradientAscent;
import edlin.types.Alphabet;
import edlin.types.DifferentiableObjective;
import edlin.types.StaticUtils;


public class CRF {

	double gaussianPriorVariance;
	double numObservations;
	Alphabet xAlphabet;
	Alphabet yAlphabet;
	SequenceFeatureFunction fxy;

	public CRF(double gaussianPriorVariance, Alphabet xAlphabet,
			Alphabet yAlphabet, SequenceFeatureFunction fxy) {
		this.gaussianPriorVariance = gaussianPriorVariance;
		this.xAlphabet = xAlphabet;
		this.yAlphabet = yAlphabet;
		this.fxy = fxy;
	}

	public LinearTagger batchTrain(ArrayList<SequenceInstance> trainingData) {
		Objective obj = new Objective(trainingData);
		// perform gradient descent
		@SuppressWarnings("unused")
		GradientAscent gaoptimizer = new GradientAscent();
		@SuppressWarnings("unused")
		ConjugateGradient optimizer = new ConjugateGradient(obj
				.getNumParameters());
		@SuppressWarnings("unused")
		boolean success = optimizer.maximize(obj);
		System.out.println("valCalls = " + obj.numValueCalls
				+ "   gradientCalls=" + obj.numGradientCalls);
		return obj.tagger;
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
		LinearTagger tagger;
		ArrayList<SequenceInstance> trainingData;
		int numValueCalls = 0;
		int numGradientCalls = 0;

		Objective(ArrayList<SequenceInstance> trainingData) {
			this.trainingData = trainingData;
			// compute empirical expectations...
			empiricalExpectations = new double[fxy.wSize()];
			for (SequenceInstance inst : trainingData) {
				StaticUtils.plusEquals(empiricalExpectations, fxy.apply(inst.x,
						inst.y));
			}
			tagger = new LinearTagger(xAlphabet, yAlphabet, fxy);
		}

		private double[][] forward(double[][][] expS) {
			double[][] res = new double[expS.length][yAlphabet.size()];
			for (int y = 0; y < yAlphabet.size(); y++) {
				res[0][y] = expS[0][0][y];
			}
			for (int t = 1; t < expS.length; t++) {
				for (int yt = 0; yt < yAlphabet.size(); yt++) {
					for (int ytm1 = 0; ytm1 < yAlphabet.size(); ytm1++) {
						res[t][yt] += res[t - 1][ytm1] * expS[t][ytm1][yt];
					}
				}
			}
			return res;
		}

		private double[][] backward(double[][][] expS) {
			double[][] res = new double[expS.length][yAlphabet.size()];
			for (int y = 0; y < yAlphabet.size(); y++) {
				res[expS.length - 1][y] = 1;
			}
			for (int t = expS.length - 1; t > 0; t--) {
				for (int yt = 0; yt < yAlphabet.size(); yt++) {
					for (int ytm1 = 0; ytm1 < yAlphabet.size(); ytm1++) {
						res[t - 1][ytm1] += res[t][yt] * expS[t][ytm1][yt];
					}
				}
			}
			return res;
		}

		private void normalizeScores(double[][][] scores) {
			for (int t = 0; t < scores.length; t++) {
				double max = 0;
				for (int ytm1 = 0; ytm1 < yAlphabet.size(); ytm1++) {
					for (int yt = 0; yt < yAlphabet.size(); yt++) {
						max = Math.max(max, scores[t][ytm1][yt]);
					}
				}
				// max = max/yAlphabet.size();
				for (int ytm1 = 0; ytm1 < yAlphabet.size(); ytm1++) {
					for (int yt = 0; yt < yAlphabet.size(); yt++) {
						scores[t][ytm1][yt] -= max;
					}
				}
			}
		}

		public double getValue() {
			numValueCalls++;
			// value = log(prob(data)) - 1/gaussianPriorVariance * ||lambda||^2
			double val = 0;
			int numUnnormalizedInstances = 0;
			for (SequenceInstance inst : trainingData) {
				double[][][] scores = tagger.scores(inst.x);
				normalizeScores(scores);
				double[][][] expScores = StaticUtils.exp(scores);
				double[][] alpha = forward(expScores);
				// just need likelihood.. so no beta
				// double[][] beta = backward(expScores);
				double Z = StaticUtils.sum(alpha[inst.x.length - 1]);
				if (Z == 0 || Double.isNaN(Z) || Double.isInfinite(Z)) {
					// throw new RuntimeException("can't normalize instance.
					// Z="+Z);
					if (numUnnormalizedInstances < 3) {
						System.err.println("Could not normalize instance (" + Z
								+ "), skipping");
					} else if (numUnnormalizedInstances == 3) {
						System.err.println("    ...");
					}
					numUnnormalizedInstances++;
					continue;
				}
				val += Math.log(expScores[0][0][inst.y[0]]);
				for (int t = 1; t < inst.y.length; t++) {
					val += Math.log(expScores[t][inst.y[t - 1]][inst.y[t]]);
				}
				val -= Math.log(Z);
			}
			if (numUnnormalizedInstances != 0)
				System.err.println("Could not normalize "
						+ numUnnormalizedInstances + " instances");
			val -= 1 / (2 * gaussianPriorVariance)
					* StaticUtils.twoNormSquared(tagger.w);
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
			int numUnnormalizedInstances = 0;
			for (SequenceInstance inst : trainingData) {
				double[][][] scores = tagger.scores(inst.x);
				normalizeScores(scores);
				double[][][] expScores = StaticUtils.exp(scores);
				double[][] alpha = forward(expScores);
				// just need likelihood.. so no beta
				double[][] beta = backward(expScores);
				double Z = StaticUtils.sum(alpha[inst.x.length - 1]);
				if (Z == 0 || Double.isNaN(Z) || Double.isInfinite(Z)) {
					if (numUnnormalizedInstances < 3) {
						System.err.println("Could not normalize instance (" + Z
								+ "), skipping");
					} else if (numUnnormalizedInstances == 3) {
						System.err.println("    ...");
					}
					numUnnormalizedInstances++;
					continue;
					// throw new RuntimeException("can't normalize instance.
					// Z="+Z);
				}
				for (int yt = 0; yt < yAlphabet.size(); yt++) {
					StaticUtils.plusEquals(modelExpectations, fxy.apply(inst.x,
							0, yt, 0), alpha[0][yt] * beta[0][yt]
							* expScores[0][0][yt] / Z);
				}
				for (int t = 1; t < inst.x.length; t++) {
					for (int ytm1 = 0; ytm1 < yAlphabet.size(); ytm1++) {
						for (int yt = 0; yt < yAlphabet.size(); yt++) {
							StaticUtils.plusEquals(modelExpectations, fxy
									.apply(inst.x, ytm1, yt, t),
									alpha[t - 1][ytm1] * beta[t][yt]
											* expScores[t][ytm1][yt] / Z);
						}
					}
				}
			}

			for (int i = 0; i < gradient.length; i++) {
				gradient[i] -= modelExpectations[i];
				gradient[i] -= 1 / gaussianPriorVariance * tagger.w[i];

			}

		}

		public void setParameters(double[] newParameters) {
			System.arraycopy(newParameters, 0, tagger.w, 0,
					newParameters.length);
		}

		public void getParameters(double[] params) {
			System.arraycopy(tagger.w, 0, params, 0, params.length);
		}

		public int getNumParameters() {
			return tagger.w.length;
		}

	}

}
