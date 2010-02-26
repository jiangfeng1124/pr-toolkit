package util.pipes;

import util.Alphabet;
import util.CountAlphabet;
import util.SparseVector;
import data.Corpus;
import data.CorpusUtils;

/**
 *  Word Suffix: Add the word suffix of size len  as a feature if this suffix occurs in more than minSuffixOccur word types. 
 * @author javg
 *
 */
public class SuffixPipe extends Pipe{
	int len;
	int minSuffixOccur;
	CountAlphabet<String> suffixes;
	
	public SuffixPipe(String[] args){
		this(args[0],args[1]);
		if (args.length!= 2) throw new IllegalArgumentException("expected 2 arguments got "+args.length);
	}
	
	//Note that String constructor has to be the first due to hack in
	// in building from string
	public SuffixPipe(String len, String minSuffixOccur){
		this.len = Integer.parseInt(len);
		this.minSuffixOccur = Integer.parseInt(minSuffixOccur);
	}
	
	public SuffixPipe(int len, int minSuffixOccur){
		this.len = len;
		this.minSuffixOccur = minSuffixOccur;
	}
	
	public void init(Corpus c){
		this.c = c;
		suffixes = CorpusUtils.createTypesSuffixAlphabet(c, len);
	}
	
	@Override
	public  void process(int wordId, String word, Alphabet<String> alphabet,SparseVector sv){
		if(word.length() > len){
			String sufix = word.substring(word.length()-len);
			if(suffixes.getCounts(suffixes.feat2index.get(sufix)) > minSuffixOccur){
				sv.add(alphabet.lookupObject("suf+"+len+"="+sufix), 1);
			}
		}
	}
	
	public String getName(){
		return "Suffix len: " + len + " min occurs " + minSuffixOccur;
	}
}
