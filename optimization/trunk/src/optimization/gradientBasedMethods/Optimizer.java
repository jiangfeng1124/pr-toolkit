package optimization.gradientBasedMethods;

import optimization.gradientBasedMethods.stats.OptimizerStats;

public interface Optimizer {
	public boolean optimize(Objective o,OptimizerStats stats);
	
	public int getCurrentIteration();
	public double[] getCurrentGradient();
	public double[] getDirection();
	public double getCurrentStep();
	public double getCurrentValue();
	
	public void setMaxIterations(int max);
	public void setGradientConvergenceValue(double value);
	public void setValueConvergenceValue(double value);
	
}
