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
public class DirectGradientAccuracyStats extends OptimizerStats{
	
	int printEvery;
	int currentCall = 0;
	
	public DirectGradientAccuracyStats(int printEvery){
		this.printEvery = printEvery;
	}
	
	public void collectIterationStats(Optimizer optimizer, Objective objective){
		if(currentCall%printEvery != 0){
			currentCall = 0;
			return;
		}
		currentCall++;
		HMMDirectGradientObjective obs = (HMMDirectGradientObjective) objective;
		String result;
		try {
			result = RunModel.testModel((HMM) obs.model, obs.model.corpus.testInstances.get(0),"testCorpus",false,"");
			System.out.println(result);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
