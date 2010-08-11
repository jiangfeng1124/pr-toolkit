package model.chain.hmm;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntProcedure;

import java.util.Random;

import com.sun.tools.javac.comp.TransTypes;

import model.AbstractCountTable;
import model.AbstractSentenceDist;
import model.distribution.trainer.MaxEntClassifier;
import model.distribution.trainer.MultinomialMaxEntDirectGradientTrainer;
import model.distribution.trainer.MultinomialMaxEntTrainer;
import optimization.gradientBasedMethods.Objective;
import util.ArrayMath;
import util.ArrayPrinting;
import util.MathUtil;

public class HMMDirectGradientObjective extends Objective {

	AbstractCountTable counts;
	MultinomialMaxEntDirectGradientTrainer initTrainer, transitionTrainer, observationTrainer;
	int initOffset, transitionOffset, observationOffset;
	HMM model;
	double gaussianPriorVariance;
	AbstractSentenceDist[] sentenceDists;
	int iter = 0;
	double value = Double.NaN;
	
	
	public HMMDirectGradientObjective(HMM hmm, double prior){
		model = hmm;
		counts = hmm.getCountTable();
		initOffset = 0;
		gaussianPriorVariance = prior;
		HMMCountTable hmmcounts = (HMMCountTable) counts;
		initTrainer = new MultinomialMaxEntDirectGradientTrainer((MultinomialMaxEntTrainer) hmm.initTrainer, hmmcounts.initialCounts);
		transitionOffset = initTrainer.numParams();
		transitionTrainer = new MultinomialMaxEntDirectGradientTrainer((MultinomialMaxEntTrainer) hmm.transitionsTrainer, hmmcounts.transitionCounts);
		observationOffset = transitionOffset+transitionTrainer.numParams();
		observationTrainer = new MultinomialMaxEntDirectGradientTrainer((MultinomialMaxEntTrainer) hmm.observationTrainer, hmmcounts.observationCounts);
		parameters = new double[observationOffset+observationTrainer.numParams()];
		Random r = new Random(2);
		for (int i = 0; i < parameters.length; i++) {
			parameters[i] = (r.nextDouble()-0.5)/10;
		}
//		// FIXME: debug
//		parameters[17] = 1;
//		parameters[18] = -1;	
//		parameters[21] = 1;
//		parameters[20] = -1;
//		parameters[7] = 100;
//		parameters[11] = 100;
		gradient = new double[parameters.length];
		sentenceDists = model.getSentenceDists();
		updateValueAndGradient();
//		testGradient();
//		setParameters(parameters);
//		System.out.println("===========================");
//		System.out.println("===========================");
	}
	// FIXME: the current way of doing things doesn't take into account the initialization, I don't think.
	public void updateValueAndGradient(){
		// set parameters: this.params -> trainer.params; trainer.params -> hmm.params
		HMMCountTable hmmcounts = (HMMCountTable) counts;
		initTrainer.setCountsAndParameters(hmmcounts.initialCounts, parameters, initOffset);
		transitionTrainer.setCountsAndParameters(hmmcounts.transitionCounts, parameters, transitionOffset);
		observationTrainer.setCountsAndParameters(hmmcounts.observationCounts, parameters, observationOffset);
		initTrainer.getMultinomialAtCurrentParams(model.initialProbabilities);
		transitionTrainer.getMultinomialAtCurrentParams(model.transitionProbabilities);
		observationTrainer.getMultinomialAtCurrentParams(model.observationProbabilities);

		// compute the inference-induced counts at the current parameters.
		counts.clear();
		//		corpusEStep(counts, sentenceDists, stats);
		//System.out.println("Using senteces on training:" + sentenceDists.length);
		value = 0;
		for(AbstractSentenceDist sd : sentenceDists){			
			// sentenceEStep(sd, counts, stats);
			sd.initSentenceDist();
			model.computePosteriors(sd);
			model.addToCounts(sd,counts);	
			value -= sd.getLogLikelihood();
			sd.clearCaches();
			sd.clearPosteriors();
		}
		// FIXME: I don't like having to compute the value seperately from the gradient
		value +=  1/gaussianPriorVariance*ArrayMath.twoNormSquared(parameters);
		// update the empirical counts so we can compute the value and gradient. 
		initTrainer.setCountsAndParameters(hmmcounts.initialCounts, parameters, initOffset);
		transitionTrainer.setCountsAndParameters(hmmcounts.transitionCounts, parameters, transitionOffset);
		observationTrainer.setCountsAndParameters(hmmcounts.observationCounts, parameters, observationOffset);
		//
//		value = initTrainer.getValue() + transitionTrainer.getValue() + observationTrainer.getValue();
		initTrainer.getGradient(gradient, initOffset);
		transitionTrainer.getGradient(gradient, transitionOffset);
		observationTrainer.getGradient(gradient, observationOffset);
	}
	
	boolean myDebug=false;
	@Override 
	public void setParameters(double[] parameters){
//		System.out.println("------------");
//		System.out.println("------------ begin set parameters ------------");
////		System.out.println(model.transitionProbabilities.toString("transition params before update = ",null,null));
////		System.out.println(((HMMCountTable)counts).transitionCounts.toString("transition counts before update", null, null));
//		System.out.println(model.observationProbabilities.toString("observation params before update = ",null,null));
//		System.out.println(((HMMCountTable)counts).observationCounts.toString("observation counts before update", null, null));
		super.setParameters(parameters);
//		System.out.println(ArrayPrinting.doubleArrayToString(parameters, null, "new parameters"));
//		myDebug = true;
		updateValueAndGradient();
//		myDebug = false;
//		System.out.println(model.transitionProbabilities.toString("transition params after update = ",null,null));
//		System.out.println(((HMMCountTable)counts).transitionCounts.toString("transition counts after update", null, null));
//		System.out.println(model.observationProbabilities.toString("observation params after update = ",null,null));
//		System.out.println(((HMMCountTable)counts).observationCounts.toString("observation counts after update", null, null));
//		System.out.println("------------");
//		testGradient();
//		System.out.println("------------ end set parameters ------------");
//		System.out.println("------------");
	}
	
	public void testGradient(){
		System.out.println("testing gradient -- value = "+getValue());
		System.out.println("Value (log-likely) = "+value);
		System.out.print("Value (trainers) =   "+String.format("%.3f  ",
				(initTrainer.getValue() + transitionTrainer.getValue() + observationTrainer.getValue())));
		System.out.println(String.format("%.3f %.3f %.3f", 
				initTrainer.getValue(), transitionTrainer.getValue(), observationTrainer.getValue()));
		System.out.println("diff "+(value - (initTrainer.getValue() + transitionTrainer.getValue() + observationTrainer.getValue())));
		TIntArrayList variables = new TIntArrayList();
		int maxNumDirs = 100;
		if (parameters.length > maxNumDirs) {
			System.out.println("big gradient ("+parameters.length+")... subsampling directions");
		}
		Random rand = new Random(0);
		for (int i = 0; i < gradient.length; i++) {
			if (rand.nextDouble()*gradient.length <= maxNumDirs)
				variables.add(i);
		}

		double epsilon = 0.0001;
		updateValueAndGradient();
		double origV = getValue();
		double[] numGrad = new double[variables.size()];
		for (int i = 0; i < numGrad.length; i++) {
			numGrad[i] = gradient[variables.get(i)];
		}
		double[] plusGrad = new double[numGrad.length];
		double[] minusGrad = new double[numGrad.length];
		for (int i = 0; i < numGrad.length; i++) {
			int gind = variables.get(i);
			double theta = parameters[gind];
			parameters[gind] += epsilon;
			updateValueAndGradient();
			plusGrad[i] = (getValue()-origV)/epsilon;
			parameters[gind] = theta - epsilon;
			updateValueAndGradient();
			minusGrad[i] = (origV-getValue())/epsilon;
			parameters[gind] = theta;
		}
		System.out.println("||analytical||^2="+util.ArrayMath.twoNormSquared(numGrad) + 
				"  ||+||^2="+util.ArrayMath.twoNormSquared(plusGrad) +
				"  ||-||^2="+util.ArrayMath.twoNormSquared(minusGrad) );
		double plusCos = ArrayMath.cosine(plusGrad, numGrad);
		double minusCos = ArrayMath.cosine(minusGrad, numGrad);
		System.out.println("cos(+,comp) = "+plusCos);
		System.out.println("cos(-,comp) = "+minusCos);
		if (plusCos <=0.97 || minusCos <= 0.97){
			for (int i = 0; i < minusGrad.length; i++) {
				String r = null;
				if(i>=initOffset && i<transitionOffset) r = " init "+initTrainer.featureToString(i-initOffset);
				if(i>=transitionOffset && i<observationOffset) r = "trans "+transitionTrainer.featureToString(i-transitionOffset);
				if(i>=observationOffset) r = "  obs "+observationTrainer.featureToString(i-observationOffset);
				System.out.print(String.format("%2d  ", i));
				System.out.print(myf2str(parameters[i]));
				System.out.print(myf2str(numGrad[i]));
				System.out.print(myf2str(plusGrad[i]));
				System.out.print(myf2str(minusGrad[i]));
				System.out.println(String.format("%s", r));
			}
			//throw new AssertionError("probable bug in gradient computation!");
		}
		
		updateValueAndGradient();
	}
	
	private String myf2str(double f){
		String s = String.format("%.2f", f);
		while (s.length() < 7) s = " "+s;
		return s;
	}
	
	@Override 
	public void setParameter(int i, double v){
		super.setParameter(i, v);
		// NOTE: need to call setCountsAndParameters on the appropriate thing!
		throw new UnsupportedOperationException("setting one parameter at a time is not supported yet!");
	}
	
	@Override
	public double[] getGradient() {
		return gradient;
	}

	@Override
	public double getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "HMMDirectGradeintObjective";
	}

}
