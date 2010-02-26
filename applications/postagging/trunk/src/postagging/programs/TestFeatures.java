package postagging.programs;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



import data.Corpus;
import data.CorpusUtils;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntDoubleHashMap;
import gnu.trove.TIntObjectHashMap;
import postagging.data.PosCorpus;
import postagging.data.PosInstance;
import util.CountAlphabet;
import util.InputOutput;
import util.MathUtil;
import util.Pair;
import util.Sorters;

public class TestFeatures {

	
	static abstract class testWordCriteria{
		abstract boolean test (int wordId, String word);
		abstract String getTestName();
	}
	
	static class testWordCountFeatures extends testWordCriteria{
		TIntDoubleHashMap wordAccum;
		double treshold;
		public testWordCountFeatures(PosCorpus c, double tresh) {
			wordAccum = c.wordAlphabet.getAccumFreqs();// TODO Auto-generated constructor stub
			this.treshold = tresh;
		}
		@Override
		boolean test(int wordId, String word) {
			if(wordAccum.get(wordId) > treshold){
				return true;
			}
			return false;
		}
		
		String getTestName(){
			return "Word count feature";
		}
		
	}
	
	
	public static double[] abstractTestAux(PosCorpus corpus,  testWordCriteria test){
		double[] counts = new double[corpus.getNrTags()];
		double total = 0;
		for(int sentenceNr=0; sentenceNr < corpus.getNrOfTrainingSentences(); sentenceNr++){
			int[] words = corpus.trainInstances.instanceList.get(sentenceNr).words;
			int[] tags = ((PosInstance)corpus.trainInstances.instanceList.get(sentenceNr)).getTags();
			for (int pos = 0; pos < tags.length; pos++) {
				int wordId = words[pos];
				int tag = tags[pos];
				String word = corpus.wordAlphabet.index2feat.get(wordId);
				if(test.test(wordId,word)){
					counts[tag]++;
					total++;
				}
			}
		}
		for (int i = 0; i < counts.length; i++) {
			counts[i]=counts[i]*1.0/total;
		}
		return counts;
	}
	
	public static void abstractTest(PosCorpus corpus,  testWordCriteria test){

		double[] counts = abstractTestAux(corpus, test);
		
		System.out.println(test.getTestName());
		for (int i = 0; i < counts.length; i++) {
			System.out.println(counts[i]*100);
		}
	}
	
	
	static class testWordSizeFeatures extends testWordCriteria{
	
		int size;
		
		String pattern;
		Pattern p;
		CountAlphabet<String> called = new CountAlphabet<String>();
		public testWordSizeFeatures(PosCorpus c, int size) {
			this.size = size;
			p = Pattern.compile("[0-9.,!?:;()\"/\\[\\]'`'-]+");
		}
		@Override
		boolean test(int wordId, String word) {
			Matcher m = p.matcher(word);	
			if(word.length() < size && !m.matches()){
				called.lookupObject(word);
				return true;
			}
			return false;
		}
		
		String getTestName(){
			return "Word size feature";
		}
		
	}

	
	static class testPatternMatchFeature extends testWordCriteria{
		
		String pattern;
		Pattern p;
		CountAlphabet<String> called = new CountAlphabet<String>();
		public testPatternMatchFeature(PosCorpus c, String pattern) {
			 this.pattern = pattern;
			 p = Pattern.compile(pattern);
		}
		@Override
		boolean test(int wordId, String word) {
			Matcher m = p.matcher(word);	
			if(m.matches()){
				called.lookupObject(word);
			//	System.out.println("Have matched "+ pattern + " Word " + word);
				if(word.equals("The")){
					System.out.println("apanhei");
					System.exit(-1);
				}
				return true;
			}
			return false;
		}
		
		String getTestName(){
			return "Patter Match " + pattern;
		}
		
	}
	
	static class testCapitalized extends testWordCriteria{
		
		Corpus c;
		CountAlphabet<String> firstLetterCounts;
		TIntDoubleHashMap wordsAccum;
		CountAlphabet<String> called = new CountAlphabet<String>();
		public testCapitalized(Corpus c){
			firstLetterCounts = new CountAlphabet<String>();
			this.c = c;
			for(int sentenceNr=0; sentenceNr < c.getNrOfTrainingSentences(); sentenceNr++){
				int[] words = c.trainInstances.instanceList.get(sentenceNr).words;
				firstLetterCounts.lookupObject(c.wordAlphabet.index2feat.get(words[0]));
			}
			wordsAccum = c.wordAlphabet.getAccumFreqs();
		}
		
		
		
		@Override
		boolean test(int wordId, String word) {			
			double numberOfTimesFirst = 0;
			if(firstLetterCounts.getCounts(wordId) != -1){
				numberOfTimesFirst = 1.0*firstLetterCounts.getCounts(wordId)
				/(c.wordAlphabet.getCounts(wordId)+c.wordAlphabet.getCounts(c.wordAlphabet.feat2index.get(word.toLowerCase())));
			}
			if( Character.isUpperCase(word.charAt(0)) && numberOfTimesFirst < 0.1 && word.length() > 1 && wordsAccum.get(wordId) < 0.05){
			//	System.out.println("Got " + word + " " + numberOfTimesFirst + " " + wordsAccum.get(wordId));
				
				
				called.lookupObject(word);
				return true;
			}
			return false;
		}
		
		String getTestName(){
			return "Upper case ";
		}
		
	}
	
	static class testSuff extends testWordCriteria{
		
		
		String suff;
		public testSuff(String suff){
			this.suff = suff;
		}
		
		
		
		@Override
		boolean test(int wordId, String word) {			
			if(word.length() > suff.length() && word.endsWith(suff)){
				return true;
			}
			return false;
		}
		
		String getTestName(){
			return "Suffix " + suff;
		}
		
	}
	
	
	
	
	/**
	 * Gets all suffixes and for each suffix gets the entropy of its tag distribution
	 * @param c
	 */
	public static TIntObjectHashMap<double[]>  testSuffixes(PosCorpus c, int minCounts, CountAlphabet<String> suffixes){
		TIntObjectHashMap<double[]> countsPerSuffix = new TIntObjectHashMap<double[]>();
		int nrSuff = suffixes.size();
		for (int j = 0; j < nrSuff; j++) {
			if(suffixes.getCounts(j) > minCounts){
				testSuff test = new testSuff(suffixes.index2feat.get(j));
				double[] freqs = abstractTestAux(c, test);
				countsPerSuffix.put(j, freqs);
			}
		}
		return countsPerSuffix;
	}
	
	public static CountAlphabet<String> createLongetSuffixes(PosCorpus c, int maxLen, int minOccurences){
		//Collect all valid suffixes
		CountAlphabet<String>[] suffixes;
		suffixes = new CountAlphabet[maxLen-1];
		for(int len = 2; len < maxLen+1;len++){
			suffixes[len-2] = CorpusUtils.createTypesSuffixAlphabet(c, len);
		}
		
		CountAlphabet<String> allSufixes = new CountAlphabet<String>();
		//Collects all suffixes
		for(int sentenceNr=0; sentenceNr < c.getNrOfTrainingSentences(); sentenceNr++){
			int[] words = c.trainInstances.instanceList.get(sentenceNr).words;
			for (int wordPos = 0; wordPos < words.length; wordPos++) {
				String word = c.wordAlphabet.index2feat.get(words[wordPos]);
				for(int len = maxLen; len >=2 ; len--){
					if(word.length() > len){
						String sufix = word.substring(word.length()-len);
						if(suffixes[len-2].getCounts(suffixes[len-2].feat2index.get(sufix)) > minOccurences){
							allSufixes.lookupObject(sufix);
						}
					}
				}
			}
		}
		
		return allSufixes;
	}
	
	
	
	
	/**
	 * Computes how many tags are there for 90% of the probability mass of each suffix.
	 * 
	 */
	public static TIntObjectHashMap<TIntArrayList> computeNumberOfTagsPerSuffix(TIntObjectHashMap<double[]> countPerSuffix
			, CountAlphabet<String> suffixesNames,PosCorpus c){
		int nrSuffixes = countPerSuffix.size();
		double threhold = 0.9;
		TIntObjectHashMap<TIntArrayList> suffixesPerTags = new TIntObjectHashMap<TIntArrayList>();
		for(int suffixNr = 0; suffixNr < nrSuffixes; suffixNr++){
			int suffixId = countPerSuffix.keys()[suffixNr];
			double[] counts = countPerSuffix.get(suffixId);
			
			//Create pairs tagId, freq
			ArrayList<Pair> pairs = new ArrayList<Pair>();
			for(int i = 0; i < counts.length; i++){
				pairs.add(new Pair(i,counts[i]));
			}
			Collections.sort(pairs,new Sorters.sortWordsDouble());
			//create pair tag id / accum
			TIntDoubleHashMap accumFreqs = new TIntDoubleHashMap();
			double sum = 0;
			for(Pair p: pairs){
				double freq = (((Double)p._second));
				sum+=freq;
				accumFreqs.put((Integer)p._first, sum);
			}

//			if(suffixesNames.index2feat.get(suffixId).equals("ana")){
//				for(int pos = 0; pos < accumFreqs.size(); pos++){
//					int tagId = accumFreqs.keys()[pos];
//					System.out.println(c.tagAlphabet.index2feat.get(tagId) + " " + accumFreqs.get(tagId));
//				}
//				int nrTags = 0;
//				for (int pos = 0; pos < accumFreqs.size(); pos++) {
//					int tagId = accumFreqs.keys()[pos];
//					if(accumFreqs.get(tagId) < threhold){
//						nrTags++;
//					}
//				}
//				nrTags++;
//				System.out.println("nr tags: " + nrTags);
//				System.exit(-1);
//				
//			}
			int nrTags = 0;
			for (int pos = 0; pos < accumFreqs.size(); pos++) {
				int tagId = accumFreqs.keys()[pos];
				if(accumFreqs.get(tagId) < threhold){
					nrTags++;
				}
			}
			if(nrTags > 0){
				if(!suffixesPerTags.contains(nrTags)){
					suffixesPerTags.put(nrTags, new TIntArrayList());
				}
				suffixesPerTags.get(nrTags).add(suffixId);
			}
		}
		return suffixesPerTags;
	}
	
	
	public static void printSuffixesPerTags(TIntObjectHashMap<TIntArrayList> suffixesPerTags, CountAlphabet<String> suffixesNames){
		for (int i = 0; i < suffixesPerTags.size(); i++) {
			int key = suffixesPerTags.keys()[i];
			System.out.println(key);
			TIntArrayList suffixes = suffixesPerTags.get(key);
			System.out.println("Suffixes with " + key + " tags ");
			for (int j = 0; j < suffixes.size(); j++) {
				System.out.print(suffixesNames.index2feat.get(suffixes.get(j))+":"+suffixesNames.getCounts(suffixes.get(j)) + " ");
			}
			System.out.println( );
		}
	}
	
	
	public static void testContextFeatures(PosCorpus c, String originalCorpusFile, String featureFile){
		
		TIntObjectHashMap<String> originalAlphabet = null;
		TIntDoubleHashMap[] featuresPerWordType = readDataForContextFeatures(c,  originalAlphabet, originalCorpusFile, featureFile);
		

		for(int j = 0; j < 100; j++){
			System.out.println("Word: " + c.wordAlphabet.index2feat.get(j));
			if(featuresPerWordType[j].size() == 0) continue;
 			double[][] topValues = getTopNSimilarWords(j, featuresPerWordType, 10);
 			double[] top = topValues[0];
 			double[] dots = topValues[1];
 			
 			for (int i = 0; i < top.length; i++) {
 				int wordId = (int) top[i];
 				double dot = dots[i];
				System.out.print(wordId + " "+c.wordAlphabet.index2feat.get(wordId)+":"+dot+" ");
			}
			System.out.println();
		}

	}
	
	public static double[] getFeatures(int wordId, TIntDoubleHashMap[] featuresPerWordType){
		double[] features;
		TIntDoubleHashMap featuresAux = featuresPerWordType[wordId];
		features = new double[featuresAux.size()];
		for (int i = 0; i < featuresAux.size(); i++) {
			features[i]=featuresAux.get(featuresAux.keys()[i]);
		}
		return features;
	}
	
	public static double[][] getTopNSimilarWords(int wordId, TIntDoubleHashMap[] featuresPerWordType, int top){
		double[] wordFeatures = getFeatures(wordId, featuresPerWordType);
		ArrayList<Pair> pairs = new ArrayList<Pair>();
		for (int otherWordID = 0; otherWordID < featuresPerWordType.length; otherWordID++) {
			double[] otherFeatures = getFeatures(otherWordID, featuresPerWordType);
//			if(wordId == otherWordID){
//				System.out.println(util.Printing.doubleArrayToString(wordFeatures, null, "original"));
//				System.out.println(util.Printing.doubleArrayToString(otherFeatures, null, "new"));
//				System.out.println("Dot " + MathUtil.dot(wordFeatures, otherFeatures));
//				continue;
//			}
			//System.out.println(otherWordID + " " + featuresPerWordType.length);
	
			//Word without features
			if(otherFeatures.length == 0) continue;

//			double dot = MathUtil.dot(wordFeatures, otherFeatures);
			double dot = 0;
			for (int i = 0; i < otherFeatures.length; i++) {
				dot+=(wordFeatures[i]-otherFeatures[i])*(wordFeatures[i]-otherFeatures[i]);
				
			}
			
			pairs.add(new Pair<Integer, Double>(otherWordID,-dot));
		}
		Collections.sort(pairs,new Sorters.sortWordsDouble());
		double[] topWords = new double[top];
		double[] dots = new double[top];
		for (int i = 0; i < top; i++) {
			topWords[i] = (Integer) pairs.get(i).first();
			dots[i] = (Double) pairs.get(i).second();
		}
		double[][] resutl = new double[2][];
		resutl[0]=topWords;
		resutl[1]=dots;
		return resutl;
	}
	
	
	
//	public static void readCooccurencesMatrix(String file, Corpus c, int nrWords){
//		
//		OpenMapRealMatrix matrix = new OpenMapRealMatrix(c.getNrWordTypes(),nrWords*2);
//		try {
//			
//			BufferedReader reader = InputOutput.openReader(file);
//			String line = reader.readLine();
//			while(line!=null){
//				String[] tokens = line.split(" ");
//				int row = Integer.parseInt(tokens[0]);
//				int column = Integer.parseInt(tokens[1]);
//				double value = Double.parseDouble(tokens[2]);
//				line = reader.readLine();
//			}
//		} catch (UnsupportedEncodingException e1) {
//			System.out.println("Error reading cooccurence file");
//			e1.printStackTrace();
//			System.exit(1);
//		} catch (FileNotFoundException e1) {
//			System.out.println("Error reading cooccurence file");
//			e1.printStackTrace();
//			System.exit(1);
//		} catch (IOException e1) {
//			System.out.println("Error reading cooccurence file");
//			e1.printStackTrace();
//			System.exit(1);
//		}
//		
//	}
	
	public static TIntDoubleHashMap[] readDataForContextFeatures(Corpus c, 
	TIntObjectHashMap<String> originalAlphabet, String originalCorpusFile, String featureFile){
		originalAlphabet = new TIntObjectHashMap<String>();
		int nrWordTypes = c.getNrWordTypes();
		TIntDoubleHashMap[] featuresPerWordType = new TIntDoubleHashMap[nrWordTypes]; 
		for (int i = 0; i < featuresPerWordType.length; i++) {
			featuresPerWordType[i] = new TIntDoubleHashMap();
		}
		try {
			
			BufferedReader reader = InputOutput.openReader(originalCorpusFile);
			String line = reader.readLine();
			while(line!=null){
				String tokens[] = line.split(" ");
				int id = Integer.parseInt(tokens[1]);
				originalAlphabet.put(id,tokens[0]);	
				line = reader.readLine();
			}
		} catch (UnsupportedEncodingException e1) {
			System.out.println("Error reading original vocabulary");
			e1.printStackTrace();
			System.exit(1);
		} catch (FileNotFoundException e1) {
			System.out.println("Error reading original vocabulary");
			e1.printStackTrace();
			System.exit(1);
		} catch (IOException e1) {
			System.out.println("Error reading original vocabulary");
			e1.printStackTrace();
			System.exit(1);
		}
		
		
		
		try {
			
			BufferedReader reader = InputOutput.openReader(featureFile);
			Pattern whitespace = Pattern.compile("\\s+");
			//for each word in the new corpus see if the word exists in the original corpus
			int originalCorpusSize = originalAlphabet.size();
			String featureLine = reader.readLine();
			
			for(int i = 0; i < originalCorpusSize && featureLine!=null; i++){
				
				String word = originalAlphabet.get(i);
//				System.out.println("using word: " + i + "  " + word);
//				System.out.println(featureLine);
				int corpusWordId = c.wordAlphabet.lookupObject(word);
				if(corpusWordId != -1){
					//Word exists then i can read that line add features of that line
					String[] features = whitespace.split(featureLine.trim());
			//		System.out.println("Reading features for word: " + word + " with " + features.length);
					for (int featureId = 0; featureId < features.length; featureId++) {
//						if(featureId < 5){
//							System.out.print(features[featureId]+ ":" );
//						}
//						if(featureId == 5)  System.out.println();

						double feature = Double.parseDouble(features[featureId]);
//						if(featureId < 5){
//							System.out.print(feature+ " ");
//						}
//						if(featureId == 5)  System.out.println();
						featuresPerWordType[corpusWordId].put(featureId, feature); 
					} 
				}
				featureLine = reader.readLine();
//				if(i > 10) System.exit(-1);
			}
		} catch (UnsupportedEncodingException e) {
			System.out.println("Error reading distributional cluster file");
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			System.out.println("Error reading distributional cluster file");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Error reading distributional cluster file");
			e.printStackTrace();
		}
//		System.exit(-1);
		return featuresPerWordType;
	}
	
	public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException, IOException {
		PosCorpus c = new PosCorpus(args[0],0,Integer.MAX_VALUE);
	//	testContextFeatures(c, args[1], args[2]);
		
	
		
//		for(int tag = 0; tag < c.getNrTags(); tag++){
//			System.out.println(c.tagAlphabet.index2feat.get(tag));
//		}

		
	
	
		
//		TestFeatures tf = new TestFeatures();
//		
//		abstractTest(c, new testWordCountFeatures(c,0.9));
//		
//		testWordSizeFeatures t = new testWordSizeFeatures(c,3);
//		abstractTest(c, t);
//		System.out.println(t.called.toString());
////		//Capitalized
//		testCapitalized t = new testCapitalized(c);
//		abstractTest(c, t);
//		System.out.println(t.called.toString());
//		
//		//has htphen
//		abstractTest(c, new testPatternMatchFeature(c,".*-.*"));
//		//Puctuation
//		testPatternMatchFeature p = new testPatternMatchFeature(c,"[.,!?:;()\"/\\[\\]'`'»«-]+");
//		abstractTest(c, p);
//		System.out.println(p.called.toString());
//		//Has underscore
//		abstractTest(c, new testPatternMatchFeature(c,".*_.*"));
//		//Mixed case
//		testPatternMatchFeature p = new testPatternMatchFeature(c,"[A-ZА-ЯÁÉÍÓÚÀÈÌÒÙÃÕÇ]+[a-záéíóúàèìòùаçãõ-я]*[A-ZА-ЯÁÉÍÓÚÀÈÌÒÙÃÕÇ]+[A-Za-zA-ZА-ЯÁÉÍÓÚÀÈÌÒÙÃÕÇáéíóúàèìòùаçãõ-я]+");
//		testPatternMatchFeature p = new testPatternMatchFeature(c,"\\p{javaUpperCase}+\\p{javaLowerCase}*\\p{javaUpperCase}+.*");

		
//		abstractTest(c, p);
//		System.out.println(p.called.toString());
//		//Numbers
		//testPatternMatchFeature p = new testPatternMatchFeature(c,"[0-9]+");
		
		
//		System.out.println("Suffixes len 2\n\n");
//		CountAlphabet<String> suffixes = CorpusUtils.createTypesSuffixAlphabet(c,2);
//		TIntObjectHashMap<double[]> countPerSuffix = testSuffixes(c, 20, suffixes);
//		TIntObjectHashMap<TIntArrayList> suffixesPerTags = 
//		computeNumberOfTagsPerSuffix(countPerSuffix,suffixes,c);
//		printSuffixesPerTags(suffixesPerTags, suffixes);
//		System.out.println("Suffixes len 3\n\n");
//		suffixes = CorpusUtils.createTypesSuffixAlphabet(c,3);
//		countPerSuffix = testSuffixes(c, 20, suffixes);
//		suffixesPerTags = computeNumberOfTagsPerSuffix(countPerSuffix,suffixes,c);
//		printSuffixesPerTags(suffixesPerTags, suffixes);
//		System.out.println("Suffixes len 4\n\n");
//		suffixes = CorpusUtils.createTypesSuffixAlphabet(c,4);
//		countPerSuffix = testSuffixes(c, 20, suffixes);
//		suffixesPerTags = computeNumberOfTagsPerSuffix(countPerSuffix,suffixes,c);
//		printSuffixesPerTags(suffixesPerTags, suffixes);
//		System.out.println("Suffixes len 5\n\n");
//		suffixes = CorpusUtils.createTypesSuffixAlphabet(c,5);
//		countPerSuffix = testSuffixes(c, 20, suffixes);
//		suffixesPerTags = computeNumberOfTagsPerSuffix(countPerSuffix,suffixes,c);
//		printSuffixesPerTags(suffixesPerTags, suffixes);
//		CountAlphabet<String> suffixes = createLongetSuffixes(c, 5, 20);
//		TIntObjectHashMap<double[]> countPerSuffix = testSuffixes(c, 20, suffixes);
//		TIntObjectHashMap<TIntArrayList> suffixesPerTags = 
//			computeNumberOfTagsPerSuffix(countPerSuffix,suffixes,c);
//			printSuffixesPerTags(suffixesPerTags, suffixes);
		
	}
}
