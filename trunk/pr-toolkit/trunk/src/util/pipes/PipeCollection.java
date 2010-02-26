package util.pipes;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import util.Alphabet;
import util.SparseVector;
import data.Corpus;

public class PipeCollection extends Pipe{
	ArrayList<Pipe> pipes;

	public PipeCollection(){
		pipes = new ArrayList<Pipe>();
	}

	public void addPipe(Pipe p){
		pipes.add(p);
	}

	@Override
	public  void process(int wordId, String word,  Alphabet<String> alphabet,SparseVector sv){
		for(Pipe p : pipes){
			p.process(wordId, word, alphabet, sv);
		}
	}

	public  void init(Corpus c) throws IOException{
		for(Pipe p : pipes){
			p.init(c);
		}
	}

	public String getName(){
		StringBuffer bf = new StringBuffer();
		for(Pipe p : pipes){
			bf.append(p.getName() + "\n");
		}
		return bf.toString();
	}

	//Implements the simple factory design pattern to read from the file
	public static Pipe buildPipe(String fileName) throws IOException, ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException{
		PipeCollection pipes = new PipeCollection();
		if(fileName.equals("")){        

		} else {
			BufferedReader reader= new BufferedReader(new InputStreamReader(new FileInputStream(fileName),"UTF8"));
			String line = reader.readLine();
			while(line != null){
				if(!line.startsWith("#") && !(line.length()==0)){
					String[] tokens = line.split("  *"); 
					try {
						ArrayList<String> ctorargs = new ArrayList<String>(tokens.length-1);
						@SuppressWarnings("unchecked")
						Class name = Class.forName("util.pipes."+tokens[0]);
						for (int ix = 1; ix < tokens.length; ix++)
							ctorargs.add(ix-1, tokens[ix]);
						System.out.println((new String[0]).getClass().getName());
						@SuppressWarnings("unchecked")
						Constructor ctor = name.getConstructor((new String[0]).getClass());  // hack? existe um...
						pipes.addPipe((Pipe)ctor.newInstance((Object)(ctorargs.toArray(new String[0]))));
					}
					catch (ClassNotFoundException e) {  // forName
						System.err.println("Stats " + tokens[0]);
						System.err.println(e);
						return null;
					} catch (SecurityException e) {
						e.printStackTrace();
						return null;
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
						return null;
					}                                  

				}
				line = reader.readLine();
			}
		}
		return pipes;
	}

	public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException, IOException, IllegalArgumentException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Corpus c = new Corpus(args[0]);
		Pipe p = PipeCollection.buildPipe(args[1]);
		p.init(c);
		util.SparseVector precomputedValues = new SparseVector();
		util.Alphabet<String> al = new util.Alphabet<String>();
		p.process(c.wordAlphabet.feat2index.get(","), ",",  al, precomputedValues);
		p.process(c.wordAlphabet.feat2index.get("Government"), "Government",  al, precomputedValues);
		p.process(c.wordAlphabet.feat2index.get("-LCB-"), "-LCB-",  al, precomputedValues);
		System.out.println(al.toString());


	}

}
