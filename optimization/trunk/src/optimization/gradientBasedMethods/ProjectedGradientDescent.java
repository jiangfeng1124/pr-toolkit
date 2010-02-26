package optimization.gradientBasedMethods;

import optimization.gradientBasedMethods.stats.OptimizerStats;
import optimization.linesearch.DifferentiableLineSearchObjective;
import optimization.linesearch.LineSearchMethod;
import optimization.linesearch.ProjectedDifferentiableLineSearchObjective;
import optimization.util.MathUtils;


/**
 * This class implements the projected gradiend
 * as described in Bertsekas "Non Linear Programming"
 * section 2.3.
 * 
 * The update is given by:
 * x_k+1 = x_k + alpha^k(xbar_k-x_k)
 * Where xbar is:
 * xbar = [x_k -s_k grad(f(x_k))]+
 * where []+ is the projection into the feasibility set
 * 
 * alpha is the step size 
 * s_k - is a positive scalar which can be view as a step size as well, by 
 * setting alpha to 1, then x_k+1 = [x_k -s_k grad(f(x_k))]+
 * This is called taking a step size along the projection arc (Bertsekas) which
 * we will use by default.
 * 
 * Note that the only place where we actually take a step size is on pick a step size
 * so this is going to be just like a normal gradient descent but use a different 
 * armijo line search where we project after taking a step.
 * 
 * 
 * @author javg
 *
 */
public class ProjectedGradientDescent extends ProjectedAbstractGradientBaseMethod{
	

	public double lastStepUsed;
	ProjectedObjective obj;
	double originalDirenctionL2Norm;
	
	public ProjectedGradientDescent(LineSearchMethod lineSearch) {
		this.lineSearch = lineSearch;
	}
	
	//Use projected gradient norm
	public boolean stopCriteria(double[] gradient){
		if(originalDirenctionL2Norm == 0){
			System.out.println("Leaving original direction norm is zero");
			return true;	
		}
		if(MathUtils.L2Norm(direction)/originalDirenctionL2Norm < gradientConvergenceValue){
			System.out.println("Leaving projected gradient Norm smaller than epsilon");
			return true;	
		}
		if((previousValue - currValue)/Math.abs(previousValue) < valueConvergenceValue) {
			System.out.println("Leaving value change below treshold " + previousValue + " - " + currValue);
			System.out.println(previousValue/currValue + " - " + currValue/currValue 
					+ " = " + (previousValue - currValue)/Math.abs(previousValue));
			return true;
		}
		return false;
	}
	
	public boolean optimize(ProjectedObjective o,OptimizerStats stats){
			stats.collectInitStats(this, o);
			obj = o;
			step = 0;
			currValue = o.getValue();
			previousValue = Double.MAX_VALUE;
			gradient = new double[o.getNumParameters()];
			o.getGradient(gradient);
			originalGradientL2Norm = MathUtils.L2Norm(gradient);
			
			
			getDirection();
			originalDirenctionL2Norm = MathUtils.L2Norm(direction);
			//MatrixOutput.printDoubleArray(currParameters, "parameters");
			for (currentProjectionIteration = 0; currentProjectionIteration < maxNumberOfIterations; currentProjectionIteration++){		
			//	System.out.println("Iter " + currentProjectionIteration);
				//o.printParameters();
				
				if(stopCriteria(direction)){
					stats.collectFinalStats(this, o);
					lastStepUsed = step;
					return true;
				}			
				
				
				
				ProjectedDifferentiableLineSearchObjective lso = new ProjectedDifferentiableLineSearchObjective(o,direction);
				step = lineSearch.getStepSize(lso);
				if(step==-1){
					System.out.println("Failed to find step");
					stats.collectFinalStats(this, o);
					return false;	
					
				}
				
				//Update the direction for stopping criteria
				previousValue = currValue;
				currValue = o.getValue();
				o.getGradient(gradient);
				direction = getDirection();		
				if(MathUtils.dotProduct(gradient, direction) > 0){
					System.out.println("Not a descent direction");
					System.out.println(" current stats " + stats.prettyPrint(1));
					System.exit(-1);
				}
				stats.collectIterationStats(this, o);
			}
			lastStepUsed = step;
			stats.collectFinalStats(this, o);
			return false;
		}
	
	public boolean optimize(Objective o,OptimizerStats stats){
		System.out.println("Objective is not a projected objective");
		System.exit(-1);
		return false;
	}
	
	@Override
	public double[] getDirection() {
		double[] currParameters = new double[obj.getNumParameters()];
		obj.getParameters(currParameters);
		//Calculating projected gradient take a step and project somewhere
		//then calculate the direction
		double[] projectedParameters = currParameters.clone();
		MathUtils.minusEquals(projectedParameters, gradient, 1000);
		direction = MathUtils.arrayMinus(projectedParameters, currParameters);
		return direction;
	}

		
}
