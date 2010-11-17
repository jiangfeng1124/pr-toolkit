package model.distribution.trainer;

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
			if(sum <= 0){
				for(int valueIndex = 0; valueIndex < possibleValues.size(); valueIndex++){
					int value = possibleValues.getQuick(valueIndex);
					paramsTable.setCounts(i, value, 0);
				}
			}
			else if( Double.isInfinite(sum) || Double.isNaN(sum)){
				System.out.println("Max Entropy normalization failed - sum: " + sum);
				System.out.println(probs.toString());
				throw new RuntimeException();
			}else{
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
			}
		}		
		System.out.println(printOptimizationStats(optimizationStats));
		return paramsTable;
	}	

	
	public String getOptimizationStats(ArrayList<OptimizerStats> stats){
		StringBuilder res = new StringBuilder();
		res.append("MEO-State \t Result \t Time(s) \t Iter \t obj \t" +
		" gradNorm \t upd " +
		"\t weightN \t steps \n");
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
					Printing.prettyPrint(os.weightsNorm, "0.0000", 6)
					+"\t");
			res.append("("+steps.toString()+")" 
					+"\n");	
		}
		return res.toString();
	}	

	
	
	public String getFeatureOptimizationStats(ArrayList<OptimizerStats> stats){
		StringBuilder res = new StringBuilder();
		ObservationMultinomialFeatureFunction fxy = (ObservationMultinomialFeatureFunction) classifiers.get(0).fxy;
		ArrayList<String> featNames = fxy.featuresPrefix;
		res.append("MEO-State \t ");
		for (int i = 0; i < featNames.size(); i++) {
			res.append(featNames.get(i) +"\t");

		}
		res.append("\n");
		double[] total = new double[featNames.size()];
		for (int i = 0; i < stats.size(); i++) {
			FeatureSplitOptimizerStats os = (FeatureSplitOptimizerStats) stats.get(i);
			res.append("GRAD::MEO-state-"+i+"\t");
			for (int j = 0; j < featNames.size(); j++) {
				res.append(os.gradPerFeat[j] +"\t");
				total[j]+=os.gradPerFeat[j];
			}
			res.append("\n");
		}
		res.append("GRAD::TOTAL::"+"\t");
		for (int j = 0; j < featNames.size(); j++) {
			res.append(total[j] +"\t");

		}
		res.append("\n");	
		total = new double[featNames.size()];
		for (int i = 0; i < stats.size(); i++) {
			FeatureSplitOptimizerStats os = (FeatureSplitOptimizerStats) stats.get(i);
			res.append("WEIGTHS::MEO-state-"+i+"\t");
			for (int j = 0; j < featNames.size(); j++) {
				res.append(os.weightsPerFeat[j] +"\t");
				total[j]+=os.weightsPerFeat[j];
			}
			res.append("\n");
		}
		res.append("WEIGHTS::TOTAL::"+"\t");
		for (int j = 0; j < featNames.size(); j++) {
			res.append(total[j] +"\t");

		}
		res.append("\n");		

		return res.toString();
	}
	
	public String printOptimizationStats(ArrayList<OptimizerStats> stats){
		StringBuilder res = new StringBuilder();
		if(stats.get(0) instanceof FeatureSplitOptimizerStats){
			res.append(getOptimizationStats(stats));
			res.append(getFeatureOptimizationStats(stats));
		}else{
			res.append(getOptimizationStats(stats));
		}
		String out = res.toString();
		if(this.classifiers.size() == 1){
			out = "INIT::"+out.replace("\n", "\nINIT::");
		}else if(this.classifiers.get(0).fxy instanceof ObservationMultinomialFeatureFunction){
			out = "OBS::"+out.replace("\n", "\nOBS::");
		}else{
			out = "TRANS::"+out.replace("\n", "\nTRANS::");
		}
		return out;
	}
		
}
