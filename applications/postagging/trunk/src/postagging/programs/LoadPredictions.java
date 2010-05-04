package postagging.programs;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntIntHashMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Pattern;

import postagging.data.PosCorpus;
import postagging.data.PosInstance;
import postagging.evaluation.PosMapping;
import util.InputOutput;

public class LoadPredictions {
	
	
	
	public static int[][] loadPredictions(PosCorpus c, String file) throws IOException{
		int[][] predictions = new int[c.testInstances.get(0).instanceList.size()][];
		BufferedReader reader = InputOutput.openReader(file);
		String line = reader.readLine();
		int nrSentences = 0;
		TIntArrayList sent = new TIntArrayList();
		Pattern whitespace = Pattern.compile("\\s+");
		while(line != null) {
			if(!line.matches("\\s*")){
				String[] info = whitespace.split(line);
				sent.add(Integer.parseInt(info[0]));
			}
			else { // Case of end of sentence
				if(!sent.isEmpty()){
				
				
				int sentenceSize = sent.size();
				int[] tags = sent.toNativeArray();
				sent.clear();
				if(c.testInstances.get(0).instanceList.get(nrSentences).words.length != sentenceSize){
					System.out.println("Sentence nr " + nrSentences + " has an incorrect number of words");
					throw new RuntimeException();
				}
//				System.out.println(util.Printing.intArrayToString(((PosInstance)c.testInstances.get(0).instanceList.get(nrSentences)).getTags(), null, "g:"));
//				System.out.println(util.Printing.intArrayToString(tags, null, "p:"));
				predictions[nrSentences] = tags;
				nrSentences++;
				}
			}
			line = reader.readLine();
		}
		return predictions;
	}
	
	public static void main(String[] args) throws IOException {
		PosCorpus c = new PosCorpus(args[0]);
		int[][] predictions = loadPredictions(c,args[1]);
		int nrStates = Integer.parseInt(args[2]);
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
