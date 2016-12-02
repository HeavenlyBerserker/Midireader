/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package midireader.MChainNoteNum;

import java.io.IOException;
import java.util.ArrayList;
import javax.sound.midi.InvalidMidiDataException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.*;

/**
 *
 * @author domini
 */
public class MChainRead {
    public static ArrayList<float[]> [][] readChainOutput(String filename, boolean b) throws IOException{
        ArrayList<float[]> [][] chainByLength = new ArrayList[17][17];
        
        for(int i = 0; i < 17; i++){
            for(int j = 0; j < 17; j++){
                chainByLength[i][j] = new ArrayList<>();
            }
        } 

        // This will reference one line at a time
        String line = null;

        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader = 
                new FileReader(filename);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = 
                new BufferedReader(fileReader);
            
            int count = 0;
            
            while((line = bufferedReader.readLine()) != null) {
                if(count >0){
                    String[] values = line.split(",");
                    int t1 = Integer.parseInt(values[0]), t2 = Integer.parseInt(values[1]);
                    for(int i = 2; i < values.length; i++){
                        String[] trans = values[i].split("=");
                        float [] f = {Float.parseFloat(trans[0]), Float.parseFloat(trans[1])}; 
                        chainByLength[t1][t2].add(f);
                    }
                }
                count++;
            }

            // Always close files.
            bufferedReader.close();         
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                "Unable to open file '" + 
                filename + "'");                
        }
        catch(IOException ex) {
            System.out.println(
                "Error reading file '" 
                + filename + "'");                  
            // Or we could just do this: 
            // ex.printStackTrace();
        }
        
        if(b){
            for(int i = 0; i < 17; i++){
                for(int j = 0; j < 17; j++){
                    System.out.print("[" + i + "][" + j + "]");
                    for(int k = 0; k < chainByLength[i][j].size(); k++){
                        float []f = chainByLength[i][j].get(k);
                        System.out.print("[" + f[0] + "=" + f[1] + "]");
                    }
                    System.out.println();
                }
            } 
        }
        
        return chainByLength;
    }
}
