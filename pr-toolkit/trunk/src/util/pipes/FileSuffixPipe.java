package util.pipes;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import util.Alphabet;
import util.InputOutput;
import util.SparseVector;
import data.Corpus;

/**
 *  Reads suffixes from a file and adds them as features to the words that contain that suffix
 *
 */
public class FileSuffixPipe extends Pipe{
	int minLen;
	String suffixFile;
	Alphabet<String> suffixes;
	
	public FileSuffixPipe(String[] args){
		this(args[0],args[1]);
		if (args.length!= 2) throw new IllegalArgumentException("expected 2 arguments got "+args.length);
	}
	
	//Note that String constructor has to be the first due to hack in
	// in building from string
	public FileSuffixPipe(String minLen, String suffixFile){
		this.minLen = Integer.parseInt(minLen);
		this.suffixFile  = suffixFile;
	}
	
	public FileSuffixPipe(int minLen,  String suffixFile){
		this.minLen = minLen;
		this.suffixFile  = suffixFile;
	}
	
	public void init(Corpus c){
		this.c = c;
		suffixes = new Alphabet<String>();
		try {
			BufferedReader reader = InputOutput.openReader(suffixFile);
			String line = reader.readLine();
			while(line != null) {
				String suffix = line.toLowerCase();
				//System.out.println("adding suffix " + suffix);
				suffixes.lookupObject(suffix);
				line = reader.readLine();
			}
		} catch (UnsupportedEncodingException e) {
			System.out.println("Error reading suffix file");
			System.out.println("Empty suffixes");
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			System.out.println("Error reading suffix file");
			System.out.println("Empty suffixes");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Error reading suffix file");
			System.out.println("Empty suffixes");
			e.printStackTrace();
		}
		
		
	}
	
	@Override
	public  void process(int wordId, String word, Alphabet<String> alphabet,SparseVector sv){
		if(suffixes.size() != 0){
			for(int i = 0; i < suffixes.size(); i++){
				String suffix = (String)suffixes.index2feat.get(i);
				if(suffix.length() > minLen){
					if(word.endsWith(suffix)){
						sv.add(alphabet.lookupObject("filesuf="+suffix), 1);
						//System.out.println("Word: " + word + " using " + suffix);
					}
				}
			}
		}
	}
	
	public  String getFeaturePrefix(){
		return "filesuf";
	}
	
	
	public String getName(){
		return "File suffix: " + suffixFile + " min lenght " + minLen;
	}
}
