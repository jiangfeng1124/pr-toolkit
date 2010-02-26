package optimization.linesearch;

import gnu.trove.TDoubleArrayList;
import gnu.trove.TIntArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import optimization.gradientBasedMethods.Objective;
import optimization.util.MathUtils;
import optimization.util.StaticTools;



import util.MathUtil;
import util.Printing;


/**
 * A wrapper class for the actual objective in order to perform 
 * line search.  The optimization code assumes that this does a lot 
 * of caching in order to simplify legibility.  For the applications 
 * we use it for, caching the entire history of evaluations should be 
 * a win. 
 * 
 * Note: the lastEvaluatedAt value is very important, since we will use
 * it to avoid doing an evaluation of the gradient after the line search.  
 * 
 * The differentiable line search objective defines a search along the ray
 * given by a direction of the main objective.
 * It defines the following function, 
 * where f is the original objective function:
 * g(alpha) = f(x_0 + alpha*direction)
 * g'(alpha) = f'(x_0 + alpha*direction)*direction
 * 
 * @author joao
 *
 */
public class DifferentiableLineSearchObjective {

	int nrIterations;
	
	Objective o;
	TDoubleArrayList steps;
	TDoubleArrayList values;
	TDoubleArrayList gradients;
	
	double[] originalParameters;
	public double[] direction;
	
	//Auxiliar variables to change
	double[] parametersChange;
	double[] realObjectiveGradient;
	
	 
	
	/**
	 * Defines a line search objective:
	 * Receives:
	 * Objective to each we are performing the line search, is used to calculate values and gradients
	 * Direction where to do the ray search, note that the direction does not depend of the 
	 * objective but depends from the method.
	 * @param o
	 * @param direction
	 */
	public DifferentiableLineSearchObjective(Objective o, double[] direction) {
		this.o = o;
		originalParameters = new double[o.getNumParameters()];
		o.getParameters(originalParameters);
		parametersChange = new double[o.getNumParameters()];
		realObjectiveGradient = new double[o.getNumParameters()];
		steps = new TDoubleArrayList();
		values = new TDoubleArrayList();
		gradients = new TDoubleArrayList();
		this.direction = direction;
		//Add initial values
		o.getGradient(realObjectiveGradient);
		steps.add(0);
		values.add(o.getValue());
		gradients.add(MathUtils.dotProduct(realObjectiveGradient,direction));	
//		System.out.println("Iter " + nrIterations + " step " + steps.get(nrIterations) +
//				" value " + values.get(nrIterations) + " grad "  + gradients.get(nrIterations));
	}
	
	public int getNrIterations(){
		return nrIterations;
	}
	
	/**
	 * return g(alpha) for the current value of alpha
	 * @param iter
	 * @return
	 */
	public double getValue(int iter){
		return values.get(iter);
	}
	
	public double getCurrentValue(){
		return values.get(nrIterations);
	}
	
	public double getOriginalValue(){
		return values.get(0);
	}

	/**
	 * return g'(alpha) for the current value of alpha
	 * @param iter
	 * @return
	 */
	public double getGradient(int iter){
		return gradients.get(iter);
	}
	
	public double getCurrentGradient(){
		return gradients.get(nrIterations);
	}
	
	public double getInitialGradient(){
		return gradients.get(0);
	}
	
	/**
	 * update the current value of alpha.
	 * Takes a step with that alpha in direction
	 * Get the real objective value and gradient and calculate all required information.
	 */
	public void updateAlpha(double alpha){
		nrIterations++;
		steps.add(alpha);
		//x_t+1 = x_t - alpha*direction
		parametersChange = originalParameters.clone();
		MathUtils.plusEquals(parametersChange, direction, alpha);
		o.setParameters(parametersChange);
		values.add(o.getValue());
		o.getGradient(realObjectiveGradient);
		gradients.add(MathUtils.dotProduct(realObjectiveGradient,direction));		
	}

	
	
	public double getAlpha(){
		return steps.get(nrIterations);
	}
	
	public void printLineSearchSteps(){
		System.out.println(
				" Steps size "+steps.size() + 
				"Values size "+values.size() +
				"Gradeients size "+gradients.size());
		for(int i =0; i < steps.size();i++){
			System.out.println("Iter " + i + " step " + steps.get(i) +
					" value " + values.get(i) + " grad "  + gradients.get(i));
		}
	}
	
	public void printSmallLineSearchSteps(){
		for(int i =0; i < steps.size();i++){
			System.out.print(StaticTools.prettyPrint(steps.get(i), "0.0000E00",8) + " ");
		}
		System.out.println();
	}
	
	public String parametersToString(){
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < parametersChange.length; i++) {
			sb.append("x"+i+":"+parametersChange[i]+" ");
		}
		return sb.toString();
	}
	
	public static void main(String[] args) {
		
	}
	
}
