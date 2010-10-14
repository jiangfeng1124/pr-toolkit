package optimization.projections;

import util.ArrayMath;
import util.ArrayPrinting;



public abstract class Projection {

	
	public abstract void project(double[] original);
	
	
	/**
	 *  From the projection theorem "Non-Linear Programming" page
	 *  201 fact 2.
	 *  
	 *  Given some z in R, and a vector x* in X;
	 *  x* = z+ iif for all x in X 
	 *  (z-x*)'(x-x*) <= 0 where 0 is when x*=x
	 *  See figure 2.16 in book
	 *  
	 * @param original
	 * @param projected
	 * @return
	 */
	public boolean testProjection(double[] original, double[] projected){
		double[] original1 = original.clone();
		//System.out.println(Printing.doubleArrayToString(original1, null, "original"));
		//System.out.println(Printing.doubleArrayToString(projected, null, "projected"));
		ArrayMath.minusEquals(original1, projected, 1);
		//System.out.println(Printing.doubleArrayToString(original1, null, "minus1"));
		for(int i = 0; i < 10; i++){
			double[] x = samplePoint(original.length);
		//	System.out.println(Printing.doubleArrayToString(x, null, "sample"));
			//If the same this returns zero so we are there.	
			ArrayMath.minusEquals(x, projected, 1);
		//	System.out.println(Printing.doubleArrayToString(x, null, "minus2"));
			double dotProd = ArrayMath.dotProduct(original1, x);
			
		//	System.out.println("dot " + dotProd);
			if(dotProd > 0) {
				System.out.println("Dot Product = " + dotProd);
				System.out.println();
				return false;
			}
		}
//		System.out.println(ArrayPrinting.doubleArrayToString(projected, null, "Projected before start perutbations"));
//		//Perturbs the point a bit in all possible directions
//		for(int i = 0; i < original.length; i++){
//			double[] x = perturbePoint(projected,i);
//		//	System.out.println(Printing.doubleArrayToString(x, null, "perturbed"));
//			//If the same this returns zero so we are there.	
//			ArrayMath.minusEquals(x, projected, 1);
//		//	System.out.println(Printing.doubleArrayToString(x, null, "minus2"));
//			double dotProd = ArrayMath.dotProduct(original1, x);
//			
//		//	System.out.println("dot " + dotProd);
//			if(dotProd > 0) {
//				System.out.println(ArrayPrinting.doubleArrayToString(x, null, "perturbed x for dim " + i));
//				System.out.println("Dot Product for dimension i " + i + " = " + dotProd);
//				return false;
//			}
//		}		
		return true;
	}

	//Samples a point from the constrained set
	public abstract double[] samplePoint(int dimensions);
	
	//Perturbs a point a bit still leaving it at the constraints set
	public abstract double[] perturbePoint(double[] point, int parameter);
	
	
}
