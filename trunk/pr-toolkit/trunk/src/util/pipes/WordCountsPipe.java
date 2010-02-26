package util.pipes;

import java.io.IOException;

import gnu.trove.TIntDoubleHashMap;
import util.Alphabet;
import util.SparseVector;
import data.Corpus;

/**
 * Number of occurrences: Adds a feature if the word is in the top X occurring words in the corpus. 
 * @author javg
 *
 */
public class WordCountsPipe extends Pipe{
	TIntDoubleHashMap wordAccum;
	double threshold;
	
	public WordCountsPipe(String[] args){
		this(args[0]);
		if (args.length!= 1) throw new IllegalArgumentException("expected 1 arguments got "+args.length);
	}

	
	//Note that String constructor has to be the first due to hack in
	// in building from string
	public WordCountsPipe(String threshold){
		this.threshold = Double.parseDouble(threshold);
	}
	
	public WordCountsPipe(double threshold){
		this.threshold = threshold;
	}
	
	
	
	public void init(Corpus c) throws IOException{
		super.init(c);
		wordAccum = c.wordAlphabet.getAccumFreqs();
	}
	
	public  void process(int wordId, String word,  Alphabet<String> alphabet,SparseVector sv){
		if(wordAccum.get(wordId) > threshold){
			sv.add(alphabet.lookupObject("woccur"), 1);
		}
	}
	
	public String getName(){
		return "WordCountsPipe threshold: " + threshold;
	}
}
