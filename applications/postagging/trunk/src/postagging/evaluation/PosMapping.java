package postagging.evaluation;


import gnu.trove.TIntIntHashMap;
import postagging.data.PosCorpus;

public class PosMapping {

	/**
	 *Maps each hidden state to the post-tag is co-occurs more.
	 *
	 *For each possible hidden state count how many times it co-occurs with 
	 *each true label. Assign the top label.
	 *This is many to one since many hidden states may map to the same tag, and some tags
	 *may not have any hidden state assign to it.
	 *
	 * @param hiddenStatesSize
	 * @param hiddenStates
	 * @param labelSetSize
	 * @param trueLables
	 * @return The list of predicted labels
	 */
	public static int[] manyTo1Mapping(PosCorpus c, int hiddenStatesSize, int[][] hiddenStates,int labelSetSize, int[][] trueLables){
		return manyTo1Mapping(c,hiddenStates.length,hiddenStatesSize, hiddenStates, labelSetSize, trueLables);
	}
	
	
	public static int[] manyTo1Mapping(PosCorpus c,int maxSentences, int hiddenStatesSize, int[][] hiddenStates,int labelSetSize, int[][] trueLables){
				
		int[][] mappingCounts = statePosCounts(c, maxSentences, hiddenStatesSize, hiddenStates, labelSetSize, trueLables);
		return manyTo1Mapping(c, mappingCounts, maxSentences, hiddenStatesSize, hiddenStates, labelSetSize, trueLables);
	}
	
	public static int[] manyTo1Mapping(PosCorpus c,int[][] mappingCounts,int maxSentences, int hiddenStatesSize, int[][] hiddenStates,int labelSetSize, int[][] trueLables){
		int[] mapping = new int[hiddenStatesSize];		
		for (int i = 0; i < hiddenStatesSize; i++) {
			int[] counts = mappingCounts[i];
			int max = -1;
			int tag = -1;
			for(int j = 0; j < labelSetSize; j++){
				if(counts[j] > max){
					max=counts[j];
					tag = j;
				}
			}
			mapping[i]=tag;
		}
		
		return mapping;
	}
	
	
	public static String printMapping(PosCorpus c, int[] mapping, int hiddenStatesSize, int labelSetSize){
		StringBuffer sb = new StringBuffer();
		TIntIntHashMap pickedTags = new TIntIntHashMap();
		for (int i = 0; i < hiddenStatesSize; i++) {
			if(!pickedTags.contains(mapping[i])){
				pickedTags.put(mapping[i], 0);
			}
			pickedTags.put(mapping[i], pickedTags.get(mapping[i])+1);
		}
		for(int i = 0; i< pickedTags.keys().length;i++){
			int key = pickedTags.keys()[i];
			sb.append("1-Many mapping - "+c.tagAlphabet.index2feat.get(key)+"-"+pickedTags.get(key)+"\n");
		}
		sb.append("1-Many mapping used " + pickedTags.size() + "/" + labelSetSize + " tags"+"\n");
		return sb.toString();
	}
	
	/**
	 * Return the sequence of tags after perform the mapping
	 * @param mapping
	 * @param hiddenStates
	 * @param nrSentences
	 * @return
	 */
	public static int[][] mapToTags(int[] mapping, int[][] hiddenStates, int nrSentences){
		nrSentences = Math.min(nrSentences,hiddenStates.length);
		int[][] result = new int[nrSentences][];
		for(int sentenceNr = 0; sentenceNr < nrSentences; sentenceNr++){
			int[] hiddenSentence = hiddenStates[sentenceNr];
			int sentenceSize = hiddenSentence.length;
			int[] predictedPos = new int[sentenceSize];
			for(int i = 0; i < sentenceSize; i++){
				predictedPos[i] = mapping[hiddenSentence[i]];
			}
			result[sentenceNr]=predictedPos;		
		}
		return result;
	}
	
	/**
	 * Computes for each hidden state how many times does it co-occur with a given tag
	 * Index 1 - Hiddend state 
	 * Index 2 - Pos Tag
	 * @param c
	 * @param maxSentences
	 * @param hiddenStatesSize
	 * @param hiddenStates 
	 * @param labelSetSize
	 * @param trueLables
	 * @return
	 */
	public static int[][] statePosCounts(PosCorpus c,int maxSentences, int hiddenStatesSize, int[][] hiddenStates,int labelSetSize, int[][] trueLables){
		int nrSentences = hiddenStates.length;
		int[][] mappingCounts = new int[hiddenStatesSize][labelSetSize];
		for(int sentenceNr = 0; (sentenceNr < nrSentences) &&  (sentenceNr < maxSentences); sentenceNr++){
			int[] hiddenSentence = hiddenStates[sentenceNr];
			int[] goldSentence = trueLables[sentenceNr];
			for(int pos = 0; pos < hiddenSentence.length; pos++){
				int hiddenState = hiddenSentence[pos];
				int trueLabel = goldSentence[pos]; 
				mappingCounts[hiddenState][trueLabel]++;
			}
		}	
		return mappingCounts;
	}
	
	/**
	 * Calculates several information theoretical values for the true distribution and 
	 * the hidden distribution:
	 * H(tag) - Entropy of tag distribution
	 * H(Gold) - Entropy of the gold distribution
	 * I(Tag,Gold) - Mutual Information between the two distributions
	 * H(Gold |Tag) -
	 * H(Tag|Gold) - 
	 * VI(Gold,Tag) - Variation of information between the two distributions
	 * Meila (2007)
	 * V Measure (Rosenberg 2007)
	 * NVI Reichart
	 *  
	 * @param posMapping - Counts of number of tokens for each hidden state tag pair
	 * @return a vector with all this measures by this order
	 */
	public static double[] informationTheorethicMeasures(int[][] posMapping, int nrHiddenStates,
			int nrTags){
		double[] infoMetrics = new double[10];
		double[] tagSums = new double[nrTags];
		double[] hiddenStateSums = new double[nrHiddenStates];
		double totalSum = 0;
		for(int i = 0; i< nrHiddenStates; i ++){
			for(int j = 0; j< nrTags; j ++){
				double num = posMapping[i][j];
				hiddenStateSums[i] += num;
				tagSums[j]+=num;
				totalSum +=num;
			}
		}
	
		//Entropy of hidden states
		for(int i = 0; i< nrHiddenStates; i ++){
			double prob = hiddenStateSums[i]/totalSum;
			if(prob!=0){
				infoMetrics[0] -= prob*Math.log(prob)/Math.log(2);
			}
		}
		//Entropy of gold states
		for(int i = 0; i< nrTags; i ++){
			double prob = tagSums[i]/totalSum;
			if(prob!=0){
				infoMetrics[1] -= prob*Math.log(prob)/Math.log(2);
			}
		}
		
		//Mutual Information
		for(int i = 0; i< nrHiddenStates; i ++){
			double hiddenProb = hiddenStateSums[i]/totalSum;			
			for(int j = 0; j< nrTags; j ++){
				double tagProb = tagSums[j]/totalSum;
				double prob = posMapping[i][j]/totalSum;
				if(prob!=0){
					infoMetrics[2] += prob*Math.log(prob/(tagProb*hiddenProb))/Math.log(2);			
				}
			}
		}
		
		//Conditional  Entropy H(Tag|Gold)
		infoMetrics[3]=infoMetrics[0]-infoMetrics[2];	
		
		//Conditional  Entropy H(Gold|Tag)
		infoMetrics[4]=infoMetrics[1]-infoMetrics[2];
		
		//Metrics taken from The NVI Clustering Evaluation Measure
		
		//Variation of information
		infoMetrics[5]=infoMetrics[3]+infoMetrics[4];
		
		//homogenity 
		double homogenity = 0;
		if (infoMetrics[1] == 0){ 
			homogenity = 1;
		}else{
			homogenity = 1 - infoMetrics[4]/infoMetrics[1];
		}
		infoMetrics[6] = homogenity;
		//homogenity 
		double completeness = 0;
		if (infoMetrics[0] == 0){ 
			completeness = 1;
		}else{
			completeness = 1 - infoMetrics[3]/infoMetrics[0];
		}
		infoMetrics[7] = completeness;

		//v measure
		infoMetrics[8] = 2 * completeness * homogenity / (homogenity + completeness);
	
		//NVI
		double nvi = 0;
		if(infoMetrics[1] == 0){
			nvi = infoMetrics[0];
		}else{
			nvi = infoMetrics[5] / infoMetrics[1];
		}
		infoMetrics[9] = nvi;
			
		return infoMetrics;
	}
	
	
	public static int[] oneToOnemapping(PosCorpus c, int hiddenStatesSize, int[][] hiddenStates,int labelSetSize, int[][] trueLables){
		return oneToOnemapping(c, hiddenStates.length,hiddenStatesSize, hiddenStates, labelSetSize, trueLables);
	}
	
	
	public static int[] oneToOnemapping(PosCorpus c,int maxSentences, int hiddenStatesSize, int[][] hiddenStates,int labelSetSize, int[][] trueLables){
		int[][] mappingCounts = statePosCounts(c, maxSentences, hiddenStatesSize, hiddenStates, labelSetSize, trueLables);
		return oneToOnemapping(c, mappingCounts, maxSentences, hiddenStatesSize, hiddenStates, labelSetSize, trueLables);
	}
	
	/**
	 * 
	 * @param c
	 * @param maxSentences
	 * @param hiddenStatesSize
	 * @param hiddenStates
	 * @param labelSetSize
	 * @param trueLables
	 * @return
	 */
	public static int[] oneToOnemapping(PosCorpus c,int[][] mappingCounts,
			int maxSentences, int hiddenStatesSize, int[][] hiddenStates,
			int labelSetSize, int[][] trueLables){
		
	
		//Contains for each hidden state what is the corresponding pos tag
		int[] mapping = new int[hiddenStatesSize];
		java.util.Arrays.fill(mapping, -1);
		
		//Contains the hidden states that were already used
		boolean[] usedStates = new boolean[hiddenStatesSize];
		java.util.Arrays.fill(usedStates, false);
	
		//Contains the pos tags that were already used
		boolean[] usedPos = new boolean[labelSetSize];
		java.util.Arrays.fill(usedPos, false);
		//For the number of tags
		//printMappingCounts(c,mappingCounts, hiddenStatesSize, labelSetSize);
		int[] stateTokes = new int[hiddenStatesSize];
		int[] bestStateTokens = new int[hiddenStatesSize];
		for(int i = 0; (i < hiddenStatesSize) && i < labelSetSize; i++){
			int bestState = -1;
			int bestTag = -1;
			int bestTagCount = -1;	
			//For all state
			//If the state was not used yet, pick the best tag it can have...
			int hiddenStateTokenCounts = 0;
			for(int tag = 0; tag < labelSetSize; tag++){
				hiddenStateTokenCounts+=mappingCounts[i][tag];
				if(usedPos[tag]) continue;
				for(int state = 0; state < hiddenStatesSize; state++){					
					if(usedStates[state]) continue;			
					
					if(mappingCounts[state][tag] > bestTagCount){
						bestTagCount = mappingCounts[state][tag];
						bestStateTokens[state]=bestTagCount;
						bestTag = tag;
						bestState = state;
					}
				}
			}
			stateTokes[i]=hiddenStateTokenCounts;
			
			mapping[bestState]=bestTag;
			usedStates[bestState]=true;
			usedPos[bestTag]=true;
		}
		return mapping;		
	}
	
	public static String print1to1Mapping(PosCorpus c, int[] mapping, int[] bestStateTokens, int[] stateTokens, int nrHiddenStates){
		StringBuffer sb = new StringBuffer();
		for(int state = 0; state < nrHiddenStates; state++){
			int bestTag = mapping[state];
			if(bestTag!=-1){
				if(c!=null){
					sb.append("s"+state+"-"+c.tagAlphabet.index2feat.get(bestTag)+":"+bestStateTokens[state]+"/"+stateTokens[state]+" ");
				}else{
					sb.append("s"+state+"-"+bestTag+":"+bestStateTokens[state]+"/"+stateTokens[state]+" ");
				}
			}
		}
		return sb.toString();
	}
	
	
	

	/**
	 * 
	 */
	public  static String printMapping(PosCorpus c, int[] mapping){
		StringBuffer sb = new StringBuffer();
		sb.append("Mapping- ");
		for(int i = 0; i < mapping.length; i++){
			if(mapping[i]==-1)continue;
			sb.append("st:" + i + " - " + c.tagAlphabet.index2feat.get(mapping[i])+" ");
		}
		sb.append("\n");
		return sb.toString();
	}

	
	public static String printMappingCounts(PosCorpus c, int[][] statePosCounts,int hiddenStatesSize,int labelSetSize){
		StringBuffer sb = new StringBuffer();
		for(int tag = 0; tag < labelSetSize; tag++){
			if(c!=null){
				sb.append(c.tagAlphabet.index2feat.get(tag)+"\t");
			}else{
				sb.append(tag+"\t");
			}
		}
		sb.append("\n");
		for(int state = 0; state < hiddenStatesSize; state++){
			sb.append(state);
			for(int tag = 0; tag < labelSetSize; tag++){
				sb.append( "\t" + statePosCounts[state][tag]);
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	
	public static void main(String[] args) {
		
//		int[][] hiddenStates = new int[4][4];
//		
//		hiddenStates[0][0]=0;
//		hiddenStates[0][1]=0;
//		hiddenStates[0][2]=1;
//		hiddenStates[0][3]=3;
//		hiddenStates[1][0]=1;
//		hiddenStates[1][1]=2;
//		hiddenStates[1][2]=0;
//		hiddenStates[1][3]=1;
//		util.Printing.printIntArray(hiddenStates, null,null,"Hidden states", 2, 4);
//		int[][] tags = new int[4][4];
//		
//		tags[0][0]=1;
//		tags[0][1]=0;
//		tags[0][2]=1;
//		tags[0][3]=0;
//		tags[1][0]=1;
//		tags[1][1]=2;
//		tags[1][2]=1;
//		tags[1][3]=1;
//		util.Printing.printIntArray(tags, null,null,"tags", 2, 4);
//		
//		int[][] mapping = oneToOnemapping(null, 2, 4, hiddenStates, 3, tags);
//		System.out.println();
//		util.Printing.printIntArray(mapping,null,null, "Mapped tags", 2, 4);
//		
	}
}
