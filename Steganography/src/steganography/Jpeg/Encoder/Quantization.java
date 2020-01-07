/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package steganography.Jpeg.Encoder;

import static steganography.Jpeg.Decoder.JpegDecoder.zigzag;

/**
 *
 * @author Alex
 */
public class Quantization {
    
    public Quantization() {
    }
    
    static final int[][] dqtY = {
        {16,11,10,16,24,40,51,61},
        {12,12,14,19,26,58,60,55},
        {14,13,16,24,40,57,69,56},
        {14,17,22,29,51,87,80,62},
        {18,22,37,56,68,109,103,77},
        {24,35,55,64,81,104,113,92},
        {49,64,78,87,103,121,120,101},
        {72,92,95,98,112,100,103,99 }
    };
    
    static final int[][] dqtCbr = {
        {17, 18, 24, 47, 99, 99, 99, 99},
        {18, 21, 26, 66, 99, 99, 99, 99},
        {24, 26, 56, 99, 99, 99, 99, 99},
        {47, 66, 99, 99, 99, 99, 99, 99},
        {99, 99, 99, 99, 99, 99, 99, 99},
        {99, 99, 99, 99, 99, 99, 99, 99},
        {99, 99, 99, 99, 99, 99, 99, 99},
        { 99, 99, 99, 99, 99, 99, 99, 99}     
    };
    
    static int[] toArray(int[][] mat){
        int[] tmp = new int[64];
        int index = 0;
        
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                tmp[index++] = mat[i][j];  
            }
        } 
        return tmp;
    }
    
    static int[] toZigzag(int[] vect){
        int[] tmp = new int[64];
        for (int i = 0; i < vect.length; i++) {
            tmp[i] = vect[zigzag[i]];    
        }
        return tmp;
    }
    
    int[] zigzagY(){
        return toZigzag(toArray(dqtY));
    }
    
    int[] zigzagCbr(){
        return toZigzag(toArray(dqtCbr));
    }
}