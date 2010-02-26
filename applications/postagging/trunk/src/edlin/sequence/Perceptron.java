package edlin.sequence;

import java.util.ArrayList;

import edlin.types.Alphabet;
import edlin.types.StaticUtils;


public class Perceptron {

	boolean performAveraging;
	int numIterations;
	Alphabet xAlphabet;
	Alphabet yAlphabet;
	SequenceFeatureFunction fxy;

	public Perceptron(boolean performAveraging, int numIterations,
			Alphabet xAlphabet, Alphabet yAlphabet, SequenceFeatureFunction fxy) {
		this.performAveraging = performAveraging;
		this.numIterations = numIterations;
		this.xAlphabet = xAlphabet;
		this.yAlphabet = yAlphabet;
		this.fxy = fxy;
	}

	public LinearTagger batchTrain(ArrayList<SequenceInstance> trainingData) {
		LinearTagger w = new LinearTagger(xAlphabet, yAlphabet, fxy);
		LinearTagger theta = null;
		if (performAveraging)
			theta = new LinearTagger(xAlphabet, yAlphabet, fxy);
		for (int iter = 0; iter < numIterations; iter++) {
			for (SequenceInstance inst : trainingData) {
				int[] yhat = w.label(inst.x);
				// if y = yhat then this update will not change w.
				StaticUtils.plusEquals(w.w, fxy.apply(inst.x, inst.y));
				StaticUtils.plusEquals(w.w, fxy.apply(inst.x, yhat), -1);
				if (performAveraging)
					StaticUtils.plusEquals(theta.w, w.w, 1);
			}
		}
		if (performAveraging)
			return theta;
		return w;
	}

}
