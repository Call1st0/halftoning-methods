package halftone;

import java.util.Random;

public class ArrayGenerator {

    public static int[] createArray(int arraySize) {
        // create threshold array and populate with correct values

        final int ARRLENGHT = arraySize * arraySize;
        int[] arrayLevelDieth = new int[ARRLENGHT];

        for (int i = 0; i < ARRLENGHT; i++) {
            arrayLevelDieth[i] = (int)Math.round(i* 255.0 / ARRLENGHT) ;
        }

        return arrayLevelDieth;
    }

    // Fisher-Yates shuffle
    public static int[] createRandomArray(int arraySize) {

        int[] arrayLevelRandDieth = createArray(arraySize);

        int j;
        int temp;
        Random rand = new Random();
        for (int i = arrayLevelRandDieth.length-1; i > 0; i--) {
            j = rand.nextInt(i);
            temp = arrayLevelRandDieth[i];
            arrayLevelRandDieth[i] = arrayLevelRandDieth[j];
            arrayLevelRandDieth[j] = temp;
        }

        return arrayLevelRandDieth;

    }


}
