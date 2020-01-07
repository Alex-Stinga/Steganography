/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package steganography;

import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.Utils;

/**
 *
 * @author Alex
 */
public class Vigenere {
    
    private final String alphabeth = "=/+0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private String text, key, hash;
    
    public Vigenere(String text, String key) {
        this.text = text;
        this.key = key;
    }
    
    public String getEncrypt(){
        return encrypt();
    }
    
    public String getDecrypt(){
        return decrypt();
    }
    
    private void init(){
        try {
            hash = Utils.hashPassword(key);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Vigenere.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private int getValue(char val){
        return alphabeth.indexOf(val);
    }
    
    private String encrypt(){
      
        init();System.out.println(hash);
        String enc = "";
        int k = 0;
        
        for (int cha = 0; cha < text.length(); cha++) {
            
            if(k >= 40)
                k = 0;
                
            int cipher = (getValue(text.charAt(cha)) + getValue(hash.charAt(k))) % alphabeth.length();
            k++;
            enc+=(alphabeth.charAt(cipher));
        }
        System.out.println(enc);
        return enc;
    }
    
    private String decrypt(){
        init();
        String dec = "";
        int k = 0;
        
        for (int i = 0; i < text.length(); i++) {
           
             if(k >= 40)
                k = 0;
             
              int cipher = ((getValue(text.charAt(i)) - getValue(hash.charAt(k))) +  alphabeth.length())%  alphabeth.length();
              k++;
              dec+=alphabeth.charAt(cipher);
        }
        System.out.println(dec);
        return dec;
    }
    
}
