package postagging.evaluation;

import java.io.IOException;

import postagging.data.PosCorpus;


public class Evaluator {
	
	
	/**
	 * Calculates the accuracy:
	 *  corpus wise (total of correct/total)
	 *  sentence wise (total of correct in a sentence/sentence total)/nr Sentences
	 *  sentence accurancy (how many sentences are completly correct)
	 * @param predicted
	 * @param gold
	 * @return
	 */
	
	public static Evaluation eval(int[][] predicted, int[][] golds){
		return eval(predicted.length, predicted, golds);
	}
	
	public static Evaluation eval(int maxSentences, int[][] predicted, int[][] golds){
		Evaluation eval = new Evaluation();
		int corpusNrCorrect=0;
		int corpusTotal=0;
		int totalSentenceNrCorrect=0;
		double avgSentenceNrCorrect=0;
		for(int sentenceNr = 0; (sentenceNr < predicted.length) && (sentenceNr < maxSentences); sentenceNr++){
			int[] predictedSentence = predicted[sentenceNr];
			int[] goldSentence = golds[sentenceNr];
			boolean allTrue = true;
			int sentenceNrCorrect=0;
			for(int pos = 0; pos < predictedSentence.length; pos++){
				int pred = predictedSentence[pos];
				int gold = goldSentence[pos];
				if(pred==gold){
					corpusNrCorrect++;
					sentenceNrCorrect++;
				}else{
					allTrue = false;
				}
				corpusTotal++;
			}
			if(allTrue){
				totalSentenceNrCorrect++;
			}
			avgSentenceNrCorrect+=sentenceNrCorrect*1.0/predictedSentence.length;
		}
		eval._corpusAccurancy=corpusNrCorrect*1.0/corpusTotal;
		eval._sentenceAverageAccurancy=avgSentenceNrCorrect/predicted.length;
		eval._sentenceAccurancy=totalSentenceNrCorrect*1.0/predicted.length;
		return eval;
	}
	
	/**
	 * Calculates accurancy for words occuring different number
	 * of times see bins.
	 * Also the number of errors for the particular "Unk" word in case it exists
	 * @param maxSentences
	 * @param predicted
	 * @param golds
	 * @return
	 */
	public static int[] bins = {1,5, 10, 50,100,200,Integer.MAX_VALUE};
	public static EvaluationPerBins evaluatePerOccurences(PosCorpus c, int maxSentences, int[][] words,int[][] predicted, int[][] golds){
		//Bins +1 is for the words unk
		Evaluation[] results = new Evaluation[bins.length+1];	
		int[] corpusNrCorrect=new int[bins.length+1];
		int[] corpusTotal=new int[bins.length+1];
		int nrUnk =0;
		int unkId = c.wordAlphabet.feat2index.get("unk");
		int readSentences = 0;
		for(int sentenceNr = 0; (sentenceNr < predicted.length) && (sentenceNr < maxSentences); sentenceNr++){
			int[] predictedSentence = predicted[sentenceNr];
			int[] goldSentence = golds[sentenceNr];
			int[] wordSentence = words[sentenceNr];
			readSentences++;
			for(int pos = 0; pos < predictedSentence.length; pos++){
				int pred = predictedSentence[pos];
				int gold = goldSentence[pos];
				int word = wordSentence[pos];
				int wordBin= -1;
				int nrWordsOccur = c.wordAlphabet.getCounts(word);
				//Check word bin
				if(word != unkId){
					for(int i = bins.length-1; i > -1 ; i--){
						//	System.out.println(i);
						if(nrWordsOccur <= bins[i]) {
						wordBin = i;
						}
					}
				}
				
				if (wordBin == -1){
					wordBin = bins.length;
				}
				
				if(pred==gold){
					corpusNrCorrect[wordBin]++;
				}
				corpusTotal[wordBin]++;
			}
		}
		for(int i = 0; i < bins.length+1; i++){
			results[i]= new Evaluation();
			results[i]._corpusAccurancy = corpusNrCorrect[i]*1.0/corpusTotal[i];
			results[i]._totalWords=corpusTotal[i];
		}
		System.out.println("Ignored " + nrUnk + " unknown");
		System.out.println("Read " + readSentences + " sentences");
		EvaluationPerBins eval = new EvaluationPerBins();
		eval.evaluations = results;
		eval.bins = bins;
		return eval;
	}
	
//	public static ArrayList<Pair<Integer, Integer>> errorsByWordType(PosCorpus c, int maxSentences, int[][] predicted, int[][] golds, int[][] wordsSentences){
//		TIntObjectHashMap<Pair<Integer, Integer>> wordCounts = new TIntObjectHashMap<Pair<Integer, Integer>>();
//		for(int sentenceNr = 0; (sentenceNr < predicted.length) && (sentenceNr < maxSentences); sentenceNr++){
//			int[] predictedSentence = predicted[sentenceNr];
//			int[] goldSentence = golds[sentenceNr];
//			int[] words = wordsSentences[sentenceNr];
//			for(int pos = 0; pos < predictedSentence.length; pos++){
//				int word = words[pos];
//				int pred = predictedSentence[pos];
//				int gold = goldSentence[pos];
//				if(c.wordAlphabet.getCounts(word) < 10) continue;
//				if(pred!=gold){
//					if(!wordCounts.contains(word)){
//						wordCounts.put(word,new Pair<Integer, Integer>(word,0));
//					}
//					Pair<Integer,Integer> pair = wordCounts.get(word);
//					pair.setSecond(pair.second()+1);
//				}
//			}
//			
//		}
//		return CollectionsUtils.convertHashMapToSortedList(wordCounts);
//	}
	
	public static class EvaluationPerBins{
		Evaluation[] evaluations;
		int bins[];
		
		public String toString(){
			StringBuffer sb = new StringBuffer();
			sb.append("\n");
			for (int i = 0; i < bins.length; i++) {
				sb.append("Bin: " + bins[i] + " " + evaluations[i].toString()+"\n");
			}
			sb.append("Unk: " + evaluations[bins.length].toString()+"\n");
			return sb.toString();
		}
		
	}
	
	public static class Evaluation{
		double _corpusAccurancy;
		double _sentenceAverageAccurancy;
		double _sentenceAccurancy;
		public double _totalWords;
		
		public String toString(){
			return "Corpus: " + _corpusAccurancy*100 + " Sent avg " + _sentenceAverageAccurancy*100 + " sent " + _sentenceAccurancy*100;
		}
	}
	
	public static void main(String[] args) throws IOException {
//		PosCorpus c = new PosCorpus(args[0],Integer.MAX_VALUE,Integer.MAX_VALUE);
//		int[][] predictedUnsRT =  c.readPredictions(args[1]);
//		ArrayList<Pair<Integer, Integer>> res = errorsByWordType(c,Integer.MAX_VALUE,predictedUnsRT,c.getTrainingTagSentences(),c.getTrainingWordSentences());
//		for(int i = 0; i < 100; i++){
//			Pair<Integer,Integer> p = res.get(i);
//			int id = p.first();
//			System.out.println(id + " - " + c.wordAlphabet.getCounts(id) + " - "+c.wordAlphabet.index2feat.get(p.first()) + " - " + p.second());
//		}
//		System.out.println(c.wordAlphabet.size());
	}
	
}
