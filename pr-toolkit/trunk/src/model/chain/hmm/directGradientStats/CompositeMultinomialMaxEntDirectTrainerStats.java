package model.chain.hmm.directGradientStats;

import java.util.ArrayList;

import model.AbstractSentenceDist;
import model.chain.hmm.HMMDirectGradientObjective;

import optimization.gradientBasedMethods.Objective;
import optimization.gradientBasedMethods.Optimizer;

/**
 * Stats framework for direct gradinet methods.
 * Keeps the functionality of OptimizerStats but allows the addition 
 * of specific classes.
 * @author graca
 *
 */
public class CompositeMultinomialMaxEntDirectTrainerStats extends MultinomialMaxEntDirectTrainerStats{

	ArrayList<MultinomialMaxEntDirectTrainerStats> statsList;
	
	
	public String getPrefix(){
		return "";
	}
	
	public CompositeMultinomialMaxEntDirectTrainerStats(){
		statsList = new ArrayList<MultinomialMaxEntDirectTrainerStats>();
	}

	public void add(MultinomialMaxEntDirectTrainerStats stats){
		statsList.add(stats);
	}
		
	public void reset(){
		super.reset();
		for(MultinomialMaxEntDirectTrainerStats stats: statsList){
			stats.reset();
		}
	}

	public void collectInitStats(Optimizer optimizer, Objective objective){
		super.collectInitStats(optimizer, objective);
		for(MultinomialMaxEntDirectTrainerStats stats: statsList){
			stats.collectInitStats(optimizer, objective);
		}
	}
	
	public void collectIterationStats(Optimizer optimizer, Objective objective){
		super.collectIterationStats(optimizer, objective);
	}
	
	public String iterationOutputString(Optimizer optimizer, Objective objective){
		StringBuffer sb = new StringBuffer();
		for(MultinomialMaxEntDirectTrainerStats stats: statsList){			
			String s = stats.iterationOutputString(optimizer, objective);
			if(s!=""){
				//Replace ending new lines by the corresponding prefix
				s=  s.replace("\n", "\nIter:"+getIterationNumber()+"::"+stats.getPrefix());
				//Add iterationd and prefix to the begining of file
				sb.append("Iter:"+getIterationNumber()+"::"+stats.getPrefix()+s+"\n");
			}
		}
		return sb.toString();
	}
	
	
	public void collectFinalStats(Optimizer optimizer, Objective objective, boolean success){
		super.collectFinalStats(optimizer, objective, success);
		for(MultinomialMaxEntDirectTrainerStats stats: statsList){
			stats.collectFinalStats(optimizer, objective, success);
		}
	}

	public void beforeInference(HMMDirectGradientObjective model){
		for(MultinomialMaxEntDirectTrainerStats stats: statsList){
			stats.beforeInference(model);
		}
	}
	
	public void beforeSentenceInference(HMMDirectGradientObjective model, AbstractSentenceDist sd){
		for(MultinomialMaxEntDirectTrainerStats stats: statsList){
			stats.beforeSentenceInference(model,sd);
		}
	}
	
	public void afterSentenceInference(HMMDirectGradientObjective model, AbstractSentenceDist sd){
		for(MultinomialMaxEntDirectTrainerStats stats: statsList){
			stats.afterSentenceInference(model,sd);
		}
	}
	
	public void endInference(HMMDirectGradientObjective model){
		for(MultinomialMaxEntDirectTrainerStats stats: statsList){
			stats.endInference(model);
		}
	}

}
