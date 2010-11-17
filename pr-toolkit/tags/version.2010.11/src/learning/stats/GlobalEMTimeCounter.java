package learning.stats;

import learning.EM;
import model.AbstractModel;



public class GlobalEMTimeCounter extends TrainStats{

        long emStartTime, emEndTime;
        long emEStepStartTime, emEStepEndTime;
        long emMStepStartTime, emMStepEndTime;
        long totalEStepTime, totalMStepTime;
        
        public GlobalEMTimeCounter() {
                
        }
        
        public String getPrefix(){
                return "";
        }
        
        @Override
        public void emStart(AbstractModel model,EM em){
                emStartTime = System.currentTimeMillis();
        }
        
        @Override
        public void emEnd(AbstractModel model,EM em){
                emEndTime = System.currentTimeMillis();
        }
        
        @Override
        public String printEndEM(AbstractModel model,EM em){
                String out = "Ended Training: Took " + util.Printing.formatTime(emEndTime - emStartTime)+"\n";
                out += "Time spend on E-Step " + util.Printing.formatTime(totalEStepTime)+"\n";
                out += "Time spend on M-Step " + util.Printing.formatTime(totalMStepTime)+"\n";
                return out;
        }
        
        @Override
        public void eStepStart(AbstractModel model,EM em){
                emEStepStartTime = System.currentTimeMillis();
        }
        
        @Override
        public void eStepEnd(AbstractModel model,EM em){
                emEStepEndTime = System.currentTimeMillis();
                totalEStepTime +=(emEStepEndTime - emEStepStartTime); 
        }
        
        @Override
        public String printEndEStep(AbstractModel model,EM em){
                return "Iter " + em.getCurrentIterationNumber() + " E-Step " + util.Printing.formatTime(emEStepEndTime - emEStepStartTime)+"\n" ;
        }
        
        @Override
        public void mStepStart(AbstractModel model,EM em){
                emMStepStartTime = System.currentTimeMillis();
        }
        
        @Override
        public void mStepEnd(AbstractModel model,EM em){
                emMStepEndTime = System.currentTimeMillis();
                totalMStepTime +=(emMStepEndTime - emMStepStartTime);
        }
        
        @Override
        public String printEndMStep(AbstractModel model,EM em){
                return "Iter " + em.getCurrentIterationNumber() + " M-Step " + util.Printing.formatTime(emMStepEndTime - emMStepStartTime)+"\n" ;
        }
}