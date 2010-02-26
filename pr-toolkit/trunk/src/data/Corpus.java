package data;

import gnu.trove.TIntArrayList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.regex.Pattern;

import util.CountAlphabet;
import util.InputOutput;
import util.MemoryTracker;


/**
 * Base corpus. 
 * Contains a set of sentences. And no annotations information.
 * 
 * @author javg
 *
 */
public  class Corpus {
	public CountAlphabet<String> wordAlphabet;	
	// Train instances data
	public InstanceList trainInstances;
	// Devel instances data
	public InstanceList devInstances;
	// Test instances data
	public ArrayList<InstanceList> testInstances;
	String name;
	int unknownWordsTresh = 0;
	
	public Corpus(String corpusParams)
	throws UnsupportedEncodingException, FileNotFoundException, IOException {
		this(corpusParams,0,Integer.MAX_VALUE);
	}
	
	public InstanceList readInstanceList(String name, String fileName, 
			String readerType, boolean lowercase, 
			int minSentenceLenght, int maxSentenceLenght,
			CountAlphabet<String> fullVocab, int minWordOccurs) throws UnsupportedEncodingException, FileNotFoundException, IOException{
		if(readerType.equalsIgnoreCase("posTag-project")){
			return InstanceList.readFromPosTagProject(name, fileName, 
					this.wordAlphabet, lowercase,
					minSentenceLenght,maxSentenceLenght,
					fullVocab, minWordOccurs);
		}else if(readerType.equalsIgnoreCase("conll-data")){
			return InstanceList.readFromConll(name, fileName, this.wordAlphabet, 
					lowercase,minSentenceLenght,maxSentenceLenght
					,fullVocab,minWordOccurs);
		}else if(readerType.equalsIgnoreCase("europarl")){
			return InstanceList.readFromEuroparl(name, fileName, this.wordAlphabet, 
					lowercase,minSentenceLenght,maxSentenceLenght
					,fullVocab,minWordOccurs);
		}else{
			System.out.println("Unknow reader type");
			System.exit(-1);
		}
		return null;
	}
	
	public void readVocab(String name, String fileName, String readerType, boolean lowercase, 
			int minSentenceLenght, int maxSentenceLenght, 
			CountAlphabet<String> vocab) throws UnsupportedEncodingException, FileNotFoundException, IOException{
		if(readerType.equalsIgnoreCase("posTag-project")){
				readVocabPosTagProject(name, fileName, vocab, lowercase,minSentenceLenght,maxSentenceLenght);
		}else if(readerType.equalsIgnoreCase("conll-data")){
				readVocabFromConll(name, fileName, vocab, lowercase,minSentenceLenght,maxSentenceLenght);
		}else if(readerType.equalsIgnoreCase("europarl")){
				readVocabFromEuroparl(name, fileName, vocab, lowercase,minSentenceLenght,maxSentenceLenght);
		}else{
			System.out.println("Unknow reader type");
			System.exit(-1);
		}
	}
	
	protected static String normalize(String s) {
    	if(s.matches("[0-9]+|[0-9]+\\.[0-9]+|[0-9]+[0-9,]+"))
    		return "<num>";
    	return s;
    }
	
	public void readVocabPosTagProject(String name, String fileName, 
			CountAlphabet<String> vocab, boolean lowercase, 
			int minSentenceLenght, int maxSentenceLenght) throws IOException{
		BufferedReader reader = InputOutput.openReader(fileName);		
		Pattern whitespace = Pattern.compile("\\s+");
		ArrayList<String> wordsList  =new ArrayList<String>();	
		String line = reader.readLine();
		while(line != null) {
			if(!line.matches("\\s*")){
				String[] info = whitespace.split(line);
			//	String word = normalize(info[1]);
				String word = info[1];
				if(lowercase){
					word = word.toLowerCase();
				}
				wordsList.add(word);
			}
			else { // Case of end of sentence
				int sentenceSize = wordsList.size();
				if(sentenceSize > minSentenceLenght && sentenceSize <= maxSentenceLenght){
					//Phrase was accepted add all words to vocab
					for(String word: wordsList){
						vocab.lookupObject(word);
					}
				}
				wordsList.clear();
			}
			line = reader.readLine();
		}	
		if(wordsList.size() > 0){
			for(String word: wordsList){
				vocab.lookupObject(word);
			}
		}
	}
	
	public void readVocabFromConll(String name, String fileName, CountAlphabet<String> vocab, 
			boolean lowercase, int minSentenceLenght, int maxSentenceLenght) throws IOException{
		BufferedReader reader = InputOutput.openReader(fileName);
	
		Pattern whitespace = Pattern.compile("\\s+");
		ArrayList<String> wordsList  =new ArrayList<String>();	
		String line = reader.readLine();
		while(line != null) {
			if(!line.matches("\\s*")){
				String[] info = whitespace.split(line);
				String word = info[1];
			//	String word = normalize(info[1]);
				if(lowercase){
					word = word.toLowerCase();
				}
				wordsList.add(word);

			}
			else { // Case of end of sentence
				int sentenceSize = wordsList.size();
				if(sentenceSize > minSentenceLenght && sentenceSize <= maxSentenceLenght){
					for(String word: wordsList){
						vocab.lookupObject(word);
					}
				}
				wordsList.clear();
			}
			line = reader.readLine();
		}	
		// Add final dependency instance
		// (need this in case file ends without a trailing newline)
		if(wordsList.size() > 0){
			for(String word: wordsList){
				vocab.lookupObject(word);
			}
		}
	}
	
	public void readVocabFromEuroparl(String name, String fileName, CountAlphabet<String> vocab, 
			boolean lowercase, int minSentenceLenght, int maxSentenceLenght) throws IOException{
		BufferedReader reader = InputOutput.openReader(fileName);
		String sentence = reader.readLine();
		while (sentence != null) {
			String[] tokens = sentence.split(" ");
			int len = tokens.length;
			
			if (len <= maxSentenceLenght && len <= maxSentenceLenght) {
				for (int i = 0; i < tokens.length; i++) {
					String word = tokens[i];
					if(lowercase){
						word = word.toLowerCase();	
					}
					vocab.lookupObject(word);
				}
			}
			sentence = reader.readLine();
		}
	}
	
	public void initStructures(String corpusParams){
		this.wordAlphabet = new CountAlphabet<String>();	
	}
	public void loadFiles(Properties properties, boolean lowerCase,int minSentenceLenght, int maxSentenceLenght) throws UnsupportedEncodingException, FileNotFoundException, IOException{
		String trainName = properties.getProperty("train-name");
		String trainFile = properties.getProperty("train-file");
		String readerType = properties.getProperty("reader-type");
		String devName = properties.getProperty("dev-name");
		String devFile = properties.getProperty("dev-file");
		unknownWordsTresh = Integer.parseInt(properties.getProperty("unknown-words-thresh","0"));
		System.out.println("Unkwnown Tresh: " + unknownWordsTresh);
		ArrayList<String> testNames = new ArrayList<String>();
		ArrayList<String> testFiles = new ArrayList<String>();
		// Stop counting occurrences if non-transductive
		boolean transductive = Boolean.parseBoolean(properties.getProperty("transductive","false"));
		for(int i = 1; true; i++){
			String testName = properties.getProperty("test-name"+i);
			String testFile = properties.getProperty("test-file"+i);
			if(testName!=null){
				testNames.add(testName);
				testFiles.add(testFile);
			}else{
				break;
			}
		}
		//Read full vocab to use to determine unknown words
		CountAlphabet<String>  fullVocab = new CountAlphabet<String> ();
		readVocab(trainName, trainFile, readerType, lowerCase, minSentenceLenght, maxSentenceLenght, fullVocab);
		if(transductive){
			if(devFile!= null){
				readVocab(devName, devFile, readerType, lowerCase, minSentenceLenght, maxSentenceLenght, fullVocab);
			}
			for(int i = 0; i < testNames.size(); i++){
				readVocab(testNames.get(i),testFiles.get(i), readerType, lowerCase, minSentenceLenght, maxSentenceLenght, fullVocab);
			}
		}
		
		//Read in the real structures
		this.trainInstances = readInstanceList(trainName, trainFile, 
				readerType,lowerCase, minSentenceLenght,
				maxSentenceLenght,fullVocab,this.unknownWordsTresh);
		if(!transductive) {
			stopGrowingAlphabets();
		}
		
		//Dev and test include all possible sizes
		if(devName!=null){
			this.devInstances = readInstanceList(devName, devFile, readerType,
					lowerCase, 0,Integer.MAX_VALUE,
					fullVocab,this.unknownWordsTresh);				
		}
		this.testInstances = new ArrayList<InstanceList>();
		for(int i = 0; i < testFiles.size(); i++){
			this.testInstances.add(
					readInstanceList(testNames.get(i), testFiles.get(i), 
							readerType,lowerCase, 0,
							Integer.MAX_VALUE,fullVocab
							,this.unknownWordsTresh));
		}
		
		// Stop growing alphabets
		stopGrowingAlphabets();
		
		if(transductive){
			System.out.println("Transductive setting add dev and test");
			if(this.devInstances != null){
				this.trainInstances.add(this.devInstances,minSentenceLenght,maxSentenceLenght);
			}
			if(this.testInstances.size() > 0){
				for(int i =0; i < this.testInstances.size(); i++){
					this.trainInstances.add(this.testInstances.get(i),minSentenceLenght,maxSentenceLenght);
				}
			}
		}
		
		System.out.println("Used " + (this.wordAlphabet.size()*100.0/fullVocab.size()) + " non rare words");
	}
	
	
	public Corpus(String corpusParams, int minSentenceLenght, int maxSentenceLenght)
	throws UnsupportedEncodingException, FileNotFoundException, IOException {
		MemoryTracker mem = new MemoryTracker();
		mem.clear();
		mem.start();
		Properties properties = InputOutput.readPropertiesFile(corpusParams);
		this.name = properties.getProperty("name");
		this.wordAlphabet = new CountAlphabet<String>();	
		boolean lowerCase = Boolean.parseBoolean(properties.getProperty("lowercase", "false"));
		if(lowerCase) System.out.println("Loading corpus in lowercase");
		else System.out.println("Loading corpus in truecase");
		initStructures(corpusParams);
		loadFiles(properties, lowerCase, minSentenceLenght, maxSentenceLenght);
		printCorpusStats();
		mem.finish();
		System.out.println("Finished loading corpus: Mem: " + mem.print());
		

	}

	public void stopGrowingAlphabets(){
		// this.wordAlphabet.stopGrowth();
		this.wordAlphabet.setStopCounts(true);
	}
	public void printCorpusStats(){
		System.out.println("Corpus name: " + this.getName());
		
		System.out.println("Number of word types: " + this.getNrWordTypes());
		System.out.println("Number of word tokens: " + this.getNumberOfTokens());	
		System.out.println("Max sentence Length on training: " + trainInstances.maxInstanceSize);
		System.out.println("Number of sentences on training: " + this.getNrOfTrainingSentences());
		if(devInstances != null){
			System.out.println("Number of sentences on developmet: " + this.devInstances.instanceList.size());
		}
		if(testInstances != null){
			for(int i = 0; i < testInstances.size(); i++){
				System.out.println("Number of sentences on test "+i+" : " + testInstances.get(i).instanceList.size());
			}
		}
	}
	
	public String getName(){
		return name;
	}
	
	public int getNrWordTypes(){
		return wordAlphabet.size();
	}
	
	public int getNumberOfTokens(){
		return wordAlphabet.getNumberElements();
	}
	
	public int getWordTypeCounts(int wordType){
		return wordAlphabet.getCounts(wordType);
	}
	
	public int getNrOfTrainingSentences(){
		return trainInstances.instanceList.size();
	}
	
	public String[] getWordStrings(int[] wordId){
		String[] words = new String[wordId.length];
		for (int i = 0; i < words.length; i++) {
			words[i] = wordAlphabet.index2feat.get(wordId[i]);
		}
		return words;
	}
	
	public void saveDescription(String directory){
		try {
			PrintStream f = new PrintStream(directory + "corpus-description");
			f.println(name);
			

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("ERROR SAVING CORPUS DESCRIPTION");
			System.exit(1);
		}
	}
	
	public boolean checkDescription(String directory) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(directory
					+ File.separator+"corpus-description"));

			String name = in.readLine();
			if (!name.equalsIgnoreCase(name)) {
				System.out.println("Corpus name is not the same, got:" + name
						+ " have " + name);
				return false;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("ERROR LOADING CORPUS DESCRIPTION");
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("ERROR LOADING CORPUS DESCRIPTION");
			System.exit(1);
		}
		return true;
	}
	
	
}
