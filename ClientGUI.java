import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.TextArea;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;
import javafx.scene.control.ChoiceBox;
public class ClientGUI extends Application {
    //for interprocess communication
    BufferedReader in;
    PrintWriter out;
    String serverAddress;
    Socket socket;
    String defaultColor; 
    String[] colorArr; 
    //for GUI
    Stage window;
    Scene scene1;
    Scene scene2; 
    Label width;
    Label height; 
    Label dcolor; 
    Label allColors;
    BorderPane borderPane = new BorderPane();;
    TextArea bottomText = new TextArea();
    GridPane bottomView = new GridPane();


    //for error checking 
    int totalHeight; 
    int totalWidth; 
    public static void main(String[] args) throws Exception {
        launch(args);
    }

    public void alertBoxes(String errorCode){
        Stage alertWindow = new Stage();


        //Find appropriate message to display
        String windowTitle = "";
        String errorMessage = "";

        if (errorCode.equals("Error01")){
            windowTitle = "Error: 01";
            errorMessage = "Request On Empty Board";

        } else if (errorCode.equals("Error99")){
            windowTitle = "Error: 99";
            errorMessage = "Server has been unexpectedly disonnected";
            window.setScene(scene1);

        } else if (errorCode.equals("Error45")){
            windowTitle = "Error: 45";
            errorMessage = "String is too large";

        } else if (errorCode.equals("Error60")){
            windowTitle = "Error: 60";
            errorMessage = "The PIN you are looking for does not exist";

        } else if (errorCode.equals("Error61")){
            windowTitle = "Error: 61";
            errorMessage = "The PIN you have requested exceeds the board dimensions";

        } else if (errorCode.equals("Error51")){
            windowTitle = "Error: 51";
            errorMessage = "The value(s) of the post statement exceeds the board dimensions";

        } else if (errorCode.equals("Error52")){
            windowTitle = "Error: 52";
            errorMessage = "Invalid coordinate values, must be an integer";
            
        } else if (errorCode.equals("Error53")){
            windowTitle = "Error: 53";
            errorMessage = "Minimum width and height is 1";
        } 


        alertWindow.initModality(Modality.APPLICATION_MODAL);
        alertWindow.setTitle(windowTitle);
        alertWindow.setMinWidth(300);

        Label errorLabel = new Label();
        errorLabel.setText(errorMessage);

        Button closeButton = new Button ("Close");
        closeButton.setOnAction(e->{
            alertWindow.close();          
        });

        VBox errorLayout = new VBox(15);
        errorLayout.getChildren().addAll(errorLabel, closeButton);
        errorLayout.setAlignment(Pos.CENTER);

        Scene errorScene = new Scene(errorLayout);
        alertWindow.setScene(errorScene);
        alertWindow.showAndWait();


    }


    private void serverOut(){
        String totalString = "";  
        String readString = "";
        try {
            try {
                readString = in.readLine();
                
            }catch(SocketException error ){
                alertBoxes("Error99");
            }
            if (readString.equals("Error60")){
                alertBoxes("Error60");

            } else if (readString.equals("Error01")){
                alertBoxes("Error01");

            } else {
                if (readString != null ){
                    totalString =  totalString.concat(readString.concat("\n"));
                    int i = 0; 
                    while (true){
                        //Checks if buffer is empty before trying to read from it.
                        if (in.ready() ){
                            readString = in.readLine();
                            totalString =  totalString.concat(readString.concat("\n"));
                        } else if (i > 3000) {
                            break;
                        }
                        i++;
                    } 
                }
            }  
        }catch(IOException error ){
            //add error messages
        }
        bottomText.setText(totalString);
        bottomText.setMinWidth(600 );
        bottomText.setMinHeight(300);
        borderPane.setBottom(bottomText);

    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        
        
        window = primaryStage;
        window.setTitle("Client Application");
        ChoiceBox<String> colorChoices = new ChoiceBox<>();
        ChoiceBox<String> getColorChoices = new ChoiceBox<>();
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(8);
        grid.setHgap(10);

        Label ipLabel = new Label("IP address:");
        GridPane.setConstraints(ipLabel, 0, 0);

        TextField ipInput = new TextField();
        GridPane.setConstraints(ipInput, 1, 0);

        Label portLabel = new Label("PortNumber:");
        GridPane.setConstraints(portLabel, 0, 1);

        TextField portInput = new TextField();
        portInput.setPromptText("####");
        GridPane.setConstraints(portInput, 1, 1);

        Button connectButton = new Button("Connect ");
        connectButton.setOnAction(e  -> {
            try {
                serverAddress = ipInput.getText();
                socket = new Socket(serverAddress, Integer.parseInt(portInput.getText()));
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                
                String serverOutput = in.readLine();
                Scanner scnr = new Scanner(serverOutput);
                //skips Color grreting message 
                
                while(!scnr.next().equals( "|")){
                }
                defaultColor = scnr.next();
                
                dcolor.setText(defaultColor);//first color is the default color 
                String otherColors = ""; 
                String current; 
                while (scnr.hasNext()){//grabs rest of colors; 
                    current = scnr.next();
                    otherColors = otherColors.concat(current);
                    otherColors = otherColors.concat(" ");
                }
                colorArr = otherColors.split(" ");
                allColors.setText(otherColors);
                scnr.close();
                //finishes finding colors 
                
                //starts finding width and height
                serverOutput = in.readLine();
                scnr = new Scanner(serverOutput);
                while(!scnr.next().equals( "=")){
                }
                String swidth = scnr.next(); 
                totalWidth = Integer.parseInt(swidth);
                width.setText(swidth);

                while(!scnr.next().equals( "=")){
                }
                String sheight = scnr.next(); 
                totalHeight= Integer.parseInt(sheight);
                height.setText(sheight);
                scnr.close();

                GridPane.setConstraints(colorChoices,1, 2);
                if (colorChoices.getValue() == null){
                    colorChoices.getItems().add(defaultColor);
                    getColorChoices.getItems().add(defaultColor);
                    colorChoices.setValue(defaultColor);
                    for (String s : colorArr){
                        colorChoices.getItems().add(s);
                        getColorChoices.getItems().add(s);
                    }
                }
                window.setScene(scene2);
            }catch(Exception error){
                System.out.println(error);
            }

        });
        GridPane.setConstraints(connectButton, 1, 2);

        //Add everything to grid
        grid.getChildren().addAll(ipLabel, ipInput, portLabel, portInput, connectButton);

        scene1 = new Scene(grid, 300, 200);

        //making scene2 

        //top Grid 
        GridPane topGrid = new GridPane();
        topGrid.setPadding(new Insets(10, 10, 10, 10));
        topGrid.setVgap(10);
        topGrid.setHgap(15);

        Label widthLabel = new Label("Board Width:");
        width = new Label();
        GridPane.setConstraints(widthLabel, 0, 0);
        GridPane.setConstraints(width, 1, 0  );

        Label heightLabel = new Label("Board Height:");
        height= new Label();
        GridPane.setConstraints(heightLabel, 0, 1);
        GridPane.setConstraints(height, 1, 1);

        //creating color section
        Label dColorLabel = new Label("Default Color:");
        dcolor = new Label();
        GridPane.setConstraints(dColorLabel, 2, 0);
        GridPane.setConstraints(dcolor, 3, 0);

        Label colorsLabel = new Label("All available colors are:");
        allColors = new Label();
        GridPane.setConstraints(colorsLabel, 2, 1);
        GridPane.setConstraints(allColors, 3, 1);
        
        topGrid.getChildren().addAll(widthLabel,width , heightLabel, height , dColorLabel , dcolor, colorsLabel , allColors);

        //middleGrids
        //postGrid Grid
        GridPane postGrid = new GridPane();
        postGrid.setPadding(new Insets(10, 10, 10, 10));
        postGrid.setVgap(10);
        postGrid.setHgap(15);
        postGrid.setStyle("-fx-background-color:white");
        Label xPostL = new Label("X:");
        GridPane.setConstraints(xPostL, 0, 0); 
        Label yPostL = new Label("Y:");
        GridPane.setConstraints(yPostL, 2, 0);

        TextField xPost = new TextField();
        GridPane.setConstraints(xPost, 1, 0);
        TextField yPost= new TextField();
        GridPane.setConstraints(yPost, 3, 0);

        Label wPost = new Label("Width:");
        GridPane.setConstraints(wPost, 0, 1);
        Label hPost = new Label("Height:");
        GridPane.setConstraints(hPost, 2, 1);

        TextField widthPost = new TextField();
        GridPane.setConstraints(widthPost, 1, 1);
        TextField heightPost = new TextField();   
        GridPane.setConstraints(heightPost, 3, 1); 

        Label colorPost = new Label("Color:");
        GridPane.setConstraints(colorPost, 0, 2);

        Label contentPost = new Label("Content:");
        GridPane.setConstraints(contentPost, 0, 3);
        TextArea contentPostInput = new TextArea();
        GridPane.setConstraints(contentPostInput, 1, 3,4,4);
        contentPostInput.setPrefHeight(300);

        Button submitPost = new Button("Submit");
        GridPane.setConstraints(submitPost , 5, 7);
        submitPost.setOnAction(e->{
            //Check for errors
            try{
                //Check for note out of bounds
                if (Integer.parseInt(xPost.getText()) > totalWidth || Integer.parseInt(xPost.getText()) < 0 //Check x value
                 || Integer.parseInt(yPost.getText()) > totalHeight || Integer.parseInt(yPost.getText()) < 0 //Check y value
                 || Integer.parseInt(yPost.getText())+Integer.parseInt(widthPost.getText()) > totalWidth || Integer.parseInt(widthPost.getText()) < 0 //Check width
                 || Integer.parseInt(yPost.getText())+Integer.parseInt(heightPost.getText()) > totalHeight || Integer.parseInt(heightPost.getText()) < 0 //Check height
                ){
                    //if note is out of bounds
                    alertBoxes("Error51");

                }else if (Integer.parseInt(widthPost.getText()) < 1 || Integer.parseInt(heightPost.getText()) < 1){
                    alertBoxes("Error53");
                } else {
                    //server input
                    String postString = "POST ";
                    postString = postString.concat(xPost.getText());
                    postString = postString.concat(" ");
                    postString = postString.concat(yPost.getText());
                    postString = postString.concat(" ");
                    postString = postString.concat(widthPost.getText());
                    postString = postString.concat(" ");
                    postString = postString.concat(heightPost.getText());
                    postString = postString.concat(" ");
                    postString = postString.concat(colorChoices.getValue());
                    postString = postString.concat(" ");
                    try{
                        postString  = postString.concat(contentPostInput.getText());
                    } catch (OutOfMemoryError e2){
                        alertBoxes("Error45");
                    }
                    
                    out.println(postString);

                    //server output
                    serverOut();
                }
            //If coordinates entered are cannot be converted to integers
            } catch (NumberFormatException e1){
                alertBoxes("Error52");
            }
            
        });

        postGrid.getChildren().addAll(xPostL,xPost,yPostL,yPost,wPost ,widthPost,hPost,heightPost,colorPost,colorChoices,contentPost, contentPostInput, submitPost);

        //getGrid Grid
        GridPane getGrid = new GridPane();
        getGrid.setPadding(new Insets(10, 10, 10, 10));
        getGrid.setVgap(10);
        getGrid.setHgap(15);
        getGrid.setStyle("-fx-background-color:white");
  
 
        //labels 
        Label getOption = new Label ("GET:"); 
        GridPane.setConstraints(getOption, 0, 0);
        Label getColor = new Label("Color:");
        GridPane.setConstraints(getColor, 0, 1);
        Label getContains = new Label("Contains:");
        GridPane.setConstraints(getContains, 0, 2);
        Label getRefers = new Label("RefersTo:");
        GridPane.setConstraints(getRefers, 0, 3);
        
        //TextFields 
        TextField containsInput = new TextField();
        GridPane.setConstraints(containsInput, 1, 2);
        TextField refersInput = new TextField();
        GridPane.setConstraints(refersInput, 1, 3);
        //drop down menus 
        getColorChoices.getItems().add("ALL");
        getColorChoices.setValue("ALL");
        GridPane.setConstraints(getColorChoices, 1, 1);
        ChoiceBox<String> getChoices = new ChoiceBox<>();
        GridPane.setConstraints(getChoices, 1, 0);
        getChoices.getItems().addAll("PINS", "Notes");
        getChoices.setValue("Notes"); 

        //buttons      
        Button submitGet = new Button("Submit");
        GridPane.setConstraints(submitGet, 4, 3);
        submitGet.setOnAction(e -> {
            if (getChoices.getValue().equals("PINS")){
                out.println("GET PINS");
                serverOut();
            }else {
                if (containsInput.getText().equals("") && refersInput.getText().equals("")  && getColorChoices.getValue().equals("ALL") ){
                    out.println("GET");
                    serverOut();
                }else if(containsInput.getText().equals("") && refersInput.getText().equals("")  && !getColorChoices.getValue().equals("ALL")){//get color

                    String outputString = "GET color=".concat(getColorChoices.getValue());

                    out.println(outputString);

                    serverOut();

                }else if(!containsInput.getText().equals("") && refersInput.getText().equals("")  && getColorChoices.getValue().equals("ALL")){//get contains 

                    String outputString = "GET contains=".concat(containsInput.getText());

                    out.println(outputString);

                    serverOut();

                }else if(containsInput.getText().equals("") && !refersInput.getText().equals("")  && getColorChoices.getValue().equals("ALL")){//get refersTo

                    String outputString = "GET refersTo=".concat(refersInput.getText());

                    out.println(outputString);

                    serverOut();

                }else if(!containsInput.getText().equals("") && !refersInput.getText().equals("")  && getColorChoices.getValue().equals("ALL")){//get contains refersTo

                    String outputString = "GET contains=".concat(containsInput.getText()) +"refersTo=".concat(refersInput.getText());

                    out.println(outputString);

                    serverOut();

                }else if(containsInput.getText().equals("") && !refersInput.getText().equals("")  && !getColorChoices.getValue().equals("ALL")){//get color refersTo

                    String outputString = "GET color=".concat(getColorChoices.getValue()) + "refersTo=".concat(refersInput.getText());

                    out.println(outputString);

                    serverOut();

                }else if(!containsInput.getText().equals("") && refersInput.getText().equals("")  && !getColorChoices.getValue().equals("ALL")){//get color contains

                    String outputString = "GET color=".concat(getColorChoices.getValue()) + "contains=".concat(containsInput.getText());

                    out.println(outputString);

                    serverOut();

                }else if(!containsInput.getText().equals("") && !refersInput.getText().equals("")  && !getColorChoices.getValue().equals("ALL")){//get color contains

                    String outputString = "GET color=".concat(getColorChoices.getValue()) + "contains=".concat(containsInput.getText())+ " refersTo=".concat(refersInput.getText());

                    out.println(outputString);

                    serverOut();

                }
            }
        });

        getGrid.getChildren().addAll(getOption,getColor,getContains,getRefers,containsInput,refersInput,getColorChoices,getChoices, submitGet);

        //pinGrid Grid
        GridPane pinGrid = new GridPane();
        
        pinGrid.setPadding(new Insets(10, 10, 10, 10));
        pinGrid.setVgap(10);
        pinGrid.setHgap(15);
        pinGrid.setStyle("-fx-background-color:white");
        Label xPinL = new Label("X:");
        GridPane.setConstraints(xPinL, 0, 0); 
        Label yPinL = new Label("Y:");
        GridPane.setConstraints(yPinL, 2, 0);

        TextField xPin = new TextField();
        GridPane.setConstraints(xPin, 1, 0);
        TextField yPin = new TextField();
        GridPane.setConstraints(yPin, 3, 0);
        Button submitPin = new Button("Submit");
        submitPin.setOnAction(e-> {
            try{
                //Check for pin out of bounds
                if (Integer.parseInt(xPin.getText()) > totalWidth || Integer.parseInt(xPin.getText()) < 0 //Check x value
                 || Integer.parseInt(yPin.getText()) > totalHeight || Integer.parseInt(yPin.getText()) < 0 //Check y value
                ){
                    //if pin is out of bounds
                    alertBoxes("Error61");
                } else {
                    //server input
                    String output = "PIN ";
                    output = output.concat(xPin.getText().concat(",")); 
                    output = output.concat(yPin.getText());
                    out.println(output);

                    //server output
                    serverOut();
                }

            } catch (NumberFormatException e1){
                alertBoxes("Error52");
            }
            
        });
        GridPane.setConstraints(submitPin, 3, 1); 
        pinGrid.getChildren().addAll(xPinL,xPin,yPinL,yPin,submitPin);

        //unpinGrid Grid
        GridPane unpinGrid = new GridPane();
        unpinGrid.setPadding(new Insets(10, 10, 10, 10));
        unpinGrid.setVgap(10);
        unpinGrid.setHgap(15);
        unpinGrid.setStyle("-fx-background-color:white");

        Label xUnpinL = new Label("X:");
        GridPane.setConstraints(xUnpinL, 0, 0); 
        Label yUnpinL = new Label("Y:");
        GridPane.setConstraints(yUnpinL, 2, 0);

        TextField xUnpin = new TextField();
        GridPane.setConstraints(xUnpin, 1, 0);
        TextField yUnpin = new TextField();
        GridPane.setConstraints(yUnpin, 3, 0);
        Button submitUnpin = new Button("Submit");
        submitUnpin.setOnAction(e-> {
            //server input
            String output = "UNPIN ";
            output = output.concat(xUnpin.getText().concat(",")); 
            output = output.concat(yUnpin.getText());
            try{
                //Check for no coorinates
                if (Integer.parseInt(xUnpin.getText()) < 0 || Integer.parseInt(yUnpin.getText()) < 0){
                   
                
                } else {
                    out.println(output);
                    //server output
                    serverOut();
                }
            } catch (NumberFormatException e1){
                alertBoxes("Error52");
            }
            
            
        });
        GridPane.setConstraints(submitUnpin, 3, 1);
        unpinGrid.getChildren().addAll(xUnpinL,xUnpin,yUnpinL,yUnpin,submitUnpin);

        //clearGrid Grid
        GridPane clearGrid = new GridPane();
        clearGrid.setPadding(new Insets(10, 10, 10, 10));
        clearGrid.setVgap(10);
        clearGrid.setHgap(50);
        clearGrid.setStyle("-fx-background-color:white");

        Label confirmationClear = new Label("Would you like to clear all unpinned notes?");
        GridPane.setConstraints(confirmationClear, 0, 0);
        Button yesClear = new Button("Yes");
        GridPane.setConstraints(yesClear, 1, 1);
        yesClear.setOnAction(e -> {
            out.println("CLEAR");
            String totalString = "";  
            //server output 
            serverOut();
        });

        Button noClear = new Button("No");  
        GridPane.setConstraints(noClear, 2, 1);  
        clearGrid.getChildren().addAll(confirmationClear,yesClear,noClear);  
        
        
        //disconnect Grid
        GridPane discGrid = new GridPane();
        discGrid.setPadding(new Insets(10, 10, 10, 10));
        discGrid.setVgap(10);
        discGrid.setHgap(50);
        discGrid.setStyle("-fx-background-color:white");     

        Label confirmationDisc = new Label("Would you like to disconnect? "); 
        GridPane.setConstraints(confirmationDisc, 0, 0);
        Button yesDisc = new Button("Yes");
        GridPane.setConstraints(yesDisc, 1, 1);
        yesDisc.setOnAction(e -> {
            //server input
            out.println("DISCONNECT");
            window.setScene(scene1);
        });
        Button noDisc = new Button("No");
        GridPane.setConstraints(noDisc, 2, 1);
        discGrid.getChildren().addAll(confirmationDisc,yesDisc,noDisc);
        



        //Left Grid
        GridPane leftGrid = new GridPane();
        leftGrid.setPadding(new Insets(10, 10, 10, 10));
        leftGrid.setVgap(10);
        leftGrid.setHgap(10);

        Button postButton = new Button("POST");
        GridPane.setConstraints(postButton, 0, 0);
        postButton.setOnAction(e ->{
            borderPane.setCenter(postGrid);
        });     
        Button getButton = new Button("GET");
        GridPane.setConstraints(getButton, 0, 1);
        getButton.setOnAction(e ->{
            borderPane.setCenter(getGrid);
        });     
        Button pinButton = new Button("PIN");
        GridPane.setConstraints(pinButton, 0, 2);
        pinButton.setOnAction(e ->{
            borderPane.setCenter(pinGrid);
        });     
        Button unpinButton = new Button("UNPIN");
        GridPane.setConstraints(unpinButton, 0, 3);
        unpinButton.setOnAction(e ->{
            borderPane.setCenter(unpinGrid);
        });        

        Button clearButton = new Button("CLEAR");
        clearButton.setOnAction(e ->{
            borderPane.setCenter(clearGrid);
        });
        GridPane.setConstraints(clearButton, 0, 4);


        Button discButton = new Button("DISCONNECT");
        discButton.setOnAction(e ->{
            borderPane.setCenter(discGrid);
        });
        GridPane.setConstraints(discButton, 0, 5);

        leftGrid.getChildren().addAll(postButton,getButton,pinButton ,unpinButton,clearButton, discButton);

        //creating user pane

        borderPane.setTop(topGrid);
        borderPane.setLeft(leftGrid);
        borderPane.setCenter(postGrid);
        scene2 = new Scene(borderPane , 700, 600 );

        window.setScene(scene1);
        window.show();
    }


}