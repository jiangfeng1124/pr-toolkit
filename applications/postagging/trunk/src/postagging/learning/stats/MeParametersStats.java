package postagging.learning.stats;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import util.InputOutput;
import util.LinearClassifier;
import learning.EM;
import learning.stats.TrainStats;
import model.AbstractModel;
import model.chain.hmm.HMM;


//Computes the accuracy after the M-Step
public class MeParametersStats extends TrainStats{

      
        int saveEvery;
        String saveDir;
        
      
        
       
        
        public MeParametersStats(int saveEvery,String saveDir) {
            
                this.saveEvery = saveEvery;
                this.saveDir = saveDir;
                util.FileSystem.createDir(saveDir);
        }

        
        @Override
        public String getPrefix() {
                return "FEAT::";
        }
        
        public void mStepEnd(AbstractModel model,EM em){
        	if(em.getCurrentIterationNumber() % saveEvery == 0){

        		if(model instanceof HMM){
        			HMM m = (HMM) model;
        			if(m.updateType == HMM.Update_Parameters.OBS_MAX_ENT){
        			
        			
        				for(int state = 0; state < m.nrStates; state++){
        	        		try {
								PrintStream file = InputOutput.openWriter(saveDir+"me-"+em.getCurrentIterationNumber()+"-"+state);
								LinearClassifier c = m.maxEntModels[state];
	        					for(int i = 0; i < c.w.length;i++){
	        						file.println(m.fxy.al.index2feat.get(i)+" "+c.w[i]);
	        					}
							} catch (UnsupportedEncodingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
        					
        				}
        			}
        		}
        		
        	}
        	
        }
        
        

        
        
        
}