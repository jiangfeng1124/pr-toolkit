package util;

public class Printing {
	static java.text.DecimalFormat fmt = new java.text.DecimalFormat();

	public static String padWithSpace(String s, int len){
		StringBuffer sb = new StringBuffer();
		while(sb.length() +s.length() < len){
			sb.append(" ");
		}
		sb.append(s);
		return sb.toString();
	}
	
	public static String prettyPrint(double d, String patt, int len) {
		fmt.applyPattern(patt);
		String s = fmt.format(d);
		while (s.length() < len) {
			s = " " + s;
		}
		return s;
	}
	
	public static  String formatTime(long duration) {
		StringBuilder sb = new StringBuilder();
		double d = duration / 1000;
		fmt.applyPattern("00");
		sb.append(fmt.format((int) (d / (60 * 60))) + ":");
		d -= ((int) d / (60 * 60)) * 60 * 60;
		sb.append(fmt.format((int) (d / 60)) + ":");
		d -= ((int) d / 60) * 60;
		fmt.applyPattern("00.0");
		sb.append(fmt.format(d));
		return sb.toString();
	}
	
	
		
}
