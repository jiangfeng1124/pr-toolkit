package depparsing.constraints;

/**
 * 
 * @author kuzman
 * 
 * We subclass {@link DifferentiableLineSearchObjective} so that if we eventually 
 * port the that class over to the optimization code, we can just plug in and replace.
 * FIXME: there should be a better way of doing this (e.g. rename projectionConstraints 
 * to DifferenctiableObjective and be done with this class?)
 *
 */
public class KLProjectionLineSearchObjective extends DifferentiableLineSearchObjective{

	private DualParameters lambda, direction;
	ProjectionConstraints objective;
	
	public KLProjectionLineSearchObjective(ProjectionConstraints objective, DualParameters initial, DualParameters direction, ProjectionStats stats){
		super(stats);
		this.objective = objective;
		this.lambda = initial;
		this.direction = direction;
	}
	
	@Override
	public double[] getValueAndDeriv(double param) {
		objective.setLambda(lambda);
		objective.updateLambda(direction, param, stats);
		double val = objective.getObjective();
		DualParameters gradient = objective.getGradient();
		return new double[] {val, gradient.dotProduct(direction)};
	}

	

	
	
}
