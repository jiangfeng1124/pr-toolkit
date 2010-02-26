package model.chain;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import data.Corpus;
import util.Alphabet;
import util.FeatureFunction;
import util.pipes.LongestSuffixPipe;
import util.pipes.Pipe;
import util.pipes.PipeCollection;
import util.pipes.SuffixPipe;


public class GenerativeFeatureFunction implements FeatureFunction, Serializable {

	data.Corpus corpus;
	util.SparseVector[] precomputedValues;
	public util.Alphabet al;
	int minNumberOfOccurrances;
	
	private static final long serialVersionUID = 1L;

	public GenerativeFeatureFunction(data.Corpus corpus, String maxEntFeaturesFile) throws IllegalArgumentException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {		
		this.corpus = corpus;
		al = new util.Alphabet();
		precomputedValues = new util.SparseVector[corpus.getNrWordTypes()];
		Pipe p = PipeCollection.buildPipe(maxEntFeaturesFile);
		p.init(corpus);
		System.out.println("MaxEnt Using Features:\n" + p.getName());
		for(int i = 0; i < corpus.getNrWordTypes(); i++){
			String word = corpus.wordAlphabet.index2feat.get(i);
			util.SparseVector v = new util.SparseVector();
			v.add(al.lookupObject("default"), 1);
			p.process(i, word, al, v);
			precomputedValues[i] = v;
		}
		System.out.println("Max-Ent using: " + al.size() + " features ");
	}
	
	
	
//	public void precomputeFeatureTable(){
//		CountAlphabet suf3 = CorpusUtils.createTypesSuffixAlphabet(corpus,3);
//		CountAlphabet suf2 = CorpusUtils.createTypesSuffixAlphabet(corpus,2);
//
//		for(int i = 0; i < corpus.getNrWordTypes(); i++){
//			String word = corpus.wordAlphabet.index2feat.get(i);
//			util.SparseVector v = new util.SparseVector();
//			v.add(al.lookupObject("default"), 1);
//			//Word identity
//			
//			if (corpus.getWordTypeCounts(i)>= minNumberOfOccurrances){
//				v.add(al.lookupObject("word="+word), 1);
//			}
//			
//			
//			
//			//Taking suffix
//			if(word.length() > 2){
//				String sufix = word.substring(word.length()-2);
//				if(suf2.getCounts(suf2.feat2index.get(sufix)) > minNumberOfOccurrances){
//					v.add(al.lookupObject("suf="+sufix), 1);
//				}
//			}
//			if(word.length() > 3){
//				String sufix = word.substring(word.length()-3);
//				if(suf3.getCounts(suf3.feat2index.get(sufix)) > minNumberOfOccurrances){
//					v.add(al.lookupObject("suf="+word.substring(word.length()-3)), 1);
//				}
//			}
//			
//			//Word counts must be put into bins
//			v.add(al.lookupObject("wc="+corpus.getWordTypeCounts(i)), 1);
//			
//			//Should be put into bins
//			if(word.length() > 3){
//				v.add(al.lookupObject("wl="+word.length()), 1);
//			}
//			
//			//Contains hiffen
//			if(word.contains("-")){
//				v.add(al.lookupObject("hiffen"), 1);
//			}
//			//Contains underscore
//			if(word.contains("_")){
//				v.add(al.lookupObject("underscore"), 1);
//			}
//			
//			//Is uppercase
//			if(Character.isUpperCase(word.charAt(0))){
//				v.add(al.lookupObject("upcase"), 1);
//			}
//				
//			precomputedValues[i]=v;
//		}
//		System.out.println("Number of Features " + al.size());
//	}	

	public String featureTableToString(Alphabet al){
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < al.size(); i++){
			sb.append(al.index2feat.get(i)+"\n");
		}
		return sb.toString();
	}
		
	public util.SparseVector apply(util.SparseVector x, int y) {
		return precomputedValues[y];
	}

	public int wSize() {
		return  al.size();
	}

	public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException, IOException, IllegalArgumentException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Corpus c = new Corpus(args[0],0,Integer.MAX_VALUE);
		GenerativeFeatureFunction gf = new GenerativeFeatureFunction(c,args[1]);
		System.out.println(gf.featureTableToString(gf.al));
		
		
	}
}

	

