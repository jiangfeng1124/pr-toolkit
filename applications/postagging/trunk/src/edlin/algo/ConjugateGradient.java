package edlin.algo;

import edlin.types.DifferentiableObjective;
import edlin.types.StaticUtils;

public class ConjugateGradient {

	int maxNumSteps = 2000;
	double defaultStepSize = 1e-3;
	double minStepSize = 1e-100;

	public ConjugateGradient(int numParams) {
		evaluateAtStorage = new double[numParams];
	}

	public boolean maximize(DifferentiableObjective o) {
		double[] p = new double[o.getNumParameters()];
		o.getParameters(p);
		double[] g = new double[o.getNumParameters()];
		double[] h = new double[o.getNumParameters()];
		double[] xi = new double[o.getNumParameters()];
		double oldScore = o.getValue();
		@SuppressWarnings("unused")
		long time = System.currentTimeMillis();
		System.err.println("	Score: " + oldScore);
		o.getGradient(g);
		System.arraycopy(g, 0, h, 0, g.length);
		System.arraycopy(g, 0, xi, 0, g.length);
		for (int iteration = 0; iteration < maxNumSteps; iteration++) {
			double newScore = lineSearch(o, p, xi);
			// System.err.println(" Score:
			// "+newScore+"\t("+((System.currentTimeMillis()-time)/1000.0)+"
			// seconds)");
			time = System.currentTimeMillis();
			if (newScore - oldScore < 1e-30)
				return true;
			oldScore = newScore;
			// check if we've converged.
			o.getGradient(xi);
			double numerator = StaticUtils.dotProduct(xi, xi)
					- StaticUtils.dotProduct(g, xi);
			double denom = StaticUtils.dotProduct(g, g);
			if (denom < minStepSize)
				return true;
			double gamma = numerator / denom;
			System.arraycopy(xi, 0, g, 0, g.length);
			StaticUtils.add(xi, g, h, gamma);
			System.arraycopy(xi, 0, h, 0, g.length);
		}
		return false;
	}

	/**
	 * finds the maximizer of o(parameters + lambda*direction)
	 * 
	 * @param o
	 * @param parameters
	 * @param direction
	 * @return the score at the new parameters
	 */
	public double lineSearch(DifferentiableObjective o, double[] parameters,
			double[] direction) {
		double min = 0;
		double minVal = evalueateAt(o, parameters, direction, min);
		if (Double.isNaN(minVal))
			throw new RuntimeException("Invalid function value: " + minVal);
		double max = defaultStepSize;
		double maxVal = evalueateAt(o, parameters, direction, max);
		if (Double.isNaN(maxVal))
			throw new RuntimeException("Invalid function value: " + maxVal);
		// make sure maxVal < minVal, that way we know that if direction is a
		// descent direction, there is a max somewhere in the middle.
		while (maxVal > minVal) {
			max = 2 * max;
			maxVal = evalueateAt(o, parameters, direction, max);
			if (Double.isNaN(maxVal))
				throw new RuntimeException("Invalid function value: " + maxVal);
		}
		// binary search between the two values
		while (max - min > max * 0.05) {
			double mid = (max + min) / 2;
			double midVal = evalueateAt(o, parameters, direction, mid);
			if (Double.isNaN(midVal))
				throw new RuntimeException("Invalid function value: " + midVal);
			if (minVal > maxVal) {
				// min is better than max. discard max
				max = mid;
				maxVal = midVal;
			} else {
				min = mid;
				minVal = midVal;
			}
		}
		StaticUtils.add(parameters, parameters, direction, min);
		defaultStepSize = min * 2;
		return minVal;
	}

	private double[] evaluateAtStorage;

	private double evalueateAt(DifferentiableObjective o, double[] params,
			double[] direction, double step) {
		StaticUtils.add(evaluateAtStorage, params, direction, step);
		o.setParameters(evaluateAtStorage);
		return o.getValue();
	}

}
