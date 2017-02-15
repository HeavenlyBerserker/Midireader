/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package midireader.processingXmk;

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
import java.util.Random;
import java.util.Scanner;

/**
 *
 * @author domini
 */
public class xmPlayer {
    
    public static ArrayList<float[]> xmkPlayMel(ArrayList<float[]> xm){
        ArrayList<float[]> notes = new ArrayList();
        
        float tsNum = xm.get(0)[0], tsDen = xm.get(0)[1], tsBpm = xm.get(0)[2];
        float speed = 60000/tsBpm*4;
        
        float lastNote = 0;
        float realTime = 0;
        for(int i = 1; i < xm.size(); i++){
            if(xm.get(i)[0] != -2 && xm.get(i)[1] != 0 && xm.get(i)[2] >= 0){
                float[] arr = {0,0,0};
                lastNote = xm.get(i)[2];
                arr[0] = xm.get(i)[2];
                arr[1] = realTime;
                realTime += speed*xm.get(i)[0]/ xm.get(i)[1];
                arr[2] = realTime;
                notes.add(arr);
            }
            else if(xm.get(i)[0] != -2 && xm.get(i)[2] == -1){
                realTime += speed*xm.get(i)[0]/ xm.get(i)[1];
            }
            else if(xm.get(i)[0] != -2 && xm.get(i)[2] == -2){
                float[] arr = notes.get(notes.size()-1);
                realTime += speed*xm.get(i)[0]/ xm.get(i)[1];
                arr[2] = realTime;
                //ChordAnalyzer.printArray(arr);
                notes.set(notes.size()-1, arr);
            }
        }
        
        return notes;
    }
    
    public static ArrayList<float[]> xmkPlayHar(ArrayList<float[]> xm){
        ArrayList<float[]> notes = new ArrayList();
        
        
        int[] pass = new int[xm.size()];
        Random randomGenerator = new Random();
        for(int i = 0; i < pass.length; i++){
            pass[i] = randomGenerator.nextInt(6);
        }
        
        int[] oct = new int[xm.size()];
        for(int i = 0; i < oct.length; i++){
            oct[i] = randomGenerator.nextInt(1);
        }
        
        int beat = 0, passing = 0;
        
        float tsNum = xm.get(0)[0], tsDen = xm.get(0)[1], tsBpm = xm.get(0)[2];
        float speed = 60000/tsBpm*4;
        //chordMaker.printF(xm);
        
        List<Float> chord = new ArrayList<>();
        float realTime = 0, relatTime = 0, tempTime = 0;
        int i = 1;
        while(i < xm.size()){
            if(xm.get(i)[0] != -2){
                realTime += speed*xm.get(i)[0]/ xm.get(i)[1];
                relatTime += speed*xm.get(i)[0]/ xm.get(i)[1];
            }
            else{
                //System.out.println("Measure " + xm.get(i)[1] + "-----------------------");
            }
            while(relatTime >= 0.25*speed){
                tempTime = realTime -relatTime;
                relatTime -= 0.25*speed;
                //System.out.print(tempTime + ": ");
                //If beat 2 or 4 and no passing tones
                if(xm.get(i)[0] != -2 && xm.get(i)[1] != 0 && xm.get(i)[3] == -2){
                    if(chord.size() >= 1){
                        float[] arr = new float[3 + chord.size()];
                        arr[0] = xm.get(i)[0];
                        arr[1] = xm.get(i)[1];
                        arr[2] = xm.get(i)[2];
                        for(int c = 0; c < chord.size(); c++){
                            arr[c+3] = chord.get(c);
                        }
                        xm.set(i, arr);
                    }
                }
                if(xm.get(i)[0] != -2 && xm.get(i)[1] != 0 && xm.get(i)[3] >= 0){
                    if((beat == 1 || beat == 3) && passing == 0){
                        if(xm.get(i)[0] != -2 && xm.get(i)[1] != 0 && xm.get(i)[3] >= 0){
                            chord.clear();
                            for(int j = 3; j < xm.get(i).length; j++){
                                float[] arr = {0,0,0};
                                //lastNote = xm.get(i)[2];
                                arr[0] = cMid(xm.get(i)[j]);
                                //System.out.print(xm.get(i)[j] + ", ");
                                chord.add(xm.get(i)[j]);
                                arr[1] = tempTime;
                                arr[2] = tempTime + speed*xm.get(i)[0]/ xm.get(i)[1];
                                notes.add(arr);
                            }
                        }
                        else if(xm.get(i)[0] != -2 && xm.get(i)[3] == -2){
                            for(int j = 0; j < chord.size(); j++){
                                float[] arr = {0,0,0};
                                //lastNote = xm.get(i)[2];
                                arr[0] = cMid(chord.get(j));
                                arr[1] = tempTime;
                                arr[2] = tempTime + speed*xm.get(i)[0]/ xm.get(i)[1];
                                notes.add(arr);
                            }
                        }
                        //System.out.println("Chord 2,4");
                    }
                    //else passing tone preparation
                    else if((beat == 0 || beat == 2) && pass[i] == 0 && passing == 0){
                        passing = 1;
                        float[] arr = {0,0,0};
                        arr[0] = bMid(xm.get(i)[3]);
                        arr[1] = tempTime;
                        arr[2] = tempTime + speed*xm.get(i)[0]/ xm.get(i)[1];
                        notes.add(arr);
                        float[] arr2 = {0,0,0};
                        arr2[0] = bMid(xm.get(i)[3])-12;
                        arr2[1] = tempTime;
                        arr2[2] = tempTime + speed*xm.get(i)[0]/ xm.get(i)[1];
                        notes.add(arr2);
                        chord.clear();
                        //Prepare last chord
                        for(int j = 3; j < xm.get(i).length; j++){
                            float[] arr3 = {0,0,0};
                            //lastNote = xm.get(i)[2];
                            arr3[0] = cMid(xm.get(i)[j]);
                            //System.out.print(xm.get(i)[j] + ", ");
                            chord.add(xm.get(i)[j]);
                            arr3[1] = tempTime;
                            arr3[2] = tempTime + speed*xm.get(i)[0]/ xm.get(i)[1];
                        }
                        //System.out.println("Passing tone prep 1,3");
                    }
                    else if((beat == 1 || beat == 3) && passing == 1);//System.out.println("Passing tone intermediate 2,4");
                    //passing tone
                    else if((beat == 0 || beat == 2) && passing == 1){
                        passing = 0;
                        if(chord.size() > 0){
                            float[] arrr = {0,0,0};
                            arrr[0] = (float)Math.floor((bMid(chord.get(0)) + bMid(xm.get(i)[3]))/2);
                            arrr[1] = tempTime - (float).25*speed;
                            arrr[2] = tempTime + speed*xm.get(i)[0]/ xm.get(i)[1] - (float).25*speed;
                            if(arrr[0] != bMid(chord.get(0))){
                                notes.add(arrr);
                                float[] arr2 = {0,0,0};
                                arr2[0] = (float)Math.floor((bMid(chord.get(0)) + bMid(xm.get(i)[3]))/2) - 12;
                                arr2[1] = tempTime - (float).25*speed;
                                arr2[2] = tempTime + speed*xm.get(i)[0]/ xm.get(i)[1] - (float).25*speed;
                                notes.add(arr2);
                            }
                            else{
                                if(beat == 0){
                                    float[] arr = {0,0,0};
                                    arr[0] = bMid(xm.get(i)[3]);
                                    arr[1] = tempTime;
                                    arr[2] = tempTime + speed*xm.get(i)[0]/ xm.get(i)[1];
                                    notes.add(arr);
                                    float[] arr2 = {0,0,0};
                                    arr2[0] = bMid(xm.get(i)[3])-12;
                                    arr2[1] = tempTime;
                                    arr2[2] = tempTime + speed*xm.get(i)[0]/ xm.get(i)[1];
                                    notes.add(arr2);
                                }
                                else if(beat == 2){
                                    float[] arr = {0,0,0};
                                    arr[0] = bMid(xm.get(i)[5]);
                                    arr[1] = tempTime;
                                    arr[2] = tempTime + speed*xm.get(i)[0]/ xm.get(i)[1];
                                    notes.add(arr);
                                    float[] arr2 = {0,0,0};
                                    arr2[0] = bMid(xm.get(i)[5])-12;
                                    arr2[1] = tempTime;
                                    arr2[2] = tempTime + speed*xm.get(i)[0]/ xm.get(i)[1];
                                    notes.add(arr2);
                                }
                                //Prepare last chord
                                for(int j = 3; j < xm.get(i).length; j++){
                                    float[] arr3 = {0,0,0};
                                    //lastNote = xm.get(i)[2];
                                    arr3[0] = cMid(xm.get(i)[j]);
                                    //System.out.print(xm.get(i)[j] + ", ");
                                    chord.add(xm.get(i)[j]);
                                    arr3[1] = tempTime;
                                    arr3[2] = tempTime + speed*xm.get(i)[0]/ xm.get(i)[1];
                                }
                            }
                        }
                        float[] arr = {0,0,0};
                        arr[0] = bMid(xm.get(i)[3]);
                        arr[1] = tempTime;
                        arr[2] = tempTime + speed*xm.get(i)[0]/ xm.get(i)[1];
                        notes.add(arr);
                        float[] arr2 = {0,0,0};
                        arr2[0] = bMid(xm.get(i)[3])-12;
                        arr2[1] = tempTime;
                        arr2[2] = tempTime + speed*xm.get(i)[0]/ xm.get(i)[1];
                        notes.add(arr2);
                        //Prepare last chord
                        for(int j = 3; j < xm.get(i).length; j++){
                            float[] arr3 = {0,0,0};
                            //lastNote = xm.get(i)[2];
                            arr3[0] = cMid(xm.get(i)[j]);
                            //System.out.print(xm.get(i)[j] + ", ");
                            chord.add(xm.get(i)[j]);
                            arr3[1] = tempTime;
                            arr3[2] = tempTime + speed*xm.get(i)[0]/ xm.get(i)[1];
                        }
                        //System.out.println("Passing tone imp 1,3");
                    }
                    else if((beat == 0 || beat == 2) && passing == 0 && oct[i] == 0){
                        if(beat == 0){
                            float[] arr = {0,0,0};
                            arr[0] = bMid(xm.get(i)[3]);
                            arr[1] = tempTime;
                            arr[2] = tempTime + speed*xm.get(i)[0]/ xm.get(i)[1];
                            notes.add(arr);
                        }
                        else if(beat == 2){
                            float[] arr = {0,0,0};
                            arr[0] = bMid(xm.get(i)[5]);
                            arr[1] = tempTime;
                            arr[2] = tempTime + speed*xm.get(i)[0]/ xm.get(i)[1];
                            notes.add(arr);
                        }
                        //Prepare last chord
                        for(int j = 3; j < xm.get(i).length; j++){
                            float[] arr3 = {0,0,0};
                            //lastNote = xm.get(i)[2];
                            arr3[0] = cMid(xm.get(i)[j]);
                            //System.out.print(xm.get(i)[j] + ", ");
                            chord.add(xm.get(i)[j]);
                            arr3[1] = tempTime;
                            arr3[2] = tempTime + speed*xm.get(i)[0]/ xm.get(i)[1];
                        }
                        //System.out.println("Single note 1,3");
                    }
                    else if((beat == 0 || beat == 2) && passing == 0 && oct[i] != 1){
                        if(beat == 0){
                            float[] arr = {0,0,0};
                            arr[0] = bMid(xm.get(i)[3]);
                            arr[1] = tempTime;
                            arr[2] = tempTime + speed*xm.get(i)[0]/ xm.get(i)[1];
                            notes.add(arr);
                            float[] arr2 = {0,0,0};
                            arr2[0] = bMid(xm.get(i)[3])-12;
                            arr2[1] = tempTime;
                            arr2[2] = tempTime + speed*xm.get(i)[0]/ xm.get(i)[1];
                            notes.add(arr2);
                        }
                        else if(beat == 2){
                            float[] arr = {0,0,0};
                            arr[0] = bMid(xm.get(i)[5]);
                            arr[1] = tempTime;
                            arr[2] = tempTime + speed*xm.get(i)[0]/ xm.get(i)[1];
                            notes.add(arr);
                            float[] arr2 = {0,0,0};
                            arr2[0] = bMid(xm.get(i)[5])-12;
                            arr2[1] = tempTime;
                            arr2[2] = tempTime + speed*xm.get(i)[0]/ xm.get(i)[1];
                            notes.add(arr2);
                        }
                        //Prepare last chord
                        for(int j = 3; j < xm.get(i).length; j++){
                            float[] arr3 = {0,0,0};
                            //lastNote = xm.get(i)[2];
                            arr3[0] = cMid(xm.get(i)[j]);
                            //System.out.print(xm.get(i)[j] + ", ");
                            chord.add(xm.get(i)[j]);
                            arr3[1] = tempTime;
                            arr3[2] = tempTime + speed*xm.get(i)[0]/ xm.get(i)[1];
                        }
                        //System.out.println("Octave 1,3");
                    }
                }
                //System.out.println();
                
                if(beat != 3) beat++;
                else beat = 0;
            }
            
            i++;
        }
        //chordMaker.printF(notes);
        return notes;
    }
    
    public static float cMid(float note){
        while(note <= 60){
            note += 12;
        }
        while(note >= 60){
            note -= 12;
        }
        return note;
    }
    public static float bMid(float note){
        while(note <= 60){
            note += 12;
        }
        while(note >= 60){
            note -= 12;
        }
        note -= 12;
        return note;
    }
    
    public static ArrayList<float[]> xmPlay(ArrayList<float[]> xm, int ischords){
        ArrayList<float[]> notes = new ArrayList();
        ArrayList<float[]> chords = new ArrayList();
        notes = xmkPlayMel(xm);
        chords = xmkPlayHar(xm);
        ArrayList<float[]> song = new ArrayList();
        song.addAll(chords);
        song.addAll(notes);
        //chordMaker.printF(chords);
        if (ischords == 1)
            return chords;
        else
            return notes;
    }
}
