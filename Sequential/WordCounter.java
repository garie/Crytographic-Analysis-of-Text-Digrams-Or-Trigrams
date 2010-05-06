import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class WordCounter {

	private HashMap<String, Integer> map;

	public WordCounter () {
		map = new HashMap<String, Integer>();
	}
	
	public void add (String word) {
		Integer i = map.get(word);
		if (i != null) {
			i++;
			map.put(word, i);
		}
		else {
			map.put(word, new Integer(1));
		}
	}
	
	public int length() {
		return map.size();
	}
	
	public String[] top (int num) {
		ArrayList<String> temp = new ArrayList<String>(num);
		
		for (Iterator<String> i = map.keySet().iterator(); i.hasNext(); ) {
			String curr = i.next();
			int currInt = map.get(curr);
			//System.out.println(curr + ": " + currInt);
			
			int index = -1;
			if (temp.size() == 0) {
				index = 0;
			}
			else {
				for (int j = temp.size() - 1;
					j >= 0 && (map.get(temp.get(j)).intValue() < currInt);
					j--) {
					index = j;
				}
			}
			
			if (index >= 0) {
				if (temp.size() == num) {
					temp.remove(num-1);
				}
				System.out.println("Adding " + index + " " + curr);
				temp.add(index, curr);
			}
		}
		
		assert (temp.size() <= num);
		
		String[] result = new String[temp.size()];
		for (int i = 0; i < result.length; ++i) {
			result[i] = temp.get(i);
		}
		
		return result;
	}
	
}
// vim:noexpandtab sw=8 softtabstop=8
