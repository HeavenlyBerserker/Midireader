
package midireader;

import java.util.ArrayList;

public class MeasureAnalyzer {
    
    //returns similarity between two measures, taking into account notes, intervals, interval directions, and rhythm
    public static double getOverallSimilarity(ArrayList<float[]> measure1, ArrayList<float[]> measure2) {
        return (double)(0.4*rhythmSimilarity(measure1,measure2) +
                0.2*intervalSimilarity(measure1,measure2) +
                0.2*directionalSimilarity(measure1,measure2) +
                0.2*simpleMelSimilarity(measure1,measure2));
    }
    
    //converts an edit distance to a similarity percentage
    public static double distToSim(float distance, int size) {
        return 1 - (1/(double)size)*(double)distance;
    }
    
    public static double rhythmSimilarity(ArrayList<float[]> measure1, ArrayList<float[]> measure2) {
        
        return 0;
    }
    
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
    
    public static double directionalSimilarity(ArrayList<float[]> measure1, ArrayList<float[]> measure2) {
        String int1 = getDirections(measure1);
        String int2 = getDirections(measure2);
        double output = distToSim(minDistance(int1,int2),Math.max(measure1.size()-1,measure2.size()-1));
        //System.out.println(output);
        return output;
    }
    
    public static String getDirections(ArrayList<float[]> measure) {
        String output = "";
        float curnote, nextnote;
        for (int i=0; i<measure.size()-1; i++) {
            curnote = measure.get(i)[0];
            nextnote = measure.get(i+1)[0];
            if (nextnote > curnote) {
                output += "u";
            }
            else if (nextnote < curnote) {
                output += "d";
            }
            else {
                output += "s";
            }
        }
        //System.out.println(output);
        return output;
    }
    
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
        //System.out.println("Edit distance: " + dp[len1][len2]);
	return dp[len1][len2];
    }
}
