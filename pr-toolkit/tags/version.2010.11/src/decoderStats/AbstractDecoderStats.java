package decoderStats;

import model.AbstractSentenceDist;
import model.chain.hmm.HMM;
import model.chain.hmm.HMMDirectGradientObjective;

public abstract class AbstractDecoderStats {

	public void beforeInference(HMM model){
		
	}
	
	public void beforeSentenceInference(HMM model, AbstractSentenceDist sd){
		
	}
	
	public void afterSentenceInference(HMM model, AbstractSentenceDist sd){
		
	}
	
	public abstract void endInference(HMM model);

	public abstract String collectFinalStats(HMM model);

    public abstract String getPrefix();

}
