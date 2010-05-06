/*
 * file: projseq.java
 */

import java.util.Scanner;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * projseq_COMMENT
 *
 * @author dht5977: Daniel Tyler
 * @author cev5122: Christine Viets
 */

public class projseq {

	// System independent newline character
	public static final String NEWLINE = System.getProperty("line.separator");
   //public static final int NUMLETTERS = 26;
   public static final String USAGE = "java projseq <data-files> [-w topN] [-c] [-g n topN]";
   public static final String ARG_WORDS = "-w";
   public static final String ARG_CHARS = "-c";
   public static final String ARG_GRAMS = "-g";

   //static Map<String, Integer> wordCounts = new TreeMap<String, Integer>();
   
   //static int[] letterCount = new int[ NUMLETTERS ];
   
 
   /**
    * main method -- DELETE METHOD IF NOT NEEDED
    *
    * @param    args       command line arguments
	*		    data-files Text files to read.
	*           -w N	   If present, give word frequency and print the top N
	*		    -c         If present, give char frequency
	*           -g n N     If present, give n-gram frequency. Print the top N. Unlimited uses.
    */
   public static void main( String args[] ) {
       if( args.length < 1 ) { System.out.println( USAGE ); return; }
       
		WordCounter wordCount = new WordCounter();
		CharCounter charCount = new CharCounter();
	   
       //Scanner dataSource = null;
       //String current = null;
       int cur = 0;
       int totalCount = 0;
       boolean findWords = false;
	   int topWords = 0;
       boolean findChars = false;
	   boolean findGrams = false;
	   
	   int numfiles = 0; // Number of text files, then counter for files.
	   int numgrams = 0; // Number of *grams, then counter for *gram array.
	   for (int i = 0; i < args.length; ++i) {
			if (args[i].charAt(0) != '-') {
				++numfiles;
			}
			if (args[i].equals(ARG_GRAMS)) {
				++numgrams;
				if (args[++i].charAt(0) < '0' || args[i].charAt(0) > '9') {
					System.out.println(USAGE);
					return;
				}
				if (args[++i].charAt(0) < '0' || args[i].charAt(0) > '9') {
					System.out.println(USAGE);
					return;
				}
			}
			else if (args[i].equals(ARG_WORDS)) {
				if (args[++i].charAt(0) < '0' || args[i].charAt(0) > '9') {
					System.out.println(USAGE);
					return;
				}
			}
		}
	   File[] files = new File[numfiles];
	   numfiles = 0;
	   Stargram[] grams = new Stargram[numgrams];
	   numgrams = 0;

	   for (int i = 0; i < args.length; ++i) {
			if (args[i].charAt(0) != '-') {
				files[numfiles++] = new File(args[i]);
			}
			else if (args[i].equals(ARG_WORDS)) {
				findWords = true;
				topWords = Integer.parseInt(args[++i]);
			}
			else if (args[i].equals(ARG_CHARS)) {
				findChars = true;
			}
			else if (args[i].equals(ARG_GRAMS)) {
				findGrams = true;
				grams[numgrams++] = new Stargram(Integer.parseInt(args[++i]),
												 Integer.parseInt(args[++i]));
			}
			else {
				System.out.println(USAGE);
				return;
			}
	   }

	   for (int i = 0; i < files.length; ++i) {
			Scanner sc = null;
			try {
			
				sc = new Scanner(files[i]);
				
				while (sc.hasNext()) {
					String curr = sc.next().toLowerCase().replaceAll("[^a-z]", "");
					if (findWords) {
						wordCount.add(curr);
					}
					if (findChars) {
						charCount.add(curr);
					}
					if (findGrams) {
						for (Stargram s : grams) {
							s.add(curr);
						}
					}
				}
				
				sc.close();
				
			} catch( FileNotFoundException e ){
				System.err.println(e.getMessage());
			} finally {
				if( sc != null ) {
					sc.close();
				}
			}
		}

       if (findWords) {
			System.out.println("TOP " + topWords + " WORDS" + NEWLINE);
			String[] topW = wordCount.top(topWords);
			for (String s : topW) {
				System.out.println(s);
			}
			System.out.println(NEWLINE);
	   
//           for( String s : wordCounts.keySet() ) {
  //             System.out.println( s + ", " +  String.valueOf( wordCounts.get( s )) );
    //       }
       }
       if (findChars) {
			System.out.println("CHAR\tCOUNT\t\tPERCENTAGE" + NEWLINE);
			System.out.println(charCount.toString() + NEWLINE + NEWLINE);
	   
//           for( int i = 0; i < 26; i++ ){
//               System.out.println( (char)( i+ 'a') + ", " +
               //((float)letterCount[i] / totalCount)*100 );
           //}
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
                
   }// main
          
} // projseq
