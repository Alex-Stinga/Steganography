/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package steganography.Jpeg.Decoder;

/**
 *
 * @author Alex
 */
public class DQT {
    
    int id;
    int precision;
    public int[] values;

    public DQT(int id, int precision, int[] values) {
        this.id = id;
        this.precision = precision;
        this.values = values;
    }
    
    public int getId(){
        return id;
    }
    
    public int[] getValues(){
        return values;
    }
}
