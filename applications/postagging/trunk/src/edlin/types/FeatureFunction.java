package edlin.types;

import edlin.types.SparseVector;

public interface FeatureFunction {

	public SparseVector apply(SparseVector x, int y);

	public int wSize();

}
