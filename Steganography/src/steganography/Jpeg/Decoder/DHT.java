/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package steganography.Jpeg.Decoder;

import java.util.LinkedHashMap;

/**
 *
 * @author Alex
 */
public class DHT {
    
    int classId;//DC or AC
    int tableId;
    int[] lengthCode;
    int[] valCode;
    LinkedHashMap<Integer, Integer> huffmanCodes = new LinkedHashMap<>();//codeWord + length
    LinkedHashMap<Integer, Integer> huffmanValues = new LinkedHashMap<>(); //codeWord + value

    DHT(int classId, int tableId, int[] lengthCode, int[] valCode, LinkedHashMap<Integer, Integer> huffmanCodes, LinkedHashMap<Integer, Integer> huffmanValues) {
        this.classId = classId;
        this.tableId = tableId;
        this.lengthCode = lengthCode;
        this.valCode = valCode;
        this.huffmanCodes = huffmanCodes;
        this.huffmanValues = huffmanValues;
    }
 
    public int getClassId() {
        return classId;
    }

    public int getTableId() {
        return tableId;
    }

    public int[] getLengthCode() {
        return lengthCode;
    }

    public int[] getValCode() {
        return valCode;
    }

    public LinkedHashMap<Integer, Integer> getHuffmanCodes() {
        return huffmanCodes;
    }

    public LinkedHashMap<Integer, Integer> getHuffmanValues() {
        return huffmanValues;
    }
    
    public boolean hasCode(int code, int len){
        return (huffmanCodes.containsKey(code) && huffmanCodes.get(code) == len);   
    }
    
    public void view(){
        System.out.println("Class "+ classId +" Table "+tableId);
        System.out.println("\nValue Codeword Len of codeword");
        huffmanCodes.entrySet().forEach((entry) -> {
            Integer key = entry.getKey();
            Integer len = entry.getValue();
 
            System.out.print(huffmanValues.get(key)+"\t");
            System.out.println(key+"\t"+len);
        });
        
    }
}
