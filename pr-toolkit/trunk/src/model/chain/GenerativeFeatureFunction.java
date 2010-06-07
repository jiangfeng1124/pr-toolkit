package model.chain;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;

import util.Alphabet;
import util.FeatureFunction;
import util.pipes.Pipe;
import util.pipes.PipeCollection;
import data.Corpus;


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
		Corpus c = new Corpus(args[0],0,Integer.MAX_VALUE,Integer.MAX_VALUE);
		GenerativeFeatureFunction gf = new GenerativeFeatureFunction(c,args[1]);
		System.out.println(gf.featureTableToString(gf.al));
		
	}
}

	

