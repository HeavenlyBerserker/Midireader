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
                float[] arr = {0,0,0};
                arr[0] = lastNote;
                arr[1] = realTime;
                realTime += speed*xm.get(i)[0]/ xm.get(i)[1];
                arr[2] = realTime;
                notes.add(arr);
            }
        }
        
        return notes;
    }
    
    public static ArrayList<float[]> xmkPlayHar(ArrayList<float[]> xm){
        ArrayList<float[]> notes = new ArrayList();
        
        float tsNum = xm.get(0)[0], tsDen = xm.get(0)[1], tsBpm = xm.get(0)[2];
        float speed = 60000/tsBpm*4;
        chordMaker.printF(xm);
        
        List<Float> chord = new ArrayList<>();
        float realTime = 0, relatTime = 0, tempTime = 0;
        int i = 1;
        while(i < xm.size()){
            if(xm.get(i)[0] != -2){
                realTime += speed*xm.get(i)[0]/ xm.get(i)[1];
                relatTime += speed*xm.get(i)[0]/ xm.get(i)[1];
            }
            while(relatTime >= 0.25*speed){
                tempTime = realTime -relatTime;
                relatTime -= 0.25*speed;
                //System.out.print(tempTime + ": ");
                if(xm.get(i)[0] != -2 && xm.get(i)[1] != 0 && xm.get(i)[3] >= 0){
                    chord.clear();
                    for(int j = 3; j < xm.get(i).length; j++){
                        float[] arr = {0,0,0};
                        //lastNote = xm.get(i)[2];
                        arr[0] = xm.get(i)[j];
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
                        arr[0] = chord.get(j);
                        arr[1] = tempTime;
                        arr[2] = tempTime + speed*xm.get(i)[0]/ xm.get(i)[1];
                        notes.add(arr);
                    }
                }
                //System.out.println();
            }
            
            i++;
        }
        chordMaker.printF(notes);
        return notes;
    }
    
    public static ArrayList<float[]> xmPlay(ArrayList<float[]> xm){
        ArrayList<float[]> notes = new ArrayList();
        ArrayList<float[]> chords = new ArrayList();
        notes = xmkPlayMel(xm);
        chords = xmkPlayHar(xm);
        ArrayList<float[]> song = notes;
        song.addAll(chords);
        return song;
    }
}
