package postagging.learning.stats.directMultinomialMaxEnt;

import java.util.ArrayList;

import model.chain.hmm.directGradientStats.MultinomialMaxEntDirectTrainerStats;
import model.distribution.trainer.ObservationMultinomialFeatureFunction;
import optimization.gradientBasedMethods.Objective;
import optimization.gradientBasedMethods.Optimizer;
import util.ArrayMath;
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
	
	ObservationMultinomialFeatureFunction features;
	public DirectGradientFeatureOptimizationStats(ObservationMultinomialFeatureFunction features) {
		this.features = features;
	}
	
	public double breakArrayPerFeatures(String prefix, double[] globalArray){
		int[] positions = features.getFeaturesByPrefix(prefix);
		int nrPositions = positions.length;
		double total = 0;
		for(int i  =0; i < nrPositions; i++){
			double value = globalArray[positions[i]];
			total += value*value;
		}
		return total;
	}
	
	public String getValues(double[] vector){
		StringBuffer res = new StringBuffer();
		ArrayList<String> featNames = features.featuresPrefix;
		for(String pref: featNames){	
			res.append(pref+" "+breakArrayPerFeatures(pref, vector)+" ");
		}
		return res.toString();
	}
	
	public String iterationOutputString(Optimizer optimizer, Objective objective){
		StringBuffer res = new StringBuffer();
		res.append("Weights: "+ getValues(objective.getParameters()).toString()+"\n");
		res.append("Grad: "+ getValues(objective.getGradient()).toString()+"\n");
		return res.toString();
	}
}
	
