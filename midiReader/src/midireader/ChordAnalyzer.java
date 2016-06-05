/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package midireader;

import java.util.ArrayList;

import java.io.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
/**
 *
 * @author Hong
 */
public class ChordAnalyzer {
    
    public static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    
    
    //Read Kernfile and figure out the Chords
    public static ArrayList<float[]> readFile(ArrayList<float[]> chords, String filename){
    
        String line = null;
        
        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader = 
                new FileReader(filename);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = 
                new BufferedReader(fileReader);
            boolean done = false;
            while((line = bufferedReader.readLine()) != null && done == false) {
                System.out.println(line.substring(0,3));
                if(line.substring(0,3).equals("*k[")){
                    System.out.println(line);
                }
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
        
        return chords;
    }
    
    public static ArrayList<float[]> chordNotes(ArrayList<float[]> notes, String filename, float quarter){
        readFile(notes, filename);
        return notes;
    }
}
