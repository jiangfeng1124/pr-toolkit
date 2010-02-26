package optimization.gradientBasedMethods;

import optimization.gradientBasedMethods.stats.OptimizerStats;
import optimization.linesearch.DifferentiableLineSearchObjective;
import optimization.linesearch.LineSearchMethod;
import optimization.util.MathUtils;

/**
 * 
 * @author javg
 *
 */
public class BFGS extends AbstractGradientBaseMethod{

	
	double[][] inverseHessian;
	
	public BFGS(LineSearchMethod ls) {
		lineSearch = ls;
	}
	
	public boolean optimize(Objective o, OptimizerStats stats){
		gradient = new double[o.getNumParameters()];
		direction = new double[o.getNumParameters()];
		double[] previousGradient = new double[o.getNumParameters()];
		double[] previousDirection = new double[o.getNumParameters()];
		double[] currParameters = new double[o.getNumParameters()];
		double[] previousParameters = new double[o.getNumParameters()];
		inverseHessian = new double[o.getNumParameters()][o.getNumParameters()];
		for(int i = 0; i < o.getNumParameters(); i++){
			inverseHessian[i][i] = 1;
		}
		stats.collectInitStats(this, o);
		//TODO repeated
		o.getGradient(gradient);
		originalGradientL2Norm=MathUtils.L2Norm(gradient);
		previousValue = Double.MAX_VALUE;
		for (currentProjectionIteration = 0; currentProjectionIteration < maxNumberOfIterations; currentProjectionIteration++){
			
			currValue = o.getValue();
			o.getGradient(gradient);
			o.getParameters(currParameters);
			getDirection();
			
			//MatrixOutput.printDoubleArray(direction, "direction");
			if(MathUtils.dotProduct(direction, gradient) > 0){				
				System.out.println("Not a descent direction");
				System.exit(-1);
			}
			DifferentiableLineSearchObjective lso = new DifferentiableLineSearchObjective(o,direction);
			step = lineSearch.getStepSize(lso);
			if(step==-1){
				System.out.println("Failed to find a step size");
				System.out.println("Failed to find step");
				stats.collectFinalStats(this, o);
				return false;	
			}
			stats.collectIterationStats(this, o);
			System.arraycopy(currParameters, 0, previousParameters, 0, currParameters.length);
			
			double[] diffX = MathUtils.arrayMinus(currParameters, previousParameters);
			//MatrixOutput.printDoubleArray(diffX, "diffX");
			System.arraycopy(gradient, 0, previousGradient, 0, gradient.length);
			o.getGradient(gradient);
			double norm = MathUtils.L2Norm(gradient);
			if(stopCriteria(gradient)){
				stats.collectFinalStats(this, o);
				return true;
			}
			double[] diffGrad = MathUtils.arrayMinus(gradient, previousGradient);
			//MatrixOutput.printDoubleArray(diffGrad, "diffGrad");
			//Aproximate first iteration
			if(currentProjectionIteration == 0){
				double scalar = MathUtils.dotProduct(diffX, diffGrad)/MathUtils.dotProduct(diffX, diffX);
				//System.out.println("Scalar " + scalar);
				MathUtils.matrixScalarMultiplication(inverseHessian, scalar);
				//MatrixOutput.printDoubleArray(inverseHessian, "Initial iterate");
			}
			inverseHessian = geInvtHessianAproximation(diffX, diffGrad, inverseHessian);
			//MatrixOutput.printDoubleArray(inverseHessian, "inverseHessian");
			previousValue=currValue;
		}
		stats.collectFinalStats(this, o);
		return false;		
	}
	
	
	public double[][] geInvtHessianAproximation(double[] diffX, double[] diffGrad, double[][] prevInvHessian){
			double p_k = 1 / MathUtils.dotProduct(diffX, diffGrad);
			//System.out.println("PK " + p_k);
			double[][] vK1 = MathUtils.identity(diffX.length);
			MathUtils.minusEquals(vK1, MathUtils.weightedouterProduct(diffX,diffGrad,p_k));
		//	MatrixOutput.printDoubleArray(vK1, "VK1");
			double[][] vK2 = MathUtils.identity(diffX.length);
			MathUtils.minusEquals(vK2, MathUtils.weightedouterProduct(diffGrad,diffX,p_k));
		//	MatrixOutput.printDoubleArray(vK2, "VK2");
			double[][] newH = MathUtils.matrixMultiplication(MathUtils.matrixMultiplication(vK1, prevInvHessian),vK2);
			MathUtils.plusEquals(newH, MathUtils.weightedouterProduct(diffX, diffX,p_k));
			return newH;
	}

	@Override
	public double[] getDirection() {
		return direction = MathUtils.negation(MathUtils.matrixVector(inverseHessian, gradient));
	}

	
}
