package model.chain.hmm;

import model.AbstractCountTable;
import model.AbstractSentenceDist;
import model.distribution.trainer.MaxEntClassifier;
import model.distribution.trainer.MultinomialMaxEntDirectGradientTrainer;
import model.distribution.trainer.MultinomialMaxEntTrainer;
import optimization.gradientBasedMethods.Objective;

public class HMMDirectGradientObjective extends Objective {

	AbstractCountTable counts;
	MultinomialMaxEntDirectGradientTrainer initTrainer, transitionTrainer, observationTrainer;
	int initOffset, transitionOffset, observationOffset;
	HMM model;
	AbstractSentenceDist[] sentenceDists;
	int iter = 0;
	double value = Double.NaN;
	
	
	public HMMDirectGradientObjective(HMM hmm){
		model = hmm;
		counts = hmm.getCountTable();
		initOffset = 0;
		HMMCountTable hmmcounts = (HMMCountTable) counts;
		initTrainer = new MultinomialMaxEntDirectGradientTrainer((MultinomialMaxEntTrainer) hmm.initTrainer, hmmcounts.initialCounts);
		transitionOffset = initTrainer.numParams();
		transitionTrainer = new MultinomialMaxEntDirectGradientTrainer((MultinomialMaxEntTrainer) hmm.transitionsTrainer, hmmcounts.transitionCounts);
		observationOffset = transitionOffset+transitionTrainer.numParams();
		observationTrainer = new MultinomialMaxEntDirectGradientTrainer((MultinomialMaxEntTrainer) hmm.observationTrainer, hmmcounts.observationCounts);
		parameters = new double[observationOffset+observationTrainer.numParams()];
		gradient = new double[parameters.length];
		sentenceDists = model.getSentenceDists();
		updateValueAndGradient();
	}
	
	public void updateValueAndGradient(){
		// compute the correct counts.
		counts.clear();
		initTrainer.getMultinomialAtCurrentParams(model.initialProbabilities);
		transitionTrainer.getMultinomialAtCurrentParams(model.transitionProbabilities);
		observationTrainer.getMultinomialAtCurrentParams(model.observationProbabilities);
		System.out.println("\nDirect gradient Iteration " + (iter++));
		System.out.flush();
		//		corpusEStep(counts, sentenceDists, stats);
		System.out.println("Using senteces on training:" + sentenceDists.length);
		for(AbstractSentenceDist sd : sentenceDists){			
			// sentenceEStep(sd, counts, stats);
			sd.initSentenceDist();
			model.computePosteriors(sd);
			sd.clearCaches();
			model.addToCounts(sd,counts);	
			sd.clearPosteriors();
		}
		HMMCountTable hmmcounts = (HMMCountTable) counts;
		initTrainer.setCountsAndParameters(hmmcounts.initialCounts, parameters, initOffset);
		transitionTrainer.setCountsAndParameters(hmmcounts.transitionCounts, parameters, transitionOffset);
		observationTrainer.setCountsAndParameters(hmmcounts.observationCounts, parameters, observationOffset);
		// update value and gradient
		value = initTrainer.getValue() + transitionTrainer.getValue() + observationTrainer.getValue();
		initTrainer.getGradient(gradient, initOffset);
		transitionTrainer.getGradient(gradient, transitionOffset);
		observationTrainer.getGradient(gradient, observationOffset);
	}
	
	@Override 
	public void setParameters(double[] parameters){
		super.setParameters(parameters);
		updateValueAndGradient();
	}
	
	@Override 
	public void setParameter(int i, double v){
		super.setParameter(i, v);
		// FIXME: is this the correct behavior?  Kuzman doesn't know. 
		updateValueAndGradient();
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
