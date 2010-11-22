package postagging.model;




import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Random;

import optimization.gradientBasedMethods.Optimizer;
import optimization.gradientBasedMethods.stats.OptimizerStats;
import optimization.linesearch.InterpolationPickFirstStep;
import optimization.linesearch.LineSearchMethod;
import optimization.linesearch.WolfRuleLineSearch;
import optimization.stopCriteria.CompositeStopingCriteria;
import optimization.stopCriteria.NormalizedGradientL2Norm;
import optimization.stopCriteria.NormalizedValueDifference;
import optimization.stopCriteria.StopingCriteria;

import learning.EM;
import learning.stats.CompositeTrainStats;
import learning.stats.LikelihoodStats;
import model.chain.hmmFinalState.HMMFinalState;
import model.distribution.trainer.AbstractMultinomialTrainer;
import model.distribution.trainer.MultinomialFeatureFunction;
import model.distribution.trainer.MultinomialMaxEntTrainer;
import model.distribution.trainer.ObservationMultinomialFeatureFunction;
import model.distribution.trainer.TableNormalizerMultinomialTrainer;
import model.distribution.trainer.TransitionMultinomialFeatureFunction;
import postagging.data.PosCorpus;

public class PosHMMFinalState extends HMMFinalState{
	
	
	public PosHMMFinalState(PosCorpus c, int nrTags,	
			AbstractMultinomialTrainer observationTrainer,
			AbstractMultinomialTrainer transitionsTrainer,
			AbstractMultinomialTrainer initTrainer) {
		super(c,c.getNrWordTypes(),nrTags,observationTrainer,transitionsTrainer,initTrainer);
	}
	
	public PosHMMFinalState(PosCorpus c, int nrTags) {
		super(c,c.getNrWordTypes(),nrTags);
	}
	
	public String getName(){
		return "POSTAG HMM Final State";
	}
	
	
	
	///// DEBUG METHODS
//	public static MultinomialMaxEntTrainer initializeObsMultinomialMaxEnt(PosCorpus c, String featureFile ,int nrTags) throws IllegalArgumentException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException{
//		ObservationMultinomialFeatureFunction fxy = new ObservationMultinomialFeatureFunction(c, featureFile);
//		ArrayList<MultinomialFeatureFunction> fxys = new ArrayList<MultinomialFeatureFunction>();
//		ArrayList<LineSearchMethod> lss = new ArrayList<LineSearchMethod>();
//		ArrayList<StopingCriteria> scs = new ArrayList<StopingCriteria>();
//		ArrayList<OptimizerStats> oss = new ArrayList<OptimizerStats>();
//		ArrayList<Optimizer> opt = new ArrayList<Optimizer>();
//		
//		for (int i = 0; i < nrTags; i++) {
//			fxys.add(fxy);
//			WolfRuleLineSearch wolfe = 
//				new WolfRuleLineSearch(new InterpolationPickFirstStep(1),0.001,0.9,100);
//			wolfe.setDebugLevel(2);
//			lss.add(wolfe);
//			StopingCriteria stopGrad = new NormalizedGradientL2Norm(0.1);
//			StopingCriteria stopValue = new NormalizedValueDifference(0.1);
//			CompositeStopingCriteria stop = new CompositeStopingCriteria();
//			stop.add(stopGrad);
//			stop.add(stopValue);
//			scs.add(stop);
//			optimization.gradientBasedMethods.LBFGS optimizer = 
//				new optimization.gradientBasedMethods.LBFGS(wolfe,30);
//			optimizer.setMaxIterations(1000);
//			opt.add(optimizer);
//			optimization.gradientBasedMethods.stats.OptimizerStats optStats = new OptimizerStats();
//			oss.add(optStats);
//		}
//
//		return new MultinomialMaxEntTrainer(nrTags,
//				1,fxys,lss,opt,oss,scs,false,false);
//	}
	
//	public static MultinomialMaxEntTrainer 
//	initializeTransitionsMultinomialMaxEnt(PosCorpus c, int nrTags) throws IllegalArgumentException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException{
//		TransitionMultinomialFeatureFunction fxy = 
//			new TransitionMultinomialFeatureFunction(c, nrTags);
//		ArrayList<MultinomialFeatureFunction> fxys = new ArrayList<MultinomialFeatureFunction>();
//		ArrayList<LineSearchMethod> lss = new ArrayList<LineSearchMethod>();
//		ArrayList<StopingCriteria> scs = new ArrayList<StopingCriteria>();
//		ArrayList<OptimizerStats> oss = new ArrayList<OptimizerStats>();
//		ArrayList<Optimizer> opt = new ArrayList<Optimizer>();
//		
//		for (int i = 0; i < nrTags; i++) {
//			fxys.add(fxy);
//			// perform gradient descent
//			WolfRuleLineSearch wolfe = 
//				new WolfRuleLineSearch(new InterpolationPickFirstStep(1),0.001,0.9,100);
//			wolfe.setDebugLevel(2);
//			lss.add(wolfe);
//			StopingCriteria stopGrad = new NormalizedGradientL2Norm(0.1);
//			StopingCriteria stopValue = new NormalizedValueDifference(0.1);
//			CompositeStopingCriteria stop = new CompositeStopingCriteria();
//			stop.add(stopGrad);
//			stop.add(stopValue);
//			scs.add(stop);
//			optimization.gradientBasedMethods.LBFGS optimizer = 
//				new optimization.gradientBasedMethods.LBFGS(wolfe,30);
//			optimizer.setMaxIterations(1000);
//			opt.add(optimizer);
//			optimization.gradientBasedMethods.stats.OptimizerStats optStats = new OptimizerStats();
//			oss.add(optStats);
//		}
//
//		return new MultinomialMaxEntTrainer(nrTags,
//				1,fxys,lss,opt,oss,scs,false,false);
//	}

	
	public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException, IOException, IllegalArgumentException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {

		PosCorpus c = new PosCorpus(args[0],0,Integer.MAX_VALUE,100);
		
//		AbstractMultinomialTrainer oT = new TableNormalizerMultinomialTrainer();
//		AbstractMultinomialTrainer tT = new TableNormalizerMultinomialTrainer();
//		AbstractMultinomialTrainer iT = new TableNormalizerMultinomialTrainer();
		
//		AbstractMultinomialTrainer oT = new MultinomialVariationalBayesTrainer(0.1);
//		AbstractMultinomialTrainer tT = new MultinomialVariationalBayesTrainer(0.1);
//		AbstractMultinomialTrainer iT = new MultinomialVariationalBayesTrainer(0.1);

		
		//Initializing the max-ent
		
		
//		AbstractMultinomialTrainer oT = initializeObsMultinomialMaxEnt(c,args[1],c.getNrTags());
//		AbstractMultinomialTrainer tT = initializeTransitionsMultinomialMaxEnt(c, c.getNrTags()+1);
//		AbstractMultinomialTrainer iT = new TableNormalizerMultinomialTrainer();
//
//		
//		PosHMMFinalState hmm = new PosHMMFinalState(c, c.getNrTags(),oT,tT,iT);
//		hmm.initializeRandom(new Random(1), 1);
//		EM em = new EM(hmm);
//		CompositeTrainStats stats = new CompositeTrainStats();
//		stats.addStats(new LikelihoodStats());
//		em.em(5, stats);		
		
	}
	
	
	
	
	
	
}
