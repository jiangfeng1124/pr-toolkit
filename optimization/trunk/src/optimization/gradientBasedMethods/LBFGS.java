package optimization.gradientBasedMethods;


import optimization.gradientBasedMethods.stats.OptimizerStats;
import optimization.linesearch.LineSearchMethod;
import optimization.stopCriteria.StopingCriteria;
import util.ArrayMath;

public class LBFGS extends AbstractGradientBaseMethod{

	//How many previous values are being saved
	int history;
	double[][] skList;
	double[][] ykList;
	double initialHessianParameters;
	double[] previousGradient;
	double[] previousParameters;
	
	//auxiliar structures
	double q[];
	double[] roi;
	double[] alphai;
	
	public LBFGS(LineSearchMethod ls, int history) {
		lineSearch = ls;
		this.history = history;
		skList = new double[history][];
		ykList = new double[history][];

	}
	
	public void reset(){
		super.reset();
		initialHessianParameters = 0;
		previousParameters = null;
		previousGradient = null;
		skList = new double[history][];
		ykList = new double[history][];
		q = null;
		roi = null;
		alphai = null;
	}
	
	public double[] LBFGSTwoLoopRecursion(double hessianConst){
		//Only create array once
		if(q == null){
			 q = new double[gradient.length];
		}
		System.arraycopy(gradient, 0, q, 0, gradient.length);
		//Only create array once
		if(roi == null){
			roi = new double[history]; 
		}
		//Only create array once
		if(alphai == null){
			alphai = new double[history];
		}
		
		for(int i = history-1; i >=0 && skList[i]!= null && ykList[i]!=null; i-- ){			
		//	System.out.println("New to Old proj " + currentProjectionIteration + " history "+history + " index " + i);
			double[] si =  skList[i];
			double[] yi = ykList[i];
			roi[i]= 1.0/ArrayMath.dotProduct(yi,si);
			alphai[i] = ArrayMath.dotProduct(si, q)*roi[i];
			ArrayMath.plusEquals(q, yi, -alphai[i]);
		}
		//Initial Hessian is just a constant
		ArrayMath.scalarMultiplication(q, hessianConst);
		for(int i = 0; i <history && skList[i]!= null && ykList[i]!=null; i++ ){
		//	System.out.println("Old to New proj " + currentProjectionIteration + " history "+history + " index " + i);
			double beta = ArrayMath.dotProduct(ykList[i], q)*roi[i];
			ArrayMath.plusEquals(q, skList[i], (alphai[i]-beta));
		}
		return q;
	}
	
	
	
	
	@Override
	public double[] getDirection() {
		
		calculateInitialHessianParameter();
//		System.out.println("Initial hessian " + initialHessianParameters);
		return direction = ArrayMath.negation(LBFGSTwoLoopRecursion(initialHessianParameters));		
	}
	
	public void calculateInitialHessianParameter(){
		if(currentProjectionIteration == 0){
			//Use gradient
			initialHessianParameters = 1;
		}else if(currentProjectionIteration <= history){
			double[] sk = skList[currentProjectionIteration-1];
			double[] yk = ykList[currentProjectionIteration-1];
			initialHessianParameters = ArrayMath.dotProduct(sk, yk)/ArrayMath.dotProduct(yk, yk);
		}else{
			//get the last one
			double[] sk = skList[history-1];
			double[] yk = ykList[history-1];
			initialHessianParameters = ArrayMath.dotProduct(sk, yk)/ArrayMath.dotProduct(yk, yk);
		}
	}
	
	//TODO if structures exit just reset them to zero
	public void initializeStructures(Objective o,OptimizerStats stats, StopingCriteria stop){
		super.initializeStructures(o, stats, stop);
		previousParameters = new double[o.getNumParameters()];
		previousGradient = new double[o.getNumParameters()];
	}
	public void updateStructuresBeforeStep(Objective o,OptimizerStats stats, StopingCriteria stop){	
		super.initializeStructures(o, stats, stop);
		System.arraycopy(o.getParameters(), 0, previousParameters, 0, previousParameters.length);
		System.arraycopy(gradient, 0, previousGradient, 0, gradient.length);
	}

	public void 	updateStructuresAfterStep( Objective o,OptimizerStats stats, StopingCriteria stop){
		double[] diffX = ArrayMath.arrayMinus(o.getParameters(), previousParameters);
		double[] diffGrad = ArrayMath.arrayMinus(gradient, previousGradient);
		//Save new values and discard new ones
		if(currentProjectionIteration >= history){
			for(int i = 0; i < history-1;i++){
				skList[i]=skList[i+1];
				ykList[i]=ykList[i+1];
			}
			skList[history-1]=diffX;
			ykList[history-1]=diffGrad;
		}else{
			skList[currentProjectionIteration]=diffX;
			ykList[currentProjectionIteration]=diffGrad;
		}	
	}
	
	

	
	

}
