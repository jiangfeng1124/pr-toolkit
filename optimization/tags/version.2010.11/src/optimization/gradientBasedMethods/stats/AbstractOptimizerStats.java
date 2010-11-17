package optimization.gradientBasedMethods.stats;


import optimization.gradientBasedMethods.Objective;
import optimization.gradientBasedMethods.Optimizer;


public abstract class AbstractOptimizerStats {
	
	public double start = 0;
	public double totalTime = 0;
	
	
	public void reset(){
		start = 0;
		totalTime = 0;
	}
	
	public void startTime() {
		start = System.currentTimeMillis();
	}
	public void stopTime() {
		totalTime += System.currentTimeMillis() - start;
	}
	
	public String prettyPrint(int level){
		StringBuffer res = new StringBuffer();
		res.append("Total time " + totalTime/1000 + " seconds \n");
		return res.toString();
	}
	
	
	public void collectInitStats(Optimizer optimizer, Objective objective){
		startTime();
	}
	
	public  void collectIterationStats(Optimizer optimizer, Objective objective){
		
	}
	
	
	public void collectFinalStats(Optimizer optimizer, Objective objective, boolean success){
		stopTime();
	}
	
}
