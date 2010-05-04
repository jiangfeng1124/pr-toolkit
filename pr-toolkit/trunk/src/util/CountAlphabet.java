package util;

import java.util.ArrayList;
import java.util.Collections;

import gnu.trove.TIntDoubleHashMap;
import gnu.trove.TIntIntHashMap;

/**
 * Extends alphabet to store counts of elements
 */
public class CountAlphabet<T> extends Alphabet<T> {

	private final static long serialVersionUID = 42L;
	
	private TIntIntHashMap counts;
	private int totalElements = 0;
	
	// Indicates whether to stop counting or not.
	// (Unless in transductive mode,we only want to count the training words,
	// not the dev and test words also).
	boolean stopCounts = false;
	
	public CountAlphabet() {
		super();
		counts = new TIntIntHashMap();
	}

	public void setStopCounts(boolean flag) {
		stopCounts = flag;
	}
	
	public void compact(){
		counts.compact();
		feat2index.compact();
		index2feat.trimToSize();
	}
	
	/**
	 * returns the index associated with a feature. This should be the same for
	 * all features a,b where a.equals(b).
	 * 
	 * @param feature
	 * @return the index corresponding to the feature
	 */
	public int lookupObject(T feature) {
		if (feat2index.contains(feature)) {
			if(canGrow&&!stopCounts) {
				int index = feat2index.get(feature);
				counts.put(index, counts.get(index)+1);
				totalElements++;
				return index;
			} else {
				return feat2index.get(feature);

			}
		} else if (canGrow) {
			int index = index2feat.size();
			feat2index.put(feature, index);
			index2feat.add(feature);
			if(!stopCounts){
				counts.put(index, 1);
				totalElements++;
			}
			return feat2index.get(feature);
		}
		return -1;
	}

	public void stopGrowth() {
		super.stopGrowth();
		counts.compact();
	}
	
	public int getCounts(int index){
		if(counts.contains(index))
			return counts.get(index);
		return -1;
	}
	
	public int getCounts(T feature){
		if (feat2index.contains(feature)){
			return counts.get(feat2index.get(feature));
		}
		return -1;
	}
	
	public int getNumberElements(){
		return totalElements;
	}
	
	
	/**
	 * Return an hash map with the increasing accum freqs 
	 * of the count alphabet.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public TIntDoubleHashMap getAccumFreqs(){
		TIntDoubleHashMap accumFreqs = new TIntDoubleHashMap();
		ArrayList<Pair> pairs = new ArrayList<Pair>();
		for(int i = 0; i < index2feat.size(); i++){
			pairs.add(new Pair(i,counts.get(i)));
		}
		Collections.sort(pairs,new Sorters.sortWordsCounts());
		double sum = 0;
		for(Pair p: pairs){
			double freq = (((Integer)p._second)*1.0/totalElements);
			sum+=freq;
			accumFreqs.put((Integer)p._first, sum);
		}
		return accumFreqs;
	}
	
	public int[] sortedKeyList(){
		TIntDoubleHashMap accumFreqs = new TIntDoubleHashMap();
		ArrayList<Pair> pairs = new ArrayList<Pair>();
		for(int i = 0; i < index2feat.size(); i++){
			pairs.add(new Pair(i,counts.get(i)));
		}
		Collections.sort(pairs,new Sorters.sortWordsCounts());
		int[] sortedList = new int[pairs.size()];
		
		for(int i = 0; i < sortedList.length; i++){
			sortedList[i] = (Integer)pairs.get(i)._first;
		}
		return sortedList;
	}
	
	/**
	 * Returns a list sorted by occurences
	 */
	@SuppressWarnings("unchecked")
	public String toString(){
		StringBuffer sb = new StringBuffer();
		ArrayList<Pair> pairs = new ArrayList<Pair>();
		for(int i = 0; i < index2feat.size(); i++){
			pairs.add(new Pair(i,counts.get(i)));
		}
		Collections.sort(pairs,new Sorters.sortWordsCounts());
		double sum = 0;
		for(Pair p: pairs){
			double freq = (((Integer)p._second)*1.0/totalElements);
			sum+=freq;
			sb.append(index2feat.get((Integer)p._first)+ " "+ (Integer)p._first + " " + p._second + " freq " + freq + " accum " + sum +"\n");
		}
		return sb.toString();
	}
	
	
	
	
}
