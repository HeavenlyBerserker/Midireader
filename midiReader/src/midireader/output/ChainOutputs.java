/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package midireader.output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author domini
 */
public class ChainOutputs {
    public int[] MCOutput(ArrayList<float[]> [][] chain, String filename, String comments){
         int [] data = new int[10];
         StringBuilder content = new StringBuilder(filename);
         
         
         try {

                File file = new File("output/" +filename + ".csv");

                // if file doesnt exists, then create it
                if (!file.exists()) {
                        file.createNewFile();
                }

                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bw1 = new BufferedWriter(fw);
                bw1.write(content.toString());
                bw1.close();
                FileWriter fw2 = new FileWriter(file.getAbsoluteFile(), true);
                BufferedWriter bw = new BufferedWriter(fw2);
                content.delete(0, content.length());
                
                bw.write(filename + ": " + comments + "\n");
                
                for(int i = 0; i < 17; i++){
                    for(int j = 0; j < 17; j++){
                        content.append(i + "," + j + ",");
                        for(int k = 0; k < chain[i][j].size(); k++){
                            float[] temp = {chain[i][j].get(k)[0], chain[i][j].get(k)[1]};
                            content.append(temp[0] + "=" + temp[1] + ",");
                        }
                        content.append("\n");
                        bw.write(content.toString());
                        content.delete(0, content.length());
                    }
                 } 
                
                bw.close();

                System.out.println("Chains Note Numbers Done");

        } catch (IOException e) {
                e.printStackTrace();
        }
        
         return data;
    } 
}
