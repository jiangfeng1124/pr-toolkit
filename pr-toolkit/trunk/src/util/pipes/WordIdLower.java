package util.pipes;

import util.Alphabet;
import util.SparseVector;


/**
 *  Word Identity: Add the word identity as a feature if it occurs more than minWordOccurs times in the corpus.
 * @author javg
 *
 */
public  class WordIdLower extends Pipe{
	int minWordOccurs;
	
	public WordIdLower(String[] args){
		this(args[0]);
		if (args.length!= 1) throw new IllegalArgumentException("expected 1 arguments got "+args.length);
	}

	
	//Note that String constructor has to be the first due to hack in
	// in building from string
	public WordIdLower(String minWordOccurs){
		this.minWordOccurs = Integer.parseInt(minWordOccurs);
	}
	
	public WordIdLower(int minWordOccurs){
		this.minWordOccurs = minWordOccurs;
	}
	
	public  void process(int wordId, String word,  Alphabet<String> alphabet,SparseVector sv){
		if (c.getWordTypeCounts(wordId)> minWordOccurs){
			sv.add(alphabet.lookupObject("word="+word.toLowerCase()), 1);
		}
	}
	
	public String getName(){
		return "WordId min: " + minWordOccurs;
	}
	
}
