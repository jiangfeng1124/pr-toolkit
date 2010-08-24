package postagging.learning.stats.decodeStats;

import gnu.trove.TIntDoubleHashMap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import data.Corpus;
import decoderStats.AbstractDecoderStats;

import model.AbstractSentenceDist;
import model.chain.hmm.HMM;
import model.chain.hmm.HMMDirectGradientObjective;
import model.chain.hmm.HMMSentenceDist;
import model.chain.hmm.directGradientStats.MultinomialMaxEntDirectTrainerStats;
import optimization.gradientBasedMethods.Objective;
import optimization.gradientBasedMethods.Optimizer;
import optimization.gradientBasedMethods.stats.OptimizerStats;
import postagging.data.PosCorpus;
import postagging.programs.RunModel;
import util.InputOutput;

/**
 * 
 * @author graca
 *
 */
public class DecodeWordTypeL1LMax extends AbstractDecoderStats{
	
	double minOccurences;
	int nrHiddenStates;
	int printClustersIter=100;
	TIntDoubleHashMap[] maxTable;
	
	PosCorpus c;

    public DecodeWordTypeL1LMax(PosCorpus c, HMM model, int minOccurences) {
    	this.c = c;
    	this.nrHiddenStates = model.getNrRealStates();
		this.minOccurences = minOccurences;
		maxTable = new TIntDoubleHashMap[nrHiddenStates];
		for (int i = 0; i < nrHiddenStates; i++) {
			maxTable[i]=new TIntDoubleHashMap();
		}
    }
	
    @Override
    public String getPrefix() {
            return "WTL1LMax::";
    }
    
    /**
     * Before starting inference clear all variables
     */
    @Override
    public void beforeInference(HMM model){
    		for (int i = 0; i < nrHiddenStates; i++) {
			maxTable[i].clear();
		}
	}
	
	
	/**
	 * Collect particular values for this iteration
	 */
    @Override
	public void afterSentenceInference(HMM model, AbstractSentenceDist sd){
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
    	sb.append("\n<20:" + l1lmax[0]+"\t");
    	sb.append("<40:" + l1lmax[1]+"\t");
    	sb.append("<80:" + l1lmax[2]+"\t");
    	sb.append("<150:" + l1lmax[3]+"\t");
    	sb.append(">150: " + l1lmax[4]);
    	return sb.toString();
    }

	@Override
	public void endInference(HMM model) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String collectFinalStats(HMM model) {
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
		return "Total L1LMax " + totalL1LMax + " avg " + (totalL1LMax/totalConstrainedWords) 
	+ l1lMaxPerWordOccurences(c);
	}
}
