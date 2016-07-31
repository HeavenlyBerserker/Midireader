
package midireader.processingXmk;

import java.util.ArrayList;


public class melodyChanger {
    
    public static ArrayList<float[]> makeMonophonic(ArrayList<float[]> notes) {
        return skyToNotes(findSkyline2(notes));
    }
    
    public static ArrayList<Float> findSkyline2(ArrayList<float[]> notes) {
        ArrayList <Float> skyline = new ArrayList();
        float min = 0;
        float max = notes.get(notes.size()-1)[2]+100000; //for some reason this has to be [1] on midi files and [2] on .note files, or just really big
        float size = max-min;
	
        float[] mysky = new float[(int)size];
        for (int i=0; i<size; i++)
                mysky[i] = 0;

        for (int i=0; i<notes.size(); i++) {
                float left = notes.get(i)[1];
                float height = notes.get(i)[0];
                float right = notes.get(i)[2];

                for (int j=(int)(left-min); j<(int)(right-min); j++)
                        if (height > mysky[j])
                                mysky[j] = height;
        }

        //System.out.print(min+" ");
        //skyline.add(min);
        int cnt = 0;
        skyline.add(mysky[cnt]);//,cnt+1+min});
        skyline.add(cnt+1+min);
        cnt++;
        while (cnt < size-1) {

            while (cnt < size-1 && mysky[cnt] == mysky[cnt+1])
                    cnt++;

            //System.out.print(mysky[cnt]+" "+(cnt+1+min)+" ");
            skyline.add(mysky[cnt]);//,cnt+1+min});
            skyline.add(cnt+1+min);
            cnt++;
        }

        if (cnt == size-1){
            //System.out.print(mysky[(int)size-1]+" "+max);
            skyline.add(mysky[(int)size-1]);
            skyline.add(max);
        }

        return skyline;
    }
    
    public static ArrayList<float[]> skyToNotes(ArrayList<Float> sky) {
        ArrayList <float[]> output = new ArrayList();
        for (int i=1; i<sky.size()-1; i+=2) {
            output.add(new float[]{sky.get(i+1),sky.get(i),sky.get(i+2)});
            //System.out.println(sky.get(i+1) + " " + sky.get(i) + " " + sky.get(i+2));
        }
        return output;
    }
}

