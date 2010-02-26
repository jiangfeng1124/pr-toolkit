package util.pipes;

import gnu.trove.TIntDoubleHashMap;
import gnu.trove.TIntObjectHashMap;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

import util.Alphabet;
import util.CountAlphabet;
import util.InputOutput;
import util.SparseVector;
import data.Corpus;

/**
 * Read the distributional clusters after PCA,
 * from a different corpus so maybe some words do not 
 * exit in the original corpus.
 * 
 */
public class DistributionalClustersPipeDiffCorpus extends Pipe{
	
	
	String featureFile,originalCorpusFile;
	TIntDoubleHashMap[] featuresPerWordType;
	public DistributionalClustersPipeDiffCorpus(String[] args){
		this(args[0],args[1]);
		if (args.length!= 2) throw new IllegalArgumentException("expected 2 arguments got "+args.length);
	}
	
	public DistributionalClustersPipeDiffCorpus(String featureFile, String originalCorpusFile){
		this.featureFile = featureFile;
		this.originalCorpusFile = originalCorpusFile;
	}
	
	/**
	 * Reads the mapping word to ids used for a particular corpus
	 * Then reads the features in case that word id exists in the corpus
	 * 
	 */
	public void init(Corpus c){
		this.c = c;
		int nrWordTypes = c.getNrWordTypes();
		featuresPerWordType = new TIntDoubleHashMap[nrWordTypes]; 
		for (int i = 0; i < featuresPerWordType.length; i++) {
			featuresPerWordType[i] = new TIntDoubleHashMap();
		}
		
		TIntObjectHashMap<String> originalAlphabet = new TIntObjectHashMap<String>();
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
					for (int featureId = 0; featureId < features.length; featureId++) {
//						System.out.println("feature " + features[featureId]);
						featuresPerWordType[corpusWordId].put(featureId, Double.parseDouble(features[featureId])); 
					} 
				}
				featureLine = reader.readLine();
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
	}
	
	@Override
	public  void process(int wordId, String word, Alphabet<String> alphabet,SparseVector sv){
		TIntDoubleHashMap features = featuresPerWordType[wordId];
		int[] keys = features.keys();
		for (int i = 0; i < keys.length; i++) {
//			System.out.println("added " + "dc="+i + features.get(i));
			sv.add(alphabet.lookupObject("dc="+i), features.get(i));
		}
	}
	
	public String getName(){
		return "Distributional cluster: " + featureFile;
	}
}
