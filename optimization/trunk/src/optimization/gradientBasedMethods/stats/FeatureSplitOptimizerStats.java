package optimization.gradientBasedMethods.stats;



import optimization.gradientBasedMethods.Objective;
import optimization.gradientBasedMethods.Optimizer;

/**
 * Extends the optimization stats with a method to split the features in two sets
 * like coarse and grain. It receives the indexes of the weights that should go to 
 * place one.
 * @author javg
 *
 */
public class FeatureSplitOptimizerStats extends OptimizerStats{
	int[] positions;
	int nrPositions;
	public double individiualWeightsL2 = 0;
	public double individiualGradL2 = 0;
	public double coarceWeightsL2 = 0;
	public double coarcelGradL2 = 0;
	
	public FeatureSplitOptimizerStats(int[] positions) {
		super();
		this.positions = positions;
		nrPositions = positions.length;
	}
	
	public void reset(){
		super.reset();
		individiualWeightsL2 = 0;
		individiualGradL2 = 0;
		coarceWeightsL2 = 0;
		coarcelGradL2 = 0;
	}
	public void collectFinalStats(Optimizer optimizer, Objective objective, boolean success){
		super.collectFinalStats(optimizer, objective, success);
		divideWeightsByFeatuares(optimizer, objective)	;
		
	}
	
	public void divideWeightsByFeatuares(Optimizer optimizer, Objective objective){
		individiualWeightsL2 = 0;
		individiualGradL2 = 0;
		double[] gradient = objective.getGradient();
		double[] parameters = objective.getParameters();
		for(int i  =0; i < nrPositions; i++){
			double grad = gradient[positions[i]];
			individiualGradL2 += grad*grad;
			double weights = parameters[positions[i]];
			individiualWeightsL2 += weights*weights;
		}
		coarceWeightsL2 = weightsNorm*weightsNorm - individiualWeightsL2;
		coarcelGradL2 = gradientNorms.get(gradientNorms.size()-1)*gradientNorms.get(gradientNorms.size()-1) - individiualGradL2;
		individiualGradL2 = Math.sqrt(individiualGradL2);
		individiualWeightsL2 = Math.sqrt(individiualWeightsL2);
		coarceWeightsL2 = Math.sqrt(coarceWeightsL2);
		coarcelGradL2 = Math.sqrt(coarcelGradL2);
	}
	
}
