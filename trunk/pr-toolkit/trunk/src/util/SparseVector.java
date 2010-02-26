package util;

import gnu.trove.TDoubleArrayList;
import gnu.trove.TIntArrayList;

/**
 * a sparse vector implementation. Not necessarily the most efficient.
 * 
 * @author kuzman
 * 
 */
public class SparseVector {
	TIntArrayList indices;
	TDoubleArrayList values;

	public SparseVector() {
		indices = new TIntArrayList();
		values = new TDoubleArrayList();
	}

	public void add(int index, double value) {
		indices.add(index);
		values.add(value);
	}

	public int getIndexAt(int i) {
		return indices.get(i);
	}

	public double getValueAt(int i) {
		return values.get(i);
	}

	public int numEntries() {
		return indices.size();
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i : indices.toNativeArray())
			sb.append(i + " ");
		return sb.toString();
	}

}
