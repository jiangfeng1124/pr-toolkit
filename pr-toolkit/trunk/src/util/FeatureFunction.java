package util;

/**
 * 
 * @author javg
 *
 */
public interface FeatureFunction {
	public SparseVector apply(SparseVector x, int y);
	public int wSize();
}
