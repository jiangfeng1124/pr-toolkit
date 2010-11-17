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
	
	/**
	 * If number becomes infinity replace it by a smallest value possible
	 * @param list
	 * @return
	 */
	public static TDoubleArrayList exp(TDoubleArrayList list){
		TDoubleArrayList exp = new TDoubleArrayList(list.size());
		for(int i = 0; i < list.size(); i++){
			double value = Math.exp(list.getQuick(i));
			exp.add(value);
		}
		return exp;
	}
	
	public static double sum(TDoubleArrayList list){
		double sum = 0;
		for(int i = 0; i < list.size(); i++){
			double value = list.getQuick(i);
			if(!(Double.isInfinite(value) || Double.isNaN(value))){
				sum+=  value;
			}
		}
		return sum;
	}
	
	
	public static double max(TDoubleArrayList list){
		return list.max();
	}
	
	/**
	 * Adds num to all elements of the list
	 * @param list
	 * @param num
	 * @return
	 */
	public static void addValue(TDoubleArrayList list, double num){
		for(int i = 0; i < list.size(); i++){
			list.setQuick(i, list.getQuick(i)+num);
		}
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
