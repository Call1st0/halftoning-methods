package halftone;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

public class Halftone {

    public BufferedImage imRead(File file) {
        try {
            return ImageIO.read(file);
        } catch (IOException e) {
            System.out.println("No such file");
        }

    return null;
    }

    public void imWrite() {

    }


    public void imSave() {

    }

    // Helper methods for converting image to grayscale
    public BufferedImage im2gray(BufferedImage image) {
        BufferedImage grayImage = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY);

        Graphics g = grayImage.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return grayImage;

    }

    /**
     * Method for halftoning using ordered dither
     * @param imgOrig Original image
     * @param thresholdArray Threshold array as integer array
     * @return dithered image
     */
        public BufferedImage orderedDither(BufferedImage imgOrig, int[] thresholdArray) {

            // size of the original image
            int imgWidth = imgOrig.getWidth();
            int imgHeight = imgOrig.getHeight();

            //TODO fix magic size of the thresholdArray maybe use sqrt?
            int taWidth = 4;
            int taHeight = 4;

            // Check if image is grayscale
            int type = imgOrig.getColorModel().getColorSpace().getType();
            boolean isGrayscale = (type == ColorSpace.TYPE_GRAY || type == ColorSpace.CS_GRAY);

            if (isGrayscale) {
                System.out.println("Image is Grayscale" );

            } else {
                System.out.println("Image is not Grayscale"+imgOrig.getColorModel().getColorSpace());
                imgOrig = im2gray(imgOrig);
            }


            // initialize Raster of the original image and WritableRaster of the dietheredImage
            BufferedImage imgDiethered = new BufferedImage(imgWidth,imgHeight,BufferedImage.TYPE_BYTE_BINARY);
            Raster imgOrigRaster = imgOrig.getData();
            WritableRaster imgDietheredRaster = imgOrigRaster.createCompatibleWritableRaster();

            // initialize a point and rectangle for dithering
            Point point = new Point();
            Dimension dim = new Dimension(taWidth, taHeight);
            Rectangle roiRect = new Rectangle(point, dim);

            // initialize arrays for getting pixel data
            int[] tempArray = new int[taWidth*taHeight];
            int[] imgOrigArray = new int[taWidth*taHeight];


            for (int i = 0; i <imgHeight/taHeight ; i++) {
                for (int j = 0; j < imgWidth / taWidth; j++) {

                    roiRect.setLocation(point); //move the rectangle to desired position
                    imgOrigRaster.getPixels((int)roiRect.getX(), (int)roiRect.getY(), (int)roiRect.getWidth(), (int)roiRect.getHeight(), imgOrigArray);
                    tempArray = compareArrays(imgOrigArray, thresholdArray);
                    imgDietheredRaster.setPixels((int)roiRect.getX(), (int)roiRect.getY(), (int)roiRect.getWidth(), (int)roiRect.getHeight(), tempArray);

                    point.translate(taWidth,0); //translate point on next part of the image
                }
                point.setLocation(0,i*taHeight);//return point to first column and next row
            }
            imgDiethered.setData(imgDietheredRaster);

            return imgDiethered;
        }

/** Helper method needed for dithering
 * @param imgArray int[] integer array of an image
 * @param thresholdArray int[] integer array of a threshold
 * @return integer array of the comparison between input arrays
 */
    private int[] compareArrays(int[] imgArray, int[] thresholdArray) {

        int[] outputArray = new int[thresholdArray.length]; // Arrays has to be the same length

        for (int i = 0; i < thresholdArray.length; i++) {
            if (imgArray[i] >= thresholdArray[i]) {
                outputArray[i] = 255;
            } else outputArray[i] = 0;
        }


        return outputArray;

    }


}


