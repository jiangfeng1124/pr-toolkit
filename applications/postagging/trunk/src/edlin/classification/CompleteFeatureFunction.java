package edlin.classification;

import java.io.Serializable;

import edlin.types.Alphabet;
import edlin.types.FeatureFunction;
import edlin.types.SparseVector;


public class CompleteFeatureFunction implements FeatureFunction, Serializable {

	/** this is the last feature for each y */
	public int defalutFeatureIndex;
	int numY;
	
	private static final long serialVersionUID = 1L;

	public CompleteFeatureFunction(Alphabet xAlphabet, Alphabet yAlphabet) {
		xAlphabet.stopGrowth();
		yAlphabet.stopGrowth();
		numY = yAlphabet.size();
		defalutFeatureIndex = xAlphabet.size();
	}

	public SparseVector apply(SparseVector x, int y) {
		SparseVector res = new SparseVector();
		for (int i = 0; i < x.numEntries(); i++) {
			res.add(y * (defalutFeatureIndex + 1) + x.getIndexAt(i), x
					.getValueAt(i));
		}
		res.add(y * (defalutFeatureIndex + 1) + defalutFeatureIndex, 1);
		return res;
	}

	public int wSize() {
		return numY * (defalutFeatureIndex + 1);
	}

}
