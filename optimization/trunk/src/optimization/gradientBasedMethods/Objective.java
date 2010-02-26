package optimization.gradientBasedMethods;



public abstract  class Objective {

	protected int functionCalls = 0;
	protected int gradientCalls = 0;
	protected int updateCalls = 0;
	
	public double[] parameters;
	int debugLevel = 0;
	
	public void setDebugLevel(int level){
		debugLevel = level;
	}
	
	public int getNumParameters() {
		return parameters.length;
	}

	public double getParameter(int index) {
		return parameters[index];
	}

	public void getParameters(double[] params) {
		System.arraycopy(parameters, 0,params, 0, parameters.length);
	}

	public void setParameter(int index, double value) {
		parameters[index]=value;
	}

	public void setParameters(double[] params) {
		updateCalls++;
		parameters = params.clone();
	}

	
	public int getNumberFunctionCalls() {
		return functionCalls;
	}

	public int getNumberGradientCalls() {
		return gradientCalls;
	}
	
	public String finalInfoString() {
		return "FE: " + functionCalls + " GE " + gradientCalls + " Params updates" +
		updateCalls;
	}
	public void printParameters() {
		System.out.println(toString());
	}	
	
	
	public abstract String toString();	
	public abstract double getValue ();
	public abstract void getGradient (double[] gradient);
}
