package edlin.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import edlin.sequence.SequenceInstance;
import edlin.types.Alphabet;
import edlin.types.SparseVector;


public class PartOfSpeechReader {

	Alphabet xAlphabet;
	Alphabet yAlphabet;

	public PartOfSpeechReader(Alphabet xAlphabet, Alphabet yAlphabet) {
		this.xAlphabet = xAlphabet;
		this.yAlphabet = yAlphabet;
	}

	public ArrayList<SequenceInstance> readFile(String dataLoc)
			throws IOException {
		ArrayList<SequenceInstance> result = new ArrayList<SequenceInstance>();
		BufferedReader reader = new BufferedReader(new FileReader(dataLoc));
		ArrayList<String> words = new ArrayList<String>();
		ArrayList<String> tags = new ArrayList<String>();
		for (String ln = reader.readLine(); ln != null; ln = reader.readLine()) {
			if (ln.length() < 2) {
				SparseVector[] x = new SparseVector[words.size()];
				Object[] y = new Object[tags.size()];
				for (int t = 0; t < x.length; t++) {
					y[t] = tags.get(t);
					String word = "^" + words.get(t) + "$";
					x[t] = new SparseVector();
					x[t].add(xAlphabet.lookupObject(word), 1);
					for (int i = 0; i < word.length() - 3; i++) {
						x[t].add(xAlphabet.lookupObject(word
								.substring(i, i + 3)), 1);
					}
				}
				result.add(new SequenceInstance(xAlphabet, yAlphabet, x, y));
				words = new ArrayList<String>();
				tags = new ArrayList<String>();
				continue;
			}
			String[] wordpos = ln.split("\t");
			words.add(wordpos[0]);
			tags.add(wordpos[1]);
		}
		return result;
	}

}
