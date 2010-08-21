package postagging.constraints;

import constraints.CorpusConstraints;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;

import util.MathUtil;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TObjectProcedure;

import optimization.gradientBasedMethods.Objective;
import optimization.gradientBasedMethods.ProjectedGradientDescent;
import optimization.gradientBasedMethods.stats.ProjectedOptimizerStats;
import optimization.linesearch.ArmijoLineSearchMinimizationAlongProjectionArc;
import optimization.linesearch.LineSearchMethod;
import optimization.linesearch.NonNewtonInterpolationPickFirstStep;
import optimization.stopCriteria.CompositeStopingCriteria;
import optimization.stopCriteria.NormalizedProjectedGradientL2Norm;
import optimization.stopCriteria.NormalizedValueDifference;
import optimization.stopCriteria.StopingCriteria;
import postagging.data.PosCorpus;
import postagging.learning.stats.AccuracyStats;
import postagging.model.PosHMMFinalState;
import util.ArrayMath;
import util.MemoryTracker;
import learning.CorpusPR;
import learning.EM;
import learning.stats.CompositeTrainStats;
import learning.stats.GlobalEMTimeCounter;
import learning.stats.LikelihoodStats;
import learning.stats.MemoryStats;
import learning.stats.TrainStats;
import model.AbstractCountTable;
import model.AbstractSentenceDist;
import model.chain.hmm.HMM;
import model.chain.hmm.HMMSentenceDist;


/**
 *
 * 
 * @author javg
 *
 */
public class L1SoftMaxEG implements CorpusConstraints{

	
	int nrWordTypesToProject;
	//number of hidden states
	int nrHiddenStates;
	//Number of parameters that the objective will have. This will be the sum
	// for each word type that is being used of nrInstance*nrHiddenStates
	int numberOfParameters=0;
	
	//Contains for sentence number, sentence position, tag a unique number
	// that is used 
	int[][][] posteriorMapping;
	
	/**
	 * For each wordType that is being used, for each hidden state the list of positions in 
	 * the parameters that have to be projected.
	 */
	TIntObjectHashMap<TIntObjectHashMap<TIntArrayList>> projectionMapping;
	
	double str;
	
	int minOccurences;
	PosCorpus c;
	HMM model;
	//Contians the parameters of the model
	public double[] parameters;
	public double initialStep = 1000;
	public final double softMaxSharpness; 
	/**
	 * 
	 * @param minOccurences - Min occurrences of a word so that it will be constraint
	 */
	public L1SoftMaxEG(PosCorpus c, HMM model,int minOccurences,double str, double softMaxSharpness){
		MemoryTracker mem = new MemoryTracker();
		mem.start();
		this.str = str;
		this.c = c;
		this.model = model;
		this.softMaxSharpness = softMaxSharpness;
		nrHiddenStates = model.getNrRealStates();
		this.minOccurences = minOccurences;
		buildMapping();
		parameters = new double[numberOfParameters];
		System.out.println("L1LMax constraints with " + numberOfParameters + " parameters");
		mem.finish();
		System.out.println("L1LMax After build mapping: " + mem.print());
	}
	
	
	
	/**
	 * Builds two mappings:
	 * posteriorMapping - For sentenceNr, position in sentence tag -- Position in parameter vector
	 * projectionMapping - WordType, tag, instance -  Position in parameter vector
	 */
	public void buildMapping(){
		nrWordTypesToProject=0;
		numberOfParameters=0;
		projectionMapping = new TIntObjectHashMap<TIntObjectHashMap<TIntArrayList>>();
		int nrTrainingSentences = c.getNrOfTrainingSentences();
		posteriorMapping = new int[nrTrainingSentences][][];
		for(int sentenceNr = 0; sentenceNr <nrTrainingSentences; sentenceNr++){
			int[] positions = c.trainInstances.instanceList.get(sentenceNr).words;
			posteriorMapping[sentenceNr]=new int[positions.length][nrHiddenStates];
			for (int i = 0; i < positions.length; i++) {
				int wordType = positions[i];
				for(int hs = 0; hs < nrHiddenStates; hs++){
					if(c.getWordTypeCounts(wordType) >= minOccurences){
						posteriorMapping[sentenceNr][i][hs]=numberOfParameters;
						if(!projectionMapping.contains(wordType)){
							projectionMapping.put(wordType, new TIntObjectHashMap<TIntArrayList>());
						}
						TIntObjectHashMap tagMapping = projectionMapping.get(wordType);
						if(!tagMapping.contains(hs)){
							tagMapping.put(hs, new TIntArrayList());
						}
						TIntArrayList instances = (TIntArrayList) tagMapping.get(hs);
						instances.add(numberOfParameters);
						numberOfParameters++;
					}else{
						posteriorMapping[sentenceNr][i][hs]=-1;
					}
				}
			}
		}
		nrWordTypesToProject=projectionMapping.size();
	}
	
	public double project(AbstractCountTable counts, AbstractSentenceDist[] posteriors, TrainStats trainStats, CorpusPR pr) {
		MemoryTracker mem  = new MemoryTracker();
		mem.start();
		
		
		//Make a copy of the original parameters:
		//Initialize the gradient and the function values and the original posteriors cache
		trainStats.eStepStart(model, pr);
		L1LMaxObjective objective = new L1LMaxObjective(model, posteriors, numberOfParameters, str, softMaxSharpness, parameters);
		mem.finish();
		System.out.println("After creating objective:" + mem.print());

		for (int i = 0; i < 10; i++) {
			objective.takeExpGradientStep(1.0/(i+1));
		}
		
//		
//		ProjectedOptimizerStats stats = new ProjectedOptimizerStats();
//		StopingCriteria stopGrad = new NormalizedProjectedGradientL2Norm(0.001);
//		StopingCriteria stopValue = new NormalizedValueDifference(0.0001);
//		CompositeStopingCriteria stop = new CompositeStopingCriteria();
//		stop.add(stopGrad);
//		stop.add(stopValue);
//		

		mem.finish();
		
		System.out.println("After  optimization:" + mem.print());
//		System.out.println("Suceess " + succed + "/n"+stats.prettyPrint(1));
		
		
		//System.out.println("Parameters after optimization:\n" + objective.toString());
		parameters = objective.getParameters();
//		initialStep = optimizer.getCurrentStep();
		//update the caches of posterior and computer forward backward to compute counts and posterior
		counts.fill(0);
		for(int sentenceNr = 0; sentenceNr <posteriors.length; sentenceNr++){
			//For each position in the sentence
			HMMSentenceDist posterior = (HMMSentenceDist)posteriors[sentenceNr];
			posterior.initSentenceDist();
			int[] words = posterior.instance.words;
			for(int sentecePosition= 0; sentecePosition < words.length; sentecePosition++){
				//Check if we want to constrain this particular word type
				int wt = words[sentecePosition];
				if(projectionMapping.contains(wt)){
					//For each possible hidden state
					for(int hs = 0; hs < nrHiddenStates; hs++){
						//Get the corresponding parameters
						double parameter = objective.getParameters()[posteriorMapping[sentenceNr][sentecePosition][hs]];
						assert (!Double.isNaN(parameter));
						posterior.observationCache[sentecePosition][hs] *= Math.exp(parameter);
						assert (!Double.isNaN(posterior.observationCache[sentecePosition][hs]));
					}
				}
			}	
			trainStats.eStepSentenceStart(model,pr,posterior);
			model.computePosteriors(posterior);
			model.addToCounts(posterior, counts);
			trainStats.eStepSentenceEnd(model,pr,posterior);
			posterior.clearCaches();
			posterior.clearPosteriors();
		}
		mem.finish();
		System.out.println("End project objective:" + mem.print());
		trainStats.eStepEnd(model, pr);
		System.out.print(trainStats.printEndEStep(model,pr));
		// FIXME -- to test.  
		return Double.NaN;
	}
	
	/**
	 * 
	 * @author javg
	 *
	 * This represents the dual objective to the L1Lmax penalty.  The primal 
	 * objective is:
	 *     min   KL(q||p) + \sum_wt Xi_wt
	 *     s.t.  Xi_wt <= E_q[f_wti] for all w,t,i
	 * where w is a word type, t is the hidden state and i is the index of the word
	 * position in the occur the corpus. 
	 *   The dual objective is:
	 *   min_lambda log(z_lambda) st \sum_i lambda_wti = scale
	 *   Where z_lambda = Sum_z p(z)*exp(-lambda*f)
	 *   
	 *   The gradient of this function is
	 *   grad(lambda_wti) = -E[f_wti] for each feature
	 *
	 */
	public class L1LMaxObjective extends Objective{

		//strength of the penaly term (sigma in the writeups)
		final double scale;
		
		// sharpness of the softmax (max is lim as sharpness approaches infty, but we have to represent sum(exp(sharpness)). 
		final double sharpness;
		
		// the original posteriors (needed to compute the gradient)
		final double[] originalPosteriors;
		
		final double[] newPosteriors;
		
		final AbstractSentenceDist[] sentencesDists;
		
		double logLikelihood = 0;
	
		/**
		 * @param sentenceDists
		 * @param nrParameters
		 * @param scale
		 */
		public L1LMaxObjective(HMM model, 
				AbstractSentenceDist[] sentenceDists,
				int nrParameters, 
				double scale,
				double sharpness,
				double[] parameters){
			this.scale = scale;
			this.sharpness = sharpness;
			// not sure if we need to store this right now.. maybe we can avoid it. 
			gradient = new double[nrParameters];
			originalPosteriors = new double[nrParameters];
			newPosteriors = new double[nrParameters];
			this.parameters = parameters;
			
			sentencesDists = sentenceDists;		
			
			// save the observation posteriors into the originalPosteriors array
			for(int sentenceNr = 0; sentenceNr <sentencesDists.length; sentenceNr++){
				//For each position in the sentence
				HMMSentenceDist sd = (HMMSentenceDist)sentencesDists[sentenceNr];
				//Only needs to recreat the observation cache all the others are the same
				sd.initSentenceDist();
				model.computePosteriors(sd);
				int[] words = sd.instance.words;

				logLikelihood += sd.logLikelihood;
				//Update the gradient
				for(int sentecePosition= 0; sentecePosition < words.length; sentecePosition++){
					int wt = words[sentecePosition];
					if(projectionMapping.contains(wt)){
						for(int hs = 0; hs < nrHiddenStates; hs++){
							originalPosteriors[getParameterIndex(sentenceNr,sentecePosition,hs)] = 
								sd.observationPosterior[sentecePosition][hs];
						}
					}
				}
				sd.clearCaches();
				sd.clearPosteriors();
			}			
			updateFunction();
		}
		
		public void takeExpGradientStep(double stepSize){
			for (int i = 0; i < parameters.length; i++) {
				parameters[i] -= stepSize*gradient[i];
				assert !Double.isInfinite(parameters[i]);
				assert !Double.isNaN(parameters[i]);
			}
			// just so we don't run out of numbers, max-subtract at each position.  This is the same as dividing 
			// all the observations at a position by a constant; the likelihood changes, but the distribution won't. 
			for(int sentenceNr = 0; sentenceNr <sentencesDists.length; sentenceNr++){
				HMMSentenceDist sd = (HMMSentenceDist)sentencesDists[sentenceNr];
				int[] words = sd.instance.words;
				for(int sentecePosition= 0; sentecePosition < words.length; sentecePosition++){
					int wt = words[sentecePosition];
					if(projectionMapping.contains(wt)){
						double max=Double.NEGATIVE_INFINITY;
						for(int hs = 0; hs < nrHiddenStates; hs++){
							max = Math.max(max, parameters[getParameterIndex(sentenceNr,sentecePosition,hs)]);
						}
						assert !Double.isNaN(max);
						assert !Double.isInfinite(max);
						for(int hs = 0; hs < nrHiddenStates; hs++){
							parameters[getParameterIndex(sentenceNr,sentecePosition,hs)] -= max;
						}
					}
				}
			}
			updateFunction();
		}
	
		public void setParameters(double[] params) {
			super.setParameters(params);
			updateFunction();
		}

		/**
		 * Maps from the posterior distributions to the parameters/gradients indexes.
		 * @param senteceNumber
		 * @param sentencePosition
		 * @param tag
		 * @return
		 */
		public final int getParameterIndex(int senteceNumber, int sentencePosition, int tag){
			return posteriorMapping[senteceNumber][sentencePosition][tag];
		}
		
		/**
		 * This is where all work is done.
		 * Using the parameters we update the caches
		 * and we run forward backward for all sentences.
		 * This function also update the gradient and the function
		 * value so the corresponding functions only return the values.
		 * 
		 */
		public void updateFunction(){
			logLikelihood = 0;
			//For each sentence
			
			//Clear the counts
			for(int sentenceNr = 0; sentenceNr <sentencesDists.length; sentenceNr++){
				//For each position in the sentence
				HMMSentenceDist sd = (HMMSentenceDist)sentencesDists[sentenceNr];
				//Only needs to recreat the observation cache all the others are the same
				sd.initSentenceDist();
				int[] words = sd.instance.words;
				for(int sentecePosition= 0; sentecePosition < words.length; sentecePosition++){
					//Check if we want to constrain this particular word type
					int wt = words[sentecePosition];
					if(projectionMapping.contains(wt)){
						//For each possible hidden state
						for(int hs = 0; hs < nrHiddenStates; hs++){
							//Get the corresponding parameters
							double parameter = parameters[getParameterIndex(sentenceNr,sentecePosition,hs)];
							assert !Double.isNaN(parameter);
							assert !Double.isInfinite(parameter);
							sd.observationCache[sentecePosition][hs] *= Math.exp(parameter);
							assert (!Double.isNaN(sd.observationCache[sentecePosition][hs]));
							assert (!Double.isInfinite(sd.observationCache[sentecePosition][hs]));
						}
					}
				}
				
				model.computePosteriors(sd);
				logLikelihood += sd.logLikelihood;
				for(int sentecePosition= 0; sentecePosition < words.length; sentecePosition++){
					int wt = words[sentecePosition];
					if(projectionMapping.contains(wt)){
						for(int hs = 0; hs < nrHiddenStates; hs++){
							int pind = getParameterIndex(sentenceNr,sentecePosition,hs);
							newPosteriors[pind] = sd.observationPosterior[sentecePosition][hs];
							assert (!Double.isNaN(newPosteriors[pind]));
							assert (!Double.isInfinite(newPosteriors[pind]));
						}
					}
				}
				sd.clearCaches();
				sd.clearPosteriors();
			}
			// 
			setGradientToSoftMaxPart(newPosteriors);
			for (int i = 0; i < gradient.length; i++) {
				if(MathUtil.almostZero(newPosteriors[i]) && MathUtil.almostZero(originalPosteriors[i])) continue;
				gradient[i] += Math.log(newPosteriors[i]) - Math.log(originalPosteriors[i]);
				assert(!Double.isNaN(gradient[i]));
				assert(!Double.isInfinite(gradient[i]));
			}
			
			
//			long endTime = System.currentTimeMillis();
//			System.out.println("Update params took: "+ util.Printing.formatTime(endTime-initTime));
			//System.out.println(util.Printing.doubleArrayToString(gradient, null, "gradient"));
		}
		
		public double[] getGradient() {
			gradientCalls++;
			return this.gradient;
		//	System.arraycopy(this.gradient, 0, gradient, 0, gradient.length);
		}

		/**
		 * Value is the  loglikelihood
		 */
		public double getValue() {
			functionCalls++;
			return logLikelihood;
		}


		/**
		 * Sets gradient to sigma * g_wti / (sum_i g_wti). 
		 * For all w,t,i where g = exp(alpha * E_q[phi_wti]).  This is the part of the gradient 
		 * that depends on the projection. 
		 */
		public void setGradientToSoftMaxPart(final double[] qPosteriors) {
			// projectionMapping is a mapping from word -> tag -> instance_# -> parameter_index
			// e.g. maybe projectionMapping[run][NN][5] = 534234 means that the parameter 534234
			// corresponds to the 5th occurrence of "run" as a "noun". 
			projectionMapping.forEachValue(new TObjectProcedure<TIntObjectHashMap<TIntArrayList>>() {
				public boolean execute(TIntObjectHashMap<TIntArrayList> tag2id2paramIndex) {
					return tag2id2paramIndex.forEachValue(new TObjectProcedure<TIntArrayList>() {
						public boolean execute(TIntArrayList id2paramIndex) {
							double[] toProject = new double[id2paramIndex.size()];
							for (int k = 0; k < toProject.length; k++) {
								toProject[k] = Math.exp(sharpness*qPosteriors[id2paramIndex.get(k)]);
							}
							double sum=ArrayMath.sum(toProject);
							if (sum == 0) return true;
							assert(!Double.isNaN(sum));
							assert(!Double.isInfinite(sum));
							for (int k = 0; k < toProject.length; k++) {
								int ind=id2paramIndex.get(k);
								gradient[ind]=(toProject[k]/sum)*str;
								assert(!Double.isNaN(gradient[ind]));
								assert(!Double.isInfinite(gradient[ind]));
							}
							return true;
						}
					});
				}
			});
		}


		
		public String toString() {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < parameters.length; i++) {
				sb.append(parameters[i]+" ");
			}
			sb.append("\n");
			return sb.toString();
		}
		
	}

	
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException, IOException, IllegalArgumentException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
		PosCorpus c = new PosCorpus(args[0]);
		int  minOccurs = Integer.parseInt(args[1]);
		double str = Double.parseDouble(args[2]);
		
		PosHMMFinalState model = new PosHMMFinalState(c,c.getNrTags());
		L1SoftMaxEG l1lmax = new L1SoftMaxEG(c,model,minOccurs,str, 20);
		System.out.println("Going to use " + l1lmax.nrWordTypesToProject + " word types");
		System.out.println("Has " + l1lmax.numberOfParameters + "/" + (c.getNumberOfTokens()*c.getNrTags())+":"+(l1lmax.numberOfParameters*100/(c.getNumberOfTokens()*c.getNrTags()))+"%");
		model.initializeRandom(new Random(1), 1);
		EM learningWarmup = new EM(model);
		CompositeTrainStats stats = (CompositeTrainStats) CompositeTrainStats.buildTrainStats("");
		stats.addStats(new GlobalEMTimeCounter());
		stats.addStats(new LikelihoodStats());
		stats.addStats(new MemoryStats());
		stats.addStats(new AccuracyStats("2","10","predictions/",c.trainInstances));
		learningWarmup.em(10, stats);
		CorpusPR learning = new CorpusPR(model,l1lmax);
		stats = (CompositeTrainStats) CompositeTrainStats.buildTrainStats("");
		stats.addStats(new GlobalEMTimeCounter());
		stats.addStats(new LikelihoodStats());
		stats.addStats(new MemoryStats());
		stats.addStats(new AccuracyStats("2","10","predictions/",c.trainInstances));
		learning.em(20, stats);
		
	}
	
}
