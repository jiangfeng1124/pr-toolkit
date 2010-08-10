package model.distribution.trainer;

import gnu.trove.TDoubleArrayList;
import gnu.trove.TIntArrayList;

import java.util.ArrayList;

import model.distribution.AbstractMultinomial;
import optimization.gradientBasedMethods.Optimizer;
import optimization.gradientBasedMethods.stats.OptimizerStats;
import optimization.gradientBasedMethods.stats.FeatureSplitOptimizerStats;
import optimization.linesearch.LineSearchMethod;
import optimization.stopCriteria.StopingCriteria;

import util.Printing;
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
	public double gaussianPriorVariance;
	
	boolean useProgressiveOptimization = false;
	int numberOfUpdates = 0;
	
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
			ArrayList<StopingCriteria> stop, boolean warmStart,
			boolean useProgressiveOptimization){
		classifiers = new ArrayList<MaxEntClassifier>();	
		for (int i = 0; i < nrHiddenStates; i++) {
			MaxEntClassifier me = new MaxEntClassifier(fxys.get(i), 
					lineSearch.get(i),
					optimizer.get(i), 
					stats.get(i), 
					stop.get(i), 
					gaussianPriorVariance, 
					warmStart);
			this.useProgressiveOptimization = useProgressiveOptimization;
			classifiers.add(me);
		}
	}
		
	
	public AbstractMultinomial update(AbstractMultinomial counts) {
		AbstractMultinomial params = counts.clone();
		return update(counts,params);
	}

	public void updateClassifiersConvervenge(){
		if(numberOfUpdates < 10){
			for (int i = 0; i < classifiers.size(); i++) {
				classifiers.get(i).optimizer.setMaxIterations(1);
			}
		}else if(numberOfUpdates < 50){
			for (int i = 0; i < classifiers.size(); i++) {
				classifiers.get(i).optimizer.setMaxIterations(10);
			}
		}else{
			for (int i = 0; i < classifiers.size(); i++) {
				classifiers.get(i).optimizer.setMaxIterations(1000);
			}
		}
	}
	
	/**
	* Trains the ME classifier base on the count table, and updates the parameters table.
	*/
	public AbstractMultinomial update(AbstractMultinomial countsTable,
			AbstractMultinomial paramsTable) {
		ArrayList<OptimizerStats> optimizationStats = new ArrayList<OptimizerStats>();
		TDoubleArrayList kls = new TDoubleArrayList();
		OptimizerStats stats;
		numberOfUpdates++;
		if(useProgressiveOptimization){
			updateClassifiersConvervenge();
		}
		for (int i = 0; i < classifiers.size(); i++) {
			MaxEntClassifier me = classifiers.get(i);
			stats = me.batchTrain(countsTable, i);
			optimizationStats.add(stats);
			TIntArrayList possibleValues = countsTable.getAvailableStates(i);
			SparseVector scores = me.computeScores(i, possibleValues,me.initialParameters);
			double max = scores.max();
			scores.plusEquals(-max);
			SparseVector probs = scores.expEntries();
			double sum = probs.sum();
			if(sum <= 0 || Double.isInfinite(sum) || Double.isNaN(sum)){
				System.out.println("Max Entropy normalization failed - sum: " + sum);
				System.out.println(probs.toString());
				throw new RuntimeException();
			}
			for(int valueIndex = 0; valueIndex < possibleValues.size(); valueIndex++){
				int value = possibleValues.getQuick(valueIndex);
				double prob = probs.getValue(value)/sum;
				if(prob < 0 || Double.isInfinite(prob) || Double.isNaN(prob)){
					System.out.println("Max Entropy normalization prob failed - prob: " 
							+ prob + ":" + probs.getValue(value) + ":" + sum);
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
				if(!(Double.isInfinite(prob) || Double.isInfinite(prob) || Double.isInfinite(logP) || Double.isInfinite(logP) )){
					kl+= prob*logP;
				}
			}
			kls.add(kl);
		}
//		//Debug code
//		double[] individiualFeatures= new double[classifiers.size()];
//		double[] coarceFeatures = new double[classifiers.size()];
//		ObservationMultinomialFeatureFunction ff = (ObservationMultinomialFeatureFunction) classifiers.get(0).fxy;
//		for(int i  =0; i < ff.nrFeatures(); i++){
//			for(int j = 0; j < classifiers.size(); j++){
//				double weight = classifiers.get(j).initialParameters[i];
//				weight = weight*weight;
//				if(((String) ff.al.index2feat.get(i)).startsWith("word")){
//					individiualFeatures[j] += weight;
//				}else{
//					coarceFeatures[j] += weight;
//				}
//			}	
//		}
//		for(int j = 0; j < classifiers.size(); j++){
//			individiualFeatures[j] = Math.sqrt(individiualFeatures[j]);
//			coarceFeatures[j] = Math.sqrt(coarceFeatures[j]);
//		}
		
		System.out.println(printOptimizationStats(optimizationStats,kls));
		return paramsTable;
	}	

	public String printOptimizationStats(ArrayList<OptimizerStats> stats,
			TDoubleArrayList kl){
		StringBuilder res = new StringBuilder();
		if(stats.get(0) instanceof FeatureSplitOptimizerStats){
			res.append("MEO-State \t Result \t Time(s) \t Iter \t obj \t " +
					"gradNorm \t upd " +
			"\t kl \t weightW  \t " +
			"individualW \t coarceW \t individualG \t coarceG \t" +
			"steps \n");
		}else{
			res.append("MEO-State \t Result \t Time(s) \t Iter \t obj \t" +
					" gradNorm \t upd " +
			"\t kl \t weightN \t steps \n");
		}
		for (int i = 0; i < stats.size(); i++) {
			OptimizerStats os = stats.get(i);
			StringBuilder steps = new StringBuilder();
			for(Double step : os.steps){
				steps.append(Printing.prettyPrint(step, "0.00E00", 6)+":");
			}
			int nrIterations = os.iterations.size();
			res.append("MEO-state-"+i+"\t"+os.succeed +"\t"+  
					Printing.prettyPrint(os.totalTime/1000, "0.00", 3) 
					+ "\t"+ nrIterations + "\t" + 
					Printing.prettyPrint(os.value.get(nrIterations - 1), "0.00E00", 6)+ 
					"\t"+					
					Printing.prettyPrint(os.gradientNorms.get(nrIterations -1), "0.000E00", 8)+
					"\t" + 
					Printing.prettyPrint(os.paramUpdates, "0000", 4)+
					"\t" + 
					Printing.prettyPrint(kl.get(i), "0.0000", 6)
					+"\t"+
					Printing.prettyPrint(os.weightsNorm, "0.0000", 6)
					+"\t");
			if(os instanceof FeatureSplitOptimizerStats){
				res.append(Printing.prettyPrint(((FeatureSplitOptimizerStats)os).individiualWeightsL2, "0.0000", 6)
					+"\t"+
					Printing.prettyPrint(((FeatureSplitOptimizerStats)os).coarceWeightsL2, "0.0000", 6)
					+"\t"+
					Printing.prettyPrint(((FeatureSplitOptimizerStats)os).individiualGradL2, "0.0000", 6)
							+"\t"+
							Printing.prettyPrint(((FeatureSplitOptimizerStats)os).coarcelGradL2, "0.0000", 6)
							+"\t");
			}
				
				res.append("("+steps.toString()+")" 
					+"\n");
		}
		return res.toString();
	}
	
	
	
	
}
