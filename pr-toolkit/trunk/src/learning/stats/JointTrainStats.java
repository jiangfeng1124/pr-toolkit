package learning.stats;


import learning.JointEM;
import model.AbstractModel;
import model.AbstractSentenceDist;
import constraints.CorpusConstraints;
import constraints.JointCorpusConstraints;



public  abstract class JointTrainStats {

        
                
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
        public void emStart(AbstractModel[] model, JointEM em){
                
        }
        
        /**
         * Stats at the end of EM. 
         */
        public void emEnd(AbstractModel[] model,JointEM em){
                
        }
        
        /**
         * Stats at the begining of EM. 
         */
        public void emIterStart(AbstractModel[] model,JointEM em){
                
        }
        
        /**
         * Stats at the begining of EM. 
         */
        public void emIterEnd(AbstractModel[] model,JointEM em){
                
        }
        
        
        
        
        /**
         * 
         */
        public void eStepStart(AbstractModel[] model,JointEM em){
                
        }
        
        /**
         * 
         */
        public void eStepEnd(AbstractModel[] model,JointEM em){
                
        }
        
        /**
         * 
         */
        public void eStepSentenceStart(AbstractModel[] model, JointEM em,AbstractSentenceDist sd[]){
                
        }
        
        /**
         * 
         */
        public void eStepSentenceEnd(AbstractModel[] model, JointEM em,AbstractSentenceDist sd[]){
                
        }
        
        /**
         * 
         */
        public void mStepStart(AbstractModel[] model,JointEM em){
                
        }
        
        /**
         * 
         */
        public void mStepEnd(AbstractModel[] model,JointEM em){
                
        }
        
        public void eStepBeforeConstraints(AbstractModel[] model,
        		JointEM em,
        		JointCorpusConstraints constraints,
        		AbstractSentenceDist[][] sentenceDists){
                
        }
        
        public void eStepAfterConstraints(AbstractModel[] model,JointEM em,
        		JointCorpusConstraints constraints,
        		AbstractSentenceDist[][] sentenceDists){
                
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
        public String printEndEM(AbstractModel[] model,JointEM em){
                return "";
        }
        
        /**
         * Outputs relevant information at the end of an EM Iteration
         */
        public String printEndEMIter(AbstractModel[] model,JointEM em){
                return "";
        }
        
        /**
         * Outputs relevant information at the end of an E Step
         */
        public String printEndEStep(AbstractModel[] model,JointEM em){
                return "";
        }
        
        /**
         * Outputs relevant information at the end of an 
         * sentence E Step
         */
        public String printEndSentenceEStep(AbstractModel[] model,JointEM em){
                return "";
        }
        
        /**
         * Outputs relevant information at the end of an 
         * M Step
         */
        public String printEndMStep(AbstractModel[] model,JointEM em){
                return "";
        }
        
        
        public String printEStepEndConstraints(AbstractModel[] model,
        		JointEM em,
        		CorpusConstraints constraints){
                return "";
        }
        
}