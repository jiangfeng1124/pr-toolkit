package model;

public class CantNormalizeException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	public double problem; // zero, NaN, Infty... 
	
	public CantNormalizeException(String str, double problem){
		super(str);
	}

}
