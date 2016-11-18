
package midireader.processingXmk;

import midireader.auxClasses.basicTransformations;
import java.util.ArrayList;
import midireader.processingHumdrumMelisma.chordMaker;

public class MeasureAnalyzer {
    
    //returns similarity between two measures, taking into account notes, intervals, interval directions, and rhythm
    //parts based on possible inputs to ThemeFinder
    public static double getOverallSimilarity(ArrayList<float[]> song, int measurenum1, int measurenum2, float GCD) {
        ArrayList<float[]> measure1 = basicTransformations.getHalfMeasure(song,measurenum1);
        ArrayList<float[]> measure2 = basicTransformations.getHalfMeasure(song,measurenum2);
        return (double)(0.4*rhythmSimilarity(measure1,measure2, measurenum1, measurenum2, GCD) +
                0.3*intervalSimilarity(measure1,measure2) +
                0.1*directionalSimilarity(measure1,measure2,false) +
                0.1*directionalSimilarity(measure1,measure2,true) + //directions with small/large jump distinction
                0.1*simpleMelSimilarity(measure1,measure2));
    }
    
    //converts an edit distance to a similarity percentage
    public static double distToSim(double distance, double maxdistance) {
        return 1 - distance/maxdistance;
    }
    
    //returns edit distance similarity between two measures based on their I/O rhythms
    public static double rhythmSimilarity(ArrayList<float[]> measure1, ArrayList<float[]> measure2, int measurenum1, int measurenum2, float GCD) {
        String int1 = getRhythm(measure1,measurenum1,GCD);
        String int2 = getRhythm(measure2,measurenum2,GCD);
        double output = distToSim(minDistance(int1,int2),16);
        //System.out.println(output);
        return output;
    }
    
    public static double rhythmSimilarity(String measure1, String measure2) {
        double output = distToSim(minDistance(measure1,measure2),16);
        return output;
    }
    
    //given measure, returns the I/O rhythm, even if polyphonic
    public static String getRhythm(ArrayList<float[]> measure, int measurenum, float GCD) {
        String output = "";
        char newchar;
        int curplace = 0;
        float measurestart = GCD*measurenum*16;
        for (int i=0; i<16; i++) {
            newchar = 'O';
            for (int j=curplace; j<measure.size(); j++) {
                if (measure.get(j)[1]-measurestart >= i*GCD && measure.get(j)[1]-measurestart < (i+1)*GCD) {
                    newchar = 'I';
                    curplace = j;
                    break;
                }
            }
            output += newchar;
        }
        //System.out.println(output);
        return output;
    }
    //returns edit distance similarity between two measures based on their intervals
    public static double intervalSimilarity(ArrayList<float[]> measure1, ArrayList<float[]> measure2) {
        String int1 = getIntervals(measure1);
        String int2 = getIntervals(measure2);
        double output = distToSim(minDistance(int1,int2),Math.max(measure1.size()-1,measure2.size()-1));
        //System.out.println(output);
        return output;
    }
    public static String getIntervals(ArrayList<float[]> measure) {
        String output = "";
        float curnote, nextnote, curint;
        for (int i=0; i<measure.size()-1; i++) {
            curnote = measure.get(i)[0]%12;
            nextnote = measure.get(i+1)[0]%12;
            curint = (int)(nextnote-curnote);
            while (curint < 0) {
                curint += 13;
            }
            output += (char)(curint + '0');
        }
        //System.out.println(output);
        return output;
    }
    
    //returns edit distance similarity between two measures based on their interval directions (up, down, same); withBig distinguishes >2 half step jumps
    public static double directionalSimilarity(ArrayList<float[]> measure1, ArrayList<float[]> measure2, boolean withBig) {
        String int1 = getDirections(measure1,withBig);
        String int2 = getDirections(measure2,withBig);
        double output = distToSim(minDistance(int1,int2),Math.max(measure1.size()-1,measure2.size()-1));
        //System.out.println(output);
        return output;
    }
    
    public static String getDirections(ArrayList<float[]> measure, boolean withBig) {
        String output = "";
        float curnote, nextnote;
        for (int i=0; i<measure.size()-1; i++) {
            curnote = measure.get(i)[0];
            nextnote = measure.get(i+1)[0];
            if (nextnote > curnote) {
                if (withBig && nextnote > curnote+2) {
                    output += "U";
                }
                else {
                    output += "u";
                }
            }
            else if (nextnote < curnote) {
                if (withBig && nextnote < curnote-2) {
                    output += "D";
                }
                else {
                    output += "d";
                }
            }
            else {
                output += "s";
            }
        }
        //System.out.println(output);
        return output;
    }
    
    //returns edit distance similarity between two measures based on their notes' pitches
    public static double simpleMelSimilarity(ArrayList<float[]> measure1, ArrayList<float[]> measure2) {
        String int1 = getPitches(measure1);
        String int2 = getPitches(measure2);
        double output = distToSim(minDistance(int1,int2),Math.max(measure1.size()-1,measure2.size()-1));
        //System.out.println(output);
        return output;
    }
    
    public static String getPitches(ArrayList<float[]> measure) {
        String output = "";
        float curnote;
        for (int i=0; i<measure.size(); i++) {
            curnote = measure.get(i)[0]%12;
            output += (char)(curnote + '0');
        }
        //System.out.println(output);
        return output;
    }
    
    //minimum edit distance between two strings
    public static int minDistance(String word1, String word2) {
	int len1 = word1.length();
	int len2 = word2.length();
	int[][] dp = new int[len1 + 1][len2 + 1];
	for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }
	for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }
	for (int i = 0; i < len1; i++) {
            char c1 = word1.charAt(i);
            for (int j = 0; j < len2; j++) {
                char c2 = word2.charAt(j);
                if (c1 == c2) {
                    dp[i + 1][j + 1] = dp[i][j];
                } else {
                    int replace = dp[i][j] + 1;
                    int insert = dp[i][j + 1] + 1;
                    int delete = dp[i + 1][j] + 1;
                    int min = replace > insert ? insert : replace;
                    min = delete > min ? min : delete;
                    dp[i + 1][j + 1] = min;
                }
            }
        }
	return dp[len1][len2];
    }
    
    public static ArrayList<String[]> measureFrequencies(ArrayList<String> patterns)  {
        ArrayList<String[]> output = new ArrayList();
        for (int i=0; i<patterns.size(); i++) {
            int found = -1;
            for (int j=0; j<output.size(); j++) {
                if (found == -1 && output.get(j)[0].equals(patterns.get(i))) {
                    found = j;
                }
            }
            if (found == -1) {
                String[] temp = {patterns.get(i),"1"};
                output.add(temp);
            }
            else {
                String[] temp = {output.get(found)[0],Float.toString(Float.valueOf(output.get(found)[1]) + 1)};
                output.set(found,temp);
            }
        }
        for (int j=0; j<output.size(); j++) {
            System.out.println(output.get(j)[0] + " " + output.get(j)[1]);
        }
        return output;
    }
    
    //returns an array of the format <n1, length(0), ... , length(n1-1), n2...>
    public static ArrayList<Float> patternNums(ArrayList<float[]> notes, float GCD, String pattern, float start) {
        /*
        System.out.println("DEBUG------------------------------------------");
        chordMaker.printF(notes);
        System.out.println("GCD = "+ GCD);
        System.out.println("Pattern = "+ pattern);
        System.out.println("Start = "+ start);
        */
        
        
        ArrayList<Float> output = new ArrayList();
        for (int i=0; i<16; i++) {
            if (pattern.charAt(i) == 'I') {
                ArrayList<Float> temp = new ArrayList();
                for (int j=0; j<notes.size(); j++) {
                    if (notes.get(j)[1]-start >= i*GCD && notes.get(j)[1]-start <= (i+1)*GCD) {
                        //System.out.println(notes.get(j)[1]-start + " " + i*GCD);
                        temp.add(notes.get(j)[2]-notes.get(j)[1]);
                    }
                }
                output.add((float)temp.size());
                output.addAll(temp);
                //System.out.print(pattern.charAt(i) +" ");
            }
        }
        /*
        System.out.println("OutputLength = "+ output.size());
        System.out.println("Output = "+ output);
        System.out.println("DEBUG//----------------------------------------");
        */
        return output;
    }
    
    //runs LHL-p metric described in http://ismir2012.ismir.net/event/papers/283_ISMIR_2012.pdf and https://www.jstor.org/stable/40285271?seq=1
    public static int LHL(String pattern) {
        int output = 0;
        int weight[] = { 5,1,2,1,3,1,2,1,4,1,2,1,3,1,2,1};
        for (int i=0; i<pattern.length(); i++) {
            if (pattern.charAt(i) == 'O') {
                int previousi = i;
                for (int j=i; j>=0; j--) {
                    if (j > 0 && pattern.charAt(j-1) == 'I') {
                        previousi = j-1;
                        break;
                    }
                    else if (j==0) {
                        previousi = 15;
                        break;
                    }
                }
                
                //System.out.println(weight[previousi% 16] + " " + weight[i % 16] + " " + (weight[i %16] - weight[previousi % 16]));
                //if (weight[i% 16] - weight[previousi % 16] > 0) {
                    output += weight[i %16] - weight[previousi % 16];
                //}
            }
        }
        //System.out.println(output);
        return output;
    }
    
    //returns the total distance that onsets move between two patterns
    public static int onsetDistance(String pattern1, String pattern2) {
        int output = 0;
        for (int i=0; i<pattern1.length() - pattern1.replace("I", "").length(); i++) {
            output += Math.abs(findI(pattern1,i)-findI(pattern2,i));
            //System.out.println(findI(pattern1,i) + " " + findI(pattern2,i));
        }
        //System.out.println(pattern1 + " " + pattern2 + " " + output);
        return output;
        
    }
    
    //returns position of nth I in a string
    public static int findI(String pattern, int n) {
        int output = 0;
        int numfound = 0;
        for (int i=0; i<16; i++) {
            if (pattern.charAt(i) == 'I') {
                if (numfound == n) {
                    output = i;
                    break;
                }
                numfound++;
            }
        }
        return output;
    }
    
}
