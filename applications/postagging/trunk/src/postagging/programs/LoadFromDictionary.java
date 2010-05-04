package postagging.programs;


import gnu.trove.TIntIntHashMap;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import data.WordInstance;
import postagging.data.PosCorpus;
import postagging.data.PosInstance;
import postagging.data.PosInstanceList;
import postagging.evaluation.PosMapping;
import util.InputOutput;

public class LoadFromDictionary {
	
	
	//Builds a mapping for each word what is the particular clusters it should have
	public static TIntIntHashMap loadDictionary(PosCorpus c, String file) throws UnsupportedEncodingException, FileNotFoundException, IOException{
		TIntIntHashMap dic = new TIntIntHashMap();
		BufferedReader reader = InputOutput.openReader(file);
		String line = reader.readLine();
		Pattern whitespace = Pattern.compile("\\s+");
		while(line != null) {
			if(!line.matches("\\s*")){
				String[] info = whitespace.split(line);
				String word = info[0];
				int cluster = Integer.parseInt(info[1]);
				dic.put(c.wordAlphabet.lookupObject(word), cluster);
			}	
			line = reader.readLine();
		}
		return dic;
	}
	
	public static int[][]  predictUsingDictionary(PosCorpus c, TIntIntHashMap dict){
		ArrayList<WordInstance> test =c.testInstances.get(0).instanceList;
		int[][] predictions = new int[test.size()][];
		for (int i = 0; i < predictions.length; i++) {
			int[] words = test.get(i).words;
			int[] pred = new int[words.length];
			for (int j = 0; j < pred.length; j++) {
				pred[j] = dict.get(words[j]);
			}
			predictions[i] = pred;
		}
		return predictions;
	}
	
	public static void main(String[] args) throws IOException {
		PosCorpus c = new PosCorpus(args[0]);
		TIntIntHashMap dict = loadDictionary(c, args[1]);
		int nrStates = Integer.parseInt(args[2]);
		int[][] predictions = predictUsingDictionary(c,dict);
		int[][]  gold = new int[c.testInstances.get(0).instanceList.size()][];
		for (int i = 0; i < gold.length; i++) {
			PosInstance inst = (PosInstance) c.testInstances.get(0).instanceList.get(i);
			gold[i] = inst.getTags();
		}
		int[][] words = new int[c.testInstances.get(0).instanceList.size()][];
		for (int i = 0; i < words.length; i++) {
			words[i] = c.testInstances.get(0).instanceList.get(i).words;
		}
		StringBuffer res = new StringBuffer();
		int[][] mappingCounts = 
			PosMapping.statePosCounts(c, Integer.MAX_VALUE, nrStates, predictions, c.getNrTags(),gold);
		res.append(PosMapping.printMappingCounts(c,mappingCounts, nrStates,c.getNrTags())+"\n");
		int[]  posteriorDecoding1ToManyMapping =  
			postagging.evaluation.PosMapping.manyTo1Mapping(c,mappingCounts,Integer.MAX_VALUE, nrStates, predictions, c.getNrTags(),gold);
		res.append(PosMapping.printMapping(c,posteriorDecoding1ToManyMapping));
		int[][] posteriorDecoding1ToMany = PosMapping.mapToTags(posteriorDecoding1ToManyMapping, predictions, Integer.MAX_VALUE);
		res.append("Posterior UnSupervised 1 to many" + postagging.evaluation.Evaluator.eval(posteriorDecoding1ToMany, gold)+"\n");	
		res.append("Posterior UnSupervised 1 to many" + 
				postagging.evaluation.Evaluator.evaluatePerOccurences(c, Integer.MAX_VALUE, words,posteriorDecoding1ToMany,  gold)+"\n");
		int[]  posteriorDecoding1to1Mapping =  
			postagging.evaluation.PosMapping.oneToOnemapping(c, mappingCounts,Integer.MAX_VALUE,nrStates, predictions, c.getNrTags(),gold);;
			res.append(PosMapping.printMapping(c,posteriorDecoding1to1Mapping));
		int[][] posteriorDecoding1to1 =  PosMapping.mapToTags(posteriorDecoding1to1Mapping, predictions, Integer.MAX_VALUE); 
			res.append("Posterior UnSupervised 1 to 1" + postagging.evaluation.Evaluator.eval(posteriorDecoding1to1, gold)+"\n");
			res.append("Posterior UnSupervised 1 to 1" + 
					postagging.evaluation.Evaluator.evaluatePerOccurences(c, Integer.MAX_VALUE, words,posteriorDecoding1to1,  gold)+"\n");
			
		
			double[] infometric = PosMapping.informationTheorethicMeasures(mappingCounts,nrStates,c.getNrTags());
		res.append(" Posterior E(Tag) " + infometric[0]+
				" E(Gold) " + infometric[1 ] +
				" MI " + infometric[2] +
				" H(Gold |Tag) " + infometric[3] +
				" H(Tag |Gold) " + infometric[4] +
				" VI " + infometric[5] +
				"\n");
		
		System.out.println(res.toString());
	}
}
