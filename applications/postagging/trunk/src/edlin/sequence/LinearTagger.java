package edlin.sequence;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import edlin.types.Alphabet;
import edlin.types.SparseVector;
import edlin.types.StaticUtils;


/**
 * A linear model for sequence classification. It has the form h(x) = arg_max_y
 * f(x,y).w where x and y are sequences of identical length, and f(x,y)
 * decomposes over pairs of positions on y.
 * 
 * @author kuzman
 * 
 */

public class LinearTagger implements Serializable{


	private static final long serialVersionUID = 1L;
	public double[] w;
	Alphabet yAlphabet;
	Alphabet xAlphabet;
	SequenceFeatureFunction fxy;

	public LinearTagger(Alphabet xAlpha, Alphabet yAlpha,
			SequenceFeatureFunction fxy) {
		w = new double[fxy.wSize()];
		yAlphabet = yAlpha;
		xAlphabet = xAlpha;
		this.fxy = fxy;
	}

	/**
	 * at each position 0<=t<x.length, computes the score of each label pair
	 * 'ytm1','yt' as f(x,ytm1,yt) . w where yt is the label at position t and
	 * ytm1 is the label at position t-1.
	 * 
	 * @param x
	 * @return result[t][ytm1][yt] = f(x,ytm1,yt).w
	 */
	public double[][][] scores(SparseVector[] x) {
		double[][][] res = new double[x.length][yAlphabet.size()][yAlphabet
				.size()];
		for (int t = 0; t < x.length; t++) {
			for (int ytm1 = 0; ytm1 < yAlphabet.size(); ytm1++) {
				for (int yt = 0; yt < yAlphabet.size(); yt++) {
					res[t][ytm1][yt] = StaticUtils.dotProduct(fxy.apply(x,
							ytm1, yt, t), w);
				}
			}
		}
		return res;
	}

	/**
	 * use the Viterbi algorithm to find arg_max_y f(x,y) . w
	 * 
	 * @param x
	 * @return y that maximizes f(x,y) . w
	 */
	public int[] label(SparseVector[] x) {
		double[][][] scores = scores(x);
		double[][] gamma = new double[x.length][yAlphabet.size()];
		int[][] back = new int[x.length][yAlphabet.size()];
		for (int y = 0; y < yAlphabet.size(); y++) {
			gamma[0][y] = scores[0][0][y];
		}
		for (int t = 1; t < x.length; t++) {
			for (int yt = 0; yt < yAlphabet.size(); yt++) {
				gamma[t][yt] = Double.NEGATIVE_INFINITY;
				for (int ytm1 = 0; ytm1 < yAlphabet.size(); ytm1++) {
					if (gamma[t][yt] < gamma[t - 1][ytm1] + scores[t][ytm1][yt]) {
						back[t][yt] = ytm1;
						gamma[t][yt] = gamma[t - 1][ytm1] + scores[t][ytm1][yt];
					}
				}
			}
		}
		int[] tags = new int[x.length];
		for (int y = 0; y < yAlphabet.size(); y++) {
			if (gamma[x.length - 1][tags[x.length - 1]] < gamma[x.length - 1][y]) {
				tags[x.length - 1] = y;
			}
		}
		for (int t = x.length - 2; t >= 0; t--) {
			tags[t] = back[t + 1][tags[t + 1]];
		}
		return tags;
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
		fxy = (SequenceFeatureFunction) in.readObject();
	}

}
