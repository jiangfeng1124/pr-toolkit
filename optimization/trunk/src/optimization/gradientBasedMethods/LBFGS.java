package optimization.gradientBasedMethods;


import optimization.gradientBasedMethods.stats.AbstractOptimizerStats;
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
	
	boolean useGradient = true;
	
	public LBFGS(LineSearchMethod ls, int history) {
		lineSearch = ls;
		this.history = history;
		skList = new double[history][];
		ykList = new double[history][];

	}
	@Override
	public void reset(){
		super.reset();
		initialHessianParameters = 1;
		skList = new double[history][];
		ykList = new double[history][];
        //FIXME Do we really need to do this here?
		q = null;
        roi = null;
		previousParameters = null;
        previousGradient = null;
        alphai = null;
        useGradient = true;
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
	
	public void resetHistory(){
		initialHessianParameters = 1;
		skList = new double[history][];
		ykList = new double[history][];
		useGradient = true;
	}
	
	public double getStepSize(AbstractOptimizerStats stats){
		lso.reset(direction);
		double step = lineSearch.getStepSize(lso);
		if(step == -1){
			//reset the memory 
			System.err.println("LBFGS Could not fins a step reseting history");
			lso.printSmallLineSearchSteps(System.err);
			resetHistory();
			getGradientDirection();
			lso.reset(direction);
			step = lineSearch.getStepSize(lso);
		}
		return step;
	}
	
	public double[] getGradientDirection(){
		for(int i = 0; i< gradient.length; i++){
			direction[i] = -gradient[i];
		}
		return direction;
	}
	
	@Override
	public double[] getDirection() {	
		calculateInitialHessianParameter();
//		System.out.println("Initial hessian " + initialHessianParameters);
		direction = ArrayMath.negation(LBFGSTwoLoopRecursion(initialHessianParameters));	
		if(ArrayMath.dotProduct(direction, gradient) > 0){
			System.err.println("LBFGS::Not a descent direction setting direction to gradient");
			direction = ArrayMath.negation(gradient);
			resetHistory();
		}
		return direction;
	}
	
	public void calculateInitialHessianParameter(){
		if(useGradient){
			//Use gradient
			initialHessianParameters = 1;
			useGradient = false;
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
	
	@Override
	public void initializeStructures(Objective o,AbstractOptimizerStats stats, StopingCriteria stop){
		super.initializeStructures(o, stats, stop);
		previousParameters = new double[o.getNumParameters()];
		previousGradient = new double[o.getNumParameters()];
	}
	@Override
	public void updateStructuresBeforeStep(Objective o,AbstractOptimizerStats stats, StopingCriteria stop){	
		super.updateStructuresBeforeStep(o, stats, stop);
	}
	
	@Override
	public void updateStructuresBeginIteration(Objective o){
		super.updateStructuresBeginIteration(o);
		System.arraycopy(gradient, 0, previousGradient, 0, gradient.length);
		System.arraycopy(o.getParameters(), 0, previousParameters, 0, previousParameters.length);
		}
	
	
	@Override
	public void 	updateStructuresAfterStep( Objective o,AbstractOptimizerStats stats, StopingCriteria stop){
		super.updateStructuresAfterStep(o, stats, stop);
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
