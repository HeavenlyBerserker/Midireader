/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package midireader.DataStructs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.Iterator;
/**
 *
 * @author domini
 */
public class BiHashMap<K1, K2, V> {
    private List<Integer> columns = new ArrayList<>();
    private int[] cols = new int[65536];
    private final TreeMap<K1, TreeMap<K2, V>> mMap;

    public BiHashMap() {
        mMap = new TreeMap<K1, TreeMap<K2, V>>();
    }

    /**
     * Associates the specified value with the specified keys in this map (optional operation). If the map previously
     * contained a mapping for the key, the old value is replaced by the specified value.
     * 
     * @param key1
     *            the first key
     * @param key2
     *            the second key
     * @param value
     *            the value to be set
     * @return the value previously associated with (key1,key2), or <code>null</code> if none
     * @see Map#put(Object, Object)
     */
    public V put(K1 key1, K2 key2, V value) {
        TreeMap<K2, V> map;
        if (mMap.containsKey(key1)) {
            map = mMap.get(key1);
        } else {
            map = new TreeMap<K2, V>();
            mMap.put(key1, map);
        }

        return map.put(key2, value);
    }

    /**
     * Returns the value to which the specified key is mapped, or <code>null</code> if this map contains no mapping for
     * the key.
     * 
     * @param key1
     *            the first key whose associated value is to be returned
     * @param key2
     *            the second key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or <code>null</code> if this map contains no mapping for
     *         the key
     * @see Map#get(Object)
     */
    public V get(K1 key1, K2 key2) {
        if (mMap.containsKey(key1)) {
            return mMap.get(key1).get(key2);
        } else {
            return null;
        }
    }

    /**
     * Returns <code>true</code> if this map contains a mapping for the specified key
     * 
     * @param key1
     *            the first key whose presence in this map is to be tested
     * @param key2
     *            the second key whose presence in this map is to be tested
     * @return Returns true if this map contains a mapping for the specified key
     * @see Map#containsKey(Object)
     */
    public boolean containsKeys(K1 key1, K2 key2) {
        return mMap.containsKey(key1) && mMap.get(key1).containsKey(key2);
    }

    public void clear() {
        mMap.clear();
    }
    
    public void printMap() {
        Iterator it = mMap.entrySet().iterator();
        int i = 0;
        while (it.hasNext()) {
            Map.Entry m = (Map.Entry)it.next();
            
            System.out.println(m.getKey() + ": ");
            Map<K2, V> map2 = (Map<K2, V>)m.getValue();
            Iterator it2 = map2.entrySet().iterator();
            while (it2.hasNext()) {
                Map.Entry pair = (Map.Entry)it2.next();
                System.out.println("\t" + pair.getKey() + " = " + pair.getValue()); // avoids a ConcurrentModificationException
            }
        }
    }
    
    public void writeToCsv(String filename) {
        Iterator it = mMap.entrySet().iterator();
        
        for(int j = 0; j < cols.length; j++){
            cols[j] = 0;
        }
        
        while (it.hasNext()) {
            Map.Entry m = (Map.Entry)it.next();
            
            //System.out.println(m.getKey() + ": ");
            Map<K2, V> map2 = (Map<K2, V>)m.getValue();
            Iterator it2 = map2.entrySet().iterator();
            while (it2.hasNext()) {
                Map.Entry pair = (Map.Entry)it2.next();
                //System.out.println("\t" + pair.getKey() + " = " + pair.getValue());
                //it2.remove(); // avoids a ConcurrentModificationException
                cols[(int)pair.getKey()] = 1;
            }
            //it.remove();
        }
        
        
        for(int j = 0; j < cols.length; j++){
            if(cols[j] == 1){
                columns.add(j);
            }
        }
        
        
        StringBuilder content = new StringBuilder(filename + ",");
        for(int j = 0; j < columns.size(); j++){
            if(j == columns.size()-1) content.append(Integer.toString(columns.get(j)));
            else{
                content.append(Integer.toString(columns.get(j)));
                content.append(",");
            }
        }
        
        Iterator lt = mMap.entrySet().iterator();
        while (lt.hasNext()) {
            Map.Entry m = (Map.Entry)lt.next();
            
            //System.out.println(m.getKey() + ": ");
            Map<K2, V> map2 = (Map<K2, V>)m.getValue();
            Iterator lt2 = map2.entrySet().iterator();
            //content += "\n" + (int)m.getKey() + ",";
            content.append("\n");
            content.append((int)m.getKey());
            content.append(",");
            
            int lastkey = 0;
            int keydiff = 0;
            while (lt2.hasNext()) {
                Map.Entry pair = (Map.Entry)lt2.next();
                //System.out.println("\t" + pair.getKey() + " = " + pair.getValue());
                keydiff = columns.indexOf((int)pair.getKey()) - lastkey;
                lastkey = columns.indexOf((int)pair.getKey()) + 1;
                for(int y = 0; y < keydiff; y++){
                    content.append("0,");
                }
                content.append((int)pair.getValue());
                content.append(",");
            }
            
            //if(lastkey == 0) lastkey = 1;
            for(int y = 0; y < columns.size()- lastkey; y++){
                content.append("0,");
            }
        }
        
         try {

                File file = new File("output/" +filename + ".csv");

                // if file doesnt exists, then create it
                if (!file.exists()) {
                        file.createNewFile();
                }

                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(content.toString());
                bw.close();

                System.out.println("Files Done");

        } catch (IOException e) {
                e.printStackTrace();
        }
        
    }
    
    public void writeToError(String filename, StringBuilder content) {
        try {

                File file = new File("output/" +filename + ".txt");

                // if file doesnt exists, then create it
                if (!file.exists()) {
                        file.createNewFile();
                }

                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(content.toString());
                bw.close();

                System.out.println("Exceptions Done");

        } catch (IOException e) {
                e.printStackTrace();
        }
    }
}
