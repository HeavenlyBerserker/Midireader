
package midireader.processingXmk;


import java.util.ArrayList;
import static midireader.XmkMain.MEASURES;

public class syncopalooza {
    
    public static String desynch(String pattern) {
        int shifted = 1;
        int weight[] = {5,1,2,1,3,1,2,1,4,1,2,1,3,1,2,1};
        while (shifted == 1) {
            shifted = 0;
            //System.out.println(pattern);
            for (int i=0; i<pattern.length() - pattern.replace("I", "").length(); i++) {
                int pos = MeasureAnalyzer.findI(pattern, i);
                int posweight = weight[pos];
                int curpos = pos+1;
                while (curpos < 16) {
                    if (pattern.charAt(curpos) == 'I') {
                        break;
                    }
                    else if (weight[curpos] == posweight+1) {
                        char[] patternchars = pattern.toCharArray();
                        patternchars[pos] = 'O';
                        patternchars[curpos] = 'I';
                        pattern = String.valueOf(patternchars);
                        shifted = 1;
                        break;
                    }
                    curpos++;
                }
            }
        }
        
        return pattern;
    }
    
    public static String resynch(String pattern) {
        int shifted = 1;
        int weight[] = {5,1,2,1,3,1,2,1,4,1,2,1,3,1,2,1};
        int minweight = 2;
        int maxweight = 3;
        for (int i=0; i<16; i++) {
            if (weight[i] > maxweight) {
                weight[i] = maxweight;
            }
            else if (weight[i] < minweight) {
                weight[i] = 0;
            }
        }
        String transforms = pattern;
        while (shifted == 1) {
            shifted = 0;
            //System.out.println(pattern);
            for (int i=0; i<pattern.length() - pattern.replace("I", "").length(); i++) {
                int pos = MeasureAnalyzer.findI(pattern, i);
                int posweight = weight[pos];
                int curpos = pos-1;
                while (curpos >=0) {
                    if (transforms.charAt(curpos) == 'I') {
                        break;
                    }
                    else if (weight[curpos] == posweight-1) {
                        char[] patternchars = pattern.toCharArray();
                        patternchars[pos] = 'O';
                        patternchars[curpos] = 'I';
                        pattern = String.valueOf(patternchars);
                        
                        char[] transformschars = transforms.toCharArray();
                        transformschars[curpos] = 'I';
                        transforms = String.valueOf(transformschars);
                        
                        shifted = 1;
                        break;
                    }
                    curpos--;
                }
            }
        }
        
        return pattern;
    }
    
public static ArrayList<String> makeRules(ArrayList<String> patterns) {
        ArrayList<String> rules = new ArrayList();
        int startingnum = 0;
        
        for (int i=0; i<MEASURES; i++) {
            int size = (patterns.get(i).length() - patterns.get(i).replace("I", "").length());
            int flag = 0;
            for (int j=0; j<rules.size(); j++) {
                if (patterns.get(i).equals( (rules.get(j)).substring(0,16) )) {
                    flag = 1;
                }
            }
            if (flag == 0) {
                rules.add(patterns.get(i) + " " + resynch(desynch(patterns.get(i))));
            }
        }
        return rules;
    }

//returns a rhythm string that is chance*string2 and (1-chance)*string1
public static String randomlyChange(String string1, String string2, double chance) {
        String output = "";
        int curplace = 0;
        int ind;
        for (int i=0; i<string1.length() - string1.replace("I", "").length(); i++) {
            double randomnum = Math.random();
            if (Math.random() > chance) {
                ind = indexOf(string1,"I",i+1);
            }
            else {
                ind = indexOf(string2,"I",i+1);
            }
            while (curplace < ind) {
                output += "O";
                curplace++;
            }
            output += "I";
            curplace++;
            
            
        }
        while (curplace < 16) {
            output += "O";
            curplace++;
        }
        System.out.println(output);
        return output;
    }

public static int indexOf(String haystack, String needle, int ordinal) {
    try {
        return haystack.length() - haystack.split(needle, ordinal + 1)[ordinal].length() - 1;
    } catch (ArrayIndexOutOfBoundsException e) {
        return -1;
    }
}
}