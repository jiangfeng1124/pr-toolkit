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
public class OccureOncePipe extends Pipe{
	
	
	public OccureOncePipe(String[] args){
		
	}

	
	//Note that String constructor has to be the first due to hack in
	// in building from string
	public OccureOncePipe(){

	}
	
	public OccureOncePipe(double threshold){
	
	}
	
	
	
	public void init(Corpus c) throws IOException{
		super.init(c);
		}
	
	public  void process(int wordId, String word,  Alphabet<String> alphabet,SparseVector sv){
		if(c.getWordTypeCounts(wordId)==1){
			sv.add(alphabet.lookupObject("occur1"), 1);
		}
	}
	
	public String getName(){
		return "Occurs 1: ";
	}
}
