package optimization.gradientBasedMethods.stats;

import java.util.ArrayList;

import optimization.gradientBasedMethods.Objective;
import optimization.gradientBasedMethods.Optimizer;
import util.ArrayMath;
import util.Printing;


public class OptimizerStats extends AbstractOptimizerStats{
	
	
	public String objectiveFinalStats;
	
	public ArrayList<Double> gradientNorms = new ArrayList<Double>();
	public ArrayList<Double> steps = new ArrayList<Double>();
	public ArrayList<Double> value = new ArrayList<Double>();
	public ArrayList<Integer> iterations = new ArrayList<Integer>();
	public double prevValue =0;
	
	public boolean succeed = false;
	public int paramUpdates;
	
	public double weightsNorm;
	
	public void reset(){
		super.reset();
		totalTime = 0;
		weightsNorm = 0;
		objectiveFinalStats="";
		
		gradientNorms.clear();
		steps.clear();
		value.clear();
		iterations.clear();
		prevValue =0;
	}
	
	
	public String prettyPrint(int level){
		StringBuffer res = new StringBuffer();
		res.append("Total time " + totalTime/1000 + " seconds \n" + "Iterations " + iterations.size() + "\n");
		res.append("weights"+weightsNorm+"\n");
		res.append(objectiveFinalStats+"\n");
		if(level > 0){
			if(iterations.size() > 0){
			res.append("\tIteration"+iterations.get(0)+"\tstep: "+Printing.prettyPrint(steps.get(0), "0.00E00", 6)+ "\tgradientNorm "+ 
					Printing.prettyPrint(gradientNorms.get(0), "0.00000E00", 10)+ "\tvalue "+ Printing.prettyPrint(value.get(0), "0.000000E00",11)+"\n");
			}
			for(int i = 1; i < iterations.size(); i++){
			res.append("\tIteration:\t"+iterations.get(i)+"\tstep:"+Printing.prettyPrint(steps.get(i), "0.00E00", 6)+ "\tgradientNorm "+ 
					Printing.prettyPrint(gradientNorms.get(i), "0.00000E00", 10)+ 
					"\tvalue:\t"+ Printing.prettyPrint(value.get(i), "0.000000E00",11)+
					"\tvalueDiff:\t"+ Printing.prettyPrint((value.get(i-1)-value.get(i)), "0.000000E00",11)+
					"\n");
			}
		}
		return res.toString();
	}
	
	
	public void collectInitStats(Optimizer optimizer, Objective objective){
		super.collectInitStats(optimizer, objective);
		iterations.add(-1);
		gradientNorms.add(ArrayMath.L2Norm(objective.getGradient()));
		steps.add(0.0);
		value.add(objective.getValue());
	}
	
	public void collectIterationStats(Optimizer optimizer, Objective objective){
		iterations.add(optimizer.getCurrentIteration());
		gradientNorms.add(ArrayMath.L2Norm(objective.getGradient()));
		steps.add(optimizer.getCurrentStep());
		value.add(optimizer.getCurrentValue());
	}
	
	
	public void collectFinalStats(Optimizer optimizer, Objective objective, boolean success){
		super.collectFinalStats(optimizer, objective, success);
		this.succeed = success;
		paramUpdates = objective.getNumberUpdateCalls();
		objectiveFinalStats = objective.finalInfoString();
		weightsNorm = util.ArrayMath.L2Norm(objective.getParameters());
	}
	
}
