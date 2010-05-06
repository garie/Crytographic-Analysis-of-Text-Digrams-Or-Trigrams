import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Stargram {

	private int length;
	private int numTop;
	private HashMap<String, Integer> map;
	private String last; // The last characters added.

	public Stargram (int length, int numTop) {
		this.length = length;
		this.numTop = numTop;
		map = new HashMap<String, Integer>(length * length);
		last = new String();
	}
	
	public void add (char c) {
		if (last.length() < this.length) {
			last += c;
		}
		else {
			last = last.substring(1) + c;
		}
		
		if (last.length() == this.length) {
			Integer i = map.get(last);
			if (i != null) {
				i++;
				map.put(last, i);
			}
			else {
				map.put(last, new Integer(1));
			}
		}
	}
	
	public void add (String toAdd) {
		for (int i = 0; i < toAdd.length(); ++i) {
			add(toAdd.charAt(i));
		}
	}
	
	public void clearLast () {
		last = new String();
	}
	
	public int getNumTop() {
		return numTop;
	}
	
	public int length() {
		return this.length;
	}
	
	public String[] top () {
		return top(numTop);
	}
	
	/**
	 * @param num - the number of values to return
	 */
	public String[] top (int num) {
		ArrayList<String> result = new ArrayList<String>(num);
		
		for (Iterator<String> i = map.keySet().iterator(); i.hasNext(); ) {
			String curr = i.next();
			int currInt = map.get(curr);
			
			int index = -1;
			for (int j = result.size() - 1;
				 j >= 0 && (map.get(result.get(j)).intValue() < currInt);
				 j--) {
				index = j;
			}
			
			if (index >= 0) {
				if (result.size() == num) {
					result.remove(num-1);
				}
				result.add(index, curr);
			}
		}
		
		assert (result.size() <= num);
		
		return (String[])result.toArray();
	}
	
}