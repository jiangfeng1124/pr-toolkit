package model.distribution.trainer;

import model.AbstractCountTable;
import model.distribution.AbstractMultinomial;

public interface DirectGradientTrainer {
	
	public void setCountsAndParameters(AbstractMultinomial counts, double[] params, int offset);
	public void getGradient(double[] gradient, int offset);
	public double getValue();

}
