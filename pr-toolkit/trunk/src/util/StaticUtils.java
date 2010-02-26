package util;

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
	

}
