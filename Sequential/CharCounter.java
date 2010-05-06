public class CharCounter {

	// System independent newline character
	public static final String NEWLINE = System.getProperty("line.separator");
	
	// This should always be 26 because it doesn't make sense to count non-characters.
	// TODO: unicode for non-English characters.
	public static final int NUM_CHARS = 26;

	int[] chars;
	int totalChars;

	public CharCounter () {
		chars = new int[NUM_CHARS];
		totalChars = 0;
	}
	
	public void add (char c) {
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
	
	/**
	 * Adds all characters in string.
	 */
	public void add (String s) {
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			add(c);
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
		for (int i = 0; i < NUM_CHARS; i++) {
			result += (char)(i + 'a') + "\t" + chars[i] + "\t\t";
			double percent = (double)(chars[i]) / (double)(totalChars) * 100.0;
			result += percent < 10.0 ? "0" : "";
			result += percent + NEWLINE;
		}
		result += "Total: " + totalChars + NEWLINE;
		return result;
	}

}
// vim:noexpandtab sw=8 softtabstop=8
