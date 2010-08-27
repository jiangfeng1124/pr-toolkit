package model.chain.hmm;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntProcedure;

import java.util.Random;

import com.sun.tools.javac.comp.TransTypes;

import constraints.CorpusConstraints;

import model.AbstractCountTable;
import model.AbstractSentenceDist;
import model.CantNormalizeException;
import model.chain.hmm.directGradientStats.MultinomialMaxEntDirectTrainerStats;
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
	int initOffset, transitionOffset;
	public int observationOffset;
	public HMM model;
	double gaussianPriorVariance;
	AbstractSentenceDist[] sentenceDists;
	int iter = 0;
	double value = Double.NaN;
	CorpusConstraints constraints;

	MultinomialMaxEntDirectTrainerStats stats;
	
	public HMMDirectGradientObjective(HMM hmm, double prior, CorpusConstraints constraints, MultinomialMaxEntDirectTrainerStats stats){
		model = hmm;
		counts = hmm.getCountTable();
		this.constraints = constraints;
		initOffset = 0;
		gaussianPriorVariance = prior;
		sentenceDists = model.getSentenceDists();
		HMMCountTable hmmcounts = (HMMCountTable) counts;
		initTrainer = new MultinomialMaxEntDirectGradientTrainer((MultinomialMaxEntTrainer) hmm.initTrainer, hmmcounts.initialCounts);
		transitionOffset = initTrainer.numParams();
		transitionTrainer = new MultinomialMaxEntDirectGradientTrainer((MultinomialMaxEntTrainer) hmm.transitionsTrainer, hmmcounts.transitionCounts);
		observationOffset = transitionOffset+transitionTrainer.numParams();
		observationTrainer = new MultinomialMaxEntDirectGradientTrainer((MultinomialMaxEntTrainer) hmm.observationTrainer, hmmcounts.observationCounts);
		parameters = new double[observationOffset+observationTrainer.numParams()];
		this.stats = stats;
		// the commented out code below is for debugging; it usually results in terrible performance
		// and large gradients that cause the optimization to fail, so it's a good way to test
		// fail-safes. 
//		Random r = new Random(0);
//		for (int i = 0; i < parameters.length; i++) {
//			parameters[i] = r.nextDouble();
//		}
		// This is the better way to initialize:
		// initialize parameters from first step of EM... 
		if (constraints != null){
			constraints.project(counts, sentenceDists, null, null);
		} else {
			counts.clear();
			stats.beforeInference(this);
			for(AbstractSentenceDist sd : sentenceDists){			
				// sentenceEStep(sd, counts, stats);
				stats.beforeSentenceInference(this,sd);
				sd.initSentenceDist();
				model.computePosteriors(sd);
				model.addToCounts(sd,counts);	
				stats.afterSentenceInference(this,sd);
				sd.clearCaches();
				sd.clearPosteriors();
			}
			stats.endInference(this);
		}
		initTrainer.getParametersForCounts(hmmcounts.initialCounts, parameters, initOffset);
		transitionTrainer.getParametersForCounts(hmmcounts.transitionCounts, parameters, transitionOffset);
		observationTrainer.getParametersForCounts(hmmcounts.observationCounts, parameters, observationOffset);
		gradient = new double[parameters.length];
		updateValueAndGradient();
		System.out.println("Finished initializing "+this.getClass().getSimpleName()+" value: "+value+" ||grad||^2="+ArrayMath.twoNormSquared(gradient));
//		testGradient();
	}
	
	/**
	 * Initializes with values between -jitter and jitter
	 * @param r
	 * @param jitter
	 */
	public static void initializeRandom(HMM model, Random r,double jitter){
		HMMCountTable hmmcounts = (HMMCountTable) model.getCountTable();
		int initOffset = 0;
		MultinomialMaxEntDirectGradientTrainer initTrainer = new MultinomialMaxEntDirectGradientTrainer((MultinomialMaxEntTrainer) model.initTrainer, hmmcounts.initialCounts);
		int transitionOffset = initTrainer.numParams();
		MultinomialMaxEntDirectGradientTrainer transitionTrainer = new MultinomialMaxEntDirectGradientTrainer((MultinomialMaxEntTrainer) model.transitionsTrainer, hmmcounts.transitionCounts);
		int observationOffset = transitionOffset+transitionTrainer.numParams();
		MultinomialMaxEntDirectGradientTrainer observationTrainer = new MultinomialMaxEntDirectGradientTrainer((MultinomialMaxEntTrainer) model.observationTrainer, hmmcounts.observationCounts);
		double[] parameters = new double[observationOffset+observationTrainer.numParams()];
		for(int i = 0; i < parameters.length; i++){
			double rand = r.nextDouble()*2 -1; //Number between -1 and 1
			double value =  jitter* rand;
			parameters[i] = value;
		}
		initTrainer.setCountsAndParameters(hmmcounts.initialCounts, parameters, initOffset);
		transitionTrainer.setCountsAndParameters(hmmcounts.transitionCounts, parameters, transitionOffset);
		observationTrainer.setCountsAndParameters(hmmcounts.observationCounts, parameters, observationOffset);
		initTrainer.getMultinomialAtCurrentParams(model.initialProbabilities);
		transitionTrainer.getMultinomialAtCurrentParams(model.transitionProbabilities);
		observationTrainer.getMultinomialAtCurrentParams(model.observationProbabilities);
	}

	
	public void updateValueAndGradient(){
		// set parameters: this.params -> trainer.params; trainer.params -> hmm.params
		HMMCountTable hmmcounts = (HMMCountTable) counts;
		try {
			initTrainer.setCountsAndParameters(hmmcounts.initialCounts, parameters, initOffset);
			transitionTrainer.setCountsAndParameters(hmmcounts.transitionCounts, parameters, transitionOffset);
			observationTrainer.setCountsAndParameters(hmmcounts.observationCounts, parameters, observationOffset);
			initTrainer.getMultinomialAtCurrentParams(model.initialProbabilities);
			transitionTrainer.getMultinomialAtCurrentParams(model.transitionProbabilities);
			observationTrainer.getMultinomialAtCurrentParams(model.observationProbabilities);
		} catch (CantNormalizeException e){
			e.printStackTrace(System.err);
			value = Double.POSITIVE_INFINITY;
			for (int i = 0; i < gradient.length; i++) {
				gradient[i] = 0;
			}
			return;
		}

		// compute the inference-induced counts at the current parameters.
		if (constraints != null){
			// FIXME -- this is to test the idea. 
			try{
			value = -constraints.project(counts, sentenceDists, null, null);
			} catch (CantNormalizeException e){
				System.err.println("Warning -- failed to normalize projection of constraints. ");
				System.err.println(e.getMessage());
				System.err.println(e.getStackTrace().toString());
				System.err.println("Assuming this is because parameters are too big/small");
				System.err.println(String.format("max param = %.2f min param = %.2f; max grad = %.2f, min grad = %.2f, grad norm = %.2f",
						MathUtil.max(parameters), MathUtil.min(parameters),
						MathUtil.max(gradient), MathUtil.min(gradient),
						Math.sqrt(ArrayMath.twoNormSquared(gradient))
				));
				value = Double.POSITIVE_INFINITY;
			}
		} else {
			counts.clear();
			value = 0;
			stats.beforeInference(this);
			for(AbstractSentenceDist sd : sentenceDists){			
				// sentenceEStep(sd, counts, stats);
				stats.beforeSentenceInference(this,sd);
				sd.initSentenceDist();
				try{
					model.computePosteriors(sd);
					model.addToCounts(sd,counts);	
					stats.afterSentenceInference(this,sd);
				} catch (CantNormalizeException e){
					System.err.print("Warning -- failed to normalize during update counts ("+e.problem+")");
					System.err.println(" thrown at: "+e.getStackTrace()[0].toString());
					//				e.printStackTrace(System.err);
					System.err.println(String.format("max param = %.2f min param = %.2f; max grad = %.2f, min grad = %.2f, grad norm = %.2f",
							MathUtil.max(parameters), MathUtil.min(parameters),
							MathUtil.max(gradient), MathUtil.min(gradient),
							Math.sqrt(ArrayMath.twoNormSquared(gradient))
					));
					TIntArrayList[] obs=model.observationProbabilities.getAvailableStates();
					for (int state = 0; state < obs.length; state++) {
						double max= Double.NEGATIVE_INFINITY;
						double min = Double.POSITIVE_INFINITY;
						int maxi=0, mini=0;
						for (int word = 0; word < obs[state].size(); word++) {
							double v = model.observationProbabilities.getCounts(state, obs[state].get(word));
							if (v > max){
								max = v;
								maxi = word;
							}
							if (v < min){
								min = v;
								mini = word;
							}
						}
						System.err.println(String.format(
								" state = %3d  min=%f  max=%f (%s)  (minword = %s)" 
								, state,min,max,model.corpus.getWordStrings(new int[]{obs[state].get(maxi)})[0]
								, model.corpus.getWordStrings(new int[]{obs[state].get(mini)})[0]));
						value = Double.POSITIVE_INFINITY;
					}
					break;
				}
				stats.endInference(this);
				value -= sd.getLogLikelihood();
				sd.clearCaches();
				sd.clearPosteriors();

			}
		}
		// FIXME: I don't like having to compute the value separately from the gradient
		value +=  1/gaussianPriorVariance*ArrayMath.twoNormSquared(parameters);
		// update the empirical counts so we can compute the value and gradient. 
		initTrainer.setCountsAndParameters(hmmcounts.initialCounts, parameters, initOffset);
		transitionTrainer.setCountsAndParameters(hmmcounts.transitionCounts, parameters, transitionOffset);
		observationTrainer.setCountsAndParameters(hmmcounts.observationCounts, parameters, observationOffset);
		//
		initTrainer.getGradient(gradient, initOffset);
		transitionTrainer.getGradient(gradient, transitionOffset);
		observationTrainer.getGradient(gradient, observationOffset);
	}
	
	boolean myDebug=false;
	@Override 
	public void setParameters(double[] parameters){
		super.setParameters(parameters);
//		System.out.println(ArrayPrinting.doubleArrayToString(parameters, null, "new parameters"));
//		myDebug = true;
		updateValueAndGradient();
//		myDebug = false;
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
	int maxNumDirs = 10;
	if (parameters.length > maxNumDirs) {
		System.out.println("big gradient ("+parameters.length+")... subsampling directions");
	}
	Random rand = new Random(0);
	for (int i = 0; i < gradient.length; i++) {
		if (rand.nextDouble()*gradient.length <= maxNumDirs)
			variables.add(i);
	}

	double epsilon = 0.00001;
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
		System.out.println("index param analyt   +   -  feature");
		for (int i = 0; i < minusGrad.length; i++) {
			int gind = variables.get(i);
			String r = null;
			if(gind>=initOffset && gind<transitionOffset) r = " init "+initTrainer.featureToString(gind-initOffset);
			if(gind>=transitionOffset && gind<observationOffset) r = "trans "+transitionTrainer.featureToString(gind-transitionOffset);
			if(gind>=observationOffset) r = "  obs "+observationTrainer.featureToString(gind-observationOffset);
			System.out.print(String.format("%2d  ", gind));
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
