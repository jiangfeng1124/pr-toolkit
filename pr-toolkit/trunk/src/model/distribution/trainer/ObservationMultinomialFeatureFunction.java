package model.distribution.trainer;



import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;

import model.chain.GenerativeFeatureFunction;
import data.Corpus;

import util.Alphabet;
import util.SparseVector;
import util.pipes.Pipe;
import util.pipes.PipeCollection;


/**
 * @author javg
 *
 */
public class ObservationMultinomialFeatureFunction implements MultinomialFeatureFunction{
	
	
	data.Corpus corpus;
	util.SparseVector[] precomputedValues;
	public util.Alphabet al;
	int minNumberOfOccurrances;
	
	private static final long serialVersionUID = 1L;

	public ObservationMultinomialFeatureFunction(data.Corpus corpus, String maxEntObsFeaturesFile) throws IllegalArgumentException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {		
		this.corpus = corpus;
		al = new util.Alphabet();
		precomputedValues = new util.SparseVector[corpus.getNrWordTypes()];
		Pipe p = PipeCollection.buildPipe(maxEntObsFeaturesFile);
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
	/**
	 * Return a sparse vector with all features from variable x 
	 * Since we are not modelling in this case anything about the tags
	 * the features are the same for each y.
	 * This may change for other applications
	 * and value y
	 * @param x - The hidden state
	 * @param y - The word
	 * @return
	 */
	public SparseVector apply(int x, int y){
		return precomputedValues[y];
	}
	
	public String featureTableToString(){
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < al.size(); i++){
			sb.append(al.index2feat.get(i)+"\n");
		}
		return sb.toString();
	}
	
	public int nrFeatures(){
		return al.size();
	}
	
	public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException, IOException, IllegalArgumentException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Corpus c = new Corpus(args[0],0,Integer.MAX_VALUE,Integer.MAX_VALUE);
		ObservationMultinomialFeatureFunction gf = new ObservationMultinomialFeatureFunction(c,args[1]);
		System.out.println(gf.featureTableToString());
		
	}
}
