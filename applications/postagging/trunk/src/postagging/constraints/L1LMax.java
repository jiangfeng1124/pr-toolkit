package postagging.constraints;

import constraints.CorpusConstraints;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntObjectHashMap;

import optimization.gradientBasedMethods.ProjectedGradientDescent;
import optimization.gradientBasedMethods.ProjectedObjective;
import optimization.gradientBasedMethods.stats.ProjectedOptimizerStats;
import optimization.linesearch.ArmijoLineSearchMinimizationAlongProjectionArc;
import optimization.linesearch.LineSearchMethod;
import optimization.linesearch.NonNewtonInterpolationPickFirstStep;
import optimization.projections.SimplexProjection;
import optimization.stopCriteria.CompositeStopingCriteria;
import optimization.stopCriteria.NormalizedProjectedGradientL2Norm;
import optimization.stopCriteria.NormalizedValueDifference;
import optimization.stopCriteria.ProjectedGradientL2Norm;
import optimization.stopCriteria.StopingCriteria;
import optimization.stopCriteria.ValueDifference;
import postagging.data.PosCorpus;
import postagging.learning.stats.AccuracyStats;
import postagging.model.PosHMMFinalState;
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
import model.chain.hmm.HMMCountTable;
import model.chain.hmm.HMM;
import model.chain.hmm.HMMSentenceDist;


/**
 *
 * 
 * @author javg
 *
 */
public class L1LMax implements CorpusConstraints{

	
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
	/**
	 * 
	 * @param minOccurences - Min occurrences of a word so that it will be constraint
	 */
	public L1LMax(PosCorpus c, HMM model,int minOccurences,double str){
		MemoryTracker mem = new MemoryTracker();
		mem.start();
		this.str = str;
		this.c = c;
		this.model = model;
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
	
	public void project(AbstractCountTable counts, AbstractSentenceDist[] posteriors, TrainStats trainStats, CorpusPR pr) {
		MemoryTracker mem  = new MemoryTracker();
		mem.start();
		
		
		//Make a copy of the original parameters:
		//Initialize the gradient and the function values and the original posteriors cache
		trainStats.eStepStart(model, pr);
		ProjectedObjective objective = new L1LMaxObjective(model,posteriors,numberOfParameters,str,parameters);
		mem.finish();
		System.out.println("After creating objective:" + mem.print());
		//System.out.println("Parameters before optimization:\n" + objective.toString());
		LineSearchMethod ls = new ArmijoLineSearchMinimizationAlongProjectionArc(new NonNewtonInterpolationPickFirstStep(initialStep));
		ProjectedGradientDescent optimizer = new ProjectedGradientDescent(ls);
		mem.finish();
		System.out.println("After creating projection and line search:" + mem.print());
//		LineSearchMethod ls = new NonMonotoneArmijoLineSearchMinimizationAlongProjectionArc(new InterpolationPickFirstStep(40));		
//		NonMonotoneSpectralProjectedGradient optimizer = new NonMonotoneSpectralProjectedGradient(ls);
		
		ProjectedOptimizerStats stats = new ProjectedOptimizerStats();
		StopingCriteria stopGrad = new NormalizedProjectedGradientL2Norm(0.001);
		StopingCriteria stopValue = new NormalizedValueDifference(0.0001);
		CompositeStopingCriteria stop = new CompositeStopingCriteria();
		stop.add(stopGrad);
		stop.add(stopValue);
		optimizer.setMaxIterations(40);
		

		boolean succed = optimizer.optimize(objective, stats,stop);
		mem.finish();
		
		System.out.println("After  optimization:" + mem.print());
		System.out.println("Suceess " + succed + "/n"+stats.prettyPrint(1));
		
		

		//System.out.println("Parameters after optimization:\n" + objective.toString());
		parameters = objective.getParameters();
		initialStep = optimizer.getCurrentStep();
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
						posterior.observationCache[sentecePosition][hs] *= Math.exp(-parameter);
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
	public class L1LMaxObjective extends ProjectedObjective{

		//Size of the simplex
		double scale;
		SimplexProjection projection;
		

		AbstractSentenceDist[] sentencesDists;
		
		
		PosCorpus corpus;
		
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
				double[] parameters){
			this.scale = scale;
			projection = new SimplexProjection(scale);
			gradient = new double[nrParameters];
			this.parameters = parameters;
			
			//Debug see if the initial parameters are on the set they should always be...
//			double[] projectedParameters = projectPoint(parameters);
//			for (int i = 0; i < parameters.length; i++) {
//				if(!MathUtils.almost(projectedParameters[i], parameters[i])){
//					System.out.println("Parameters not the same " + i);
//					System.out.println("original " + parameters[i] + " projectedParameters " + projectedParameters[i] );
//					System.out.println("sum " + MathUtils.sum(parameters));
////					System.out.println(Printing.doubleArrayToString(parameters, null, " original "));
////					System.out.println(Printing.doubleArrayToString(projectedParameters, null, " original "));
//					System.exit(-1);
//				}
//			}
			
			//System.out.println("Parameters:\n"+toString());
			sentencesDists = sentenceDists;		
			
			
			for(int sentenceNr = 0; sentenceNr <sentencesDists.length; sentenceNr++){
				//For each position in the sentence
				HMMSentenceDist posterior = (HMMSentenceDist)sentencesDists[sentenceNr];
				//Create the required data structures
				posterior.initSentenceDist();
				model.computePosteriors(posterior);
				logLikelihood += posterior.logLikelihood;
				//Update the gradient
				for(int sentecePosition= 0; sentecePosition < posterior.instance.words.length; sentecePosition++){
					int wt = posterior.instance.words[sentecePosition];
					if(projectionMapping.contains(wt)){
						for(int hs = 0; hs < nrHiddenStates; hs++){
							gradient[getParameterIndex(sentenceNr,sentecePosition,hs)] = 
							-posterior.observationPosterior[sentecePosition][hs];
						}
					}
				}
				posterior.clearCaches();
				posterior.clearPosteriors();
			}
			
			
		//	System.out.println(util.Printing.doubleArrayToString(gradient, null, "gradient"));
		}
	
		public void setParameters(double[] params) {
			super.setParameters(params);
			updateFunction();
		//	printParameters();
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
		//	long initTime = System.currentTimeMillis();
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
							double parameter = getParameter(getParameterIndex(sentenceNr,sentecePosition,hs));
							sd.observationCache[sentecePosition][hs] *=Math.exp(-parameter);
						}
					}
				}
				
				model.computePosteriors(sd);
				logLikelihood += sd.logLikelihood;
				//Update the gradient
				for(int sentecePosition= 0; sentecePosition < words.length; sentecePosition++){
					int wt = words[sentecePosition];
					if(projectionMapping.contains(wt)){
						for(int hs = 0; hs < nrHiddenStates; hs++){
							gradient[getParameterIndex(sentenceNr,sentecePosition,hs)] = 
							-sd.observationPosterior[sentecePosition][hs];
						}
					}
				}
				sd.clearCaches();
				sd.clearPosteriors();
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


		
		public double[] projectPoint2(double[] point) {
			return point;
		}
		
		/**
		 * Need to project individual parts of the point.
		 * We want to project all instance of a given
		 * word type, pos tag.
		 */
		double[] newPoint  = new double[numberOfParameters];
		public double[] projectPoint(double[] point) {
			//long initTime = System.currentTimeMillis();		
			int[] wordTypeKeys = projectionMapping.keys();	
			for (int i = 0; i < wordTypeKeys.length; i++) {
				TIntObjectHashMap<TIntArrayList> tagsMapping = projectionMapping.get(wordTypeKeys[i]);
	
				int[] tagKeys = tagsMapping.keys();
				for (int j = 0; j < tagKeys.length; j++) {
					TIntArrayList instances = tagsMapping.get(tagKeys[j]);
					//DEBUG
//					System.out.println("printing word " + c.wordAlphabet.index2feat.get(wordTypeKeys[i])
//							+ " with "  + instances.size() + " instances "+
//							 " true as " + c.getWordTypeCounts(wordTypeKeys[i]));
					
					double[] toProject = new double[instances.size()];
					
					for (int k = 0; k < toProject.length; k++) {
					//	System.out.print(instances.get(k) + " ");
						toProject[k] = point[instances.get(k)];
					}
//					System.out.println();
					//debug
					//System.out.println(Printing.doubleArrayToString(toProject, null, "before"));
					projection.project(toProject);
					//System.out.println(Printing.doubleArrayToString(toProject, null, "after"));
					for (int k = 0; k < toProject.length; k++) {
						newPoint[instances.get(k)]=toProject[k];
					}
				}
			}
//			long endTime = System.currentTimeMillis();
//			System.out.println("Projecting took: "+ util.Printing.formatTime(endTime-initTime));
			return newPoint;
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

	
	
	public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException, IOException, IllegalArgumentException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
		PosCorpus c = new PosCorpus(args[0]);
		int  minOccurs = Integer.parseInt(args[1]);
		double str = Double.parseDouble(args[2]);
		
		PosHMMFinalState model = new PosHMMFinalState(c,c.getNrTags());
		L1LMax l1lmax = new L1LMax(c,model,minOccurs,str);
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
