/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package steganography.Raster;

import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.Utils;
import steganography.Steganography;
import steganography.StegoImage;

/**
 *
 * @author Alex
 */
public class RandomEncode extends StegoImage{
    
    private int initWidth, initHeight;
    private int[] pixels;
    private int index = 0;
    private Queue<Integer> bits = new LinkedList();
    
    public RandomEncode(BufferedImage original, String key, File message, String extension) {
        super(original, key, message, extension);
        this.initHeight = initImage.getHeight();
        this.initWidth = initImage.getWidth();
        pixels = new int[initHeight*initWidth];
    }
    
    public void embed(){
        
        //encode message
        String text = Utils.readFile(this.message);
        String encoded = Utils.toEncode(text);
        encoded+=endSpace;
        
        int nrPixels = randomNr(encoded);
        if(!canEmbed(nrPixels)){
            infWindow("The message is too long");
            return;
        }
  
        Steganography.embedded = true;
        //get pseudorandom positions
        setPixels();
        LFSR rand = new LFSR(initWidth, initHeight, key, nrPixels);
        int[] randNrs = rand.getList();
        
        //embed
        embedId(randNrs);
        embedMsg(encoded, randNrs);
    }
    
    private boolean canEmbed(int nrPixels){  
        return (nrPixels < 65534);
    }
    
    private int randomNr(String encoded){
        
        int nrBitNeeded = encoded.length() * 8 + 16; 
        int nrPixels = ((nrBitNeeded % 3) != 0) ? (nrBitNeeded/3 + 1) : (nrBitNeeded/3);
        return nrPixels;
    }
 
    private void embedMsg(String text, int[] rand){

        for (int i = 0; i < text.length(); i++) {//for each character
            int currentByte = (int)text.charAt(i);
            
            for(int posBit=7; posBit>=0; posBit--){//get each bit of the character
                bits.add(((currentByte>> posBit) & 1));       
            }

            while(bits.size() >= 3) {//embed each bit
               embedPixel(rand);
            }
            //System.out.println("---");
        }
        
        if(!bits.isEmpty())
            remained(rand);
        else
            recreate();
    }
    
    private void embedId(int[] rand){
        
        String start ="R";
        int red, green, blue;
        Queue<Integer> bitsStart = new LinkedList();
        
        for(int posBit=7; posBit>=0; posBit--){
            int v = ((start.charAt(0)>> posBit) & 1);
            bitsStart.add(v);       
        }
        
        for(index=0; index < 3; index++) {
            
            int rgb = pixels[rand[index]]; 
            //embed bits
            red = (((rgb >>16) & 0xFE) | bitsStart.remove());
            green = (((rgb >>8) & 0xFE ) | bitsStart.remove());
            blue = (rgb & 0xFE);
            
            if(index != 2){
                blue |= bitsStart.remove();
            }
     
            rgb = ((red<<16) | ((green<<8)) | (blue)); 
            pixels[rand[index]] = rgb;
            //System.out.println("Index " +" Q "+bitsStart +" "+bitsStart.size());
        }  
    }
    
    private void remained(int[] rand){
        
        int rgb = pixels[rand[index]]; 
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
        pixels[rand[index]] = rgb;
        //System.out.println("Index "+index +" Q "+bits);
        
        recreate();
    }
    
    private void embedPixel(int[] rand){
          
        int rgb = pixels[rand[index]]; 
        int red = (((rgb >>16) & 0xFE) | bits.remove());
        int green = (((rgb >>8) & 0xFE ) | bits.remove());
        int blue = ((rgb & 0xFE) | bits.remove());

        rgb = ((red<<16) | ((green<<8)) | (blue)); 
        pixels[rand[index]] = rgb;
        index++;
        //System.out.println("Index "+index +" Q "+bits +" "+bits.size());
    }
    
    private void setPixels(){
        //create an array of pixels
        PixelGrabber pix = new PixelGrabber(initImage, 0, 0, initWidth, initHeight, true);
     
        try {
            boolean grabPixels = pix.grabPixels();
        } catch (InterruptedException ex) {
            Logger.getLogger(RandomEncode.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        pixels = (int[]) pix.getPixels();
    }
    
    private void recreate(){

        BufferedImage newImage = new BufferedImage(initWidth, initHeight, BufferedImage.TYPE_INT_RGB);    
        newImage.setRGB(0, 0, initWidth, initHeight, pixels, 0, initWidth);
        Utils.createImage(newImage, extension);
    }
    
}
