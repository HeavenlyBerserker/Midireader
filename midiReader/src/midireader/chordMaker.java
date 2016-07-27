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
import java.util.Random;
/**
 *
 * @author Hong
 */
public class chordMaker {
    public static ArrayList<float[]> print(ArrayList<float[]> notes){
        for(int i = 0; i < notes.size(); i++){
            for(int j = 0; j < notes.get(i).length; j++){
              //  System.out.print((int)notes.get(i)[j] + " ");
            }
           // System.out.println();
        }
        return notes;
    }
    
    public static ArrayList<float[]> printFl(ArrayList<float[]> notes){
        for(int i = 0; i < notes.size(); i++){
            for(int j = 0; j < notes.get(i).length; j++){
                //System.out.print(notes.get(i)[j] + " ");
            }
            //System.out.println();
        }
        return notes;
    }
    
    public static ArrayList<float[]> printF(ArrayList<float[]> notes){
        for(int i = 0; i < notes.size(); i++){
            for(int j = 0; j < notes.get(i).length; j++){
                System.out.print(notes.get(i)[j] + " ");
            }
            System.out.println();
        }
        return notes;
    }
    
    public static float closestMid(float note){
        while(note <= 64){
            note += 12;
        }
        return note;
    }
    public static float belowMid(float note){
        while(note <= 64){
            note += 12;
        }
        note -= 12;
        return note;
    }
    
    public static ArrayList<float[]> chordBD(ArrayList<float[]> notes, float ts, float speed){
        //print(notes);
        //System.out.println("Measure 1");
        int[] tempN = {0,0,0,0};
        Random randomGenerator = new Random();
        float modTime = 0, mTime = 0, realTime = -notes.get(0)[0], difTime = 0, onTime = 0, offTime = 0, prevOff = 0, prevOn = 0;
        int measure = 2, beat = 0, octave = 0, passing = 0;
        ArrayList<float[]> currChord = new ArrayList();
        ArrayList<float[]> chordsWrite = new ArrayList();
        
        //For every note in the list
        for(int i = 0; i < notes.size(); i++){
            mTime += notes.get(i)[0];
            modTime += notes.get(i)[0];
            realTime += notes.get(i)[0];
            //System.out.println(" NoteValue: " + 1/notes.get(i)[0] + " RealTime: " + realTime + " modTime: " + modTime + " ");
            while(modTime >= 1/4){
                difTime = realTime - modTime + 1/4;
                prevOn = onTime;
                onTime = difTime*speed+speed/4;
                //System.out.print(" RealTime: " + realTime + " modTime: " + modTime + " ");
                if(notes.get(i).length >= 2 && notes.get(i)[1] == -1){
                    //System.out.println("Rest");
                    modTime = modTime - (float).25;
                    break;
                }
                //System.out.println("##Note");
                prevOff = offTime = (difTime + (float).25)*speed+speed/4;
                if(modTime >= 1/4)modTime = modTime - (float).25;
                octave = randomGenerator.nextInt(2);
                if(beat == 1 || beat == 3)passing = randomGenerator.nextInt(6);
                //System.out.println(passing);
                if(beat == 0){
                    //System.out.print("(" +  belowMid(notes.get(i)[1]) + ") " + onTime + " " + offTime);
                    //Passing tone
                    if(notes.get(i)[1] != -1){
                        if(passing == 1){
                            if(notes.size() >= 1){
                                int note1 = (int)chordsWrite.get(chordsWrite.size() - 1)[0];
                                int note2 = (int)belowMid(notes.get(i)[1]);
                                int note3 = (note1 + note2)/2;
                                if(note3 == note1 || note3 == note2){
                                    float[] arr = {tempN[0] , prevOn, onTime};
                                    float[] arr2 = {tempN[1] , prevOn, onTime};
                                    float[] arr3 = {tempN[2] , prevOn, onTime};
                                    chordsWrite.add(arr);
                                    chordsWrite.add(arr2);
                                    chordsWrite.add(arr3);
                                    if(tempN[3]!= 0){
                                        float[] arr4 = {tempN[3] , prevOn, onTime};
                                        chordsWrite.add(arr4);
                                    }
                                }
                                else{
                                    //System.out.println("Notes: " + note1 + " " + note2 + " " + note3);
                                    float[] arr2 = {note3, prevOn, onTime};
                                    chordsWrite.add(arr2);
                                    float[] arr3 = {note3-12, prevOn, onTime};
                                    chordsWrite.add(arr3);
                                    float[] arr4 = {note1-12, notes.get(notes.size() - 1)[1], notes.get(notes.size() - 1)[2]};
                                    chordsWrite.add(arr3);
                                    octave = 1;
                                }
                            }
                        }
                    }
                    //Octave
                    if(octave == 1){
                        float[] arr2 = {belowMid(notes.get(i)[1])-12, onTime, offTime};
                        chordsWrite.add(arr2);
                    }
                    //One note
                    float[] arr = {belowMid(notes.get(i)[1]), onTime, offTime};
                    chordsWrite.add(arr);
                    
                }
                else if(beat == 1 || beat == 3){
                    //System.out.print("(" + belowMid(notes.get(i)[1]) + " " + belowMid(notes.get(i)[2]) + " " + belowMid(notes.get(i)[3]) + ") "  + onTime + " " + offTime);
                    //Normal
                    if(notes.get(i)[1] != -1){
                        float[] arr = {closestMid(notes.get(i)[1]), onTime, offTime};
                        float[] arr2 = {closestMid(notes.get(i)[2]), onTime, offTime};
                        float[] arr3 = {closestMid(notes.get(i)[3]), onTime, offTime};
                        tempN[0] = (int)closestMid(notes.get(i)[1]);
                        tempN[1] = (int)closestMid(notes.get(i)[2]);
                        tempN[2] = (int)closestMid(notes.get(i)[3]);
                        if(passing != 1){
                            chordsWrite.add(arr);
                            chordsWrite.add(arr2);
                            chordsWrite.add(arr3);
                        }
                        //Seventh
                        if(notes.get(i).length > 4){
                            //System.out.print(belowMid(notes.get(i)[3]) + " "  + onTime + " " + offTime);
                            float[] arr4 = {closestMid(notes.get(i)[4]), onTime, offTime};
                            if(passing != 1)chordsWrite.add(arr4);
                            tempN[3] = (int)closestMid(notes.get(i)[4]);
                            //System.out.println();
                            //System.out.println(notes.get(i).length);
                        }
                        else{
                            tempN[3] = 0;
                        }
                    }
                }
                else if(beat == 2){
                    //System.out.print("(" + belowMid(notes.get(i)[1]) + ") " + onTime + " " + offTime);
                    //Passing tone
                    if(notes.get(i)[1] != -1){
                        if(passing == 1){
                            if(notes.size() >= 1){
                                int note1 = (int)chordsWrite.get(chordsWrite.size() - 1)[0];
                                int note2 = (int)belowMid(notes.get(i)[3]);
                                int note3 = (note1 + note2)/2;
                                if(note3 == note1 || note3 == note2){
                                    float[] arr = {tempN[0] , prevOn, onTime};
                                    float[] arr2 = {tempN[1] , prevOn, onTime};
                                    float[] arr3 = {tempN[2] , prevOn, onTime};
                                    chordsWrite.add(arr);
                                    chordsWrite.add(arr2);
                                    chordsWrite.add(arr3);
                                    if(tempN[3]!= 0){
                                        float[] arr4 = {tempN[3] , prevOn, onTime};
                                        chordsWrite.add(arr4);
                                    }
                                }
                                else{
                                //System.out.println("Notes: " + note1 + " " + note2 + " " + note3);
                                float[] arr2 = {note3, prevOn, onTime};
                                chordsWrite.add(arr2);
                                float[] arr3 = {note3-12, prevOn, onTime};
                                chordsWrite.add(arr3);
                                float[] arr4 = {note1-12, notes.get(notes.size() - 1)[1], notes.get(notes.size() - 1)[2]};
                                chordsWrite.add(arr3);
                                octave = 1;
                                }
                            }
                        }
                        if(octave == 1){
                            float[] arr2 = {belowMid(notes.get(i)[3])-12, onTime, offTime};
                            chordsWrite.add(arr2);
                        }
                        float[] arr = {belowMid(notes.get(i)[3]), onTime, offTime};
                        chordsWrite.add(arr);
                    }
                }
                beat++;
                //System.out.print(" RealTime: " + realTime + " modTime: " + modTime);
                if(beat >=4)beat=0;
                //System.out.println();
            }
            
            if(mTime >= ts){
                
                //System.out.println("Measure " + measure);
                mTime -= 1;
                measure++;
            }
        }
        //print(chordsWrite);
        return chordsWrite;
    }
    
    public static ArrayList<float[]> chordInt(ArrayList<float[]> notes){
        for(int i = 0; i < notes.size(); i++){
            float[] temp = new float[notes.get(i).length];
            for(int j = 0; j < notes.get(i).length; j++){
                temp[j] = (int)notes.get(i)[j];
            }
            notes.set(i, temp);
        }
        return notes;
    }
    
    public static ArrayList<float[]> listCut(ArrayList<float[]> notes, int size){
        ArrayList<float[]> notes2 = new ArrayList();
        for(int i = 0; i < size; i++){
            float[] temp = new float[notes.get(i).length];
            for(int j = 0; j < notes.get(i).length; j++){
                temp[j] = (int)notes.get(i)[j];
            }
            notes2.add(temp);
        }
        return notes2;
    }
    
    
    public static ArrayList<float[]> chordMake(ArrayList<float[]> notes, float ts, float speed){
        ArrayList<float[]> chordsWrite = new ArrayList();
        
        //print(notes);
        chordsWrite = chordBD(notes, ts,speed);
        chordInt(chordsWrite);
        //return listCut(chordsWrite, 10);
        /*System.out.println("--------------------------------------------------------------------------------\n--------------------------------------------------------------------------------");
        printF(chordsWrite);*/
        //print(notes);
        return chordsWrite;
    }
}