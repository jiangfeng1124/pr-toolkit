package postagging.learning.stats;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import postagging.data.PosCorpus;
import postagging.model.PosHMM;
import gnu.trove.TIntDoubleHashMap;
import learning.EM;
import learning.stats.TrainStats;
import model.AbstractModel;
import model.AbstractSentenceDist;
import model.chain.hmm.HMM;
import model.chain.hmm.HMMSentenceDist;


/**
 * Computes the L1LMax stats at the end of each iteration
 * @author javg
 *
 */
public class L1LMaxStats extends TrainStats{

		int printEvery;
		double minOccurences;
		int nrHiddenStates;
		int printClustersIter=100;
		String outputDir;
		//For each hidden state (fixed number) contains
		// the max for each wordId
		TIntDoubleHashMap[] maxTable;
		
		PosCorpus c;
	
        public L1LMaxStats(PosCorpus c, HMM model, String printEvery, int minOccurences, String outputDir, int printClustersIter) {
        	this.c = c;
        	this.nrHiddenStates = model.getNrRealStates();
			this.minOccurences = minOccurences;
			this.printEvery = Integer.parseInt(printEvery);
			maxTable = new TIntDoubleHashMap[nrHiddenStates];
			for (int i = 0; i < nrHiddenStates; i++) {
				maxTable[i]=new TIntDoubleHashMap();
			}
			if(outputDir!=null){
				this.outputDir = outputDir;
				util.FileSystem.createDir(outputDir);
			}
			this.printClustersIter = printClustersIter;
        }
        
        //Clears the counts tables
        public void eStepStart(AbstractModel model,EM em){   
        	if(em.getCurrentIterationNumber() % printEvery == 0){
        		for (int i = 0; i < nrHiddenStates; i++) {
        			maxTable[i].clear();
        		}
        	}
        }
        
        //Change the maxes for this particular sentence
        public void eStepSentenceEnd(AbstractModel model, EM em,AbstractSentenceDist sd){
        	if(em.getCurrentIterationNumber() % printEvery == 0){
        		HMMSentenceDist dist = (HMMSentenceDist)sd;
        		int[] words = dist.instance.words;
        		for (int i = 0; i < words.length; i++) {
					int wordId = words[i];
					if(c.getWordTypeCounts(wordId) > minOccurences){
						for (int hiddenState = 0; hiddenState < nrHiddenStates; hiddenState++) {
							double prob = dist.getStatePosterior(i, hiddenState);
							if(maxTable[hiddenState].contains(wordId)){
								if(maxTable[hiddenState].get(wordId) < prob){
									maxTable[hiddenState].put(wordId,prob);
								}
							}else{
								maxTable[hiddenState].put(wordId, prob);
							}
						}
					}
				}
        		
        	}
        }
        
        public String printEndEStep(AbstractModel model,EM em) {
        	if(em.getCurrentIterationNumber() % printEvery == 0){
            	double totalL1LMax =0;
            	double totalConstrainedWords = 0;
            	
            	int[] keys = maxTable[0].keys();
            	double totals[] = new double[keys.length];
            	for (int i = 0; i < keys.length; i++) {
            		totalConstrainedWords++;
            		for (int j = 0; j < nrHiddenStates; j++) {
            			totalL1LMax += maxTable[j].get(keys[i]);
            			totals[i]+=maxTable[j].get(keys[i]);
					}
				}
            	
            	if(em.getCurrentIterationNumber() % printClustersIter == 0 && outputDir!= null){
            		PrintStream output;
					try {
						output = new PrintStream(outputDir+"/l1lmax-iter."+em.getCurrentIterationNumber());
						for (int i = 0; i < keys.length; i++){
		            		output.println(c.wordAlphabet.index2feat.get(keys[i])+ " "+totals[i]);
		            		}
		            		output.close();
					} catch (FileNotFoundException e) {
						System.out.println("Unable to print l1LmaxValues per word");
						e.printStackTrace();
					}
            		
            	}
            	
            	return "Total L1LMax " + totalL1LMax + " avg " + (totalL1LMax/totalConstrainedWords);
        	}else return "";
        }
        
        
        
        @Override
        public String getPrefix() {
                return "L1LMax::";
        }
        
       
        
        
    
}