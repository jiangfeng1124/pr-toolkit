package learning.stats;

import java.io.FileNotFoundException;

import learning.EM;
import model.AbstractModel;
import util.MemoryTracker;


/**
 * Prints the cumulative memory for EM.
 * We are not restarting the memory tracker
 * @author javg
 *
 */
public class ComulativeMemoryStats extends TrainStats{
	
	MemoryTracker emMT = new MemoryTracker();
	

		public ComulativeMemoryStats(){
			
		}
                
        public String getPrefix(){
        	return "ACCUM-MEM::";
        }
        
        
        public void emStart(AbstractModel model, EM em){
        	emMT.clear();
        	emMT.start();
        }
        
        public void emEnd(AbstractModel model,EM em){
        	emMT.finish();
        }
        
        
        
        public void emIterEnd(AbstractModel model,EM em){
        	emMT.finish();
        }
        
        
       
        
        public void eStepEnd(AbstractModel model,EM em){
        	emMT.finish();
        }
        
        
      
        
        public void mStepEnd(AbstractModel model,EM em){
        	emMT.finish();
        }
        
        public String printEndEM(AbstractModel model,EM em){
                return "EM-"+emMT.print();
        }
        
        public String printEndEMIter(AbstractModel model,EM em){
                return "EMIter-"+emMT.print();
        }
        
        public String printEndEStep(AbstractModel model,EM em){
                return "EStep-"+emMT.print();
        }
        
        public String printEndMStep(AbstractModel model,EM em) throws FileNotFoundException{
                return "MStep-"+emMT.print();
        }
        
        
}