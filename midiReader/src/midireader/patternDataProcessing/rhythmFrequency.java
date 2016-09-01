
package midireader.patternDataProcessing;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import midireader.XmkMain;

public class rhythmFrequency {
    
    //returns an arraylist of strings of the form {num of I's, frequency, pattern} from csv data
    public static ArrayList<String[]> readFile(String filename) throws IOException {
        FileInputStream in = null;
        ArrayList<String[]> output = new ArrayList();

        String line;
        

        try (
            InputStream fis = new FileInputStream(filename);
            InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
            BufferedReader br = new BufferedReader(isr);
        ) {
            while ((line = br.readLine()) != null) {
                
                String[] thisline = line.split(",");
                String pattern = "";
                for (int i=0; i<16; i++) {
                    pattern+= thisline[i+3].replace("[", "");
                }
                //System.out.println(pattern);
                int found = -1;
                for (int i=0; i<output.size(); i++) {
                    if (found == -1 && output.get(i)[2].equals(pattern)) {
                        found = i;
                    }
                }
                XmkMain.lines[pattern.length() - (pattern.replace("1", "")).length()]++;
                
                if (found == -1) {
                    String[] temp = {Integer.toString(pattern.length() - (pattern.replace("1", "")).length()),"1",pattern};
                    output.add(temp);
                }
                else {
                    String[] temp = {output.get(found)[0],Float.toString(Float.valueOf(output.get(found)[1]) + 1),output.get(found)[2]};
                    output.set(found,temp);
                }
                //System.out.println(pattern);
            }
        }
        finally {
            if (in != null) {
                in.close();
            }
        }
        
        for (int i=0; i<output.size(); i++) {
            String[] temp = {output.get(i)[0],String.format("%.12f", (Float.valueOf(output.get(i)[1]) / XmkMain.lines[ Integer.parseInt(output.get(i)[0])] )),output.get(i)[2]};
            output.set(i,temp);
        }
        
        Collections.sort(output,new Comparator<String[]>() {
            @Override
            public int compare(String[] strings, String[] otherStrings) {
                return new Float(Float.parseFloat(otherStrings[1])).compareTo(Float.parseFloat(strings[1]));
            }
        });
        Collections.sort(output,new Comparator<String[]>() {
            @Override
            public int compare(String[] strings, String[] otherStrings) {
                return new Float(Float.parseFloat(strings[0])).compareTo(Float.parseFloat(otherStrings[0]));
            }
        });
        
        for (int i=0; i<output.size(); i++) {
            //System.out.println(output.get(i)[0] + " " + output.get(i)[1] + " " + output.get(i)[2]);
        }
        
        return output;
    }
    
    //changes from 1/0 to I/O
    public static ArrayList<String[]> changeToIO(ArrayList<String[]> patterns) {
        ArrayList<String[]> output = new ArrayList();
        for (int i=0; i<patterns.size(); i++) {
            String[] temp = {patterns.get(i)[0], patterns.get(i)[1], patterns.get(i)[2].replace("1", "I").replace("0", "O")};
            output.add(temp);
        }
        
        return output;
    }

}