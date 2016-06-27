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
    public static String[] chordRomanS = {"I", "", "II", "", "III", "IV", "", "V", "", "VI", "", "VII"};
    public static String[] chordRomanC = {"i", "", "ii", "", "iii", "iv", "", "v", "", "vi", "", "vii"};
    public static String[] natNoteNames = {"C", "", "D", "", "E", "F", "", "G", "", "A", "", "B"};
    public static int[] noteNumbers = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
    public static int[] natNoteNumbers = {0, 2, 4, 5, 7, 9, 11};
    public static int key = 0;
    public static final int startingNote = 36;
    
    
    //Read rootanalysis file(from kern website) and figures out the Chord notes with alterations and durations as given in a kern file
    //Function assumes every unit in a measure has the same duration
    
    public static ArrayList<float[]> readFile(ArrayList<float[]> chords, String filename){
        /*System.out.println("--------------------------------------------------------------------------------------------------------------------------------------\n"
                + "Chords with measure count starts here\n"
                + "--------------------------------------------------------------------------------------------------------------------------------------\n"
                + "format = {note1, note2, note3, duration as quarter(4)/eight(8)/sixteenth(16)/etc. note}\n"
                + "Measure 1");*/
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
                if(line.length() >= 4 && line.charAt(4) == ':'){
                    String c = "" + Character.toUpperCase(line.charAt(3));
                    int ind = Arrays.asList(noteNames).indexOf(c);
                    key = ind;
                }
                else if(line.length() >= 5 && line.charAt(5) == ':'){
                    String c = "" + Character.toUpperCase(line.charAt(3)) + Character.toUpperCase(line.charAt(4));
                    int ind = Arrays.asList(noteNames).indexOf(c);
                    key = ind;
                }
                else if(line.length() >= 2 && line.charAt(2) == ':'){
                    String c = "" + Character.toUpperCase(line.charAt(1));
                    int ind = Arrays.asList(noteNames).indexOf(c);
                    key = ind;
                }
                else if(line.length() >= 3 && line.charAt(3) == ':'){
                    String c = "" + Character.toUpperCase(line.charAt(1)) + Character.toUpperCase(line.charAt(2));
                    int ind = Arrays.asList(noteNames).indexOf(c);
                    key = ind;
                }
                
                //System.out.println(key);
                int curr = key;
                int rest = -1;
                int M = 1;
                int count = 0;
                int dim = -1;
                int augmented = -1;
                int seven = -1;
                int ninth = -1;
                int eleventh = -1;
                int thirteenth = -1;
                int inv = 0;
                
                
                //Find chord
                if(line.length() >= 3){
                    if(line.substring(0,3).equals("VII")){
                        curr += 10;
                        M = 1;
                        count = 3;
                    }
                    else if(line.substring(0,3).equals("vii")){
                        curr += 10;
                        M = 0;
                        count = 3;
                    }
                    else if(line.substring(0,3).equals("III")){
                        curr += 4;
                        M = 1;
                        count = 3;
                    }
                    else if(line.substring(0,3).equals("iii")){
                        curr += 4;
                        M = 0;
                        count = 3;
                    }
                    else if(line.substring(0,2).equals("II")){
                        curr += 2;
                        M = 1;
                        count = 2;
                    }
                    else if(line.substring(0,2).equals("ii")){
                        curr += 2;
                        M = 0;
                        count = 2;
                    }
                    else if(line.substring(0,2).equals("VI")){
                        curr += 9;
                        M = 1;
                        count = 2;
                    }
                    else if(line.substring(0,2).equals("vi")){
                        curr += 9;
                        M = 0;
                        count = 2;
                    }
                    else if(line.substring(0,2).equals("IV")){
                        curr += 5;
                        M = 1;
                        count = 2;
                    }
                    else if(line.substring(0,2).equals("iv")){
                        curr += 5;
                        M = 0;
                        count = 2;
                    }
                    else if(line.charAt(0) == 'I'){
                        M = 1;
                        count = 1;
                    }
                    else if(line.charAt(0) == 'i' ){
                        M = 0;
                        count = 1;
                    }
                    else if(line.charAt(0) == 'V'){
                        curr += 7;
                        M = 1;
                        count = 1;
                    }
                    else if(line.charAt(0) == 'v'){
                        curr += 7;
                        M = 0;
                        count = 1;
                    }
                    else if(line.charAt(0) == 'r'){
                        //curr += 7;
                        //M = 0;
                        //count = 1;
                        rest = 1;
                    }
                    else if(line.charAt(0) == '.'){
                        //curr += 7;
                        //M = 0;
                        //count = 1;
                        rest = 0;
                    }
                    int is = count;
                    if(count!=0){
                        while(line.charAt(is) == '#' || line.charAt(is) == '-' 
                                || line.charAt(is) == '+'|| line.charAt(is) == 'b'|| line.charAt(is) == 'c'|| line.charAt(is) == 'd'|| line.charAt(is) == 'e'|| line.charAt(is) == 'f'
                                || line.charAt(is) == 'g'|| line.charAt(is) == '7'|| line.charAt(is) == '9'|| line.charAt(is) == '1'|| line.charAt(is) == '/'){
                            if(line.charAt(is) == '#'){
                                curr += 1;
                                is++;
                            }
                            else if(line.charAt(is) == '-'){
                                dim = 1;
                                is++;
                            }
                            else if(line.charAt(is) == '+'){
                                augmented = 1;
                                is++;
                            }
                            else if(line.charAt(is) == 'b'){
                                inv = 1;
                                is++;
                            }
                            else if(line.charAt(is) == 'c'){
                                inv = 2;
                                is++;
                            }
                            else if(line.charAt(is) == 'd'){
                                inv = 3;
                                is++;
                            }
                            else if(line.charAt(is) == 'e'){
                                inv = 4;
                                is++;
                            }
                            else if(line.charAt(is) == 'f'){
                                inv = 5;
                                is++;
                            }
                            else if(line.charAt(is) == 'g'){
                                inv = 6;
                                is++;
                            }
                            else if(line.charAt(is) == '7'){
                                seven = 1;
                                is++;
                            }
                            else if(line.charAt(is) == '9'){
                                ninth = 1;
                                is++;
                            }
                            else if(line.charAt(is) == '1'){
                                if(line.charAt(is+1) == '1') eleventh = 1;
                                if(line.charAt(is+1) == '3') thirteenth = 1;
                                is += 2;
                            }
                            //Handle later
                            else if(line.charAt(is) == '/'){
                                ninth = 1;
                                is++;
                            }
                        }
                        //Not handling inversions, only major minor diminish augmented and seventh
                        if(rest == -1){
                            ArrayList<int[]> notes = new ArrayList();

                            int[] arr = {curr};
                            notes.add(arr);
                            if(M == 0){
                                int[] arr2 = {(curr + 3)};
                                notes.add(arr2);
                            }
                            else if(M == 1){
                                int[] arr2 = {(curr + 4)};
                                notes.add(arr2);
                            }

                            
                            if(dim == 1){
                                int[] arr3 = {(curr + 6)};
                                notes.add(arr3);
                            }
                            else if(augmented == 1){
                                int[] arr3 = {(curr + 8)};
                                notes.add(arr3);
                            }
                            else{
                                int[] arr3 = {(curr + 7)};
                                notes.add(arr3);
                            }
                            
                            if(seven == 1){
                                int[] arr3 = {(curr + 10)};
                                notes.add(arr3);
                            }
                            
                            if(notes.size() == 3){
                                float[] arr3 = {findNums2(line, is),notes.get(0)[0],notes.get(1)[0], notes.get(2)[0]};
                                chords.add(arr3);
                                ///System.out.println(findNums2(line, is) + " " + notes.get(0)[0] + " " + notes.get(1)[0] + " " + notes.get(2)[0]);
                            }
                            else if(notes.size() == 4){
                                float[] arr3 = {findNums2(line, is),notes.get(0)[0],notes.get(1)[0], notes.get(2)[0], notes.get(3)[0]};
                                chords.add(arr3);
                                ///System.out.println(findNums2(line, is) + " " + notes.get(0)[0] + " " + notes.get(1)[0] + " " + notes.get(2)[0] + " " + notes.get(3)[0]);
                            }
                        }
                    }
                    else if(rest == 0){
                            if(chords.size() >= 1){
                                float[] arr3 = chords.get(chords.size()-1);
                                chords.add(arr3);
                                if(chords.get(chords.size()-1).length == 4){
                                    ///System.out.println( chords.get(chords.size()-1)[0] + " " + chords.get(chords.size()-1)[1] + " "+ chords.get(chords.size()-1)[2] + " "+ chords.get(chords.size()-1)[3] + " ");
                                }
                                else if(chords.get(chords.size()-1).length == 5){
                                    ///System.out.println( chords.get(chords.size()-1)[0] + " "+ chords.get(chords.size()-1)[1] + " "+ chords.get(chords.size()-1)[2] + " "+ chords.get(chords.size()-1)[3] + " "+ chords.get(chords.size()-1)[4] + " ");
                                }
                            }
                            else{
                                float[] arr3 = {findNums2(line, is),-1};
                                chords.add(arr3);
                                ///System.out.println(findNums2(line, is) + " .");
                            }
                    }
                }
                
                
                /*for(int i=0; i < noteNumbers.length; i++){
                    System.out.println(noteNumbers[i]);
                }*/
                /*
                if(line.charAt(0) == '=' && measureNotes != 0 && index != 0){
                    System.out.println("Measure " + measureCnt);
                    measureCnt++;
                }
                else if(line.charAt(0) == 'r'){
                    //System.out.println("Rest");
                    float[] temp = {-1, -1, -1, findNums(line)};
                    chords.add(temp);
                    //System.out.println(chords.get(index)[0] + " " + chords.get(index)[1] + " " + chords.get(index)[2] + " " + chords.get(index)[3]);
                    index++;
                    measureNotes++;
                }
                else{
                    String c = "" + Character.toUpperCase(line.charAt(0));
                    int startInd = Arrays.asList(natNoteNames).indexOf(c);
                    if(startInd != -1){
                        int third = startInd + 4;
                        int fifth = startInd + 7;
                        if(line.charAt(0) == '#') startInd++;
                        else if(line.charAt(0) == '-') startInd--;
                        /*else{
                            //System.out.println(findNum(natNoteNumbers, startInd));
                            third = natNoteNumbers[(findNum(natNoteNumbers, startInd) + 9) % 7];
                            fifth = natNoteNumbers[(findNum(natNoteNumbers, startInd) + 11) % 7];
                        }
                        //System.out.println(startInd + " " + third + " " + fifth);
                        float[] temp = {36 + noteNumbers[(startInd+12)%12], 36 + noteNumbers[(third+12)%12], 36 + noteNumbers[(fifth+12)%12], findNums(line)};
                        //System.out.println(temp[0] + " " + temp[1] + " " + temp[2]);
                        chords.add(temp);
                        System.out.println(chords.get(index)[0] + " " + chords.get(index)[1] + " " + chords.get(index)[2] + " " + chords.get(index)[3]);
                        index++;
                        //chords.set(0, temp);
                        measureNotes++;
                    }
                }*/
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
    
    public static int findNum(int[] s, int t){
       int tempo = -1;
       String temp = "";
       for(int i=0; i < s.length; i++){
            if(s[i] == t){
                return i;
            }
        }
       return tempo;
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
    
    public static float findNums2(String s, int n){
       double tempo = 0;
       String temp = "";
       for(int i= n + 1; i < s.length(); i++){
            if(Character.isDigit(s.charAt(i))){
                temp = "";
                temp += s.charAt(i);
                for(int j=1; j+i < s.length(); j++){
                    if(Character.isDigit(s.charAt(i+j))){
                        temp += s.charAt(i+j);
                    }
                    else if(s.charAt(i+j) == 'r'){
                        temp = "0";
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
       return (float)tempo;
    }
    
    public static ArrayList<float[]> chordNotes(ArrayList<float[]> notes, String filename){
        readFile(notes, filename);
        return notes;
    }
    
    public static ArrayList<float[]> oompah(ArrayList<float[]> notes, int GCD){
        ArrayList<float[]> notes2 = new ArrayList();

        float currtimeGCD = 0;
        float[] currnote = notes.get(0);
        int j=0;
        for (int i=0; i<notes.size(); i++) {
            if (notes.get(i)[0] != -1){
                currnote = notes.get(i);
            }
            if (currtimeGCD/GCD % 8 == 0) {
                for (j=0; j<GCD*4; j+=GCD*currnote[3]) {
                    notes2.add(new float[]{currnote[0]+12,currtimeGCD+8*j,currtimeGCD+GCD*4+8*j});
                    notes2.add(new float[]{currnote[0]+12+12,currtimeGCD+GCD*4+8*j,currtimeGCD+GCD*8+8*j});
                    notes2.add(new float[]{currnote[1]+12+12,currtimeGCD+GCD*4+8*j,currtimeGCD+GCD*8+8*j});
                    notes2.add(new float[]{currnote[2]+12+12,currtimeGCD+GCD*4+8*j,currtimeGCD+GCD*8+8*j});
                }
            }
            currtimeGCD += GCD*32/currnote[3];
        }
        
        return notes2;
    }
}
