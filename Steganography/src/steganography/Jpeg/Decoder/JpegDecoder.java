/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package steganography.Jpeg.Decoder;

import steganography.Vigenere;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import lib.Utils;

/**
 *
 * @author Alex
 */
public class JpegDecoder {
    
    private BufferedInputStream input;
    private int inputSize;
    private List<DQT> listDQT = new ArrayList<>();
    private List<DHT>huffDC = new ArrayList<>();
    private List<DHT>huffAC = new ArrayList<>();
    private SOF imageData ;
    private int bitPosition = -1;
    private byte currentByte;
    private boolean cannotDecode = false;
    private int[] previousDc = new int[3];
    public static final int[] zigzag = {
            0, 1, 8, 16, 9, 2, 3, 10,
            17, 24, 32, 25, 18, 11, 4, 5,
            12, 19, 26, 33, 40, 48, 41, 34,
            27, 20, 13, 6, 7, 14, 21, 28,
            35, 42, 49, 56, 57, 50, 43, 36,
            29, 22, 15, 23, 30, 37, 44, 51,
            58, 59, 52, 45, 38, 31, 39, 46,
            53, 60, 61, 54, 47, 55, 62, 63
	};
    
    private Queue<Integer> outBits = new LinkedList<>();  
    private String outMsg ="", key; 
    private boolean notEnd = true, hasMsg = true;
    
    public JpegDecoder(File fileImage, String key) {
        try {
            input =  new BufferedInputStream(new FileInputStream(fileImage));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JpegDecoder.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            inputSize = input.available();
        } catch (IOException ex) {
            Logger.getLogger(JpegDecoder.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.key = key;
    }
    
    public boolean decodeJpeg() throws IOException{
        
        if(decodeMarkers() == false){
            decodeScanData();
            input.close();
            return hasMsg;
        }else{
            return false;
        }
       
    }
    
    private boolean decodeMarkers() throws IOException{
        
        byte[] buffer = new byte[2];
        boolean notScan = true;     
        input.read(buffer);

            
        while(inputSize > 0 && notScan){
            input.read(buffer);
            
            if(buffer[0]==(byte)0xFF ){
            
                //System.out.println("Marker "+Integer.toHexString(buffer[1])); 
                int length;
                
            switch(buffer[1]){
                   
                case (byte) 0xDB:  
                    input.read(buffer, 0, 2);
                    readDQT(buffer);
                    break;
               
                case (byte) 0xC4: 
                    input.read(buffer, 0, 2);
                    readDHT(buffer);
                    break;
                    
                case (byte)0xC0:  //baseline
                    input.read(buffer, 0, 2);
                    readSOF(buffer);
                    break;             

                case (byte) 0xDA:
                    input.read(buffer, 0, 2);
                    readSOS(buffer);
                    notScan = false;
                    break;
                    
                case (byte) 0xE0:   //header information  
                    input.read(buffer, 0, 2);
                    length = ((buffer[0] & 0xFF) << 8) | (buffer[1] & 0xFF);
                    //System.out.println("Length skipped "+ length);
                    input.skip(length - 2);
                    break;
                
                 case (byte) 0xFE:      //COM
                    input.read(buffer, 0, 2);
                    length = ((buffer[0] & 0xFF) << 8) | (buffer[1] & 0xFF);
                    //System.out.println("Length skipped "+ length);
                    input.skip(length - 2);
                    cannotDecode = true;
                    break;
                    
                 //not useful
                case (byte)0xC1:   //baseline extended
                case (byte) 0xE1: //start header inf  array       
                case (byte) 0xE2:
                case (byte) 0xE3:
                case (byte) 0xE4:
                case (byte) 0xE5:
                case (byte) 0xE6:
                case (byte) 0xE7:
                case (byte) 0xE8:
                case (byte) 0xE9:
                case (byte) 0xEA:
                case (byte) 0xEB:
                case (byte) 0xEC:
                case (byte) 0xED:
                case (byte) 0xEE:
                case (byte) 0xEF:    //stop header inf array     
                case (byte) 0xC2:    //progressive   
                case (byte) 0xC3:     
                case (byte) 0xC5:  
                case (byte) 0xC6:               
                case (byte) 0xC7: 
                case (byte) 0xC9:    
                case (byte) 0xCA:               
                case (byte) 0xCB:                
                case (byte) 0xCD:              
                case (byte) 0xCE:  
                case (byte) 0xCF: 
                case (byte) 0xDC:
                case (byte) 0xCC:
                case (byte) 0xDD:   //rst
                    cannotDecode = true;
            }    
        
         }//end if
            
        }// end while
        return cannotDecode;
    }
 
    private void readDQT(byte[] buffer) throws IOException {
        
       byte tmp;
       int  length = ((buffer[0] & 0xFF) << 8) | (buffer[1] & 0xFF);
       //System.out.println("Length "+length);
       
       length-=2;
       int precision;    //get first 4 bits  - high
       int index;
       int[] table = new int[64];
       
        while(length > 0){
            tmp = (byte) input.read();
            precision  = (tmp >> 4) & 0xF0;    //get first 4 bits  - high
            index = tmp & 0x0F;   
            length--;
            
            for (int i = 0; i < table.length; i++) {
               table[zigzag[i]] = input.read();
            }
            length-=table.length;
            
            //System.out.println(precision+" "+index+" "+Arrays.toString(table));
            
            DQT quantization = new DQT(index, precision, table);
            listDQT.add(quantization);
        }
       
    }
    
    private void readSOF(byte[] buffer) throws IOException {
       
       int  length = ((buffer[0] & 0xFF) << 8) | (buffer[1] & 0xFF);
       //System.out.println("Length "+length);
       length-=2;
       
       List<Component>listComponents = new ArrayList<>();
       int precision = input.read();//8 or 12 else error ( i work with 8) //Bits per pixel per color component
       int nrLines = ((input.read() & 0xFF) << 8) | (input.read() & 0xFF);   //height    (make sure > 0)
       int nrSamplesLine = ((input.read() & 0xFF) << 8) | (input.read() & 0xFF); //width  >0
       int nrFrames = input.read();   //1=Grayscale or 3=YCbCr <=>color components

       int id,sampleX,sampleY,idQuant,tmp;

       for(int i=0;i<nrFrames;i++){
            id = input.read(); //System.out.print(id+" ");
            tmp = input.read();

            sampleX = (tmp & 0xF0) >> 4;    //System.out.print(sampleX+" ");
            sampleY = tmp & 0xF;    //System.out.print(sampleY+" ");
            idQuant = input.read();    //System.out.println(idQuant+" ");

            Component comp = new Component(id, sampleX, sampleY, idQuant);
            listComponents.add(comp);
        }
       
        //System.out.println("Nr components  "+ listComponents.size());
        if(listComponents.size() == 1)  //components are not interleaved
            System.out.println("1:1:1 => Grayscale");
        else{
            if(listComponents.size() == 3){ //components are interleaved
                System.out.println("RGB");
                if(listComponents.get(0).getSampleX()==1)
                    System.out.println("4:4:4");
                
                if(listComponents.get(0).getSampleX()==2 &&listComponents.get(0).getSampleY()==1 )
                    System.out.println("4:2:2");
                
                if(listComponents.get(0).getSampleX()==2 &&listComponents.get(0).getSampleY()==2 )
                    System.out.println("4:2:0");
            }
        }
              
//        for (int i = 0; i < listComponents.size(); i++) {
//            System.out.println(listComponents.get(i).getId() +" "+ listComponents.get(i).sampleX+" "+ listComponents.get(i).sampleY +" "+ listComponents.get(i).tableId);
//        }
        
        imageData = new SOF(precision, nrLines, nrSamplesLine, nrFrames, listComponents);
    }
    
    private void readDHT(byte[] buffer) throws IOException {
       
       int length = ((buffer[0] & 0xFF) << 8) | (buffer[1] & 0xFF);
       //System.out.println("Length "+length);
       length-=2;
        //DHT Class=0 ID=0   -- DC Y
        //DHT Class=1 ID=0   -- AC Y
        //DHT Class=0 ID=1   -- DC (Cb & Cr)
        //DHT Class=1 ID=1   -- AC (Cb & Cr)
        
        while(length > 0){
            
            byte current = (byte) input.read();
            int tableClass = (current & 0xF0) >> 4;   // 0 = dc or 1 = ac
            int tableId = current & 0xF;              // 0 or 1
            //System.out.println("\nTable class "+tableClass+" "+"Id "+tableId);
            length--;      
            
            //how many Huffman codes there are of each bit length, from 1 to 16, followed by an array that has all of the bytes that the Huffman codes   
            int []nrIhuffCodes = new int[16];
            int sumCodes = 0;       //must be <= 256
                
            int i;
            for (i = 0; i < nrIhuffCodes.length; i++) {
                int lengtHuffCode = input.read();
                sumCodes +=lengtHuffCode;
                nrIhuffCodes[i] = lengtHuffCode;
            }
            //System.out.println();
            length-=16;
            
            int []values = new int[sumCodes];
            for (int j = 0; j < sumCodes; j++) {
                values[j] = input.read() & 0xFF;
            }
           // System.out.println("\n");
            
           //genertae huffman code for each value
            LinkedHashMap<Integer, Integer> huffmanCodes = new LinkedHashMap<>();//code + length
            LinkedHashMap<Integer, Integer> huffmanValues = new LinkedHashMap<>(); //code + value
            int pos = 0;
            int code = 0;
            for (int nrBits = 0; nrBits < nrIhuffCodes.length; nrBits++) {
                int codeLength = nrIhuffCodes[nrBits];
                //System.out.print("Codes of length "+(nrBits+1)+" : ");
                 
                for (int k = 0; k < codeLength; k++) {
                   // System.out.print(values[pos++]+" ");
                    huffmanCodes.put(code, (nrBits+1));
                    huffmanValues.put(code, values[pos++]);
                    //System.out.println("Length "+(nrBits+1)+  " Code "+code+" ( "+Integer.toBinaryString(code)+" ) ");
                    code++;
                }
                
                //System.out.println();
                code<<=1;
            }
            
            
            DHT huff = new DHT(tableClass, tableId, nrIhuffCodes, values, huffmanCodes,huffmanValues);
            if(tableClass == 0)
                huffDC.add(huff);       
            else
                huffAC.add(huff);
            //huff.view();
            length-=sumCodes;
           // System.out.println("\nSum of codes "+ sumCodes+"\n");
        }
       
    }
    
    private void readSOS(byte[] buffer) throws IOException {
       
        int  length = ((buffer[0] & 0xFF) << 8) | (buffer[1] & 0xFF);
        //System.out.println("Length "+length);
        length-=2;
    
        int nrComp = input.read();
        int id,tmp,ac, dc;

       // System.out.println("ID\tDC\tAC");
        for (int i = 0; i < nrComp; i++) {
            id = input.read();
            tmp = input.read();

            dc = (tmp & 0xF0) >> 4;    
            ac = tmp & 0xF;  
          //  System.out.println(id+" "+dc+" "+ac);
        }
        input.skip(3);
    }  
     
    private int nextMultiple(int nr){//get next multiple of 8, which is bigger than nr
        return ((nr + 7) & (-8));
    }
    
    private void decodeScanData() throws IOException{

        int newWidth =  (imageData.getWidth() %8 == 0)? imageData.getWidth() : nextMultiple(imageData.getWidth());
        int newHeight = (imageData.getHeight() %8 == 0)?  imageData.getHeight() : nextMultiple(imageData.getHeight());
        
        int nrColorComp = imageData.getList().size();
        int mcuWidth = imageData.getList().get(0).sampleX * 8;
        int mcuHeight = imageData.getList().get(0).sampleY * 8;
        int nrHmcu = (int) Math.ceil(newWidth/ mcuWidth);   //blocks per width
        int nrVmcu = (int) Math.ceil(newHeight/ mcuHeight); //blocks per height
        System.out.println(mcuWidth+" "+mcuHeight+"\t"+ nrHmcu+" "+nrVmcu+ "\t"+ nrHmcu*mcuWidth+" "+ nrVmcu*mcuHeight);   

        int samplingX, samplingY;
        List<Component> listComp = imageData.getList();
        
        int tableIdComp;
        int[] aCcoeff;
        int mcuCount = 0;
        
        //page 93   //page 103 (ac decoding)
        for (int mcuV = 0; mcuV < nrVmcu && notEnd; mcuV++) { //for each mcu
            for (int mcuH = 0; mcuH < nrHmcu; mcuH++) {
                
                System.out.println("------------------- BLOCK "+ (++mcuCount) +"------------------ ");
                
                for (int comp = 0; comp < nrColorComp; comp++) {//for each component
                   samplingX = listComp.get(comp).getSampleX();
                   samplingY = listComp.get(comp).getSampleY();
                   
                    for (int vSampling = 0; vSampling < samplingY; vSampling++) {
                        for (int hSampling = 0; hSampling < samplingX; hSampling++) {
                            
                            //DC
                            //System.out.println("\nDC");
                            int dcDiff = 0;
                            int coeff;
                            
                            tableIdComp = listComp.get(comp).getTableId();
                            int coeffValueLength = getHuffValue(huffDC.get(tableIdComp));
                            coeff = readCoeffValue(coeffValueLength);
                            dcDiff = extend(coeff, coeffValueLength);   //System.out.println("dcDiff "+ dcDiff);
                            previousDc[comp] = previousDc[comp] + dcDiff;
                                                      
                            //AC
                            //System.out.println("\nAC");
                            aCcoeff = new int[64];
                            aCcoeff[0] = previousDc[comp];
                            
                            int index = 1;
                            while(index < 64){
                                tableIdComp = listComp.get(comp).getTableId();
                                int value = getHuffValue(huffAC.get(tableIdComp));
                                int codeLen = value & 0x0F;
                                int zeroCount = (value & 0xF0) >> 4;//high bits
                                //System.out.println("Value "+value+" Low "+ codeLen +" High "+zeroCount);
                                
                                if(codeLen != 0){
                                    coeff = readCoeffValue(codeLen);
                                    index+=zeroCount;
                                    aCcoeff[zigzag[index]] = extend(coeff, codeLen);
                                    index++; 
                                }                               
                                else{
                                    if(zeroCount == 16)
                                        index+=16;
                                    if(zeroCount == 0)
                                       index = aCcoeff.length ;
                                }
                                   
                            }//end while
//                            System.out.println(Arrays.toString(aCcoeff));
//                            System.out.println();
                            
                            if(notEnd){
                                extract(aCcoeff);
                            }
                        }
                    }
                    
                }//end 3 for        
            }
        }
        
        String copy;
        if(outMsg.charAt(outMsg.length() - 1) == 0){    //if has 0 added at the end remove it
            outMsg = outMsg.substring(0, outMsg.length() - 1);
        }
        copy = outMsg.substring(0, outMsg.length() - 1);    //remove endspace
        
         //decrypt text
        Vigenere decrypt = new Vigenere(copy, key);
        String decrypted = decrypt.getDecrypt();
        
        Pattern pattern = Pattern.compile("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$");         //check if decoded string is base64 
        if (pattern.matcher(decrypted).matches()) {
            //decode base
            byte[] decodedValue = Base64.getDecoder().decode(decrypted); 
            Utils.writeToFile(new String(decodedValue, StandardCharsets.UTF_8.toString())); 
        }
        else{
            hasMsg = false;
        }
      
    }
    
    private int readBit() throws IOException{
    
        if(bitPosition == -1){
            currentByte = (byte) input.read(); 
            
            if(currentByte == (byte)0xFF){
                byte next = (byte) input.read();
                
                if(next == 0)
                   currentByte = (byte) 0xFF;
                
                if(next == (byte)0xD9){
                   System.out.println("This is EOI");
                   input.close(); 
                   System.exit(0);
                }
            }    
            //System.out.println(Integer.toHexString(currentByte));  
            bitPosition = 7;
        }
    
//        int bit = ((currentByte>>(bitPosition--)) & 1);
//        System.out.println("Bit "+ bit);
//        return bit;
        return  ((currentByte>>(bitPosition--)) & 1);
    }
    
    private int getHuffValue(DHT huffTable) throws IOException{
       
        int code = 0;
        int len = 0;
        
        while(!huffTable.hasCode(code, len)){ //find a valid huffman code 
            code = ((code <<1 ) | readBit()); //form the code
            len++;
            
            if(len > 16)           
                throw new Error("This is not a valid huffman code value");
        }

       // System.out.println("Code "+ code+" len "+len+" "+ Integer.toBinaryString(code) +" value "+huffTable.getHuffmanValues().get(code));
        return huffTable.getHuffmanValues().get(code);
    }
    
    private int readCoeffValue(int coeffValueLength) throws IOException{
        
        int coeff = 0;    
        for (int i = 0; i < coeffValueLength; i++) {  
            coeff = ((coeff << 1)| readBit());
        }
        //System.out.println("\nCoef "+ coeff);
        return coeff;
    }
 
    private int extend(int coeff, int lenCoef){
        //page 95
        // converting a magnitude value and additional bits to the difference value
        int vt = (1 << lenCoef - 1);     
        if(coeff < vt)
            return (coeff + (-1 << lenCoef)+1);
        else
            return coeff;
    }
    
    private boolean canExtract(int nr){
        return ((nr == 0) || (nr == 1) || (nr == -1));
    }
     
    private void extract(int[] acCoeff){

        int index = 0,i,j;
        int[][]mat = new int[8][8];
        for (i = 0; i < 8; i++) {
            for (j = 0; j <8; j++) {
               mat[i][j] = acCoeff[index++];
            }
        }

        int max = 8, tmp;
         for(i = 0; i< 8; i++){
            for(j = 0; j< max;j++){
                
                if(!(i == 0 && j == 0)){                   
                    tmp = mat[i][j];
                    
                    if(!canExtract(tmp)){
                        int v = (mat[i][j] & 0xFF) ;
                        outBits.add(v & 1);
                    }
                }
            }//end inner for
            max--;
         }
        
        while(outBits.size() >= 8){
            outMsg+=(char)getChar(outBits);
        }

    }
    
    private int getChar(Queue<Integer> outBits){
        
        int number = 0;
        for (int bit = 0; bit <= 7; bit++) {
            number = (number << 1) | outBits.remove();
        }
        
        //System.out.println("N "+number);
        if(number == 32){
            notEnd = false;
        }
        return number;
    }
    
}
