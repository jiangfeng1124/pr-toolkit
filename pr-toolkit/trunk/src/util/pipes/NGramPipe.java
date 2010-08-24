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
public class NGramPipe extends Pipe{
	int maxLen;
	int minNGramOccur;
	CountAlphabet<String>[] ngrams;
	
	public NGramPipe(String[] args){
		this(args[0],args[1]);
		if (args.length!= 2) throw new IllegalArgumentException("expected 2 arguments got "+args.length);
	}
	
	//Note that String constructor has to be the first due to hack in
	// in building from string
	public NGramPipe(String maxLen, String minNgramOccur){
		this.maxLen= Integer.parseInt(maxLen);
		this.minNGramOccur = Integer.parseInt(minNgramOccur);
	}
	
	public NGramPipe(int maxL, int minNgramOccur){
		this.maxLen = maxL;
		this.minNGramOccur = minNgramOccur;
	}
	
	public void init(Corpus c){
		this.c = c;
		ngrams = new CountAlphabet[maxLen];
		for(int len = 1; len < maxLen+1;len++){
			ngrams[len-1] = CorpusUtils.createTypesNGramAlphabet(c, len);
		}
	}
	
	@Override
	public  void process(int wordId, String word, Alphabet<String> alphabet,SparseVector sv){
		for(int lenght = 1; lenght < maxLen+1; lenght++){
			if(word.length() > lenght){
				for(int j = 0; j < word.length() - lenght; j++){
					String ngram = word.substring(j,j+lenght);
					int ngramId = ngrams[lenght-1].lookupObject(ngram);
					if(ngrams[lenght-1].getCounts(ngramId) >= minNGramOccur){
						sv.add(alphabet.lookupObject("ngram+"+lenght+"="+ngram), 1);
					}
				}
			}
		}			
	}
	
	public  String getFeaturePrefix(){
		return "ngram";
	}
	
	
	public String getName(){
		return "LongestSuffix len: " + maxLen + " min occurs " + minNGramOccur;
	}
}
