package util.pipes;

import gnu.trove.TIntDoubleHashMap;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

import util.Alphabet;
import util.InputOutput;
import util.SparseVector;
import data.Corpus;

/**
 * Read the distributional clusters after PCA. 
 */
public class DistributionalClustersPipe extends Pipe{
		
	String file;
	TIntDoubleHashMap[] featuresPerWordType;
	public DistributionalClustersPipe(String[] args){
		this(args[0]);
		if (args.length!= 1) throw new IllegalArgumentException("expected 1 arguments got "+args.length);
	}
	
	public DistributionalClustersPipe(String file){
		this.file = file;
	}
	
	public void init(Corpus c){
		this.c = c;
		int nrWordTypes = c.getNrWordTypes();
		featuresPerWordType = new TIntDoubleHashMap[nrWordTypes]; 
		for (int i = 0; i < featuresPerWordType.length; i++) {
			featuresPerWordType[i] = new TIntDoubleHashMap();
		}
		try {
			BufferedReader reader = InputOutput.openReader(file);
			String line = reader.readLine();
//			System.out.println(line);
			Pattern whitespace = Pattern.compile("\\s+");
			int wordId = 0;
			while(line != null) {
				String[] features = whitespace.split(line.trim());
				for (int i = 0; i < features.length; i++) {
	//				System.out.println("feature " + features[i]);
					featuresPerWordType[wordId].put(i, Double.parseDouble(features[i])); 
				} 
				wordId++;
				line = reader.readLine();
			}
			if(wordId != nrWordTypes){
				System.out.println("File does not have the same size: have " + wordId + " truth " + nrWordTypes );
				System.exit(-1);
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
			sv.add(alphabet.lookupObject("dc="+i), features.get(i));
		}
	}
	
	public String getName(){
		return "Distributional cluster: " + file;
	}
}
