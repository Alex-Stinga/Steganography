/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package steganography.Raster;

import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.Utils;
import steganography.StegoImage;

/**
 *
 * @author Alex
 */
public class RandomDecode extends StegoImage{
    
    private int initWidth, initHeight;
    private int[] pixels;
    private int index  = 0;
    private boolean notEnd = true;
    private final int max = 65534;
    
    public RandomDecode(BufferedImage initImage, String key) {
        super(initImage, key);
        this.initHeight = initImage.getHeight();
        this.initWidth = initImage.getWidth();
    }
    
    public boolean check(){
        return randomExtract();
    }
    
    public boolean randomExtract(){
        
        boolean hasMsg = false;
        
        //generate length numbers
        LFSR rand = new LFSR(initWidth, initHeight, key, 3);
        int[] randPos = rand.getList();
        setPixels();

        int togen = (initHeight*initWidth < max)? (initHeight*initWidth): max;
        
        if(check2(randPos)){
            hasMsg = true;
            rand = new LFSR(initWidth, initHeight, key, togen);
            randPos = rand.getList();
            extractMsg(randPos);      
        }
        return hasMsg;
    }
    
    private boolean check2(int[] randPos){
        
        int rgb, red, blue, green; 
        Queue<Integer> outBits = new LinkedList<>();
        
        for (int i = 0; i < 3; i++) {
            
            rgb = pixels[randPos[index]];
            red = ((rgb >>16) & 0xFF);
            green = ((rgb >>8) & 0xFF);   
            blue = (rgb & 0xFF);
            
            //get the bits from the pixesl
            outBits.add(red & 1);outBits.add(green & 1);outBits.add(blue & 1);
            index++;
        }
        
        int number = 0;
        for (int bit = 0; bit <=7; bit++) {
            number = (number << 1) | outBits.remove();//System.out.println("N "+number);
        }
        //System.out.println("Number "+ number +" "+ (char)number);
        
        return ((char)number == 'R');
    }
    
    private void extractMsg(int[] rand){
        
        int rgb, red, blue, green; 
        String outMsg ="";     
        Queue<Integer> outBits = new LinkedList<>();                
        
        for (int i = 0; i < rand.length && notEnd; i++) {
            rgb = pixels[rand[index]];   
            red = ((rgb >>16) & 0xFF);
            green = ((rgb >>8) & 0xFF );   
            blue = (rgb & 0xFF);
            
            //get the bits from the pixesl
            outBits.add(red & 1);outBits.add(green & 1);outBits.add(blue & 1);
            
            //form the word
            while(outBits.size() >= 8){
                outMsg+=(char)getChar(outBits);
            }
            index++;
        }
        
        String dec = Utils.fromDecode(outMsg);
        Utils.writeToFile(dec);
    }
    
    private int getChar(Queue<Integer> outBits){
        
        int number = 0;
        for (int bit = 0; bit <=7; bit++) {
            number = (number << 1) | outBits.remove();
        }
       
        //System.out.println("N "+ number);
        if(number == 32)
            notEnd = false;
        return number;
    }
    
    private void setPixels(){
        //create an array of pixels
        PixelGrabber pix = new PixelGrabber(initImage, 0, 0, initWidth, initHeight, true);
     
        try {
            boolean grabPixels = pix.grabPixels();
        } catch (InterruptedException ex) {
            Logger.getLogger(RandomDecode.class.getName()).log(Level.SEVERE, null, ex);
        }
       
        pixels = (int[]) pix.getPixels();
    }
    
}
