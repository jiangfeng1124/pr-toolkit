package util;

import gnu.trove.TDoubleArrayList;
import gnu.trove.TDoubleProcedure;

public class TroveUtils {
	
	public static final class GetSum implements TDoubleProcedure{
		double sum = 0;
		public boolean execute(double value) {
			sum+= value;
			return true;
		}
	}
	
	//Creates a new TDoubleArrayList with all exp of all entries 
	public static TDoubleArrayList exp(TDoubleArrayList list){
		TDoubleArrayList exp = new TDoubleArrayList(list.size());
		for(int i = 0; i < list.size(); i++){
			exp.add(Math.exp(list.getQuick(i)));
		}
		return exp;
	}
	
	public static double sum(TDoubleArrayList list){
		double sum = 0;
		for(int i = 0; i < list.size(); i++){
			sum+= list.getQuick(i);
		}
		return sum;
	}
	
	public static void main(String[] args) {
		TDoubleArrayList list = new TDoubleArrayList();
		for(int i = 0; i < 3; i++){
			list.add(i);
		}
		System.out.println(list.toString());
		TDoubleArrayList res = exp(list);
		System.out.println(res.toString());
		
	}
	
}
