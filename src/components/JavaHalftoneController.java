package components;

import halftone.Halftone;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


public class JavaHalftoneController {

    File fileName;

    @FXML
    private Button button1;

    @FXML
    private TextArea textField1;

    @FXML
    private ImageView imgView1;

    @FXML
    protected void handleChoseButtonClicked(ActionEvent event){
        // Update imageView with chosen image and show image name in textField1
        File tempFile=imgFile();
        textField1.setText(tempFile.getName());

        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                BufferedImage bimg = imRead(tempFile);
                setImageInView(imgView1, bimg);


                return null;

            }
        };

        new Thread(task).start();
        fileName = tempFile;
    }

    // Helper method to set image in image view
    private void setImageInView(ImageView imgView, BufferedImage bimg) {
        Image imgfx = SwingFXUtils.toFXImage(bimg,null);
        imgView.setImage(imgfx);
    }

    @FXML
    protected void handleProcessButtonClicked(ActionEvent event) {
        Halftone img = new Halftone();
        BufferedImage image= img.imRead(fileName);
        image = img.imProcess(image);
        setImageInView(imgView1,image);

    }

    // helper function to show only image files in file chooser and return selected Image File
    protected File imgFile() {
        FileChooser imFileChooser = new FileChooser();
        imFileChooser.setTitle("Choose image");
        imFileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files","*.png", "*.jpg","*.jpeg","*.gif","*.tif","*.tiff"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        File selectedImageFile=imFileChooser.showOpenDialog(new Stage());
        return selectedImageFile;

    }
    // Helper function to read image
    public BufferedImage imRead(File file) {
        try {
            return ImageIO.read(file);
        } catch (IOException e) {
            System.out.println("No such file");
        }

        return null;
    }
}
