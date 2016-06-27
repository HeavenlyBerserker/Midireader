package midireader;

import java.util.ArrayList;
import java.io.IOException;
import java.nio.charset.Charset;
import java.io.*;

public class RhythmReader {
    
    //returns an arraylist of strings of the form {# of onsets, probability, pattern}
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
                String[] words = line.split("\\s+");
                //System.out.println(words[0]);
                String[] notey = {Integer.toString(words[1].length() - (words[1].replace("I", "")).length()),words[0],words[1]};
                //System.out.println(notey[0] + " " +notey[1] + " " +notey[2]);
                output.add(notey);
            }
        }
        finally {
            if (in != null) {
                in.close();
            }
        }
        return output;
    }
}