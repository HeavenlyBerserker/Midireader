
package midireader.processingXmk;

public class syncopalooza {
    
    public static String desynch(String pattern) {
        int shifted = 1;
        int weight[] = {5,1,2,1,3,1,2,1,4,1,2,1,3,1,2,1};
        while (shifted == 1) {
            shifted = 0;
            System.out.println(pattern);
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
        String transforms = pattern;
        while (shifted == 1) {
            shifted = 0;
            System.out.println(pattern);
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
}
