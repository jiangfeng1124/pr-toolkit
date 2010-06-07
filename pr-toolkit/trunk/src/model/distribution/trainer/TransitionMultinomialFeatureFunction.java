package model.distribution.trainer;



import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import util.SparseVector;


/**
 * Feature function for the transitions probabilities:
 * P(y_t|y_t-1)
 * 
 * @author javg
 *
 */
public class TransitionMultinomialFeatureFunction implements MultinomialFeatureFunction{
	
	
	data.Corpus corpus;
	util.SparseVector[] precomputedValues;
	public util.Alphabet al;

	
	private static final long serialVersionUID = 1L;

	/**
	 * Very simple feature function just adds a default features and the hidden state id
	 * @param corpus
	 * @param nrHiddenStates
	 * @param maxEntTranistionFeaturesFile
	 * @throws IllegalArgumentException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public TransitionMultinomialFeatureFunction(data.Corpus corpus, 
			int nrHiddenStates) throws IllegalArgumentException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {		
		this.corpus = corpus;
		al = new util.Alphabet();
		precomputedValues = new util.SparseVector[nrHiddenStates];
		for(int i = 0; i < nrHiddenStates; i++){
			util.SparseVector v = new util.SparseVector();
			v.add(al.lookupObject("default"), 1);
			v.add(al.lookupObject("going-to"+i), 1);
			precomputedValues[i] = v;
		}
		System.out.println("Max-Ent using: " + al.size() + " features ");
	}
	/**
	 * Return a sparse vector with all features from variable x 
	 *
	 * @param x - The previous hidden state
	 * @param y - The current hidden state
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
	
}
