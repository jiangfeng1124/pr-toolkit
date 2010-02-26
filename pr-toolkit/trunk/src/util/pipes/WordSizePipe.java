package util.pipes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.Alphabet;
import util.SparseVector;

/**
 *  Word Length: Adds a feature if the word is smaller than X letters.
 * @author javg
 *
 */
public class WordSizePipe extends Pipe{
	double minSize;
	String pattern;
	Pattern p;
	public WordSizePipe(String[] args){
		this(args[0]);
		if (args.length!= 1) throw new IllegalArgumentException("expected 1 arguments got "+args.length);
	}

	//Note that String constructor has to be the first due to hack in
	// in building from string
	public WordSizePipe(String minSize){
		this.minSize = Integer.parseInt(minSize);
		p = Pattern.compile("[0-9.,!?:;()\"/\\[\\]'`'-]+");
	}
	
	public WordSizePipe(int minSize){
		this.minSize = minSize;
		p = Pattern.compile("[0-9.,!?:;()\"/\\[\\]'`'-]+");
	}
	
	public  void process(int wordId, String word, Alphabet<String> alphabet,SparseVector sv){
		Matcher m = p.matcher(word);	
		if(word.length() < minSize && !m.matches()){
			sv.add(alphabet.lookupObject("wsize"), 1);
		}
	}
	
	public String getName(){
		return "WordSize min size: " + minSize;
	}
}
