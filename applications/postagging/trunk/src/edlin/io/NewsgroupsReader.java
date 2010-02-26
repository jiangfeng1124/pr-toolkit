package edlin.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import edlin.types.Alphabet;
import edlin.types.ClassificationInstance;
import edlin.types.SparseVector;


public class NewsgroupsReader {
	Alphabet xAlphabet;
	Alphabet yAlphabet;

	public NewsgroupsReader(Alphabet xAlphabet, Alphabet yAlphabet) {
		this.xAlphabet = xAlphabet;
		this.yAlphabet = yAlphabet;
	}

	public ArrayList<ClassificationInstance> readFile(String dataLoc)
			throws IOException {
		ArrayList<ClassificationInstance> result = new ArrayList<ClassificationInstance>();
		File baseDir = new File(dataLoc);
		for (String label : baseDir.list()) {
			String ydir = baseDir + File.separator + label;
			System.out.println(ydir);
			for (String fname : new File(ydir).list()) {
				HashSet<String> words = new HashSet<String>();
				BufferedReader reader = new BufferedReader(new FileReader(ydir
						+ File.separator + fname));
				for (String ln = reader.readLine(); ln != null; ln = reader
						.readLine()) {
					ln = ln.toLowerCase();
					ln = ln.replaceAll("[^a-z']", " ");
					for (String w : ln.split("\\s\\s*")) {
						words.add(w);
					}
				}
				reader.close();
				SparseVector x = new SparseVector();
				for (String w : words)
					x.add(xAlphabet.lookupObject(w), 1);
				result.add(new ClassificationInstance(xAlphabet, yAlphabet, x,
						label));
			}
		}
		return result;
	}

}
