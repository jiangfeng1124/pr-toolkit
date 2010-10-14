package optimization.examples;


import optimization.gradientBasedMethods.ConjugateGradient;
import optimization.gradientBasedMethods.GradientDescent;
import optimization.gradientBasedMethods.LBFGS;
import optimization.gradientBasedMethods.Objective;
import optimization.gradientBasedMethods.stats.OptimizerStats;
import optimization.linesearch.GenericPickFirstStep;
import optimization.linesearch.LineSearchMethod;
import optimization.linesearch.WolfRuleLineSearch;
import optimization.stopCriteria.GradientL2Norm;
import optimization.stopCriteria.StopingCriteria;


/**
 * @author javg
 *
 */
public class x2y2 extends Objective{

	
	//Implements function ax2+ by2 
	double a, b;
	public x2y2(double a, double b){
		this.a = a;
		this.b = b;
		parameters = new double[2];
		parameters[0] = 4;
		parameters[1] = 4;
		gradient = new double[2];
	}
	
	public double getValue() {
		functionCalls++;
		return a*parameters[0]*parameters[0]+b*parameters[1]*parameters[1];
	}

	public double[] getGradient() {
		gradientCalls++;
		gradient[0]=2*a*parameters[0];
		gradient[1]=2*b*parameters[1];
		return gradient;
//		if(debugLevel >=2){
//			double[] numericalGradient = DebugHelpers.getNumericalGradient(this, parameters, 0.000001);
//			for(int i = 0; i < parameters.length; i++){
//				double diff = Math.abs(gradient[i]-numericalGradient[i]);
//				if(diff > 0.00001){
//					System.out.println("Numerical Gradient does not match");
//					System.exit(1);
//				}
//			}
//		}
	}

	
	
	
	public static void optimizeWithGradientDescent(LineSearchMethod ls, OptimizerStats stats,
			Objective o, double precision, int maxIterations){
		stats.reset();
		GradientDescent optimizer = new GradientDescent(ls);
		StopingCriteria stop = new GradientL2Norm(precision);
//		optimizer.setGradientConvergenceValue(0.001);
		optimizer.setMaxIterations(maxIterations);
		boolean succed = optimizer.optimize(o,stats,stop);
		System.out.println("Ended optimzation Gradient Descent\n" + stats.prettyPrint(1));
		System.out.println("Solution: " + o.toString());
		if(succed){
			System.out.println("Ended optimization in " + optimizer.getCurrentIteration());
		}else{
			System.out.println("Failed to optimize");
		}
	}
	
	public static void optimizeWithConjugateGradient(LineSearchMethod ls, OptimizerStats stats, 
			Objective o, double precision, int maxIterations){
		stats.reset();
		ConjugateGradient optimizer = new ConjugateGradient(ls);
		StopingCriteria stop = new GradientL2Norm(precision);

		optimizer.setMaxIterations(maxIterations);
		boolean succed = optimizer.optimize(o,stats,stop);
		System.out.println("Ended optimzation Conjugate Gradient\n" + stats.prettyPrint(1));
		System.out.println("Solution: " + o.toString());
		if(succed){
			System.out.println("Ended optimization in " + optimizer.getCurrentIteration());
		}else{
			System.out.println("Failed to optimize");
		}
	}
	
	public static void optimizeWithLBFGS(LineSearchMethod ls, OptimizerStats stats,
			Objective o, double precision, int maxIterations){
		stats.reset();
		LBFGS optimizer = new LBFGS(ls,10);
		StopingCriteria stop = new GradientL2Norm(precision);
		optimizer.setMaxIterations(maxIterations);
		boolean succed = optimizer.optimize(o,stats,stop);
		System.out.println("Ended optimzation LBFGS\n" + stats.prettyPrint(1));
		System.out.println("Solution: " + o.toString());
		if(succed){
			System.out.println("Ended optimization in " + optimizer.getCurrentIteration());
		}else{
			System.out.println("Failed to optimize");
		}
	}
	public static Objective createObjective(){
		x2y2 o = new x2y2(1,10);
		o.setDebugLevel(4);
		System.out.println("Created objective: " + o.toString());
		
		return o;
	}

	
	public static void main(String[] args) {
		double precision = 1.E-5;
		int maxIter = 100;
		Objective o = createObjective();
		LineSearchMethod wolfe = new WolfRuleLineSearch(new GenericPickFirstStep(100),0.00001,0.9,100);
		wolfe.setDebugLevel(2);
//		LineSearchMethod ls = new ArmijoLineSearchMinimization();
		OptimizerStats stats = new OptimizerStats();
		optimizeWithGradientDescent(wolfe, stats, o,precision,maxIter);
		o = createObjective();
		wolfe = new WolfRuleLineSearch(new GenericPickFirstStep(100),0.00001,0.1,100);
		optimizeWithConjugateGradient(wolfe, stats, o, precision,maxIter);
		o = createObjective();
		wolfe = new WolfRuleLineSearch(new GenericPickFirstStep(1),0.00001,0.9,100);
		optimizeWithLBFGS(wolfe, stats, o, precision, maxIter);
	}
	
	public String toString(){
		return "P1: " + parameters[0] + " P2: " + parameters[1] + " value " + getValue();
	}
	
	
}
