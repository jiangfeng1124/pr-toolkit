package util;

import util.TroveUtils;
import gnu.trove.TDoubleArrayList;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntIntHashMap;

/**
 * a sparse vector implementation. Not necessarily the most efficient.
 * 
 * @author kuzman
 * 
 */
public class SparseVector {
	TIntArrayList indices;
	TDoubleArrayList values;
	TIntIntHashMap positions;

	public SparseVector() {
		indices = new TIntArrayList();
		values = new TDoubleArrayList();
		positions=new TIntIntHashMap();
	}
	
	SparseVector(TIntArrayList indices, TIntIntHashMap positions, TDoubleArrayList values) {
		this.indices = indices;
		this.values=values;
		this.positions=positions;
	}

	public void add(int index, double value) {
		indices.add(index);
		values.add(value);
		positions.put(index, indices.size()-1);
	}

	/**
	 * Gets Index at position i
	 * @param i
	 * @return
	 */
	public int getIndexAt(int i) {
		return indices.get(i);
	}

	/**
	 * Gets value at position i.
	 * Note not value at index i but at position i
	 * @param i
	 * @return
	 */
	public double getValueAt(int i) {
		if(i>=values.size()){
			return 0;
		}
		return values.get(i);
	}

	/**
	 * Gets value at index index
	 * @param index
	 * @return
	 */
	public double getValue(int index) {
		if(!positions.contains(index)){
			return 0;
		}
		return values.get(positions.get(index));
	}
	
	
	public int numEntries() {
		return indices.size();
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < indices.size(); i++){
			sb.append(indices.get(i) + ":"+values.get(i)+" ");
		}
		return sb.toString();
	}
	
	
	/**
	 * Adds all entries of the sparse vector
	 * @return
	 */
	public  final double sum(){
		return TroveUtils.sum(values);
	}
	
	/**
	 * Return a new sparse vector with exponential of each entry
	 * @return
	 */
	public final SparseVector expEntries(){
		return new SparseVector(indices,positions,TroveUtils.exp(values));
	}

	
	public static void main(String[] args) {
		SparseVector s = new SparseVector();
		s.add(1, 2);
		s.add(10, 5);
		System.out.println(s.toString());
		SparseVector exp = s.expEntries();
		System.out.println(exp.toString());
		System.out.println(s.getValue(1));
		System.out.println(s.getValue(10));
		System.out.println(s.getValueAt(0));
		System.out.println(s.getValueAt(1));
		System.out.println(s.getValueAt(2));
	}
	
}
