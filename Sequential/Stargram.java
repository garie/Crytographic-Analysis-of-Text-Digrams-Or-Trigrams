/*
 * file: Stargram.java
 */
 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Creates and keeps track of *grams.
 */
public class Stargram {

    /* The length of this *gram */
	private int length;
    
    /* The number of top *grams to print */
	private int numTop;
    
    /* The map with all of the data */
	private HashMap<String, Integer> map;
    
    /* The last characters added. */
    private String first;
	private String last;

    /**
     * Constructor
     */
	public Stargram (int length, int numTop) {
		this.length = length;
		this.numTop = numTop;
		map = new HashMap<String, Integer>(length * length);
        first = new String();
		last = new String();
	} // Stargram

    /**
     * Add a single character.
     */	
	private void add (char c) {
        if (first.length() < this.length) {
            first += c;
        }

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
	} // add (char)

    /**
     * Add the characters in the string.
     */	
	public void add (String toAdd) {
		for (int i = 0; i < toAdd.length(); ++i) {
			add(toAdd.charAt(i));
		}
	} // add (String)

    /**
     * Add 'count' to the given gram.
     * Used in reduction.
     * TODO: deal with last.
     */
    private void add (String gram, Integer count) {
        Integer i = map.get(gram);
		if (i != null) {
            map.put(gram, i + count);
		}
		else {
			map.put(gram, count);
		}
    } // add (String, Integer)
    
    /**
     * Clear the last part read.
     *
     * Use this when a new file is reached so you're not counting the
     * end of one file + the beginning of another.
     */	
	public void clear () {
        first = new String();
		last = new String();
	} // clearLast

    /**
     * Return the first gram found.
     */
    public String getFirst () {
        return first;
    } // getFirst
    
    /**
     * Return the last gram found.
     */
    public String getLast () {
        return last;
    } // getLast
    
    /**
     * Gets the number of top *grams to print.
     */	
	public int getNumTop() {
		return numTop;
	} // getNumTop

    /**
     * Returns the length of this *gram.
     */	
	public int length() {
		return this.length;
	} // length

    /**
     * Reduces the grams in other with this one.
     * TODO: deal with "last"
     */
    public void reduce (Stargram other) {
        for (String s : other.map.keySet()) {
            Integer i = other.map.get(s);
            add(s, i);
        }
    } // reduce
    
    /**
     * Returns String representations of the top numTop *grams.
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
	} // top ()
	
} // Stargram// vim:noexpandtab sw=8 softtabstop=8
