package edlin.types;

import java.io.IOException;
import java.io.ObjectOutputStream;

import edlin.types.SparseVector;

import edlin.classification.CompleteFeatureFunction;

/**
 * A linear model for classification. It has the form h(x) = arg_max_y f(x,y).w
 * 
 * @author kuzman
 * 
 */

public class LinearClassifier {
	private static final long serialVersionUID = 1L;
	public double[] w;
	public Alphabet yAlphabet;
	public Alphabet xAlphabet;
	FeatureFunction fxy;

	public LinearClassifier(Alphabet xAlpha, Alphabet yAlpha,
			FeatureFunction fxy) {
		w = new double[fxy.wSize()];
		yAlphabet = yAlpha;
		xAlphabet = xAlpha;
		this.fxy = fxy;
	}

	/**
	 * computes the score of each label 'y' defined as f(x,y) . w
	 * 
	 * @param x
	 * @return [f(x,0).w, f(x,1).w, ...]
	 */
	public double[] scores(SparseVector x) {
		double[] res = new double[yAlphabet.size()];
		for (int y = 0; y < yAlphabet.size(); y++) {
			res[y] = StaticUtils.dotProduct(fxy.apply(x, y), (w));
		}
		return res;
	}

	/**
	 * computes the classification according to this linear classifier.
	 * arg_max_y f(x,y) . w
	 * 
	 * @param x
	 * @return y that maximizes f(x,y) . w
	 */
	public int label(SparseVector x) {
		double[] scores = scores(x);
		int max = 0;
		for (int y = 0; y < yAlphabet.size(); y++) {
			if (scores[max] < scores[y])
				max = y;
		}
		return max;
	}
	
	public void writeObject(ObjectOutputStream out) throws IOException{
		out.writeLong(serialVersionUID);
		out.writeInt(w.length);
		for (double d:w) out.writeDouble(d);
		out.writeObject(xAlphabet);
		out.writeObject(yAlphabet);
		out.writeObject(fxy);
	}
	
	@SuppressWarnings("unchecked")
	public void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException{
		long inid = in.readLong();
		if (inid!=serialVersionUID) throw new IOException("Serial version mismatch: expected "+serialVersionUID+" got "+inid);
		w = new double[in.readInt()];
		for (int i = 0; i < w.length; i++) w[i] = in.readDouble();
		xAlphabet = (Alphabet) in.readObject();
		yAlphabet = (Alphabet) in.readObject();
		fxy = (CompleteFeatureFunction) in.readObject();
	}

}
