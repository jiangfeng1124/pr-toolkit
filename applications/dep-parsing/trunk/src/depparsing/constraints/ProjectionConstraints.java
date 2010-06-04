package depparsing.constraints;

import java.util.ArrayList;

public abstract class ProjectionConstraints {
	
	/**
	 * Optimization parameters
	 * c1 - Sufficient increase
	 * c2 - Sufficie Curvature
	 */
	double c1 = 1.0E-4;
	double c2 = 0.9;
	
	//Max step allowed on step search
	
	double maxStep;
	public int _maxNumberIterations;
	public int _numberIterations;
	public double[]gnorm;
	public double[] gval;
	public double[] gdot;
	public double[] glambda;
	double currentDot;
	int _maxExtrapolationIter = 20;
	int _maxZoomEvals = 20;
	//public int _maxTriesPerGradientStep = 20;
	double optimizationPrecision;	
	public DualParameters lambda;
	public double lambdaNorm = 0;
	public double slack = 0;
	

	double[] stepsUsed;
	int[] evaluationToPickBracketingSteps;
	ArrayList<Double>[] bracketingStepsUsed;
	double[] zoomSteps;
	int[] evaluationToPickzoomSteps;
	ArrayList<Double>[] zoomStepsUsed;
	
	public int projectionIteration;
	
//	public abstract int getNumParamters();
	public abstract double getObjective();
	public abstract DualParameters getGradient();
	public abstract DualParameters getAscentDirection(DualParameters gradient);
	public abstract void updateLambda(DualParameters ascentDirection, double stepSize, ProjectionStats stats);
	public abstract void setLambda(DualParameters oldLambda);
	public abstract void testGradient();
	public abstract double testGradientProjection();
	int debugLevel = 1;

	public double gradientNorm(DualParameters gradient){
		return Math.sqrt(gradient.dotProduct(gradient));
	}

	
	double conjugateGradientP;
	@SuppressWarnings({ "unchecked", "unused" })
	private ProjectionStats conjugateGradientProjection2(){
		
		c1 = 1.10-4;
		c2 = 0.9;
		
		
		ProjectionStats stats = new ProjectionStats();
		stats.startTime();	
		_numberIterations = _maxNumberIterations;//Math.min((_maxNumberIterations/getModelMaxTrainingIteration())*getModelTrainingIteration(), _maxNumberIterations);	
		
		if(debugLevel >= 1){
			gnorm = new double[_numberIterations];
			gval = new double[_numberIterations];
			gdot = new double[_numberIterations];
			glambda = new double[_numberIterations];
			stepsUsed = new double[_numberIterations];
			evaluationToPickBracketingSteps = new int[_numberIterations];
			bracketingStepsUsed = new ArrayList[_numberIterations];
			evaluationToPickzoomSteps = new int[_numberIterations];
			zoomStepsUsed = new ArrayList[_numberIterations];
		}
		
		DualParameters ascentDirection, previousAscentDirection;	
		double previousStepSize =0;
		double previousGradientDot =0;
		DualParameters currentGradient;
		DualParameters previousGradient;
		double currentObjective, previousObjective;
		double gradientDot  =0;
		double gradientNorm;
		
		//Before start iteration 0
		
		currentObjective = getObjective();
		previousObjective = currentObjective;
		//Added new
		if(Double.isNaN(currentObjective) || Double.isInfinite(currentObjective)){
			stats.objectiveBecomeNotANumber=true;
			
			return stats;
		}
		
		currentGradient= getGradient();
		
		
		

		gradientNorm = gradientNorm(currentGradient);
		ascentDirection = currentGradient.deepCopy();
		gradientDot = currentGradient.dotProduct(ascentDirection); 
		if(gradientDot < 0){
			System.out.println("Error not an ascent directon");
			throw new RuntimeException();
		}
		previousGradientDot = gradientDot; 
		previousGradient = currentGradient.deepCopy();
		 
		previousAscentDirection = currentGradient;
		DualParameters previousLambda; 
		DualParameters y, s;
		for( projectionIteration = 0; projectionIteration < _numberIterations; projectionIteration++){	
			if(debugLevel >= 1){
				gdot[projectionIteration] = gradientDot;
				gval[projectionIteration] = currentObjective;
				gnorm[projectionIteration]=gradientNorm;
				glambda[projectionIteration] = lambdaNorm;
//				if(projectionIteration > 0){
//					System.out.println("np " + getNumParamters() + "\t"+
//							projectionIteration + "\t" + currentObjective +
//							"("+(gval[projectionIteration]-gval[projectionIteration-1])+")" +
//							"\t" + gnorm[projectionIteration] + 
//							"("+(gnorm[projectionIteration]-gnorm[projectionIteration-1])+")"+ 
//							"\t" + gdot[projectionIteration] + 
//							"("+(gdot[projectionIteration]-gdot[projectionIteration-1])+")" + 
//							"\t" + glambda[projectionIteration] +
//							"("+(glambda[projectionIteration]-glambda[projectionIteration-1])+")"
//							+ "previous step " + previousStepSize
//							+ "conjugate Gradiet p " + conjugateGradientP);
//				}
			}
			
			
			if(stoppingGradientCriteria(gradientNorm, currentGradient)){
				if(projectionIteration == 0){
					stats.noConstrains = true;
				}
				break;
			}
			stats.numberOfProjections++;
			//Here the ascent direction is the conjugate direction
	
			previousLambda = lambda.deepCopy();
			
			// getStepSize return a changed model and model parameters wither to best value found
			// or the original values in which case it also return -1 and we leave
			
					

			
			previousStepSize = getStepSize2(currentObjective,gradientDot,previousStepSize,
					previousGradientDot,ascentDirection,currentGradient, projectionIteration,stats);	
			
			//Case where no step was found
			if(previousStepSize  == -1){
				break;
			}
			previousObjective = currentObjective;
			previousGradient = currentGradient.deepCopy();
			previousGradientDot = gradientDot;
			currentObjective = getObjective();
			currentGradient= getGradient();
			
			gradientNorm = gradientNorm(currentGradient);
			
			//Test for leaving the projection 
			if(Double.isNaN(currentObjective) || Double.isInfinite(currentObjective)){
				stats.objectiveBecomeNotANumber = true;
				break;
			}
			
			y = DualParameters.difference(currentGradient,previousGradient);
			s = DualParameters.difference(lambda, previousLambda);
			
			conjugateGradientP = getConjugateGradientParamenter(currentGradient, previousGradient, s,  y,  ascentDirection);
			previousAscentDirection = ascentDirection.deepCopy();
			ascentDirection = getConjugateDirection(currentGradient, ascentDirection, conjugateGradientP,s);
			
			//Restart criteria of Powel
			//No need only for non linear functions NA (pg 124)
			gradientDot = currentGradient.dotProduct(ascentDirection); 
			if(restartCriteria(currentGradient, previousGradient) || gradientDot < 0){
//			if(restartCriteria(currentGradient, previousGradient)){
				///System.out.println("Entramos no restar criteria\n\n " + projectionIteration);	
				ascentDirection = currentGradient.deepCopy();
		//		throw new RuntimeException();
				gradientDot = currentGradient.dotProduct(ascentDirection); 
			}
			
			//debug
			if(gradientDot < 0){
				stats.directionNotAscentDirection = true;
				break;
//				System.out.println("gradient dot" + gradientDot);
//				System.out.println("Error not an ascent direction after update gradien in main loop");
//				System.out.println("grained");
//				currentGradient.printAll();
//				System.out.println("s");
//				s.printAll();
//				System.out.println("ascent");
//				ascentDirection.printAll();
//				System.out.println("PAramenter" + conjugateGradientP);
//				throw new RuntimeException();
			}
			
			//Collecting stats
			int nrSteps = bracketingStepsUsed[projectionIteration].size() + zoomStepsUsed[projectionIteration].size();
			stats._numberOfSteps.add(nrSteps);
			if(stats.maxStepEval < nrSteps) stats.maxStepEval = nrSteps;
		} 				
		updateStats(stats);
		if(projectionIteration == _numberIterations){
			stats.noConvergence = true;
			//Set number of projection to zero so tey don't count for average
			stats.numberOfProjections=0;
		}
//		if(projectionIteration > 20){
//			System.out.println("NRCONSTRAINS\tIteration\tobjective\tnomr\tdot\tlambdaNorm");
//			for(int i = 1; i < projectionIteration; i++){
//				System.out.println("np " + getNumParamters() + "\t"+
//						i + "\t" + gval[i] +
//						"("+(gval[i]-gval[i-1])+")" +
//						"\t" + gnorm[i] + 
//						"("+(gnorm[i]-gnorm[i-1])+")"+ 
//						"\t" + gdot[i] + 
//						"("+(gdot[i]-gdot[i-1])+")" + 
//						"\t" + glambda[i] +
//						"("+(glambda[i]-glambda[i-1])+")"
//						);
//			}
//			for(int i = 0; i < projectionIteration; i++){
//				ArrayList stepUsedAux = bracketingStepsUsed[i];
//				System.out.println("bracketing steps no brackwting found? " + stats.noBracketingStepFound);
//				for(int j = 0; j < stepUsedAux.size(); j++){
//					System.out.print(stepUsedAux.get(j) + "\t");
//				}
//				System.out.println("\nZoom steps no zoom step found " + stats.noZoomStepFound);
//				stepUsedAux = zoomStepsUsed[i];
//				for(int j = 0; j < stepUsedAux.size(); j++){
//					System.out.print(stepUsedAux.get(j) + "\t");
//				}
//				System.out.println();
//			}
//			//System.exit(-1);
//		}
		stats.stopTime();
		return stats;
	}
	
	
	
	/**
	 * Several methods Using method from polak and ribiere 5.45 numerical optimization
	 * @param gradient
	 * @param previousGradient
	 * @param s
	 * @param y
	 * @param currentDirection
	 * @param previousDirection
	 * @return
	 */
	public double getConjugateGradientParamenter(DualParameters gradient, DualParameters previousGradient, 
			DualParameters s, DualParameters y, DualParameters previousDirection){
		//formual 5.45
		return Math.max(gradient.dotProduct(y)/previousGradient.dotProduct(previousGradient),0);
	}
	
	
	/**
	 * Implementes the powel condition to restart the conjugate gradient
	 * if |g_t+1,g_t| > 0.2 |g_t+1| then d_k+1 = g_k+1
	 * @param currentGradient
	 * @param previousGradient
	 * @return
	 */
	public boolean restartCriteria(DualParameters currentGradient, DualParameters previousGradient){
		return currentGradient.dotProduct(previousGradient) > 0.2*currentGradient.dotProduct(currentGradient);
	}
	
	public DualParameters getConjugateDirection(DualParameters gradient, DualParameters conjugateDirection, double conjugateGradientParameter, DualParameters s){
		return gradient.plus(s,conjugateGradientParameter);
	}
	
	/**
	 * Projects the posteriors using gradient ascent.
	 * 
	 * SIDE-EFFECTS  - Note the posteriors have to be in a proper state since they will be directly used for the 
	 * model updates.
	 * 
	 * Leaving conditions:
	 * 1 - Sentence satisfies all the constrains:
	 *  No changes are required just leave the loop		
	 * 2 - Projection concluded sucessfully in the number of steps
	 *  Posteriors are retuned as is
	 * 3 - Projection did not finish in the number of allowed steps
	 * Posteriors are retuned as is
	 * 4 - No step size was found on line search
	 *   Retuned posteriors from the previous iteration and leave.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public ProjectionStats steepestAscentProjection(){
		ProjectionStats stats = new ProjectionStats();
		stats.startTime();
		_numberIterations = _maxNumberIterations;//Math.min((_maxNumberIterations/getModelMaxTrainingIteration())*getModelTrainingIteration(), _maxNumberIterations);	
		gnorm = new double[_numberIterations];
		gval = new double[_numberIterations];
		gdot = new double[_numberIterations];
		glambda = new double[_numberIterations];
		//Info on line search for step
		stepsUsed = new double[_numberIterations];
		evaluationToPickBracketingSteps = new int[_numberIterations];
		bracketingStepsUsed = new ArrayList[_numberIterations];
		evaluationToPickzoomSteps = new int[_numberIterations];
		zoomStepsUsed = new ArrayList[_numberIterations];
		DualParameters ascentDirection = null;	
		double previousStepSize =0;
		double previousGradientDot =0;
		DualParameters gradient;
		double gradientDot  =0;
		for( projectionIteration = 0; projectionIteration < _numberIterations; projectionIteration++){	
//			System.out.println("------------------------");
//			testGradientProjection();
			double objective = getObjective();
			gval[projectionIteration] = objective;
			gradient= getGradient();

			previousGradientDot = gradientDot;
			
			gradientDot = gradient.dotProduct(gradient); 
			gdot[projectionIteration] = gradientDot;
			double gradientNorm = gradientNorm(gradient);
			//testGradient();
			
			System.out.println("ProjIter " + projectionIteration + " obj " + objective + " gradientDot " + gradientDot + "gradientNorm " + gradientNorm);
			gnorm[projectionIteration]=gradientNorm;
			glambda[projectionIteration] = lambdaNorm;
			//Test for leaving the projection 
			if(Double.isNaN(objective) || Double.isInfinite(objective)){
				System.out.println("Objective is not a number before starting to project  " );
				stats.objectiveBecomeNotANumber = true;
				break;
			}
			
			if(stoppingGradientCriteria(gradientNorm, gradient)){
				System.out.println("Stopping because of gradient...");
				if(projectionIteration == 0){
					stats.noConstrains = true;
				}
				break;
			}
			stats.numberOfProjections++;
			ascentDirection = getAscentDirection(gradient);
			previousStepSize = getStepSize2(gval[projectionIteration],gradientDot,previousStepSize,previousGradientDot,ascentDirection,gradient ,projectionIteration,stats);			
			if(previousStepSize  == -1){
				System.out.println("No step size found!");
				break;
			}
//			System.out.println("stepSize = "+previousStepSize);
			//Collecting stats
			int nrSteps = bracketingStepsUsed[projectionIteration].size() + zoomStepsUsed[projectionIteration].size();
			stats._numberOfSteps.add(nrSteps);
			if(stats.maxStepEval < nrSteps) stats.maxStepEval = nrSteps;

			//No need to update since pick step size already took care of that
			//updateLambda(ascentDirection, previousStepSize, stats);		
		//	System.out.println("Iterations " + projectionIteration + "lambda");
		//	lambda.print(getNumParamters(), 0);
		} 						
		updateStats(stats);
		if(projectionIteration == _numberIterations){
			stats.noConvergence = true;
			//Set number of projection to zero so tey don't count for average
			stats.numberOfProjections=0;
		}
		stats.stopTime();
		return stats;
	}
	
	
	public boolean stoppingGradientCriteria(double gradientNorm, DualParameters gradient){
	//	System.out.println("gradient norm = "+gradientNorm+"num params = "+gradient.getNumParameters()+"precision = "+optimizationPrecision);
		return gradientNorm/gradient.getNumParameters() <= optimizationPrecision; 
	}
	
	public abstract void updateStats(ProjectionStats stats);
	
	
	
	
	
	public void getLineSearchGraph(double initialStep, double finalStep, double increase,DualParameters ascentDirection,  DualParameters oldLambda, ProjectionStats stats, double originalObj, double originalDot){
		ArrayList<Double> stepS = new ArrayList<Double>();
		ArrayList<Double> obj = new ArrayList<Double>();
		ArrayList<Double> norm = new ArrayList<Double>();
		for(double step = initialStep; step < finalStep; step +=increase ){
			lambda = oldLambda;
			updateLambda(ascentDirection, step, stats);
			stepS.add(step);
			double objective = getObjective();
			if(Double.isNaN(objective) || Double.isInfinite(objective)){
				break;
			}
			
			obj.add(objective);
			double dot = ascentDirection.dotProduct(getGradient());
			norm.add(dot);
			lambda = oldLambda;
		}
		System.out.println("step\torigObj\tobj\tsuff increase\tnorm\tcurvature");
		for(int i = 0; i < stepS.size(); i++){
			System.out.println(stepS.get(i)+"\t"+originalObj +"\t"+obj.get(i) + "\t" + 
					(originalObj + originalDot*((Double)stepS.get(i))*c1) +"\t"+norm.get(i) +"\t"+c2*originalDot);
		}
	}
	
	
	public final boolean sufficientImprovement(double currentObjective, double initialObjective, double initialGradientDot, double currentStep){
	//	System.out.println("Sufficiemte imprvement returend  " + (currentObjective > initialObjective + initialGradientDot*currentStep*c1));
		return currentObjective > initialObjective + initialGradientDot*currentStep*c1;
	}
	
	/**
	 * Apply strong wolf
	 * @param initialGradientDot
	 * @param currentGradientDot
	 * @return
	 */
	public final boolean sufficientCurvature(double initialGradientDot, double currentGradientDot){
	//	System.out.println("Sufficiemte curvature "+currentGradientDot + "<=" + initialGradientDot+" " + (currentGradientDot <= c2*initialGradientDot));
		return Math.abs(currentGradientDot) <= c2*initialGradientDot;
	}
	public final boolean correctGradientSign(double currentGradient){
		return currentGradient > 0;
	}
	
	public double bracketingGetNextStep(double currentStep, double maxStep){
		return Math.min(2*currentStep, maxStep);
	}
	
	
	/**
	 * Numerical optimization algorithm 3.5
	 * SIDE-EFFECTS - The models parameters are left at the position of the best step. The main loop will use this values
	 * so be carefull not to damage this values....
	 * 
	 * Exit points
	 * 1 - A acceptable step size was found
	 *  In which case the step is returned and the lambda and model parameters are according to that 
	 *  value.
	 * 2 - No step size was found
	 *  -1 is returned and the model parameters are reset to the initial state
	 *  
	 * @param ascentDirection
	 * @param currentIteration
	 * @param stats
	 * @return
	 */
	public double getStepSize2(double initialObjective ,double initialGradientDot, double previousIterationStep, double previousIterationDot, DualParameters ascentDirection, DualParameters gradient, int currentIteration, ProjectionStats stats){
		double previousStep = 0;
		System.out.println("Starting getStep");
		System.out.println("objective" + initialObjective);
		System.out.println("gradinet" + initialGradientDot);
		initialObjective = getObjective();
		initialGradientDot = getGradient().dotProduct(ascentDirection);
		System.out.println("objective" + initialObjective);
		System.out.println("gradinet" + initialGradientDot);
		if(initialGradientDot < 1.E-10){
			System.out.println("Trying to pick steps with zero gradient on pick step");
			throw new RuntimeException();
		}
		
		//dEBUG if initial dot product is negative then we have a provlem we are no longer in as ascent direction
		if(initialGradientDot < 0){
			System.out.println("Error not an ascent direction when starting line search");
			throw new RuntimeException();
		}
		
		double currentStep;
		
		ArrayList<Double> bracketingSteps = new ArrayList<Double>();
		ArrayList<Double> zoomSteps = new ArrayList<Double>();
		
		//currentStep = (maxStep+previousStep)/2;
		if(currentIteration > 0){
			//currentStep = previousIterationStep;
			currentStep = pickInitalStepSize(previousIterationStep, initialGradientDot, previousIterationDot);
		}else{
			currentStep = 1;
			//currentStep = (maxStep+previousStep)/2;
		}
		
		if(debugLevel >= 1){
			if(currentStep == 0){
				System.out.println("output a step of zero??? iter " + currentIteration + " prev step " + previousIterationStep + " inital gradient dot " + initialGradientDot + " prev dot " + previousIterationDot);
				System.exit(-1);
			}
		}
		
		
	//	System.out.println("Using initial Step size of " + currentStep);
		
		//Save the original constrains.
		DualParameters oldLambda = lambda.deepCopy();
		double previousObjective = initialObjective;
		double previousGradient = initialGradientDot;
		double bestStep = maxStep;
		int extrapolationIteration;
		for(extrapolationIteration = 0; extrapolationIteration < _maxExtrapolationIter; extrapolationIteration++){	
			//testGradient();
			System.out.println("extrapolation iteration "+extrapolationIteration);
		
			//Debug sanity check
			if(currentStep< 0 || previousStep < 0){
				System.out.println("Brackwting step got to < than zerp");
				throw new RuntimeException();
			}
			
			
			bracketingSteps.add(currentStep);
			//Clear the lambdas
			lambda = oldLambda;
			//Update lambas with current step of the ascent direction
			updateLambda(ascentDirection, currentStep, stats);
			
			double currentObjective = getObjective();
			double currentGradientDot;
	
			//Check if we got a not a number for the objective
			if(Double.isNaN(currentObjective) || Double.isInfinite(currentObjective)){
				currentObjective = -5000;
				currentGradientDot = -500;
				//System.out.println("Discarding step size due to objective NAN  " + currentStep);
				bestStep = zoom(initialObjective,initialGradientDot,previousStep, currentStep, previousObjective, previousGradient, currentObjective,currentGradientDot,ascentDirection,oldLambda, stats,zoomSteps);
				break;
			}else{
				currentGradientDot  = getGradient().dotProduct(ascentDirection);
			}
			System.out.println("Looking for extrapolation step: OBJ " + currentObjective + " grad " + currentGradientDot + " Step " + currentStep);
			if(!sufficientImprovement(currentObjective, initialObjective, initialGradientDot, currentStep) ||  
					(!objectiveBetter(currentObjective, previousObjective) && extrapolationIteration > 0)){			
				if(currentStep < 0 || previousStep < 0){
					System.out.println("Brackwting step got to < than zerp");
					throw new RuntimeException();
				}
				bestStep = zoom(initialObjective,initialGradientDot,previousStep, currentStep, previousObjective, previousGradient, currentObjective,currentGradientDot,ascentDirection,oldLambda, stats,zoomSteps);
				break;
			}
			if(sufficientCurvature(initialGradientDot, currentGradientDot)){
				bestStep =  currentStep;
				break;
			}
			if(!correctGradientSign(currentGradientDot)){
				bestStep = zoom(initialObjective,initialGradientDot,currentStep, previousStep, currentObjective, currentGradientDot, previousObjective,previousGradient, ascentDirection,oldLambda, stats,zoomSteps);
				break;
			}
			previousStep = currentStep;
			previousObjective = currentObjective;
			currentStep = bracketingGetNextStep(currentStep, maxStep);
			
			//If no sufficient change in step size
			if(Math.abs(currentStep - maxStep) < 0.1){
				System.out.println("step size too close to max!");
				stats.noBracketingStepFound=true;
				bestStep = -1;
				//update model to old values
				lambda = oldLambda;
				updateLambda(ascentDirection, 0, stats);
				break;
			}
		}
		if( extrapolationIteration == _maxExtrapolationIter-1){
			System.out.println("Failing extrapolation iteration " + currentStep);
			bestStep = -1;
			lambda = oldLambda;	
			updateLambda(ascentDirection, 0, stats);
			stats.noBracketingStepFound = true;
		}
		
		if(debugLevel >= 1){
			stepsUsed[currentIteration]=bestStep;
			evaluationToPickBracketingSteps[currentIteration] = bracketingSteps.size();
			bracketingStepsUsed[currentIteration]=bracketingSteps;
			evaluationToPickzoomSteps[currentIteration] = zoomSteps.size();
			zoomStepsUsed[currentIteration]=zoomSteps;
		}
		if(debugLevel >= 1){
			if(Math.abs(bestStep) < 1.E-100){
				System.out.println("returning  a step of zero??? "+ bestStep +  " iter " + currentIteration + " prev step " + previousIterationStep + " inital gradient dot " + initialGradientDot + " prev dot " + previousIterationDot);
				System.out.println("bracketing steps ");
				for(int j = 0; j < bracketingSteps.size(); j++){
					System.out.print(bracketingSteps.get(j) + " ");
				}
				System.out.println("\nZoom steps ");
				for(int j = 0; j < zoomSteps.size(); j++){
					System.out.print(zoomSteps.get(j) + " ");
				}
				throw new RuntimeException();

			}
			
		}
	//	System.out.println("Returned " + bestStep);
		return bestStep;
	}
	
	/**
	 * Formula at page 59 (between 3.59 and 3.60)
	 * We assume that the first order change at the iterate will be the same as the in the previous 
	 * iteration.
	 * InitialStepSize = PreviousIterationStepSize*(PreviousGradientDot/ThisGradientDot) 
	 * @param leftValue
	 * @param rightValue
	 * @return
	 */
	public final double pickInitalStepSize(double previousIterationStep, double currentDotProductGrad, double prevIterationDotProductGrad){	
		//System.out.println("Picking intial Steps " + previousIterationStep + " prevNorm " + Math.abs(prevIterationDotProductGrad) + " currNomr " + Math.abs(currentDotProductGrad));
		return Math.min(maxStep, previousIterationStep*( (Math.abs(prevIterationDotProductGrad)+0.1)/(Math.abs(currentDotProductGrad)+0.1)));
	}
	/**
	 * TODO Can be improved
	 * @param leftValue
	 * @param rightValue
	 * @return
	 */
	public final double interpolateStep(double leftValue, double rightValue,
			double leftObjective, double leftGradient, double rightObjective, double rightGradient, int iteration){
//		if (iter > 1){
//			//Can do a cubic approximation
//			return (rightValue+leftValue)/2;
//		}else 
//			if(iter > 0){
//			//Need derivate at point zero
//			//objective at point zero
//			//derivative of previous iterate
//			//previous step
//			return -leftGradient*leftValue*leftValue/(2*(rightObjective-leftObjective-leftGradient*leftValue));
//		}else{
			//Just return the bisection
			return (rightValue+leftValue)/2;
//		}
	}
	
	public final boolean objectiveBetter(double currentObjective, double previousObjective){
		//System.out.println("Better than next returend  " + (currentObjective >= previousObjective));
		return currentObjective >= previousObjective;
	}
	
	public final double chooseShrinkDirection(double currentGradient, double alphaLow, double alphaHigh){
		if(currentGradient > 0)
			return Math.min(alphaLow, alphaHigh); 
		else  
			return Math.min(alphaLow, alphaHigh) ;
	}
	
	/**
	 * Debug function to check if the interval passed to zoom as the required properties:
	 * 1 - [l_low l_high] contains steps satisfying wolf conditions
	 * 2 - All points so far that satisfied the sufficient increase condition l_low is the one with highest function value
	 *     For simplicity just check that valye at alpha low is smaller than value at alpha high
	 * 3 - alpha_hi is such that gradient(alpha_low)*(alpha_high -alpha_low) < 0
	 * @return
	 */
	public boolean checkZoomConditions(double initialObjective, double initialGradient, 
			double alphaLow, double alphaHigh, 
			double alphaLowObjective, double alphaLowGradinetDot,
			double alphaHighObjective, double alphaHighGradinetDot,
			DualParameters ascentDirection, DualParameters oldLambda,
			ProjectionStats stats, ArrayList<Double> stepList){
		if(!(alphaLowGradinetDot*(alphaHigh-alphaLow) >=0)){
			System.out.println("Interval to zoom failed condition 3");
			throw new RuntimeException();
		}else 
			//Ignore first iteration case
			if(!(alphaLowObjective > alphaHighObjective) && alphaLow != 0){
			System.out.println("Interval to zoom failed condition 2 " + alphaLowObjective + " " + alphaHighObjective);
			throw new RuntimeException();
		}
//		else{
//			double i = 0;
//			//System.out.println("Step\t initialObj \t initGrad \t currentObj\tcurrentGradient\tsuffincrease\tcurvature");
//			for(i = 0; (i <= Math.max(alphaHigh,alphaLow)) ; i +=0.1){
//				updateLambda(ascentDirection, i, stats);
//				double currentObjective = getObjective();
//				double currentGradient = dotProduct(getGradient(), ascentDirection);
//				lambda = oldLambda;	
//				if(sufficientImprovement(currentObjective, initialObjective, initialGradient, i) && sufficientCurvature(initialGradient, currentGradient)){
//					return true;
//				}else{
//					System.out.println(i + "\t" + initialObjective + "\t" +initialGradient + "\t" + currentObjective + "\t" + currentGradient+"\t"+(initialObjective + initialGradient*i*c1)+"\t"+c2*initialGradient );
//				}
//				
//			}
//			
//			System.out.println("Interval to zoom failed condition 1 [" + alphaLow + " " + alphaHigh+"]");
//			throw new RuntimeException();
//		}
		return true;
	}
	/**
	 * 	 * SIDE-EFFECTS - The models parameters are left at the position of the best step. The main loop will use this values
	 * so be carefull not to damage this values....
	 * 
	 * Exit points
	 * 1 - A acceptable step size was found
	 *  In which case the step is returned and the lambda and model parameters are according to that 
	 *  value.
	 * 2 - No step size was found
	 *  -1 is returned and the model parameters are reset to the initial state
	 *  

	 * @param initialObjective
	 * @param initialGradient
	 * @param alphaLow
	 * @param alphaHigh
	 * @param alphaLowObjective
	 * @param alphaLowGradinetDot
	 * @param alphaHighObjective
	 * @param alphaHighGradinetDot
	 * @param ascentDirection
	 * @param oldLambda
	 * @param stats
	 * @param stepList
	 * @return
	 */
	public final double zoom(double initialObjective, double initialGradient, 
			double alphaLow, double alphaHigh, 
			double alphaLowObjective, double alphaLowGradinetDot,
			double alphaHighObjective, double alphaHighGradinetDot,
			DualParameters ascentDirection, DualParameters oldLambda,
			ProjectionStats stats, ArrayList<Double> stepList){	
		double initialLow = alphaLow;
		double initialHigh = alphaHigh;
		double previousObjective = alphaLowObjective;
		double previousGradient = alphaLowGradinetDot;
		double currentObjective = alphaLowObjective;
		double currentGradientDot = alphaLowGradinetDot;
		double bestStep = -1;
		int zoomIterations;
		if (debugLevel ==2){
			System.out.println("Entering zoom at iteration low " + alphaLow + " high " + alphaHigh);
			//debug code
		getLineSearchGraph(0, Math.max(alphaHigh,alphaLow), Math.max(alphaHigh,alphaLow)/40, ascentDirection,oldLambda, stats, initialObjective, initialGradient);
		
		checkZoomConditions(initialObjective, initialGradient, alphaLow, alphaHigh, alphaLowObjective, alphaLowGradinetDot, alphaHighObjective, alphaHighGradinetDot, ascentDirection, oldLambda, stats, stepList);
		}
		
		//System.out.println("starting zoom [" + alphaLow + " " + alphaHigh+"]");
		for(zoomIterations=0; zoomIterations < _maxZoomEvals; zoomIterations++){
			if(alphaLow < 0 || alphaHigh < 0){
				System.out.println("One of the boundaries reach bellow at zoom iteration " + zoomIterations + " init low " + initialLow + " init high " + initialHigh);
				throw new RuntimeException();
			}
			if (Math.abs(alphaLow - alphaHigh) < 1.E-4){
				if(alphaLow == 0){
					bestStep = -1;
					lambda = oldLambda;	
					updateLambda(ascentDirection, 0, stats);
					System.out.println("Failing zoom due to not finding value between " + alphaLow + "-" + alphaHigh);
					stats.noZoomStepFound = true;
				}else{
					bestStep =  alphaLow;
				}
//				//Debug
//				if(debugLevel == 1){
//					if(bestStep == 0){
//						System.out.println("Got best step zero whrn checking difference betwee interval");
//						System.out.println("low" + alphaLow + " high " + alphaHigh);
//						System.exit(-1);
//					}
//				}
				break;
			}
			
			double currentStepSize = interpolateStep(alphaLow, alphaHigh,previousGradient,previousObjective,currentObjective, currentGradientDot, zoomIterations);
			stepList.add(currentStepSize);
			lambda = oldLambda;	
			updateLambda(ascentDirection, currentStepSize, stats);
			
			currentObjective = getObjective();
			if(Double.isNaN(currentObjective) || Double.isInfinite(currentObjective)){
				currentObjective = -5000;
				currentGradientDot = -500;
				System.out.println("Discarding step size in zoom due to objective NAN low " + alphaLow + " high " + alphaHigh);
			}else{
				currentGradientDot  = getGradient().dotProduct(ascentDirection);
			}
			if(!sufficientImprovement(currentObjective, initialObjective, initialGradient, currentStepSize) ||  !objectiveBetter(currentObjective, previousObjective)){
				alphaHigh = currentStepSize;
				if(debugLevel == 2){
					System.out.println("changing for not sufficient improvement [" + alphaLow + " " + alphaHigh+"]" + " obj " + currentObjective + " init " + initialObjective);
				}
				//	System.out.println("changed to " + alphaLow + " - " + alphaHigh);
			}else{
				if(sufficientCurvature(initialGradient, currentGradientDot)){
					bestStep  =currentStepSize;
					break;
				}
				alphaHigh = chooseShrinkDirection(currentGradientDot, alphaLow, alphaHigh);
				alphaLow = currentStepSize;
			//	System.out.println("changing by shrinking direction [" + alphaLow + " " + alphaHigh+"]");
				previousObjective = currentObjective;
				previousGradient = currentGradientDot;
			}	
		}
		//If we did not find a point
		if(zoomIterations == _maxZoomEvals-1){
			System.out.println("Failing zoom exceed maximum itartiosn " + alphaLow + "-" + alphaHigh);
			bestStep = -1;
			lambda = oldLambda;	
			updateLambda(ascentDirection, 0, stats);
			stats.noZoomStepFound = true;
		}

		
		if(debugLevel >= 1){
			if(Math.abs(bestStep) < 1.E-100){
				System.out.println("zoom returning  a step of zero??? "+ bestStep + " alphaLow " + alphaLow + " alphaHigh " + alphaHigh);
				System.out.println(" Used iterations " + zoomIterations + " max " + _maxZoomEvals);
				System.out.println("\nZoom steps ");
				for(int j = 0; j < stepList.size(); j++){
					System.out.print(stepList.get(j) + " ");
				}
				throw new RuntimeException();

			}
			
		}

		return bestStep;
	}
	
}


//double prevStepSize = 0;		
//double maxStepSize = 10;
//double currentStepSize = pickInitalStepSize(prevStepSize,maxStepSize);
//double stepSize=currentStepSize; 
//double originalObjective = getObjective();
//double originalDot = dotProduct(ascentDirection,ascentDirection);
//double curvatureCondition = c2*originalDot;
//double prevObjective = originalObjective;
//double newObjective=originalObjective;
//double loop = 0;
//Trellis oldLambda = lambda.deppCopy();
//ArrayList stepsTried = new ArrayList();		
//ArrayList objs = new ArrayList();
//ArrayList norms = new ArrayList();
////getLineSearchGraph(ascentDirection, stats, originalObjective);
////System.exit(-1);
//while (true){
//	stepsTried.add(currentStepSize);
//	updateLambda(ascentDirection, currentStepSize, stats);
//	prevObjective = newObjective;
//	newObjective =getObjective();
//	double newGradientDot = dotProduct(ascentDirection, getGradient());
//	objs.add(newObjective);
//	norms.add(newGradientDot);
//	double sufficientIncreaseValue = originalObjective + c1*currentStepSize*originalDot;
//	if(newObjective < sufficientIncreaseValue || (newObjective <= prevObjective && loop > 0)){
//		System.out.println("Moving left barrier + obj " + newObjective + " suff " + sufficientIncreaseValue + " prev " + prevObjective + " loop " + loop);
//		stepSize = zoom(prevStepSize, currentStepSize,ascentDirection,stats,sufficientIncreaseValue,curvatureCondition,prevObjective);
//		stepsTried.add(stepSize);
//		break;
//	}
//	
//	if(newGradientDot >= curvatureCondition){
//		break;
//	}
//	if(newGradientDot <= 0){
//		System.out.println("Moving right barrier gradient " + newGradientDot );
//		stepSize = zoom(currentStepSize, prevStepSize,ascentDirection,stats,sufficientIncreaseValue,curvatureCondition,prevObjective);
//		stepsTried.add(stepSize);
//		break;
//	}
//	stepSize = currentStepSize;
//	currentStepSize = interpolateStep(currentStepSize, prevStepSize);
//	prevStepSize = stepSize;
//	stepSize = currentStepSize;
//	if(currentStepSize == prevStepSize ){
//		System.out.println("Error using same step size");
//		System.exit(-1);
//	}
//	this.lambda = oldLambda;
//	System.out.println("Trying " + stepSize);
//	loop++;
//}
//
//
//if(stepSize < 0){
//	System.out.println("Step size got smaller than zero " + stepSize);
//	System.exit(-1);
//}
//if((newObjective - originalObjective < 0)){
//	System.out.println("Objective decrease instead of increasing new " + newObjective + " orig " + originalObjective + " diff "+ (newObjective - originalObjective) + " step " + stepSize);
//	System.out.print("Tried : ");
//	for(int i =0; i< stepsTried.size(); i++){
//		System.out.print(stepsTried.get(i) + "\t");
//	}
//	System.out.println();
//	getLineSearchGraph(ascentDirection, stats,originalObjective);
//	System.out.println("");
//	getLineSearchGraph(ascentDirection, stats,originalObjective);
//	System.out.println("");
//	for(int i =0; i < objs.size(); i++){
//		System.out.println(stepsTried.get(i) +  "\t" + objs.get(i) + "\t" + norms.get(i));
//	}
//	
//	System.exit(-1);
//}
//System.out.println("Returned step size of " + stepSize + " L< " + (newObjective - originalObjective));
