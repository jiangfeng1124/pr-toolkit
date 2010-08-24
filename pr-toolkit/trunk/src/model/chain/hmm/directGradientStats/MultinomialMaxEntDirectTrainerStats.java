package model.chain.hmm.directGradientStats;

import model.AbstractSentenceDist;
import model.chain.hmm.HMMDirectGradientObjective;
import optimization.gradientBasedMethods.Objective;
import optimization.gradientBasedMethods.Optimizer;
import optimization.gradientBasedMethods.stats.AbstractOptimizerStats;

/**
 * Stats framework for direct gradinet methods.
 * Keeps the functionality of OptimizerStats but allows the addition 
 * of specific classes.
 * @author graca
 *
 */
public abstract class MultinomialMaxEntDirectTrainerStats extends AbstractOptimizerStats{

	
	
	
	public abstract String getPrefix();
	
	static int iter = 0;
	
	public void reset(){
		super.reset();
		iter  =0;
	}
	
	public void beforeInference(HMMDirectGradientObjective model){
		
	}
	
	public void beforeSentenceInference(HMMDirectGradientObjective model, AbstractSentenceDist sd){
		
	}
	
	public void afterSentenceInference(HMMDirectGradientObjective model, AbstractSentenceDist sd){
		
	}
	
	public void endInference(HMMDirectGradientObjective model){
		
	}

	public int getIterationNumber(){
		return iter;
	}
	public void collectIterationStats(Optimizer optimizer, Objective objective){
		super.collectIterationStats(optimizer, objective);
		System.out.println(iterationOutputString(optimizer, objective));
		iter++;
	}

	public abstract String iterationOutputString(Optimizer optimizer, Objective objective);

}
