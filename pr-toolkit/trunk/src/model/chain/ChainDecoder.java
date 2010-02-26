package model.chain;

import model.AbstractModel;
import model.AbstractSentenceDist;
import data.InstanceList;

public abstract class ChainDecoder {
	
	
	public int[][] decodeSet(AbstractModel model, InstanceList toDecode){
		
		int[][] result = new int[toDecode.instanceList.size()][];
		AbstractSentenceDist[] sentences = model.getSentenceDists(toDecode);
		System.out.println("Decoding "+ sentences.length);
		for (int i = 0; i < sentences.length; i++) {
			result[i]=decode((ChainSentenceDist)sentences[i]);
			((ChainSentenceDist)sentences[i]).clearCaches();
			((ChainSentenceDist)sentences[i]).clearPosteriors();
		}
		return result;
	}
	
	public abstract int[] decodeWithoutInit(ChainSentenceDist sentence);
	public abstract int[] decode(ChainSentenceDist sentence);
	
}
