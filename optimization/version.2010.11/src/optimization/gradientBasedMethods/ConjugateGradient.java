package optimization.gradientBasedMethods;

import optimization.gradientBasedMethods.stats.AbstractOptimizerStats;
import optimization.linesearch.LineSearchMethod;
import optimization.stopCriteria.StopingCriteria;
import util.ArrayMath;



public class ConjugateGradient extends AbstractGradientBaseMethod{
	
	
	double[] previousGradient;
	double[] previousDirection;

	public ConjugateGradient(LineSearchMethod lineSearch) {
		this.lineSearch = lineSearch;
	}
	@Override
	public void reset(){
		super.reset();
		java.util.Arrays.fill(previousDirection, 0);
		java.util.Arrays.fill(previousGradient, 0);
	}
	@Override
	public void initializeStructures(Objective o,AbstractOptimizerStats stats, StopingCriteria stop){
		super.initializeStructures(o, stats, stop);
		previousGradient = new double[o.getNumParameters()];
		previousDirection = new double[o.getNumParameters()];
	}

	@Override
	public void updateStructuresBeginIteration(Objective o){
		super.updateStructuresBeginIteration(o);
		//Update previous gradient and direction
		System.arraycopy(gradient, 0, previousGradient, 0, gradient.length);
		System.arraycopy(direction, 0, previousDirection, 0, direction.length);
	}
	
	@Override
	public double[] getDirection(){
		direction = ArrayMath.negation(gradient);
		if(currentProjectionIteration != 0){
			
			//Using Polak-Ribiere method (book equation 5.45)
			double b = ArrayMath.dotProduct(gradient, ArrayMath.arrayMinus(gradient, previousGradient))
			/ArrayMath.dotProduct(previousGradient, previousGradient);
			
			b = Math.max(b, 0);
			
			
			ArrayMath.plusEquals(direction, previousDirection, b);
			//Debug code
			if(ArrayMath.dotProduct(direction, gradient) > 0){
				System.err.println("ConjugateGradient::Not a descent direction setting direction to gradient");
				direction = ArrayMath.negation(gradient);
			}
		}
		return direction;
	}
	
	
	



}
