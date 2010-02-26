package optimization.gradientBasedMethods;


import optimization.gradientBasedMethods.stats.OptimizerStats;
import optimization.linesearch.DifferentiableLineSearchObjective;
import optimization.linesearch.LineSearchMethod;
import optimization.util.MathUtils;

public class LBFGS extends AbstractGradientBaseMethod{

	//How many previous values are being saved
	int history;
	double[][] skList;
	double[][] ykList;
	double initialHessianParameters;
	double[] previousGradient;
	double[] currParameters;
	double[] previousParameters;
	
	
	public LBFGS(LineSearchMethod ls, int history) {
		lineSearch = ls;
		this.history = history;
		skList = new double[history][];
		ykList = new double[history][];
	}
	
	public double[] LBFGSTwoLoopRecursion(double hessianConst){
		double[] q = new double[gradient.length];
		System.arraycopy(gradient, 0, q, 0, gradient.length);
		double[] roi = new double[history]; 
		double[] alphai = new double[history];
		for(int i = history-1; i >=0 && skList[i]!= null && ykList[i]!=null; i-- ){			
		//	System.out.println("New to Old proj " + currentProjectionIteration + " history "+history + " index " + i);
			double[] si =  skList[i];
			double[] yi = ykList[i];
			roi[i]= 1.0/MathUtils.dotProduct(yi,si);
			alphai[i] = MathUtils.dotProduct(si, q)*roi[i];
			MathUtils.plusEquals(q, yi, -alphai[i]);
		}
		//Initial Hessian is just a constant
		MathUtils.scalarMultiplication(q, hessianConst);
		for(int i = 0; i <history && skList[i]!= null && ykList[i]!=null; i++ ){
		//	System.out.println("Old to New proj " + currentProjectionIteration + " history "+history + " index " + i);
			double beta = MathUtils.dotProduct(ykList[i], q)*roi[i];
			MathUtils.plusEquals(q, skList[i], (alphai[i]-beta));
		}
		return q;
	}
	
	

	public boolean optimize(Objective o, OptimizerStats stats) {
		gradient = new double[o.getNumParameters()];
		direction = new double[o.getNumParameters()];
		previousGradient = new double[o.getNumParameters()];
		currParameters = new double[o.getNumParameters()];
		previousParameters = new double[o.getNumParameters()];
	
		stats.collectInitStats(this, o);
		previousValue = Double.MAX_VALUE;
		currValue= o.getValue();
		//Used for stopping criteria
		double[] originalGradient = new double[o.getNumParameters()];
		o.getGradient(originalGradient);
		originalGradientL2Norm = MathUtils.L2Norm(originalGradient);
		if(stopCriteria(originalGradient)){
			stats.collectFinalStats(this, o);
			return true;
		}
		for (currentProjectionIteration = 1; currentProjectionIteration < maxNumberOfIterations; currentProjectionIteration++){
			
			
			currValue = o.getValue();
			o.getGradient(gradient);
			o.getParameters(currParameters);
			
			
			if(currentProjectionIteration == 1){
				//Use gradient
				initialHessianParameters = 1;
			}else if(currentProjectionIteration <= history){
				double[] sk = skList[currentProjectionIteration-2];
				double[] yk = ykList[currentProjectionIteration-2];
				initialHessianParameters = MathUtils.dotProduct(sk, yk)/MathUtils.dotProduct(yk, yk);
			}else{
				//get the last one
				double[] sk = skList[history-1];
				double[] yk = ykList[history-1];
				initialHessianParameters = MathUtils.dotProduct(sk, yk)/MathUtils.dotProduct(yk, yk);
			}
			
			getDirection();
			
			//MatrixOutput.printDoubleArray(direction, "direction");
			double dot = MathUtils.dotProduct(direction, gradient);
			if(dot > 0){				
				throw new RuntimeException("Not a descent direction");
			} if (Double.isNaN(dot)){
				throw new RuntimeException("dot is not a number!!");
			}
			System.arraycopy(currParameters, 0, previousParameters, 0, currParameters.length);
			System.arraycopy(gradient, 0, previousGradient, 0, gradient.length);
			DifferentiableLineSearchObjective lso = new DifferentiableLineSearchObjective(o,direction);
			step = lineSearch.getStepSize(lso);
			if(step==-1){
				System.out.println("Failed to find a step size");
//				lso.printLineSearchSteps();
//				System.out.println(stats.prettyPrint(1));
				stats.collectFinalStats(this, o);
				return false;	
			}
			stats.collectIterationStats(this, o);
			
			//We are not updating the alpha since it is done in line search already
			o.getParameters(currParameters);
			o.getGradient(gradient);
			
			if(stopCriteria(gradient)){
				stats.collectFinalStats(this, o);
				return true;
			}
			double[] diffX = MathUtils.arrayMinus(currParameters, previousParameters);
			double[] diffGrad = MathUtils.arrayMinus(gradient, previousGradient);
			//Save new values and discard new ones
			if(currentProjectionIteration > history){
				for(int i = 0; i < history-1;i++){
					skList[i]=skList[i+1];
					ykList[i]=ykList[i+1];
				}
				skList[history-1]=diffX;
				ykList[history-1]=diffGrad;
			}else{
				skList[currentProjectionIteration-1]=diffX;
				ykList[currentProjectionIteration-1]=diffGrad;
			}		
			previousValue = currValue;
		}
		stats.collectFinalStats(this, o);
		return false;	
	}
	


	

	

	
	@Override
	public double[] getDirection() {
		return direction = MathUtils.negation(LBFGSTwoLoopRecursion(initialHessianParameters));		
	}

}
