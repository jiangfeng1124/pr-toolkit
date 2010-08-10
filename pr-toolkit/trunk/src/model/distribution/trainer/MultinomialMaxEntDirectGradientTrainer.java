package model.distribution.trainer;

import gnu.trove.TDoubleArrayList;
import gnu.trove.TIntArrayList;

import java.util.ArrayList;

import model.distribution.AbstractMultinomial;
import model.distribution.trainer.MaxEntClassifier.MaxEntMinimizationObjective;
import util.SparseVector;

/**
 * Max Ent Classifier for a multinomial. It receives a table representing the observevations in our case
 * this will be the counts collected in the E-Step and returns a new multinomial obtained from the learned parameters.
 * 
 * @author javg
 *
 */
public class MultinomialMaxEntDirectGradientTrainer implements DirectGradientTrainer{
	
	ArrayList<MaxEntClassifier> classifiers;
	private AbstractMultinomial countsTable; 
	MaxEntMinimizationObjective[] smallObjs;
	private int[] offsets; // what the offsets are inside the parameters arrays. 
	
	int numberOfUpdates = 0;
	int numParams = 0;

	
	/**
	 * Creates a maxEntClassifier for each hidden state
	 * @param gaussianPriorVariance
	 * @param fxy
	 * @param classifier
	 */
	public MultinomialMaxEntDirectGradientTrainer(MultinomialMaxEntTrainer baseTrainer, AbstractMultinomial counts){
		classifiers = baseTrainer.classifiers;
		smallObjs = new MaxEntMinimizationObjective[classifiers.size()];
		numParams = 0;
		offsets = new int[classifiers.size()];
		for (int i = 0; i < classifiers.size(); i++) {
			MaxEntClassifier me = classifiers.get(i);
			smallObjs[i] = me.getObjective(counts, i);
			offsets[i] = numParams;
			numParams += me.fxy.nrFeatures();
		}
	}
	
	public void setCountsAndParameters(AbstractMultinomial counts, double[] parameters, int offset){
		this.countsTable = counts;
		// compute the value and gradient... 
		for (int i = 0; i < classifiers.size(); i++) {
//			MaxEntClassifier me = classifiers.get(i);
			smallObjs[i].setCounts(counts); // objective already knows which variable it is
			int numparamsI = i==classifiers.size()-1 ? numParams - offsets[i] : offsets[i+1]-offsets[i];
			for (int j = 0; j < numparamsI; j++) {
				smallObjs[i].setParameter(j, parameters[offset+offsets[i]+j]);
			}
			smallObjs[i].updateObjectiveAndGradient();
		}
	}
		
	/**
	* Trains the ME classifier base on the count table, and updates the parameters table.
	*/
	public void getMultinomialAtCurrentParams(AbstractMultinomial paramsTable) {
//		ArrayList<OptimizerStats> optimizationStats = new ArrayList<OptimizerStats>();
		TDoubleArrayList kls = new TDoubleArrayList();
//		OptimizerStats stats;
		for (int i = 0; i < classifiers.size(); i++) {
			MaxEntClassifier me = classifiers.get(i);
			TIntArrayList possibleValues = countsTable.getAvailableStates(i);
			SparseVector scores = me.computeScores(i, possibleValues,smallObjs[i].getParameters());
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
		
	}	

	
	public void getGradient(double[] gradient, int offset) {
		for (int i = 0; i < classifiers.size(); i++) {
			double[] g = smallObjs[i].getGradient();
			System.arraycopy(g, 0, gradient, offset+offsets[i], g.length);
		}
	}


	public double getValue() {
		double val = 0;
		for (int i = 0; i < smallObjs.length; i++) {
			val+=smallObjs[i].getValue();
		}
		return val;
	}


	@Override
	public String toString() {
		return "MultinominalMaxEntDirectGradientTrainer";
	}
	
	public int numParams(){
		return numParams;
	}

	
}
