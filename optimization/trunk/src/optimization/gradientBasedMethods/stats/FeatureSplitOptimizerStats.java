package optimization.gradientBasedMethods.stats;



import optimization.gradientBasedMethods.Objective;
import optimization.gradientBasedMethods.Optimizer;

/**
 * Extends the optimization stats and computes the l2^2 norm of weight and parameters
 * for different positions of the vectors. The positions are received as arguments and 
 * concern the normally different features sets. 
 * @author javg
 */
public class FeatureSplitOptimizerStats extends OptimizerStats{
	int[][] positions;
	public double[] weightsPerFeat;
	public double[] gradPerFeat;
	
	//Differnt sets of positions to collect stats
	public FeatureSplitOptimizerStats(int[][] positions) {
		super();
		
		this.positions = positions;
		weightsPerFeat = new double[positions.length];
		gradPerFeat = new double[positions.length];
	}
	
	public void reset(){
		super.reset();
		java.util.Arrays.fill(weightsPerFeat, 0);
		java.util.Arrays.fill(gradPerFeat, 0);
	}
	public void collectFinalStats(Optimizer optimizer, Objective objective, boolean success){
		super.collectFinalStats(optimizer, objective, success);
		divideWeightsByFeatuares(optimizer, objective)	;
		
	}
	
	public void divideWeightsByFeatuares(Optimizer optimizer, Objective objective){		
		double[] gradient = objective.getGradient();
		double[] parameters = objective.getParameters();
		for(int featType = 0; featType < positions.length; featType++){
			int[] positionsPerFeat = positions[featType];
			int nrPositions = positionsPerFeat.length;
			double gradFeat = 0;
			double weightFeat = 0;
			for(int i  =0; i < nrPositions; i++){
				double grad = gradient[positionsPerFeat[i]];
				gradFeat += grad*grad;
				double weights = parameters[positionsPerFeat[i]];
				weightFeat += weights*weights;
			}
			gradPerFeat[featType] = gradFeat;
			weightsPerFeat[featType] = weightFeat;
		}
	}
}
