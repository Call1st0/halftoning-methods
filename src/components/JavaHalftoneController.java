package components;

import halftone.ArrayGenerator;
import halftone.Halftone;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class JavaHalftoneController {
    // member variables
    private File fileName;
    private static final int MIN_PIXELS = 10;
    private double width = 0.0;
    private double height = 0.0;

    @FXML
    private ChoiceBox<String> choiceBox1;

    @FXML
    private TextArea textField1;

    @FXML
    private ImageView imgView1;

    // Initialization method that can call needed procedures during loading of the
    // form
    @FXML
    protected void initialize() {
        setChoiceBox(choiceBox1);

        // Set listener for change in choicebox
        choiceBox1.getSelectionModel().selectedItemProperty().addListener((ChangeListener<String>) (observable, oldValue, newValue) -> textField1.setText(newValue));

        // Neaded to initialize Point2D object without values
        ObjectProperty<Point2D> mouseDown = new SimpleObjectProperty<>();

        imgView1.setOnMousePressed(event -> {
            Point2D mousePress = new Point2D(event.getX(), event.getY());
            mouseDown.set(mousePress);
        });

        // Set listener for mouse Dragged
        imgView1.setOnMouseDragged(event -> {
            Point2D mouseAbsPos = new Point2D(event.getX(), event.getY()); // Absolute position
            Point2D mouseRelPos = mouseAbsPos.subtract(mouseDown.get()); // Relative difference

            shiftImageViewPort(imgView1, mouseRelPos);
            mouseDown.set(new Point2D(event.getX(), event.getY()));
        });

        //Zooming with scroll - taken from https://gist.github.com/james-d/ce5ec1fd44ce6c64e81a
        imgView1.setOnScroll(e -> {
            double delta = e.getDeltaY();
            Rectangle2D viewport = imgView1.getViewport();

            double scale = clamp(Math.pow(1.01, delta),

                // don't scale so we're zoomed in to fewer than MIN_PIXELS in any direction:
                Math.min(MIN_PIXELS / viewport.getWidth(), MIN_PIXELS / viewport.getHeight()),

                // don't scale so that we're bigger than image dimensions:
                Math.max(width / viewport.getWidth(), height / viewport.getHeight())

            );

            Point2D mouse = imageViewToImage(imgView1, new Point2D(e.getX(), e.getY()));

            double newWidth = viewport.getWidth() * scale;
            double newHeight = viewport.getHeight() * scale;

            // To keep the visual point under the mouse from moving, we need
            // (x - newViewportMinX) / (x - currentViewportMinX) = scale
            // where x is the mouse X coordinate in the image

            // solving this for newViewportMinX gives

            // newViewportMinX = x - (x - currentViewportMinX) * scale 

            // we then clamp this value so the image never scrolls out
            // of the imageview:

            double newMinX = clamp(mouse.getX() - (mouse.getX() - viewport.getMinX()) * scale, 
                    0, width - newWidth);
            double newMinY = clamp(mouse.getY() - (mouse.getY() - viewport.getMinY()) * scale, 
                    0, height - newHeight);

            imgView1.setViewport(new Rectangle2D(newMinX, newMinY, newWidth, newHeight));
        });

    }

    // Helper method to populate choiceBox
    @FXML
    private void setChoiceBox(ChoiceBox<String> choiceBox) {
        ObservableList<String> choiceOptions = FXCollections.observableArrayList("Grayscale", "Threshold",
                "Ordered dither", "Error diffusion");
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
                width = bimg.getWidth();
                height = bimg.getHeight();
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
        imgView.setViewport(new Rectangle2D(0, 0, imgView.getImage().getWidth(), imgView.getImage().getHeight()));
    }

    private void shiftImageViewPort(ImageView imageView, Point2D delta) {
        // Check if image is set
        if (imageView.getImage() != null) {

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
    
    // convert mouse coordinates in the imageView to coordinates in the actual image:
    private Point2D imageViewToImage(ImageView imageView, Point2D imageViewCoordinates) {
        double xProportion = imageViewCoordinates.getX() / imageView.getBoundsInLocal().getWidth();
        double yProportion = imageViewCoordinates.getY() / imageView.getBoundsInLocal().getHeight();

        Rectangle2D viewport = imageView.getViewport();
        return new Point2D(
                viewport.getMinX() + xProportion * viewport.getWidth(), 
                viewport.getMinY() + yProportion * viewport.getHeight());
    }

    // helper function to show only image files in file chooser and return selected
    // Image File
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
