package postagging.learning.stats.directMultinomialMaxEnt;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import model.chain.hmm.HMM;
import model.chain.hmm.HMMDirectGradientObjective;
import optimization.gradientBasedMethods.Objective;
import optimization.gradientBasedMethods.Optimizer;
import optimization.gradientBasedMethods.stats.OptimizerStats;
import postagging.programs.RunModel;

/**
 * 
 * @author graca
 *
 */
public class DirectGradientLikelihoodStats extends OptimizerStats{
	
		
	int iter = 0;
	public void collectIterationStats(Optimizer optimizer, Objective objective){
		iter++;
		System.out.println("Iter-"+iter+"::Objective " + optimizer.getCurrentValue());
	}
	
	public void collectFinalStats(Optimizer optimizer, Objective objective, boolean success){
		iter++;
		System.out.println("Iter-"+iter+"::Objective " + optimizer.getCurrentValue());
	}
}
