package learning.stats;

import learning.EM;
import model.AbstractModel;
import model.AbstractSentenceDist;


public class LikelihoodStats extends TrainStats{

        double likelihood;
        double prevLikelihood = 0;
        
        public String getPrefix(){
                return "LogL::";
        }
        
        @Override
        public void eStepSentenceEnd(AbstractModel model,EM em,AbstractSentenceDist sd){
                likelihood += sd.getLogLikelihood();
        }
        
        @Override
        public String printEndEStep(AbstractModel model,EM em){
                StringBuffer s = new StringBuffer();    
                s.append("Iter " + em.getCurrentIterationNumber() +
                                "\t-log(L)=" + util.Printing.prettyPrint(-likelihood, "0.000000E00", 9));
                s.append(" diff="+ util.Printing.prettyPrint(likelihood - prevLikelihood,"0.000000E00", 9));
                //Perform clean up
                prevLikelihood = likelihood;
                likelihood = 0;                
                return s.toString();
        }
}