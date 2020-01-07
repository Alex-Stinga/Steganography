/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package steganography.Jpeg.Encoder;

/**
 *
 * @author Alex
 */
public class Table {
    int[][] codeLen;

    public Table(int[][] codeLen) {
        this.codeLen = codeLen;
    }

    public int[][] getCodeLen() {
        return codeLen;
    }
    
    
}
