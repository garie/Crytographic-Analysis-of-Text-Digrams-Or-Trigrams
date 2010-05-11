/*
 * file: CharCounter.java
 */

/**
 * Keeps track of the number of each character.
 */
public class CharCounter {

	/* System independent newline character */
	public static final String NEWLINE = System.getProperty("line.separator");

    /* number of characters */
	public static final int NUM_CHARS = 26;
	public static final int NUM_UNICODE = (int)Character.MAX_VALUE -
										  (int)Character.MIN_VALUE;

    /* chars[0] holds the number of a's that have been seen */
	private int[] chars;
    
    /* Total number of characters seen */
	private int totalChars;
    
    /* Whether this is a unicode counter or not */
	private boolean unicode;

    /**
     * Constructor
     */
	public CharCounter (boolean unicode) {
		this.unicode = unicode;
		if (unicode) {
			chars = new int[NUM_UNICODE];
		}
		else {
			chars = new int[NUM_CHARS];
		}
		totalChars = 0;
	} // CharCounter
	
    /**
     * Adds a single character.
     */
	private void add (char c) {
		if (unicode) {
			++chars[(int)c - (int)Character.MIN_VALUE];
			++totalChars;
		}
		else {
			if (c >= 'a' || c <= 'z') {
				++chars[c - 'a'];
				++totalChars;
			}
			else if (c >= 'A' || c <= 'Z') {
				++chars[c - 'A'];
				++totalChars;
			}
			else {
				System.err.println("Tried to add non-char: " + c);
			}
		}
	} // add
	
	/**
	 * Adds all characters in string.
	 */
	public void add (String s) {
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c >= 'a' && c <= 'z' ||
			    c >= 'A' && c <= 'Z') {
				add(c);
			}
		}
	}
	
	/**
	 * Returns string of format:
	 *
	 * a	num		percent
	 * b	num		percent
	 * etc.
	 * Total: total
	 */
	public String toString () {
		String result = new String();
		if (unicode) {
			for (int i = 0; i < NUM_UNICODE; i++) {
				if (chars[i] != 0) {
					result += (char)(i + (int)Character.MIN_VALUE) + "\t" +
							  chars[i] + "\t\t";
					double percent = (double)(chars[i]) / (double)(totalChars) * 100.0;
					result += percent < 10.0 ? "0" : "";
					result += percent + NEWLINE;
				}
			}
		}
		else {
			for (int i = 0; i < NUM_CHARS; i++) {
				result += (char)(i + 'a') + "\t" + chars[i] + "\t\t";
				double percent = (double)(chars[i]) / (double)(totalChars) * 100.0;
				result += percent < 10.0 ? "0" : "";
				result += percent + NEWLINE;
			}
		}
		result += "Total: " + totalChars + NEWLINE;
		return result;
	} // toString

} // CharCounter

