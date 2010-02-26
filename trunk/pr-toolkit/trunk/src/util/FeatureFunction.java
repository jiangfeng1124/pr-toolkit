package util;

public interface FeatureFunction {

	public SparseVector apply(SparseVector x, int y);

	public int wSize();

}
