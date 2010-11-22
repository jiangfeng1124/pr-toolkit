package postagging.learning.stats.directMultinomialMaxEnt;

import model.chain.hmm.directGradientStats.MultinomialMaxEntDirectTrainerStats;
import optimization.gradientBasedMethods.Objective;
import optimization.gradientBasedMethods.Optimizer;
import util.ArrayMath;
import util.Printing;

/**
 * 
 * @author graca
 * Collects optimization stats
 */
public class DirectGradientOptimizationStats extends MultinomialMaxEntDirectTrainerStats{
	
	public String getPrefix(){
        return "OPT::";
	}
	
	double prevValue = Double.NaN;
	double originalNorm = Double.NaN;
	double prevUpdates = 0;
	
	public String iterationOutputString(Optimizer optimizer, Objective objective){
		StringBuffer res = new StringBuffer();
		
		double norm = ArrayMath.L2Norm(objective.gradient);
		if(Double.isNaN(originalNorm)) originalNorm = norm;
		res.append(" Iteration "
				+getIterationNumber()
				+ " updates "
				+(objective.getNumberUpdateCalls() -prevUpdates)
				+" step "
				+Printing.prettyPrint(optimizer.getCurrentStep(), "0.00E00", 6)
				+" Params "
				+Printing.prettyPrint(ArrayMath.L2Norm(objective.getParameters()), "0.00E00", 6)
				+ " gradientNorm "+ 
				Printing.prettyPrint(norm, "0.00000E00", 10)
				+ " gradientNormalizedNorm "+ 
				Printing.prettyPrint(norm/originalNorm, "0.00000E00", 10)
				+ " value "+ Printing.prettyPrint(optimizer.getCurrentValue(), "0.000000E00",11));	
		if(!Double.isNaN(prevValue)){
					res.append(" valueDiff "+ Printing.prettyPrint((prevValue-optimizer.getCurrentValue()), "0.000000E00",11));
					res.append(" normValue "+ Printing.prettyPrint((1-(optimizer.getCurrentValue()/prevValue)), "0.000000E00",11));
		}
		
		res.append("\n");
		prevValue = optimizer.getCurrentValue();
		prevUpdates = objective.getNumberUpdateCalls();
		return res.toString();
	}
}
	
