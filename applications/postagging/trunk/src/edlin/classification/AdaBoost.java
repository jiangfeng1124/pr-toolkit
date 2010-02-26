package edlin.classification;

import java.util.ArrayList;
import java.util.Random;

import edlin.types.Alphabet;
import edlin.types.ClassificationInstance;
import edlin.types.FeatureFunction;
import edlin.types.LinearClassifier;
import edlin.types.SparseVector;
import edlin.types.StaticUtils;


public class AdaBoost {

	int numIterations;
	Alphabet xAlphabet;
	Alphabet yAlphabet;
	FeatureFunction fxy;
	double smooth = 0.01;

	public AdaBoost(int numIterations, Alphabet xAlphabet, Alphabet yAlphabet,
			FeatureFunction fxy) {
		this.numIterations = numIterations;
		this.xAlphabet = xAlphabet;
		this.yAlphabet = yAlphabet;
		this.fxy = fxy;
	}

	public void printArray(double[] a) {
		for (int i = 0; i < a.length; i++) {
			System.out.print(a[i] + " ");
		}
		System.out.println();
	}

	public LinearClassifier batchTrain(
			ArrayList<ClassificationInstance> trainingData) {
		LinearClassifier result = new LinearClassifier(xAlphabet, yAlphabet,
				fxy);
		double[] w = new double[trainingData.size()];
		for (int i = 0; i < w.length; i++)
			w[i] = 1.0 / trainingData.size();
		// choose $t$ weights.
		double[] correct = new double[fxy.wSize()];
		double[] wrong = new double[fxy.wSize()];

		@SuppressWarnings("unused")
		int oldBest = 0;
		for (int iter = 0; iter < numIterations; iter++) {
			computeAccuracies(correct, wrong, trainingData, w);
			// System.out.println();
			// System.out.print(" correct = ");
			// printArray(correct);
			// System.out.print(" wrong = ");
			// printArray(wrong);
			int bestFeature = chooseBest(correct, wrong);
			double alpha = Math
					.log((correct[bestFeature]) / wrong[bestFeature]) / 2;
			result.w[bestFeature] += alpha;
			updateW(w, bestFeature, alpha, trainingData);
			// System.out.print(" w = ");
			// printArray(w);
		}
		return result;
	}

	private int chooseBest(double[] correct, double[] wrong) {
		int res = 0;
		double bestval = Double.MIN_VALUE;
		for (int i = 0; i < correct.length; i++) {
			double val = correct[i] - wrong[i];
			if (val > bestval) {
				res = i;
				bestval = val;
			}
		}
		return res;
	}

	private void updateW(double[] w, int bestFeature, double alpha,
			ArrayList<ClassificationInstance> trainingData) {
		double wrongUpdate = Math.exp(alpha);
		double correctUpdate = Math.exp(-alpha);
		for (int instInd = 0; instInd < trainingData.size(); instInd++) {
			ClassificationInstance inst = trainingData.get(instInd);
			for (int y = 0; y < yAlphabet.size(); y++) {
				SparseVector fv = fxy.apply(inst.x, y);
				for (int i = 0; i < fv.numEntries(); i++) {
					if (fv.getIndexAt(i) == bestFeature) {
						if (y == inst.y)
							w[instInd] *= correctUpdate;
						else
							w[instInd] *= wrongUpdate;
					}
				}
			}
		}
		double sum = StaticUtils.sum(w);
		for (int i = 0; i < w.length; i++) {
			w[i] /= sum;
		}

	}

	private void computeAccuracies(double[] correct, double[] wrongs,
			ArrayList<ClassificationInstance> trainingData, double[] w) {
		double total = 2 * smooth;
		for (int i = 0; i < correct.length; i++) {
			correct[i] = smooth;
			wrongs[i] = smooth;
		}
		for (int instInd = 0; instInd < trainingData.size(); instInd++) {
			ClassificationInstance inst = trainingData.get(instInd);
			total += w[instInd];
			for (int y = 0; y < yAlphabet.size(); y++) {
				SparseVector fv = fxy.apply(inst.x, y);
				if (y == inst.y) {
					for (int i = 0; i < fv.numEntries(); i++) {
						correct[fv.getIndexAt(i)] += w[instInd];
					}
				} else {
					for (int i = 0; i < fv.numEntries(); i++) {
						wrongs[fv.getIndexAt(i)] += w[instInd];
					}
				}
			}
		}
		for (int i = 0; i < correct.length; i++) {
			correct[i] /= total;
			wrongs[i] /= total;
		}
	}

	public static void main(String[] args) {
		ArrayList<ClassificationInstance> train = new ArrayList<ClassificationInstance>();
		Alphabet xAlphabet = new Alphabet();
		Alphabet yAlphabet = new Alphabet();
		String[] classes = new String[] { "a", "b" };
		Random r = new Random(10);
		int numFeats = 5;
		double randomFrac = 0.5;
		double missingFrac = 0.5;
		for (int instInd = 0; instInd < 10; instInd++) {
			String label = classes[r.nextInt(classes.length)];
			SparseVector sv = new SparseVector();
			for (int fInd = 0; fInd < numFeats; fInd++) {
				if (r.nextDouble() < missingFrac)
					continue;
				String tmpLab = label;
				if (r.nextDouble() < randomFrac)
					tmpLab = classes[r.nextInt(classes.length)];
				sv.add(xAlphabet.lookupObject(tmpLab + fInd), 1);
			}
			train.add(new ClassificationInstance(xAlphabet, yAlphabet, sv,
					label));
		}
		AdaBoost boost = new AdaBoost(10, xAlphabet, yAlphabet,
				new CompleteFeatureFunction(xAlphabet, yAlphabet));
		LinearClassifier h = boost.batchTrain(train);
		System.out.println(StaticUtils.computeAccuracy(h, train));
	}

}
