package optimization.examples;

import optimization.gradientBasedMethods.NonMonotoneSpectralProjectedGradient;
import optimization.gradientBasedMethods.ProjectedGradientDescent;
import optimization.gradientBasedMethods.ProjectedObjective;
import optimization.gradientBasedMethods.stats.OptimizerStats;
import optimization.linesearch.ArmijoLineSearchMinimizationAlongProjectionArc;
import optimization.linesearch.InterpolationPickFirstStep;
import optimization.linesearch.LineSearchMethod;
import optimization.linesearch.NonMonotoneArmijoLineSearchMinimizationAlongProjectionArc;
import optimization.projections.BoundsProjection;
import optimization.projections.Projection;
import optimization.projections.SimplexProjection;


/**
 * @author javg
 * 
 * 
 *ax2+ b(y2 -displacement)
 */
public class x2y2WithConstraints extends ProjectedObjective{


	double a, b;
	double dx = 0.5;
	double dy = 0.5;
	Projection projection;
	
	
	public x2y2WithConstraints(double a, double b, double x0,double x1, double dx, double dy){
		//projection = new BoundsProjection(0.2,Double.MAX_VALUE);		
		projection = new SimplexProjection(5);		
		this.a = a;
		this.b = b;
		this.dx = dx;
		this.dy = dy;
		parameters = new double[2];
		parameters[0] = x0;
		parameters[1] = x1;
		System.out.println("Function " +a+"(x-"+dx+")^2 + "+b+"(y-"+dy+")^2");
		System.out.println("Gradient " +(2*a)+"(x-"+dx+") ; "+(b*2)+"(y-"+dy+")");
		printParameters();
		projection.project(parameters);
		printParameters();
	}
	
	public double getValue() {
		functionCalls++;
		return a*(parameters[0]-dx)*(parameters[0]-dx)+b*((parameters[1]-dy)*(parameters[1]-dy));
	}

	public void getGradient(double[] gradient) {
		gradientCalls++;
		gradient[0]=2*a*(parameters[0]-dx);
		gradient[1]=2*b*(parameters[1]-dy);
	}
	
	
	public double[] projectPoint(double[] point) {
		double[] newPoint = point.clone();
		projection.project(newPoint);
		return newPoint;
	}	
	
	public void optimizeWithProjectedGradientDescent(LineSearchMethod ls, OptimizerStats stats, x2y2WithConstraints o){
		ProjectedGradientDescent optimizer = new ProjectedGradientDescent(ls);
		optimizer.setGradientConvergenceValue(0.001);
		optimizer.setMaxIterations(100);
		boolean succed = optimizer.optimize(o,stats);
		System.out.println("Ended optimzation Projected Gradient Descent\n" + stats.prettyPrint(1));
		System.out.println("Solution: " + " x0 " + o.parameters[0]+ " x1 " + o.parameters[1]);
		if(succed){
			System.out.println("Ended optimization in " + optimizer.getCurrentIteration());
		}else{
			System.out.println("Failed to optimize");
		}
	}
	
	public void optimizeWithSpectralProjectedGradientDescent(OptimizerStats stats, x2y2WithConstraints o){
		LineSearchMethod ls = new NonMonotoneArmijoLineSearchMinimizationAlongProjectionArc();
		NonMonotoneSpectralProjectedGradient optimizer = new NonMonotoneSpectralProjectedGradient(ls);
		optimizer.setGradientConvergenceValue(0.001);
		optimizer.setMaxIterations(100);
		boolean succed = optimizer.optimize(o,stats);
		System.out.println("Ended optimzation Spectral Projected Gradient Descent\n" + stats.prettyPrint(1));
		System.out.println("Solution: " + " x0 " + o.parameters[0]+ " x1 " + o.parameters[1]);
		if(succed){
			System.out.println("Ended optimization in " + optimizer.getCurrentIteration());
		}else{
			System.out.println("Failed to optimize");
		}
	}
	
	public String toString(){
		return "P1: " + parameters[0] + " P2: " + parameters[1] + " value " + getValue();
	}
	
	public static void main(String[] args) {
		double a = 10;
		double b=1;
		double x0 = 2;
		double y0  =2;
		double dx =0.5;
		double dy = 0;
		x2y2WithConstraints o = new x2y2WithConstraints(a,b,x0,y0,dx,dy);
		System.out.println("Starting optimization " + " x0 " + o.parameters[0]+ " x1 " + o.parameters[1] + " a " + a + " b "+b );
		o.setDebugLevel(4);
		
		LineSearchMethod ls = new ArmijoLineSearchMinimizationAlongProjectionArc(new InterpolationPickFirstStep(1));
		OptimizerStats stats = new OptimizerStats();
		o.optimizeWithProjectedGradientDescent(ls, stats, o);
		
		o = new x2y2WithConstraints(a,b,x0,y0,dx,dy);
		stats = new OptimizerStats();
		o.optimizeWithSpectralProjectedGradientDescent(stats, o);
	}
	
	
	
	
}
