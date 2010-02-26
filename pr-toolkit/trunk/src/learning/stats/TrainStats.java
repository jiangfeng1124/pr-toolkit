package learning.stats;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import learning.EM;
import model.AbstractModel;
import model.AbstractSentenceDist;
import constraints.CorpusConstraints;



public  abstract class TrainStats {

        
                
        public abstract String getPrefix();
        
        
        /**
         * 
         * 
         * 
         * HOOKS To collect information
         * Different points where one might one to collect information
         * 
         * 
         * NOTE: There are several hooks. To add a new hook just put on the code and
         * create this method here an in CompositeTrainStats.
         * Each stat class is responsible to collect the info they want as well as do 
         * all cleanup they want.
         * 
         * 
         */
        
        
        /**
         * Stats at the begining of EM. 
         */
        public void emStart(AbstractModel model, EM em){
                
        }
        
        /**
         * Stats at the end of EM. 
         */
        public void emEnd(AbstractModel model,EM em){
                
        }
        
        /**
         * Stats at the begining of EM. 
         */
        public void emIterStart(AbstractModel model,EM em){
                
        }
        
        /**
         * Stats at the begining of EM. 
         */
        public void emIterEnd(AbstractModel model,EM em){
                
        }
        
        
        
        
        /**
         * 
         */
        public void eStepStart(AbstractModel model,EM em){
                
        }
        
        /**
         * 
         */
        public void eStepEnd(AbstractModel model,EM em){
                
        }
        
        /**
         * 
         */
        public void eStepSentenceStart(AbstractModel model, EM em,AbstractSentenceDist sd){
                
        }
        
        /**
         * 
         */
        public void eStepSentenceEnd(AbstractModel model, EM em,AbstractSentenceDist sd){
                
        }
        
        /**
         * 
         */
        public void mStepStart(AbstractModel model,EM em){
                
        }
        
        /**
         * 
         */
        public void mStepEnd(AbstractModel model,EM em){
                
        }
        
        public void eStepBeforeConstraints(AbstractModel model,
        		EM em,
        		CorpusConstraints constraints,
        		AbstractSentenceDist[] sentenceDists){
                
        }
        
        public void eStepAfterConstraints(AbstractModel model,EM em,
        		CorpusConstraints constraints,
        		AbstractSentenceDist[] sentenceDists){
                
        }
        
        /**
         * 
         * 
         * 
         * OUTPUT POINTS
         * Different Points where we might want to output statistics
         * 
         * 
         * 
         * 
         */
        /**
         * Outputs relevant information at the end of training
         */
        public String printEndEM(AbstractModel model,EM em){
                return "";
        }
        
        /**
         * Outputs relevant information at the end of an EM Iteration
         */
        public String printEndEMIter(AbstractModel model,EM em){
                return "";
        }
        
        /**
         * Outputs relevant information at the end of an E Step
         */
        public String printEndEStep(AbstractModel model,EM em){
                return "";
        }
        
        /**
         * Outputs relevant information at the end of an 
         * sentence E Step
         */
        public String printEndSentenceEStep(AbstractModel model,EM em){
                return "";
        }
        
        /**
         * Outputs relevant information at the end of an 
         * M Step
         * @throws FileNotFoundException 
         * @throws IOException 
         * @throws UnsupportedEncodingException 
         */
        public String printEndMStep(AbstractModel model,EM em) throws FileNotFoundException, UnsupportedEncodingException, IOException{
                return "";
        }
        
        
        public String printEStepEndConstraints(AbstractModel model,
        		EM em,
        		CorpusConstraints constraints){
                return "";
        }
        
}