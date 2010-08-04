package postagging.data;

import gnu.trove.TIntArrayList;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

import util.Alphabet;
import util.CountAlphabet;
import util.InputOutput;
import data.InstanceList;

public class PosInstanceList extends InstanceList{
	
	
	// This is useful to compare when merging two instance lists to see if they are from the same alphabet.
	// Also to print the sentences with names instead of numbers.
	Alphabet<String> tagsAlphabet;
	
	public PosInstanceList(String name) {
		super(name);
	}
	
	
		
	
	
	public static PosInstanceList readFromPosTagProject(String name, String fileName, Alphabet<String> words, 
			Alphabet<String> tags,
			boolean lowercase,	
			int minSentenceLength,int maxSentenceLenght, int maxNrSentences
			, CountAlphabet fullAlphabet, int minWordOccurs)
	throws UnsupportedEncodingException, FileNotFoundException, IOException{
		PosInstanceList il = new PosInstanceList(name);
		il.wordsAlphabet = words;
		il.tagsAlphabet = tags;
		il.name = name;
		BufferedReader reader = InputOutput.openReader(fileName);
		
		Pattern whitespace = Pattern.compile("\\s+");
		TIntArrayList wordsList  =new TIntArrayList();
		TIntArrayList posList  =new TIntArrayList();
		String line = reader.readLine();
		int nrSentences = 0;
		while(line != null) {
			if(!line.matches("\\s*")){
				String[] info = whitespace.split(line);
				//String word = normalize(info[1]);
				//TODO Check if we want to call normalizer or no...
				String word = info[1];
				if(lowercase){
					word = word.toLowerCase();
				}	
				if(fullAlphabet.getCounts(word) <= minWordOccurs){
			//		System.out.println("adding unk for word" + word + " :"+ fullAlphabet.getCounts(word)+":");
					word = "unk";
				}
				wordsList.add(words.lookupObject(word));
				posList.add(tags.lookupObject(info[0]));
			}
			else { // Case of end of sentence
				int sentenceSize = wordsList.size();
				if(sentenceSize > minSentenceLength && sentenceSize <= maxSentenceLenght){
					addDepInst(il, wordsList,posList);
				}
				wordsList.clear();
				posList.clear();
				nrSentences++;
				if(nrSentences >= maxNrSentences){
					break;
				}
			}
			line = reader.readLine();
		}	
		// Add final dependency instance
		// (need this in case file ends without a trailing newline)
		if(wordsList.size() > 0)
			addDepInst(il, wordsList,posList);
		
		return il;
	}
	

	public static PosInstanceList readFromConll(String name, String fileName, Alphabet<String> words, 
			Alphabet<String> tags,
			boolean lowercase,
			int minSentenceLength,int maxSentenceLenght,
			int maxNrSentences, CountAlphabet fullAlphabet, int minWordOccurs)
	throws UnsupportedEncodingException, FileNotFoundException, IOException{	
		
		PosInstanceList il = new PosInstanceList(name);
		il.wordsAlphabet = words;
		il.tagsAlphabet = tags;
		il.name = name;
		BufferedReader reader = InputOutput.openReader(fileName);
		
		Pattern whitespace = Pattern.compile("\\s+");
		TIntArrayList wordsList  =new TIntArrayList();
		TIntArrayList posList  =new TIntArrayList();
		String line = reader.readLine();
		int nrSentences = 0;
		while(line != null) {
			if(!line.matches("\\s*")){
				String[] info = whitespace.split(line);
				String word = normalize(info[1]);
				if(lowercase){
					word = word.toLowerCase();
				}
				if(fullAlphabet.getCounts(word) <= minWordOccurs){
					//System.out.println("adding unk for word" + word);
					word = "unk";
				}
				wordsList.add(words.lookupObject(word));
				posList.add(tags.lookupObject(info[4]));
			}
			else { // Case of end of sentence
				int sentenceSize = wordsList.size();
				if(sentenceSize > minSentenceLength && sentenceSize <= maxSentenceLenght){
					addDepInst(il, wordsList,posList);
				}
				wordsList.clear();
				posList.clear();
				nrSentences++;
				if(nrSentences >= maxNrSentences){
					break;
				}
			}
			line = reader.readLine();
		}	
		// Add final dependency instance
		// (need this in case file ends without a trailing newline)
		if(wordsList.size() > 0)
			addDepInst(il, wordsList,posList);
		
		return il;
	}
	
	private static void addDepInst(PosInstanceList il, TIntArrayList wordsList, TIntArrayList posList)
	{
		il.add(new PosInstance(wordsList.toNativeArray(),posList.toNativeArray(),il.instanceList.size()));
		il.maxInstanceSize = Math.max(il.maxInstanceSize,wordsList.size());

	}
}
