package model.chain.hmm;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

import util.ArrayPrinting;

import model.AbstractCountTable;
import model.AbstractModel;
import model.AbstractSentenceDist;
import model.chain.ChainDecoder;
import model.distribution.AbstractMultinomial;
import model.distribution.Multinomial;
import model.distribution.trainer.AbstractMultinomialTrainer;
import model.distribution.trainer.TableNormalizerMultinomialTrainer;
import data.Corpus;
import data.InstanceList;
import data.WordInstance;
import decoderStats.AbstractDecoderStats;

/** Implements an HMM without Final State.
 * 
 * @author javg
 *
 */
public  class HMM extends AbstractModel{

	

	/**
	 * Model Parameters
	 */
	public AbstractMultinomial initialProbabilities;
	public AbstractMultinomial transitionProbabilities;
	public AbstractMultinomial observationProbabilities;
	//Methods to update the multinomials from the counts
	public AbstractMultinomialTrainer observationTrainer;
	public AbstractMultinomialTrainer transitionsTrainer;
	public AbstractMultinomialTrainer initTrainer;
	
	protected int nrStates;
	protected int nrWordTypes;
	
	protected boolean initialized;

	public Corpus corpus;
	
	
	
	protected 	HMM(){
		
	}
	
	/**
	 * Initialize multinomial tables
	 * @param nrWordTypes
	 * @param nrHiddenStates
	 */
	public HMM(Corpus c,int nrWordTypes, int nrHiddenStates,
			AbstractMultinomialTrainer observationTrainer,
			AbstractMultinomialTrainer transitionsTrainer,
			AbstractMultinomialTrainer initTrainer){
		corpus = c;
		this.nrWordTypes = nrWordTypes;
		//Note does not have to be the same as the number of true tags
		this.nrStates = nrHiddenStates;
		initialProbabilities = new Multinomial(1,nrStates);
		//Model P(S_t|S_t-1)
		transitionProbabilities = new Multinomial(nrStates,nrStates);
		//Models p(obs|state)
		observationProbabilities = new Multinomial(nrStates,nrWordTypes);
		this.observationTrainer = observationTrainer;
		this.transitionsTrainer = transitionsTrainer;
		this.initTrainer = initTrainer;
	}
	
	public HMM(Corpus c,int nrWordTypes, int nrHiddenStates){
		this(c, nrWordTypes, nrHiddenStates,
				new TableNormalizerMultinomialTrainer(), 
				new TableNormalizerMultinomialTrainer(), 
				new TableNormalizerMultinomialTrainer());
	}

	public int getNrRealStates(){
		return nrStates;
	}

	public int getNrStates(){
		return nrStates;
	}
	
	@Override
	public void addToCounts(AbstractSentenceDist sd, AbstractCountTable counts) {
		HMMSentenceDist dist = (HMMSentenceDist)sd;
		dist.updateInitCounts((HMMCountTable) counts);
		dist.updateTransitionCounts((HMMCountTable) counts);
		dist.updateObservationCounts((HMMCountTable) counts);
	}

	@Override
	public void computePosteriors(AbstractSentenceDist dist) {
		HMMSentenceDist d = (HMMSentenceDist)dist;
		d.makeInference();
	}
	
	@Override
	public AbstractSentenceDist[] getSentenceDists() {
		return getSentenceDists(corpus.trainInstances);
	}

	
	@Override
	public AbstractSentenceDist[] getSentenceDists(InstanceList inst) {
		HMMSentenceDist[] sentences = new HMMSentenceDist[inst.instanceList.size()];
		for (int i = 0; i < sentences.length; i++) {
			sentences[i] = getSentenceDist(this, inst.instanceList.get(i));
		}
		return sentences;
	}
	
	public HMMSentenceDist getSentenceDist(HMM model, WordInstance inst){
		return new HMMSentenceDist(model,inst,nrStates);	
	}
	
	@Override
	public AbstractCountTable getCountTable() {
		return new HMMCountTable(nrWordTypes,nrStates);
	}

	/**
	 * Checks if the counts tables make sense:
	 * Initial Counts - Should sum to the number of sentences
	 * Transition Counts - Should sum to the number of tokens - number of sentences
	 * State Counts - Should sum to the number of tokens
	 * @param counts
	 */
	public void checkCountsTable(AbstractCountTable counts){
		System.out.println("DEBUG:::Checking if counts are correct");
		checkInitialCounts(((HMMCountTable)counts).initialCounts);
		checkObservationCounts(((HMMCountTable)counts).observationCounts);
		checkTransitionCounts(((HMMCountTable)counts).transitionCounts);
	}
	
	public void checkObservationCounts(Multinomial table){
		double sum = 0;
		for(int i = 0; i < getNrRealStates(); i++){
			for(int j = 0; j < corpus.getNrWordTypes(); j++){
				sum += table.getCounts(i, j);
			}
		}
		if(Math.abs(sum - corpus.getNumberOfTokens()) > 1.E-5){
			System.out.println("Transition counts do not sum to the number of tokens");
			throw new RuntimeException();
		}
	}
	
	//InitialCounts should sum to the number of sentences
	public void checkInitialCounts(Multinomial table){
		double sum = 0;
		for(int i = 0; i < getNrRealStates(); i++){
			sum += table.getCounts(0, i);
		}
		if(Math.abs(sum - corpus.getNrOfTrainingSentences()) > 1.E-5){
			System.out.println("Initial counts do not sum to the number of sentences got: " + sum + " truth: " + corpus.getNrOfTrainingSentences());
			throw new RuntimeException();
		}
	}
	
	/**
	 * Should sum to the number of tokens minus the number of sentences
	 * @param table
	 */
	public void checkTransitionCounts(Multinomial table){
		double sum = 0;
		for(int i = 0; i < nrStates; i++){
			for(int j = 0; j < nrStates; j++){
				sum += table.getCounts(i, j);
			}
		}
		if(Math.abs(sum - corpus.getNumberOfTokens() + corpus.getNrOfTrainingSentences()) > 1.E-5){
			System.out.println("Transition counts do not sum to the number of tokens minus number of sentences got: "+
					sum + " true: " + (corpus.getNumberOfTokens() - corpus.getNrOfTrainingSentences()));
			throw new RuntimeException();
		}
	}
	
	public void updateParameters(AbstractCountTable counts) {
		observationTrainer.update(((HMMCountTable)counts).observationCounts,observationProbabilities);
		transitionsTrainer.update(((HMMCountTable)counts).transitionCounts,transitionProbabilities);
		initTrainer.update(((HMMCountTable)counts).initialCounts,initialProbabilities);		
	}
	
	


	public void initializeRandom(Random r,double jitter){
		initialProbabilities.initializeRandom(r,jitter);
		transitionProbabilities.initializeRandom(r,jitter);
		observationProbabilities.initializeRandom(r,jitter);
		initialized = true;
//		printModelParameters();
//		System.exit(-1);
	}

	
	/**
	 * Initialize from data
	 * @param smoothing
	 * @param nrSentences
	 * @param nrHiddenStates
	 * @param wordSentences
	 * @param tagSentences
	 */
	public void initializeSupervised(double smoothing, int nrHiddenStates, InstanceList list){
		System.out.println("Initializing supervised");
		if(getNrRealStates() != nrHiddenStates){
			System.out.println("Number of hidden states from supervised data does not match HMM number of hidden states");
			System.exit(-1);
		}
		HMMCountTable count = (HMMCountTable) getCountTable();
		count.fill(smoothing);
		for (WordInstance inst : list.instanceList) {
			int len = inst.getNrWords();
			count.initialCounts.addCounts(0, inst.getTagId(0), 1);
			count.observationCounts.addCounts(inst.getTagId(0),inst.getWordId(0), 1);
			for(int pos = 1; pos < len; pos++){
				count.transitionCounts.addCounts(inst.getTagId(pos-1),inst.getTagId(pos), 1);
				count.observationCounts.addCounts(inst.getTagId(pos),inst.getWordId(pos), 1);
			}
		}
		updateParameters(count);
		initialized = true;
	//	printModelParameters();
		System.out.println("Finished supervised");
	}
	
	
	public void initFromPosteriors(HMMSentenceDist[] sd){
		HMMCountTable counts = (HMMCountTable)getCountTable();
		for (int i = 0; i < sd.length; i++) {
			addToCounts(sd[i], counts);
		}
		updateParameters(counts);
	}
	
	
	public AbstractSentenceDist[] getRandomPosteriors(Random r,double jitter){
		AbstractSentenceDist[] sentences = getSentenceDists(corpus.trainInstances);
		for (int i = 0; i < sentences.length; i++) {
			HMMSentenceDist sd = (HMMSentenceDist) sentences[i];
			sd.createRandomPosteriors();
		}
		
		return sentences;
	}
	
	public void printModelParameters(){
		initialProbabilities.print("Initial Parameters",null,null);
		transitionProbabilities.print("Transition Parameters",null,null);
		//observationProbabilities.print("Observatiob Parameters",null,null);
	}
	
	public String params2string(){
		return 
		initialProbabilities.toString("Initial Parameters", null, null) + 
		"\n"+
		transitionProbabilities.toString("Transition Parameters",null,null);
	}
	
	
	
	public int[][] decode(ChainDecoder decoder, InstanceList list, AbstractDecoderStats stats){
		int[][] output = decoder.decodeSet(this, list,stats);
		proceddDecodingOutput(output);
		return output;
	}
	
	public void proceddDecodingOutput(int[][] output){
	}
	
	
	public void printStamp(PrintStream file){
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        java.util.Date date = new Date();
        System.out.println("Current Date Time : " + dateFormat.format(date));
		
	}
	
	public void saveProperties(PrintStream file) throws IOException{
        Properties props = new Properties(); 
        props.put("nrStates", nrStates);
        props.put("nrWordTypes", nrWordTypes);
		
	}
	
	public void saveModel(String directory) throws IOException{
		if(!util.FileSystem.createDir(directory)){
			System.out.println("Failed to create directory not saving");
			return;
		}
		corpus.saveDescription(directory);
		try {
			PrintStream file = new PrintStream(new FileOutputStream(directory+ "stamp"));
			printStamp(file);
		} catch (FileNotFoundException e) {
			System.out.println("Could not save model");
			System.exit(-1);
		}
		PrintStream properties = new PrintStream(new FileOutputStream(directory+ "properties"));
		saveProperties(properties);
		initialProbabilities.saveTable(directory+"/initProb");
		observationProbabilities.saveTable(directory+"/obsProb");
		transitionProbabilities.saveTable(directory+"/transProb");
	}
	
	
}
