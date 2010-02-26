package edlin.types;

import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * This class is used to map from some descriptive features (e.g. words in text)
 * to an index in an array.
 * 
 * @author kuzman
 * 
 */

public class DummyAlphabet extends Alphabet{
	
	int size;


	public DummyAlphabet(int size) {
		this.size = size;
	}

	/**
	 * returns the index associated with a feature. This should be the same for
	 * all features a,b where a.equals(b).
	 * 
	 * @param feature
	 * @return the index corresponding to the feature
	 */
	public int lookupObject(Object feature) {
		throw new RuntimeException("lookup object not implemented in dummy alphabet");
	}
	
	public Object lookupIndex(int ind){
		return ind+"";
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
		return size;
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException{
		out.writeInt(size);
	}
	
	@SuppressWarnings("unchecked")
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException{
		size = in.readInt();
	}

	/**
	 * returns the string representation of feature associated with a index. This should be the same for
	 * all features a,b where a.equals(b).
	 *
	 * @param index of feature
	 * @return the String representation of a feature
	 */
	public String lookupInt(int key) {
		return "" +key;
	}
	
}
