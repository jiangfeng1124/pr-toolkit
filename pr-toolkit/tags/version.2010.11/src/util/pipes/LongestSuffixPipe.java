package util.pipes;

import util.Alphabet;
import util.CountAlphabet;
import util.SparseVector;
import data.Corpus;
import data.CorpusUtils;

/**
 *  Word Suffix: Add the suffix with biggest len that occurs more than minSuffixOccur
 *   * @author javg
 *
 */
public class LongestSuffixPipe extends Pipe{
	int maxLen;
	int minSuffixOccur;
	CountAlphabet<String>[] suffixes;
	
	public LongestSuffixPipe(String[] args){
		this(args[0],args[1]);
		if (args.length!= 2) throw new IllegalArgumentException("expected 2 arguments got "+args.length);
	}
	
	//Note that String constructor has to be the first due to hack in
	// in building from string
	public LongestSuffixPipe(String maxLen, String minSuffixOccur){
		this.maxLen= Integer.parseInt(maxLen);
		this.minSuffixOccur = Integer.parseInt(minSuffixOccur);
	}
	
	public LongestSuffixPipe(int maxL, int minSuffixOccur){
		this.maxLen = maxL;
		this.minSuffixOccur = minSuffixOccur;
	}
	
	public void init(Corpus c){
		this.c = c;
		suffixes = new CountAlphabet[maxLen-1];
		for(int len = 2; len < maxLen+1;len++){
			suffixes[len-2] = CorpusUtils.createTypesSuffixAlphabet(c, len);
		}
	}
	
	@Override
	public  void process(int wordId, String word, Alphabet<String> alphabet,SparseVector sv){
		for(int len = maxLen; len >=2 ; len--){
			if(word.length() > len){
				String sufix = word.substring(word.length()-len);
				if(suffixes[len-2].getCounts(suffixes[len-2].feat2index.get(sufix)) > minSuffixOccur){
					sv.add(alphabet.lookupObject("suf+"+len+"="+sufix), 1);
					break;
				}
			}
		}
	}
	
	public  String getFeaturePrefix(){
		return "suf";
	}
	
	
	public String getName(){
		return "LongestSuffix len: " + maxLen + " min occurs " + minSuffixOccur;
	}
}
