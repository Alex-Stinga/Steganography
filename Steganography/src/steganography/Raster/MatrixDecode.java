/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package steganography.Raster;

import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.Queue;
import lib.Utils;
import steganography.StegoImage;

/**
 *
 * @author Alex
 */
public class MatrixDecode extends StegoImage{
    
    private int lin = 0, col = 0;
    private int initWidth, initHeight;
    private boolean notEnd = true;

    public MatrixDecode(BufferedImage initImage, String key) {
        super(initImage, key);
        initWidth = initImage.getWidth();
        initHeight = initImage.getHeight();
    }
    
    public boolean check(){
        return matrixExtract();
    }
    
    private boolean matrixExtract(){
        
        boolean hasMsg = false;
        LFSR alg = new LFSR(initWidth,initHeight,key);
        int[][] matrix = alg.getMatrix();   
        
        if(check1(matrix)){   
            hasMsg = true;
            extractMsg(matrix);
        }
        
        return hasMsg;
    }
    
    private boolean check1(int[][] matrix){
        
        int rgb, red, blue, green; 
        int[] pair;
        Queue<Integer> outBits = new LinkedList<>();
        
        for (int i = 0; i < 3; i++) {
            pair = goodPos(matrix);
            rgb =  initImage.getRGB(pair[0],pair[1]);      
            red = ((rgb >>16) & 0xFF);
            green = ((rgb >>8) & 0xFF );   
            blue = (rgb & 0xFF);
            
            //get the bits from the pixesl
            outBits.add(red & 1);outBits.add(green & 1);outBits.add(blue & 1);
        }
        
        int number = 0;
        for (int bit = 0; bit <=7; bit++) {
            number = (number << 1) | outBits.remove();
        }
        //System.out.println("Number "+ number +" "+ (char)number);
        
        return ((char)number == 'M');
    }
    
    private void extractMsg(int[][] matrix){
        
        int rgb, red, blue, green; 
        String outMsg ="";   
        Queue<Integer> outBits = new LinkedList<>();                
        int[] pair;
        
        while(notEnd) {
            
            pair = goodPos(matrix);
            rgb =  initImage.getRGB(pair[0],pair[1]);      
            red = ((rgb >>16) & 0xFF);
            green = ((rgb >>8) & 0xFF );   
            blue = (rgb & 0xFF);
            
            //get the bits from the pixesl
            outBits.add(red & 1);outBits.add(green & 1);outBits.add(blue & 1);
            
            //form the word
            while(outBits.size() >= 8){
                outMsg+=(char)getChar(outBits);
            }
        }
        
        String dec = Utils.fromDecode(outMsg);
        Utils.writeToFile(dec);
    }
    
    
    private int getChar(Queue<Integer> outBits){
        
        int number = 0;
        for (int bit = 0; bit <=7; bit++) {
            number = (number << 1) | outBits.remove();
        }

        if(number == 32){
            notEnd = false;
        }
        return number;
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
