package decoderStats;

import java.util.ArrayList;

import model.AbstractSentenceDist;
import model.chain.hmm.HMM;
import model.chain.hmm.HMMDirectGradientObjective;
import model.chain.hmm.directGradientStats.MultinomialMaxEntDirectTrainerStats;

public class CompositeDecoderStats extends AbstractDecoderStats{

	ArrayList<AbstractDecoderStats> list;
	
	public CompositeDecoderStats() {
		list = new ArrayList<AbstractDecoderStats>();
	}
	
	public void addStat(AbstractDecoderStats stat){
		list.add(stat);
	}
	
	public void beforeInference(HMM model){
		for(AbstractDecoderStats stat: list){
			stat.beforeInference(model);
		}
	}
	
	public void beforeSentenceInference(HMM model, AbstractSentenceDist sd){
		for(AbstractDecoderStats stat: list){
			stat.beforeSentenceInference(model,sd);
		}

	}
	
	public void afterSentenceInference(HMM model, AbstractSentenceDist sd){
		for(AbstractDecoderStats stat: list){
			stat.afterSentenceInference(model,sd);
		}
	}
	
	public void endInference(HMM model){
		StringBuilder sb = new StringBuilder();
		for(AbstractDecoderStats stat: list){
			String s = (stat.collectFinalStats(model));
			if(s!=""){
				//Replace ending new lines by the corresponding prefix
				s=  s.replace("\n", "\nFINAL::"+stat.getPrefix()+"::");
				//Add iterationd and prefix to the begining of file
				sb.append("FINAL::"+stat.getPrefix()+"::"+s+"\n");
			}
		}
		System.out.println(sb.toString());
	}

	@Override
	public String collectFinalStats(HMM model) {
		return "";
	}
	
	  public  String getPrefix(){
		  return "";
	  }
	
	
	
}
