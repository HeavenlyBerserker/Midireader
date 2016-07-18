/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package midireader;

import java.io.File;
import java.util.ArrayList;
import javax.sound.midi.MetaMessage;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Track;

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
 * @author domini
 */

//xmRead(1)
//input: filename .xm
//output: returns the notes in the file
public class xmReader {
    public static ArrayList<float[]> xmTakeIn(String filename){
        String line = null;
        ArrayList<float[]> notes = new ArrayList();
         try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader = 
                new FileReader(filename);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            
            int[] timeSig = {0, 0, 0};
            
            //Figure out time signature (num, denom) and bpm
            line = bufferedReader.readLine();
            String temp = "";
            int i = 1;
            while(line.length() > i && Character.isDigit(line.charAt(i))){
                temp += line.charAt(i);
                i++;
            }
            //System.out.println(line);
            timeSig[0] = Integer.parseInt(temp);
            temp = "";
            i += 2;
            while(line.length() > i && Character.isDigit(line.charAt(i))){
                temp += line.charAt(i);
                i++;
            }
            timeSig[1] = Integer.parseInt(temp);
            temp = "";
            i += 2;
            while(line.length() > i && Character.isDigit(line.charAt(i))){
                temp += line.charAt(i);
                i++;
            }
            timeSig[2] = Integer.parseInt(temp);
            System.out.println("Time sig and bpm = " + timeSig[0] + "/" + timeSig[1] + " at " + timeSig[2] );
            
            
            
            //-------------------------------------------------------------------------------------------------------
            //Substantial content
            //-------------------------------------------------------------------------------------------------------
            int measure = 1;
            while((line = bufferedReader.readLine()) != null) {
                if(line.charAt(0) == '='){
                    float[] arr = {-2, measure};
                    notes.add(arr);
                    measure++;
                }
                else{
                    /*
                    i = 1;
                    while(line.length() > i && (Character.isDigit(line.charAt(i)) || line.charAt(i) == '-')){
                        temp += line.charAt(i);
                        i++;
                    }
                    //System.out.println(line);
                    int num = Integer.parseInt(temp);
                    temp = "";
                    i += 2;
                    }
                    while(line.length() > i && (Character.isDigit(line.charAt(i)) || line.charAt(i) == '-')){
                        temp += line.charAt(i);
                        i++;
                    }
                    int den = Integer.parseInt(temp);
                    temp = "";
                    
                    while(line.length() > i && line.charAt(i) != '\t') i++;
                    i++;
                    while(line.length() > i && (Character.isDigit(line.charAt(i)) || line.charAt(i) == '-')){
                        temp += line.charAt(i);
                        i++;
                    }
                    int note = Integer.parseInt(temp);
                    while(line.length() > i && line.charAt(i) != '\t') i++;
                    i += 2;
                    List<Integer> l = new ArrayList<Integer>();
                    while(line.length() > i){
                        while(line.length() > i && (Character.isDigit(line.charAt(i)) || line.charAt(i) == '-')){
                            temp += line.charAt(i);
                            i++;
                        }
                        l.add(Integer.parseInt(temp));
                        i += 2;
                    }*/
                    
                }
            }
            //-------------------------------------------------------------------------------------------------------
            //Substantial content end
            //-------------------------------------------------------------------------------------------------------
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
        return notes;
    }
    
    //#Main
    public static ArrayList<float[]> xmRead(String filename){
        ArrayList<float[]> notes = new ArrayList();
        notes = xmTakeIn(filename);
        return notes;
    }
}
