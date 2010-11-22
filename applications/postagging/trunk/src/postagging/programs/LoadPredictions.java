package postagging.programs;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntIntHashMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.regex.Pattern;

import model.chain.PosteriorDecoder;

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
	
	public static String test(int[][] gold, int[][] predictions, int nrStates, PosCorpus corpus){
		
		int[][] words = new int[corpus.testInstances.get(0).instanceList.size()][];
		for (int i = 0; i < words.length; i++) {
			words[i] = corpus.testInstances.get(0).instanceList.get(i).words;
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
		res.append("loadPred" + "-"+"test"+" " + "Posterior UnSupervised 1 to many: " + postagging.evaluation.Evaluator.eval(posteriorDecoding1ToMany, gold)+"\n");	
		res.append("loadPred" + "-"+"test"+" " + "Posterior UnSupervised 1 to many per bin: " + 
				postagging.evaluation.Evaluator.evaluatePerOccurences(corpus, Integer.MAX_VALUE, 
						words,posteriorDecoding1ToMany,  gold)+"\n");
		int[]  posteriorDecoding1to1Mapping =  
			postagging.evaluation.PosMapping.oneToOnemapping(corpus, mappingCounts,Integer.MAX_VALUE,nrStates, 
					predictions, corpus.getNrTags(),gold);;
			res.append("1To1::"+PosMapping.printMapping(corpus,posteriorDecoding1to1Mapping));
			int[][] posteriorDecoding1to1 =  PosMapping.mapToTags(posteriorDecoding1to1Mapping, 
					predictions, Integer.MAX_VALUE); 
			res.append("loadPred" + "-"+"test"+" " + "Posterior UnSupervised 1 to 1: " + postagging.evaluation.Evaluator.eval(posteriorDecoding1to1, gold)+"\n");
			res.append("loadPred" + "-"+"test"+" " + "Posterior UnSupervised 1 to 1 per bin: " + 
					postagging.evaluation.Evaluator.evaluatePerOccurences(corpus,Integer.MAX_VALUE, words,
							posteriorDecoding1to1,  gold)+"\n");

			double[] infometric = PosMapping.informationTheorethicMeasures(mappingCounts,
					nrStates,corpus.getNrTags());
			res.append("loadPred" + "-"+"test"+" " 
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
		int[][] predictions = loadPredictions(c,args[1]);
		int nrStates = Integer.parseInt(args[2]);
		int[][]  gold = new int[c.testInstances.get(0).instanceList.size()][];
		for (int i = 0; i < gold.length; i++) {
			PosInstance inst = (PosInstance) c.testInstances.get(0).instanceList.get(i);
			gold[i] = inst.getTags();
		}
		
		System.out.println(test(gold, predictions, nrStates, c));
	}
	
}
