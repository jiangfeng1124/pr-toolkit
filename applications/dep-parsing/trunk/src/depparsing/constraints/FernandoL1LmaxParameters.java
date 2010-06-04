package depparsing.constraints;

public class FernandoL1LmaxParameters extends DualParameters {

	// indexed by: constraint ID,index. 
	final double [][] value;
	final int numParams;
	
	public FernandoL1LmaxParameters(FernandoL1Lmax owner){
		value = new double[owner.edge2scp.length][];
		int myNumParams = 0;
		// c is short for constraint id
		for (int c = 0; c < owner.edge2scp.length; c++) {
			value[c] = new double[owner.edge2scp[c].length];
			myNumParams+= value[c].length;
		}
		numParams = myNumParams;
	}
	
	
	public FernandoL1LmaxParameters(FernandoL1LmaxParameters other){
		value = new double[other.value.length][];
		int myNumParams = 0;
		// c is short for constraint ID
		for (int c = 0; c < other.value.length; c++) {
			value[c] = other.value[c].clone();
			myNumParams+= other.value[c].length;
		}
		numParams = myNumParams;
	}
	
	@Override
	public DualParameters deepCopy() {
		return new FernandoL1LmaxParameters(this);
	}

	@Override
	public double dotProduct(DualParameters b) {
		FernandoL1LmaxParameters other = (FernandoL1LmaxParameters) b;
		double sum=0;
		// constr short for constraint id
		for (int constr = 0; constr < other.value.length; constr++) {
			for (int i = 0; i < other.value[constr].length; i++) {
					sum+=value[constr][i]*other.value[constr][i];
			}
		}
		return sum;
	}

	@Override
	public int getNumParameters() {
		return numParams;
	}

	// a.plusEquals(b, d)  <=>  a += b * d 
	@Override
	public void plusEquals(DualParameters b, double d) {
		FernandoL1LmaxParameters other = (FernandoL1LmaxParameters) b;
		for (int cons = 0; cons < other.value.length; cons++) {
			for (int i = 0; i < other.value[cons].length; i++) {
					value[cons][i]+=d*other.value[cons][i];
			}
		}
	}

	@Override
	public void scaleBy(double d) {
		for (int cons = 0; cons < value.length; cons++) {
			for (int i = 0; i < value[cons].length; i++) {
					value[cons][i]*=d;
			}
		}
	}


	@Override
	public void copyFrom(DualParameters b) {
		FernandoL1LmaxParameters other = (FernandoL1LmaxParameters) b;
		// c stands for constraint ID
		for (int c = 0; c < other.value.length; c++) {
			System.arraycopy(other.value[c], 0, value[c], 0, other.value[c].length);
		}
	}

}
