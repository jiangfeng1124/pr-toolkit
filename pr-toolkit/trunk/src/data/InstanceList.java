package data;

import gnu.trove.TIntArrayList;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import util.Alphabet;
import util.CountAlphabet;
import util.InputOutput;

public class InstanceList {
	public ArrayList<WordInstance> instanceList;
	public String name;
	public int maxInstanceSize;
	//Indicates the max number of instances that we want this instance list to have,
	//by default we want all
	public int maxNumberOfInstances = Integer.MAX_VALUE;
	
	// This is useful to compare when merging two instance lists to see if they are from the same alphabet.
	// Also to print the sentences with names instead of numbers.
	public Alphabet<String> wordsAlphabet;

	
	public InstanceList(String name) {
		instanceList = new ArrayList<WordInstance>();
		this.name = name;
	}
	
	public void add(WordInstance inst){
		instanceList.add(inst);
	}
	
	// Add all entries from a instance list to the other.
	// First check that all alphabets are the same.
	// Useful for transductive setting to add dev and test.
	public void add(InstanceList il, int minSentenceSize, int maxSentenceSize){
		if(il.wordsAlphabet.getId() != wordsAlphabet.getId()){
			System.out.println("Cannot add InstanceLists using different alphabets");
			System.exit(-1);
		}
		for(int i = 0; i < il.instanceList.size(); i++){
			WordInstance inst = il.instanceList.get(i);
			int sentenceSize = inst.getNrWords();
			
			if(sentenceSize > minSentenceSize && sentenceSize <= maxSentenceSize){
				if(sentenceSize > this.maxInstanceSize){
					this.maxInstanceSize = sentenceSize;
				}
				add(inst);
			}
		}
		
	}
	
	
	public static InstanceList readFromPosTagProject(String name, String fileName, Alphabet<String> words, boolean lowercase,
			int minSentenceLength,int maxSentenceLenght,CountAlphabet<String> fullAlphabet, int minWordOccurs)
	throws UnsupportedEncodingException, FileNotFoundException, IOException{		
		InstanceList il = new InstanceList(name);
		il.wordsAlphabet = words;
		il.name = name;
		BufferedReader reader = InputOutput.openReader(fileName);
	
		Pattern whitespace = Pattern.compile("\\s+");
		TIntArrayList wordsList  =new TIntArrayList();	
		String line = reader.readLine();
		while(line != null) {
			if(!line.matches("\\s*")){
				String[] info = whitespace.split(line);
				//String word = normalize(info[1]);
				String word = info[1];
				if(lowercase){
					word = word.toLowerCase();
				}
				if(fullAlphabet.getCounts(word) <= minWordOccurs){
					word = "unk";
				}
				wordsList.add(words.lookupObject(word));
			}
			else { // Case of end of sentence
				int sentenceSize = wordsList.size();
				if(sentenceSize > minSentenceLength && sentenceSize <= maxSentenceLenght){
					addDepInst(il, wordsList);
				}
				wordsList.clear();
			}
			line = reader.readLine();
		}	
		// Add final dependency instance
		// (need this in case file ends without a trailing newline)
		if(wordsList.size() > 0)
			addDepInst(il, wordsList);
		
		return il;
	}
	
	public static InstanceList readFromConll(String name, String fileName, Alphabet<String> words, boolean lowercase,
			int minSentenceLength,int maxSentenceLenght, CountAlphabet<String> fullAlphabet, int minWordOccurs)
	throws UnsupportedEncodingException, FileNotFoundException, IOException{	
		InstanceList il = new InstanceList(name);
		il.wordsAlphabet = words;
		il.name = name;
		BufferedReader reader = InputOutput.openReader(fileName);
		
		Pattern whitespace = Pattern.compile("\\s+");
		TIntArrayList wordsList  =new TIntArrayList();	
		String line = reader.readLine();
		while(line != null) {
			if(!line.matches("\\s*")){
				String[] info = whitespace.split(line);
				//TODO Normalize should be paramter or somethinf
				
				//String word = normalize(info[1]);
				String word = info[1];
				if(lowercase){
					word = word.toLowerCase();
				}
				if(fullAlphabet.getCounts(word) <= minWordOccurs){
					word = "unk";
				}
				wordsList.add(words.lookupObject(word));
			}
			else { // Case of end of sentence
				int sentenceSize = wordsList.size();
				if(sentenceSize > minSentenceLength && sentenceSize <= maxSentenceLenght){
					addDepInst(il, wordsList);
				}
				wordsList.clear();
			}
			line = reader.readLine();
		}	
		// Add final dependency instance
		// (need this in case file ends without a trailing newline)
		if(wordsList.size() > 0)
			addDepInst(il, wordsList);
		
		return il;
	}
	
	
	public static InstanceList readFromEuroparl(String name, String fileName, Alphabet<String> words, boolean lowercase,
			int minSentenceLength,int maxSentenceLenght, CountAlphabet<String> fullAlphabet, int minWordOccurs)
	throws UnsupportedEncodingException, FileNotFoundException, IOException{	
		InstanceList il = new InstanceList(name);
		il.wordsAlphabet = words;
		il.name = name;
		BufferedReader reader = InputOutput.openReader(fileName);
		String sentence = reader.readLine().trim();
		while (sentence != null) {
			String[] tokens = sentence.split(" ");
			int len = tokens.length;
			TIntArrayList wordsList  =new TIntArrayList();
			if (len <= maxSentenceLenght && len <= maxSentenceLenght) {
				for (int i = 0; i < tokens.length; i++) {
					String word = tokens[i];
					if(lowercase){
						word = word.toLowerCase();	
					}
					if(fullAlphabet.getCounts(word) <= minWordOccurs){
						word = "unk";
					}
					wordsList.add(words.lookupObject(word));
				}
				if(len > minSentenceLength && len <= maxSentenceLenght){
					addDepInst(il, wordsList);
				}
				wordsList.clear();
			}
			sentence = reader.readLine();
		}
		return il;
	}
	
	
	private static void addDepInst(InstanceList il, TIntArrayList wordsList)
	{
		il.add(new WordInstance(wordsList.toNativeArray(),il.instanceList.size()));
		il.maxInstanceSize = Math.max(il.maxInstanceSize,wordsList.size());
	
	}
	
	protected static String normalize(String s) {
    	if(s.matches("[0-9]+|[0-9]+\\.[0-9]+|[0-9]+[0-9,]+"))
    		return "<num>";
    	return s;
    }
	
	public String toString(){
		StringBuffer sd = new StringBuffer();
		sd.append(name);
		for(int i = 0; i < instanceList.size(); i++){
			sd.append(i +"-"+instanceList.get(i).toString()+"\n");
		}
		return sd.toString();
	}
	
	public String toStringFirst(int max){
		StringBuffer sd = new StringBuffer();
		sd.append(name);
		for(int i = 0; i < instanceList.size() && i < max ; i++){
			sd.append(i +"-"+instanceList.get(i).toString()+"\n");
		}
		return sd.toString();
	}
}
