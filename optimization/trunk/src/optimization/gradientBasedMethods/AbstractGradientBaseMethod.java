package optimization.gradientBasedMethods;

import optimization.gradientBasedMethods.stats.OptimizerStats;
import optimization.linesearch.LineSearchMethod;
import optimization.util.MathUtils;

/**
 * 
 * @author javg
 *
 */
public abstract class AbstractGradientBaseMethod implements Optimizer{
	
	protected int maxNumberOfIterations=10000;
	/**
	 * Stop if gradientNorm/(originalGradientNorm) smaller
	 * than gradientConvergenceValue
	 */
	protected double gradientConvergenceValue=0.01;
	/**
	 * Stop if previousValue - value < valueConvergenceValue
	 */
	protected double valueConvergenceValue=0.01;
	protected int currentProjectionIteration;
	protected double currValue;	
	protected double previousValue = Double.MAX_VALUE;;
	protected double step;
	protected double[] gradient;
	public double[] direction;
	
	//Original values
	protected double originalGradientL2Norm;
	
	protected LineSearchMethod lineSearch;
	
	public abstract boolean optimize(Objective o,OptimizerStats stats);
	
	
	public boolean stopCriteria(double[] gradient){
		if(originalGradientL2Norm == 0){
			System.out.println("Leaving original norm is zero");
			return true;	
		}
		if (MathUtils.L2Norm(gradient)/originalGradientL2Norm < gradientConvergenceValue) {
			System.out.println("Leaving norm below treshold");
			return true;
		}
		if(previousValue - currValue < valueConvergenceValue) {
			System.out.println("Leaving value change below treshold");
			return true;
		}
		return false;
	}

	public int getCurrentIteration() {
		return currentProjectionIteration;
	}

	public double[] getCurrentGradient() {
		return gradient;
	}
	
	public abstract double[] getDirection();

	public double getCurrentStep() {
		return step;
	}

	public void setGradientConvergenceValue(double value) {
		gradientConvergenceValue = value;	
	}
	
	public void setValueConvergenceValue(double value) {
		valueConvergenceValue = value;	
	}

	public void setMaxIterations(int max) {
		maxNumberOfIterations = max;
	}

	public double getCurrentValue() {
		return currValue;
	}
}
