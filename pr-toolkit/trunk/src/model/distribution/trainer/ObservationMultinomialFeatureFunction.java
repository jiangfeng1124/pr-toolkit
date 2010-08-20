package model.distribution.trainer;



import gnu.trove.TIntArrayList;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import util.SparseVector;
import util.pipes.Pipe;
import util.pipes.PipeCollection;
import data.Corpus;


/**
 * @author javg
 *
 */
public class ObservationMultinomialFeatureFunction implements MultinomialFeatureFunction{
	
	
	data.Corpus corpus;
	util.SparseVector[] precomputedValues;
	public util.Alphabet al;
	int minNumberOfOccurrances;
	public ArrayList<String> featuresPrefix;
	
	private static final long serialVersionUID = 1L;

	public ObservationMultinomialFeatureFunction(data.Corpus corpus, String maxEntObsFeaturesFile) throws IllegalArgumentException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {		
		this.corpus = corpus;
		al = new util.Alphabet();
		precomputedValues = new util.SparseVector[corpus.getNrWordTypes()];
		Pipe p = PipeCollection.buildPipe(maxEntObsFeaturesFile);
		p.init(corpus);
		String[] fnames = p.getName().split("\n");
		System.out.println("MaxEnt Using Features:\n");
		for (int i = 0; i < fnames.length; i++) {
			System.out.println(fnames[i]);
		}
		System.out.println("default");
		featuresPrefix = new ArrayList<String>();
		String[] fPref = p.getFeaturePrefix().split("\n");
		for (int i = 0; i < fPref.length; i++) {
			featuresPrefix.add(fPref[i]);
		}
		featuresPrefix.add("default");
		System.out.println(featuresPrefix.toString());

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
	
	public int[] getFeaturesByPrefix(String prefix) {
		TIntArrayList featureIndexes = new TIntArrayList();
		for(int i  =0; i < nrFeatures(); i++){
				if(((String) al.index2feat.get(i)).startsWith(prefix)){
					featureIndexes.add(i);
				}
			}	
		return featureIndexes.toNativeArray();
	}
	
	public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException, IOException, IllegalArgumentException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Corpus c = new Corpus(args[0],0,Integer.MAX_VALUE,Integer.MAX_VALUE);
		ObservationMultinomialFeatureFunction gf = new ObservationMultinomialFeatureFunction(c,args[1]);
		System.out.println(gf.featureTableToString());
		
	}
	
}
