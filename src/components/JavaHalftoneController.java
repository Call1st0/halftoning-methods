package components;

import halftone.ArrayGenerator;
import halftone.Halftone;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


public class JavaHalftoneController {
    //member variables
    private File fileName;

    @FXML
    private ChoiceBox<String> choiceBox1;

    @FXML
    private TextArea textField1;

    @FXML
    private ImageView imgView1;

    // Initialization method that can call needed procedures during loading of the form
    @FXML
    protected void initialize() {
        setChoiceBox(choiceBox1);

        //Set listener for change in choicebox
        choiceBox1.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                textField1.setText(newValue);
            }
        });

        // Neaded to initialize Point2D object without values
        ObjectProperty<Point2D> mouseDown = new SimpleObjectProperty<>();

        imgView1.setOnMousePressed(event -> {
            Point2D mousePress = new Point2D(event.getX(), event.getY());
            mouseDown.set(mousePress);
        });

        //Set listener for mouse Dragged
        imgView1.setOnMouseDragged(event -> {
            Point2D mouseAbsPos = new Point2D(event.getX(), event.getY()); //Absolute position
            Point2D mouseRelPos = mouseAbsPos.subtract(mouseDown.get()); //Relative difference

            shiftImageViewPort(imgView1,mouseRelPos);
            mouseDown.set(new Point2D(event.getX(), event.getY()));
        });

        imgView1.setOnScroll(event -> {
            double scrollValue = event.getDeltaY();
            System.out.println(scrollValue);

            if (imgView1.getImage() != null) {

                Rectangle2D viewport = imgView1.getViewport();
                Rectangle2D newViewport = new Rectangle2D(0,0, viewport.getHeight() - scrollValue, viewport.getHeight() - scrollValue);
                imgView1.setViewport(newViewport);


            }


        });



//        imgView1.setOnScroll(event -> {
//            double scrollDelta = event.getDeltaY();
//            Rectangle2D viewport = imgView1.getViewport();
//
//            double scale = MathscrollDelta;
//            System.out.println(scrollDelta);
//
//
//
//           // imgView1.setViewport(new Rectangle2D(0,0,newWidth,newHeight));
//        });
    }

    // Helper method to populate choiceBox
    @FXML
    private void setChoiceBox(ChoiceBox choiceBox) {
        ObservableList<String> choiceOptions = FXCollections.observableArrayList(
                "Grayscale", "Threshold","Ordered dither", "Error diffusion");
        choiceBox.setItems(choiceOptions);
        choiceBox.setValue("Grayscale");

    }

    @FXML
    protected void handleChoseButtonClicked(ActionEvent event) {
        // Update imageView with chosen image and show image name in textField1
        File tempFile = imgFile();
        textField1.setText(tempFile.getName());

        Task task = new Task() {
            @Override
            protected Object call() {
                BufferedImage bimg = imRead(tempFile);
                setImageInView(imgView1, bimg);
                return null;
            }
        };

        new Thread(task).start();
        fileName = tempFile;
    }

    @FXML
    protected void handleProcessButtonClicked(ActionEvent event) {
        if (fileName == null) {
            textField1.setText("Image not chosen! Please choose an image.");
        } else {
            Halftone img = new Halftone();
            BufferedImage image = img.imRead(fileName);
            switch (choiceBox1.getValue()) {
                case "Grayscale":
                    image = img.im2gray(image);
                    break;
                case "Threshold":
                    image = img.threshold(image, 50);
                    break;
                case "Ordered dither":
                    image = img.orderedDither(image, ArrayGenerator.createArray(100));
                    break;
                case "Error diffusion":
                    image = img.errorDiff(image);
                    break;
            }

            setImageInView(imgView1, image);
        }
    }


    // Helper method to set image in image view
    private void setImageInView(ImageView imgView, BufferedImage bimg) {
        Image imgfx = SwingFXUtils.toFXImage(bimg, null);
        imgView.setImage(imgfx);
        imgView.setViewport(new Rectangle2D(0,0,imgView.getImage().getWidth(), imgView.getImage().getHeight()));
    }

    private void shiftImageViewPort(ImageView imageView, Point2D delta) {
        // Check if image is set
        if (imageView.getImage()!=null) {

            Rectangle2D viewport = imageView.getViewport();

            double width = imageView.getImage().getWidth();
            double height = imageView.getImage().getHeight();

            double maxX = width - viewport.getWidth();
            double maxY = height - viewport.getHeight();

            double minX = clamp(viewport.getMinX() - delta.getX(), 0, maxX);
            double minY = clamp(viewport.getMinY() - delta.getY(), 0, maxY);

            imageView.setViewport(new Rectangle2D(minX, minY, viewport.getWidth(), viewport.getHeight()));
        }


    }

    // helper function to show only image files in file chooser and return selected Image File
    private File imgFile() {
        FileChooser imFileChooser = new FileChooser();
        imFileChooser.setTitle("Choose image");
        imFileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.tif", "*.tiff"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        return imFileChooser.showOpenDialog(new Stage());

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

    // Helper function for clamping a value between min and max
    private double clamp(double value, double min, double max) {

        if (value < min)
            return min;
        if (value > max)
            return max;
        return value;
    }




}
