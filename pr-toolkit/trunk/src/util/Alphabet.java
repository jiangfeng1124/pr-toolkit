package util;

import gnu.trove.TObjectIntHashMap;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * This class is used to map from some descriptive features (e.g. words in text)
 * to an index in an array.
 * 
 * @author kuzman
 * 
 */

public class Alphabet<T> implements Serializable {

	public static final long serialVersionUID = 1L;
	public TObjectIntHashMap<T> feat2index;
	public ArrayList<T> index2feat;
	boolean canGrow;
	//random 
	double id;

	public Alphabet() {
		feat2index = new TObjectIntHashMap<T>();
		index2feat = new ArrayList<T>();
		canGrow = true;
		id = new Random().nextDouble();
	}

	public double getId(){
		return id;
	}
	
	/**
	 * returns the index associated with a feature. This should be the same for
	 * all features a,b where a.equals(b).
	 * 
	 * @param feature
	 * @return the index corresponding to the feature
	 */
	public int lookupObject(T feature) {
		if (feat2index.contains(feature))
			return feat2index.get(feature);
		else if (canGrow) {
			feat2index.put(feature, index2feat.size());
			index2feat.add(feature);
			return feat2index.get(feature);
		}
		return -1;
	}
	
	public T lookupIndex(int ind){
		return index2feat.get(ind);
	}

	/**
	 * at test time, we need to stop the growth of the alphabet so we do not
	 * increase the size of the feature vector in case the user tries to use
	 * features not encountered at training time.
	 */
	public void stopGrowth() {
		canGrow = false;
		feat2index.compact();
	}

	public void startGrowth() {
		canGrow = true;
	}
	
	public boolean canGrow(){
		return canGrow;
	}

	public int size() {
		return index2feat.size();
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException{
		out.writeLong(serialVersionUID);
		out.writeBoolean(canGrow);
		out.writeObject(index2feat);		
	}
	
	@SuppressWarnings("unchecked")
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException{
		long inid = in.readLong();
		if (inid!=serialVersionUID) throw new IOException("Serial version mismatch: expected "+serialVersionUID+" got "+inid);
		canGrow = in.readBoolean();
		index2feat = (ArrayList<T>) in.readObject();
		feat2index = new TObjectIntHashMap<T>();
		for(int i=0; i<index2feat.size(); i++){
			if (feat2index.contains(index2feat.get(i))) 
				throw new IOException("duplicate feature in file. feature: "+index2feat.get(i));
			feat2index.put(index2feat.get(i),i);
		}
	}

	/**
	 * returns the string representation of feature associated with a index. This should be the same for
	 * all features a,b where a.equals(b).
	 *
	 * @param index of feature
	 * @return the String representation of a feature
	 */
	public String lookupInt(int key) {
		String f = index2feat.get(key).toString();
		if (f == null) {
			throw new RuntimeException("Not one and the same alphabet");
		}
		return f;
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		ArrayList<Pair> pairs = new ArrayList<Pair>();
		for(int i = 0; i < index2feat.size(); i++){
			sb.append(index2feat.get(i)+"\t");
		}
		return sb.toString();
	}
	
}
