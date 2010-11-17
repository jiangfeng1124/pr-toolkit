package util;

public class ArrayPrinting {

	public static void printDoubleArray(double[][] array, String arrayName) {
		int size1 = array.length;
		int size2 = array[0].length;
		System.out.println(arrayName);
		for (int i = 0; i < size1; i++) {
			for (int j = 0; j < size2; j++) {
				System.out.print(" " + Printing.prettyPrint(array[i][j],
						"00.00E00", 4) + " ");

			}
			System.out.println();
		}
		System.out.println();
	}
	
	public static String doubleArrayToString(double[] array, String[] labels, String arrayName) {
		StringBuffer res = new StringBuffer();
		res.append(arrayName);
		res.append("\n");
		for (int i = 0; i < array.length; i++) {
			if (labels == null){
				res.append(i+"       \t");
			}else{
				res.append(labels[i]+     "\t");
			}
		}
		res.append("sum\n");
		double sum = 0;
		for (int i = 0; i < array.length; i++) {
			res.append(Printing.prettyPrint(array[i],
					"0.00000E00", 8) + "\t");
			sum+=array[i];
		}
		res.append(Printing.prettyPrint(sum,
				"0.00000E00", 8)+"\n");
		return res.toString();
	}
	
	
	
	public static void printDoubleArray(double[] array, String labels[], String arrayName) {
		System.out.println(doubleArrayToString(array, labels,arrayName));
	}
	
	
	public static String doubleArrayToString(double[][] array, String[] labels1, String[] labels2,
			String arrayName){
		StringBuffer res = new StringBuffer();
		res.append(arrayName);
		res.append("\n\t");
		//Calculates the column sum to keeps the sums
		double[] sums = new double[array[0].length+1];
		//Prints rows headings
		for (int i = 0; i < array[0].length; i++) {
			if (labels1 == null){
				res.append(i+"        \t");
			}else{
				res.append(labels1[i]+"        \t");
			}
		}
		res.append("sum\n");
		double sum = 0;
		//For each row print heading
		for (int i = 0; i < array.length; i++) {
			if (labels2 == null){
				res.append(i+"\t");
			}else{
				res.append(labels2[i]+"\t");
			}
			//Print values for that row
			for (int j = 0; j < array[0].length; j++) {
				res.append(" " + Printing.prettyPrint(array[i][j],
						"0.00000E00", 8) + "\t");
				sums[j] += array[i][j]; 
				sum+=array[i][j]; //Sum all values of that row
			}
			//Print row sum
			res.append(Printing.prettyPrint(sum,"0.00000E00", 8)+"\n");
			sums[array[0].length]+=sum;
			sum=0;
		}
		res.append("sum\t");
		//Print values for colums sum
		for (int i = 0; i < array[0].length+1; i++) {
			res.append(Printing.prettyPrint(sums[i],"0.00000E00", 8)+"\t");
		}
		res.append("\n");
		return res.toString();
	}
	
	public static void printDoubleArray(double[][] array, String[] labels1, String[] labels2
			, String arrayName) {
		System.out.println(doubleArrayToString(array, labels1,labels2,arrayName));
	}
	
	
	public static void printIntArray(int[][] array, String[] labels1, String[] labels2, String arrayName,
			int size1, int size2) {
		System.out.println(arrayName);
		for (int i = 0; i < size1; i++) {
			for (int j = 0; j < size2; j++) {
				System.out.print(" " + array[i][j] +  " ");

			}
			System.out.println();
		}
		System.out.println();
	}
	
	public static String intArrayToString(int[] array, String[] labels, String arrayName) {
		StringBuffer res = new StringBuffer();
		res.append(arrayName);
		for (int i = 0; i < array.length; i++) {
			res.append(" " + array[i] + " ");
			
		}
		res.append("\n");
		return res.toString();
	}
	
	public static void printIntArray(int[] array, String[] labels, String arrayName) {
		System.out.println(intArrayToString(array, labels,arrayName));
	}
	
	public static String toString(double[][] d){
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < d.length; i++) {
			for (int j = 0; j < d[0].length; j++) {
				sb.append(Printing.prettyPrint(d[i][j], "0.00E0", 10));
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	
	public static void printDoubleArray(double[] array, String arrayName) {
		System.out.println(arrayName);
		for (int i = 0; i < array.length; i++) {
				System.out.print(" " + Printing.prettyPrint(array[i],
						"00.00E00", 4) + " ");
		}
		System.out.println();
	}
}
