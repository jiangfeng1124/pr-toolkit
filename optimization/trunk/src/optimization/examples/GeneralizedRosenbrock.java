package optimization.examples;


import optimization.gradientBasedMethods.ConjugateGradient;
import optimization.gradientBasedMethods.GradientDescent;
import optimization.gradientBasedMethods.LBFGS;
import optimization.gradientBasedMethods.Objective;
import optimization.gradientBasedMethods.Optimizer;
import optimization.gradientBasedMethods.stats.OptimizerStats;
import optimization.linesearch.ArmijoLineSearchMinimization;
import optimization.linesearch.GenericPickFirstStep;
import optimization.linesearch.InterpolationPickFirstStep;
import optimization.linesearch.LineSearchMethod;
import optimization.linesearch.WolfRuleLineSearch;
import optimization.stopCriteria.GradientL2Norm;
import optimization.stopCriteria.StopingCriteria;
import util.ArrayMath;

/**
 * 
 * @author javg
 * f(x) = \sum_{i=1}^{N-1} \left[ (1-x_i)^2+ 100 (x_{i+1} - x_i^2 )^2 \right] \quad \forall x\in\mathbb{R}^N.
 */
public class GeneralizedRosenbrock extends Objective{

	
	
	public GeneralizedRosenbrock(int dimensions){
		parameters = new double[dimensions];		
		java.util.Arrays.fill(parameters, 0);
		gradient = new double[dimensions];
		
	}
	
	public GeneralizedRosenbrock(int dimensions, double[] params){
		parameters = params;	
		gradient = new double[dimensions];
	}
	
	
	public double getValue() {
		functionCalls++;
		double value = 0;
		for(int i = 0; i < parameters.length-1; i++){
			value += ArrayMath.square(1-parameters[i]) + 100*ArrayMath.square(parameters[i+1] - ArrayMath.square(parameters[i]));
		}
		
		return value;
	}

	/**
	 * gx = -2(1-x) -2x200(y-x^2)
	 * gy = 200(y-x^2)
	 */
	public double[] getGradient() {
		gradientCalls++;
		java.util.Arrays.fill(gradient,0);
		for(int i = 0; i < parameters.length-1; i++){
			gradient[i]+=-2*(1-parameters[i]) - 400*parameters[i]*(parameters[i+1] - ArrayMath.square(parameters[i]));
			gradient[i+1]+=200*(parameters[i+1] - ArrayMath.square(parameters[i]));
		}	
		return gradient;
	}

	
	public String toString(){
		String  res ="";
		for(int i = 0; i < parameters.length; i++){
			res += "P" + i+ " " + parameters[i];
		}
		res += " Value " + getValue();
		return res;
	}
	
	public static void doGradientDescent(int dimensions, int maxIter){
		GeneralizedRosenbrock o = new GeneralizedRosenbrock(dimensions);
		System.out.println("Starting optimization " + " x0 " + o.parameters[0]+ " x1 " + o.parameters[1]);
		System.out.println("Doing Gradient descent");
		LineSearchMethod ls = new WolfRuleLineSearch(new GenericPickFirstStep(1),100,0.001,0.1);
		StopingCriteria stop = new GradientL2Norm(0.001);		
//		LineSearchMethod ls = new ArmijoLineSearchMinimization();
		Optimizer optimizer = new GradientDescent(ls);		
		OptimizerStats stats = new OptimizerStats();
		optimizer.setMaxIterations(maxIter);
		ls.setDebugLevel(2);
		boolean succed = optimizer.optimize(o,stats, stop);
		System.out.println("Suceess " + succed + "/n"+stats.prettyPrint(1));

	}

	public static void doConjugateGradientDescent(int dimensions, int maxIter){
		GeneralizedRosenbrock o = new GeneralizedRosenbrock(dimensions);
		System.out.println("Starting optimization " + " x0 " + o.parameters[0]+ " x1 " + o.parameters[1]);
		System.out.println("Doing Gradient descent");
		LineSearchMethod ls = new WolfRuleLineSearch(new GenericPickFirstStep(1),100,0.001,0.1);
		StopingCriteria stop = new GradientL2Norm(0.001);		
		Optimizer optimizer = new ConjugateGradient(ls);		
		OptimizerStats stats = new OptimizerStats();
		optimizer.setMaxIterations(maxIter);
		boolean succed = optimizer.optimize(o,stats, stop);
		System.out.println("Suceess " + succed + "/n"+stats.prettyPrint(1));

	}

	
	public static void doLBFGSDescent(int dimensions, int maxIter){
		GeneralizedRosenbrock o = new GeneralizedRosenbrock(dimensions);
		System.out.println("Starting optimization " + " x0 " + o.parameters[0]+ " x1 " + o.parameters[1]);
		System.out.println("Doing Gradient descent");
		LineSearchMethod ls = new WolfRuleLineSearch(new GenericPickFirstStep(1),100,0.001,0.1);
		StopingCriteria stop = new GradientL2Norm(0.001);		
		Optimizer optimizer = new LBFGS(ls,10);		
		OptimizerStats stats = new OptimizerStats();
		optimizer.setMaxIterations(maxIter);
		boolean succed = optimizer.optimize(o,stats, stop);
		System.out.println("Suceess " + succed + "/n"+stats.prettyPrint(1));
	}
	
	public static Objective createObjective(int dimensions){
		GeneralizedRosenbrock o = new GeneralizedRosenbrock(dimensions);
		o.setDebugLevel(4);
		return o;
	}
	
	public static void main(String[] args) {
		double precision = 1.E-5;
		int dimensions = 10;
		int maxIter = 1000;
		Objective o = createObjective(dimensions);
		LineSearchMethod wolfe = new WolfRuleLineSearch(new GenericPickFirstStep(100),0.00001,0.9,100);
//		wolfe.setDebugLevel(2);
//		LineSearchMethod ls = new ArmijoLineSearchMinimization();
		OptimizerStats stats = new OptimizerStats();
		x2y2.optimizeWithGradientDescent(wolfe, stats, o,precision,maxIter);
		o = createObjective(dimensions);
		wolfe = new WolfRuleLineSearch(new GenericPickFirstStep(100),0.00001,0.1,100);
		x2y2.optimizeWithConjugateGradient(wolfe, stats, o, precision,maxIter);
		o = createObjective(dimensions);
		wolfe = new WolfRuleLineSearch(new GenericPickFirstStep(1),0.00001,0.9,100);
		x2y2.optimizeWithLBFGS(wolfe, stats, o, precision, maxIter);
	}
	
	
}
