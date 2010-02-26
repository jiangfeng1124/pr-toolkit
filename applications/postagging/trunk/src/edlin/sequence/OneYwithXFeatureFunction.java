package edlin.sequence;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import edlin.types.Alphabet;
import edlin.types.SparseVector;
import edlin.types.StaticUtils;

public class OneYwithXFeatureFunction implements SequenceFeatureFunction, Serializable {

	private static final long serialVersionUID = 1L;
	public int xAsize, yAsize;

	public OneYwithXFeatureFunction(Alphabet xAlphabet, Alphabet yAlphabet) {
		this.xAsize = xAlphabet.size();
		this.yAsize = yAlphabet.size();
	}

	// see SequenceFeatureFunction for java doc
	public SparseVector apply(SparseVector[] x, int[] y) {
		SparseVector result = apply(x, 0, y[0], 0);
		for (int t = 1; t < x.length; t++) {
			SparseVector fsubt = apply(x, y[t - 1], y[t], t);
			StaticUtils.plusEquals(result, fsubt);
		}
		return result;
	}

	private int initialIndex(int y) {
		return y;
	}

	private int finalIndex(int y) {
		return yAsize + y;
	}

	private int labelXlabel(int y1, int y2) {
		return 2 * yAsize + // initial
				y1 * yAsize + y2;
	}

	private int labelXinput(int y, int x) {
		return 2 * yAsize + // initial
				yAsize * yAsize + // labelXlabel
				y * xAsize + x;
	}

	public int wSize() {
		return 2 * yAsize + // initial
				yAsize * yAsize + // labelXlabel
				yAsize * xAsize;
	}

	// see SequenceFeatureFunction for java doc
	public SparseVector apply(SparseVector[] x, int ytm1, int yt, int t) {
		SparseVector result = new SparseVector();
		// initial yt
		if (t == 0)
			result.add(initialIndex(yt), 1);
		// ymt1 times yt
		else
			result.add(labelXlabel(ytm1, yt), 1);
		if (t == x.length - 1)
			result.add(finalIndex(yt), 1);
		// yt times x
		for (int i = 0; i < x[t].numEntries(); i++) {
			result.add(labelXinput(yt, x[t].getIndexAt(i)), 1);
		}
		return result;
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException{
		out.writeLong(serialVersionUID);
		out.writeInt(xAsize);
		out.writeInt(yAsize);
	}
	
	@SuppressWarnings("unchecked")
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException{
		long inid = in.readLong();
		if (inid!=serialVersionUID) throw new IOException("Serial version mismatch: expected "+serialVersionUID+" got "+inid);
		xAsize = in.readInt();
		yAsize = in.readInt();
	}

}
