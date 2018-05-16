package halftone;

import java.awt.image.BufferedImage;
import java.io.File;

public interface Image {

    public BufferedImage imRead(File file);

    public void imWrite();

    public void imProcess(BufferedImage image);

    public void imSave();



}
