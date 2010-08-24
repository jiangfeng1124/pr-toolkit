package model.distribution.trainer;

import gnu.trove.TDoubleArrayList;
import gnu.trove.TIntArrayList;

import java.util.ArrayList;

import model.distribution.AbstractMultinomial;
import model.distribution.trainer.stats.MultinomialMaxEntFeatureOptStats;
import optimization.gradientBasedMethods.Optimizer;
import optimization.gradientBasedMethods.stats.OptimizerStats;
import optimization.gradientBasedMethods.stats.FeatureSplitOptimizerStats;
import optimization.linesearch.LineSearchMethod;
import optimization.stopCriteria.StopingCriteria;

import util.ArrayMath;
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

	
	public String printIterationOptimizationStats(ArrayList<OptimizerStats> stats){
		StringBuilder res = new StringBuilder();
		for(int i = 0; i < stats.size(); i++){
			if(stats.get(i) instanceof MultinomialMaxEntFeatureOptStats){
				res.append(printIterationOptimizationStat((MultinomialMaxEntFeatureOptStats)stats.get(i),i));
			}else{
				res.append(printIterationOptimizationStat(stats.get(i),i));
			}
		}
		String out = res.toString();
		if(this.classifiers.size() == 1){
			out = out.replace("\n", "\nINIT::");
		}else if(this.classifiers.get(0).fxy instanceof ObservationMultinomialFeatureFunction){
			out = out.replace("\n", "\nOBS::");
		}else{
			out = out.replace("\n", "\nTRANS::");
		}
		return out;
	}
	
	
	public String printIterationOptimizationStat(OptimizerStats stat, int i){
		StringBuilder res = new StringBuilder();
//		
//		res.append(" Iteration "
//				+i
//				+ " updates "
//				+(stat.paramUpdates)
//				+" step "
//				+Printing.prettyPrint(stat.steps.get(i), "0.00E00", 6)
//				+" Params "
//				//+Printing.prettyPrint(ArrayMath.L2Norm(objective.getParameters()), "0.00E00", 6)
//				+Printing.prettyPrint(0, "0.00E00", 6)
//				+ " gradientNorm "+ 
//				Printing.prettyPrint(stat.gradientNorms.get(i), "0.00000E00", 10)
//				+ " gradientNormalizedNorm "+ 
//				Printing.prettyPrint(norm/originalNorm, "0.00000E00", 10)
//				+ " value "+ Printing.prettyPrint(stats., "0.000000E00",11));
//		
//		
//		System.out.println("Printing optimization stats");
		return res.toString();
	}
	
	public String printIterationOptimizationStat(MultinomialMaxEntFeatureOptStats stat, int i){
		StringBuilder res = new StringBuilder();
		System.out.println("Printing MaxEnt optimization stats");
		return res.toString();
	}
	
	public String printOptimizationStats(ArrayList<OptimizerStats> stats){
		StringBuilder res = new StringBuilder();
		if(stats.get(0) instanceof FeatureSplitOptimizerStats){
			res.append("MEO-State \t Result \t Time(s) \t Iter \t obj \t " +
					"gradNorm \t upd " +
			"\t weightW  \t " +
			"individualW \t coarceW \t individualG \t coarceG \t" +
			"steps \n");
		}else{
			res.append("MEO-State \t Result \t Time(s) \t Iter \t obj \t" +
					" gradNorm \t upd " +
			"\t weightN \t steps \n");
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
		String out = res.toString();
		if(this.classifiers.size() == 1){
			out = out.replace("\n", "\nINIT::");
		}else if(this.classifiers.get(0).fxy instanceof ObservationMultinomialFeatureFunction){
			out = out.replace("\n", "\nOBS::");
		}else{
			out = out.replace("\n", "\nTRANS::");
		}
		return out;
	}	
}
