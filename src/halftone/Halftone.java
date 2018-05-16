package halftone;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ImageFilter;
import java.io.File;
import java.io.IOException;

public class Halftone implements Image {

    @Override
    public BufferedImage imRead(File file) {
        try {
            return ImageIO.read(file);
        } catch (IOException e) {
            System.out.println("No such file");
        }

    return null;
    }

    @Override
    public void imWrite() {

    }

    @Override
    public BufferedImage imProcess(BufferedImage image) {

        BufferedImage grayImage = image;
        // Check if image is grayscale
        int type = image.getColorModel().getColorSpace().getType();
        boolean isGrayscale = (type == ColorSpace.TYPE_GRAY || type == ColorSpace.CS_GRAY);


        if (isGrayscale) {
            System.out.println("Image is Grayscale" );

        } else {
            System.out.println("Image is not Grayscale"+image.getColorModel().getColorSpace());
            grayImage = im2gray(image);
        }

        return grayImage;
    }

    @Override
    public void imSave() {

    }

    // Helper methods for converting image to grayscale
    private BufferedImage im2gray(BufferedImage image) {
        BufferedImage grayImage = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = grayImage.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return grayImage;

    }
}
