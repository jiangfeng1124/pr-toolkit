package model.distribution.trainer;

import gnu.trove.TDoubleArrayList;
import gnu.trove.TIntArrayList;

import java.util.ArrayList;

import model.distribution.AbstractMultinomial;
import optimization.gradientBasedMethods.Optimizer;
import optimization.gradientBasedMethods.stats.OptimizerStats;
import optimization.linesearch.LineSearchMethod;
import optimization.stopCriteria.StopingCriteria;
import optimization.util.StaticTools;
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
		ArrayList<OptimizerStats> optimizationStats = new ArrayList<OptimizerStats>();
		TDoubleArrayList kls = new TDoubleArrayList();
		OptimizerStats stats;
		for (int i = 0; i < classifiers.size(); i++) {
			MaxEntClassifier me = classifiers.get(i);
			stats = me.batchTrain(countsTable, i);
			optimizationStats.add(stats);
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
			kls.add(kl);
		}
		System.out.println(printOptimizationStats(optimizationStats,kls));
		return paramsTable;
	}	

	public String printOptimizationStats(ArrayList<OptimizerStats> stats, TDoubleArrayList kl){
		StringBuilder res = new StringBuilder();
		res.append("Result \t\t Time(s) \t\t Iter \t\t obj \t\t gradNorm \t\t upd \t\t kl \n");
		for (int i = 0; i < stats.size(); i++) {
			OptimizerStats os = stats.get(i);
			int nrIterations = os.iterations.size();
			res.append(os.succeed +"\t\t"+  
					StaticTools.prettyPrint(os.totalTime/1000, "0.00", 3) 
					+ "\t\t"+ nrIterations + "\t" + 
					StaticTools.prettyPrint(os.value.get(nrIterations - 1), "0.00E00", 6)+ 
					"\t\t"+					
					StaticTools.prettyPrint(os.gradientNorms.get(nrIterations -1), "0.000E00", 8)+
					"\t\t" + 
					StaticTools.prettyPrint(os.paramUpdates, "0000", 4)+
					"\t\t" + 
					StaticTools.prettyPrint(kl.get(i), "0.0000", 6)
					+"\n");
		}
		return res.toString();
	}
	
	
	
	
}
