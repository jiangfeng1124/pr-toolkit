package optimization.gradientBasedMethods;

import optimization.gradientBasedMethods.stats.OptimizerStats;
import optimization.linesearch.DifferentiableLineSearchObjective;
import optimization.linesearch.LineSearchMethod;
import optimization.util.MathUtils;
import optimization.util.MatrixOutput;



public class GradientDescent extends AbstractGradientBaseMethod{
	
	
	
	public GradientDescent(LineSearchMethod lineSearch) {
		this.lineSearch = lineSearch;
	}
		
	public boolean optimize(Objective o,OptimizerStats stats){
		stats.collectInitStats(this, o);
		step = 0;
		currValue = o.getValue();
		gradient = new double[o.getNumParameters()];
		o.getGradient(gradient);
		originalGradientL2Norm = MathUtils.L2Norm(gradient);
		previousValue = Double.MAX_VALUE;
		double[] currParameters = new double[o.getNumParameters()];
		o.getParameters(currParameters);
		double[] prevGradient = new double[gradient.length];
		direction = new double[gradient.length];
		//MatrixOutput.printDoubleArray(currParameters, "parameters");
		for (currentProjectionIteration = 0; currentProjectionIteration < maxNumberOfIterations; currentProjectionIteration++){		
			//System.out.println("Iter " + currentProjectionIteration);
			
			if(stopCriteria(gradient)){
				stats.collectFinalStats(this, o);
				return true;
			}			
			getDirection();
			if(MathUtils.dotProduct(gradient, direction) > 0){
				System.out.println("Not a descent direction");
				System.out.println(" current stats " + stats.prettyPrint(1));
				System.exit(-1);
			}
			
			DifferentiableLineSearchObjective lso = new DifferentiableLineSearchObjective(o,direction);
			step = lineSearch.getStepSize(lso);
			if(step==-1){
				System.out.println("Failed to find step");
				stats.collectFinalStats(this, o);
				return false;	
				
			}
			//Step size already keeps the true objective updated.
			previousValue = currValue;
			currValue = o.getValue();
			o.getGradient(gradient);
			stats.collectIterationStats(this, o);
		}
		
		stats.collectFinalStats(this, o);
		return false;
	}
	
	public double[] getDirection(){
		for(int i = 0; i< gradient.length; i++){
			direction[i] = -gradient[i];
		}
		return direction;
	}
}
