package model.distribution.trainer;



import util.SparseVector;


/**
 * Represents a feature function for a multinomial table.
 * Receives the variable and the value of the multinomial.
 * The particular subclasses are responsible for knowing
 * how to interpret this indexes, map to words or to the corresponding
 * meaning.
 * @author javg
 *
 */
public interface MultinomialFeatureFunction {
	/**
	 * Return a sparse vector with all features from variable x 
	 * and value y
	 * @param x
	 * @param y
	 * @return
	 */
	public SparseVector apply(int x, int y);
	public int nrFeatures();
	public String featureTableToString();
	public int[] getFeaturesByPrefix(String prefix);
	
	
}
