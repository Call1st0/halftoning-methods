package halftone;

import java.awt.image.BufferedImage;

public class ArrayGenerator {

    public static int[] createArray(int arraySize) {
        // create threshold array and populate with correct values

        final int ARRLENGHT = arraySize * arraySize;
        int[] arrayLevelDieth = new int[ARRLENGHT];

        for (int i = 0; i < ARRLENGHT; i++) {
            arrayLevelDieth[i] = (int)Math.round(((double)i)* 255.0 / ARRLENGHT) ;
        }


     /*   int[] arrayLevelDieth = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
        for (int i = 0; i < arrayLevelDieth.length; i++) {
            arrayLevelDieth[i] = arrayLevelDieth[i] * 255 / 15;
        }*/

        return arrayLevelDieth;
    }

}
