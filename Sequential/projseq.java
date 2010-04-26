/*
 * file: projseq.java
 */

import java.util.Map;
import java.util.TreeMap;

import java.util.Scanner;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * projseq_COMMENT
 *
 * @author dht5977: Daniel Tyler
 * @author cev5122: Christine Viets
 */

public class projseq {
   public static final int NUMLETTERS = 26;
   public static final String USAGE = "java <data-file> [-w] [-c]";

   static Map<String, Integer> wordCounts = new TreeMap<String, Integer>();
   static int[] letterCount = new int[ NUMLETTERS ];
 
   /**
    * main method -- DELETE METHOD IF NOT NEEDED
    *
    * @param    args      command line arguments
    */
   public static void main( String args[] ) {
       if( args.length < 1 ) { System.out.println( USAGE ); return; }
       
       Scanner dataSource = null;
       String current = null;
       int cur = 0;
       int totalCount = 0;
       boolean findWords = false;
       boolean findChar = false;

       if( args.length > 1 ) {
           findWords = args[1].toLowerCase().equals( "-w" );
           findChar = args[1].toLowerCase().equals( "-c" );
       }
       if( args.length > 2 ) {
           findChar = args[2].toLowerCase().equals( "-c" ) ||
                      args[1].toLowerCase().equals( "-c" );
       }

       try {
           dataSource = new Scanner(new BufferedReader(
                       new FileReader( args[0] )));

           if( findWords ) {
               while( dataSource.hasNext() ) {
                   current = dataSource.next();
                   if( wordCounts.get( current ) == null){
                       wordCounts.put( current, 1 );
                   } else {
                       wordCounts.put( current, 
                               wordCounts.get( current ) + 1 );
                   }
               }
           }

           dataSource = new Scanner(new BufferedReader(
                       new FileReader( args[0] )));

           if( findChar ) {
               while( dataSource.hasNext() ){
                   current = dataSource.next().toLowerCase();
                   for( int i = 0; i < current.length(); i++){
                        cur = current.charAt(i);
                        if( cur < 'a' || cur > 'z' ){
                            continue;
                        } else {
                            letterCount[ cur - 'a' ] += 1;
                            totalCount += 1;
                        }
                   }
               }
           }
       } catch( Exception e ){
           System.err.println( e.getClass().getSimpleName() + " " + e.getMessage() );
       } finally {
           if( dataSource != null ) {
               dataSource.close();
           }
       }

       if( findWords ){
           for( String s : wordCounts.keySet() ) {
               System.out.println( s + ", " +  String.valueOf( wordCounts.get( s )) );
           }
       }
       if ( findChar ) {
           for( int i = 0; i < 26; i++ ){
               System.out.println( (char)( i+ 'a') + ", " +
               ((float)letterCount[i] / totalCount)*100 );
           }
       } 
                
   }// main
          
} // projseq
