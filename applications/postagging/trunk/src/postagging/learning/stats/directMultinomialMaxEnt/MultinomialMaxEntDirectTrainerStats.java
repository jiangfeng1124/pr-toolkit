package postagging.learning.stats.directMultinomialMaxEnt;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import model.chain.hmm.HMM;
import model.chain.hmm.HMMDirectGradientObjective;
import optimization.gradientBasedMethods.Objective;
import optimization.gradientBasedMethods.Optimizer;
import optimization.gradientBasedMethods.stats.OptimizerStats;
import postagging.data.PosCorpus;
import postagging.programs.RunModel;
import util.ArrayMath;
import util.Printing;

/**
 * Stats framework for direct gradinet methods.
 * Keeps the functionality of OptimizerStats but allows the addition 
 * of specific classes.
 * @author graca
 *
 */
public class MultinomialMaxEntDirectTrainerStats extends OptimizerStats{

	ArrayList<OptimizerStats> statsList;
	
	
	public MultinomialMaxEntDirectTrainerStats(){
		statsList = new ArrayList<OptimizerStats>();
	}

	public void add(OptimizerStats stats){
		statsList.add(stats);
	}
	
	
	public void reset(){
		super.reset();
		for(OptimizerStats stats: statsList){
			stats.reset();
		}
	}
	
	
	
	
	
	
	public void collectInitStats(Optimizer optimizer, Objective objective){
		super.collectInitStats(optimizer, objective);
		for(OptimizerStats stats: statsList){
			stats.collectInitStats(optimizer, objective);
		}
	}
	
	public void collectIterationStats(Optimizer optimizer, Objective objective){
		super.collectIterationStats(optimizer, objective);
		for(OptimizerStats stats: statsList){
			stats.collectIterationStats(optimizer, objective);
		}
	}
	
	
	public void collectFinalStats(Optimizer optimizer, Objective objective, boolean success){
		super.collectFinalStats(optimizer, objective, success);
		for(OptimizerStats stats: statsList){
			stats.collectFinalStats(optimizer, objective, success);
		}
	}
	

}
