package util.pipes;

import util.Alphabet;
import util.SparseVector;


/**
 *  Word Identity: Add the word identity as a feature if it occurs more than minWordOccurs times in the corpus.
 * @author javg
 *
 */
public  class WordId extends Pipe{
	int minWordOccurs;
	
	public WordId(String[] args){
		this(args[0]);
		if (args.length!= 1) throw new IllegalArgumentException("expected 1 arguments got "+args.length);
	}

	
	//Note that String constructor has to be the first due to hack in
	// in building from string
	public WordId(String minWordOccurs){
		this.minWordOccurs = Integer.parseInt(minWordOccurs);
	}
	
	public WordId(int minWordOccurs){
		this.minWordOccurs = minWordOccurs;
	}
	
	public  void process(int wordId, String word,  Alphabet<String> alphabet,SparseVector sv){
		if (c.getWordTypeCounts(wordId)> minWordOccurs){
			sv.add(alphabet.lookupObject("word="+word), 1);
		}
	}
	public String getFeaturePrefix(){
		return "word";
	}
	public String getName(){
		return "WordId min: " + minWordOccurs;
	}
	
}
