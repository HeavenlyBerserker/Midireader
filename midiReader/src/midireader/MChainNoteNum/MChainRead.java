/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package midireader.MChainNoteNum;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
    
     public static ArrayList<String[]> readTable(String filename, boolean b) throws IOException{
         ArrayList<String[]> chainByLength = new ArrayList();
        
        // This will reference one line at a time
        String line = null;
        int[] sum1 = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader = 
                new FileReader(filename);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = 
                new BufferedReader(fileReader);
            
            
            
            int count = 0;
            while((line = bufferedReader.readLine()) != null) {
                if(count >= 1){
                    String[] values = line.split(",");
                    int sum = 0;
                    for(int i = 1; i < values.length; i++){
                        String[] trans = values[i].split("\\(");
                        //System.out.println(trans[1]);
                        if (!trans[1].substring(0, trans[1].length()-1).equals(""))sum += Integer.parseInt(trans[1].substring(0, trans[1].length()-1));
                    }
                    //System.out.println("///////////////" + sum);
                    String [] f = {CountIs(Integer.parseInt(values[0])), Integer.toString(sum),KeytoIO(Integer.parseInt(values[0]))};
                    sum1[Integer.parseInt(f[0])] += sum;
                    chainByLength.add(f);
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
        
        for(int i = 0; i < chainByLength.size() ; i ++){
            String [] str = chainByLength.get(i);
            //System.out.println(str[1] + " divides " + sum1);
            str[1] = String.valueOf(Float.parseFloat(str[1])/sum1[Integer.parseInt(str[0])]);
            chainByLength.set(i, str);
        }
        
        float[] f = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}; 
        for(int i = 0; i < chainByLength.size() ; i ++){
            String [] str = chainByLength.get(i);
            //System.out.println(str[1] + " divides " + sum1);
            f[Integer.parseInt(str[0])] += Float.parseFloat(str[1]);
            str[1] = Float.toString(f[Integer.parseInt(str[0])]);
            chainByLength.set(i, str);
        }
        
        return chainByLength;
     }
     
     private static String KeytoIO(int key){
        String s = "";
        
        String number = Integer.toBinaryString(key);
        
        //System.out.println("BinaryString [" + number + "]");
        
        for(int i = 0; i < number.length(); i++){
            if(number.charAt(i) == '1')
                s += "I";
            else
                s += "O";
        }
        
        while(s.length() < 16){
            s = "O" + s;
        }
        return s;
    }
     
     private static String CountIs(int key){
        String s = "";
        
        String number = Integer.toBinaryString(key);
        
        //System.out.println("BinaryString [" + number + "]");
        int c = 0;
        
        for(int i = 0; i < number.length(); i++){
            if(number.charAt(i) == '1'){
                s += "I";
                c++;
            }
            else
                s += "O";
        }
        
        while(s.length() < 16){
            s = "O" + s;
        }
        s = Integer.toString(c);
        return s;
    }
}
