package depparsing.constraints;

public abstract class DualParameters {
	
	public abstract DualParameters deepCopy();
	public abstract void plusEquals(DualParameters b, double d);
	public abstract void scaleBy(double d);
	public abstract double dotProduct(DualParameters b);
	public abstract int getNumParameters();
	public abstract void copyFrom(DualParameters b);

	public static DualParameters difference(DualParameters a, DualParameters b){ return a.plus(b, -1); }
	
	public DualParameters plus(DualParameters b, double d){
		DualParameters res = deepCopy();
		res.plusEquals(b, d);
		return res;
	}
	
	public double twoNorm(){
		return Math.sqrt(dotProduct(this));
	}
	
	

	

}
