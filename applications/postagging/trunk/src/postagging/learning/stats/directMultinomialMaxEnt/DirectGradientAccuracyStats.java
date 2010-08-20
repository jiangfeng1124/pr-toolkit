package postagging.learning.stats.directMultinomialMaxEnt;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

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
 *
 */
public class DirectGradientAccuracyStats extends MultinomialMaxEntDirectTrainerStats{
	
	int printEvery;

	
	public DirectGradientAccuracyStats(int printEvery){
		this.printEvery = printEvery;
	}
	
	

	@Override
    public String getPrefix() {
            return "ACC::";
    }

	@Override
	public String iterationOutputString(Optimizer optimizer, Objective objective) {
		if(getIterationNumber()%printEvery != 0){
			return "";
		}
		HMMDirectGradientObjective obs = (HMMDirectGradientObjective) objective;
		String result = "";
		try {
			result =  RunModel.testModel((HMM) obs.model, obs.model.corpus.testInstances.get(0),"testCorpus",false,"");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
}
