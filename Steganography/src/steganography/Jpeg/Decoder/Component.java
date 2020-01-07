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
public class Component {
    int id;
    int sampleX, sampleY;
    int tableId;    //quantization

    public Component(int id, int sampleX, int sampleY, int tableId) {
        this.id = id;
        this.sampleX = sampleX;
        this.sampleY = sampleY;
        this.tableId = tableId;
    }

    public int getId() {
        return id;
    }

    public int getSampleX() {
        return sampleX;
    }

    public int getSampleY() {
        return sampleY;
    }

    public int getTableId() {
        return tableId;
    }
    
}
