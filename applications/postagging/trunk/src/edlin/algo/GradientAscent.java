package edlin.algo;

import edlin.types.DifferentiableObjective;
import edlin.types.StaticUtils;

public class GradientAscent {

	int maxNumSteps = 2000;
	double maxStepSize = 0.1;
	double minStepSize = 1e-100;

	public boolean maximize(DifferentiableObjective o) {
		double[] gradient = new double[o.getNumParameters()];
		double[] currParameters = new double[o.getNumParameters()];
		double[] newParameters = new double[o.getNumParameters()];
		for (int step = 0; step < maxNumSteps; step++) {
			double currValue = o.getValue();
			o.getParameters(currParameters);
			o.getGradient(gradient);
			double stepSize = maxStepSize;
			while (true) {
				StaticUtils.add(newParameters, currParameters, gradient,
						stepSize);
				o.setParameters(newParameters);
				double newValue = o.getValue();
				if (newValue > currValue)
					break;
				if (stepSize < minStepSize) {
					System.out.println("Converged in "
							+ step
							+ " steps. TwoNorm of gradient is "
							+ Math.pow(StaticUtils.twoNormSquared(gradient),
									0.5));
					return true;
				}
				stepSize /= 2;
			}
		}
		System.out.println("Did not converge in " + maxNumSteps
				+ " gradient steps. TwoNorm of gradient is "
				+ Math.pow(StaticUtils.twoNormSquared(gradient), 0.5));
		return false;
	}

}
