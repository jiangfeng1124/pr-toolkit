package model.distribution.trainer;

import gnu.trove.TIntArrayList;

import java.util.ArrayList;

import model.distribution.AbstractMultinomial;


import optimization.gradientBasedMethods.Optimizer;
import optimization.gradientBasedMethods.stats.OptimizerStats;
import optimization.linesearch.LineSearchMethod;
import optimization.stopCriteria.StopingCriteria;
import util.SparseVector;

/**
 * Max Ent Classifier for a multinomial. It receives a table representing the observevations in our case
 * this will be the counts collected in the E-Step and returns a new multinomial obtained from the learned parameters.
 * 
 * @author javg
 *
 */
public class MultinomialMaxEntTrainer implements AbstractMultinomialTrainer{
	
	ArrayList<MaxEntClassifier> classifiers;
	
	
	/**
	 * Creates a maxEntClassifier for each hidden state
	 * @param gaussianPriorVariance
	 * @param fxy
	 * @param classifier
	 */
	public MultinomialMaxEntTrainer(int nrHiddenStates,
			double gaussianPriorVariance,
			ArrayList<MultinomialFeatureFunction> fxys,
			ArrayList<LineSearchMethod> lineSearch,
			ArrayList<Optimizer> optimizer,
			ArrayList<OptimizerStats> stats,
			ArrayList<StopingCriteria> stop, boolean warmStart){
		classifiers = new ArrayList<MaxEntClassifier>();	
		for (int i = 0; i < nrHiddenStates; i++) {
			MaxEntClassifier me = new MaxEntClassifier(fxys.get(i), 
					lineSearch.get(i),
					optimizer.get(i), 
					stats.get(i), 
					stop.get(i), 
					gaussianPriorVariance, warmStart);
			classifiers.add(me);
		}
	}
		
	
	public AbstractMultinomial update(AbstractMultinomial counts) {
		AbstractMultinomial params = counts.clone();
		return update(counts,params);
	}

	/**
	* Trains the ME classifier base on the count table, and updates the parameters table.
	*/
	public AbstractMultinomial update(AbstractMultinomial countsTable,
			AbstractMultinomial paramsTable) {
		for (int i = 0; i < classifiers.size(); i++) {
			MaxEntClassifier me = classifiers.get(i);
			me.batchTrain(countsTable, i);
			TIntArrayList possibleValues = countsTable.getAvailableStates(i);
			SparseVector scores = me.computeScores(i, possibleValues,me.initialParameters);
			SparseVector probs = scores.expEntries();
			double sum = probs.sum();
			if(sum <= 0 || Double.isInfinite(sum) || Double.isNaN(sum)){
				System.out.println("Max Entropy normalization failed - sum: " + sum);
				throw new RuntimeException();
			}
			for(int valueIndex = 0; valueIndex < possibleValues.size(); valueIndex++){
				int value = possibleValues.getQuick(valueIndex);
				double prob = probs.getValue(value)/sum;
				if(prob <= 0 || Double.isInfinite(prob) || Double.isNaN(prob)){
					System.out.println("Max Entropy normalization prob failed - prob: " + prob);
					throw new RuntimeException();
				}
				paramsTable.setCounts(i, value, prob);
			}
			double MLsum = countsTable.sum(i);
			double kl = 0;
			for(int valueIndex = 0; valueIndex < possibleValues.size(); valueIndex++){
				int value = possibleValues.getQuick(valueIndex);
				double prob = probs.getValue(value)/sum;
				double mlProb = countsTable.getCounts(i, value)/MLsum;
				double logP = Math.log(prob/mlProb);
//				System.out.println("prob: " + prob + " mlProb " + mlProb + " kl " + prob*logP);
				kl+= prob*logP;
			}
			System.out.println("Kl between ME and ML estimates for state "+i+": " + kl);				
		}
		return paramsTable;
	}	

	
	
	
	
}
