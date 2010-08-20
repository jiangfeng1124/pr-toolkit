package postagging.learning.stats;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import data.Corpus;
import postagging.data.PosCorpus;
import postagging.model.PosHMM;
import util.ArrayMath;
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
		double[][] l2Table;
	
		
		PosCorpus c;
		String[] allTags;
        public TransitionsTypeL1LMaxStats(PosCorpus c, HMM model, String printEvery, String outputDir, int printClustersIter) {
        	this.c = c;
        	this.nrHiddenStates = model.getNrRealStates();
			this.printEvery = Integer.parseInt(printEvery);
			maxTable = new double[nrHiddenStates][nrHiddenStates];
			l2Table = new double[nrHiddenStates][nrHiddenStates];
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
        			java.util.Arrays.fill(l2Table[i],0);
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
								l2Table[prevState][nextState] += prob*prob;
							}
						}
					}
				}
        }
        
        public String printEndEStep(AbstractModel model,EM em) {
        	if(em.getCurrentIterationNumber() % printEvery == 0){
            	double totals[] = new double[nrHiddenStates];
            	double l2totals[] = new double[nrHiddenStates];
            	for (int i = 0; i < nrHiddenStates; i++) {		
            		for (int j = 0; j < nrHiddenStates; j++) {
            			totals[i] += maxTable[i][j];
            			l2totals[i] +=l2Table[i][j];
					}
            		l2totals[i] = Math.sqrt(l2totals[i])/nrHiddenStates;
            		totals[i]/=nrHiddenStates;
				}
            	
            	if(em.getCurrentIterationNumber() % printClustersIter == 0 && outputDir!= null){
            		PrintStream outputMax,outputL2, outputSum;
					try {
						outputMax = InputOutput.openWriter(outputDir+"/transl1lmax-iter."+em.getCurrentIterationNumber());
						outputL2 = InputOutput.openWriter(outputDir+"/transl1l2-iter."+em.getCurrentIterationNumber());
						outputMax.println((util.ArrayPrinting.doubleArrayToString(maxTable, null, null, "Maxes table")));
						outputL2.println((util.ArrayPrinting.doubleArrayToString(l2Table, null, null, "L2 table")));
						outputMax.close();
						outputL2.close();
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
            double totalL1LMax = ArrayMath.sum(totals);
            double totalL1L2 = ArrayMath.sum(l2totals);
            	return "L1LMax" + totalL1LMax + " AVG " + totalL1LMax/nrHiddenStates 
            	     + "L1LMax" + totalL1L2 + " AVG " + totalL1L2/nrHiddenStates;
        	}else return "";
        }
        
     
        
        @Override
        public String getPrefix() {
                return "TransL1LMax::";
        }
        
       
        
        
    
}