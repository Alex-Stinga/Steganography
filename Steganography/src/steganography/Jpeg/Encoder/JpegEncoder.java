/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package steganography.Jpeg.Encoder;

import steganography.Vigenere;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.Utils;
import static steganography.Jpeg.Encoder.Quantization.toArray;
import static steganography.Jpeg.Encoder.Quantization.toZigzag;
import steganography.Steganography;
import steganography.StegoImage;

/**
 *
 * @author Alex
 */
public class JpegEncoder extends StegoImage{
    
    private static final int N = 8;
    private static final int nrComp = 3;
    private File f;
    private BufferedOutputStream output;
    private int initWidth,initHeight, newWidth, newHeight;
    private List<float[][]> listYCbCr = new ArrayList<>();
    
    private int[] compId = { 1, 2, 3 };
    private int[] HsampFac = { 1, 1, 1 };
    private int[] VsampFac = { 1, 1, 1 };
    private int[] tblNr = { 0, 1, 1 };
    private int[] dcIdTbl = { 0, 1, 1 };
    private int[] acIdTbl = { 0, 1, 1 };
    Huffman huff = new Huffman();
    
    List<Table> listTableDC = huff.listTableDC;    
    List<Table> listTableAC = huff.listTableAC;  
    private int nrBitsBuffer = 0;
    private int bitsBuffer;
    
    private int bitPosition = -1;
    private int currentByte;
    
    public JpegEncoder(BufferedImage initImage,String key, File messageFile) {
        super(initImage, key, messageFile);
        initWidth = initImage.getWidth();
        initHeight = initImage.getHeight();
        //messageF = Utils.readFile(messageFile);
        huff.generateTables();
    }
    
    public void encodeJpg(){ 
        
        int messageSize = getMessageArray().size();
        if(messageSize >= 1000){
            infWindow("The message file either is empty or too large.");
            return;
        }
        
        encodeImage();
        Steganography.embedded = true;
    }
    
    private void encodeImage(){
        
        String userHomeFolder = System.getProperty("user.home");  
        f = new File(userHomeFolder+"\\Desktop\\stegoImage.jpg");
        try {
            output = new BufferedOutputStream(new FileOutputStream(f));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JpegEncoder.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        checkDimensions();
        BufferedImage newImage = paddedImage();
        toYCbCr(newImage);
        writeHeaders();
        encodeJpeg();
        WriteEOI();
    }

    private Queue getMessageArray(){
       
        //encode 
        String message = Utils.readFile(this.message);
        String tmpMsg = Utils.toEncode(message);
        
        //encrypt
        Vigenere encrypt = new Vigenere(tmpMsg, key);
        String encrypted = encrypt.getEncrypt();
        
        Queue<Integer> messageStream = new LinkedList<>();

        for (int i = 0; i < encrypted.length(); i++) {  
           messageStream.add((int)encrypted.charAt(i));
        }       
        messageStream.add((int)endSpace.charAt(0));
        messageStream.add(0);
        
        System.out.println(messageStream);

        return messageStream;
    }
    
    private void writeHeaders() {
        WriteSOI();
        WriteAPP();
        WriteDQT();
        WriteSOF();
        WriteDHT();
        WriteSOS();
    }
    
    private void WriteSOI() {
        byte[] SOI = { (byte) 0xFF, (byte) 0xD8 };
        try {
            writeMarker(SOI);
        } catch (IOException ex) {
            Logger.getLogger(JpegEncoder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void WriteAPP() {
        
        byte JFIF[] = new byte[18];
        JFIF[0] = (byte) 0xff;
        JFIF[1] = (byte) 0xe0;
        JFIF[2] = (byte) 0x00;
        JFIF[3] = (byte) 0x10;
        JFIF[4] = (byte) 0x4a;
        JFIF[5] = (byte) 0x46;
        JFIF[6] = (byte) 0x49;
        JFIF[7] = (byte) 0x46;
        JFIF[8] = (byte) 0x00;
        JFIF[9] = (byte) 0x01;
        JFIF[10] = (byte) 0x00;
        JFIF[11] = (byte) 0x00;
        JFIF[12] = (byte) 0x00;
        JFIF[13] = (byte) 0x01;
        JFIF[14] = (byte) 0x00;
        JFIF[15] = (byte) 0x01;
        JFIF[16] = (byte) 0x00;
        JFIF[17] = (byte) 0x00;
        try {  
            writeArray(JFIF);
        } catch (IOException ex) {
            Logger.getLogger(JpegEncoder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void WriteDQT(){
         
        int index = 5;
        int[] tmp = new int[N*N];
        Quantization dqt = new  Quantization();
        tmp = dqt.zigzagY();
        
        byte DQT[] = new byte[134];
        DQT[0] = (byte) 0xFF;
        DQT[1] = (byte) 0xDB;
        DQT[2] = (byte) 0x00;
        DQT[3] = (byte) 0x84;
        DQT[4] = (byte) 0x00;   //id
        for (int i = 0; i < 64; i++) {
            DQT[index++] = (byte) tmp[i];
        }
        tmp = dqt.zigzagCbr();
        DQT[index++] = (byte) 0x1;
        for (int i = 0; i < 64; i++) {
            DQT[index++] = (byte) tmp[i];
        }    
        
        try {
            writeArray(DQT);
        } catch (IOException ex) {
            Logger.getLogger(JpegEncoder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void WriteSOF() {
        
        int index;
        
        byte SOF[] = new byte[19];
        SOF[0] = (byte) 0xFF;
        SOF[1] = (byte) 0xC0;
        SOF[2] = (byte) 0x00;
        SOF[3] = (byte) 17;
        SOF[4] = (byte) N;  //precision
        SOF[5] = (byte) ((initHeight >> 8) & 0xFF);
        SOF[6] = (byte) (initHeight & 0xFF);
        SOF[7] = (byte) ((initWidth >> 8) & 0xFF);
        SOF[8] = (byte) ((initWidth) & 0xFF);
        SOF[9] = (byte) nrComp;
        index = 10;
        for (int i = 0; i < nrComp; i++) {
          SOF[index++] = (byte) compId[i];
          SOF[index++] = (byte) ((HsampFac[i] << 4) + VsampFac[i]);
          SOF[index++] = (byte) tblNr[i];
        }
        try {
            writeArray(SOF);
        } catch (IOException ex) {
            Logger.getLogger(JpegEncoder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void WriteDHT(){
        
        Huffman huff = new Huffman();
        int index = 5, length = 418;
        int[] tmpBits = new int[16];
        int[] tmpVal;
        
        byte DHT[] = new byte[420];
        DHT[0] = (byte) 0xFF;
        DHT[1] = (byte) 0xC4;
        DHT[2] = (byte) ((length >> 8) & 0xFF);
        DHT[3] = (byte) (length & 0xFF);
        DHT[4] = (byte) 0x00;
        
        tmpBits = huff.getBitsDCY();
        tmpVal = huff.getValDCY();
        for (int i = 0; i < tmpBits.length; i++) {
            DHT[index++] = (byte) tmpBits[i];
        }    
        for (int i = 0; i < tmpVal.length; i++) {
            DHT[index++] = (byte) tmpVal[i]; 
        }
        
        tmpBits = huff.getBitsACY();
        tmpVal = huff.getValACY();
        DHT[index++] = (byte) 0x10;
        for (int i = 0; i < tmpBits.length; i++) {
            DHT[index++] = (byte) tmpBits[i];
        }
        for (int i = 0; i < tmpVal.length; i++) {
            DHT[index++] = (byte) tmpVal[i];
        }
        
        tmpBits = huff.getBitsDCCbr();
        tmpVal = huff.getValDCCbr();
        DHT[index++] = (byte) 0x01;
        for (int i = 0; i < tmpBits.length; i++) {
            DHT[index++] = (byte) tmpBits[i];
        }
        for (int i = 0; i < tmpVal.length; i++) {
            DHT[index++] = (byte) tmpVal[i];
        }
        
        tmpBits = huff.getBitsACCbr();
        tmpVal = huff.getValACCbr();
        DHT[index++] = (byte) 0x11;
        for (int i = 0; i < tmpBits.length; i++) {
            DHT[index++] = (byte) tmpBits[i];
        }
        for (int i = 0; i < tmpVal.length; i++) {
            DHT[index++] = (byte) tmpVal[i];
        }
        try {
            writeArray(DHT);
        } catch (IOException ex) {
            Logger.getLogger(JpegEncoder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void WriteSOS(){
        
        int index = 5, length = 12;
        
        byte SOS[] = new byte[14];
        SOS[0] = (byte) 0xFF;
        SOS[1] = (byte) 0xDA;
        SOS[2] = (byte) ((length >> 8) & 0xFF);
        SOS[3] = (byte) (length & 0xFF);
        SOS[4] = (byte) nrComp;
        for (int i = 0; i < nrComp; i++) {
          SOS[index++] = (byte) compId[i];
          SOS[index++] = (byte) ((tblNr[i] << 4) + tblNr[i]);
        }
        SOS[index++] = (byte) 0x00;
        SOS[index++] = (byte) 0x3F;
        SOS[index++] = (byte) 0x00;
        try {
            writeArray(SOS);
        } catch (IOException ex) {
            Logger.getLogger(JpegEncoder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void WriteEOI(){
        byte[] EOI = { (byte) 0xFF, (byte) 0xD9 };
        try {
            writeMarker(EOI);
        } catch (IOException ex) {
            Logger.getLogger(JpegEncoder.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            output.flush();
        } catch (IOException ex) {
            Logger.getLogger(JpegEncoder.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            output.close();
        } catch (IOException ex) {
            Logger.getLogger(JpegEncoder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void writeMarker(byte[] marker) throws IOException {
        output.write(marker, 0, 2);
    }

    private void writeArray(byte[] marker) throws IOException {//get the length from the marker itfelf,assemble it and add 2(for the marker)
        int length = ((marker[2] & 0xFF) << 8) + (marker[3] & 0xFF) + 2;//when creating the marker, the 2 bytes for the marker are not added
        output.write(marker, 0, length);
    }

    private void checkDimensions(){
        newWidth =  (initWidth %8 == 0)? initWidth : nextMultiple(initWidth);
        newHeight = (initHeight %8 == 0)? initHeight : nextMultiple(initHeight);
    }
    
    private int nextMultiple(int nr){
        return ((nr + 7) & (-8));
    }
    
    private BufferedImage paddedImage(){
                
        BufferedImage newImage = new BufferedImage(newWidth, newHeight, initImage.getType());
        int[] v;
        int pixel;
        for (int i = 0; i < newWidth; i++) {
            for (int j = 0; j < newHeight; j++) {             
                try{
                    v = rgb(initImage, i, j);
                    pixel = (v[0]<<16) | (v[1]<<8) | v[2];
                    newImage.setRGB(i, j, pixel);    
                }catch(ArrayIndexOutOfBoundsException ex){ }     
            }
        }        
        return newImage;
    }
    
    private float C(int u){
        if(u==0)
            return (float) (1.0/(Math.sqrt(2.0)));
        return (float) 1.0;
    }
    
    private float[][] DCT(float[][] dct){     
       float[][] DCT = new float[N][N]; //DCT e [-1024,1023]
   
        float sum;
          for (int u = 0; u < N; u++) {
              for (int v = 0; v < N; v++) {
                  sum=0;            
                  for (int pix = 0; pix < N; pix++) {//each block
                      for (int piy = 0; piy < N; piy++) {
                         sum+= ((float)dct[pix][piy] ) * Math.cos( ((2*pix+1)*(u*Math.PI))/16 )  * Math.cos( ((2*piy+1)*(v*Math.PI))/16 );       
                      }   
                    DCT[u][v] = (float)(0.25*(sum*C(u)*C(v)));
                }
                   
              }   
          }
//          System.out.println("DCT "+Arrays.deepToString(DCT).replace("], ", "]\n").replace("[[", "[").replace("]]", "]"));
//          System.out.println();
      return DCT;
    }  
    
    private int[][] quantize(float[][] block, int id){
        int[][] quantzed = new int[N][N];    
        int[][] table;
         
        if(id == 0)
            table = Quantization.dqtY;
        else
            table = Quantization.dqtCbr;
        
        for (int i = 0; i < N; i++) {
             for (int j = 0; j < N; j++) {
                quantzed[i][j] = (int) Math.round((block[i][j]/table[i][j]));
             }
        }
//        System.out.println("DQT "+Arrays.deepToString(quantzed).replace("], ", "]\n").replace("[[", "[").replace("]]", "]"));
//        System.out.println();
        return quantzed;
    }
    
    private void encodeJpeg(){

        int mcuWidth = HsampFac[0] * 8;//?
        int mcuHeight = VsampFac[0] * 8;
        int nrHmcu = (int) Math.ceil(newWidth/ mcuWidth);   //blocks per width
        int nrVmcu = (int) Math.ceil(newHeight/ mcuHeight); //blocks per height
        System.out.println(mcuWidth+" "+mcuHeight+"\t"+ nrHmcu+" "+nrVmcu+ "\t"+ nrHmcu*mcuWidth+" "+ nrVmcu*mcuHeight);   
        System.out.println("Nr mcu "+ nrHmcu*nrVmcu);
        
        int[] previousDc = new int[nrComp];
        int samplingX, samplingY;
      //  int mcuCount = 0;
        float[][] ycbcr;
        float[][] tmp = new float[N][N];
        float[][] copyDCT =  new float[N][N];
        int[][] copyQuant =  new int[N][N];
        int[][] bitsEmbded = new int[N][N];
        
        Queue messageBytes = getMessageArray();
        
        for (int mcuV = 0; mcuV < nrVmcu; mcuV++) { //for each mcu
            for (int mcuH = 0; mcuH < nrHmcu; mcuH++) {
                
               // System.out.println("------------------- BLOCK "+ (++mcuCount) +"------------------ ");
                
                for (int comp = 0; comp < nrComp; comp++) {//for each component
                   samplingX = VsampFac[comp];
                   samplingY = HsampFac[comp];
                   ycbcr = (float[][]) listYCbCr.get(comp);
                   
                    for (int vSampling = 0; vSampling < samplingY; vSampling++) {
                        for (int hSampling = 0; hSampling < samplingX; hSampling++) {
                           
                            for (int k = 0; k < N; k++) {//take the block
                                for (int l = 0; l < N; l++) {
                                    tmp[k][l] =  ycbcr[l+mcuH*8][k+mcuV*8] - 128;
                                    //System.out.print(tmp[k][l]+" ");
                                }
                                //System.out.println();
                            }
  
                            copyDCT = DCT(tmp);
                            copyQuant = quantize(copyDCT, tblNr[comp]);                                                     
                            bitsEmbded = embed(copyQuant, messageBytes); //steganography
                            encodeDc(copyQuant[0][0] - previousDc[comp],dcIdTbl[comp]); 
                            encodeAC(copyQuant, acIdTbl[comp]);
                            previousDc[comp] = copyQuant[0][0];
                        }
                    }
                    
                }
            }
        }
        try {
            output.flush();
        } catch (IOException ex) {
            Logger.getLogger(JpegEncoder.class.getName()).log(Level.SEVERE, null, ex);
        }
        remained();
    }
    
    private int[][] embed(int[][] block, Queue messageBytes){
        
        int[][] copy = block;
        if(messageBytes.isEmpty()){
            return copy;
        }   
       
        int tmp, currentBit, copyNr, chCopyNr;
        int max = 8,i,j;
        
        for(i = 0; i< N; i++){
            for(j = 0; j< max;j++){
                
                if(!(i == 0 && j == 0)){                   
                    tmp = copy[i][j];
                    if(!canEmbed(tmp)){
                        
                       if(bitPosition == -1 && !messageBytes.isEmpty()){ 
                           bitPosition = 7;
                           currentByte = (int) messageBytes.remove();
                       }
                       currentBit = ((currentByte>>(bitPosition--)) & 1); 
                       
                       if(tmp < 0){
                            copyNr = -tmp;  //make the number positive
                            chCopyNr = ((copyNr & 0xFE) | currentBit);  //set lsb
                         //   System.out.println("Bit: "+ currentBit+" Val "+tmp+" "+Integer.toBinaryString(tmp)+" "+chCopyNr+" "+" "+(~chCopyNr)+Integer.toBinaryString(~chCopyNr)+ " "+currentByte);
                            copy[i][j] = ~chCopyNr + 1; //back to negative (2'C)
                        }else{
                          //  System.out.println("Bit: "+ currentBit+" Val "+tmp+" "+(tmp & 0xFE)+" "+((tmp & 0xFE) | currentBit)+" M "+currentByte);                        
                            copy[i][j] = ((tmp & 0xFE) | currentBit);
                       }
                    }     
                }//end if         
            }
            max--;
        }       
        return copy;
    }

    private boolean canEmbed(int nr){
        return ((nr == 0) || (nr == 1) || (nr == -1));
    }

    private void encodeDc(int dcCoeff, int dcTable) {
         //page 115
        int[][] nrBits_code = listTableDC.get(dcTable).getCodeLen();
        int value,extraBits, magnitude = 0;
        
        value = dcCoeff;
        extraBits = dcCoeff;
        
        if(value < 0){
            value = -value;
            extraBits = ~value; // 1'C
        }
        while(value != 0){//calculate nr of bits on which the value can be represented 
            value>>=1;
            ++magnitude;
        }
//        System.out.println("DC");
//        System.out.println("New coeff "+extraBits + " magnitude "+magnitude);     

        writeBits(nrBits_code[magnitude][0], nrBits_code[magnitude][1]);
        if(magnitude != 0 ){
            writeBits(magnitude, extraBits);    //write extrabits
        }
    }
    
    private void encodeAC(int block[][], int acTable){

        //page 116
        int[] acCoeff = toZigzag(toArray(block));
        int zeroRun = 0, index = 1;
        int magnitude, coeff, acEncoded, copy;   
        int[][] nrBits_code = listTableAC.get(acTable).getCodeLen(); 
        
       // System.out.println("AC");
        
        while (index < 64) {
        coeff = acCoeff[index];
        if (coeff == 0) {
              zeroRun++;
        }else {
          
          while (zeroRun >= 16) {   //zrl
              writeBits(nrBits_code[0xF0][0], nrBits_code[0xF0][1]);
              zeroRun -= 16;
          }

          copy = coeff;
          if(coeff < 0){
              coeff = -coeff;
              copy = ~coeff;
          }
          magnitude = 0;
          while(coeff != 0){
              coeff>>=1;
              ++magnitude;
          }

          acEncoded = (zeroRun << 4) | magnitude;
          writeBits(nrBits_code[acEncoded][0], nrBits_code[acEncoded][1]);
          if(magnitude != 0){
            writeBits(magnitude ,copy);
          }
          zeroRun = 0;
        }
        index++;
    }
        
        if(zeroRun > 0){ //eob
            writeBits( nrBits_code[0x00][0],  nrBits_code[0x00][1]);
        }
    }

    private void writeBits(int nrBits, int code){
 
        int PutBuffer = code;
        int PutBits = nrBitsBuffer;

        PutBuffer &= (1 << nrBits) - 1;
        PutBits += nrBits;
        PutBuffer <<= 24 - PutBits;
        PutBuffer |= bitsBuffer;

        while (PutBits >= 8) {
          int c = ((PutBuffer >> 16) & 0xFF);
          try {
            output.write(c);
          } catch (IOException e) {
            System.out.println("IO Error: " + e.getMessage());
          }
          if (c == 0xFF) {
            try {
              output.write(0);
            } catch (IOException e) {
              System.out.println("IO Error: " + e.getMessage());
            }
          }
          PutBuffer <<= 8;
          PutBits -= 8;
        }
        bitsBuffer = PutBuffer;
        nrBitsBuffer = PutBits;
    }
    
    private void remained(){
      
        int PutBuffer = bitsBuffer;
        int PutBits = nrBitsBuffer;

        while (PutBits >= 8) {
          int current = ((PutBuffer >> 16) & 0xFF);
            try {
                output.write(current);
            } catch (IOException ex) {
                Logger.getLogger(JpegEncoder.class.getName()).log(Level.SEVERE, null, ex);
            }
          if(current == 0xFF) {
              try {
                  output.write(0);
              } catch (IOException ex) {
                  Logger.getLogger(JpegEncoder.class.getName()).log(Level.SEVERE, null, ex);
              }
            }
          PutBuffer <<= 8;
          PutBits -= 8;
        }

        if (PutBits > 0) {
          int current = ((PutBuffer >> 16) & 0xFF);
            try {
                output.write(current);
            } catch (IOException ex) {
                Logger.getLogger(JpegEncoder.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void toYCbCr(BufferedImage newImage){

        float[][] Y = new float[newWidth][newHeight], Cb =  new float[newWidth][newHeight],Cr =  new float[newWidth][newHeight];
        for (int j = 0; j < newWidth; j++) { 
             for (int k = 0; k < newHeight; k++) {
                 int v[] = rgb(newImage, j, k);
                 Y[j][k] = ((int)(0.299 * v[0] + 0.587 * v[1] + 0.114 * v[2]));
                 Cb[j][k] = (int)((-0.169 * v[0] - 0.331 * v[1] + 0.500 * v[2]) ) +128;
                 Cr[j][k] = (int)((0.500 * v[0] - 0.419 * v[1] - 0.081 * v[2]) ) +128;
             }
         } 
        
       // System.out.println("Y"+Arrays.deepToString(Y).replace("], ", "]\n").replace("[[", "[").replace("]]", "]"));
        listYCbCr.add(Y);
        listYCbCr.add(Cb);
        listYCbCr.add(Cr);

    }
    
    private int[] rgb(BufferedImage copy, int col,int lin){
        int[] v = new int[3];
        int p = copy.getRGB(col,lin);
        
        v[0] = (p>>16) & 0xff; //r
        v[1] = (p>>8) & 0xff;   //g
        v[2] = p & 0xff;    //b
        
        return  v;
   }
}
