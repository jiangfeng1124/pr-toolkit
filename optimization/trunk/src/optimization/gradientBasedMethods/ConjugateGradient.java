package optimization.gradientBasedMethods;

import optimization.gradientBasedMethods.stats.OptimizerStats;
import optimization.linesearch.DifferentiableLineSearchObjective;
import optimization.linesearch.LineSearchMethod;
import optimization.util.MathUtils;



public class ConjugateGradient extends AbstractGradientBaseMethod{
	
	
	double[] previousGradient;
	double[] previousDirection;

	public ConjugateGradient(LineSearchMethod lineSearch) {
		this.lineSearch = lineSearch;
	}
	
	public boolean optimize(Objective o,OptimizerStats stats){
		stats.collectInitStats(this, o);
		gradient = new double[o.getNumParameters()];
		previousGradient = new double[o.getNumParameters()];
		previousDirection = new double[o.getNumParameters()];
		direction = new double[o.getNumParameters()];
		double[] currParameters = new double[o.getNumParameters()];
		previousValue = Double.MAX_VALUE;
		currValue= o.getValue();
		o.getGradient(gradient);
		originalGradientL2Norm=MathUtils.L2Norm(gradient);
		for (currentProjectionIteration = 0; currentProjectionIteration < maxNumberOfIterations; currentProjectionIteration++){
			//Save previous values
			
			//System.out.println("Iter " + currentProjectionIteration + " value " + currValue + " gradient " + gradient[0]+"-"+ gradient[1]);
			
			o.getParameters(currParameters);
			if(stopCriteria(gradient)){
				stats.collectFinalStats(this, o);
				return true;
			}
			getDirection();
			DifferentiableLineSearchObjective lso = new DifferentiableLineSearchObjective(o,direction);
			step = lineSearch.getStepSize(lso);	
			if(step==-1){
				System.out.println("Failed to find a step size");
				System.out.println("Failed to find step");
				stats.collectFinalStats(this, o);
				return false;	
			}
			previousValue = currValue;
			currValue = o.getValue();
			System.arraycopy(gradient, 0, previousGradient, 0, gradient.length);
			System.arraycopy(direction, 0, previousDirection, 0, direction.length);		
			o.getGradient(gradient);
			stats.collectIterationStats(this, o);
		}
		stats.collectFinalStats(this, o);
		return false;
	}
	
	public double[] getDirection(){
		direction = MathUtils.negation(gradient);
		if(currentProjectionIteration != 0){
			//Using Polak-Ribiere method (book equation 5.45)
			double b = MathUtils.dotProduct(gradient, MathUtils.arrayMinus(gradient, previousGradient))
			/MathUtils.dotProduct(previousGradient, previousGradient);
			if(b<0){
				//System.out.println("Defaulting to gradient descent");
				b = Math.max(b, 0);
			}
			MathUtils.plusEquals(direction, previousDirection, b);
			//Debug code
			if(MathUtils.dotProduct(direction, gradient) > 0){
				System.out.println("Not an descent direction reseting to gradien");
				direction = MathUtils.negation(gradient);
			}
		}else{
			//System.out.println("At iteration 1 returning gradient\n\n ");
		}
		return direction;
	}
	
	
	



}
