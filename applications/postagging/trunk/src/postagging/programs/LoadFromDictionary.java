package postagging.programs;


import gnu.trove.TIntIntHashMap;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
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
	
	public static int[][]  predictUsingDictionary(PosCorpus c, TIntIntHashMap dict, PosInstanceList list){
		ArrayList<WordInstance> test =list.instanceList;
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
	
	
public static String test(int[][] gold, int[][] predictions, int nrStates, PosCorpus corpus, PosInstanceList list,String source){
		
		int[][] words = new int[list.instanceList.size()][];
		for (int i = 0; i < words.length; i++) {
			words[i] = list.instanceList.get(i).words;
		}
		
		StringBuffer res = new StringBuffer();        	
		int[][] mappingCounts = 
			PosMapping.statePosCounts(corpus, Integer.MAX_VALUE, nrStates, predictions,corpus.getNrTags(),gold);

		String cm =PosMapping.printMappingCounts(corpus,mappingCounts, nrStates,
				corpus.getNrTags()); 	
		res.append("CM::"+cm.replace("\n", "\nCM::"));

		res.append("\n");
		int[]  posteriorDecoding1ToManyMapping =  
			postagging.evaluation.PosMapping.manyTo1Mapping(corpus,mappingCounts,
					Integer.MAX_VALUE, nrStates, predictions, corpus.getNrTags(),gold);
		res.append("MANYTo1::"+PosMapping.printMapping(corpus,posteriorDecoding1ToManyMapping));
		int[][] posteriorDecoding1ToMany = PosMapping.mapToTags(posteriorDecoding1ToManyMapping, 
				predictions, Integer.MAX_VALUE);
		res.append("loadPred" + "-"+source+" " + "Posterior UnSupervised 1 to many: " + postagging.evaluation.Evaluator.eval(posteriorDecoding1ToMany, gold)+"\n");	
		res.append("loadPred" + "-"+source+" " + "Posterior UnSupervised 1 to many per bin: " + 
				postagging.evaluation.Evaluator.evaluatePerOccurences(corpus, Integer.MAX_VALUE, 
						words,posteriorDecoding1ToMany,  gold)+"\n");
		int[]  posteriorDecoding1to1Mapping =  
			postagging.evaluation.PosMapping.oneToOnemapping(corpus, mappingCounts,Integer.MAX_VALUE,nrStates, 
					predictions, corpus.getNrTags(),gold);;
			res.append("1To1::"+PosMapping.printMapping(corpus,posteriorDecoding1to1Mapping));
			int[][] posteriorDecoding1to1 =  PosMapping.mapToTags(posteriorDecoding1to1Mapping, 
					predictions, Integer.MAX_VALUE); 
			res.append("loadPred" + "-"+source+" " + "Posterior UnSupervised 1 to 1: " + postagging.evaluation.Evaluator.eval(posteriorDecoding1to1, gold)+"\n");
			res.append("loadPred" + "-"+source+" " + "Posterior UnSupervised 1 to 1 per bin: " + 
					postagging.evaluation.Evaluator.evaluatePerOccurences(corpus,Integer.MAX_VALUE, words,
							posteriorDecoding1to1,  gold)+"\n");

			double[] infometric = PosMapping.informationTheorethicMeasures(mappingCounts,
					nrStates,corpus.getNrTags());
			res.append("loadPred" + "-"+source+" " 
					+ " Posterior E(Tag) " + infometric[0]+
					" E(Gold) " + infometric[1 ] +
					" MI " + infometric[2] +
					" H(Gold |Tag) " + infometric[3] +
					" H(Tag |Gold) " + infometric[4] +
					" VI " + infometric[5] +
					" Homogenity " + infometric[6] + 
					" Completeness " + infometric[7] +
					" V " + infometric[8] +
					" NVI " +  infometric[9] +
			"\n");

			String out = "FINAL::"+res.toString().replace("\n", "\nFINAL::");
			return out;
	}
	
	public static void main(String[] args) throws IOException {
		PosCorpus c = new PosCorpus(args[0]);
		
		TIntIntHashMap dict = loadDictionary(c, args[1]);
		int nrStates = Integer.parseInt(args[2]);
		if(nrStates == -1){
			nrStates = c.getNrTags();
		}
		boolean savePredictions = Boolean.parseBoolean(args[3]);
		String saveDir = args[4];
		String model = args[5];
		PosInstanceList test_list =  (PosInstanceList) c.testInstances.get(0);	
		PosInstanceList train_list =  (PosInstanceList) c.trainInstances;
		int[][] test_predictions = predictUsingDictionary(c,dict,test_list);
		int[][] train_predictions = predictUsingDictionary(c,dict,train_list);
		int[][]  test_gold = new int[test_list.instanceList.size()][];
		for (int i = 0; i < test_gold.length; i++) {
			PosInstance inst = (PosInstance) test_list.instanceList.get(i);
			test_gold[i] = inst.getTags();
		}
		int[][]  train_gold = new int[train_list.instanceList.size()][];
		for (int i = 0; i < train_gold.length; i++) {
			PosInstance inst = (PosInstance) train_list.instanceList.get(i);
			train_gold[i] = inst.getTags();
		}
		
		System.out.println(test(test_gold, test_predictions, nrStates, c,test_list,"test"));
		System.out.println(test(train_gold, train_predictions, nrStates, c,train_list,"train"));
		
		if(savePredictions){
			String description = model+"_"+c.getName();
			//Save predictions
			util.FileSystem.createDir(saveDir);
			PrintStream predictions = InputOutput.openWriter(saveDir+description + "-"+test_list.name+".posteriorDec");
			predictions.print(((PosCorpus)c).printClusters(test_list, test_predictions));
			predictions.close();
			predictions = InputOutput.openWriter(saveDir+description + "-"+train_list.name+".posteriorDec");
			predictions.print(((PosCorpus)c).printClusters(train_list, train_predictions));
			predictions.close();
				
		}
	}
}
