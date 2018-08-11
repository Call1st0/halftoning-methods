package halftone;

import java.awt.image.BufferedImage;

public class ArrayGenerator {

    public static int[] createArray() {
        // create threshold array and populate with correct values
        BufferedImage arrayLevel = new BufferedImage(4,4, BufferedImage.TYPE_BYTE_BINARY);
        int[] arrayLevelDieth = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
        for (int i = 0; i < arrayLevelDieth.length; i++) {
            arrayLevelDieth[i] = arrayLevelDieth[i] * 255 / 15;
        }

        // set pixels of the threshold array
        arrayLevel.getRaster().setPixels(0,0,4,4,arrayLevelDieth);
        return arrayLevelDieth;
    }

}
