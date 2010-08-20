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

	public String iterationOutputString(Optimizer optimizer, Objective objective){
		StringBuffer res = new StringBuffer();
		res.append(" Iteration "
				+getIterationNumber()
				+ " updates "
				+objective.getNumberUpdateCalls()
				+" step "
				+Printing.prettyPrint(optimizer.getCurrentStep(), "0.00E00", 6)
				+" Params "
				+Printing.prettyPrint(ArrayMath.L2Norm(objective.getParameters()), "0.00E00", 6)
				+ " gradientNorm "+ 
				Printing.prettyPrint(ArrayMath.L2Norm(optimizer.getDirection()), "0.00000E00", 10)
				+ " value "+ Printing.prettyPrint(optimizer.getCurrentValue(), "0.000000E00",11));
		if(!Double.isNaN(prevValue)){
					res.append(" valueDiff "+ Printing.prettyPrint((prevValue-optimizer.getCurrentValue()), "0.000000E00",11));			
		}
		res.append("\n");
		prevValue = optimizer.getCurrentValue();
		return res.toString();
	}
}
	
