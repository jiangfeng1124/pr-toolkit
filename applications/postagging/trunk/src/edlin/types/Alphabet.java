package edlin.types;

import gnu.trove.TObjectIntHashMap;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class is used to map from some descriptive features (e.g. words in text)
 * to an index in an array.
 * 
 * @author kuzman
 * 
 */

public class Alphabet implements Serializable{

	private static final long serialVersionUID = 1L;
	TObjectIntHashMap feat2index;
	ArrayList<Object> index2feat;
	boolean canGrow;

	public Alphabet() {
		feat2index = new TObjectIntHashMap();
		index2feat = new ArrayList<Object>();
		canGrow = true;
	}

	/**
	 * returns the index associated with a feature. This should be the same for
	 * all features a,b where a.equals(b).
	 * 
	 * @param feature
	 * @return the index corresponding to the feature
	 */
	public int lookupObject(Object feature) {
		if (feat2index.contains(feature))
			return feat2index.get(feature);
		else if (canGrow) {
			feat2index.put(feature, index2feat.size());
			index2feat.add(feature);
			return feat2index.get(feature);
		}
		return -1;
	}
	
	public Object lookupIndex(int ind){
		return index2feat.get(ind);
	}

	/**
	 * at test time, we need to stop the growth of the alphabet so we do not
	 * increase the size of the feature vector in case the user tries to use
	 * features not encountered at training time.
	 */
	public void stopGrowth() {
		canGrow = false;
	}

	public void startGrowth() {
		canGrow = true;
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
		index2feat = (ArrayList<Object>) in.readObject();
		feat2index = new TObjectIntHashMap();
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
	
}
