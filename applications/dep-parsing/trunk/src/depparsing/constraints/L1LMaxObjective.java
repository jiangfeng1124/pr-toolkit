package depparsing.constraints;

import java.util.Arrays;
import java.util.Random;


import data.SentenceDistribution;
import depparsing.constraints.L1Lmax.SentenceChildParent;

import util.ArrayMath;
import util.LogSummer;

public class L1LMaxObjective extends ProjectionConstraints {

	private final L1Lmax parent;
	private Cache cache;
	static final double epsilon = 0.0001;

	class Cache{
		public L1LmaxParameters expectations;
		SentenceDistribution[] posteriors;
		public double logLikelihood;
		boolean stale;
		
		Cache(SentenceDistribution[] posteriors){
			stale = true;
			this.posteriors = posteriors;
			refreshExpectationsAndLikelihood();
		}
		
		private void refreshExpectationsAndLikelihood() {
			if (expectations==null) expectations = new L1LmaxParameters(parent);
			logLikelihood = 0;
			for (int constr = 0; constr < parent.edge2scp.length; constr++){
				for (int i = 0; i < parent.edge2scp[constr].length; i++) {
					SentenceChildParent scp = parent.edge2scp[constr][i];
					if (scp.p < 0){
						expectations.value[constr][i] = Math.exp(posteriors[scp.s].getRootPosterior(scp.c));
					} else {
						double p = Double.NEGATIVE_INFINITY;
						SentenceDistribution sd = posteriors[scp.s];
						for(int v = 0; v < sd.nontermMap.childValency; v++)
							p = LogSummer.sum(p, sd.getChildPosterior(scp.c, scp.p, v));
						expectations.value[constr][i] = Math.exp(p);
					}
				}
			}
			for (int sent = 0; sent < posteriors.length; sent++) {
				logLikelihood += posteriors[sent].insideRoot;
			}
		}

		/** runs inside-outside on the entire corpus, and updates expectations and likelihood */
		void update() {
			for (int sent = 0; sent < posteriors.length; sent++) {
				double[][][] child = posteriors[sent].child;
				int numWords = posteriors[sent].depInst.numWords;
				for (int c = 0; c < numWords; c++) {
					posteriors[sent].root[c] = parent.originalRoots[sent][c];
					for (int p = 0; p < numWords; p++) {
						System.arraycopy(parent.originalChildren[sent][c][p], 0, child[c][p], 0, child[c][p].length);
					}
				}
				
			}
			for (int constr = 0; constr < parent.edge2scp.length; constr++){
				for (int i = 0; i < parent.edge2scp[constr].length; i++) {
					SentenceChildParent scp = parent.edge2scp[constr][i];
					if (scp.p < 0){
						posteriors[scp.s].root[scp.c] -= ((L1LmaxParameters)lambda).value[constr][i];
					} else {
						for(int v = 0; v < posteriors[scp.s].nontermMap.childValency; v++)
							posteriors[scp.s].child[scp.c][scp.p][v] -= ((L1LmaxParameters)lambda).value[constr][i];
					}
				}
			}

			for (int sent = 0; sent < posteriors.length; sent++) {
				posteriors[sent].computeIO();
			}

			refreshExpectationsAndLikelihood();
			stale = false;
		}
		
		void setStale(){ stale = true;}
	}
	
	public L1LMaxObjective(L1LmaxParameters initialLambda, L1Lmax parent, SentenceDistribution[] posteriors) {
		lambda = initialLambda;
		this.parent = parent;
		cache = new Cache(posteriors);
	}
	
	@Override
	public DualParameters getAscentDirection(DualParameters gradient) {
		return gradient;
	}

	@Override
	public DualParameters getGradient() {
		if (cache.stale) {
			cache.update();
		}
		L1LmaxParameters res = (L1LmaxParameters) cache.expectations.deepCopy();
		projectGradient(res);
		return res;
	}

	@Override
	public double getObjective() {
		if (cache.stale) cache.update();
		return -cache.logLikelihood;
	}

	boolean doTestGradient = false;
	@Override
	public void setLambda(DualParameters oldLambda) {
		if(doTestGradient){
			doTestGradient = false;
			testGradient();
			doTestGradient = true;
		}
		cache.setStale();
		lambda.copyFrom(oldLambda.deepCopy());
		doLambdaProjection((L1LmaxParameters) lambda);
	}


	double testGradientStep = epsilon;
	int numdimstocheck = 10;
	@Override
	/**
	 * uses sampling to see whether the gradient is the same as taking a small step. 
	 * FIXME: not yet implemented. 
	 */
	public void testGradient() {
		// choose some dimensions
		L1LmaxParameters grad = (L1LmaxParameters) getGradient();
		double origObjective = getObjective();
		L1LmaxParameters origLambda = (L1LmaxParameters) lambda.deepCopy();
		L1LmaxParameters newLambda = (L1LmaxParameters) lambda.deepCopy();
		Random r = new Random(0);
		int[] dims = new int[numdimstocheck];
		int[] indices = new int[numdimstocheck];
		double[] algebraicGradient = new double[numdimstocheck];
		double[] numericalGradientP = new double[numdimstocheck];
		double[] numericalGradientN = new double[numdimstocheck];
 		for (int i = 0; i < dims.length; i++) {
			dims[i] = r.nextInt(grad.value.length);
			indices[i] = r.nextInt(grad.value[dims[i]].length);
			algebraicGradient[i] = grad.value[dims[i]][indices[i]];
			double myStepSize = this.testGradientStep*grad.value[indices[i]].length;
			{
				newLambda.value[dims[i]][indices[i]] += myStepSize;
				setLambda(newLambda);
				double currObjective = getObjective();
				numericalGradientP[i] = (currObjective - origObjective)/myStepSize;
				newLambda.value[dims[i]][indices[i]] -= myStepSize;
			} {
				newLambda.value[dims[i]][indices[i]] -= myStepSize;
				setLambda(newLambda);
				double currObjective = getObjective();
				numericalGradientN[i] = (origObjective - currObjective)/myStepSize;
				newLambda.value[dims[i]][indices[i]] += myStepSize;
			}
			
		}
 		
 		double [] numerical = new double[numdimstocheck];
 		for (int i = 0; i < numerical.length; i++) {
			double min = Math.min(numericalGradientN[i], numericalGradientP[i]);
			double max = Math.max(numericalGradientN[i], numericalGradientP[i]);
			if (min <= algebraicGradient[i] && algebraicGradient[i] <= max) numerical[i] = algebraicGradient[i];
			if (min > algebraicGradient[i]) numerical[i] = min;
			if (max < algebraicGradient[i]) numerical[i] = max;
			// if alg > 0, then we will add. In this case we want numP > 0
			// if alg < 0, tehn we will subtract, and we want numM < 0
			if (algebraicGradient[i] > 0 && numericalGradientP[i] <= 0){
				System.out.println(" *** ASSERT FAILED ***"+
						String.format(" - %11s  %.4f  %.4f  %.4f  l%.4f (%.4f)    p%.4f (%.4f)", 
						parent.cstraints.constraint2string(dims[i]),
						numericalGradientP[i], numericalGradientN[i], 
						algebraicGradient[i],
						origLambda.value[dims[i]][indices[i]],
						ArrayMath.sum(origLambda.value[dims[i]]),
						cache.expectations.value[dims[i]][indices[i]],
						ArrayMath.max(cache.expectations.value[dims[i]])
						));
			}
		}
 		double cosine = ArrayMath.cosine(numerical, algebraicGradient);
 		System.out.println("Test gradient (num ,algebraic) cos = "+cosine+" (angle "+Math.acos(cosine));
 		double cosineP = ArrayMath.cosine(numericalGradientP, algebraicGradient);
 		System.out.println("Test gradient (num+,algebraic) cos = "+cosineP+" (angle "+Math.acos(cosineP));
 		double cosineN = ArrayMath.cosine(numericalGradientN, algebraicGradient);
 		System.out.println("Test gradient (num-,algebraic) cos = "+cosineN+" (angle "+Math.acos(cosineN));
 		if(cosine < 2){
 			System.out.println("     type      num+  num-  algebraic lambda (sum) posteriors (max)");
 			for (int i = 0; i < numericalGradientP.length; i++) {
				System.out.println(String.format(" - %11s  %.4f  %.4f  %.4f  l%.4f (%.4f)    p%.4f (%.4f)", 
						parent.cstraints.constraint2string(dims[i]),
						numericalGradientP[i], numericalGradientN[i], 
						algebraicGradient[i],
						origLambda.value[dims[i]][indices[i]],
						ArrayMath.sum(origLambda.value[dims[i]]),
						cache.expectations.value[dims[i]][indices[i]],
						ArrayMath.max(cache.expectations.value[dims[i]])
						));
			}
 		}
 		setLambda(origLambda);
 		getObjective();
		
//		L1LmaxParameters preproj = (L1LmaxParameters) cache.expectations.deepCopy();
//		// FIXME remove this debugging code.. 
//		double[] sums = new double[res.value.length];
//		int max = 0;
//		for (int i = 0; i < sums.length; i++) {
//			sums[i] = ArrayMath.sum(res.value[i]);
//			if(parent.cstraints.constraint2string(i).substring(0,4).equals("root"))
//				System.out.println("   - "+parent.cstraints.constraint2string(i)+" : "+
//						String.format("%.4f lambda = %.4f   preproj = %.4f", sums[i], 
//						ArrayMath.sum(((L1LmaxParameters)lambda).value[i])
//						,ArrayMath.sum(((L1LmaxParameters)preproj).value[i])));
//			if (sums[max]< sums[i]) max = i;
//		}
//		System.out.println("   - "+parent.cstraints.constraint2string(max)+" : "+
//				String.format("%.4f lambda = %.4f   preproj = %.4f", sums[max], 
//				ArrayMath.sum(((L1LmaxParameters)lambda).value[max])
//				,ArrayMath.sum(((L1LmaxParameters)preproj).value[max])));
		

	}

	/**
	 * this method is not needed when we do gradient projection by taking a small step 
	 * in the gradient direction.  
	 */
	@Override
	public double testGradientProjection() {
		return 0;
	}

	@Override
	public void updateLambda(DualParameters ascentDirection, double stepSize,
			ProjectionStats stats) {
		cache.setStale();
		lambda.plusEquals(ascentDirection, stepSize);
		doLambdaProjection((L1LmaxParameters) lambda);
	}

	@Override
	public void updateStats(ProjectionStats stats) {
		// TODO Auto-generated method stub

	}

	/**
	 * This projects the gradient by doing:
	 * gradient <= (project(lambda + epsilon * gradient) - lambda)/epsilon
	 * which is identical to:
	 *  - take a small step in the gradien direction
	 *  - do the projection 
	 *  - see in what direction you actually took a small step. 
	 */
	private void projectGradient(L1LmaxParameters gradient) {
		gradient.scaleBy(epsilon);
		gradient.plusEquals(lambda, 1);
		doLambdaProjection(gradient);
		gradient.plusEquals(lambda, -1);
		gradient.scaleBy(1/epsilon);
	}

	private void doLambdaProjection(L1LmaxParameters lambda) {
		for (int edgeType = 0; edgeType < lambda.value.length; edgeType++) {
			double[] ds = lambda.value[edgeType].clone();
			double theta = doSimplexProjection(ds, parent.getConstraintStrength(edgeType));
			for (int i = 0; i < lambda.value[edgeType].length; i++) {
				double v = lambda.value[edgeType][i];
				lambda.value[edgeType][i] = Math.max(0, v-theta);
			}
			ds = lambda.value[edgeType].clone();
			theta = doSimplexProjection(ds, parent.getConstraintStrength(edgeType));
			for (int i = 0; i < lambda.value[edgeType].length; i++) {
				double v = lambda.value[edgeType][i];
				lambda.value[edgeType][i] = Math.max(0, v-theta);
			}
			// test that simplex projection is not broken... 
			if (theta > 0 && !almost(ArrayMath.sum(lambda.value[edgeType]),parent.getConstraintStrength(edgeType), 1e-2 ) ){
				System.out.println("  ***** simplex projection failure! theta="+theta);
				System.out.println(" length = "+lambda.value[edgeType].length+" sum="+ArrayMath.sum(lambda.value[edgeType])+" cstrength"+parent.getConstraintStrength(edgeType));
			}
		}
	}

	
	/**
	 * Computes the theta that we need to subtract from all the entries of 
	 * v in order to project it onto the scaled simplex. 
	 * WARNING : this destructively modifies ds! 
	 * @param ds
	 * @return
	 */
	protected double doSimplexProjection(double[] ds, double scale) {
		for (int i = 0; i < ds.length; i++) ds[i] = ds[i]>0? ds[i]:0;
		double sum = ArrayMath.sum(ds);
		if (sum <= scale && !almost(sum,scale)) return 0;
		sortDescending(ds);
//		double dsum = MathUtil.sum(ds);
//		if (Math.abs(dsum-sum)>0) System.out.println("sum = "+sum+" sorted = "+MathUtil.sum(ds)+"diff = "+(dsum-sum));
		sum=0;
//		System.out.println("mu = "+Arrays.toString(ds));
		double oldSum = Double.NaN;
		for (int i =  0; i <ds.length; i++) {
//			System.out.println("mu["+i+"] = "+ds[i]);
			sum+= ds[i];
			// number used so far is i+1.
			double theta = (sum-scale)/(i+1);
//			System.out.println("theta["+i+"] = "+theta);
			if (ds[i]<theta) return (oldSum-scale)/(i);
			oldSum = sum;
		}
		// they're all too big!
		return (sum - scale)/ds.length;
	}

	private void sortDescending(double[] ds){
		for (int i = 0; i < ds.length; i++) ds[i] = -ds[i];
		Arrays.sort(ds);
		for (int i = 0; i < ds.length; i++) ds[i] = -ds[i];
	}
	
	protected boolean almost(double a, double b, double prec){
		return Math.abs(a-b)/Math.abs(a+b) <= prec || (almostZero(a) && almostZero(b));
	}

	protected boolean almost(double a, double b){
		return Math.abs(a-b)/Math.abs(a+b) <= 1e-10 || (almostZero(a) && almostZero(b));
	}

	boolean almostZero(double a) {
		return Math.abs(a) <= 1e-30;
	}

	
}
