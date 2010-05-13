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
	} // add (String)

	/**
	 * Adds the total count of a word to this map.
	 * Used in reduction.
	 */
	private void add (String word, Integer count) {
		Integer i = map.get(word);
		if (i != null) {
			map.put(word, i + count);
		}
		else {
			map.put(word, count);
		}
	} // add (String, Integer)

	/**
	 * Convert a WordCounter map to two arrays.
	 */
	private static void convert (HashMap<String, Integer> map,
			String[] strResult,
			int[] countResult) {
		strResult = new String[map.size()];
		int i = 0;
		for (String s : map.keySet()) {
			strResult[i] = s;
			countResult[i++] = map.get(s);
		}
	}

	/**
	 * Convert string and int array to HashMap.
	 */
	private static HashMap<String, Integer> convert (String[] strs, int[] counts) {
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		for (int i = 0; i < strs.length; ++i) {
			result.put(strs[i], new Integer(counts[i]));
		}
		return result;
	}

	/**
	 * Get the map of word counts.
	 */
	public HashMap<String, Integer> getMap () {
		return map;
	} // getMap

	/**
	 * Returns the number of top words to print.
	 */    
	public int getNumTop() {
		return numTop;
	} // getNumTop

	/**
	 * Adds all words from other map to this one.
	 */
	public void reduce (HashMap<String, Integer> other) {
		for (String s : other.keySet()) {
			Integer i = other.remove(s);
			add(s, i);
		}
	} // reduce (HashMap)

	/**
	 * Adds all words from other WordCounter to this one.
	 */
	public synchronized void reduce (WordCounter other) {
		HashMap<String, Integer> removeSet = new HashMap<String, Integer>();
		for (String s : other.map.keySet()) {
			Integer i = other.map.get(s);
			removeSet.put(s, i);
		}
		for( String s : removeSet.keySet() ) {
			map.put( s, removeSet.get(s) );
		}
	} // reduce (WordCounter)

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

} // WordCounter// vim:noexpandtab sw=8 softtabstop=8
