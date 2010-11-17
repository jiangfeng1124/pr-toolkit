package learning.stats;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import learning.EM;
import model.AbstractModel;
import model.AbstractSentenceDist;
import constraints.CorpusConstraints;



public  abstract class TrainStats<K extends AbstractModel, J extends AbstractSentenceDist> {

        
                
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
        public void emStart(K model, EM em){
                
        }
        
        /**
         * Stats at the end of EM. 
         */
        public void emEnd(K model,EM em){
                
        }
        
        /**
         * Stats at the begining of EM. 
         */
        public void emIterStart(K model,EM em){
                
        }
        
        /**
         * Stats at the begining of EM. 
         */
        public void emIterEnd(K model,EM em){
                
        }
        
        
        
        
        /**
         * 
         */
        public void eStepStart(K model,EM em){
                
        }
        
        /**
         * 
         */
        public void eStepEnd(K model,EM em){
                
        }
        
        /**
         * 
         */
        public void eStepSentenceStart(K model, EM em,J sd){
                
        }
        
        /**
         * 
         */
        public void eStepSentenceEnd(K model, EM em,J sd){
                
        }
        
        /**
         * 
         */
        public void mStepStart(K model,EM em){
                
        }
        
        /**
         * 
         */
        public void mStepEnd(K model,EM em){
                
        }
        
        public void eStepBeforeConstraints(K model,
        		EM em,
        		CorpusConstraints constraints,
        		J[] sentenceDists){
                
        }
        
        public void eStepAfterConstraints(K model,EM em,
        		CorpusConstraints constraints,
        		J[] sentenceDists){
                
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
        public String printEndEM(K model,EM em){
                return "";
        }
        
        /**
         * Outputs relevant information at the end of an EM Iteration
         */
        public String printEndEMIter(K model,EM em){
                return "";
        }
        
        /**
         * Outputs relevant information at the end of an E Step
         */
        public String printEndEStep(K model,EM em){
                return "";
        }
        
        /**
         * Outputs relevant information at the end of an 
         * sentence E Step
         */
        public String printEndSentenceEStep(K model,EM em){
                return "";
        }
        
        /**
         * Outputs relevant information at the end of an 
         * M Step
         * @throws FileNotFoundException 
         * @throws IOException 
         * @throws UnsupportedEncodingException 
         */
        public String printEndMStep(K model,EM em) throws FileNotFoundException, UnsupportedEncodingException, IOException{
                return "";
        }
        
        
        public String printEStepEndConstraints(K model,
        		EM em,
        		CorpusConstraints constraints){
                return "";
        }
        
}