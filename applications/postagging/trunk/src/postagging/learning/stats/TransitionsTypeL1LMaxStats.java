package postagging.learning.stats;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import data.Corpus;
import postagging.data.PosCorpus;
import postagging.model.PosHMM;
import util.InputOutput;
import util.Printing;
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
public class TransitionsTypeL1LMaxStats extends TrainStats{

		int printEvery;
		int nrHiddenStates;
		int printClustersIter=100;
		String outputDir;
		//L1LMax for transitions previous state next state
		double[][] maxTable;
		
		//sum for transitions previous state next state
		double[][] sumTable;
		
		PosCorpus c;
		String[] allTags;
        public TransitionsTypeL1LMaxStats(PosCorpus c, HMM model, String printEvery, String outputDir, int printClustersIter) {
        	this.c = c;
        	this.nrHiddenStates = model.getNrRealStates();
			this.printEvery = Integer.parseInt(printEvery);
			maxTable = new double[nrHiddenStates][nrHiddenStates];
			sumTable = new double[nrHiddenStates][nrHiddenStates];
			if(outputDir!=null){
				this.outputDir = outputDir;
				util.FileSystem.createDir(outputDir);
			}
			this.printClustersIter = printClustersIter;
			String[] tagNames = c.getAllTagsStrings();
			//Add final tag state
			allTags = new String[tagNames.length+1];
			System.arraycopy(tagNames, 0, allTags, 0, tagNames.length);
			allTags[tagNames.length]="last";
        }
        
        //Clears the counts tables
        public void eStepStart(AbstractModel model,EM em){   
        	if(em.getCurrentIterationNumber() % printEvery == 0){
        		for (int i = 0; i < nrHiddenStates; i++) {
        			java.util.Arrays.fill(maxTable[i],0);
        			java.util.Arrays.fill(sumTable[i],0);
        		}
        	}
        }
        
        //Change the maxes for this particular sentence
        public void eStepSentenceEnd(AbstractModel model, EM em,AbstractSentenceDist sd){
        	if(em.getCurrentIterationNumber() % printEvery == 0){
        		HMMSentenceDist dist = (HMMSentenceDist)sd;
        		for (int pos = 0; pos < dist.getNumberOfPositions()-1; pos++) {
						for (int prevState = 0; prevState < nrHiddenStates; prevState++) {
							for (int nextState = 0; nextState < nrHiddenStates; nextState++) {
								double prob = dist.getTransitionPosterior(pos, prevState, nextState);
								if(maxTable[prevState][nextState] < prob){
									maxTable[prevState][nextState] = prob;
								}
								sumTable[prevState][nextState] += prob;
							}
						}
					}
				}
        }
        
        public String printEndEStep(AbstractModel model,EM em) {
        	if(em.getCurrentIterationNumber() % printEvery == 0){
            	double totals[] = new double[nrHiddenStates];
            	double totalL1LMax=0;
            	for (int i = 0; i < nrHiddenStates; i++) {		
            		for (int j = 0; j < nrHiddenStates; j++) {
            			totals[i] += maxTable[i][j];
            			totalL1LMax +=maxTable[i][j];
					}
            		totals[i]/=nrHiddenStates;
				}
            	
            	if(em.getCurrentIterationNumber() % printClustersIter == 0 && outputDir!= null){
            		PrintStream outputMax, outputSum;
					try {
						outputMax = InputOutput.openWriter(outputDir+"/transl1lmax-iter."+em.getCurrentIterationNumber());
						outputSum = InputOutput.openWriter(outputDir+"/transSum-iter."+em.getCurrentIterationNumber());
						outputMax.println((Printing.doubleArrayToString(maxTable, null, null, "Maxes table")));
						outputSum.println((Printing.doubleArrayToString(sumTable, null, null, "Sum table")));
						outputMax.close();
						outputSum.close();
					} catch (FileNotFoundException e) {
						System.out.println("Unable to print transition l1LmaxValues");
						e.printStackTrace();
					} catch (UnsupportedEncodingException e) {
						System.out.println("Unable to print transition l1LmaxValues");
						e.printStackTrace();
					} catch (IOException e) {
						System.out.println("Unable to print transition l1LmaxValues");
						e.printStackTrace();
					}
            		
            	}
            	
            	return "Total L1LMax " + totalL1LMax + " avg " + totalL1LMax/nrHiddenStates;
        	}else return "";
        }
        
     
        
        @Override
        public String getPrefix() {
                return "TransL1LMax::";
        }
        
       
        
        
    
}