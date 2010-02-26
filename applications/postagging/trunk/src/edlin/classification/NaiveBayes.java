package edlin.classification;

import java.util.ArrayList;

import edlin.types.Alphabet;
import edlin.types.ClassificationInstance;
import edlin.types.LinearClassifier;

public class NaiveBayes {

	double[] counts;
	Alphabet xAlphabet;
	Alphabet yAlphabet;
	CompleteFeatureFunction fxy;

	public NaiveBayes(double smoothTrue, double smoothFalse,
			Alphabet xAlphabet, Alphabet yAlphabet) {
		this.xAlphabet = xAlphabet;
		this.yAlphabet = yAlphabet;
		fxy = new CompleteFeatureFunction(xAlphabet, yAlphabet);
		counts = new double[fxy.wSize()];
		int defaultFeatureIndex = fxy.defalutFeatureIndex;
		for (int y = 0; y < yAlphabet.size(); y++) {
			counts[indexOf(y, defaultFeatureIndex)] = smoothTrue + smoothFalse;
			for (int f = 0; f < xAlphabet.size(); f++) {
				counts[indexOf(y, f)] = smoothTrue;
			}
		}
	}

	private int indexOf(int y, int feat) {
		return y * (fxy.defalutFeatureIndex + 1) + feat;
	}

	public LinearClassifier batchTrain(
			ArrayList<ClassificationInstance> trainingData) {
		LinearClassifier res = new LinearClassifier(xAlphabet, yAlphabet, fxy);
		int defaultFeatureIndex = fxy.defalutFeatureIndex;

		// update the counts that we've seen so far
		for (ClassificationInstance inst : trainingData) {
			counts[indexOf(inst.y, defaultFeatureIndex)] += 1;
			for (int i = 0; i < inst.x.numEntries(); i++) {
				counts[indexOf(inst.y, inst.x.getIndexAt(i))] += 1;
			}
		}

		double sumYCounts = 0;
		for (int y = 0; y < yAlphabet.size(); y++) {
			sumYCounts += counts[indexOf(y, defaultFeatureIndex)];
		}

		// compute the probabilities given the current counts
		for (int y = 0; y < yAlphabet.size(); y++) {
			double countOfY = counts[indexOf(y, defaultFeatureIndex)];
			double prY = countOfY / sumYCounts;
			double weightY = Math.log(prY);
			if (Double.isNaN(weightY))
				throw new AssertionError();
			for (int f = 0; f < defaultFeatureIndex; f++) {
				double prXfgivenY = counts[indexOf(y, f)] / countOfY;
				double prNotXfgivenY = 1 - prXfgivenY;
				weightY += Math.log(prNotXfgivenY);
				if (Double.isNaN(weightY))
					throw new AssertionError();
				res.w[indexOf(y, f)] -= Math.log(prNotXfgivenY);
				res.w[indexOf(y, f)] += Math.log(prXfgivenY);
			}
			res.w[indexOf(y, defaultFeatureIndex)] = weightY;
		}
		return res;
	}

}
