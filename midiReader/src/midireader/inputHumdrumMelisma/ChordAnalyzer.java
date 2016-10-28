/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package midireader.inputHumdrumMelisma;

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
import midireader.auxClasses.Fractions;
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
    public static int num = 0;
    public static int den = 0;
    public static int beats = 0;
    public static final int startingNote = 36;
    public static ArrayList<float[]> tempoTable = new ArrayList();
    public static ArrayList<float[]> tTable = new ArrayList();
    public static List<Fractions> table = new ArrayList<>();
    public static List<Fractions> lastArry = new ArrayList<>();
    public static Fractions realTemp =  new Fractions();
    
    //Read rootanalysis file(from kern website) and figures out the Chord notes with alterations and durations as given in a kern file
    //Function assumes every unit in a measure has the same duration
    
    public static ArrayList<float[]> readFile(ArrayList<float[]> chords, String filename){
        /*System.out.println("--------------------------------------------------------------------------------------------------------------------------------------\n"
                + "Chords with measure count starts here\n"
                + "--------------------------------------------------------------------------------------------------------------------------------------\n"
                + "format = {note1, note2, note3, duration as quarter(4)/eight(8)/sixteenth(16)/etc. note}\n"
                + "Measure 1");*/
        String line = null;
        float chrono = 0;
        float chrono2 = 0;
        float[] array = {-5};
        tTable.add(array);
        tTable.add(array);
        tTable.add(array);
        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader = 
                new FileReader(filename);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = 
                new BufferedReader(fileReader);
            int measureCnt = 0, index = 1;
            int measureNotes = 0, noteCount = 0;
            float temporal = 0;
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
                for(int i = 2; i < line.length(); i++){
                    if(line.charAt(i) == ':'){
                        int alt = 0;
                        for(int j = 0; j < noteNames.length; j++){
                            if(Character.toUpperCase(line.charAt(i-1)) == noteNames[j].charAt(0)){
                                alt = 1;
                                String c = "" + Character.toUpperCase(line.charAt(i-1));
                                int ind = Arrays.asList(noteNames).indexOf(c);
                                key = ind;
                            }
                            if(Character.toUpperCase(line.charAt(i-2)) == noteNames[j].charAt(0)){
                                alt = 2;
                                String c = "" + Character.toUpperCase(line.charAt(i-2));
                                int ind = Arrays.asList(noteNames).indexOf(c);
                                key = ind;
                                if(Character.toUpperCase(line.charAt(i-1)) == '#') ind++;
                                if(Character.toUpperCase(line.charAt(i-1)) == 'b') ind--;
                            }
                        }
                        if(alt == 1){
                            
                        }
                    }
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
                
                
                if(line.charAt(0) == '=' && Character.isDigit(line.charAt(1))){
                    //if(chrono != 16){
//------------------System.out.print("\nMeasure " + measureCnt + " Chrono " + chrono2);
                    //}
                    if(chrono2 != 1){
                       System.out.print("\nMeasure " + measureCnt + " Chrono " + chrono2);
                    }
                    chrono = 0;
                    chrono2 = 0;
                    
                    measureCnt++;
                }
                
                //Find chord
                if(line.length() >= 3){
                    if(line.substring(0,3).equals("*MM")){
                        timeBeat(line);
                        //System.out.println("Beat " + beats);
                    }
                    else if(line.substring(0,2).equals("*M")){
                        timeSig(line);
                        //System.out.println("Num " + num);
                        //System.out.println("Den " + den);
                    }
                    if(line.charAt(0) == 'D'){
                        count = 1;
                        dim = 1;
                        line = line.substring(1, line.length()-1);
                    }
                    if(line.substring(0,3).equals("VII")){
                        curr += 10;
                        M = 1;
                        count = 3;
                    }
                    else if(line.substring(0,3).equals("Chr")){
                        //curr += 7;
                        //M = 0;
                        //count = 1;
                        rest = 0;
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
                    if(line.charAt(0) == '=' && Character.isDigit(line.charAt(1))){
                        float[] arr = {-2, index};
                        index++;
                        tempoTable.add(arr);
                    }
                    int is = count;
                    if(count!=0 || rest == 0 || rest == 1){
                        putInArray2(line);
                        putInArray3(line);
                        //printArray(tTable.get(1));
                        //if no prev array
                        if(noteCount == 0){
                            //System.out.println();
                            realTemp = findGreat(table);
                            Fractions[] arry = new Fractions[table.size()];
                            if(realTemp.num > 0){
                                for(int i = 0; i < table.size(); i++){
                                    if(table.get(i).num > 0 && realTemp.num == table.get(i).num && realTemp.den == table.get(i).den){
                                        arry[i] = new Fractions(0,1);
                                        //arry[i].print("1");
                                    }
                                    else if(table.get(i).num > 0){
                                        arry[i] = table.get(i).subtract(realTemp);
                                        //arry[i].print("2");
                                    }
                                    else{
                                        arry[i] = new Fractions(0,1);
                                    }
                                }
                            }
                            for(int i = 0; i < arry.length; i++){
                                Fractions temp = new Fractions(arry[i].num, arry[i].den);
                                lastArry.add(temp);
                            }
                            //printListFrac(lastArry);
                            if(realTemp.num != 0) chrono2 += Fractions.fl(realTemp);
                            //printListFrac(lastArry);
                            //System.out.println();System.out.println();System.out.println();
                        }
                        else{
                            //System.out.println();
                            //System.out.print("LastArray ");printListFrac(lastArry);
                            List<Fractions> arry = new ArrayList<>();
                            for(int i = 0; i < table.size(); i++){
                                if(lastArry.size() > i && lastArry.get(i).num > 0){
                                    arry.add(lastArry.get(i));
                                }
                                else if(table.get(i).num > 0){
                                    if(lastArry.size() > i)
                                        lastArry.set(i, new Fractions(table.get(i).num, table.get(i).den));
                                    else
                                        lastArry.add(new Fractions(table.get(i).num, table.get(i).den));
                                }
                                else{
                                    if(lastArry.size() > i)
                                        lastArry.set(i,new Fractions(0,1));
                                    else
                                        lastArry.add(new Fractions(0,1));
                                }
                            }
                            //System.out.print("Arry ");printListFrac(lastArry);
                            realTemp.num = findGreat(lastArry).num;
                            int t = findGreat(lastArry).den;
                            realTemp.den = t;
                            //System.out.print("Arry ");printListFrac(lastArry);
                            //System.out.println(realTemp);
                            //printArray(tTable.get(0));
                            //printArray(arry);
                            //printListFrac(lastArry);
                            //System.out.print("Table ");printListFrac(table);
                            //System.out.println();
                            //realTemp.print();
                            Fractions realTemp2 = new Fractions(realTemp.num, realTemp.den);
                            //System.out.print(Fractions.fl(realTemp));
                            if(realTemp.num != 0) {chrono2 += Fractions.fl(realTemp);}
                            //System.out.println(chrono2);
                            //System.out.print("Arry ");printListFrac(arry);
                            if(realTemp.num > 0){
                                //realTemp.print();
                                for(int i = 0; i < lastArry.size(); i++){
                                    //System.out.print("{");
                                    //realTemp.print(", ");
                                    //lastArry.get(i).print("}");
                                    if(lastArry.get(i).num > 0 && realTemp.num == lastArry.get(i).num && realTemp.den == lastArry.get(i).den){
                                        lastArry.set(i, new Fractions(0,1));
                                        //System.out.print("1");
                                        //realTemp.print("1 ");
                                        //arry[i].print("1 ");
                                    }
                                    else if(lastArry.size() > i && lastArry.get(i).num > 0){
                                        lastArry.set(i, lastArry.get(i).subtract(realTemp));
                                        //realTemp.print("2 ");
                                        //arry[i].print("2 {");
                                        //lastArry.get(i).print("2 ");
                                        //realTemp.print("}2 ");
                                        //System.out.print("2");
                                    }
                                    else{ 
                                        lastArry.set(i, new Fractions(0,1));
                                        //realTemp.print("3 ");
                                        //arry[i].print("3 ");
                                        //lastArry.get(i).print("3 ");
                                        //realTemp.print("}3 ");
                                        //System.out.print("3");
                                    }
                                }
                            }
                            //System.out.println("\nNewArry ");printListFrac(lastArry);
                            //realTemp = findGreat(arry);
                            //realTemp.print();
                            //System.out.print(Fractions.fl(realTemp));
                            //if(realTemp.num != 0) chrono2 += Fractions.fl(realTemp);
                            /*lastArry.clear();
                            for(int i = 0; i < arry.size(); i++){
                                lastArry.add(arry.get(i));
                            }*/
                            //printArray(tTable.get(1));
                        }

                        //Set new thing
                        //printArray(tTable.get(0));
                        //System.out.println("NextL");
                        noteCount++;
                    }
                    /*
                    if(count!=0 || rest == 0 || rest == 1){
                        putInArray2(line);
                        putInArray3(line);
                        //printArray(tTable.get(1));
                        
                        //if no prev array
                        if(noteCount == 0){
                            realTemp = findGreat(tTable.get(0));
                            System.out.println(realTemp);
                            float[] arry = new float[tTable.get(0).length];
                            if(realTemp > 0){
                                for(int i = 0; i < tTable.get(0).length; i++){
                                    if(tTable.get(0)[i] > 0 && realTemp == tTable.get(0)[i]){
                                        arry[i] = 0;
                                    }
                                    else if(tTable.get(0)[i] > 0){
                                        arry[i] = 1/(1/tTable.get(0)[i] - 1/realTemp);
                                    }
                                }
                            }
                            tTable.set(1, arry);
                            printArray(tTable.get(1));
                            if(realTemp!= 0) chrono += 16/realTemp;
                        }
                        else{
                            float[] arry = new float[tTable.get(0).length];
                            for(int i = 0; i < tTable.get(0).length; i++){
                                if(tTable.get(1).length > i && tTable.get(1)[i] > 0){
                                    arry[i] = tTable.get(1)[i];
                                }
                                else if(tTable.get(0)[i] > 0){
                                    arry[i] = tTable.get(0)[i];
                                }
                                else{
                                    arry[i] = 0;
                                }
                            }
                            realTemp = findGreat(arry);
                            //System.out.println(realTemp);
                            //printArray(tTable.get(0));
                            //printArray(arry);
                            if(realTemp > 0){
                                for(int i = 0; i < tTable.get(0).length; i++){
                                    if(arry[i] > 0 && realTemp == arry[i]){
                                        arry[i] = 0;
                                    }
                                    else if(arry[i] > 0){
                                        arry[i] = 82944/(82944/arry[i] - 82944/realTemp);
                                    }
                                    else arry[i] = 0;
                                }
                            }
                            if(realTemp!= 0) chrono += 16/realTemp;
                            tTable.set(1, arry);
                            //printArray(tTable.get(1));
                        }

                        //Set new thing
                        //printArray(tTable.get(0));
                        //System.out.println("NextL");
                        noteCount++;
                    }*/
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
                                float[] arr3 = {Fractions.fl(realTemp),notes.get(0)[0] % 12,notes.get(1)[0] % 12, notes.get(2)[0] % 12};
                                chords.add(arr3);
                                //printArray(arr3);
                                //System.out.print(chords.size());
                                //chordMaker.printF(chords);
                                //System.out.println(Fractions.fl(realTemp) + " " + notes.get(0)[0] + " " + notes.get(1)[0] + " " + notes.get(2)[0]);
                            }
                            else if(notes.size() == 4){
                                float[] arr3 = {Fractions.fl(realTemp),notes.get(0)[0] % 12,notes.get(1)[0] % 12, notes.get(2)[0] % 12, notes.get(3)[0] % 12};
                                chords.add(arr3);
                                //printArray(arr3);
                                //System.out.print(chords.size());
                                //chordMaker.printF(chords);
                                //System.out.println(Fractions.fl(realTemp) + " " + notes.get(0)[0] + " " + notes.get(1)[0] + " " + notes.get(2)[0] + " " + notes.get(3)[0]);
                            }
                        }
                    }
                    else if(rest == 0){
                            if(chords.size() >= 1){
                                float[] arr3 = chords.get(chords.size()-1);
                                arr3[0] = Fractions.fl(realTemp);
                                chords.add(arr3);
                                if(chords.get(chords.size()-1).length == 4){
                                    //System.out.println( chords.get(chords.size()-1)[0] + " " + chords.get(chords.size()-1)[1] + " "+ chords.get(chords.size()-1)[2] + " "+ chords.get(chords.size()-1)[3] + " ");
                                }
                                else if(chords.get(chords.size()-1).length == 5){
                                    //System.out.println( chords.get(chords.size()-1)[0] + " "+ chords.get(chords.size()-1)[1] + " "+ chords.get(chords.size()-1)[2] + " "+ chords.get(chords.size()-1)[3] + " "+ chords.get(chords.size()-1)[4] + " ");
                                }
                                else if(chords.get(chords.size()-1).length == 2){
                                    //System.out.println( chords.get(chords.size()-1)[0] + " "+ chords.get(chords.size()-1)[1]);
                                }
                            }
                            else{
                                float[] arr3 = {Fractions.fl(realTemp),-1};
                                chords.add(arr3);
                                //System.out.println(findNums2(line, is) + " .");
                            }
                    }
                    else if(rest == 1){
                        float[] arr3 = {Fractions.fl(realTemp),-1};
                        chords.add(arr3);
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
                    System.out.println(chords.get(index)[0] + " " + chords.get(index)[1] + " " + chords.get(index)[2] + " " + chords.get(index)[3]);
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
            //Figure out tempos
            //chordMaker.printF(tempoTable);
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
    
    public static float findGreat(float[] a){
       float bignum = 0;
       for(int i=0; i < a.length; i++){
           if(a[i] != 0 && a[i] > bignum){
               bignum = a[i];
           }
       }
       //printArray(a);
       //System.out.println("Big = " + bignum + "Length =" + a.length);
       return bignum;
    }
    
    public static Fractions findGreat(List <Fractions> a){
       Fractions bignum = new Fractions(10000, 1);
       for(int i=0; i < a.size(); i++){
           if(a.get(i).num != 0 && a.get(i).den != 0 && a.get(i).den != 0 && Fractions.bigger( bignum, a.get(i))){
               bignum = a.get(i);
           }
       }
       if(bignum.num == 10000) return new Fractions(0,1);
       //printArray(a);
       //System.out.println("Big = " + bignum + "Length =" + a.length);
       //printListFrac(a);
       return bignum;
    }
    
    public static Fractions findGreat(Fractions [] a){
       Fractions bignum = new Fractions(10000, 1);
       for(int i=0; i < a.length; i++){
           if(a[i].num != 0 && a[i].den != 0 && a[i].den != 0 && Fractions.bigger( bignum, a[i])){
               bignum = a[i];
           }
       }
       //printArray(a);
       //System.out.println("Big = " + bignum + "Length =" + a.length);
       //printListFrac(a);
       return bignum;
    }
    
    public static float findMaxInd(float[] a){
       float bignum = 0;
       int i = 0, s = 0;
       for(i=0; i < a.length; i++){
           if(a[i] > bignum){
               bignum = a[i];
               s = i;
           }
       }
       return (float)s;
    }
    public static float sumArray(float[] a){
       float bignum = 0;
       int i = 0;
       for(i=0; i < a.length; i++){
           if(a[i] > 0){
               bignum += 1/a[i];
           }
       }
       return bignum;
    }
    
    public static void printArray(float[] a){
       for(int i=0; i < a.length; i++){
           System.out.print(a[i] + ", ");
       }
       System.out.println();
    }
    

    public static void printArray(int[] a){
       for(int i=0; i < a.length; i++){
           System.out.print(a[i] + ", ");
       }
       System.out.println();
    }
    
    
    public static void printListFrac(List <Fractions> f){
       for(int i=0; i < f.size(); i++){
           //System.out.print("[" + f.get(i).num + "/" + f.get(i).den + "]");
       }
       System.out.println();
    }
    
    public static void printListFrac(Fractions[] f){
       for(int i=0; i < f.length; i++){
           System.out.print("[" + f[i].num + "/" + f[i].den + "]");
       }
       System.out.println();
    }
    
    public static int findNum(String s){
       int minnum = 10000000;
       String temp = "";
       for(int i=0; i < s.length(); i++){
           if(Character.isDigit(s.charAt(i))){
               temp = temp + s.charAt(i);
           }
       }
       if(temp == "") return -1;
       return Integer.parseInt(temp);
    }
    
    public static int tempo(String s1, String s2){
       int temp = 0, ind1 = 0, ind2 = 0;
       String test1 = "", test2 = "";
       ArrayList<float[]> numbers = new ArrayList();
       while(ind1 < s1.length()){
           if(s1.charAt(ind1) == '\t'){
               float[] arr = {(float)findNum(test1)};
               numbers.add(arr);
               test1 = "";
           }
           else{
               
           }
           ind1++;
       }
       return temp;
    }
    public static int putInArray(String s){
       double tempo = 0;
       String temp = "";
       int tabs = 0, tabOn = 0, i = 0;
       //System.out.println("String--------------------------------" + s.length());
       if(s.charAt(0) == '=' && Character.isDigit(s.charAt(1))){
           float[] arr = {-2};
           System.out.println("Hi");
           tempoTable.add(arr);
       }
       else{
            ArrayList<float[]> numbers = new ArrayList();
            while(i < s.length()){
                 float[] time = {-1};
                 //System.out.print(i + ", ");
                 if(s.charAt(i) == '\t' && tabs < 1){
                     tabs++;
                     tabOn = 1;
                     i++;
                 }
                 else if(s.charAt(i) == '\t'){
                     time[0] = -1;
                     i++;
                     //System.out.println(s.charAt(i));
                     while(i < s.length() && !Character.isDigit(s.charAt(i))  && s.charAt(i) != '\t') i++;
                     if(i < s.length() && s.charAt(i) != '\t'){
                         temp = "";
                         temp += s.charAt(i);
                         i++;
                         int c = i-1;
                         while(i < s.length()-1 && s.charAt(i) != '\t'){i++;}
                         /*    //System.out.println(s.charAt(i));
                             if(Character.isDigit(s.charAt(i))){
                                 temp += s.charAt(i);
                             }
                             else if(s.charAt(i) == '.'){
                                 temp = String.valueOf(Float.valueOf(temp)*2/3);
                             }
                             else{
                                 i++;
                                 break;
                             }
                             i++;
                         }*/
                         float ts = smallOf(s.substring(c, i));
                         time[0] = ts;
                         //System.out.print(time[0] + ", ");
                         /*float[] time = {Float.valueOf(temp)};
                         numbers.add(time);
                         tabOn = 0;*/
                     }
                     numbers.add(time);
                     
                     //tabOn = 0;
                 }
                 else{
                     i++;
                 }
             }
            //System.out.println();
            float[] arr = new float[numbers.size()];
            for(int j= 0; j < numbers.size(); j++){
                arr[j] = numbers.get(j)[0];
            }
            tempoTable.add(arr);
       }
       return (int)tempo;
    }
    
    public static int putInArray2(String s){
       double tempo = 0;
       String temp = "";
       int tabs = 0, tabOn = 0, i = 0;
       //System.out.println("String--------------------------------" + s.length());
       if(s.charAt(0) == '=' && Character.isDigit(s.charAt(1))){
           float[] arr = {-2};
           System.out.println("Hi");
           tempoTable.add(arr);
       }
       else{
            ArrayList<float[]> numbers = new ArrayList();
            while(i < s.length()){
                 float[] time = {-1};
                 //System.out.print(i + ", ");
                 if(s.charAt(i) == '\t' && tabs < 1){
                     tabs++;
                     tabOn = 1;
                     i++;
                 }
                 else if(s.charAt(i) == '\t'){
                     time[0] = -1;
                     i++;
                     //System.out.println(s.charAt(i));
                     while(i < s.length() && !Character.isDigit(s.charAt(i))  && s.charAt(i) != '\t') i++;
                     if(i < s.length() && s.charAt(i) != '\t'){
                         temp = "";
                         temp += s.charAt(i);
                         i++;
                         int c = i-1;
                         while(i < s.length()-1 && s.charAt(i) != '\t'){i++;}
                         /*    //System.out.println(s.charAt(i));
                             if(Character.isDigit(s.charAt(i))){
                                 temp += s.charAt(i);
                             }
                             else if(s.charAt(i) == '.'){
                                 temp = String.valueOf(Float.valueOf(temp)*2/3);
                             }
                             else{
                                 i++;
                                 break;
                             }
                             i++;
                         }*/
                         float ts = smallOf(s.substring(c, i));
                         time[0] = ts;
                         //System.out.print(time[0] + ", ");
                         /*float[] time = {Float.valueOf(temp)};
                         numbers.add(time);
                         tabOn = 0;*/
                     }
                     numbers.add(time);
                     
                     //tabOn = 0;
                 }
                 else{
                     i++;
                 }
             }
            //System.out.println();
            float[] arr = new float[numbers.size()];
            for(int j= 0; j < numbers.size(); j++){
                arr[j] = numbers.get(j)[0];
            }
            tTable.set(0, arr);
       }
       return (int)tempo;
    }
    
    public static int putInArray3(String s){
       table.clear();
       double tempo = 0;
       String temp = "";
       int tabs = 0, tabOn = 0, i = 0;
       //System.out.println("String--------------------------------" + s.length());
       if(s.charAt(0) == '=' && Character.isDigit(s.charAt(1))){
           //Fractions ts = new Fractions(-2, 2);
           //table.add(ts);
       }
       else{
            ArrayList<float[]> numbers = new ArrayList();
            while(i < s.length()){
                 //System.out.print(i + ", ");
                 if(s.charAt(i) == '\t' && tabs < 1){
                     tabs++;
                     tabOn = 1;
                     i++;
                 }
                 else if(s.charAt(i) == '\t'){
                     Fractions ts = new Fractions();
                     i++;
                     //System.out.println(s.charAt(i));
                     while(i < s.length() && !Character.isDigit(s.charAt(i))  && s.charAt(i) != '\t') i++;
                     if(i < s.length() && s.charAt(i) != '\t'){
                         temp = "";
                         temp += s.charAt(i);
                         i++;
                         int c = i-1;
                         while(i < s.length()-1 && s.charAt(i) != '\t'){i++;}
                         /*    //System.out.println(s.charAt(i));
                             if(Character.isDigit(s.charAt(i))){
                                 temp += s.charAt(i);
                             }
                             else if(s.charAt(i) == '.'){
                                 temp = String.valueOf(Float.valueOf(temp)*2/3);
                             }
                             else{
                                 i++;
                                 break;
                             }
                             i++;
                         }*/
                         ts = smallOf2(s.substring(c, i));
                         //ts.print();
                         //System.out.print(time[0] + ", ");
                         /*float[] time = {Float.valueOf(temp)};
                         numbers.add(time);
                         tabOn = 0;*/
                     }
                     table.add(ts);
                     
                     //tabOn = 0;
                 }
                 else{
                     i++;
                 }
             }
       }
       //printListFrac(table);
       //findGreat(table).print();
       //System.out.println();
       return (int)tempo;
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
    
    public static float timeSig(String s){
       float tempo = 0;
       String temp = "";
       int in = 0;
       while(in < s.length() && !Character.isDigit(s.charAt(in))) in++;
       while(in < s.length() && Character.isDigit(s.charAt(in))){
           temp += s.charAt(in);
           in++;
       }
       num = (int)Float.parseFloat(temp);
       if(s.charAt(in) == '/'){
           in++;
       }
       temp = "";
       while(in < s.length() && Character.isDigit(s.charAt(in))){
           temp += s.charAt(in);
           in++;
       }
       den = (int)Float.parseFloat(temp);
       return tempo;
    }
    
    public static float timeBeat(String s){
       float tempo = 0;
       String temp = "";
       int in = 0;
       while(in < s.length() && !Character.isDigit(s.charAt(in))) in++;
       while(in < s.length() && Character.isDigit(s.charAt(in))){
           temp += s.charAt(in);
           in++;
       }
       beats = (int)Float.parseFloat(temp);
       return tempo;
    }
    
    public static float findNums2(String s, int n){
       double tempo = 0;
       String temp = "";
       for(int i= n; i < s.length(); i++){
            if(Character.isDigit(s.charAt(i))){
                temp = "";
                temp += s.charAt(i);
                for(int j=1; j+i < s.length(); j++){
                    if(Character.isDigit(s.charAt(i+j))){
                        temp += s.charAt(i+j);
                    }
                    else if(s.charAt(i+j) == '.'){
                        temp = String.valueOf(Float.valueOf(temp)*2/3);
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
    
    public static float smallOf(String s){
       double tempo = 0;
       String temp = "";
       int i= 0;
       while(i < s.length()){
            if(Character.isDigit(s.charAt(i))){
                temp = "";
                temp += s.charAt(i);
                i++;
                while(i < s.length() && (Character.isDigit(s.charAt(i))|| s.charAt(i) == '.')){
                    if(Character.isDigit(s.charAt(i))){
                        temp += s.charAt(i);
                    }
                    else if(s.charAt(i) == '.'){
                        temp = String.valueOf(Float.valueOf(temp)*2/3);
                    }
                    else{
                        break;
                    }
                    i++;
                }
                if(Float.parseFloat(temp) > tempo){
                    tempo = Float.parseFloat(temp);
                }
            }
            i++;
        }
       //System.out.print("||" + temp + ", ");
       return (float)tempo;
    }
    
    public static Fractions smallOf2(String s){
       int q = 0;
       Fractions tempo = new Fractions();
       String temp = "";
       int i= 0;
       while(i < s.length()){
            if(Character.isDigit(s.charAt(i))){
                temp = "";
                temp += s.charAt(i);
                int dot = -1;
                i++;
                Fractions tempo2 = new Fractions(1, 1);
                while(i < s.length()){
                    if(Character.isDigit(s.charAt(i))){
                        temp += s.charAt(i);
                    }
                    else if(s.charAt(i) == '.'){
                        tempo2.num = 3;
                        dot = 1;
                    }
                    else if(s.charAt(i) == 'Q'){
                        q = 1;
                    }
                    else if(s.charAt(i) == ' '){
                        break;
                    }
                    i++;
                }
                tempo2.den = (int)Float.parseFloat(temp);
                if(dot == 1) tempo2.den = 2*(int)Float.parseFloat(temp);
                //tempo2.print();
                if(Fractions.bigger(tempo2, tempo)){
                    tempo.den = tempo2.den;
                    tempo.num = tempo2.num;
                }
            }
            i++;
        }
       if(q == 1){
            tempo.den = 1;
            tempo.num = 0;
       }
       //System.out.print("||" + temp + ", ");
       return tempo;
    }
    
    public static ArrayList<float[]> chordNotes(ArrayList<float[]> notes, String filename, int[] timeSig){
        readFile(notes, filename);
        //chordMaker.printF(tempoTable);

        timeSig[0] = num;
        timeSig[1] = den;
        timeSig[2] = beats;
        //chordMaker.printFl(notes);
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
