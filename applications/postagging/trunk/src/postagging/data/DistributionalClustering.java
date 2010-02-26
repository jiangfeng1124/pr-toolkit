package postagging.data;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;


import org.apache.commons.math.linear.MatrixVisitorException;
import org.apache.commons.math.linear.OpenMapRealMatrix;
import org.apache.commons.math.linear.RealMatrixChangingVisitor;
import org.apache.commons.math.linear.RealMatrixPreservingVisitor;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.linear.SingularValueDecompositionImpl;

import data.Corpus;
import data.WordInstance;


import util.InputOutput;
import util.MathUtil;
import util.Pair;
import util.Sorters;
import util.SparseVector;



import gnu.trove.TIntArrayList;
import gnu.trove.TIntDoubleHashMap;
import gnu.trove.TIntHash;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntObjectHashMap;

/**
 * Calculates a representation of a word given the words that occur near by it.
 * Steps:
 *  1 - Take the top TOP most frequent words.
 *  2 - For each word see how many times each of the top most words occurs to the left and to the right
 *  This will gives us a matrix X of dimensions N*M
 *  Where N is the number of word types
 *  And M is 2*TOP words
 *  3 -  Run PCA on X
 *    - Replace each entry by log(counts +1)
 *    - Make mean zero
 *    - Run SVD X = UDV'
 *    
 *  4 - Use the top x eigen vectors
 * @author javg
 *
 */
public class DistributionalClustering {
	
	/**
	 * Get a list of top most frequent words
	 * @param c
	 * @return
	 */
	public int[] getTopNFrequentWords(Corpus c, int nrWords){
		int[] topWords = new int[nrWords];
		int[] sortedList = c.wordAlphabet.sortedKeyList();
		int j = 0;
		for(int i = sortedList.length-1; i > sortedList.length-nrWords-1; i--){
			topWords[j]=sortedList[i];
			j++;
		}
		return topWords;
	}
	
	/**
	 * Visitor to print every entry of the matrix
	 * @author javg
	 *
	 */
	class PrintVisitor implements RealMatrixChangingVisitor{
		Corpus c;
		int size;
		TIntIntHashMap[] mapping;
		boolean printRows;
		boolean printColumns;
		
		public PrintVisitor(Corpus c, int size, TIntIntHashMap[] mapping, boolean printRows, boolean printColumns){
			this.c = c;
			this.size = size;
			this.mapping = mapping;
			this.printRows = printRows;
			this.printColumns = printColumns;
		}
		
		public PrintVisitor(Corpus c, int size, TIntIntHashMap[] mapping){
			this.c = c;
			this.size = size;
			this.mapping = mapping;
			this.printRows = true;
			this.printColumns = true;
		}
		
		public double end() {
			// TODO Auto-generated method stub
			return 0;
		}

		public void start(int arg0, int arg1, int arg2, int arg3, int arg4,
				int arg5) {
			// TODO Auto-generated method stub
			
		}

		public double visit(int row, int column, double value)
				throws MatrixVisitorException {

//			System.out.println("row:" + row + " size " + size);
//			System.out.println("column:" + column);
			if(column+1 > size){
				
				int contextId = mapping[1].get(column-size);
				if(printColumns && printRows){
					System.out.println("row " + row + " " + c.wordAlphabet.index2feat.get(row) + " R: " 
							+ "column " + column + " " +c.wordAlphabet.index2feat.get(contextId) + " " + value);
				}else if(printColumns){
					System.out.println("row " + row + " R: " 
							+ "column " + column + " " +c.wordAlphabet.index2feat.get(contextId) + " " + value);
				}else if(printRows){
					System.out.println("row " + row + " " + c.wordAlphabet.index2feat.get(row) + " R: " 
							+ "column " + column + " " + value);
				}else{
					System.out.println("row " + row + " R: " 
							+ "column " + column +  " " + value);
				}
			}else{
				int contextId = mapping[1].get(column);
				if(printColumns && printRows){
					System.out.println("row " + row + " " +c.wordAlphabet.index2feat.get(row) + " L: " 
							+ "column " + column + " " +c.wordAlphabet.index2feat.get(contextId) + " " + value);
				}else if(printColumns){
					System.out.println("row " + row + " L: " 
							+ "column " + column + " " +c.wordAlphabet.index2feat.get(contextId) + " " + value);
				}else if(printRows){
					System.out.println("row " + row + " " +c.wordAlphabet.index2feat.get(row) + " L: " 
							+ "column " + column +  " " + value);
				}else{
					System.out.println("row " + row + " L: " 
							+ "column " + column + " " + value);
				}
				
			}
			return value;
		}
		
	}
	
	/**
	 * 
	 * @param c
	 * @param matrix
	 * @param mapping
	 */
	public void print(Corpus c, OpenMapRealMatrix matrix, TIntIntHashMap[] mapping){
		int size = mapping[0].size();
		matrix.walkInOptimizedOrder(new PrintVisitor(c,size,mapping));
		
	}
	
	/**
	 * Runs SVD
	 * @param c
	 * @param matrix
	 * @param k
	 * @param mapping
	 * @throws NotConvergedException
	 */
	public void dimensionalityReduction(Corpus c, 
			OpenMapRealMatrix matrix,int k, TIntIntHashMap[] mapping,PrintStream file) {
		SingularValueDecompositionImpl svd = new SingularValueDecompositionImpl(matrix);
//		System.out.println(svd.getVT().toString());
//		int size = mapping[0].size();
//		svd.getU().walkInOptimizedOrder(new PrintVisitor(c,size,mapping,true,false));
		

		for(int rows = 0; rows < svd.getU().getRowDimension(); rows++){
			//For each word id
			file.print(c.wordAlphabet.index2feat.get(rows)+ " ");
			for(int i = 0; i < k && i < svd.getU().getColumnDimension(); i++){
				file.print(svd.getU().getEntry(rows, i)+ " ");
			}
			file.println();
		}


		
	}
	
	/**
	 * Gets a mapping for the top most used words.
	 * 
	 * @param c
	 * @param topWords
	 * @return
	 */
	public TIntIntHashMap[] getMap(Corpus c, int[] topWords){
		TIntIntHashMap map = new TIntIntHashMap();
		TIntIntHashMap reverseMap = new TIntIntHashMap();
		for (int i = 0; i < topWords.length; i++) {
			if(map.contains(topWords[i])){
				System.out.println("Adding duplicate entry to map: " + c.wordAlphabet.index2feat.get(topWords[i]));
				System.exit(-1);
			}
			map.put(topWords[i], i);
			reverseMap.put(i, topWords[i]);
		}
		TIntIntHashMap[] maps = new TIntIntHashMap[2];
		System.out.println("Created maps with sizes: " + map.size() + " " + reverseMap.size());
		maps[0]=map;
		maps[1]=reverseMap;
		return maps;
	}
	
	
	
	
	/**
	 * Build the co-occurence matrix
	 * @param c
	 * @param maps
	 * @return
	 */
	public OpenMapRealMatrix getCoOccurrences(Corpus c,  TIntIntHashMap[] maps){
		int nrWords = maps[0].size();
		
		
		System.out.println("Creating matrix with: " + c.getNrWordTypes() + " " + nrWords*2);
		OpenMapRealMatrix matrix = new OpenMapRealMatrix(c.getNrWordTypes(),nrWords*2);
		
		//Go for each word in the corpus
		ArrayList<WordInstance> list = c.trainInstances.instanceList;
		for (int sentence = 0; sentence < list.size(); sentence++) {
			int[] words = list.get(sentence).words;
			for(int wordPos = 0; wordPos < words.length; wordPos++){
				//Check both sides
				if(wordPos > 0 && wordPos < words.length-1){
					if(maps[0].contains(words[wordPos-1])){
						int wordId = words[wordPos];
						int prevWordId = words[wordPos-1];
						int matrixPosition = maps[0].get(prevWordId);
//						if(c.wordAlphabet.index2feat.get(wordId).equals("waterways")){
//							System.out.println("Word: " + c.wordAlphabet.index2feat.get(wordId));
//							System.out.println("Prev Context: " + c.wordAlphabet.index2feat.get(prevWordId));
//							System.out.println("Adding word at position " + wordId + " " + matrixPosition);
//						}
						matrix.addToEntry(words[wordPos], matrixPosition, 1);
					}
					if(maps[0].contains(words[wordPos+1])){
						
						int wordId = words[wordPos];
						int nextWordId = words[wordPos+1];
						int matrixPosition = maps[0].get(nextWordId)+nrWords;
//						if(c.wordAlphabet.index2feat.get(wordId).equals("waterways")){
//							System.out.println("Word: " + c.wordAlphabet.index2feat.get(wordId));
//							System.out.println("Next Context: " + c.wordAlphabet.index2feat.get(nextWordId));
//							System.out.println("Adding word at position " + wordId + " " + matrixPosition);
//						}
						 
						
						matrix.addToEntry(wordId, matrixPosition, 1);
					}
				}
				//Check left side
				else if(wordPos > 0){
					if(maps[0].contains(words[wordPos-1])){
						int wordId = words[wordPos];
						int prevWordId = words[wordPos-1];
						int matrixPosition = maps[0].get(prevWordId);
//						if(c.wordAlphabet.index2feat.get(wordId).equals("waterways")){
//						System.out.println("Word: " + c.wordAlphabet.index2feat.get(wordId));
//						System.out.println("Prev Context: " + c.wordAlphabet.index2feat.get(prevWordId));
//						System.out.println("Adding word at position " + wordId + " " + matrixPosition);
//						}
						matrix.addToEntry(wordId, matrixPosition, 1);
						}
				}
				//check righ side
				else if(wordPos < words.length-1){
					if(maps[0].contains(words[wordPos+1])){
						int wordId = words[wordPos];
						int nextWordId = words[wordPos+1];
						int matrixPosition = maps[0].get(nextWordId)+nrWords; 
//						if(c.wordAlphabet.index2feat.get(wordId).equals("waterways")){
//						System.out.println("Word: " + c.wordAlphabet.index2feat.get(wordId));
//						System.out.println("Next Context: " + c.wordAlphabet.index2feat.get(nextWordId));
//						System.out.println("Adding word at position " + wordId + " " + matrixPosition);
//						}
						
						matrix.addToEntry(wordId, matrixPosition, 1);
					}
				}
			}
		}
		
		
		
		//Make each entry log(counts+1)
		matrix.walkInOptimizedOrder(new LogCountsVisitor());
		
		//Normalize the counts to sum to one
		for(int i =0; i < c.getNrWordTypes(); i++){
			RealVector vector = matrix.getRowVector(i);
			
			double sum = 0;
			double []row= matrix.getRow(i);
//			System.out.println(vector.toString());
			for (int j = 0; j < row.length; j++) {
				sum+=row[j]*row[j];
			}
			if(sum != 0){
				//System.out.println("Vector sum" + sum + " : " + c.wordAlphabet.getCounts(i));
				vector.mapDivideToSelf(Math.sqrt(sum));
			}
//			System.out.println(vector.toString());
			matrix.setRowVector(i,vector);
		}
		
		//Sanity check
//		for(int i =0; i < c.getNrWordTypes(); i++){
//			RealVector vector = matrix.getRowVector(i);
//			double dot = vector.dotProduct(vector);
//			System.out.println("vector for dot" + vector.toString());
//			if(Math.abs(dot -1) > 1.E-4){
//				System.out.println("Error dot is not 1: " + dot);
//				System.exit(-1);
//			}
//		}
		
		
		//Make mean zero
//		for(int i =0; i < nrWords*2; i++){
//			RealVector vector = matrix.getColumnVector(i);
//			double mean = MathUtil.sum(matrix.getColumn(i))/c.getNrWordTypes();
//			vector.mapSubtractToSelf(mean);
//			matrix.setColumnVector(i, vector);
//		}
		
		return matrix;
	}
	
	
	/**
	 * Saves the matrix to matlab format. 
	 * The format is:
	 * 
	 * @param file
	 * @param matrix
	 */
	public void toMatlabFormat(PrintStream file, OpenMapRealMatrix matrix){
		matrix.walkInOptimizedOrder(new ToMatlabFileVisitor(file));
	}
	
	
	
	
	public void printTopContexts(PosCorpus c, OpenMapRealMatrix matrix,TIntIntHashMap[] maps){
		int nrWordTypes = 100;
		
		int[][] mostOccuringWords = new int[nrWordTypes][];
		for(int wordType = 0; wordType < nrWordTypes; wordType++){
//			if(!c.wordAlphabet.index2feat.get(wordType).equals("waterways")) continue;
			double[] vector =  matrix.getRow(wordType);
			ArrayList<Pair<Integer,Double>> pairs = new ArrayList<Pair<Integer,Double>> ();
			
			for(int contextWordId = 0; contextWordId < maps[0].size(); contextWordId++ ){
				double leftValue = vector[contextWordId];
				double rightValue = vector[contextWordId+maps[0].size()];
				if(leftValue + rightValue <=0){
//					System.out.println("Skyping " + " "+c.wordAlphabet.index2feat.get(maps[1].get(contextWordId)));
					continue;
				}
//				System.out.println("adding word "+ contextWordId + " " + maps[1].get(contextWordId) 
//						+ " "+c.wordAlphabet.index2feat.get(maps[1].get(contextWordId))+" to sort: L: " 
//						+ leftValue + " R: " + rightValue+ " T: " +  (rightValue + leftValue) );
				//Counts to the left + right
				pairs.add(new Pair<Integer, Double>(maps[1].get(contextWordId),rightValue+leftValue));	
			}
			Collections.sort(pairs,new Sorters.sortWordsDouble());
			int[] wordIds = new int[10];
			java.util.Arrays.fill(wordIds, -1);
//			System.out.println("List size " + pairs.size());
			for(int contextWordId = 0; contextWordId < 10 && contextWordId < pairs.size(); contextWordId++){
//				System.out.println("Adding wordId " +  pairs.get(contextWordId).first());
				wordIds[contextWordId] = pairs.get(contextWordId).first();
			}
			mostOccuringWords[wordType]=wordIds;
		}
		TIntObjectHashMap<HashSet<Integer>> tagsPerWord = c.getTagPerWord();
		for(int wordId = 0; wordId < nrWordTypes; wordId++){
//			if(!c.wordAlphabet.index2feat.get(wordId).equals("waterways")) continue;
			int[] wordIds = mostOccuringWords[wordId];
			System.out.println("Word Analysis: " + c.wordAlphabet.index2feat.get(wordId));
 			for (int contextWordId = 0; contextWordId < wordIds.length && wordIds[contextWordId] != -1; contextWordId++) {
				StringBuffer wordAndTags = new StringBuffer();
				wordAndTags.append(c.wordAlphabet.index2feat.get(wordIds[contextWordId])+ " (");
				HashSet<Integer> tags = tagsPerWord.get(contextWordId);
				Iterator iter = tags.iterator();
				while(iter.hasNext()){
					wordAndTags.append(iter.next()+",");
				}
				wordAndTags.append(") ");
				System.out.print(wordAndTags.toString());
			}
			System.out.println();
		}
		
	}
	
	public void printTopClosesWords(PosCorpus c, OpenMapRealMatrix matrix,TIntIntHashMap[] maps){
		int nrWordTypes = Math.min(5000,c.getNrWordTypes());
		TIntObjectHashMap<HashSet<Integer>> tagsPerWord = c.getTagPerWord();
		for(int wordType = 0; wordType < nrWordTypes; wordType++){
			
			//Don't print words that are part of the topX
		//	if(maps[0].contains(wordType)) continue;
			
			RealVector original = matrix.getRowVector(wordType);
			ArrayList<Pair<Integer,Double>> pairs = new ArrayList<Pair<Integer,Double>> ();
			for(int toCompare = 0; toCompare < c.getNrWordTypes(); toCompare++){
//				if(toCompare == wordType){
//					System.out.println("original " + original.toString());
//					System.out.println("tocompare " + matrix.getRowVector(toCompare).toString());
//					double dot = original.dotProduct(matrix.getRowVector(toCompare));
//					System.out.println("dot " + dot);
//					//continue;
//				}
				double dot = original.dotProduct(matrix.getRowVector(toCompare));
				pairs.add(new Pair<Integer, Double>(toCompare,dot));
			}
			Collections.sort(pairs,new Sorters.sortWordsDouble());
			int[] wordIds = new int[10];
			double[] dots = new double[10];
			java.util.Arrays.fill(wordIds, -1);
//			System.out.println("List size " + pairs.size());
			for(int contextWordId = 0; contextWordId < 10 && contextWordId < pairs.size(); contextWordId++){
//				System.out.println("Adding wordId " +  pairs.get(contextWordId).first());
				wordIds[contextWordId] = pairs.get(contextWordId).first();
				dots[contextWordId] = pairs.get(contextWordId).second();
			}
			StringBuffer wordAndTags = new StringBuffer();
			wordAndTags.append("original " + c.wordAlphabet.index2feat.get(wordType)+ " (");
			HashSet<Integer> tags = tagsPerWord.get(wordType);
			Iterator iter = tags.iterator();
			while(iter.hasNext()){
				wordAndTags.append(c.tagAlphabet.index2feat.get((Integer)iter.next())+",");
			}
			wordAndTags.append(") ");
			System.out.println(wordAndTags.toString());
//			System.out.println("Word " +c.wordAlphabet.index2feat.get(wordType) );
			for (int i = 0; i < wordIds.length; i++) {
				wordAndTags = new StringBuffer();
				wordAndTags.append(c.wordAlphabet.index2feat.get(wordIds[i])+ ":" + dots[i] + " (");
				tags = tagsPerWord.get(wordIds[i]);
				iter = tags.iterator();
				while(iter.hasNext()){
					wordAndTags.append(c.tagAlphabet.index2feat.get((Integer)iter.next())+",");
				}
				wordAndTags.append(") ");
				System.out.print(wordAndTags.toString());
			}
			System.out.println();
		}
	}
	
	public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException, IOException {
		Corpus c = new Corpus(args[0]);
		DistributionalClustering dc = new DistributionalClustering();
		int topNWords = Integer.parseInt(args[1]);
		int[] top = dc.getTopNFrequentWords(c,topNWords);
		String[] words= c.getWordStrings(top);
		String baseDir = args[2];
		baseDir = baseDir+"/"+c.getName()+"/"+topNWords+"/";
		util.FileSystem.createDir(baseDir);
		PrintStream corpusFile = InputOutput.openWriter(baseDir+"/"+"corpus.gz");
		corpusFile.print(c.wordAlphabet.toString());
		corpusFile.close();
		PrintStream topWords = InputOutput.openWriter(baseDir+"/"+"topWords.txt");
		for (int i = 0; i < words.length; i++) {
			//System.out.println(i+" "+ words[i]);
			topWords.println(i+" "+ words[i]);
		}
		topWords.close();

		System.out.println("ended top words");
		TIntIntHashMap[] maps = dc.getMap(c,top);
		System.out.println("Number of contexts" + topNWords*2);
		System.out.println("Number of words" + c.getNrWordTypes());
		OpenMapRealMatrix matrix = dc.getCoOccurrences(c, maps);
//		double[] column  = matrix.getRow(1);
//		System.out.println("column size " + column.length);
//		for (int i = 0; i < 100; i++) {
//			
//			if(column[i]!=0){
//				System.out.println(i+":"+column[i]);
//			}
//		}
//		System.exit(-1);
		PrintStream file = InputOutput.openWriter(baseDir+"/"+"matrix.txt");
		dc.toMatlabFormat(file, matrix);
//		dc.printTopContexts(c, matrix,maps);
//		if(c.getName().startsWith("pt")){
//			dc.printTopClosesWords(c, matrix,maps);
//		}
	}
	
	
	
	//Add plus one to each entry in the table and takes its log
	class LogCountsVisitor implements RealMatrixChangingVisitor{
		public double end() {
			// TODO Auto-generated method stub
			return 0;
		}

		public void start(int arg0, int arg1, int arg2, int arg3, int arg4,
				int arg5) {
			// TODO Auto-generated method stub
			
		}

		public double visit(int row, int column, double value)
				throws MatrixVisitorException {
			return Math.log(value+1);
		}
	}
	

	//Gets the mean of all entries
	class GetMeanVisitor implements RealMatrixChangingVisitor{
		double mean;
		int numberOfElements;
		
		public double end() {
			mean = mean/numberOfElements;
			return 0;
			
		}

		public void start(int arg0, int arg1, int arg2, int arg3, int arg4,
				int arg5) {
			mean = 0;
			numberOfElements = 0;
			
		}

		public double visit(int row, int column, double value)
				throws MatrixVisitorException {
			mean+=value;
			numberOfElements++;
			return value;
		}
	}
	
	//Adds a number to all entries
	class AddToEveryVisitor implements RealMatrixChangingVisitor{
		double valueToAdd;
		
		
		public AddToEveryVisitor(double value){
			this.valueToAdd = value;
		}
		
		public double end() {
		
			return 0;
			
		}

		public void start(int arg0, int arg1, int arg2, int arg3, int arg4,
				int arg5) {
		
			
		}

		public double visit(int row, int column, double value)
				throws MatrixVisitorException {
			if(value != 0){
				return value+valueToAdd;
			}
			return value;
		}
	}

	/**
	 * Prints a matric to matlab format
	 * Column Row Value
	 * @author javg
	 *
	 */
	class ToMatlabFileVisitor implements RealMatrixPreservingVisitor{
		
		public PrintStream outputStream;
		
		public ToMatlabFileVisitor(PrintStream outputStream){
			this.outputStream=outputStream;
		}
		
		public double end() {
			// TODO Auto-generated method stub
			return 0;
		}

		public void start(int arg0, int arg1, int arg2, int arg3, int arg4,
				int arg5) {
			// TODO Auto-generated method stub
			
		}

		public void visit(int row, int column, double value)
				throws MatrixVisitorException {
			if(value != 0){
				//Matlab starts at one
				outputStream.println((column+1)+" "+(1+row)+" "+value);
			}
		}
		
	}
	
}
