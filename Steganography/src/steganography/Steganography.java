/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package steganography;

import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import lib.Utils;

/**
 *
 * @author Alex
 */
public class Steganography extends Application {
    
    @Override
    public void start(Stage primaryStage) { 
        initialize();
        createWindow(primaryStage);
    }

    //top vbox
    private ImageView imageV = new ImageView();
    private Button choseImage = new Button("Choose Image");
    private TextField keyField = new PasswordField(); 
    
    private Label action = new Label("Choose action");
    private ToggleButton encode = new ToggleButton("Embed"), decode = new ToggleButton("Extract"), details = new ToggleButton("Details");
    private StackPane middle = new StackPane(), bottom = new StackPane(), encodeStack = new StackPane();
    private ToggleGroup toggleGroup = new ToggleGroup();
    
    private VBox container = new VBox();
    private Button chooseFile = new Button("Choose File");
    private ComboBox<String> algorithm = new ComboBox<>();
    
    //bottom vbox
    private Label capsLock = new Label("Caps lock is on");
    private Button go = new Button("Go"), reset = new Button("Reset");
    private HBox bottomContainer0 = new HBox(details, reset);
    private VBox bottomContainer = new VBox(capsLock, bottomContainer0);
    
    private VBox upperContainer = new VBox(imageV, choseImage, keyField, action, decode, encode, encodeStack); 
    private StackPane detailsPane = new StackPane(); 
    
    private boolean capsIsOn;
    public static boolean embedded = false;
    
    BufferedImage original = null;
    String extension;
    File imageFile = null, msgFile = null;
  
    private void initialize(){

        //check caps lock state
        capsLock.setVisible(Toolkit.getDefaultToolkit().getLockingKeyState(20));
        keyField.setOnMouseClicked(event -> capsLock.setVisible(Toolkit.getDefaultToolkit().getLockingKeyState(20)));
        
        middle.getChildren().addAll(upperContainer);
        bottom.getChildren().add(bottomContainer);
                
        details.setDisable(true);
        encode.setDisable(true);
        decode.setDisable(true);
        go.setDisable(true);
        keyField.setPromptText("Password");
        keyField.setTooltip(new Tooltip("The password can have minimum 4 letter"));
         
        encode.setToggleGroup(toggleGroup);
        decode.setToggleGroup(toggleGroup);  
    }
    
    private void createWindow(Stage primaryStage){
        
        //allow user to chooe only bmp, png or jpg images
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Images", "*.bmp","*.png", "*.jpeg", "*.jpg");
        fileChooser.getExtensionFilters().add(extFilter);
        
        //window details  
        BorderPane root = new BorderPane();
        root.setCenter(middle);
        root.setBottom(bottom);
        displayWindow(primaryStage);
        Scene scene = new Scene(root, 680, 680);   
        primaryStage.setMinWidth(400);
        primaryStage.setTitle("Steganography");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        actionsUpper(fileChooser, root); 
        actionsBottom(root);
    }
      
    private void actionsUpper(FileChooser fileChooser, BorderPane root){
        
        choseImage.setOnAction((event) -> {      
            
            if(imageFile != null){
                reset.fire();
            }
            
            imageFile = fileChooser.showOpenDialog(null);
            if(imageFile != null){
                Image image = new Image(imageFile.toURI().toString());
                imageV.setImage(image);
                
            try {//read image
                original =  ImageIO.read(imageFile);
                
                //if bit depth < 24
                if(checkImage(original))
                    errorWindow("This image is not accepted. Bit depth is either less than 24 or bigger");
                
                //if image has transparency
                if(Utils.containsTransparency(original))
                    errorWindow("This image contains transparency"); 
                
                //if the image is too small
                if(original.getWidth() < 200 && original.getHeight() < 200)
                    errorWindow("This image is too small");
                
                //if are given the image and the key -> deselect the encode, decode buttons
                enableButtons();
                
                //add container specific to the image
                if(isRaster(imageFile)){
                    algorithm.setPromptText("Choose an algorithm");
                    container = new VBox(chooseFile,algorithm, go);
                }else{
                    container = new VBox(chooseFile, go);
                }
                
                if(root.getRight() != null){
                    root.setRight(null);
                    detailsPane.getChildren().clear();
                    details.setSelected(false);
                }
                
                encodeStack.getChildren().clear();
                encodeStack.getChildren().add(container);
                container.setVisible(false);
                
            } catch (IOException ex) {
                //Logger.getLogger(Steganography.class.getName()).log(Level.SEVERE, null, ex);
                errorWindow("Cannot read image file");
            }
            } 
           
        });
        
        keyField.setOnKeyReleased(event -> {  
            if(event.getCode() == KeyCode.CAPS){
                capsIsOn = !capsIsOn;
                capsLock.setVisible(capsIsOn);
            }
        });

       
        keyField.textProperty().addListener((observable) -> {
            enableButtons();
        });
       
        encode.setOnAction((event) -> {  
            if(encode.isSelected()){               
                detailsEncode();
            }else{
                container.setVisible(false);
            }
        });
        
        decode.setOnAction((event) -> {
            StegoImage stego = new StegoImage(original, keyField.getText());
            
            if(container.isVisible())
                container.setVisible(false);
            
            if(isRaster(imageFile)){
                stego.decode();
            }else{
                stego.decodeJpeg(imageFile);
            }
            
            keyField.clear();
            decode.setSelected(false);
                
        });
        
    }
    
    private void actionsBottom(BorderPane root){
        reset.setOnAction((event) -> {
            imageFile = null;
            original = null;
            imageV.setImage(null);
            msgFile = null;
            
            keyField.clear();
            keyField.setPromptText("Password");
            
            details.setDisable(true);
            encode.setDisable(true);
            decode.setDisable(true);       
            encode.setSelected(false);
            decode.setSelected(false);
            toggleGroup.selectToggle(null);
            
            go.setDisable(true);
            details.setSelected(false);
            encodeStack.getChildren().clear();
            embedded = false;
            root.setRight(null);
        });
        
        details.setOnAction((event) -> {
            if(details.isSelected()){
                detailsPane();
                root.setRight(detailsPane);
            }else{
                root.setRight(null);
            }
        });
    }

    private void displayWindow(Stage primaryStage){
        
        //imageview
        imageV.setFitHeight(300);
        imageV.setFitWidth(300);
        imageV.setPreserveRatio(true);
        keyField.setMinWidth(200);
        
        //vbox  
        upperContainer.setAlignment(Pos.TOP_CENTER);
        upperContainer.setPadding(new Insets(20, 20, 20, 20));
        upperContainer.setSpacing(10);
        upperContainer.setFillWidth(false);
        
        bottomContainer.setAlignment(Pos.BOTTOM_CENTER);
        bottomContainer.setPadding(new Insets(0, 20, 10, 20));
        bottomContainer.setSpacing(5);
        bottomContainer.setFillWidth(false); 
        
        //buttons
        choseImage.setMinWidth(200);
        encode.setMinWidth(200);
        decode.setMinWidth(200);
       
        //encode needs
        algorithm.getItems().addAll("Matrix", "Random");
        chooseFile.setMinWidth(200);
        algorithm.setMinWidth(200);
        go.setMinWidth(200);
        
         //vbox encode
        bottomContainer0.setPadding(new Insets(0, 10, 5, 10));    //hbox(res, det)
        bottomContainer0.setSpacing(10);
  
    }
    
    private void detailsEncode(){

        //filter user choice's
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Text file", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);

        chooseFile.setOnAction((event) -> {
            msgFile = fileChooser.showOpenDialog(null);
            if(msgFile != null){
                
                if(msgFile.length() != 0){
                    details.setDisable(false);
                    go.setDisable(false);
                }        
                else{
                    errorWindow("The text file is empty.");
                }
            }    
        });    
        
        go.setOnAction((event) -> {
            
            if(imageFile == null){
                errorWindow("Could not find the image on disk.");
                reset.fire();
            }
            
            if(msgFile == null){
                errorWindow("You haven't selected a text file.");
            }
            
            extension = Utils.getExtension(imageFile);
            String key = keyField.getText();
            StegoImage stego;
            String alg = algorithm.getValue();
            
            if(alg == null && isRaster(imageFile)){
                errorWindow("You haven't selected an algorithm.");
            }else{            
            if (isRaster(imageFile)) {
                stego = new StegoImage(original, key, msgFile, extension);
                stego.encode(alg);
            }else{
                stego = new StegoImage(original, key, msgFile);
                stego.encode();
                
                if(embedded)
                    infWindow("Message embedded.");
            }
                if(embedded)
                    infWindow("Message embedded.");
            }
        });
        
        container.setVisible(true);                    
        container.setPadding(new Insets(10, 20, 0, 20));
        container.setSpacing(10);
        container.setFillWidth(false); 
    }
    
    private boolean validatePass(String text){
        
        if(!text.isEmpty()){
            return (text.length() >= 4);
        }
        return false;
    }
    
    private boolean canContinue(){
        return (validatePass(keyField.getText()) && original != null);
    }
    
    private boolean enableButtons(){
        
        if(canContinue()){
            encode.setDisable(false);
            decode.setDisable(false);
            return true;
        }else{
            encode.setDisable(true);
            decode.setDisable(true);
            return false;
        }
    }
    
    private void detailsPane(){
        
        GridPane imageDet = new GridPane();
        double sizeImg = imageFile.length();
        double sizeMsg = msgFile.length();
        
        //image details
        Label path = new Label("Path of image: "), dims = new Label("Dimensions: "), bit = new Label("Bit Depth: "), size = new Label("Size: ");
        Label set1 = new Label(imageFile.getPath()), set2 = new Label(original.getWidth() + " x " + original.getHeight()), set3 = new Label((String.valueOf(original.getColorModel().getPixelSize()))), set4 = new Label(String.format("%.2f", sizeImg/1024) + " KB");
        
        imageDet.addRow(0,path, set1);   
        imageDet.addRow(1,dims, set2);
        imageDet.addRow(2,bit, set3);
        imageDet.addRow(3,size, set4);

        //textfile details
        Label pathTxt = new Label("Path of file: "), sizeTxt = new Label("Size: ");
        Label set5 = new Label(msgFile.getPath()), set6 = new Label(String.format("%.2f", sizeMsg/1024) + " KB");
        
        GridPane fileDet = new GridPane();
        fileDet.addRow(15, pathTxt, set5);   
        fileDet.addRow(16, sizeTxt, set6);
        
        //display maximum size each alg can embed
//        GridPane sizeAlgDet = new GridPane();
//        Label size1 = new Label("Max capacity of Matrix algorithm: "), size2 = new Label("");
//        Label rez1 = new Label("Max capacity of Random algorithm: "), rez2 = new Label("");
//        
//        sizeAlgDet.addRow(20, size1, rez1);   
//        sizeAlgDet.addRow(21, size2, rez2);

        //others
        imageDet.setPadding(new Insets(20,10,10,10));
        imageDet.setVgap(10);   imageDet.setHgap(10);
        
        fileDet.setPadding(new Insets(10,10,10,10));
        fileDet.setVgap(10);   fileDet.setHgap(10);
        
//        sizeAlgDet.setPadding(new Insets(10,10,10,10));
//        sizeAlgDet.setVgap(10);   sizeAlgDet.setHgap(10);
        
        imageDet.setAlignment(Pos.TOP_LEFT);
        fileDet.setAlignment(Pos.TOP_LEFT);
//        sizeAlgDet.setAlignment(Pos.TOP_LEFT);
        detailsPane.setPrefSize(600, 600);
        detailsPane.getChildren().addAll(imageDet, fileDet);
    }

    private boolean isRaster(File imageFile){
        return ("bmp".equals(Utils.getExtension(imageFile)) || "png".equals(Utils.getExtension(imageFile)));
    }
    
    private boolean checkImage(BufferedImage image){
        return (image.getColorModel().getPixelSize() != 24);
    }
    
    private void errorWindow(String exception){

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(exception);
        alert.setHeaderText(null);
        alert.showAndWait();
        
        if(exception.contains("image"))
           imageV.setImage(null);
    }
    
    private void infWindow(String exception){

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setContentText(exception);
        alert.setHeaderText(null);
        alert.showAndWait();
        
        reset.fire();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
