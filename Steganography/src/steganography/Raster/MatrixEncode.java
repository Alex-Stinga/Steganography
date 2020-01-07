/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package steganography.Raster;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedList;
import java.util.Queue;
import lib.Utils;
import steganography.Steganography;
import steganography.StegoImage;

/**
 *
 * @author Alex
 */
public class MatrixEncode extends StegoImage{
    
    private int lin = 0, col = 0;
    private int initWidth, initHeight;
    private Queue<Integer> bits = new LinkedList();
    
    public MatrixEncode(BufferedImage initImage, String key, File message, String extension) {
        super(initImage, key, message, extension);
        this.initWidth = initImage.getWidth();
        this.initHeight = initImage.getHeight();
    }

    public void embed() {
        
        String message = Utils.readFile(this.message);
       
        //get pseudorandom positions
        LFSR alg = new LFSR(initWidth,initHeight,key);
        int[][] matrix = alg.getMatrix();       
        int nrOne = count(matrix);
        
        //encode
        String encoded = Utils.toEncode(message);
        encoded+=endSpace;
        
        //if nr of required pixels > nr of available pixels
        if((encoded.length() * 8 + 16)/3 >= nrOne){    //message+ (start + end) > nr of position * 3 
            infWindow("The message is too long.");
            return;
        }

        Steganography.embedded = true;
        //embed
        embedId(matrix);
        embedMsg(encoded,matrix);
        
    }
    
    private int count(int[][] matrix){
        //count nr of pixels in which the bits can be hidden
        int nrOne = 0;
         for (int i = 0; i < initWidth; i++) {
             for (int j = 0; j < initHeight; j++) {
               if(matrix[i][j] == 1)
                   nrOne++;
            }  
        }  
        return nrOne;
    }
    
    private void embedId(int[][]matrix){
        
        String start ="M";
        int[] pair;
        int red, green, blue;
        Queue<Integer> bitsStart = new LinkedList();
        
        for(int posBit=7; posBit>=0; posBit--){
            int v = ((start.charAt(0)>> posBit) & 1);   //System.out.println("V "+v);
            bitsStart.add(v);       
        }
        
        for(int i=0;i<3; i++) {
            pair = goodPos(matrix);
            int rgb = initImage.getRGB(pair[0],pair[1]); 

            //embed bits
            red = (((rgb >>16) & 0xFE) | bitsStart.remove());
            green = (((rgb >>8) & 0xFE ) | bitsStart.remove());
            blue = (rgb & 0xFE);
            
            if(i != 2){
                blue |= bitsStart.remove();
            }
            //put bits back in matrix
            rgb = ((red<<16) | ((green<<8)) | (blue)); 
            initImage.setRGB(pair[0],pair[1],rgb);

            //System.out.println("Index " +" Q "+bitsStart +" "+bitsStart.size());
        }  
    }
    
    private void embedMsg(String text, int[][]matrix){
        
        for (int i = 0; i < text.length(); i++) {//for each character
            int currentByte = text.charAt(i);
            
            for(int posBit=7; posBit>=0; posBit--){//get each bit of the character
                bits.add(((currentByte>> posBit) & 1));       
            }

            while(bits.size() >= 3) {//embed each bit
               embedPixel(matrix);
            }
            //System.out.println("---");
        }
        
        if(!bits.isEmpty())
            remained(matrix);
        else
            Utils.createImage(initImage, extension);
    }
    
    private void remained(int[][] matrix){
        
        int[] pair = goodPos(matrix);
        int rgb = initImage.getRGB(pair[0],pair[1]); 
        int red = ((rgb >>16) & 0xFE);
        int green = (((rgb >>8) & 0xFE ));
        int blue = (rgb & 0xFE);   
      
        if(bits.size() == 2){
          //System.out.println("IF");
          red |= bits.remove();
          green |= bits.remove();
        }
        else if (bits.size() == 1){
            //System.out.println("ELSE");
            red |= bits.remove();             
        }
        
        rgb = ((red<<16) | ((green<<8)) | (blue)); 
        initImage.setRGB(pair[0],pair[1],rgb);
        //System.out.println("Index " +" Q "+bits);
        
        Utils.createImage(initImage, extension);
    }
    
    private void embedPixel(int[][] matrix){
          
        //find a good position
        int[] pair = goodPos(matrix);
        int rgb = initImage.getRGB(pair[0],pair[1]); 
        
        //embed bits
        int red = (((rgb >>16) & 0xFE) | bits.remove());
        int green = (((rgb >>8) & 0xFE ) | bits.remove());
        int blue = ((rgb & 0xFE) | bits.remove());
        
        //put bits back in matrix
        rgb = ((red<<16) | ((green<<8)) | (blue)); 
        initImage.setRGB(pair[0],pair[1],rgb);

        //System.out.println("Index " +" Q "+bits +" "+bits.size());
    }
    
    private int[] goodPos(int[][] matrix){
        
        int[] pair = new int[2];
        int val = matrix[lin][col];
        //System.out.println("L "+lin+" C "+col);
        while(val == 0){
            lin++;
            val = matrix[lin][col];   
        if(lin == initWidth-1){col++;lin=0;}
        } 
        pair[0] = lin;
        pair[1] = col;
        
        lin++;                
        if(lin == initWidth-1){col++;lin=0;} 
        return pair;
    }

    
}
