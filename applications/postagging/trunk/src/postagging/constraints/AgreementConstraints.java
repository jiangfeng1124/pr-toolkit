package postagging.constraints;

import constraints.JointCorpusConstraints;
import model.AbstractSentenceDist;
import model.chain.ChainSentenceDist;
import model.chain.ForwardBackwardInference;
import model.chain.hmm.HMMSentenceDist;
import model.chain.hmmFinalState.HMMFinalStateSentenceDist;

public class AgreementConstraints implements JointCorpusConstraints{

	public void project(AbstractSentenceDist[][] posteriors) {
		for (int i = 0; i < posteriors[0].length; i++) {
			agreeConstraints(posteriors[0][i], posteriors[1][i]);
		}	
	}
	
	/**
	 * Return the changed posterior to agree. 
	 * q = 1/z \prod_i sqrt (p1^d-1(yi|x)p2^d-1(yi|x)) \prod_ij sqrt (p1^d-1(yi,yj|x)p2^d-1(yi,yj|x))
	 * Where d - Degree of the edge
	 * p1 - Model leftRigh
	 * p2 - Model rightLeft
	 * p(yi|x) - is the node potentials, i ranges from 1 to n
	 * p(yi,yj|x) - is the edge potentials  i ranges from 1 to n and j ranges from i+1 to n
	 * z - is the normalizer constant, obtainig by running a procedure similar to forward backward.
	 * We do this by creating a faque hmm sentence dist where we create special caches using the node potentials
	 * and the edge potentials and then run normal forward backward. Using the new posterior to
	 * update the counts
	 *  
	 * @param leftRight
	 * @param rightLeft
	 */
	public void agreeConstraints(AbstractSentenceDist leftRight, AbstractSentenceDist rightLeft){
		if(leftRight instanceof HMMFinalStateSentenceDist){
			System.out.println("Unknow instances to make agree");
			System.exit(-1);
		}else if(leftRight instanceof HMMSentenceDist){
			
			
			HMMSentenceDist lr = (HMMSentenceDist)leftRight;
			HMMSentenceDist rl = (HMMSentenceDist)rightLeft;
//			
//			System.out.println("Before Left Right using HMM");
//			lr.printStatePosteriors();
//			System.out.println("Before to Right Left using HMM");
//			rl.printStatePosteriors();
//			System.out.println("Before Left Right transition using HMM");
//			lr.printTransitionPosteriors();
//			System.out.println("Before Right Left transition using HMM");
//			rl.printTransitionPosteriors();
//			
//			System.out.println("");
//			System.out.println("");
//			System.out.println("");
//			System.out.println("");
//			System.out.println("");
			
			AgreementHMMSentenceDist faque = new AgreementHMMSentenceDist(lr,rl);
			faque.initSentenceDist();
			ForwardBackwardInference inference  = new ForwardBackwardInference((AgreementHMMSentenceDist)faque);
			
			inference.makeForwardTables();
			inference.makeBacwardTables();
			inference.makeStatePosteriors(faque);
			inference.makeTransitionPosteriors(faque);
			//Must normalize posterior since with this cliques there is no guarantee they are normalized
			int nrPosition = faque.getNumberOfPositions();
			int nrStates = faque.getNumberOfHiddenStates();
			for (int pos = 0; pos < nrPosition; pos++) {
				//Set transition posterior for that state
				double sum = 0;
				for (int state = 0; state < nrStates; state++) {
					sum += faque.getStatePosterior(pos, state);
				}
				if(sum == 0){
					System.out.println("Normalizer of state posterior is getting close to zero");
					System.exit(-1);
				}
				for (int state = 0; state < nrStates; state++) {
					faque.setStatePosterior(pos, state,faque.getStatePosterior(pos, state)/sum);
				}
				if(pos < nrPosition-1){
					sum = 0;
					for (int state = 0; state < nrStates; state++) {
						for (int prevState = 0; prevState < nrStates; prevState++) {
							sum += faque.getTransitionPosterior(pos, prevState, state);
						}
					}
					if(sum == 0){
						System.out.println("Normalizer of transition posterior is getting close to zero");
						System.exit(-1);
					}
					for (int state = 0; state < nrStates; state++) {
						for (int prevState = 0; prevState < nrStates; prevState++) {
							faque.setTransitionPosterior(pos, prevState, state,
									faque.getTransitionPosterior(pos, prevState, state)/sum);
						}
					}
				}
			}


			for (int pos = 0; pos < nrPosition; pos++) {
				for (int state = 0; state < nrStates; state++) {
					lr.setStatePosterior(pos, state, faque.getStatePosterior(pos, state));
					rl.setStatePosterior(pos, state, faque.getStatePosterior(faque.getNumberOfPositions()-pos-1, state));
					if(pos < nrPosition-1){					
						for (int prevState = 0; prevState < nrStates; prevState++) {
							lr.setTransitionPosterior(pos, prevState, state, 
									faque.getTransitionPosterior(pos, prevState, state));	
							//Note that the chain is going on reverse order so 
							//The prev state is now state on the faque
							rl.setTransitionPosterior(pos,prevState, state, 
									faque.getTransitionPosterior(faque.getNumberOfPositions()-pos-2, state,prevState));								
						}
					}
				}
			}
			
//			System.out.println("After Left Right using HMM");
//			lr.printStatePosteriors();
//			System.out.println("After to Right Left using HMM");
//			rl.printStatePosteriors();
//			
//			System.out.println("After Left Right transition using HMM");
//			lr.printTransitionPosteriors();
//			System.out.println("After Right Left transition using HMM");
//			rl.printTransitionPosteriors();
			
			//System.exit(-1);
			
		}else{
			System.out.println("Unknow instances to make agree");
			System.exit(-1);
		}	
	}
	
//	public void createSentenceDistribution(HMMFinalStateSentenceDist leftRight, HMMFinalStateSentenceDist rightLeft){
//		HMMFinalStateSentenceDist faque = new HMMFinalStateSentenceDist(leftRight.model,leftRight.instance,leftRight.nrHiddenStates);
//		
//	}
	
	class AgreementHMMSentenceDist extends HMMSentenceDist{

		HMMSentenceDist lr,rl;
		int nrStates;
		int sentenceSize;
		public double observationPosterior[][];
		public double transitionPosterior[][][];
		
		public double observationCache[][];
		public double transitionCache[][][];

		public double logLikelihood;

		protected AgreementHMMSentenceDist(){
			
		}
		
		public AgreementHMMSentenceDist(HMMSentenceDist lr, HMMSentenceDist rl){
			this.lr=lr;
			this.rl=rl;
			this.nrStates = lr.nrHiddenStates;
			this.sentenceSize=lr.getNumberOfPositions();
			observationPosterior = new double[sentenceSize][nrStates];
			transitionPosterior=new double[sentenceSize-1][nrStates][nrStates];
			
			observationCache = new double[sentenceSize][nrStates];
			transitionCache=new double[sentenceSize-1][nrStates][nrStates];
			
		}
		
		public int getNumberOfHiddenStates() {
			return nrStates;
		}
		
		public int getNumberOfPositions() {
			return sentenceSize;
		}

		
		public double getInitProb(int state) {
			 return 1;
		}

		/**
		 * Maps the edges if we have 2 edges and 3 states the mapping is
		 * edge starting at zero goes to edge starting at 1 and edge starting at one goes to edge starting at
		 * zero
		 */
		public double getTransitionProbability(int position,int prevState, int state) {		
			return transitionCache[position][prevState][state];
			
		}
		
		@Override
		public double getObservationProbability(int position, int state) {
			return observationCache[position][state];
		}

		@Override
		public double getTransitionPosterior(int position, int prevState, int state) {
			return transitionPosterior[position][prevState][state];
		}

		@Override
		public void setStatePosterior(int position, int state, double prob) {
				observationPosterior[position][state] = prob;
		}
		@Override
		public double getStatePosterior(int position, int state) {
				return observationPosterior[position][state];
		}

		@Override
		public void setTransitionPosterior(int position, int prevState, int state,
				double prob) {
				transitionPosterior[position][prevState][state] = prob;
		}


		@Override
		public int getWordId(int position) {
				return lr.instance.getWordId(position);
		}

		@Override
		public double getLogLikelihood() {
			return logLikelihood;
		}
		
		@Override
		/**
		 * Init caches with the changes factores
		 * We have that q can be viwed as:
		 * \prod_i p1(yi)^(d-1)/2*p2(yi)^(d-1)/2 *p1(yi,yj)*p2(yi,yj)
		 * where d is the degree of the edge and j = i+1
		 *  
		 */
		public void initSentenceDist() {
			int nrPosition = getNumberOfPositions();
			int nrStates = getNumberOfHiddenStates();
				
			for (int pos = 0; pos < nrPosition; pos++) {
				for (int state = 0; state < nrStates; state++) {				
					if(pos == 0 || pos == nrPosition-1){
						observationCache[pos][state]=1;
					}else{
						double number = Math.sqrt(lr.getStatePosterior(pos, state)*rl.getStatePosterior(sentenceSize-1-pos, state));
						if(number < 1.E-100){
							observationCache[pos][state]= 1.E-100;
						}else{
							observationCache[pos][state]= number;
						}
					}
					if(pos < nrPosition-1){
						//Note that on rl we need to change the prevState with State since we are looking from last
						//position to initial position
						for (int prevState = 0; prevState < nrStates; prevState++) {
							double number = 	Math.sqrt(lr.getTransitionPosterior(pos, prevState, state)*
									rl.getTransitionPosterior(sentenceSize-2-pos,state, prevState));
							if(number < 1.E-100){
								transitionCache[pos][prevState][state]=1.E-100;
							}else{
								transitionCache[pos][prevState][state]= number;
							}
						}
					}	
				}
			}	
			
//			System.out.println("Observation Cache");
//			util.Printing.printDoubleArray(observationCache, null, null, "Observation Cache");
//			for (int pos = 0; pos < nrPosition-1; pos++) {
//				util.Printing.printDoubleArray(transitionCache[pos], null, null, "Transition Cache pos " + pos);
//			}
		}	
		
		@Override
		public String toString() {
			return "AGREEMENT SENTENCE DIST \n\n\n LR: \n"
			+ lr.statePosteriorToString() + " \nRL:\n" + rl.statePosteriorToString();
		}
	}
	
	
}
