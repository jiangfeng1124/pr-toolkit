package edlin.types;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import edlin.sequence.Evaluate;
import edlin.sequence.LinearTagger;
import edlin.sequence.SequenceInstance;
import edlin.types.SparseVector;


/**
 * Some methods that are useful to have in linear models.
 *
 * @author kuzman
 *
 */
public class StaticUtils {

	/**
	 * the dot product between a sparse vector v and a dense vector w.
	 */
	public static double dotProduct(SparseVector v, double[] w) {
		double res = 0;
		for (int i = 0; i < v.indices.size(); i++) {
			res += w[v.indices.get(i)] * v.values.get(i);
		}
		return res;
	}

	public static double dotProduct(double[] v, double[] w) {
		if (v.length != w.length) {
			throw new AssertionError("unequal lengths");
		}
		double res = 0;
		for (int i = 0; i < v.length; i++) {
			res += w[i] * v[i];
		}
		return res;

	}

	/**
	 * shuffle a list (intended as a list of instances) in place using seed as
	 * the random seed.
	 */
	@SuppressWarnings("unchecked")
	public static void shuffle(ArrayList list, long seed) {
		Random r = new Random(seed);
		for (int i = 0; i < list.size() - 1; i++) {
			int j = r.nextInt(list.size() - (i + 1)) + i + 1;
			Object iObj = list.get(i);
			list.set(i, list.get(j));
			list.set(j, iObj);
		}
	}
	
	/**
	 * The accuracy of classifier h on the data set data. This is just the
	 * number of correct tokens divided by the number of tokens.
	 */
	public static double computeAccuracyS(LinearTagger h,
			ArrayList<SequenceInstance> data) {
		return Evaluate.eval(h, data, 0).accuracy();
	}

	/**
	 * The accuracy of classifier h on the data set data. This is just the
	 * number of correct instance divided by the number of instances.
	 */
	public static double computeAccuracy(LinearClassifier h,
			ArrayList<ClassificationInstance> data) {
		double correct = 0;
		double wrong = 0;
		for (ClassificationInstance inst : data) {
			int hx = h.label(inst.x);
			if (hx == inst.y) {
				correct++;
			} else {
				wrong++;
			}
		}
		return correct / (correct + wrong);
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<ClassificationInstance>[] split(
			ArrayList<ClassificationInstance> l, int splitAt) {
		ArrayList<ClassificationInstance> tmp1 = new ArrayList<ClassificationInstance>(
				splitAt);
		ArrayList<ClassificationInstance> tmp2 = new ArrayList<ClassificationInstance>(
				l.size() - splitAt);
		int i = 0;
		for (ClassificationInstance inst : l) {
			if (i < splitAt) {
				tmp1.add(inst);
			} else {
				tmp2.add(inst);
			}
			i++;
		}
		return new ArrayList[] { tmp1, tmp2 };
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<SequenceInstance>[] splitS(
			ArrayList<SequenceInstance> l, int splitAt) {
		ArrayList<SequenceInstance> tmp1 = new ArrayList<SequenceInstance>(
				splitAt);
		ArrayList<SequenceInstance> tmp2 = new ArrayList<SequenceInstance>(l
				.size()
				- splitAt);
		int i = 0;
		for (SequenceInstance inst : l) {
			if (i < splitAt) {
				tmp1.add(inst);
			} else {
				tmp2.add(inst);
			}
			i++;
		}
		return new ArrayList[] { tmp1, tmp2 };
	}

	/**
	 * update: w = w + v
	 */
	public static void plusEquals(double[] w, SparseVector v) {
		for (int i = 0; i < v.indices.size(); i++) {
			w[v.indices.get(i)] += v.values.get(i);
		}
	}

	/**
	 * res = exp(vec)
	 */
	public static double[] exp(double[] vec) {
		double[] res = new double[vec.length];
		for (int i = 0; i < vec.length; i++) {
			res[i] = Math.exp(vec[i]);
		}
		return res;
	}

	/**
	 * res = exp(vec)
	 */
	public static double[][] exp(double[][] vec) {
		double[][] res = new double[vec.length][];
		for (int t = 0; t < vec.length; t++) {
			res[t] = new double[vec[t].length];
			for (int i = 0; i < vec[t].length; i++) {
				res[t][i] = Math.exp(vec[t][i]);
			}
		}
		return res;
	}

	/**
	 * res = exp(vec)
	 */
	public static double[][][] exp(double[][][] vec) {
		double[][][] res = new double[vec.length][][];
		for (int t = 0; t < vec.length; t++) {
			res[t] = new double[vec[t].length][];
			for (int i = 0; i < vec[t].length; i++) {
				res[t][i] = new double[vec[t][i].length];
				for (int j = 0; j < vec[t].length; j++) {
					res[t][i][j] = Math.exp(vec[t][i][j]);
				}
			}
		}
		return res;
	}

	/**
	 * @param w
	 * @return sum_i w[i]^2
	 */
	public static double twoNormSquared(double[] w) {
		double res = 0;
		for (double d : w) {
			res += d * d;
		}
		return res;
	}

	/**
	 * @param probs
	 * @return sum_i probs[i]
	 */
	public static double sum(double[] probs) {
		double res = 0;
		for (double d : probs) {
			res += d;
		}
		return res;
	}

	/**
	 * w <- w + v
	 */
	public static void plusEquals(SparseVector w, SparseVector v) {
		for (int i = 0; i < v.indices.size(); i++) {
			w.add(v.indices.get(i), v.values.get(i));
		}
	}

	/**
	 * w <- w + d*v
	 */
	public static void plusEquals(double[] w, SparseVector v, double d) {
		for (int i = 0; i < v.indices.size(); i++) {
			w[v.indices.get(i)] += d * v.values.get(i);
		}
	}

	/**
	 * w <- w + d*v
	 */
	public static void plusEquals(double[] w, double[] v, double d) {
		StaticUtils.add(w, w, v, d);
	}

	/**
	 * w <- w + v
	 */
	public static void plusEquals(double[] w,  double d) {
		for (int i = 0; i < w.length; i++) {
			w[i] += d;
		}
	}
	
	
	/**
	 * u = v + d*w
	 */
	public static void add(double[] u, double[] v, double[] w, double d) {
		for (int i = 0; i < v.length; i++) {
			u[i] = v[i] + d * w[i];
		}
	}

	/**
	 * computes arg max_i v[i]
	 */
	public static int argmax(double[] v) {
		int res = 0;
		for (int i = 0; i < v.length; i++) {
			if (v[res] < v[i]) {
				res = i;
			}
		}
		return res;
	}

	public static double max(double[] v) {
		return v[argmax(v)];
	}
	
	
	public static SparseVector lookupCollection(Collection<String> in, Alphabet a){
		SparseVector out = new SparseVector();
		for (String x:in){
			out.add(a.lookupObject(x), 1);
		}
		return out;
	}
	
	public static void saveTagger(LinearTagger h, String fname) throws IOException{
		ObjectOutputStream out;
		if (fname.endsWith(".gz")){
			out = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(fname)));
		} else {
			out = new ObjectOutputStream(new FileOutputStream(fname));
		}
		out.writeObject(h);
		out.close();
	}
	
	public static LinearTagger loadTagger(String fname) throws IOException, ClassNotFoundException{
		ObjectInputStream in;
		if (fname.endsWith(".gz")){
			in = new ObjectInputStream(new GZIPInputStream(new FileInputStream(fname)));
		} else {
			in = new ObjectInputStream(new FileInputStream(fname));
		}
		LinearTagger h= (LinearTagger) in.readObject();
		in.close();
		return h;
	}


}
