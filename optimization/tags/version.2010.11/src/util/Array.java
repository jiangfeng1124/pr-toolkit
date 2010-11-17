package util;

import java.util.Arrays;

/**
 * General utilities for arrays.
 * 
 * @author javg
 *
 */
public class Array {

	public  static void sortDescending(double[] ds){
		for (int i = 0; i < ds.length; i++) ds[i] = -ds[i];
		Arrays.sort(ds);
		for (int i = 0; i < ds.length; i++) ds[i] = -ds[i];
	}
	
	/** 
	 * Return a new reversed array
	 * @param array
	 * @return
	 */
	public static int[] reverseIntArray(int[] array){
		int[] reversed = new int[array.length];
		for (int i = 0; i < reversed.length; i++) {
			reversed[i] = array[reversed.length-1-i];
		}
		return reversed;
	}
	
		public static void main(String[] args) {
		int[] i = {1,2,3,4};
		util.ArrayPrinting.printIntArray(i, null, "original");
		util.ArrayPrinting.printIntArray(reverseIntArray(i), null, "reversed");
	}
		
		
		public static  double[][] deepclone(double[][] in){
			double[][] res = new double[in.length][];
			for (int i = 0; i < res.length; i++) {
				res[i] = in[i].clone();
			}
			return res;
		}

		
		public static  double[][][] deepclone(double[][][] in){
			double[][][] res = new double[in.length][][];
			for (int i = 0; i < res.length; i++) {
				res[i] = deepclone(in[i]);
			}
			return res;
		}
		
		public static double[][] identity(int size){
			double[][] result = new double[size][size];
			for(int i = 0; i < size; i++){
				result[i][i] = 1;
			}
			return result;
		}
		
}
