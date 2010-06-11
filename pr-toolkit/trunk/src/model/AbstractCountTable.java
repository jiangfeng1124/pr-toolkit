package model;

public abstract class AbstractCountTable {
	/**
	 * clears the table at the start of an E-Step.  
	 * This is typically a fill(0) or fill(Double.NEGATIVE_INFINITY) depending
	 * on whether the counts are stored in log-space. 
	 */
	public abstract void clear();
	public abstract void fill(double value);
	public abstract void fill(AbstractCountTable value);
}
