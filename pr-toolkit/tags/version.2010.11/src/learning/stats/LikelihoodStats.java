package learning.stats;

import learning.EM;
import model.AbstractModel;
import model.AbstractSentenceDist;


public class LikelihoodStats<K extends AbstractModel, J extends AbstractSentenceDist> extends TrainStats<K,J> {

        double likelihood;
        double prevLikelihood = 0;
        
        public String getPrefix(){
                return "LogL::";
        }
        
        @Override
        public void eStepSentenceEnd(K model, EM em, J sd){
                likelihood += sd.getLogLikelihood();
        }
        
        @Override
        public String printEndEStep(K model,EM em){
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