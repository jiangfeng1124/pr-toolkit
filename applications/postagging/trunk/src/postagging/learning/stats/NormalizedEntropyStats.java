package postagging.learning.stats;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import data.Corpus;
import postagging.data.PosCorpus;
import postagging.model.PosHMM;
import util.InputOutput;
import gnu.trove.TIntDoubleHashMap;
import learning.EM;
import learning.stats.TrainStats;
import model.AbstractModel;
import model.AbstractSentenceDist;
import model.chain.hmm.HMM;
import model.chain.hmm.HMMSentenceDist;


/**
 * Computes the normalized entropu asd defined in paper
 * "Optimization with EM and Expectation-Conjugate-Gradient" 
 * as a measure of missing information
 * @author javg
 *
 */
public class NormalizedEntropyStats extends TrainStats{

		int printEvery;
		double totalEntropy;
        
		public NormalizedEntropyStats(int printEvery) {
			this.printEvery = printEvery;
		}
		
        public void eStepStart(AbstractModel model,EM em){   
        		if(em.getCurrentIterationNumber() % printEvery == 0){
        			totalEntropy = 0;
        		}
        }
        

        	public void eStepSentenceEnd(AbstractModel model, EM em,AbstractSentenceDist sd){
        	
        		if(em.getCurrentIterationNumber() % printEvery == 0){ 			
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
        
        public String printEndEStep(AbstractModel model,EM em) {
        		if(em.getCurrentIterationNumber() % printEvery == 0){
        			//Divide entropy by number of sentences
        			totalEntropy /= ((HMM)model).corpus.getNrOfTrainingSentences();
        			return "NormEntropy: " + totalEntropy;
        		}	
        		return "";
        }
        
        @Override
        public String getPrefix() {
                return "NormEntropy::";
        }
        
       
        
        
    
}