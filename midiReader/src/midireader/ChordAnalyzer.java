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
 * @author Hong
 */
public class ChordAnalyzer {
    
    public static String[] noteNames = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    public static String[] natNoteNames = {"C", "", "D", "", "E", "F", "", "G", "", "A", "", "B"};
    public static int[] noteNumbers = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
    public static final int startingNote = 36;
    
    
    //Read rootanalysis file(from kern website) and figures out the Chord notes with alterations and durations as given in a kern file
    //Function assumes every unit in a measure has the same duration
    
    public static ArrayList<float[]> readFile(ArrayList<float[]> chords, String filename){
        System.out.println("--------------------------------------------------------------------------------------------------------------------------------------\n"
                + "Chords with measure count starts here\n"
                + "--------------------------------------------------------------------------------------------------------------------------------------\n"
                + "format = {note1, note2, note3, duration as quarter(4)/eight(8)/sixteenth(16)/etc. note}\n"
                + "Measure 1");
        String line = null;
        
        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader = 
                new FileReader(filename);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = 
                new BufferedReader(fileReader);
            int measureCnt = 2, index = 0;
            int measureNotes = 0;
            while((line = bufferedReader.readLine()) != null) {
                //System.out.println(line.substring(0,3));
                if(line.substring(0,3).equals("*k[")){
                    int  i = 3;
                    while(line.charAt(i) != ']'){
                        String c = "" + Character.toUpperCase(line.charAt(i));
                        int ind = Arrays.asList(noteNames).indexOf(c);
                        //System.out.println(Character.toUpperCase(line.charAt(i)));
                        //System.out.println(ind);
                        for(int j=0; j < noteNumbers.length; j++){
                            noteNumbers[j] = j;
                        }
                        if(line.charAt(i+1) == '#'){
                            noteNumbers[ind]++;
                        }
                        else{
                            noteNumbers[ind]--;
                        }
                        i += 2;
                    }
                }
                
                /*for(int i=0; i < noteNumbers.length; i++){
                    System.out.println(noteNumbers[i]);
                }*/
                
                if(line.charAt(0) == '=' && measureNotes != 0 && index != 0){
                    System.out.println("Measure " + measureCnt);
                    measureCnt++;
                    /*
                    for(int i=1; i - 1 < measureNotes; i++){
                        //System.out.println(i);
                        float[] temp = {chords.get(index-i)[0], chords.get(index-i)[1],chords.get(index-i)[2], 120*quarter/measureNotes};
                        //System.out.println(temp[0] + " " + temp[1] + " " + temp[2] + " " + temp[3]);
                        chords.set(index-i, temp);
                        //System.out.println(chords.get(index-i)[0] + " " + chords.get(index-i)[1] + " " + chords.get(index-i)[2] + " " + chords.get(index-i)[3]);
                        //*/
                    /*}
                    measureNotes = 0;*/
                }
                else if(line.charAt(0) == 'r'){
                    //System.out.println("Rest");
                    float[] temp = {-1, -1, -1, findNums(line)};
                    chords.add(temp);
                    System.out.println(chords.get(index)[0] + " " + chords.get(index)[1] + " " + chords.get(index)[2] + " " + chords.get(index)[3]);
                    index++;
                    measureNotes++;
                }
                else{
                    String c = "" + Character.toUpperCase(line.charAt(0));
                    int startInd = Arrays.asList(natNoteNames).indexOf(c);
                    if(startInd != -1){
                        if(line.charAt(0) == '#') startInd++;
                        else if(line.charAt(0) == '-') startInd--;
                        int third = startInd + 4;
                        int fifth = startInd + 7;
                        //System.out.println(startInd + " " + third + " " + fifth);
                        float[] temp = {36 + noteNumbers[(startInd+12)%12], 36 + noteNumbers[(third+12)%12], 36 + noteNumbers[(fifth+12)%12], findNums(line)};
                        //System.out.println(temp[0] + " " + temp[1] + " " + temp[2]);
                        chords.add(temp);
                        System.out.println(chords.get(index)[0] + " " + chords.get(index)[1] + " " + chords.get(index)[2] + " " + chords.get(index)[3]);
                        index++;
                        //chords.set(0, temp);*/
                        measureNotes++;
                    }
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
        /*System.out.println("--------------------------------------------------------------------------------------------------------------------------------------\n"
                + "Chords without measure count starts here\n"
                + "--------------------------------------------------------------------------------------------------------------------------------------");*/
        return chords;
    }
    
    public static float findNums(String s){
       float tempo = 0;
       String temp = "";
       for(int i=1; i < s.length(); i++){
            if(Character.isDigit(s.charAt(i))){
                temp = "";
                temp += s.charAt(i);
                for(int j=1; j+i < s.length(); j++){
                    if(Character.isDigit(s.charAt(i+j))){
                        temp += s.charAt(i+j);
                    }
                    else{
                        break;
                    }
                }
                if(Float.parseFloat(temp) > tempo){
                    tempo = Float.parseFloat(temp);
                }
            }
        }
       return tempo;
    }
    
    public static ArrayList<float[]> chordNotes(ArrayList<float[]> notes, String filename){
        readFile(notes, filename);
        return notes;
    }
}
