package edlin.sequence;

import java.io.Serializable;

import edlin.types.SparseVector;


public interface SequenceFeatureFunction extends Serializable {

	/** apply the feature function to an entire sequence of y's */
	public SparseVector apply(SparseVector[] x, int[] y);

	/**
	 * apply the feature function to a pair of tags (label positions)
	 * 
	 * @param x
	 *            entire input sequence
	 * @param ytm1
	 *            label at position t-1
	 * @param yt
	 *            label at position t
	 * @param t
	 *            position of interest. Should be 0<=t<x.length where t=0
	 *            ignores ytm1. ignores the second label.
	 */
	public SparseVector apply(SparseVector[] x, int ymt1, int yt, int t);

	public int wSize();

}
