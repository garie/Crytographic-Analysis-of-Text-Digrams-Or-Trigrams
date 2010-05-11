/*
 * file: WordCounter.java
 */
 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Counts the number of words in the text.
 */
public class WordCounter {

    /* The map that holds all of the word counts. */
	private HashMap<String, Integer> map;
    
    /* The number of top words to print. */
    private int numTop;

    /**
     * Constructor
     */
	public WordCounter (int numTop) {
		map = new HashMap<String, Integer>();
        this.numTop = numTop;
	} // WordCounter

    /**
     * Add a word to the counter.
     */
	public void add (String word) {
		Integer i = map.get(word);
		if (i != null) {
			map.put(word, ++i);
		}
		else {
			map.put(word, new Integer(1));
		}
	} // add

    /**
     * Returns the number of top words to print.
     */    
    public int getNumTop() {
        return numTop;
    } // getNumTop

    /**
     * Returns String representations of the top n words.
     * Format: word\t\tcount
     */
    public String[] top () {
		ArrayList<String> temp = new ArrayList<String>(numTop);
		
		for (Iterator<String> i = map.keySet().iterator(); i.hasNext(); ) {
			String curr = i.next();
			int currInt = map.get(curr);
			
			int index = -1;
			for (int j = temp.size() - 1;
				j >= 0 && (map.get(temp.get(j)).intValue() < currInt);
				j--) {
				index = j;
			}
				
			if (index >= 0) {
				if (temp.size() == numTop) {
					temp.remove(numTop - 1);
				}
				temp.add(index, curr);
			}
			else if (temp.size() < numTop) {
				temp.add(temp.size(), curr);
			}
		}
		
		String[] result = new String[temp.size()];
		for (int i = 0; i < result.length; ++i) {
			String s = temp.get(i);
			result[i] = s + "\t\t" + map.get(s);
		}
		
		return result;
    } // top
	
} // WordCounter