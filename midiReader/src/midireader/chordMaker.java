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
public class chordMaker {
    public static ArrayList<float[]> print(ArrayList<float[]> notes){
        for(int i = 0; i < notes.size(); i++){
            for(int j = 0; j < notes.get(i).length; j++){
                //System.out.print((int)notes.get(i)[j] + " ");
            }
            //System.out.println();
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
        print(notes);
        //System.out.println("Measure 1");
        float modTime = 0, mTime = 0, realTime = 0, difTime = 0, onTime = 0, offTime = 0;
        int measure = 2, beat = 0;
        ArrayList<float[]> currChord = new ArrayList();
        ArrayList<float[]> chordsWrite = new ArrayList();
        for(int i = 0; i < notes.size(); i++){
            mTime += 1/notes.get(i)[0];
            modTime += 1/notes.get(i)[0];
            if(modTime >= 1/4){
                difTime = realTime - modTime + 1/4;
                onTime = difTime*speed+speed/4;
                offTime = (difTime + (float).25)*speed+speed/4;
                modTime = modTime - (float).25;
                if(beat == 0){
                    //System.out.print("(" +  belowMid(notes.get(i)[1]) + ") " + onTime + " " + offTime);
                    float[] arr = {belowMid(notes.get(i)[1]), onTime, offTime};
                    chordsWrite.add(arr);
                }
                else if(beat == 1 || beat == 3){
                    //System.out.print("(" + belowMid(notes.get(i)[1]) + " " + belowMid(notes.get(i)[2]) + " " + belowMid(notes.get(i)[3]) + ") "  + onTime + " " + offTime);
                    float[] arr = {closestMid(notes.get(i)[1]), onTime, offTime};
                    float[] arr2 = {closestMid(notes.get(i)[2]), onTime, offTime};
                    float[] arr3 = {closestMid(notes.get(i)[3]), onTime, offTime};
                    chordsWrite.add(arr);
                    chordsWrite.add(arr2);
                    chordsWrite.add(arr3);
                    if(notes.get(i).length > 4){
                        //System.out.print(belowMid(notes.get(i)[3]) + " "  + onTime + " " + offTime);
                        float[] arr4 = {closestMid(notes.get(i)[3]), onTime, offTime};
                        chordsWrite.add(arr4);
                    }
                }
                else if(beat == 2){
                    //System.out.print("(" + belowMid(notes.get(i)[1]) + ") " + onTime + " " + offTime);
                    float[] arr = {belowMid(notes.get(i)[3]), onTime, offTime};
                    chordsWrite.add(arr);
                }
                beat++;
                if(beat >=4)beat=0;
                //System.out.println();
            }
            realTime += 1/notes.get(i)[0];
            if(mTime >= ts){
                
                //System.out.println("Measure " + measure);
                mTime -= 1;
                measure++;
            }
        }
        //print(chordsWrite);
        return chordsWrite;
    }
    
    public static ArrayList<float[]> chordMake(ArrayList<float[]> notes, float ts, float speed){
        ArrayList<float[]> chordsWrite = new ArrayList();
        chordsWrite = chordBD(notes, ts,speed);
        return chordsWrite;
    }
}