

package midireader;


import midireader.DataStructs.BiHashMap;
import java.util.ArrayList;
import midireader.MChainNoteNum.MChainProcess;
import midireader.MChainNoteNum.MChainRead;
import midireader.auxClasses.FunctionCallers;
import java.io.File; 


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
        
        //System.out.print("Hello World\n");
        
        //Uncomment the following line to run note analysis
        //FunctionCallers.noteAnalysis("input/InputV1/notefiles", "table");
        
        //------Reads transition probabilities from file-------------------
        //Reading chainOutput (toggle second arg for print or not)
        ArrayList<float[]> [][] chain = MChainRead.readChainOutput("output/ChainOutput.csv", false);
        //-----------------------------------------------------------------
        
        //------Reads table-------------------
        ArrayList<String[]> patterns = MChainRead.readTable("output/table.csv", true);
        //------------------------------------
        /*for(int i = 0; i < patterns.size() ; i ++){
            for(int j = 0; j < patterns.get(i).length ; j ++){
                System.out.print(patterns.get(i)[j] + " ");
            }
            System.out.println();
        }*/
        
        File dir = new File("input/xm/");
        File[] directoryListing = dir.listFiles();
        int songNum = 0;
        if (directoryListing != null) {
          for (File child : directoryListing) {
            
            String name = child.getName();
            if(name.endsWith(".xmk")){
                name = name.substring(0, name.length() - 4);
                System.out.println(name);
                songNum++;
                
                //-------------Version 1 activation-------------------
                FunctionCallers.V1Call(name,1f,patterns);
                //----------------------------------------------------

                //-------------Version 2 activation-------------------
                //Line 1 prints out results, line 2 doesn't.
                //MChainProcess.processingS1("yankeeDb", true, chain);
                MChainProcess. processingS1(name, false, chain, 1f);
                //----------------------------------------------------

                //-------------Syncopalooza activation-------------------
                //Input file location: "input/xm/" + filePath + ".xmk"
                FunctionCallers.SyncoCall(name,0.7f);
                //----------------------------------------------------
            }
          }
          System.out.println("Transformed: " + songNum + " songs.");
          for (File child : directoryListing) {
            
            String name = child.getName();
            if(name.endsWith(".xmk")){
                name = name.substring(0, name.length() - 4);
                System.out.println(name);
            }
          }
        }
        
    }

}
/*      for(int i = 0; i < patterns.size() ; i ++){
            for(int j = 0; j < patterns.get(i).length ; j ++){
                System.out.print(patterns.get(i)[j] + " ");
            }
            System.out.println();
        }
*/