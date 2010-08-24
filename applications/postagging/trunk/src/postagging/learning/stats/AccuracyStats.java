package postagging.learning.stats;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import postagging.data.PosCorpus;
import postagging.data.PosInstance;
import postagging.evaluation.PosMapping;
import postagging.programs.RunModel;
import data.InstanceList;
import learning.EM;
import learning.stats.TrainStats;
import model.AbstractModel;
import model.chain.PosteriorDecoder;
import model.chain.ViterbiDecoder;
import model.chain.hmm.HMM;


//Computes the accuracy after the M-Step
public class AccuracyStats extends TrainStats{

        int printEvery;
        int saveEvery;
        String saveDir;
        
        InstanceList list;
        boolean savePredictions;
        
        public AccuracyStats(String printEvery, String saveEvery,String saveDir, 
        		InstanceList list) {
                this.printEvery = Integer.parseInt(printEvery);
                this.saveEvery = Integer.parseInt(saveEvery);
                this.saveDir = saveDir;
                this.list = list;
        }
        
        public AccuracyStats(int printEvery, int saveEvery,String saveDir, boolean savePredictions,
        		InstanceList list) {
                this.printEvery = printEvery;
                this.savePredictions = savePredictions;
                this.saveEvery = saveEvery;
                this.saveDir = saveDir;
                this.list = list;
        }

        
        @Override
        public String getPrefix() {
                return "ACC::";
        }
        
        public String printEndMStep(AbstractModel model, EM em) throws UnsupportedEncodingException, IOException{
            String result = "";     
        	if(em.getCurrentIterationNumber() % printEvery == 0){
                      result =  RunModel.testModel((HMM)model, list, 
                    		  "em-" + em.getCurrentIterationNumber()+ "-",savePredictions && (em.getCurrentIterationNumber() != 0 & em.getCurrentIterationNumber() % saveEvery == 0)
                    		  ,saveDir,null);
                      
        	}
        	return result;
        }
        
        

        
        
        
}