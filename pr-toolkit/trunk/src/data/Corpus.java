package data;


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
	/**Contains the words alphabet, assigning a single integer for each word type.*/
	public CountAlphabet<String> wordAlphabet;	
	/** List of training sentences */
	public InstanceList trainInstances;
	/** List of development sentences */
	public InstanceList devInstances;

	/** List of list of test sentences for the case we have more than one test corpus*/
	public ArrayList<InstanceList> testInstances;
	
	/** The name of the corpus. */
	String name;
	
	/** Number of word occurrences for a word to be considered unknown, and replaced by the token unknown*/
	int unknownWordsTresh = 0;
	
	/**
	 * If all words of the corpus are to be lowercase or not.
	 * By default the value is false. It can be changed on the corpus parameters
	 */
	boolean lowerCase = false;
	
	/** Minimum length of a sentence to be used */
	int minSentenceLenght = 0;
	
	/** Maximum length of a sentence to be used */
	int maxSentenceLenght = 0;
	
	/** Maximum number of training sentences to be loaded
	 * In the case of transductive sentence the development and test 
	 * sentences are not counted for this number. */
	int maxNrSentences = 0;
	
	/**
	 * Reads a corpus from the corpus description:
	 * 
	 * The corpus description consists of:
	 * name=name_of_the_corpus
	 *
	 * Location of the training corpus
	 * train-name=train-corpus-name
	 * train-file=train-file-location
	 * Location of the development corpus
	 * dev-name=dev-corpus-name
	 * dev-file=dev-corpus-location
	 * Location of the several test corpus, it goes from test_name1... test_nameXXX the same for test_file
	 * test-name1=test-corpus-name
	 * test-file1=test-corpus-location
	 * Indicates the format of the files and how to read them
	 * reader-type= reader_name
	 * 
	 * Corpus loading options:
	 * Lowercase all words, by default false
	 * lowercase=false
	 * Use transductive setting, meaning appending all development and test files to the train set.
	 * Default false
     * transductive=false
	 * Minimum of word occurrences for a word to be used, if the occurrences are smaller than this the word is 
	 * replaced with the token "unk". Default 0 
	 * unknown-words-thresh=0
	 * 
	 * @param corpusParams - The file containing the specification of the corpus
	 * @throws UnsupportedEncodingException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public Corpus(String corpusParams)
	throws UnsupportedEncodingException, FileNotFoundException, IOException {
		this(corpusParams,0,Integer.MAX_VALUE, Integer.MAX_VALUE);
	}
	
	/**
	 * Reads the corpus. See description of corpus file parameters in the method above
	 * @param corpusParams
	 * @param minSentenceLenght - Minimum size of a sentence to be included
	 * @param maxSentenceLenght - Maximum size of a sentence to be included
	 * @param maxNumberOfSentences - Maximum number of training sentences to be added to the corpus.
	 * In case of the tranductive setting the development and test set sentences do not count for this number. 
	 * @throws UnsupportedEncodingException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public Corpus(String corpusParams, int minSentenceLenght, int maxSentenceLenght, int maxNumberOfSentences)
	throws UnsupportedEncodingException, FileNotFoundException, IOException {
		MemoryTracker mem = new MemoryTracker();
		mem.clear();
		mem.start();
		this.minSentenceLenght = minSentenceLenght;
		this.maxSentenceLenght = maxSentenceLenght;
		this.maxNrSentences = maxNumberOfSentences;
		Properties properties = InputOutput.readPropertiesFile(corpusParams);
		this.name = properties.getProperty("name");
		this.wordAlphabet = new CountAlphabet<String>();	
		lowerCase = Boolean.parseBoolean(properties.getProperty("lowercase", "false"));
		if(lowerCase) System.out.println("Loading corpus in lowercase");
		else System.out.println("Loading corpus in truecase");
		initStructures(corpusParams);
		loadFiles(properties);
		printCorpusStats();
		mem.finish();
		System.out.println("Finished loading corpus: Mem: " + mem.print());
	}

	/**
	 * Initialize the corpus structures. 
	 * In this case we just have the count alphabet
	 * @param corpusParams
	 */
	public void initStructures(String corpusParams){
		this.wordAlphabet = new CountAlphabet<String>();	
	}
	
	/**
	 * 
	 * @param properties
	 * @param lowerCase
	 * @param minSentenceLenght
	 * @param maxSentenceLenght
	 * @throws UnsupportedEncodingException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void loadFiles(Properties properties) throws UnsupportedEncodingException, FileNotFoundException, IOException{
		String trainName = properties.getProperty("train-name");
		String trainFile = properties.getProperty("train-file");
		String readerType = properties.getProperty("reader-type");
		String devName = properties.getProperty("dev-name");
		String devFile = properties.getProperty("dev-file");
		unknownWordsTresh = Integer.parseInt(properties.getProperty("unknown-words-thresh","0"));
		System.out.println("Unkwnown Tresh: " + unknownWordsTresh);
		ArrayList<String> testNames = new ArrayList<String>();
		ArrayList<String> testFiles = new ArrayList<String>();
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
		System.out.println("Reading Vocabulary " + trainName + " from:" + trainFile);
		readVocab(trainName, trainFile, readerType, lowerCase, minSentenceLenght, maxSentenceLenght, 
				maxNrSentences,
				fullVocab);
		
		// Stop counting occurrences if non-transductive
		boolean transductive = Boolean.parseBoolean(properties.getProperty("transductive","false"));
		if(transductive){
			if(devFile!= null){
				readVocab(devName, devFile, readerType, lowerCase, minSentenceLenght, maxSentenceLenght, 
						Integer.MAX_VALUE,
						fullVocab);
			}
			for(int i = 0; i < testNames.size(); i++){
				readVocab(testNames.get(i),testFiles.get(i), readerType, lowerCase, minSentenceLenght, maxSentenceLenght, 
						Integer.MAX_VALUE,
						fullVocab);
			}
		}
		fullVocab.compact();
		//Read in the real structures
		this.trainInstances = readInstanceList(trainName, trainFile, 
				readerType,lowerCase, minSentenceLenght,
				maxSentenceLenght,maxNrSentences,fullVocab,this.unknownWordsTresh);
		if(!transductive) {
			stopGrowingAlphabets();
		}
		
		//Dev and test include all possible sizes
		if(devName!=null){
			this.devInstances = readInstanceList(devName, devFile, readerType,
					lowerCase, 0,Integer.MAX_VALUE,
					Integer.MAX_VALUE,
					fullVocab,this.unknownWordsTresh);				
		}
		this.testInstances = new ArrayList<InstanceList>();
		for(int i = 0; i < testFiles.size(); i++){
			this.testInstances.add(
					readInstanceList(testNames.get(i), testFiles.get(i), 
							readerType,lowerCase, 0,
							Integer.MAX_VALUE,
							Integer.MAX_VALUE,
							fullVocab,
							this.unknownWordsTresh));
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
	
	
	/**
	 * Filter to normalize a word, for instance if we wish 
	 * to replace numbers by the token num.
	 * By default does not do nothing, should be defined
	 * for particular applications.
	 * @param s
	 * @return
	 */
	protected static String normalize(String s) {
		return s;
    }
	
	public InstanceList readInstanceList(String name, String fileName, 
			String readerType, boolean lowercase, 
			int minSentenceLenght, int maxSentenceLenght, int maxNrSentences,
			CountAlphabet<String> fullVocab, int minWordOccurs) throws UnsupportedEncodingException, FileNotFoundException, IOException{
		if(readerType.equalsIgnoreCase("posTag-project")){
			return InstanceList.readFromPosTagProject(name, fileName, 
					this.wordAlphabet, lowercase,
					minSentenceLenght,maxSentenceLenght,maxNrSentences,
					fullVocab, minWordOccurs);
		}else if(readerType.equalsIgnoreCase("conll-data")){
			return InstanceList.readFromConll(name, fileName, this.wordAlphabet, 
					lowercase,minSentenceLenght,maxSentenceLenght, maxNrSentences,
					fullVocab,minWordOccurs);
		}else if(readerType.equalsIgnoreCase("europarl")){
			return InstanceList.readFromEuroparl(name, fileName, this.wordAlphabet, 
					lowercase,minSentenceLenght,maxSentenceLenght,maxNrSentences
					,fullVocab,minWordOccurs);
		}else{
			System.out.println("Unknow reader type");
			System.exit(-1);
		}
		return null;
	}
	
	/**
	 * Creates a vocabulary with all words and how many times they occur.
	 * This vocabulary is then used to decide if a word should be unknown or not when building the instances 
	 * list. 
	 * @param name
	 * @param fileName
	 * @param readerType
	 * @param lowercase
	 * @param minSentenceLenght
	 * @param maxSentenceLenght
	 * @param maxNrSentences
	 * @param vocab
	 * @throws UnsupportedEncodingException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void readVocab(String name, String fileName, String readerType, boolean lowercase, 
			int minSentenceLenght, int maxSentenceLenght, int maxNrSentences,
			CountAlphabet<String> vocab) throws UnsupportedEncodingException, FileNotFoundException, IOException{
		if(readerType.equalsIgnoreCase("posTag-project")){
				readVocabPosTagProject(name, fileName, vocab, lowercase,minSentenceLenght,maxSentenceLenght, maxNrSentences);
		}else if(readerType.equalsIgnoreCase("conll-data")){
				readVocabFromConll(name, fileName, vocab, lowercase,minSentenceLenght,maxSentenceLenght, maxNrSentences);
		}else if(readerType.equalsIgnoreCase("europarl")){
				readVocabFromEuroparl(name, fileName, vocab, lowercase,minSentenceLenght,maxSentenceLenght, maxNrSentences);
		}else{
			System.out.println("Unknow reader type");
			System.exit(-1);
		}
	}
	
	
	
	public void readVocabPosTagProject(String name, String fileName, 
			CountAlphabet<String> vocab, boolean lowercase, 
			int minSentenceLenght, int maxSentenceLenght, int maxNrSentences) throws IOException{
		BufferedReader reader = InputOutput.openReader(fileName);		
		Pattern whitespace = Pattern.compile("\\s+");
		ArrayList<String> wordsList  =new ArrayList<String>();	
		String line = reader.readLine();
		int nrSentences = 0;
		while(line != null) {
			if(!line.matches("\\s*")){
				String[] info = whitespace.split(line);
				//normalize word
				String word = normalize(info[1]);
				if(lowercase){
					word = word.toLowerCase();
				}
				wordsList.add(word);
			}
			else { // Case of end of sentence
				int sentenceSize = wordsList.size();
				if(sentenceSize >= minSentenceLenght && sentenceSize <= maxSentenceLenght){
					//Phrase was accepted add all words to vocab
					for(String word: wordsList){
						vocab.lookupObject(word);
					}
				}
				wordsList.clear();
				nrSentences++;
				//Reached maximum number of sentences
				if(nrSentences >= maxNrSentences){
					break;
				}
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
			boolean lowercase, int minSentenceLenght, int maxSentenceLenght, int maxNrSentences) throws IOException{
		BufferedReader reader = InputOutput.openReader(fileName);
	
		Pattern whitespace = Pattern.compile("\\s+");
		ArrayList<String> wordsList  =new ArrayList<String>();	
		String line = reader.readLine();
		int nrSentences = 0;
		while(line != null) {
			if(!line.matches("\\s*")){
				String[] info = whitespace.split(line);
				//Normalize the word in case
				String word = normalize(info[1]);
				if(lowercase){
					word = word.toLowerCase();
				}
				wordsList.add(word);

			}
			else { // Case of end of sentence
				int sentenceSize = wordsList.size();
				if(sentenceSize >= minSentenceLenght && sentenceSize <= maxSentenceLenght){
					for(String word: wordsList){
						vocab.lookupObject(word);
					}
				}
				wordsList.clear();
				nrSentences++;
				//Reached maximum number of sentences
				if(nrSentences >= maxNrSentences){
					break;
				}
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
			boolean lowercase, int minSentenceLenght, int maxSentenceLenght, 
			int maxNrSentences) throws IOException{
		BufferedReader reader = InputOutput.openReader(fileName);
		String sentence = reader.readLine().trim();
		int nrSentences = 0;
		while (sentence != null) {
			String[] tokens = sentence.split(" ");
			int len = tokens.length;
			
			if (len >= minSentenceLenght && len <= maxSentenceLenght) {
				for (int i = 0; i < tokens.length; i++) {
					String word = normalize(tokens[i]);
					if(lowercase){
						word = word.toLowerCase();	
					}
					vocab.lookupObject(word);
				}
			}
			nrSentences ++;
			//If the maximum number of sentences is reached
			if(nrSentences >= maxNrSentences){
				break;
			}
			sentence = reader.readLine();
		}
	}
	
	
	
	
	
	public void stopGrowingAlphabets(){
		// this.wordAlphabet.stopGrowth();
		this.wordAlphabet.setStopCounts(true);
	}
	public void printCorpusStats(){
		System.out.println("Corpus name: " + this.getName());
		System.out.println("Min Sentence Lenght: " + this.minSentenceLenght);
		System.out.println("Max Sentence Lenght: " + this.maxSentenceLenght);
		System.out.println("Max Nr training Sentences: " + this.maxNrSentences);
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
	
	public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException, IOException {
		Corpus c = new Corpus(args[0],0,40,1000);
		
	}
}
