package edlin.experiments;


import java.io.IOException;
import java.util.ArrayList;

import edlin.io.PartOfSpeechReader;
import edlin.sequence.CRF;
import edlin.sequence.HammingLoss;
import edlin.sequence.LinearTagger;
import edlin.sequence.Mira;
import edlin.sequence.OneYwithXFeatureFunction;
import edlin.sequence.Perceptron;
import edlin.sequence.SequenceInstance;
import edlin.sequence.TwoYwithXFeatureFunction;
import edlin.types.Alphabet;
import edlin.types.StaticUtils;


public class PartOfSpeech {

	public static void main(String[] args) throws IOException {
		// read in the data.
		ArrayList<SequenceInstance> allData = (new PartOfSpeechReader(
				new Alphabet(), new Alphabet())).readFile(args[0]);
		StaticUtils.shuffle(allData, 0);
		// randomly split data into training and testing part
		ArrayList<SequenceInstance>[] tmp = StaticUtils.splitS(allData, 150);
		ArrayList<SequenceInstance> train = tmp[0];
		ArrayList<SequenceInstance> test = tmp[1];
		Alphabet xA = allData.get(0).xAlphabet;
		Alphabet yA = allData.get(0).yAlphabet;
		System.out.println("num Features = " + allData.get(0).xAlphabet.size());
		LinearTagger h;
//		h = PartOfSpeech.trainCRF(train, xA, yA);
//		System.out.println("CRF    Train Accuracy = "
//				+ StaticUtils.computeAccuracyS(h, train));
//		System.out.println("CRF    Test  Accuracy = "
//				+ StaticUtils.computeAccuracyS(h, test));
		h = PartOfSpeech.trainPerceptron(false, 20, train, xA, yA);
		System.out.println("Percep Train Accuracy = "
				+ StaticUtils.computeAccuracyS(h, train));
		System.out.println("Percep Test  Accuracy = "
				+ StaticUtils.computeAccuracyS(h, test));
		h = PartOfSpeech.trainPerceptron(true, 20, train, xA, yA);
		System.out.println("AvgPer Train Accuracy = "
				+ StaticUtils.computeAccuracyS(h, train));
		System.out.println("AvgPer Test  Accuracy = "
				+ StaticUtils.computeAccuracyS(h, test));
		h = PartOfSpeech.trainMira(false, 20, train, xA, yA);
		System.out.println("Mira Train Accuracy = "
				+ StaticUtils.computeAccuracyS(h, train));
		System.out.println("Mira Test  Accuracy = "
				+ StaticUtils.computeAccuracyS(h, test));
		h = PartOfSpeech.trainMira(true, 20, train, xA, yA);
		System.out.println("Avg Mira Train Accuracy = "
				+ StaticUtils.computeAccuracyS(h, train));
		System.out.println("Avg Mira Test  Accuracy = "
				+ StaticUtils.computeAccuracyS(h, test));
	}

	public static LinearTagger trainPerceptron(boolean doAveraging,
			int numIters, ArrayList<SequenceInstance> train, Alphabet xA,
			Alphabet yA) {
		Perceptron p = new Perceptron(doAveraging, numIters, xA, yA,
				new OneYwithXFeatureFunction(xA, yA));
		LinearTagger h = p.batchTrain(train);
		return h;
	}

	public static LinearTagger trainMira(boolean doAveraging,
			int numIters, ArrayList<SequenceInstance> train, Alphabet xA,
			Alphabet yA) {
		Mira p = new Mira(doAveraging, numIters, xA, yA,
				new TwoYwithXFeatureFunction(xA, yA), new HammingLoss());
		LinearTagger h = p.batchTrain(train);
		return h;
	}

	public static LinearTagger trainCRF(ArrayList<SequenceInstance> train,
			Alphabet xA, Alphabet yA) {
		CRF crf = new CRF(10, xA, yA, new OneYwithXFeatureFunction(xA, yA));
		LinearTagger h = crf.batchTrain(train);
		return h;
	}

}
