package learning.stats;

import java.io.FileNotFoundException;

import util.MemoryTracker;

import learning.EM;
import model.AbstractModel;
import model.AbstractSentenceDist;
import constraints.CorpusConstraints;



public class MemoryStats extends TrainStats{
	
	MemoryTracker emMT = new MemoryTracker();
	MemoryTracker emIterMT = new MemoryTracker();
	MemoryTracker eStepMT = new MemoryTracker();
	MemoryTracker mStepMT = new MemoryTracker();

		public MemoryStats(){
			
		}
                
        public String getPrefix(){
        	return "MEM::";
        }
        
        
        public void emStart(AbstractModel model, EM em){
        	emMT.clear();
        	emMT.start();
        }
        
        public void emEnd(AbstractModel model,EM em){
        	emMT.finish();
        }
        
        public void emIterStart(AbstractModel model,EM em){
           emIterMT.clear();
           emIterMT.start();
        }
        
        public void emIterEnd(AbstractModel model,EM em){
        	emIterMT.finish();
        }
        
        
        public void eStepStart(AbstractModel model,EM em){
        	eStepMT.clear();
        	eStepMT.start();
        }
        
        public void eStepEnd(AbstractModel model,EM em){
        	eStepMT.finish();
        }
        
        
        public void mStepStart(AbstractModel model,EM em){
        	mStepMT.clear();
        	mStepMT.start();
        }
        
        public void mStepEnd(AbstractModel model,EM em){
        	mStepMT.finish();
        }
        
        public String printEndEM(AbstractModel model,EM em){
                return "EM-"+emMT.print();
        }
        
        public String printEndEMIter(AbstractModel model,EM em){
                return "EMIter-"+emIterMT.print();
        }
        
        public String printEndEStep(AbstractModel model,EM em){
                return "EStep-"+eStepMT.print();
        }
        
        public String printEndMStep(AbstractModel model,EM em) throws FileNotFoundException{
                return "MStep-"+mStepMT.print();
        }
        
        
}