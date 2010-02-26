package util.pipes;

import java.io.IOException;

import gnu.trove.TIntDoubleHashMap;
import util.Alphabet;
import util.CountAlphabet;
import util.SparseVector;
import data.Corpus;

/**
 * Number of occurrences: Adds a feature if the word is in the top X occurring words in the corpus. 
 * @author javg
 *
 */
public class CapitalizationPipe extends Pipe{
	TIntDoubleHashMap wordAccum;
	CountAlphabet<String> firstLetterCounts;
	double threshold;
	
	public CapitalizationPipe(String[] args){
		this(args[0]);
		if (args.length!= 1) throw new IllegalArgumentException("expected 1 arguments got "+args.length);
	}

	
	//Note that String constructor has to be the first due to hack in
	// in building from string
	public CapitalizationPipe(String threshold){
		this.threshold = Double.parseDouble(threshold);
	}
	
	public CapitalizationPipe(double threshold){
		this.threshold = threshold;
	}
	
	
	
	public void init(Corpus c) throws IOException{
		super.init(c);
		firstLetterCounts = new CountAlphabet<String>();
		this.c = c;
		for(int sentenceNr=0; sentenceNr < c.getNrOfTrainingSentences(); sentenceNr++){
			int[] words = c.trainInstances.instanceList.get(sentenceNr).words;
			firstLetterCounts.lookupObject(c.wordAlphabet.index2feat.get(words[0]));
		}
		wordAccum = c.wordAlphabet.getAccumFreqs();
	}
	
	public  void process(int wordId, String word,  Alphabet<String> alphabet,SparseVector sv){
		double numberOfTimesFirst = 0;
		if(firstLetterCounts.getCounts(wordId) != -1){
			numberOfTimesFirst = 1.0*firstLetterCounts.getCounts(wordId)
			/(c.wordAlphabet.getCounts(wordId)+c.wordAlphabet.getCounts(c.wordAlphabet.feat2index.get(word.toLowerCase())));
		}
		if( Character.isUpperCase(word.charAt(0)) && numberOfTimesFirst < threshold && word.length() > 1 && wordAccum.get(wordId) < 0.05){
			sv.add(alphabet.lookupObject("capitalized"), 1);
		}
	
	}
	
	public String getName(){
		return "Capitalized: ";
	}
}
