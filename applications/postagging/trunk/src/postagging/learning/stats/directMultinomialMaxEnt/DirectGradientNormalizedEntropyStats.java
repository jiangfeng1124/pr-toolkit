package postagging.learning.stats.directMultinomialMaxEnt;

import model.AbstractSentenceDist;
import model.chain.hmm.HMMDirectGradientObjective;
import model.chain.hmm.HMMSentenceDist;
import model.chain.hmm.directGradientStats.MultinomialMaxEntDirectTrainerStats;
import optimization.gradientBasedMethods.Objective;
import optimization.gradientBasedMethods.Optimizer;

/**
 * 
 * @author graca
 *
 */
public class DirectGradientNormalizedEntropyStats extends MultinomialMaxEntDirectTrainerStats{
	
	int printEvery;
	double totalEntropy = 0;
    public DirectGradientNormalizedEntropyStats(int printEvery) {
		this.printEvery = printEvery;
    }
	
    @Override
    public String getPrefix() {
            return "NormEntropy::";
    }
    
    /**
     * Before starting inference clear all variables
     */
    public void beforeInference(HMMDirectGradientObjective model){
    		if(getIterationNumber() % printEvery == 0){
    			totalEntropy = 0;
    		}
	}
	
	
	/**
	 * Collect particular values for this iteration
	 */
	public void afterSentenceInference(HMMDirectGradientObjective model, AbstractSentenceDist sd){
		if(getIterationNumber() % printEvery == 0){
  			HMMSentenceDist hmmsd = (HMMSentenceDist) sd;
  			double sentenceEntropy = 0;
  			int nrPositions = hmmsd.getNumberOfPositions();
  			int nrStates = hmmsd.getNumberOfHiddenStates();
			for(int pos = 0; pos < nrPositions; pos++){
				for(int hs = 0; hs < nrStates; hs++){
					double prob = hmmsd.getStatePosterior(pos, hs);
					if(prob != 0){
						sentenceEntropy += prob*Math.log(prob);	
					}
				}
			}		
			totalEntropy += -sentenceEntropy/(nrPositions*Math.log(nrStates));
    		}
	}
    
    /**
     * At the end of an optimization print the results
     */
	public String iterationOutputString(Optimizer optimizer, Objective objective){
		if(getIterationNumber() % printEvery == 0){
			//Divide entropy by number of sentences
			totalEntropy /= ((HMMDirectGradientObjective)objective).model.corpus.getNrOfTrainingSentences();
			return "NormEntropy: " + totalEntropy;
    		}
		return "";
	}
	
}
