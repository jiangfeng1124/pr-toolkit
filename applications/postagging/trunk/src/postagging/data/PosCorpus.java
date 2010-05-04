package postagging.data;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntObjectHashMap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.TreeSet;

import util.CountAlphabet;
import util.MemoryTracker;
import data.Corpus;
import data.InstanceList;
import data.WordInstance;

/**
 * Contains a pos corpus:
 * Extends the base corpus by adding pos tags information
 * @author javg
 *
 */
public class PosCorpus extends Corpus{
	public CountAlphabet<String> tagAlphabet;
	
	
	public PosCorpus(String corpusParams) throws UnsupportedEncodingException, FileNotFoundException, IOException {
		this(corpusParams,0,Integer.MAX_VALUE);
	}
	
	public PosCorpus(String corpusParams, int minSentenceSize,
			int maxSentenceSize) throws UnsupportedEncodingException, FileNotFoundException, IOException {
		super(corpusParams,minSentenceSize,maxSentenceSize);	
	}

	@Override
	public void initStructures(String corpusParams) {
		super.initStructures(corpusParams);
		this.tagAlphabet = new CountAlphabet<String>();	
	}
	
	public void stopGrowingAlphabets(){
		super.stopGrowingAlphabets();
		this.tagAlphabet.stopGrowth();
		this.tagAlphabet.setStopCounts(true);
	}

	
	public int getNrTags(){
		return tagAlphabet.size();
	}
	
	public String[] getTagStrings(int[] tagIds) {
		String[] tags = new String[tagIds.length];
		for (int i = 0; i < tags.length; i++) {
			tags[i] = tagAlphabet.index2feat.get(tagIds[i]);
		}
		return tags;	
	}
	
	public String[] getAllTagsStrings() {
		String[] tags= new String[getNrTags()];
		for (int i = 0; i < tags.length; i++) {
			tags[i]=tagAlphabet.index2feat.get(i);
		}
		return tags;
	}
	
	@Override 
	public InstanceList readInstanceList(String name, String fileName,
			String readerType, boolean lowercase, 
			int minSentenceLenght, int maxSentenceLenght
			,CountAlphabet fullVocab, int minWordOccurs) throws UnsupportedEncodingException, FileNotFoundException, IOException{
		System.out.println("Calling readInstanceList " + name + " " + fileName);
		if(readerType.equalsIgnoreCase("posTag-project")){
			return PosInstanceList.readFromPosTagProject(name, fileName, 
					this.wordAlphabet,this.tagAlphabet, 
					lowercase,minSentenceLenght,maxSentenceLenght
					,fullVocab,minWordOccurs);
		}else if(readerType.equalsIgnoreCase("conll-data")){
			return PosInstanceList.readFromConll(name, fileName, 
					this.wordAlphabet,this.tagAlphabet, 
					lowercase,minSentenceLenght,maxSentenceLenght
					,fullVocab,minWordOccurs);
		}else if(readerType.equalsIgnoreCase("europarl")){
			return PosInstanceList.readFromEuroparl
			(name, fileName, 
					this.wordAlphabet, 
					lowercase,minSentenceLenght,maxSentenceLenght
					,fullVocab,minWordOccurs);
		}else{
			System.out.println("Unknow reader type");
			System.exit(-1);
		}
		return null;
	}

	
	
	public void printCorpusStats(){
		super.printCorpusStats();
		System.out.println("Number of tags: " + this.getNrTags());
	}

	public String printTags(InstanceList list ,int[][] predictions){
		StringBuffer sb = new StringBuffer();
		int i = 0;
		for(WordInstance inst: list.instanceList){
			int[] words = inst.words;
			int[] tags = predictions[i];
			for (int j = 0; j < tags.length; j++) {
				String tagS = "noTag:"+j;
				if(tags[j] != -1){
					tagS = tagAlphabet.index2feat.get(tags[j]);
				}
				sb.append(tagS+"\t"
						+wordAlphabet.index2feat.get(words[j])+"\n");
			}
			sb.append("\n");
			i++;
		}
		return sb.toString();
	}
	
	public String printClusters(InstanceList list, int[][] predictions){
		StringBuffer sb = new StringBuffer();
		int i = 0;
		for(WordInstance inst: list.instanceList){
			int[] words = inst.words;
			int[] tags = predictions[i];
			for (int j = 0; j < tags.length; j++) {
				sb.append(tags[j]+"\t"
						+wordAlphabet.index2feat.get(words[j])+"\n");
			}
			sb.append("\n");
			i++;
		}
		return sb.toString();
	}
	
	public TIntObjectHashMap<HashSet<Integer>> getTagPerWord(){
		TIntObjectHashMap<HashSet<Integer>> tagsPerWord = new TIntObjectHashMap<HashSet<Integer>>();
		
		for(int sentenceNr=0; sentenceNr < getNrOfTrainingSentences(); sentenceNr++){
			int[] words = trainInstances.instanceList.get(sentenceNr).words;
			int tags[] = ((PosInstance)trainInstances.instanceList.get(sentenceNr)).tags;
			for (int wordPos = 0; wordPos < words.length; wordPos++) {
				int wordId = words[wordPos];
				if(!tagsPerWord.contains(wordId)){
					tagsPerWord.put(wordId, new HashSet<Integer>());
				}
				tagsPerWord.get(wordId).add(tags[wordPos]);
			}
		}
		
		return tagsPerWord;
		
	}
	
	public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException, IOException {
			PosCorpus c = new PosCorpus(args[0]);
			int i = 0;
			for (WordInstance inst : c.trainInstances.instanceList) {
				PosInstance inst2 = (PosInstance) inst;
				System.out.print(inst.getSentence(c));
				System.out.print(inst2.getTagsStrings(c));
				if(i > 100) break;
				i++;
			}
		}
	
}
