package optimization.linesearch;

import optimization.util.Interpolation;
import optimization.util.MathUtils;





/**
 * Extends the armijo rule along the projection arc to the non-monotone version
 * Grippo, Lampariello and Lucidi (86) where we only require the armijo condition to be
 * satistified by one of the last iterations of the gradient algorithm and not just the last.
 * 
 * 
 * @author javg
 *
 */
public class NonMonotoneArmijoLineSearchMinimizationAlongProjectionArc implements LineSearchMethod{

	/**
	 * How much should the step size decrease at each iteration.
	 */
	double contractionFactor = 0.5;
	double c1 = 0.0001;
	
	
	public double initialStep;
	int maxIterations = 100;
			
	
	//Experiment
	double previousStepPicked = -1;;
	double previousInitGradientDot = -1;
	double currentInitGradientDot = -1;
	
	
	//history
	double[] previousInitialFunctionValues;
	int historySize = 5;
	double sigma1 = 0.1;
	double sigma2 = 0.9;
	
	
	public NonMonotoneArmijoLineSearchMinimizationAlongProjectionArc(){
		this.initialStep = 1;
		previousInitialFunctionValues = new double[historySize];
		java.util.Arrays.fill(previousInitialFunctionValues, Double.MIN_VALUE);
	}
	
	public NonMonotoneArmijoLineSearchMinimizationAlongProjectionArc(GenericPickFirstStep strategy){
		previousInitialFunctionValues = new double[historySize];
		java.util.Arrays.fill(previousInitialFunctionValues, Double.MIN_VALUE);
		this.initialStep = strategy.getFirstStep(this);
	}
	
	
	public void setInitialStep(double initial){
		previousInitialFunctionValues = new double[historySize];
		java.util.Arrays.fill(previousInitialFunctionValues, Double.MIN_VALUE);
		initialStep = initial;
	}
	
	/**
	 * 
	 */
	
	public double getStepSize(DifferentiableLineSearchObjective o) {	
		if(!(o instanceof ProjectedDifferentiableLineSearchObjective)){
			System.out.println("Requires a ProjectedDifferentiableLineSearchObjective");
			System.exit(-1);
		}
		ProjectedDifferentiableLineSearchObjective obj = (ProjectedDifferentiableLineSearchObjective) o;
		currentInitGradientDot = o.getInitialGradient();	
		
		if(o.nrIterations > historySize){
			for(int i = 1; i < historySize; i++){
				previousInitialFunctionValues[i-1]=previousInitialFunctionValues[i];
			}
			previousInitialFunctionValues[historySize-1]=o.getOriginalValue();
		}else{
			previousInitialFunctionValues[o.nrIterations] = o.getOriginalValue();
		}
		
		
		double maxFunctionVal = Double.MIN_VALUE;
		for (int i = 0; i < previousInitialFunctionValues.length; i++) {
			if(previousInitialFunctionValues[i] > maxFunctionVal){
				maxFunctionVal = previousInitialFunctionValues[i];
			}
		}
		//Should update all in the objective
		obj.updateAlpha(initialStep);	
		int nrIterations = 0;
	
		
		while(!satisfyArmijoCondition(obj,maxFunctionVal)){			
			if(nrIterations >= maxIterations){
				System.out.println("Could not find a step leaving line search with -1");
				o.printLineSearchSteps();
				return -1;
			}
			double alpha=obj.getAlpha();
			double alphaTemp = 
				Interpolation.quadraticInterpolation(obj.getOriginalValue(), obj.getInitialGradient(), alpha, obj.getCurrentValue());
			if(alphaTemp >= sigma1 || alphaTemp <= sigma2*obj.getAlpha()){
				alpha = alphaTemp;
			}else{
				alpha = alpha*contractionFactor;
			}
			obj.updateAlpha(alpha);
			nrIterations++;			
		}

		
//		System.out.println("Leavning line search used:");
//		o.printLineSearchSteps();	
		previousInitGradientDot = currentInitGradientDot;
		previousStepPicked = o.getAlpha();
		return obj.getAlpha();
	}
	
	public boolean satisfyArmijoCondition(ProjectedDifferentiableLineSearchObjective obj, double maxFunctionVal){
//		System.out.println("Curr" + obj.getCurrentValue() + " alpha " + obj.getAlpha());
//		System.out.println("Max" + maxFunctionVal );
//		System.out.println("Decrease" + c1*(MathUtils.dotProduct(obj.originalGradient,
//				MathUtils.arrayMinus(obj.originalParameters,obj.parametersChange))));
		return obj.getCurrentValue() <=
				maxFunctionVal -
					c1*(MathUtils.dotProduct(obj.originalGradient,
							MathUtils.arrayMinus(obj.originalParameters,obj.parametersChange)));
		
	}
	
	public double getInitialGradient() {
		return currentInitGradientDot;
		
	}

	public double getPreviousInitialGradient() {
		return previousInitGradientDot;
	}

	public double getPreviousStepUsed() {
		return previousStepPicked;
	}
		
}
