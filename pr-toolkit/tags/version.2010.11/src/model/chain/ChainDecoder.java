package model.chain;

import model.AbstractModel;
import model.AbstractSentenceDist;
import model.chain.hmm.HMM;
import model.chain.hmm.HMMSentenceDist;
import data.InstanceList;
import decoderStats.AbstractDecoderStats;

public abstract class ChainDecoder {
	
	
	public int[][] decodeSet(AbstractModel model, InstanceList toDecode,AbstractDecoderStats stats){
		
		int[][] result = new int[toDecode.instanceList.size()][];
		AbstractSentenceDist[] sentences = model.getSentenceDists(toDecode);
		System.out.println("Decoding "+ sentences.length);
		if(stats != null){
			stats.beforeInference((HMM)model);
		}
		for (int i = 0; i < sentences.length; i++) {
	
			if(stats != null){
				stats.beforeSentenceInference((HMM) model, (HMMSentenceDist)sentences[i]);
			}
			result[i]=decode((ChainSentenceDist)sentences[i]);
			if(stats != null){
				stats.afterSentenceInference((HMM) model, (HMMSentenceDist)sentences[i]);
			}
			((ChainSentenceDist)sentences[i]).clearCaches();
			((ChainSentenceDist)sentences[i]).clearPosteriors();
		}
		if(stats != null){
			stats.endInference((HMM)model);
		}
		return result;
	}
	
	public abstract int[] decodeWithoutInit(ChainSentenceDist sentence);
	public abstract int[] decode(ChainSentenceDist sentence);
	
}
