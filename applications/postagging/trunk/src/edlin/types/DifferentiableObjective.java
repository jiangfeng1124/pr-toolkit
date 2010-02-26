package edlin.types;

public interface DifferentiableObjective {

	public double getValue();

	public void getGradient(double[] gradient);

	public void getParameters(double[] params);

	public void setParameters(double[] newParameters);

	public int getNumParameters();
}
