/*
 * file: projclu.java
 */

import edu.rit.mp.IntegerBuf;
import edu.rit.mp.ObjectBuf;

import edu.rit.pj.Comm;
import edu.rit.pj.reduction.IntegerOp;
import edu.rit.pj.reduction.ReduceArrays;

import edu.rit.util.Arrays;
import edu.rit.util.Range;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.HashMap;
import java.util.Scanner;

/**
 * Cluster implementation of Cryptographic Analyzer.
 * This needs editing if used with >26 processors.
 *
 * @author dht5977: Daniel Tyler
 * @author cev5122: Christine Viets
 */

public class projclu {

    /* System independent newline character */
    public static final String NEWLINE = System.getProperty("line.separator");
    
    /* Root processor */
    public static final int ROOT = 0;
    
    public static final int MAX_SIZE = 26;
    
  	public static final int NOT_UNICODE = 26;
	public static final int UNICODE = (int)Character.MAX_VALUE -
                                      (int)Character.MIN_VALUE;

    /* Usage statement */
    public static final String USAGE = "java projclu arg1 [arg...] text-files";
                                       
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
	
    /* Runtime */
    static long start, end;
    
    /* Char containers */
    static int totalCharCount;
    static int[] totalCharArray;
    static int[][] charCountArray;
    static int[][] charArrayArray;
	static CharCounter charCounter;
    static IntegerBuf[] charCounts;
    static IntegerBuf myCharCount;
    static IntegerBuf[] charArrays;
    static IntegerBuf myCharArray;

    /* Word containers */
	static WordCounter wordCounter = null;
    //static HashMap<String, Integer> totalWordMap;
    static WordCounter totalWordCounter;
    static HashMap<String, Integer>[][] wordMapArray;
    static ObjectBuf<HashMap<String, Integer>>[] mapBufs;
    static ObjectBuf<HashMap<String, Integer>> myMap;

	static String[] filenames;
	static Stargram[] grams;
    static int maxgram;
    static String[] firstgrams;
    static String[] lastgrams;
    
    static Range[] ranges;
    static Range myrange;
    static Range charRange;
    
    /* Communicator */
    static Comm world;
    static int size;
    static int rank;
	
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
    public static void main (String args[]) throws Exception {
    
        start = System.currentTimeMillis();
    
        Comm.init(args);
        world = Comm.world();
        rank = world.rank();
        size = world.size();
        if (size > MAX_SIZE) {
            usage();
        }
        ranges = new Range(0, size-1).subranges(size);
        myrange = ranges[rank];
	
		parseArgs(args);
        
        for (int i = 0; i < filenames.length; ++i) {
            // this is inside the for loop because the other threads
            // need to wait for the files to be split up
            if (rank == ROOT) {
                splitFile(new File(filenames[i]));
            }
        }
        
        if (findChars) {
            charCluster();
        }
        if (findWords) {
            wordCluster();
        }
        
        for (int i = 0; i < filenames.length; ++i) {
			readFile(filenames[i]);
            
            // Clear the "last" holder from the *grams for a new file.
            if (findGrams) {
                for (Stargram s : grams) {
                    if (s.length() == maxgram) {
                        firstgrams[i] = s.getFirst();
                        lastgrams[i] = s.getLast();
                    }
                    s.clear();
                }
            }
        }

        gatherAndReduce();
        
        end = System.currentTimeMillis();
        if (rank == ROOT) {
            print();
        }
		
    } // main
	
    /**
     * Initialize cluster stuff for findChar
     */
    private static void charCluster () {
        if (unicode) {
            charRange = new Range(0, UNICODE-1);
        }
        else {
            charRange = new Range(0, NOT_UNICODE-1);
        }
        charCountArray = new int[size][];
        charArrayArray = new int[size][];
        if (rank == ROOT) {
            Arrays.allocate(charCountArray, 1);
            if (unicode) {
                Arrays.allocate(charArrayArray, UNICODE);
            }
            else {
                Arrays.allocate(charArrayArray, NOT_UNICODE);
            }       
        }
        else {
            Arrays.allocate(charCountArray, myrange, 1);
            if (unicode) {
                Arrays.allocate(charArrayArray, myrange, UNICODE);
            }
            else {
                Arrays.allocate(charArrayArray, myrange, NOT_UNICODE);
            }
        }
        
        charCounts = IntegerBuf.rowSliceBuffers(charCountArray, ranges);
        myCharCount = IntegerBuf.rowSliceBuffer(charCountArray, myrange);
        charArrays = IntegerBuf.rowSliceBuffers(charArrayArray, ranges);
        myCharArray = IntegerBuf.rowSliceBuffer(charArrayArray, myrange);
    } // charCluster
    
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
        maxgram = 0;
        numgrams = 0;
        		        
        /* Parse args */
        for (int i = 0; i < args.length; ++i) {
			if (args[i].equals(ARG_WORDS)) {
                System.out.println("DEBUG: Finding words");
				if (findWords || wordCounter != null) {
					usage();
				}
                findWords = true;
                wordCounter = new WordCounter(Integer.parseInt(args[++i]));
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
                grams[numgrams] = new Stargram(Integer.parseInt(args[++i]),
                                               Integer.parseInt(args[++i]));
                if (grams[numgrams].length() < maxgram) {
                    maxgram = grams[numgrams].length();
                }
                ++numgrams;
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
        filenames = new String[args.length - index];
        
        if (filenames.length == 0) {
            usage();
        }
		
		for (int i = 0; index < args.length; ++index, ++i) {
			if (args[index].charAt(0) == '-') {
				usage();
			}
			filenames[i] = args[index];
		}
        
        if (findGrams) {
            firstgrams = new String[filenames.length];
            lastgrams = new String[filenames.length];
        }

        charCounter = new CharCounter(unicode);

	} // parseArgs
	
    /**
     * print - print the results of the program
     */
	static void print() {
	    System.out.println();
        if (findWords) {
            System.out.println("TOP " + wordCounter.getNumTop() +
                               " WORDS" + NEWLINE);
            String[] topW = wordCounter.top();
            for (String s : topW) {
                System.out.println(s);
            }
            System.out.println(NEWLINE);
        }
        if (findChars) {
            System.out.println("CHAR\tCOUNT\t\tPERCENTAGE" + NEWLINE);
            CharCounter cc = new CharCounter(totalCharCount, totalCharArray);
            System.out.println(cc.toString() + NEWLINE + NEWLINE);            
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
        
        System.out.println((end - start) + " msec");
	} // print
	
    /**
     * readFile - read and parse a file
     */
	static void readFile (String filename) {
	    Scanner sc = null;
        try {
			sc = new Scanner(new File(filename + (char)(rank + 'a')));
                
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
                        wordCounter.add(curr2);
                    }
                }
                if (findChars) {
                    if (unicode) {
                        charCounter.add(curr);
                    }
                    else {
                        // Never include non-alpha characters.
                        charCounter.add(curr.replaceAll("[^a-z]", ""));
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
                
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        } finally {
            if (sc != null) {
                sc.close();
            }
        }
	} // readFile

    /**
     * Gather data to root and reduce all the data sets.
     */
    static void gatherAndReduce () throws Exception {

        if (findChars) {
            charCountArray[rank][0] = charCounter.getCharCount();
            charArrayArray[rank] = charCounter.getCharArray();
        
            world.gather (ROOT, myCharCount, charCounts);
            world.gather (ROOT, myCharArray, charArrays);
            
            if (rank == ROOT) {
                totalCharCount = 0;
                if (unicode) {
                    totalCharArray = new int[UNICODE];
                }
                else {
                    totalCharArray = new int[NOT_UNICODE];
                }
                for (int i = 0; i < size; ++i) {
                    totalCharCount += charCountArray[i][0];
                    ReduceArrays.reduce(charArrayArray[i], charRange,
                                        totalCharArray, charRange, IntegerOp.SUM);
                }
            }
        }
        
        if (findWords) {
            //charCountArray[rank][0] = charCounter.getCharCount();
            //charArrayArray[rank] = charCounter.getCharArray();
            wordMapArray[rank][0] = wordCounter.getMap();
        
            //world.gather (ROOT, myCharCount, charCounts);
            //world.gather (ROOT, myCharArray, charArrays);
            world.gather (ROOT, myMap, mapBufs);
            
            if (rank == ROOT) {
            //    totalCharCount = 0;
                //totalWordMap = new HashMap<String, Integer>();
                totalWordCounter = new WordCounter(wordCounter.getNumTop());
            //    if (unicode) {
            //        totalCharArray = new int[UNICODE];
            //    }
            //    else {
            //        totalCharArray = new int[NOT_UNICODE];
            //    }
                for (int i = 0; i < size; ++i) {
                    totalWordCounter.reduce(wordMapArray[i][0]);
            //        totalCharCount += charCountArray[i][0];
            //        ReduceArrays.reduce(charArrayArray[i], charRange,
            //                            totalCharArray, charRange, IntegerOp.SUM);
                }
            }
        }
        
        if (findGrams) {
            // firstgrams
            // lastgrams
        }
    } // gatherAndReduce
    
    /**
     * Split each file into (size) equal parts.
     */
    static void splitFile (File filename) {
        long filesize = filename.length();
        filesize += filesize % size;
        try {
            Runtime.getRuntime().exec("split -b " + (filesize / size) +
                                      " -a 1 " + filename + " " + filename);
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
    }
    
    /**
     * usage - print the usage statement and exit
     */
    private static void usage() {
        System.out.println(USAGE);
        
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
        
        System.out.println(NEWLINE + "Max number of processes: " + MAX_SIZE);
		
        System.exit(1);
    }

    /**
     * Initialize the word cluster variables.
     */
    private static void wordCluster() {
        //charRange = new Range(0, UNICODE-1);
        //charCountArray = new int[size][];
        wordMapArray = new HashMap<String, Integer>[size][];
        if (rank == ROOT) {
            Arrays.allocate(wordMapArray, 1, totalWordMap.getClass());
            //Arrays.allocate(int[][] charCountArray, 1);
            //Arrays.allocate(charArrayArray, UNICODE);
        }
        else {
            Arrays.allocate(wordMapArray, myrange, 1, totalWordMap.getClass());
            //Arrays.allocate(charCountArray, myrange, 1);
            //Arrays.allocate(charArrayArray, myrange, UNICODE);
        }
        
        mapBufs = ObjectBuf.rowSliceBuffers(wordMapArray, ranges);
        myMap = ObjectBuf.rowSliceBuffer(wordMapArray, myrange);
        //charCounts = IntegerBuf.rowSliceBuffers(charCountArray, ranges);
        //myCharCount = IntegerBuf.rowSliceBuffer(charCountArray, myrange);
        //charArrays = IntegerBuf.rowSliceBuffers(charArrayArray, ranges);
        //myCharArray = IntegerBuf.rowSliceBuffer(charArrayArray, myrange);
    } // wordCluster

} // projclu
