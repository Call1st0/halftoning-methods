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
     * Method for halftoning using threshold
     * @param imgOrig
     * @param thresholdLevel
     * @return threshold image
     */
    public BufferedImage threshold(BufferedImage imgOrig, int thresholdLevel) {

        // Check if image is grayscale
        int type = imgOrig.getColorModel().getColorSpace().getType();
        boolean isGrayscale = (type == ColorSpace.TYPE_GRAY || type == ColorSpace.CS_GRAY);

        if (isGrayscale) {
            System.out.println("Image is Grayscale" );

        } else {
            System.out.println("Image is not Grayscale"+imgOrig.getColorModel().getColorSpace());
            imgOrig = im2gray(imgOrig);
        }

        // initialize Raster of the original image and WritableRaster of the threshold Image
        BufferedImage imgThreshold= new BufferedImage(imgOrig.getWidth(),imgOrig.getHeight(),BufferedImage.TYPE_BYTE_BINARY);
        Raster imgOrigRaster = imgOrig.getData();
        WritableRaster imgThresholdRaster = imgOrigRaster.createCompatibleWritableRaster();

        for (int i = 0; i < imgOrig.getHeight(); i++) {
            for (int j = 0; j <imgOrig.getWidth() ; j++) {
                int[] tempArray = new int[1]; //TODO array with only one element, since the image is grayscale, ugly consider refactoring

                imgOrigRaster.getPixel(j, i, tempArray);

                if (tempArray[0] > thresholdLevel) {
                    tempArray[0] = 255;
                    imgThresholdRaster.setPixel(j, i, tempArray);
                } else {
                    tempArray[0] = 0;
                    imgThresholdRaster.setPixel(j, i, tempArray);
                }
            }
        }
        imgThreshold.setData(imgThresholdRaster);
        return imgThreshold;
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
        int taWidth = (int)Math.sqrt(thresholdArray.length);
        int taHeight = taWidth;

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

        //TODO solve problem with the halftoning near the lover and right edges. Define conditions to go to the edg
        for (int i = 0; i <imgHeight/taHeight ; i++) {
            for (int j = 0; j <imgWidth / taWidth; j++) {

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

    /*
    Pseudo code for error diffusion

    Create writable raster from original image



    for each y from top to bottom
   for each x from left to right
      oldpixel  := pixel[x][y]
      newpixel  := find_closest_palette_color(oldpixel)
      pixel[x][y]  := newpixel
      quant_error  := oldpixel - newpixel
      pixel[x + 1][y    ] := pixel[x + 1][y    ] + quant_error * 7 / 16
      pixel[x - 1][y + 1] := pixel[x - 1][y + 1] + quant_error * 3 / 16
      pixel[x    ][y + 1] := pixel[x    ][y + 1] + quant_error * 5 / 16
      pixel[x + 1][y + 1] := pixel[x + 1][y + 1] + quant_error * 1 / 16

     */

    public BufferedImage errorDiff(BufferedImage imgOrig) {

        // size of the original image
        int imgWidth = imgOrig.getWidth();
        int imgHeight = imgOrig.getHeight();

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
        WritableRaster imgOrigWritableRaster = imgOrig.getRaster();
        WritableRaster imgDietheredRaster = imgOrigWritableRaster.createCompatibleWritableRaster();


        int whitePix = 255;
        int blackPix = 0;
        int thresholdPix = 127;
        int newPix;
        int bandSample = 0;




        for (int row = 0; row <imgHeight ; row++) {
            for (int col = 0; col < imgWidth; col++) {

                // Get the original pixel value

                int origPix=imgOrigWritableRaster.getSample(col, row, bandSample);//Sample band is 0 since the image must be grayscale

                if (origPix >= thresholdPix) {
                    newPix = whitePix;
                    imgDietheredRaster.setSample(col, row, bandSample, newPix); //White pixel
                } else {
                    newPix = blackPix;
                    imgDietheredRaster.setSample(col, row, bandSample, newPix); //Black pixel
                }
                double quantError = origPix - newPix;

                if (col < imgWidth-1) {
                    imgOrigWritableRaster.setSample(col + 1, row, bandSample,
                            imgOrigWritableRaster.getSample(col + 1, row, bandSample) + (int)(quantError*7.0/16.0 ));


                }

                if (col!=0 && row < imgHeight-1) {
                    imgOrigWritableRaster.setSample(col - 1, row + 1, bandSample,
                            imgOrigWritableRaster.getSample(col - 1, row + 1, bandSample) + (int)(quantError*3.0/16.0 ));
                }

                if (row < imgHeight-1) {
                    imgOrigWritableRaster.setSample(col, row + 1, bandSample,
                            imgOrigWritableRaster.getSample(col, row + 1, bandSample) + (int)(quantError*5.0/16.0 ));
                }

                if (col < imgWidth-1 && row < imgHeight-1) {
                    imgOrigWritableRaster.setSample(col+1, row + 1, bandSample,
                            imgOrigWritableRaster.getSample(col+1, row + 1, bandSample) + (int)(quantError*1.0/16.0 ));
                }

            }

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


