package edlin.experiments;

import java.io.IOException;
import java.util.ArrayList;

import edlin.classification.CompleteFeatureFunction;
import edlin.classification.MaxEntropy;
import edlin.classification.NaiveBayes;
import edlin.classification.Perceptron;
import edlin.io.NewsgroupsReader;
import edlin.types.Alphabet;
import edlin.types.ClassificationInstance;
import edlin.types.LinearClassifier;
import edlin.types.StaticUtils;



public class Newsgroups {

	public static void main(String[] args) throws IOException {
		// read in the data.
		ArrayList<ClassificationInstance> allData = (new NewsgroupsReader(
				new Alphabet(), new Alphabet())).readFile(args[0]);
		StaticUtils.shuffle(allData, 0);
		// randomly split data into training and testing part
		ArrayList<ClassificationInstance>[] tmp = StaticUtils.split(allData,
				(10));
		System.out.println(allData.get(0).xAlphabet.size());
		ArrayList<ClassificationInstance> train = tmp[0];
		ArrayList<ClassificationInstance> test = tmp[1];
		Alphabet xA = allData.get(0).xAlphabet;
		Alphabet yA = allData.get(0).yAlphabet;
		System.out.println("num Features = " + allData.get(0).xAlphabet.size());
		LinearClassifier h;
		h = trainMaxEnt(train, xA, yA);
		// print out accuracy
		System.out.println("MaxEnt Train Accuracy = "
				+ StaticUtils.computeAccuracy(h, train));
		System.out.println("MaxEnt Test  Accuracy = "
				+ StaticUtils.computeAccuracy(h, test));
		h = trainNaivBayes(train, xA, yA);
		System.out.println("NaiveB Train Accuracy = "
				+ StaticUtils.computeAccuracy(h, train));
		System.out.println("NaiveB Test  Accuracy = "
				+ StaticUtils.computeAccuracy(h, test));
		h = trainPerceptron(false, 20, train, xA, yA);
		System.out.println("Percep Train Accuracy = "
				+ StaticUtils.computeAccuracy(h, train));
		System.out.println("Percep Test  Accuracy = "
				+ StaticUtils.computeAccuracy(h, test));
		h = trainPerceptron(true, 20, train, xA, yA);
		System.out.println("AvgPer Train Accuracy = "
				+ StaticUtils.computeAccuracy(h, train));
		System.out.println("AvgPer Test  Accuracy = "
				+ StaticUtils.computeAccuracy(h, test));
	}

	public static LinearClassifier trainMaxEnt(
			ArrayList<ClassificationInstance> train, Alphabet xA, Alphabet yA) {
		MaxEntropy maxent = new MaxEntropy(10.0, xA, yA,
				new CompleteFeatureFunction(xA, yA));
		LinearClassifier h = maxent.batchTrain(train);
		return h;
	}

	public static LinearClassifier trainNaivBayes(
			ArrayList<ClassificationInstance> train, Alphabet xA, Alphabet yA) {
		NaiveBayes nb = new NaiveBayes(0.1, 0.1, xA, yA);
		LinearClassifier h = nb.batchTrain(train);
		return h;
	}

	public static LinearClassifier trainPerceptron(boolean doAveraging,
			int numIters, ArrayList<ClassificationInstance> train, Alphabet xA,
			Alphabet yA) {
		Perceptron p = new Perceptron(doAveraging, numIters, xA, yA,
				new CompleteFeatureFunction(xA, yA));
		LinearClassifier h = p.batchTrain(train);
		return h;
	}

}
