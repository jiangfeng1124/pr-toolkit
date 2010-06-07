package util.pipes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.Alphabet;
import util.SparseVector;

public class PatternMatchPipe extends Pipe{
	 String pattern;
	 String name;
	 Pattern p;
	 
	 public PatternMatchPipe(String[] args){
		 this(args[0],args[1]);
		 if (args.length!= 2) throw new IllegalArgumentException("expected 2 arguments got "+args.length);
	 }
	 
	 public PatternMatchPipe(String pattern, String name){
		 this.pattern = pattern;
		 p = Pattern.compile(pattern);
		 this.name = name;
	 }
	 
	 @Override
	 public  void process(int wordId, String word,  Alphabet<String> alphabet,SparseVector sv){
		 Matcher m = p.matcher(word);	
		 if(m.matches()){
			 sv.add(alphabet.lookupObject(name), 1);
		 }
	}
	 
	 public String getName(){
			return "Pattern match: " + pattern+" "+name;
	}
}
