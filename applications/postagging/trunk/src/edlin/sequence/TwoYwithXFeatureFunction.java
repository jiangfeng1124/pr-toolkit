package edlin.sequence;

/***
 * This class implements general ner structure for
 * structured algorithms
 * @TODO: need more description later on
 * @author Kuzman Ganchev and Georgi Georgiev
 * <A HREF="mailto:georgiev@ontotext.com>georgi.georgiev@ontotext.com</A>
 * <A HREF="mailto:ganchev@ontotext.com>kuzman.ganchev@ontotext.com</A>
 * Date: Thu Feb 26 12:27:56 EET 2009
 */

import java.io.IOException;
import java.io.ObjectOutputStream;

import edlin.types.Alphabet;
import edlin.types.SparseVector;
import edlin.types.StaticUtils;


public class TwoYwithXFeatureFunction implements SequenceFeatureFunction {

	private static final long serialVersionUID = 1L;
	public int xAsize, yAsize;
	public int numInputs;

	public TwoYwithXFeatureFunction(Alphabet xAlphabet, Alphabet yAlphabet) {
		this.xAsize = xAlphabet.size();
		this.yAsize = yAlphabet.size();
		this.numInputs = xAlphabet.size()+1;
	}

	// see SequenceFeatureFunction for java doc
	public SparseVector apply(SparseVector[] x, int[] y) {
		SparseVector result = this.apply(x, 0, y[0], 0);
		for (int t = 1; t < x.length; t++) {
			SparseVector fsubt = this.apply(x, y[t - 1], y[t], t);
			StaticUtils.plusEquals(result, fsubt);
		}
		return result;
	}

	/*
	 * order of entries in array:
	 * inital (x*y)
	 * final (x*y)
	 * labelXlabel(x*y*y)
	 * labelXinput(x*y)
	 */

	private int initialIndex(int y, int x) {
		return y*this.numInputs + x;
	}

	private int finalIndex(int y, int x) {
		return this.yAsize*this.numInputs + y*this.numInputs + x;
	}

	private int labelXlabel(int y1, int y2, int x) {
		return 2 * this.yAsize*this.numInputs + // initial
				y1 * this.yAsize*this.numInputs + y2*this.numInputs + x;
	}

	private int labelXinput(int y, int x) {
		return 2 * this.yAsize*this.numInputs + // initial, final
				this.yAsize * this.yAsize*this.numInputs + // labelXlabelXinput
				y * this.xAsize + x;
	}

	public int wSize() {
		return 2 * this.yAsize*this.numInputs + // initial, final
				this.yAsize * this.yAsize*this.numInputs + // labelXlabel
				this.yAsize * this.numInputs;
	}

	// see SequenceFeatureFunction for java doc
	public SparseVector apply(SparseVector[] xseq, int ytm1, int yt, int t) {
		SparseVector result = new SparseVector();
		for (int i = 0; i < xseq[t].numEntries(); i++) {
			int x = xseq[t].getIndexAt(i);
			// initial yt
			if (t == 0) {
				result.add(this.initialIndex(yt,x), 1);
			// ymt1 times yt
			} else {
				result.add(this.labelXlabel(ytm1, yt,x), 1);
			}
			if (t == xseq.length - 1) {
				result.add(this.finalIndex(yt,x), 1);
			}
			// yt times x
			result.add(this.labelXinput(yt, x), 1);
		}
		int x = this.xAsize;
		// initial yt
		if (t == 0) {
			result.add(this.initialIndex(yt,x), 1);
			// ymt1 times yt
		} else {
			result.add(this.labelXlabel(ytm1, yt,x), 1);
		}
		if (t == xseq.length - 1) {
			result.add(this.finalIndex(yt,x), 1);
		}
		// yt times x
		result.add(this.labelXinput(yt, x), 1);

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
		numInputs = xAsize+1;
		yAsize = in.readInt();
	}

}
