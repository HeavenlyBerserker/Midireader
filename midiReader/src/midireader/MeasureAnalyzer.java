
package midireader;

import java.util.ArrayList;

public class MeasureAnalyzer {
    
    //returns similarity between two measures, taking into account notes, intervals, interval directions, and rhythm
    //parts based on possible inputs to ThemeFinder
    public static double getOverallSimilarity(ArrayList<float[]> song, int measurenum1, int measurenum2, float GCD) {
        ArrayList<float[]> measure1 = MidiReader.getHalfMeasure(song,measurenum1);
        ArrayList<float[]> measure2 = MidiReader.getHalfMeasure(song,measurenum2);
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
        System.out.println(output);
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
            curnote = measure.get(i)[0]%13;
            nextnote = measure.get(i+1)[0]%13;
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
            curnote = measure.get(i)[0]%13;
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
}
