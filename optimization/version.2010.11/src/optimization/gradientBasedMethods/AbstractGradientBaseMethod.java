package optimization.gradientBasedMethods;

import optimization.gradientBasedMethods.stats.AbstractOptimizerStats;
import optimization.gradientBasedMethods.stats.OptimizerStats;
import optimization.linesearch.DifferentiableLineSearchObjective;
import optimization.linesearch.LineSearchMethod;
import optimization.stopCriteria.StopingCriteria;
import util.ArrayMath;

/**
 * 
 * @author javg
 *
 */
public abstract class AbstractGradientBaseMethod implements Optimizer{
	
	protected int maxNumberOfIterations=10000;
	
	
	
	protected int currentProjectionIteration;
	protected double currValue;	
	protected double previousValue = Double.MAX_VALUE;;
	protected double step;
	protected double[] gradient;
	public double[] direction;
	
	//Original values
	protected double originalGradientL2Norm;
	
	protected LineSearchMethod lineSearch;
	DifferentiableLineSearchObjective lso;
	
	
	public void reset(){
		direction = null;
		gradient = null;
		previousValue = Double.MAX_VALUE;
		currentProjectionIteration = 0;
		originalGradientL2Norm = 0;
		step = 0;
		currValue = 0;
	}
	
	public void initializeStructures(Objective o,AbstractOptimizerStats stats, StopingCriteria stop){
		lso =   new DifferentiableLineSearchObjective(o);
	}
	public void updateStructuresBeforeStep(Objective o,AbstractOptimizerStats stats, StopingCriteria stop){
	}
	
	public void updateStructuresAfterStep(Objective o,AbstractOptimizerStats stats, StopingCriteria stop){
	}
	
	public void updateStructuresBeginIteration(Objective o){
		previousValue = currValue;
		currValue = o.getValue();
		gradient = o.getGradient();
	}
	
	public boolean optimize(Objective o,AbstractOptimizerStats stats, StopingCriteria stop){
		//Initialize structures
			
		stats.collectInitStats(this, o);
		direction = new double[o.getNumParameters()];
		initializeStructures(o, stats, stop);
		for (currentProjectionIteration = 0; currentProjectionIteration < maxNumberOfIterations; currentProjectionIteration++){		
			if(o.debugLevel > 3){
				System.err.println("Iter: " + currentProjectionIteration + " : " + o.toString());
			}
			updateStructuresBeginIteration(o);
			if(stop.stopOptimization(o)){
				stats.collectFinalStats(this, o,true);
				return true;
			}	
			getDirection();
			if(ArrayMath.dotProduct(gradient, direction) > 0){
				throw new NotADescentDirectionException("Not a descent direction current stats\n" + stats.prettyPrint(1));
			}
			updateStructuresBeforeStep(o, stats, stop);
			step = getStepSize(stats);
			if(step==-1){
				System.err.println("Failed to find step");
				lso.printSmallLineSearchSteps(System.err);
				stats.collectFinalStats(this, o,false);
				return false;		
			}
			updateStructuresAfterStep( o, stats,  stop);
			stats.collectIterationStats(this, o); 
		}
		stats.collectFinalStats(this, o,false);
		return false;
	}
	
	public double getStepSize(AbstractOptimizerStats stats){
		lso.reset(direction);
		double step = lineSearch.getStepSize(lso);
		return step;
	}
	
	public int getCurrentIteration() {
		return currentProjectionIteration;
	}

	
	/**
	 * Method specific
	 */
	public abstract double[] getDirection();

	public double getCurrentStep() {
		return step;
	}



	public void setMaxIterations(int max) {
		maxNumberOfIterations = max;
	}

	public double getCurrentValue() {
		return currValue;
	}
}
