/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package steganography;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Alert;
import steganography.Jpeg.Decoder.JpegDecoder;
import steganography.Jpeg.Encoder.JpegEncoder;
import steganography.Raster.MatrixDecode;
import steganography.Raster.MatrixEncode;
import steganography.Raster.RandomDecode;
import steganography.Raster.RandomEncode;

/**
 *
 * @author Alex
 */
public class StegoImage {
    
    protected BufferedImage initImage;
    protected String key;
    protected File message;
    protected String extension;
    protected String endSpace = " ";

    public StegoImage(BufferedImage initImage, String key, File message, String extension) { //embed
        this.initImage = initImage;
        this.key = key;
        this.message = message;
        this.extension = extension;
    }
    
    public StegoImage(BufferedImage initImage, String key) { //extract
        this.initImage = initImage;
        this.key = key;
    }
    
    public StegoImage(BufferedImage initImage, String key, File message) { //embed j
        this.initImage = initImage;
        this.key = key;
        this.message = message;
    }
    
    public void encode(String id){
        
        switch(id){
            case "Matrix":
                MatrixEncode matEnc = new MatrixEncode(initImage, key, message,extension);
                matEnc.embed();
                break;
            case "Random":
                RandomEncode ranEnc = new RandomEncode(initImage, key, message, extension);
                ranEnc.embed();
                break;
        }
    }
    
    public void decode() {
        
        MatrixDecode matDec = new MatrixDecode(initImage, key);
        RandomDecode ranDec = new RandomDecode(initImage, key);
        
        if(matDec.check()){   
        }else{
            if(ranDec.check()){
            }else{
                infWindow("There is no secret message.");
            }
        }

    }
    
    public void encode(){
        
        JpegEncoder jpgEnc = new JpegEncoder(initImage, key, message);
        jpgEnc.encodeJpg();
    }
     
    public void decodeJpeg(File imageFile) {
        
        try {
            JpegDecoder jpgDec = new JpegDecoder(imageFile, key);
            if(jpgDec.decodeJpeg()){
            }else{
                infWindow("There is no secret message.");
            }  
                
        } catch (IOException ex) {
            Logger.getLogger(StegoImage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    protected void infWindow(String exception){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setContentText(exception);
        alert.setHeaderText(null);
        alert.showAndWait();  
    }
    
}
