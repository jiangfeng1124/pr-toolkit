package data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import util.CountAlphabet;

public class CorpusUtils {
	
	
	/**
	 * Creates a count alphabet of suffixes of a given length based on the word tokens
	 * @param suffixLen
	 * @return
	 */
	public static CountAlphabet<String> createTokenSuffixAlphabet(Corpus c, int suffixLen){
		CountAlphabet<String> al = new CountAlphabet<String>();
		for(int i = 0; i < c.getNrWordTypes(); i++){
			String word = c.wordAlphabet.index2feat.get(i);
			if(word.length() > suffixLen+1){
				int counts = c.wordAlphabet.getCounts(i);
				//Add several instances of this suffix
				for(int count = 0; count < counts; count++){
					al.lookupObject(word.substring(word.length()-suffixLen));
				}
			}
		}
		return al;
	}
	
	/**
	 * Creates a count alphabet of suffixes of a given length based on the word types
	 * @param suffixLen
	 * @return
	 */
	public static CountAlphabet<String> createTypesSuffixAlphabet(Corpus c, int suffixLen){
		CountAlphabet<String> al = new CountAlphabet<String>();
		for(int i = 0; i < c.getNrWordTypes(); i++){
			String word = c.wordAlphabet.index2feat.get(i);
			if(word.length() > suffixLen){
				al.lookupObject(word.substring(word.length()-suffixLen));
			}
		}
		return al;
	}
	
	
	public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException, IOException {
		Corpus c = new Corpus(args[0]);
		
		//Word type suffix
//		System.out.println("Type suffix");
//		CountAlphabet<String> typeSuff = CorpusUtils.createTypesSuffixAlphabet(c, 5);
//		String res = typeSuff.toString();
//		PrintStream ps = new PrintStream(System.out, true,"UTF-8");
		//ps.println(res);
	//	System.out.println(res);
		
		//Word Tokken suffix
//		System.out.println("Token suffix");
//		CountAlphabet tokenSuff = CorpusUtils.createTokenSuffixAlphabet(c, 3);
//		System.out.println(tokenSuff.toString());
		
		System.out.println(c.wordAlphabet);
		
	}
}
