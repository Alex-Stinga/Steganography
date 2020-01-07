/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package steganography.Raster;

import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.Utils;

/**
 *
 * @author Alex
 */
public class LFSR {
    
    private int max = 16;
    private int initWidth, initHeight;
    private int[][] bits;
    private List<Integer> register;
    private String seed;
    private int length;
    private int[] listBits;
   
    private List<Integer> initRegister(){
        
        //get the hash of password
        String hash = "";
        try {
            hash = Utils.hashPassword(seed);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(LFSR.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //fill the register with binary reprez of hash
        int bit;
        for (int i = 0; i < hash.length(); i++) {

          for(int pos = 7; pos>=0; pos--){//get every bit
               bit = (hash.charAt(i) >> pos)& 1;  //System.out.print(bit);
               register.add(bit);
           }
           // System.out.println();
        }
        return register;
    }

    private int step(){ // fibonacci
       
        int out =  register.get(register.size()-1);//get the output bit
        int taps = register.get(register.size()-1)^ register.get(318)^ register.get(316)^ register.get(315);//xor the bits
        
        register.remove(register.size() - 1);//throw the last bit
        register.add(0, taps);  //insert the new bit
        return out;
    }
       
    private int generate(){
        //form number from the output bits of lfsr
        int number = 0;
        for (int i = 0; i < max; i++) {
            number<<=1;
            number|=step();
        }
        return number;
    }
    
     
    public LFSR(int width, int height,String key) {//for matrix alg
        initHeight = height;
        initWidth = width;
        register = new LinkedList<>();
        bits = new int[initWidth][initHeight];
        seed = key;
    }
    
    private int[][] fillMatrix(){
        
        //fill thr matrix with the output from lfsr
        initRegister();
        for (int i = 0; i < initWidth; i++) {
             for (int j = 0; j < initHeight; j++) {               
                bits[i][j] = step();
            }  
        }      
        return bits;
    }

    public int[][] getMatrix() {   
        int[][] matrix = fillMatrix();
        return matrix;
    }  
    
   
    public LFSR(int width, int height,String key, int inlength) {//for random alg
        initHeight = height;
        initWidth = width;
        register = new LinkedList<>();
        seed = key;
        length = inlength;
        listBits = new int[length];
    }
 
    private int[] fillList(){
        
        initRegister();
        for (int index = 0; index < length; index++) {
            int number = generate(); 
            
            while((number >= initHeight*initWidth) || contains(number, listBits)){
                number = generate();
            }
            listBits[index] = number; 
            //System.out.println(number);
        }
        return listBits;
    }
    
    private boolean contains(int nr, int[] vect){
       boolean check = false;
        for (int i = 0; i < vect.length; i++) {
            if(vect[i] == nr)
                check = true;
        }
        return check;
    }
    
    public int[] getList(){
        int[] randLst = fillList();
        return randLst;
    }
}
