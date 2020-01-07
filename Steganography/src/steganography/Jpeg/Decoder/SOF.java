/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package steganography.Jpeg.Decoder;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Alex
 */
public class SOF {
    
    int precision;
    int height;
    int width;
    int nrFrames; //1=Grayscale or 3=YCbCr <=>color components
    List<Component> list = new ArrayList<>();

    SOF(int precision, int nrLines, int nrSamplesLine, int nrFrames, List<Component> listComponents) {
       this.precision = precision;
       height = nrLines;
       width = nrSamplesLine;
       this.nrFrames = nrFrames;
       list = listComponents;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getPrecision() {
        return precision;
    }

    public int getNrFrames() {
        return nrFrames;
    }

    public List<Component> getList() {
        return list;
    }
}
