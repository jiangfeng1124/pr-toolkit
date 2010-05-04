package model.distribution;

import gnu.trove.TIntArrayList;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;



/**
 * Contains a multivariate multinomial distribution
 * Sum over all states for a given variable = 1
 * 
 * @author javg
 *
 */
public interface  AbstractMultinomial {
	
	public abstract int numStates();
	
	public abstract void print(String name, String[] labels1, String[] labels2);
	
	public abstract void fill(double value);
	
	public abstract void fill(AbstractMultinomial other);
	
	public abstract  void addCounts(int variable, int state, double value);
	public abstract  void setCounts(int variable, int state, double value);
	public abstract  double getCounts(int variable, int state);
	public abstract  void initializeRandom(Random r, double jitter);
	public abstract void copyAndNormalize(AbstractMultinomial other);
	public abstract void saveTable(String name);
	public abstract AbstractMultinomial clone();
	public abstract TIntArrayList[] getAvailableStates();
	public abstract TIntArrayList getAvailableStates(int position);
	
	public abstract String toString(String name, String[] labels1, String[] labels2);
	
	
		

	
	
}
