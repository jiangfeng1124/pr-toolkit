package postagging.learning.stats.directMultinomialMaxEnt;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import model.AbstractSentenceDist;
import model.chain.hmm.HMM;
import model.chain.hmm.HMMDirectGradientObjective;
import model.chain.hmm.directGradientStats.MultinomialMaxEntDirectTrainerStats;
import optimization.gradientBasedMethods.Objective;
import optimization.gradientBasedMethods.Optimizer;
import optimization.gradientBasedMethods.stats.OptimizerStats;
import postagging.programs.RunModel;

/**
 * 
 * @author graca
 * Collects the likelihood for the entire corpus, just the likelihood no normalization
 */
public class DirectGradientLikelihoodStats extends MultinomialMaxEntDirectTrainerStats{
	
	public String getPrefix(){
        return "LogL::";
	}
	
	double likelihood  =0;
	
	public void beforeInference(HMMDirectGradientObjective model){
		likelihood = 0;
	}

	public void afterSentenceInference(HMMDirectGradientObjective model, AbstractSentenceDist sd){
		likelihood += sd.getLogLikelihood();
	}
	public String iterationOutputString(Optimizer optimizer, Objective objective){
	
		return "Iter-"+getIterationNumber()+"::Likelihood " + likelihood;
	}
	
	
}
