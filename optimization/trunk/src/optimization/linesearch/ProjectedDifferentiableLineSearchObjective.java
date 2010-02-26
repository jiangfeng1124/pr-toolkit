package optimization.linesearch;

import optimization.gradientBasedMethods.AbstractGradientBaseMethod;
import optimization.gradientBasedMethods.Objective;
import optimization.gradientBasedMethods.ProjectedGradientDescent;
import optimization.gradientBasedMethods.ProjectedObjective;
import optimization.util.MathUtils;
import optimization.util.MatrixOutput;
import util.Printing;


/**
 * See ArmijoLineSearchMinimizationAlongProjectionArc for description
 * @author javg
 *
 */
public class ProjectedDifferentiableLineSearchObjective extends DifferentiableLineSearchObjective{

	
	//Need to store the original gradient since now we will step in the negative gradient direction
	public double[] originalGradient;
	
	
	
	
	
	public ProjectedDifferentiableLineSearchObjective(Objective o,
			double[] direction) {
		super(o, direction);
		if(!(o instanceof ProjectedObjective)){
			System.out.println("Must receive a projected objective");
			System.exit(-1);
		}
		originalGradient = new double[o.getNumParameters()];
		o.getGradient(originalGradient);
		//We don't want it to change
		originalGradient = originalGradient.clone();
		//System.out.println("Starting a new line search objective");
	}

	public double[] projectPoint (double[] point){
		return ((ProjectedObjective)o).projectPoint(point);
	}
	
	/**
	 * 
	 */
	public void updateAlpha(double alpha){
		nrIterations++;
		steps.add(alpha);
		//x_t+1 = x_t - alpha*direction
		parametersChange = originalParameters.clone();
//		MatrixOutput.printDoubleArray(parametersChange, "parameters before step");
//		MatrixOutput.printDoubleArray(originalGradient, "gradient + " + alpha);

		MathUtils.minusEquals(parametersChange, originalGradient, alpha);
		
		//Project the points into the feasibility set
//		MatrixOutput.printDoubleArray(parametersChange, "before projection");
		//x_k(alpha) = [x_k - alpha*grad f(x_k)]+
		parametersChange = projectPoint(parametersChange);
//		MatrixOutput.printDoubleArray(parametersChange, "after projection");
		o.setParameters(parametersChange);
		values.add(o.getValue());
		//Computes the new direction x_k-[x_k-alpha*Gradient(x_k)]+
		
		direction=MathUtils.arrayMinus(parametersChange,originalParameters);
		
		
		double gradient = MathUtils.dotProduct(originalGradient,direction);
		
		if(gradient > 0){
			System.out.println("ProjecteLineSearchObjective: Should be a descent direction: " + gradient);
//			System.out.println(Printing.doubleArrayToString(originalGradient, null,"Original gradient"));
//			System.out.println(Printing.doubleArrayToString(originalParameters, null,"Original parameters"));
//			System.out.println(Printing.doubleArrayToString(parametersChange, null,"Projected parameters"));
//			System.out.println(Printing.doubleArrayToString(direction, null,"Direction"));
			System.exit(-1);
		}
		gradients.add(gradient);		
	}
	
}
