package postagging.learning.stats.directMultinomialMaxEnt;

import java.util.ArrayList;

import model.chain.hmm.HMMDirectGradientObjective;
import model.chain.hmm.directGradientStats.MultinomialMaxEntDirectTrainerStats;
import model.distribution.trainer.ObservationMultinomialFeatureFunction;
import optimization.gradientBasedMethods.Objective;
import optimization.gradientBasedMethods.Optimizer;
import util.ArrayMath;
import util.ArrayPrinting;
import util.Printing;

/**
 * 
 * @author graca
 * Collects optimization stats
 */
public class DirectGradientFeatureOptimizationStats extends MultinomialMaxEntDirectTrainerStats{
	
	public String getPrefix(){
        return "FEATOPT::";
	}
	int nrStates;
	ObservationMultinomialFeatureFunction features;
	HMMDirectGradientObjective model;
	public DirectGradientFeatureOptimizationStats(ObservationMultinomialFeatureFunction features, 
			int nrHiddenStates,
			HMMDirectGradientObjective model) {
		this.model = model;
		this.features = features;
		this.nrStates = nrHiddenStates;
	}
	
	public double breakArrayPerFeatures(String prefix, double[] globalArray,int nrStates){
		int[] positions = features.getFeaturesByPrefix(prefix);
		int nrPositions = positions.length;
		double total = 0;
		int nrObsFeatures = features.nrFeatures();
		for(int i  =0; i < nrPositions; i++){
			for(int j = 0; j <nrStates; j++){
				double value = globalArray[positions[i]+j*nrObsFeatures+model.observationOffset];
				total += value*value;
			}
		}
		totalValue +=total;
		return total;
	}
	
	double totalValue;
	public String getValues(double[] vector, int nrStates){
		totalValue = 0;
		StringBuffer res = new StringBuffer();
		ArrayList<String> featNames = features.featuresPrefix;
		for(String pref: featNames){	
			res.append(pref+" "+breakArrayPerFeatures(pref, vector,  nrStates)+" ");
		}
		
		//Add transition features:
		double trans = 0;
		for(int i = 0; i < model.observationOffset; i++){
			double value =vector[i]; 
			trans+=value*value;
		}
		totalValue+=trans;
		res.append(" trans "+  trans + " total " + Math.sqrt(totalValue));
		return res.toString();
	}
	
	public String iterationOutputString(Optimizer optimizer, Objective objective){
		System.out.println("Number of parameters:" + objective.getParameters().length);
		StringBuffer res = new StringBuffer();
		res.append("Weights: "+ getValues(objective.getParameters(),nrStates).toString()+"\n");
		res.append("Grad: "+ getValues(objective.getGradient(),nrStates).toString()+"\n");
		return res.toString();
	}
}
	
