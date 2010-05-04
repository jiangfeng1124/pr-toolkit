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
 * Computes the L1LMax stats at the end of each iteration
 * @author javg
 *
 */
public class WordTypeL1LMaxStats extends TrainStats{

		int printEvery;
		double minOccurences;
		int nrHiddenStates;
		int printClustersIter=100;
		String outputDir;
		//For each hidden state (fixed number) contains
		// the max for each wordId
		TIntDoubleHashMap[] maxTable;
		
		PosCorpus c;
	
        public WordTypeL1LMaxStats(PosCorpus c, HMM model, String printEvery, int minOccurences, String outputDir, int printClustersIter) {
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
						output = InputOutput.openWriter(outputDir+"/wtl1lmax-iter."+em.getCurrentIterationNumber());
						for (int i = 0; i < keys.length; i++){
		            		output.println(c.wordAlphabet.index2feat.get(keys[i])+ " "+totals[i]);
		            		}
		            		output.close();
					} catch (FileNotFoundException e) {
						System.out.println("Unable to print l1LmaxValues per word");
						e.printStackTrace();
					} catch (UnsupportedEncodingException e) {
						System.out.println("Unable to print l1LmaxValues per word");
						e.printStackTrace();
					} catch (IOException e) {
						System.out.println("Unable to print l1LmaxValues per word");
						e.printStackTrace();
					}
            		
            	}
            	
            	return "Total L1LMax " + totalL1LMax + " avg " + (totalL1LMax/totalConstrainedWords) + l1lMaxPerWordOccurences(c);
        	}else return "";
        }
        
        /**
         * Calculates the average l1lmax per number of occurences of words
         * Looking for words that occure between:
         * < 20
         * 20 < x < 40
         * 40 < x < 80
         * 80 < x <  150
         * x > 150
         */
        public String l1lMaxPerWordOccurences(Corpus c){
        	double[] l1lmax = new double[5];
        	int[] numberOfEntries = new int[5];
        	int[] keys = maxTable[0].keys();
        	for (int i = 0; i < keys.length; i++) {
        		for (int j = 0; j < nrHiddenStates; j++) {
        			int wordId = keys[i];
        			int counts = c.getWordTypeCounts(wordId);
        			if(counts < 20){
        				l1lmax[0] +=  maxTable[j].get(wordId);
        				numberOfEntries[0]++;
        			}else if(counts < 40 ){
        				l1lmax[1] +=  maxTable[j].get(wordId);
        				numberOfEntries[1]++;
        			}else if(counts < 80 ){
        				l1lmax[2] +=  maxTable[j].get(wordId);
        				numberOfEntries[2]++;
        			}else if(counts < 150 ){
        				l1lmax[3] +=  maxTable[j].get(wordId);
        				numberOfEntries[3]++;
        			}else{
        				l1lmax[4] +=  maxTable[j].get(wordId);
        				numberOfEntries[4]++;
        			}
				}
			}
        	for(int i = 0; i < l1lmax.length; i++){
        		l1lmax[i]=l1lmax[i]*nrHiddenStates/numberOfEntries[i];
        	}
        	StringBuffer sb = new StringBuffer();
        	sb.append("\nL1LMax word occurences < 20: " + l1lmax[0]+"\n");
        	sb.append("L1LMax word occurences < 40: " + l1lmax[1]+"\n");
        	sb.append("L1LMax word occurences < 80: " + l1lmax[2]+"\n");
        	sb.append("L1LMax word occurences < 150: " + l1lmax[3]+"\n");
        	sb.append("L1LMax word occurences > 150: " + l1lmax[4]);
        	return sb.toString();
        }
        
        @Override
        public String getPrefix() {
                return "WTL1LMax::";
        }
        
       
        
        
    
}