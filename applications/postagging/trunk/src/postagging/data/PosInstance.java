package postagging.data;

import data.Corpus;
import data.WordInstance;




public class PosInstance extends WordInstance{

	
	public PosInstance(int[] words, int tags[],int instanceNumber) {
		super(words, instanceNumber);
		this.tags = tags;
	}

	int tags[];
	
	/**
	 * Return the word id at position . 
	 * @param position
	 * @return
	 */
	public int getTagId(int position){
		return tags[position];
	}
	
	
	
	public int[] getTags(){
		return tags;
	}

	@Override
	public String toString() {
		return super.toString() 
		+ util.Printing.intArrayToString(tags, null,"sentence tags") + "\n";
		
	}

	@Override
	public String toString(Corpus c) {
		return super.toString(c) 
		+ util.Printing.intArrayToString(tags, ((PosCorpus)c).getTagStrings(tags),"sentence tags") + "\n";
		
	}

	public String getTagsStrings(PosCorpus c){
		String tagsS[] = c.getTagStrings(tags);
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < tagsS.length; i++) {
			sb.append(tagsS[i]+ " ");
		}
		sb.append("\n");
		return sb.toString();
	}
}
