package optimization.gradientBasedMethods;



import optimization.gradientBasedMethods.stats.OptimizerStats;
import optimization.linesearch.LineSearchMethod;
import optimization.linesearch.ProjectedDifferentiableLineSearchObjective;
import optimization.util.MathUtils;


/**
 * This class implementes the non-monotone Spectral Projected gradient
 * Birgin, Martines and Raydan
 * 
 * Combination of projected gradient with two new features:
 *    - Non Monotone Line Search defined by Grippo, Lampariello and Lucidi (86)
 *    - Uses the spectral stepLenght by Barzilai and Borwein (88)
 * 
 * Requires a projected objective
 * 
 * @author javg
 *
 */
public class NonMonotoneSpectralProjectedGradient extends AbstractGradientBaseMethod{
	
	public double[] projectedGradient;
	
	public NonMonotoneSpectralProjectedGradient(LineSearchMethod lineSearch) {
		this.lineSearch = lineSearch;
	}
	
	double previousParameters[];
	double previousGradient[];
	double alphaMax;
	double alphaMin;
	double sigma1;
	double sigma2;
	
	/**
	 * Following the algorithm from the paper..
	 * @param o
	 * @param stats
	 * @return
	 * 
	 * alphaMin, alphaMax - Smaller and MaxStep
	 * c1 - constant for armijo rule
	 * 0<sigma1<sigma2<1 safeguards
	 * 
	 * Pick alpha0 in [alphaMin, alphaMax]
	 * 
	 */
	public boolean optimize(ProjectedObjective o,OptimizerStats stats){
			stats.collectInitStats(this, o);
			step = 0;
			currValue = o.getValue();
			previousValue = Double.MAX_VALUE;
			gradient = new double[o.getNumParameters()];
			previousGradient = new double[o.getNumParameters()];
			o.getGradient(gradient);
			projectedGradient = gradient.clone();
			originalGradientL2Norm = MathUtils.L2Norm(gradient);
			
			double[] currParameters = new double[o.getNumParameters()];
			previousParameters = new double[o.getNumParameters()];
			o.getParameters(currParameters);
			
			direction = new double[gradient.length];
			//MatrixOutput.printDoubleArray(currParameters, "parameters");
			for (currentProjectionIteration = 0; currentProjectionIteration < maxNumberOfIterations; currentProjectionIteration++){		
				System.out.println("Iter " + currentProjectionIteration);
				
				//Step 1 see the stoopign criteria
				if(stopCriteria(gradient)){
					stats.collectFinalStats(this, o);
					return true;
				}			
				
				System.arraycopy(currParameters, 0, previousParameters, 0, currParameters.length);
				System.arraycopy(gradient, 0, previousGradient, 0, gradient.length);
				//Step 2 take a step
				ProjectedDifferentiableLineSearchObjective lso = new ProjectedDifferentiableLineSearchObjective(o,direction);
				step = lineSearch.getStepSize(lso);
				if(step==-1){
					System.out.println("Failed to find step");
					stats.collectFinalStats(this, o);
					return false;	
					
				}
				getDirection();
				if(MathUtils.dotProduct(gradient, direction) > 0){
					System.out.println("Not a descent direction");
					System.out.println(" current stats " + stats.prettyPrint(1));
					System.exit(-1);
				}
				//Step size already keeps the true objective updated.
				previousValue = currValue;
				currValue = o.getValue();
				o.getGradient(gradient);

				stats.collectIterationStats(this, o);
				
				//Step three calculate new initial step
				//Diference between parameteres
				double[] diffX = MathUtils.arrayMinus(currParameters, previousParameters);
				double[] diffGrad = MathUtils.arrayMinus(gradient, previousGradient);
				double bk = MathUtils.dotProduct(diffX, diffGrad);
				if(bk <= 0){
					step = alphaMax;
					
				}else{
					step = MathUtils.dotProduct(diffX, diffX);
					step = Math.min(alphaMax, Math.max(alphaMin,step/bk));
				}
			}
			
			stats.collectFinalStats(this, o);
			return false;
		}
	
	
	//Use projected gradient norm
	public boolean stopCriteria(double[] gradient){
		if(originalGradientL2Norm == 0){
			System.out.println("Leaving original norm is zero");
			return true;	
		}
		if (MathUtils.L2Norm(projectedGradient)/originalGradientL2Norm < gradientConvergenceValue) {
			System.out.println("Leaving norm below treshold");
			return true;
		}
		if(previousValue - currValue < valueConvergenceValue) {
			System.out.println("Leaving value change below treshold");
			return true;
		}
		return false;
	}
	
	public boolean optimize(Objective o,OptimizerStats stats){
		System.out.println("Objective is not a projected objective");
		System.exit(-1);
		return false;
	}
	
	@Override
	public double[] getDirection() {
		for(int i = 0; i< gradient.length; i++){
			direction[i] = -gradient[i];
		}
		return direction;
	}	
}
