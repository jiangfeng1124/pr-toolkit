package edlin.classification;

import java.util.ArrayList;

import edlin.types.Alphabet;
import edlin.types.ClassificationInstance;
import edlin.types.FeatureFunction;
import edlin.types.LinearClassifier;
import edlin.types.StaticUtils;


public class Perceptron {

	boolean performAveraging;
	int numIterations;
	Alphabet xAlphabet;
	Alphabet yAlphabet;
	FeatureFunction fxy;

	public Perceptron(boolean performAveraging, int numIterations,
			Alphabet xAlphabet, Alphabet yAlphabet, FeatureFunction fxy) {
		this.performAveraging = performAveraging;
		this.numIterations = numIterations;
		this.xAlphabet = xAlphabet;
		this.yAlphabet = yAlphabet;
		this.fxy = fxy;
	}

	public LinearClassifier batchTrain(
			ArrayList<ClassificationInstance> trainingData) {
		LinearClassifier w = new LinearClassifier(xAlphabet, yAlphabet, fxy);
		LinearClassifier theta = null;
		if (performAveraging)
			theta = new LinearClassifier(xAlphabet, yAlphabet, fxy);
		for (int iter = 0; iter < numIterations; iter++) {
			for (ClassificationInstance inst : trainingData) {
				int yhat = w.label(inst.x);
				if (yhat != inst.y) {
					StaticUtils.plusEquals(w.w, fxy.apply(inst.x, inst.y));
					StaticUtils.plusEquals(w.w, fxy.apply(inst.x, yhat), -1);
				}
				if (performAveraging)
					StaticUtils.plusEquals(theta.w, w.w, 1);
			}
		}
		if (performAveraging)
			return theta;
		return w;
	}

}
