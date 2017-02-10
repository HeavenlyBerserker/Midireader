

package midireader;


import midireader.DataStructs.BiHashMap;
import java.util.ArrayList;
import midireader.MChainNoteNum.MChainProcess;
import midireader.MChainNoteNum.MChainRead;
import midireader.auxClasses.FunctionCallers;


public class XmkMain {
    public static final int NOTE_ON = 0x90;
    public static final int NOTE_OFF = 0x80;
    public static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    public static ArrayList differences = new ArrayList();
    public static int GCD = 0;
    public static float resolution;
    public static float ppq;
    public static float MEASURES;
    public static float MM; //beats per minute from melisma
    public static int lines[] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
    public static BiHashMap<Integer, Integer, Integer> hash = new BiHashMap<Integer, Integer, Integer>();
    
    public static void main(String[] args) throws Exception {
        
        //Uncomment the following line to run note analysis
        //FunctionCallers.noteAnalysis("input/InputV1/notefiles", "table");
        
        //------Reads transition probabilities from file-------------------
        //Reading chainOutput (toggle second arg for print or not)
        ArrayList<float[]> [][] chain = MChainRead.readChainOutput("output/ChainOutput.csv", false);
        //-----------------------------------------------------------------
        
        //Names of file
        String filename = "spring";
        
        //-------------Version 1 activation-------------------
        FunctionCallers.V1Call(filename,0.3f);
        //----------------------------------------------------
        
        //-------------Version 2 activation-------------------
        //Line 1 prints out results, line 2 doesn't.
        //MChainProcess.processingS1("yankeeDb", true, chain);
        MChainProcess. processingS1(filename, false, chain, 0.3f);
        //----------------------------------------------------
        
        //-------------Syncopalooza activation-------------------
        //Input file location: "input/xm/" + filePath + ".xmk"
        FunctionCallers.SyncoCall(filename,0.3f);
        //----------------------------------------------------
        
    }

}