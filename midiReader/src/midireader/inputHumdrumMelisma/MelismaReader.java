package midireader.inputHumdrumMelisma;

import java.util.ArrayList;
import java.io.IOException;
import java.nio.charset.Charset;
import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import midireader.XmkMain;

public class MelismaReader {
   
    public static List<String> getFileNames(List<String> fileNames, Path dir) {
        System.out.println(dir);
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path path : stream) {
                if(path.toFile().isDirectory()) {
                    getFileNames(fileNames, path);
                } else {
                    if(path.toAbsolutePath().toString().contains(".notelist"))
                        fileNames.add(path.toAbsolutePath().toString());
                    //System.out.println(path.getFileName());
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        } 
        return fileNames;
    } 
    
    public static ArrayList<float[]> readFile(String filename) throws IOException {
        FileInputStream in = null;
        ArrayList<float[]> output = new ArrayList();

        String line;

        try (
            InputStream fis = new FileInputStream(filename);
            InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
            BufferedReader br = new BufferedReader(isr);
        ) {
            while ((line = br.readLine()) != null) {
                String[] words = line.split("\\s+");
                //System.out.println(words[0]);
                if (words[0].equals("Note")) {
                    float[] notey = {Float.parseFloat(words[3])+12,Float.parseFloat(words[1]),Float.parseFloat(words[2])};
                    //System.out.println(Float.toString(notey[0]) + " " +Float.toString(notey[1]) + " " + Float.toString(notey[2]));
                    output.add(notey);
                }
                else if (words[1].equals("Tempo")) {
                    XmkMain.MM = Float.parseFloat(words[2]);
                }
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