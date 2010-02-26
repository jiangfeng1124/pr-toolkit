package optimization.examples;

import optimization.gradientBasedMethods.BFGS;
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
import optimization.util.MathUtils;

/**
 * @author javg
 * (1-x)^2 + 100(y-x^2)^2
 */
public class Rosenbrock extends Objective{

	
	
	public Rosenbrock(){
		parameters = new double[2];		
		parameters[0] = 4;
		parameters[1] = 4;
		
	}
	
	public Rosenbrock(double x, double y){
		parameters = new double[2];		
		parameters[0] = x;
		parameters[1] = y;
		
	}
	
	public double getValue() {
		functionCalls++;
		return MathUtils.square(1-parameters[0]) + 100*MathUtils.square(parameters[1] - MathUtils.square(parameters[0])) ;
	}

	/**
	 * gx = -2(1-x) -2x200(y-x^2)
	 * gy = 200(y-x^2)
	 */
	public void getGradient(double[] gradient) {
		gradientCalls++;
		gradient[0]=-2*(1-parameters[0]) - 400*parameters[0]*(parameters[1] - MathUtils.square(parameters[0]));
		gradient[1]=200*(parameters[1] - MathUtils.square(parameters[0]));
	}

	public String toString(){
		return "P1: " + parameters[0] + " P2: " + parameters[1] + " value " + getValue();
	}
	
	public static void main(String[] args) {
		int maxIter = 40;
		Rosenbrock o = new Rosenbrock(-1.2,1);
		System.out.println("Starting optimization " + " x0 " + o.parameters[0]+ " x1 " + o.parameters[1]);
		;
	
		System.out.println("Doing Gradient descent");
		LineSearchMethod wolfe = new WolfRuleLineSearch(new InterpolationPickFirstStep(1),0.001,0.1);
		LineSearchMethod ls = new ArmijoLineSearchMinimization();
		Optimizer optimizer = new GradientDescent(ls);		
		OptimizerStats stats = new OptimizerStats();
		optimizer.setGradientConvergenceValue(0.001);
		optimizer.setValueConvergenceValue(1E-10);
		optimizer.setMaxIterations(maxIter);
		boolean succed = optimizer.optimize(o,stats);
		System.out.println("Suceess " + succed + "/n"+stats.prettyPrint(1));
//		System.out.println("Doing Conjugate Gradient descent");
//		o = new Rosenbrock(-1.2,1);
//		wolfe = new WolfRuleLineSearch(new InterpolationPickFirstStep(1),0.001,0.1);
//		optimizer = new ConjugateGradient(wolfe);
//		stats = new OptimizerStats();
//		optimizer.setGradientConvergenceValue(0.001);
//		optimizer.setValueConvergenceValue(1E-10);
//		optimizer.setMaxIterations(maxIter);
//		succed = optimizer.optimize(o,stats);
//		System.out.println("Suceess " + succed + "/n"+stats.prettyPrint(1));
//		System.out.println("Doing Quasi newton descent");
//		o = new Rosenbrock(-1.2,1);
//		wolfe = new WolfRuleLineSearch(new InterpolationPickFirstStep(1),0.001,0.9);
//		optimizer = new BFGS(wolfe);
//		stats = new OptimizerStats();
//		optimizer.setGradientConvergenceValue(0.001);
//		optimizer.setValueConvergenceValue(1E-10);
//		optimizer.setMaxIterations(maxIter);
//		succed = optimizer.optimize(o,stats);
//		System.out.println("Suceess " + succed + "/n"+stats.prettyPrint(1));
////		
//		System.out.println("Doing Limited Quasi newton descent");
//		o = new Rosenbrock(-1.2,1);
//		wolfe = new WolfRuleLineSearch(new InterpolationPickFirstStep(1),0.001,0.9);
//		optimizer = new LBFGS(wolfe,10);
//		stats = new OptimizerStats();
//		optimizer.setGradientConvergenceValue(0.001);
//		optimizer.setValueConvergenceValue(1E-10);
//		optimizer.setMaxIterations(maxIter);
//		succed = optimizer.optimize(o,stats);
//		System.out.println("Suceess " + succed + "/n"+stats.prettyPrint(1));

	}
	
	

	
}
