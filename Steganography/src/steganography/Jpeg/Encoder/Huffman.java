/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package steganography.Jpeg.Encoder;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Alex
 */
public class Huffman {
    
     public Huffman() {
    }
    
    List<Table> listTableDC = new ArrayList<>();
    List<Table> listTableAC = new ArrayList<>();
    
    int[] bitsDCY ={ 0, 1, 5, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0 };  //codes of length i, starti = 1
    int[] valDCY = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };            //value of length i

    int[] bitsDCCbr = { 0, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0 };
    int[] valDCCbr = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };

    int[] bitsACY = { 0, 2, 1, 3, 3, 2, 4, 3, 5, 5, 4, 4, 0, 0, 1, 0x7d };
    int[] valACY = { 0x01, 0x02, 0x03, 0x00, 0x04, 0x11, 0x05, 0x12, 0x21, 0x31, 0x41,
      0x06, 0x13, 0x51, 0x61, 0x07, 0x22, 0x71, 0x14, 0x32, 0x81, 0x91, 0xa1, 0x08, 0x23, 0x42,
      0xb1, 0xc1, 0x15, 0x52, 0xd1, 0xf0, 0x24, 0x33, 0x62, 0x72, 0x82, 0x09, 0x0a, 0x16, 0x17,
      0x18, 0x19, 0x1a, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2a, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39,
      0x3a, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58,
      0x59, 0x5a, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x73, 0x74, 0x75, 0x76, 0x77,
      0x78, 0x79, 0x7a, 0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89, 0x8a, 0x92, 0x93, 0x94, 0x95,
      0x96, 0x97, 0x98, 0x99, 0x9a, 0xa2, 0xa3, 0xa4, 0xa5, 0xa6, 0xa7, 0xa8, 0xa9, 0xaa, 0xb2,
      0xb3, 0xb4, 0xb5, 0xb6, 0xb7, 0xb8, 0xb9, 0xba, 0xc2, 0xc3, 0xc4, 0xc5, 0xc6, 0xc7, 0xc8,
      0xc9, 0xca, 0xd2, 0xd3, 0xd4, 0xd5, 0xd6, 0xd7, 0xd8, 0xd9, 0xda, 0xe1, 0xe2, 0xe3, 0xe4,
      0xe5, 0xe6, 0xe7, 0xe8, 0xe9, 0xea, 0xf1, 0xf2, 0xf3, 0xf4, 0xf5, 0xf6, 0xf7, 0xf8, 0xf9,
      0xfa };

    int[] bitsACCbr = { 0, 2, 1, 2, 4, 4, 3, 4, 7, 5, 4, 4, 0, 1, 2, 0x77 };
    int[] valACCbr = { 0x00, 0x01, 0x02, 0x03, 0x11, 0x04, 0x05, 0x21, 0x31, 0x06,
      0x12, 0x41, 0x51, 0x07, 0x61, 0x71, 0x13, 0x22, 0x32, 0x81, 0x08, 0x14, 0x42, 0x91, 0xa1,
      0xb1, 0xc1, 0x09, 0x23, 0x33, 0x52, 0xf0, 0x15, 0x62, 0x72, 0xd1, 0x0a, 0x16, 0x24, 0x34,
      0xe1, 0x25, 0xf1, 0x17, 0x18, 0x19, 0x1a, 0x26, 0x27, 0x28, 0x29, 0x2a, 0x35, 0x36, 0x37,
      0x38, 0x39, 0x3a, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x53, 0x54, 0x55, 0x56,
      0x57, 0x58, 0x59, 0x5a, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x73, 0x74, 0x75,
      0x76, 0x77, 0x78, 0x79, 0x7a, 0x82, 0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89, 0x8a, 0x92,
      0x93, 0x94, 0x95, 0x96, 0x97, 0x98, 0x99, 0x9a, 0xa2, 0xa3, 0xa4, 0xa5, 0xa6, 0xa7, 0xa8,
      0xa9, 0xaa, 0xb2, 0xb3, 0xb4, 0xb5, 0xb6, 0xb7, 0xb8, 0xb9, 0xba, 0xc2, 0xc3, 0xc4, 0xc5,
      0xc6, 0xc7, 0xc8, 0xc9, 0xca, 0xd2, 0xd3, 0xd4, 0xd5, 0xd6, 0xd7, 0xd8, 0xd9, 0xda, 0xe2,
      0xe3, 0xe4, 0xe5, 0xe6, 0xe7, 0xe8, 0xe9, 0xea, 0xf2, 0xf3, 0xf4, 0xf5, 0xf6, 0xf7, 0xf8,
      0xf9, 0xfa };
    
    void generateTables(){
        //generate code lengths
        int[] huffSize = new int[256];
        int[] huffCode = new int[256];

        huffSize = generateCodeLen(bitsDCY);
        huffCode = generateCodes(huffSize);
        generateTable(huffSize, huffCode, valDCY, 0);
        
        huffSize = generateCodeLen(bitsDCCbr);
        huffCode = generateCodes(huffSize);
        generateTable(huffSize, huffCode, valDCCbr, 0);
         
        huffSize = generateCodeLen(bitsACY);
        huffCode = generateCodes(huffSize);
        generateTable(huffSize, huffCode, valACY, 1);
      
        huffSize = generateCodeLen(bitsACCbr);
        huffCode = generateCodes(huffSize);
        generateTable(huffSize, huffCode, valACCbr, 1);
        
        //view();
    }
    
      private void generateTable(int[] huffSize, int[] huffCode,int[] valCodes, int id){
          
          //create a matrix with 2 columns and length lines which holds [sizeOfCode][huffmanCode]
        int length = (id == 0) ? 12 : 256;

        int[][] tab = new int[length][2];
        for (int i = 0; i < valCodes.length; i++) {
           tab[valCodes[i]][0] = huffSize[i];   // sizeOfCode
           tab[valCodes[i]][1] = huffCode[i];   // code
        }
        
        Table huff = new Table(tab);
        if(id==0)
            listTableDC.add(huff);
        else
            listTableAC.add(huff);
    }
    
    
    private int[] generateCodeLen(int[] bitsArray){
        int[] huffSize = new int[257];
        int count = 0;
        
        //page 71
        for (int length = 1; length <= 16; length++) {
            for (int lenCount = 0; lenCount <bitsArray[length-1]; lenCount++) {
              huffSize[count++] = length;
            }
        }
        return huffSize;
    }
    
    private int[] generateCodes(int[] huffSize){

        //generate huffman codes 
        int code = 0, current = 0;
        int sizeCode = huffSize[0];
        int[] huffCode = new int[257];
        
        while(huffSize[current] != 0){
        while(huffSize[current] == sizeCode){
            huffCode[current++] = code;
            code++;
        }
         code<<=1;
         sizeCode++;
        }
        
        return huffCode;
    }

    private void view(){
        
        System.out.println();
        System.out.println("\nDC");
        for (int i = 0; i < listTableDC.size(); i++) {   
           
           int[][] tab = listTableDC.get(i).getCodeLen();
           for (int j = 0; j < tab.length; j++) {
                System.out.print(tab[j][0]+" "+ tab[j][1]+"\n" );
            }
            System.out.println("\n");
        }   
           
        System.out.println("\nAC");
        for (int i = 0; i < listTableAC.size(); i++) {

            int[][] tab = listTableAC.get(i).getCodeLen();
            for (int j = 0; j < tab.length; j++) {
                System.out.print(tab[j][0]+" "+ tab[j][1]+"\n" );
            }
            System.out.println("\n");
        }
        
    }

    public int[] getBitsACY() {
        return bitsACY;
    }

    public int[] getBitsDCY() {
        return bitsDCY;
    }

    public int[] getBitsDCCbr() {
        return bitsDCCbr;
    }

    public int[] getValACCbr() {
        return valACCbr;
    }

    public int[] getValDCCbr() {
        return valDCCbr;
    }

    public int[] getValACY() {
        return valACY;
    }

    public int[] getValDCY() {
        return valDCY;
    }
    
    public int[] getBitsACCbr() {
        return bitsACCbr;
    }
    
    
}
