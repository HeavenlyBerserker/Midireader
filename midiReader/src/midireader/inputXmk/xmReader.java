/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package midireader.inputXmk;

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
            
            //Figure out time signature
            float[] timeSig = {0, 0, 0};
            
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
            //System.out.println("Time sig and bpm = " + timeSig[0] + "/" + timeSig[1] + " at " + timeSig[2] );
            notes.add(timeSig);
            
            
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
                    //System.out.println(line);
                    temp = "";
                    i = 0;
                    while(line.length() > i && (Character.isDigit(line.charAt(i)) || line.charAt(i) == '-')){
                        temp += line.charAt(i);
                        i++;
                    }
                    
                    
                    int num = Integer.parseInt(temp);
                    temp = "";
                    i += 1;
                    
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
                    i += 1;
                    
                    //Reading chords---------------------------------------------------------------
                    if(line.length() > i && line.charAt(i) == '['){
                        i++;
                        List<Integer> l = new ArrayList<Integer>();
                        while(line.length() > i){
                            temp = "";
                            while(line.length() > i && (Character.isDigit(line.charAt(i)) || line.charAt(i) == '-')){
                                temp += line.charAt(i);
                                i++;
                            }
                            l.add(Integer.parseInt(temp));
                            i += 2;
                        }
                        //System.out.print(num + ", " + den + ", " + note + ",");
                        //printListI(l);
                        float[] lline = new float[3+l.size()];
                        lline[0] = num;
                        lline[1] = den;
                        lline[2] = note;
                        for(int j = 0; j < l.size(); j++){
                            lline[j+3] = l.get(j);
                        }
                        notes.add(lline);
                    }
                    else{
                        //System.out.println(line);
                        //System.out.println(line.charAt(i));
                        temp = "";
                        while(line.length() > i && (Character.isDigit(line.charAt(i)) || line.charAt(i) == '-')){
                            temp += line.charAt(i);
                            i++;
                        }
                        int key = Integer.parseInt(temp);
                        //m, minor; 7, seventh; 6, sixth; d, diminished; a, aug; 2, sus 2; 4, sus 4;
                        int m = 0, sev = 0, six = 0, dim = 0, aug = 0, sus2 = 0, sus4 = 0;
                        if(line.length() > i && line.charAt(i) == '['){
                            i++;
                            while(line.length() > i && line.charAt(i) != ']'){
                                if(line.charAt(i) == 'm') m = 1; 
                                else if(line.charAt(i) == '7') sev = 1; 
                                else if(line.charAt(i) == '6') six = 1; 
                                else if(line.charAt(i) == 'd') dim = 1; 
                                else if(line.charAt(i) == 'a') aug = 1; 
                                else if(line.charAt(i) == '2') sus2 = 1; 
                                else if(line.charAt(i) == '4') sus4 = 1; 
                                i++;
                            }
                        }
                        float[] lline = new float[3+3+sev+six];
                        lline[0] = num;
                        lline[1] = den;
                        lline[2] = note;
                        lline[3] = key;
                        if(sev == 1) {
                            lline[6] = key + 10;
                            if(six == 1) lline[7] = key + 8;
                        }
                        else if(six == 1) lline[6] = key + 8;
                        if(m == 1){
                            lline[4] = key + 3;
                            lline[5] = key + 7;
                        }
                        else if(sus2 == 1){
                            lline[4] = key + 2;
                            lline[5] = key + 7;
                        }
                        else if(sus4 == 1){
                            lline[4] = key + 5;
                            lline[5] = key + 7;
                        }
                        else{
                            lline[4] = key + 4;
                            lline[5] = key + 7;
                        }
                        if(dim == 1){
                            lline[4] = key + 3;
                            lline[5] = key + 6;
                        }
                        else if(aug == 1){
                            lline[5] = key + 8;
                        }
                        notes.add(lline);
                    }
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
        //chordMaker.printF(notes);
        return notes;
    }
    
    public static void printListI(List <Integer> f){
       for(int i=0; i < f.size(); i++){
           if (i != f.size() - 1)System.out.print(f.get(i) + ", ");
           else System.out.print(f.get(i));
       }
       System.out.println();
    }
    
    //#Main
    public static ArrayList<float[]> xmRead(String filename){
        ArrayList<float[]> notes = new ArrayList();
        notes = xmTakeIn(filename);
        return notes;
    }
}
