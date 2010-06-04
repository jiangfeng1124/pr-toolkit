package depparsing.constraints;


public class GradientAscentProjection {
	
	public int _maxNumberIterations;
	public int _numberIterations;
	
	double optimizationPrecision;	
	

	public int projectionIteration;
	
	int debugLevel = 1;


	private LineSearchMethod linesearch;
	
	
	@SuppressWarnings("unchecked")
	public GradientAscentProjection(LineSearchMethod lsm, double stoppingPrecision, int maxIterations){
		linesearch = lsm;
		this.optimizationPrecision = stoppingPrecision;
		_maxNumberIterations = maxIterations;
	}
	
	
		
	/**
	 * Projects the posteriors using gradient ascent.
	 * 
	 * SIDE-EFFECTS  - Note the posteriors have to be in a proper state since they will be directly used for the 
	 * model updates.
	 * 
	 * Leaving conditions:
	 * 1 - Sentence satisfies all the constrains:
	 *     No changes are required just leave the loop		
	 * 2 - Projection concluded successfully in the number of steps
	 *     Posteriors are returned as is
	 * 3 - Projection did not finish in the number of allowed steps
	 *     Posteriors are returned as is
	 * 4 - No step size was found on line search
	 *     Return posteriors from the previous iteration and leave.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public ProjectionStats steepestAscentProjection(ProjectionConstraints o){
		ProjectionStats stats = new ProjectionStats();
		stats.startTime();
		_numberIterations = _maxNumberIterations;//Math.min((_maxNumberIterations/getModelMaxTrainingIteration())*getModelTrainingIteration(), _maxNumberIterations);	
		//Info on line search for step
		DualParameters ascentDirection = null;	
		double previousStepSize = -1;
		DualParameters gradient;
		double gradientDot  =0;
		double oldGradientNorm = Double.NaN;
		stats.originalObjective = o.getObjective();
		for( projectionIteration = 0; projectionIteration < _numberIterations; projectionIteration++){	
			if (debugLevel > 0) 
				System.out.println("------------------------");
			double objective = o.getObjective();
			gradient= o.getGradient();
			//if(projectionIteration>1) o.testGradient();
			
			gradientDot = gradient.dotProduct(gradient); 
			double gradientNorm = gradient.twoNorm();
			
			
			if (debugLevel > 0) System.out.println("ProjIter " + projectionIteration + " obj " + objective + " gradientDot " + gradientDot + "gradientNorm " + gradientNorm);
			//Test for leaving the projection 
			if(Double.isNaN(objective) || Double.isInfinite(objective)){
				System.out.println("Objective is not a number before starting to project  " );
				stats.objectiveBecomeNotANumber = true;
				break;
			}
			
			if(stoppingGradientCriteria(gradientNorm, gradient)){
				if (debugLevel > 0) 
					System.out.println("Stopping because of gradient..." + (gradientNorm/gradient.getNumParameters()));
				if(projectionIteration == 0){
					stats.noConstrains = true;
				}
				break;
			}
			
			if (debugLevel > 0 && gradientNorm > oldGradientNorm){
				System.out.println("Gradient norm went up!");
//				double numericalProjectionGradientNorm = o.testGradientProjection();
//				System.out.println("      Numerical = "+numericalProjectionGradientNorm);
//				System.out.println("      Computed  = "+gradientNorm);
//				if (stoppingGradientCriteria(numericalProjectionGradientNorm, gradient)){
//					System.out.println("Stopping because numerical projection gradient norm is small");
//					break;
//				}
			} 
			oldGradientNorm = gradientNorm;

			
			stats.numberOfProjections++;
			ascentDirection = o.getAscentDirection(gradient);
			DifferentiableLineSearchObjective lsobjective = new KLProjectionLineSearchObjective(o,o.lambda.deepCopy(),ascentDirection, stats);
			lsobjective.tell(0,objective, gradientDot);
			previousStepSize = linesearch.getStepSize(lsobjective, previousStepSize, gradientDot, stats);
			if(previousStepSize  == -1){
				if (debugLevel > 0) System.out.println("No step size found!");
				if (debugLevel > 0) System.out.println(lsobjective);
				break;
			}
			if (debugLevel >= 1 && ! lsobjective.isQuasiConcave()){
				System.out.println("Line search objective was not quasi-convex.  Maybe bug or numerical issue?");
				System.out.println(lsobjective);
				o.testGradientProjection();
			}

			if (debugLevel > 0) System.out.println("stepSize = "+previousStepSize);
			//Collecting stats
			int nrSteps = lsobjective.getNumberEvaluations();
			stats._numberOfSteps.add(nrSteps);
			if(stats.maxStepEval < nrSteps) stats.maxStepEval = nrSteps;

		} 						
		o.updateStats(stats);
		double objective = o.getObjective();
		stats.finalObjective = objective;
		if(projectionIteration == _numberIterations){
			stats.noConvergence = true;
			//Set number of projection to zero so tey don't count for average
			stats.numberOfProjections=0;
		}
		stats.stopTime();
		return stats;
	}
	
	
	public boolean stoppingGradientCriteria(double gradientNorm, DualParameters gradient){
		return gradientNorm/gradient.getNumParameters() <= optimizationPrecision; 
	}
		
	

	
}

