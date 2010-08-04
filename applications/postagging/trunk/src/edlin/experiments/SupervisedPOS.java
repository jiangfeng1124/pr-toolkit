package edlin.experiments;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import model.distribution.trainer.ObservationMultinomialFeatureFunction;

import postagging.data.PosCorpus;
import util.InputOutput;
import edlin.io.Corpus2POSFeatures;
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
import gnu.trove.TDoubleArrayList;

public class SupervisedPOS {
	
	long seed = 0;
	int numTrials = 10;
	int numTrain = 10;
	ArrayList<String> predictionFiles = new ArrayList<String>();
	
	private void parseOption(String[] args){
		System.out.println("Raw Predictions prasing options");
		for(String arg:args){
			String[] a = arg.split(":");
			String opt  = a[0];
			String v = null;
			if (a.length > 1) v = a[1];
			if (false);
			else if (opt.equals("seed")) seed = Long.parseLong(v);
			else if (opt.equals("num-trials")) numTrials = Integer.parseInt(v);
			else if (opt.equals("num-train")) numTrain = Integer.parseInt(v);
			else if (opt.equals("pred-file")) predictionFiles.add(v);
			else throw new RuntimeException("Unrecognized option: "+opt);
		}
	}

//	public static ArrayList<SequenceInstance> getRandom(ArrayList<SequenceInstance> source, int num, long seed){
//		ArrayList<SequenceInstance> res = new ArrayList<SequenceInstance>();
//		Random r = new Random(seed);
//		int[] ids = new int[num];
//		for (int i = 0; i < num; i++) {
//			boolean found = true;
//			int id = 0;
//			while (found){
//				found = false;
//				id = r.nextInt(source.size());
//				for (int j = 0; j < i; j++) {
//					if (ids[i]==id) found = true;
//				}
//			}
//			ids[i] = id;
//			res.add(source.get(r.nextInt(source.size())));
//		}
//		return res;
//	}

	public static void main(String[] args) throws IOException, IllegalArgumentException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
		String corpusFile = args[0];
		String maxEntFeaturesFile = args[1];
		PosCorpus c = new PosCorpus(corpusFile,0,Integer.MAX_VALUE,Integer.MAX_VALUE);
		SupervisedPOS me = new SupervisedPOS();
		ObservationMultinomialFeatureFunction fxy = new ObservationMultinomialFeatureFunction(c,maxEntFeaturesFile);
		me.parseOption(subArray(args,2));
		Corpus2POSFeatures inPipe = new Corpus2POSFeatures(new Alphabet(), new Alphabet(), c, fxy);
		ArrayList<SequenceInstance> allTraining = inPipe.getTrain();
		Alphabet xA = allTraining.get(0).xAlphabet;
		Alphabet yA = allTraining.get(0).yAlphabet;
		// we don't know how to decode on the test set
		// ArrayList<SequenceInstance> test = inPipe.getTest();
		for (String predFile: me.predictionFiles){
			System.out.println("Loading predictions from: "+predFile);
			BufferedReader in = InputOutput.openReader(predFile);
			ArrayList<ArrayList<String>> newFeats = new ArrayList<ArrayList<String>>();
			ArrayList<String> labels = new ArrayList<String>();
			for (String ln=in.readLine(); ln!=null; ln=in.readLine()){
				ln.replaceAll("^[ \t]*$", "");
				if (ln.length()==0){
					newFeats.add(labels);
					labels = new ArrayList<String>();
				} else {
					labels.add(ln.split("[ \t][ \t]*")[0]);
				}
			}
			if(labels.size()!=0) newFeats.add(labels);
			if (allTraining.size() != newFeats.size()) throw new RuntimeException("mismatched data and features: num instances "+newFeats.size()+" "+allTraining.size()+" "+c.trainInstances.instanceList.size()+" "+c.testInstances.get(0).instanceList.size());
			for (int instId = 0; instId < allTraining.size(); instId++) {
				labels = newFeats.get(instId);
				SequenceInstance inst = allTraining.get(instId);
				if (labels.size()!=inst.y.length) throw new RuntimeException("wrong lengths: wanted "+inst.y.length+" but observed "+labels.size());
				for (int i = 0; i < labels.size(); i++) {
					inst.x[i].add(xA.lookupObject(predFile+labels.get(i)), 1);
				}				
			}
		}
		System.out.println("num Features = " + allTraining.get(0).xAlphabet.size());

		// pred-file:tmp/pt-conll-hmm.gz
		// randomly split data into training and testing part
		for (int i = 0; i < me.numTrials; i++) {
			// shallow copy
			ArrayList<SequenceInstance> shuffled = new ArrayList<SequenceInstance>();
			for(SequenceInstance inst: allTraining) shuffled.add(inst);
			StaticUtils.shuffle(shuffled, me.seed+i);
			System.out.println("seed = "+(me.seed+i));
			ArrayList<SequenceInstance>[] trainandtest = StaticUtils.splitS(shuffled, me.numTrain);
			ArrayList<SequenceInstance> train = trainandtest[0];
			ArrayList<SequenceInstance> test = new ArrayList<SequenceInstance>();
			for (int j = 0; j < 500 && j<trainandtest[1].size(); j++) {
				test.add(trainandtest[1].get(j));
			}
			LinearTagger h;
//			h = PartOfSpeech.trainCRF(train, xA, yA);
//			System.out.println("CRF    Train Accuracy = "
//			+ StaticUtils.computeAccuracyS(h, train));
//			System.out.println("CRF    Test  Accuracy = "
//			+ StaticUtils.computeAccuracyS(h, test));
//			h = SupervisedPOS.trainPerceptron(false, 20, train, xA, yA);
//			System.out.println("Percep Train Accuracy = "
//			+ StaticUtils.computeAccuracyS(h, train));
//			System.out.println("Percep Test  Accuracy = "
//			+ StaticUtils.computeAccuracyS(h, test));
			int numIters = pickNumPerceptronIters(true, train, xA, yA);
			System.out.println("AvgPer with iters = "+numIters);
			h = SupervisedPOS.trainPerceptron(true, numIters, train, xA, yA);
			System.out.println("AvgPer Train Accuracy = "
					+ StaticUtils.computeAccuracyS(h, train));
			System.out.println("AvgPer Test  Accuracy = "
					+ StaticUtils.computeAccuracyS(h, test));
		}

//		h = SupervisedPOS.trainMira(false, 20, train, xA, yA);
//		System.out.println("Mira Train Accuracy = "
//				+ StaticUtils.computeAccuracyS(h, train));
//		System.out.println("Mira Test  Accuracy = "
//				+ StaticUtils.computeAccuracyS(h, test));
//		h = SupervisedPOS.trainMira(true, 20, train, xA, yA);
//		System.out.println("Avg Mira Train Accuracy = "
//				+ StaticUtils.computeAccuracyS(h, train));
//		System.out.println("Avg Mira Test  Accuracy = "
//				+ StaticUtils.computeAccuracyS(h, test));
	}


	private static String[] subArray(String[] a, int from) {
		String[] res = new String[a.length-from];
		for (int j = from; j < a.length; j++) {
			res[j-from] = a[j];
		}
		return res;
	}

	public static int pickNumPerceptronIters(boolean doAveraging, 
			ArrayList<SequenceInstance> allTraining, Alphabet xA, Alphabet yA){
		// how many do we do for validataion data... 
		int numTest = (int)(0.2 * allTraining.size());
		// split the training data into train and test... 
		ArrayList<SequenceInstance> shuffled = new ArrayList<SequenceInstance>();
		for(SequenceInstance inst: allTraining) shuffled.add(inst);
		StaticUtils.shuffle(shuffled, 0);
		ArrayList<SequenceInstance>[] trainandtest = StaticUtils.splitS(shuffled, numTest);
		ArrayList<SequenceInstance> test = trainandtest[0];
		ArrayList<SequenceInstance> train = trainandtest[1];
		TDoubleArrayList accuracies = new TDoubleArrayList();
		accuracies.add(0);
		int bestIndex = 0;
		for(int numIters = 1; numIters<500; numIters++){
			Perceptron p = new Perceptron(doAveraging, numIters, xA, yA, new OneYwithXFeatureFunction(xA,yA));
			LinearTagger h = p.batchTrain(train);
			accuracies.add(StaticUtils.computeAccuracyS(h, test));
			if(accuracies.get(bestIndex) < accuracies.get(numIters)){
				bestIndex = numIters;
			}
			if (bestIndex+5 <= numIters) break;
		}
		return bestIndex;
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
