package optimization.linesearch;

public interface LineSearchObjective {
	
	public double getValue(double param);
	public int getNumberEvaluations();

}
