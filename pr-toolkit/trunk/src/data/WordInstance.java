package data;



public class WordInstance {
	public int[] words;
	protected int instanceNumber;
	
	public WordInstance(){}
	
	public WordInstance(int[] words, int instanceNumber){
		this.instanceNumber = instanceNumber;
		this.words =words;
	}
	
	public  int getNrWords(){
		return words.length;
	}
	
	public int getInstanceNumber(){
		return instanceNumber;
	}
	
	/**
	 * Return the word id at position . 
	 * @param position
	 * @return
	 */
	public int getWordId(int position){
		return words[position];
	}
	
	public int getTagId(int position){
		System.out.println("Not defines for this instance");
		System.exit(-1);
		return -1;
	}
	
	public String toString() {
		return "\n Word Instance " + instanceNumber+ "\n size " + words.length + "\n"  + util.Printing.intArrayToString(words, null,"sentence words") + "\n";
		
	}
		
	public String toString(Corpus c) {
		return "\n Word Instance " + instanceNumber+ "\n size " + words.length + "\n"  
		+ util.Printing.intArrayToString(words, c.getWordStrings(words),"sentence words") + "\n";
	}
	
	public String getSentence(Corpus c){
		String wordsS[] = c.getWordStrings(words);
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < wordsS.length; i++) {
			sb.append(wordsS[i]+ " ");
		}
		sb.append("\n");
		return sb.toString();
	}
	
}
