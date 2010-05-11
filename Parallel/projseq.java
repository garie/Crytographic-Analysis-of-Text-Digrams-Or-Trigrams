/*
 * file: projseq.java
 */

import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * Sequential implementation of Cryptographic Analyzer.
 *
 * @author dht5977: Daniel Tyler
 * @author cev5122: Christine Viets
 */

public class projseq {

    /* System independent newline character */
    public static final String NEWLINE = System.getProperty("line.separator");
    
    /* Usage statement */
    public static final String USAGE = "java projseq arg1 [arg...] text-files";
                                       
    /* Argument strings */
    public static final String ARG_WORDS   = "-w";
    public static final String ARG_CHARS   = "-c";
    public static final String ARG_GRAMS   = "-g";
    public static final String ARG_UNICODE = "-u";
    public static final String ARG_SPACES  = "-s";
    public static final String ARG_APOST   = "-a";
 
    /* Flags */
    static boolean findChars  = false;
    static boolean findGrams  = false;
    static boolean findWords  = false;
    static boolean keepApost  = false;
    static boolean keepSpaces = false;
    static boolean unicode    = false;
	
    /* Data containers */
	static CharCounter charCount;
	static WordCounter wordCount = null;
	static File[] files;
	static Stargram[] grams;
	
    /**
     * main method
     *
     * @param    args       command line arguments
     *           data-files Text files to read.
     *           -w N       If present, give word frequency and print the top N
     *           -c         If present, give char frequency
     *           -g n N     If present, give n-gram frequency. Print the top N.
     *                          Unlimited uses.
     *           -s         Keep spaces
     *           -a         Keep apostrophes
     *           -u         Unicode - implies keeping punctuation
     */
    public static void main (String args[]) {
	
		parseArgs(args);

        for (int i = 0; i < files.length; ++i) {
			readFile(files[i]);
        }

		print();
		
    } // main
	
    /**
     * parseArgs - parse the command line arguments
     */
	private static void parseArgs (String[] args) {
	    if (args.length < 2) {
            usage();
        }

        /* Check for correct args and find number of files and *grams */
        int numgrams = 0; // Number of *grams, then counter for *gram array.
		int index;
        for (index = 0; index < args.length; ++index) {
            if (args[index].equals(ARG_GRAMS)) {
                ++numgrams;
                if (++index >= args.length ||
                    (args[index].charAt(0) < '0' || args[index].charAt(0) > '9')) {
                    usage();
                }
                if (++index >= args.length ||
                    (args[index].charAt(0) < '0' || args[index].charAt(0) > '9')) {
                    usage();
                }
            }
            else if (args[index].equals(ARG_WORDS)) {
                if (++index >= args.length ||
                    (args[index].charAt(0) < '0' || args[index].charAt(0) > '9')) {
                    usage();
                }
            }
            else if (args[index].charAt(0) != '-') {
				if (index == 0) {
					usage();
				}
				break;
            }
        }
        grams = new Stargram[numgrams];
        numgrams = 0;
        		        
        /* Parse args */
        for (int i = 0; i < args.length; ++i) {
			if (args[i].equals(ARG_WORDS)) {
                System.out.println("DEBUG: Finding words");
				if (findWords || wordCount != null) {
					usage();
				}
                findWords = true;
                wordCount = new WordCounter(Integer.parseInt(args[++i]));
            }
            else if (args[i].equals(ARG_CHARS)) {
                System.out.println("DEBUG: Finding chars");
				if (findChars) {
					usage();
				}
                findChars = true;
            }
            else if (args[i].equals(ARG_GRAMS)) {
                System.out.println("DEBUG: Finding grams");
                findGrams = true;
                grams[numgrams++] = new Stargram(Integer.parseInt(args[++i]),
                                                 Integer.parseInt(args[++i]));
            }
            else if (args[i].equals(ARG_UNICODE)) {
                System.out.println("DEBUG: Unicode");
				if (unicode) {
					usage();
				}
                unicode = true;
            }
            else if (args[i].equals(ARG_SPACES)) {
                System.out.println("DEBUG: Keep spaces");
				if (keepSpaces) {
					usage();
				}
                keepSpaces = true;
            }
            else if (args[i].equals(ARG_APOST)) {
                System.out.println("DEBUG: Keep apostrophes");
				if (keepApost) {
					usage();
				}
                keepApost = true;
            }
            else if (args[i].charAt(0) != '-') {
				break;
			}
			else {
                usage();
            }
        }
		
		if ((keepSpaces || keepApost) && !findGrams) {
			usage();
		}

		if (unicode && (keepSpaces || keepApost)) {
			usage();
		}
       
        /* Create data holders */
        files = new File[args.length - index];
        
        if (files.length == 0) {
            usage();
        }
		
		for (int i = 0; index < args.length; ++index, ++i) {
			if (args[index].charAt(0) == '-') {
				usage();
			}
			files[i] = new File(args[index]);
		}

        charCount = new CharCounter(unicode);

	} // parseArgs
	
    /**
     * print - print the results of the program
     */
	static void print() {
	    System.out.println();
        if (findWords) {
            System.out.println("TOP " + wordCount.getNumTop() +
                               " WORDS" + NEWLINE);
            String[] topW = wordCount.top();
            for (String s : topW) {
                System.out.println(s);
            }
            System.out.println(NEWLINE);
        }
        if (findChars) {
            System.out.println("CHAR\tCOUNT\t\tPERCENTAGE" + NEWLINE);
            System.out.println(charCount.toString() + NEWLINE + NEWLINE);
        } 
        if (findGrams) {
            for (Stargram st : grams) {
                System.out.println("TOP " + st.getNumTop() + " " +
                                   st.length() + "-GRAMS" + NEWLINE);
                String[] topG = st.top();
                for (String s : topG) {
                    System.out.println(s);
                }
                System.out.println(NEWLINE);
            }
        }
	} // print
	
    /**
     * readFile - read and parse a file
     */
	static void readFile (File filename) {
	    Scanner sc = null;
        try {
			sc = new Scanner(filename);
                
		    while (sc.hasNext()) {
                String curr = sc.nextLine().trim();
                if (!unicode) {
                    // Non alpha, space, and punctuation chars removed
                    curr = curr.toLowerCase().replaceAll("[^a-z ']", " ");
                    if (!keepApost) {
                        // Apostrophes removed
                        curr = curr.replaceAll("[']", "");
                    }
                }
                if (findWords) {
                    Scanner sc2 = null;
                    if (unicode) {
                        sc2 = new Scanner(curr);
                    }
                    else {
                        // Never include punctuation in words.
                        sc2 = new Scanner(curr.replaceAll("[^a-z ]", ""));
                    }
                    while (sc2.hasNext()) {
                        String curr2 = sc2.next();
                        wordCount.add(curr2);
                    }
                }
                if (findChars) {
                    if (unicode) {
                        charCount.add(curr);
                    }
                    else {
                        // Never include non-alpha characters.
                        charCount.add(curr.replaceAll("[^a-z]", ""));
                    }
                }
                if (findGrams) {
                    if (!keepSpaces) {
                        curr = curr.replaceAll(" ", "");
                    }
                    else {
                        // Add a space to the end of the line for word end.
                        curr += " ";
                    }

                    for (Stargram s : grams) {
                        s.add(curr);
                    }
                }
            }

            // Clear the "last" holder from the *grams for a new file.
            for (Stargram s : grams) {
                s.clearLast();
            }
                
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        } finally {
            if (sc != null) {
                sc.close();
            }
        }
	} // readFile

    /**
     * usage - print the usage statement and exit
     */
    private static void usage() {
        System.out.println(USAGE); //java projseq arg1 [arg...] text-files
        
        System.out.println("-a     = consider apostrophes in *grams");
        System.out.println("\t\tDo not use without -g option.");
        System.out.println("\t\tDo not use with -u option.");

        System.out.println("-c     = give a-z character frequency");
        System.out.println("\t\tIf used with -u, gives all characters");
        
        System.out.println("-g x y = give top y x-grams");
        
        System.out.println("-s     = consider spaces in *grams");
        System.out.println("\t\tDo not use without -g option.");
        System.out.println("\t\tDo not use with -u option.");

        System.out.println("-w     = give word count");
        System.out.println("\t\tDoes not consider punctuation.");
        
        System.out.println("-u     = parse files in unicode");
        System.out.println("\t\tDo not use with -s or -a options.");
		
        System.exit(1);
    }
          
} // projseq
