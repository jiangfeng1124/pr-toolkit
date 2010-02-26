package edlin.experiments;

import edlin.algo.ConjugateGradient;
import edlin.algo.GradientAscent;
import edlin.types.DifferentiableObjective;

public class TestMaximization implements DifferentiableObjective {

	public double[] params;
	double[] desiredValues;
	double[] skew;
	public int numValueCalls = 0;
	public int numGradientCalls = 0;

	public TestMaximization(double[] desiredValues, double[] skew) {
		this.desiredValues = desiredValues;
		this.skew = skew;
		this.params = new double[desiredValues.length];
	}

	public void reset() {
		for (int i = 0; i < params.length; i++) {
			params[i] = 0;
		}
		numValueCalls = 0;
		numGradientCalls = 0;
	}

	public void getGradient(double[] gradient) {
		numGradientCalls += 1;
		for (int i = 0; i < desiredValues.length; i++) {
			gradient[i] = -2 * (params[i] - desiredValues[i]) * skew[i];
		}
	}

	public double getValue() {
		numValueCalls += 1;
		double v = 0;
		for (int i = 0; i < desiredValues.length; i++) {
			v -= Math.pow(params[i] - desiredValues[i], 2) * skew[i];
		}
		return v;
	}

	public int getNumParameters() {
		return params.length;
	}

	public void getParameters(double[] p) {
		System.arraycopy(params, 0, p, 0, params.length);
	}

	public void setParameters(double[] newParameters) {
		System.arraycopy(newParameters, 0, params, 0, params.length);
	}

	public static void main(String[] args) {
		double[] center = { 2, 3 };
		double[] skew = { 1, 100 };
		TestMaximization obj = new TestMaximization(center, skew);
		GradientAscent ga = new GradientAscent();
		ga.maximize(obj);
		System.out.println("Gradient descent found params (val="
				+ obj.numValueCalls + " grad=" + obj.numGradientCalls + "): ");
		for (int i = 0; i < obj.params.length; i++) {
			System.out.print(obj.params[i] + " ");
		}
		System.out.println();
		obj.reset();
		ConjugateGradient cg = new ConjugateGradient(obj.params.length);
		cg.maximize(obj);
		System.out.println("Conjugate gradient found params (val="
				+ obj.numValueCalls + " grad=" + obj.numGradientCalls + "): ");
		for (int i = 0; i < obj.params.length; i++) {
			System.out.print(obj.params[i] + " ");
		}
		System.out.println();

	}

}
