/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bowerbird;

import javafx.application.Application;
import javafx.beans.Observable;
import javafx.beans.InvalidationListener;
import javafx.collections.MapChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class Bowerbird extends Application {
    
    private String artist, title, album, year, genre;
    @Override
    public void start(Stage primaryStage) {
        
        Media media = new Media("file:///users/josh5/OneDrive/Documents/GitHub/Bowerbird/Bowerbird/resources/test.mp3");
        media.getMetadata().addListener(new MapChangeListener<String, Object>() {
            @Override
            public void onChanged(MapChangeListener.Change<? extends String, ? extends Object> change) {
                if(change.wasAdded())
                {
                    String key = change.getKey();
                    Object value = change.getValueAdded();
                    
                    switch(key)
                    {
                        case "title": title = value.toString();
                        break;
                        case "artist": artist = value.toString();
                        break;
                        case "album": album = value.toString();
                        break;
                        case "year": year = value.toString();
                        break;
                        case "genre": genre = value.toString();
                        break;
                    }
                }
            }
        
        });
        MediaPlayer mp = new MediaPlayer(media);

        Label outputLabel = new Label();

        Slider volumeSlider = new Slider();
        volumeSlider.setValue(mp.getVolume()*100);
        volumeSlider.valueProperty().addListener(new InvalidationListener() {

            @Override
            public void invalidated(Observable observable) {
                mp.setVolume(volumeSlider.getValue() / 100);

            }
        });

        
        Button playBtn = new Button();
        playBtn.setText("Play");
        playBtn.setAlignment(Pos.CENTER);
        playBtn.setOnAction(new EventHandler<ActionEvent>() {
            
            @Override
            public void handle(ActionEvent event) {
                mp.play();
                System.out.println("Now Playing\n" + "Title: " + title + "\n"
                        + "Artist: " + artist + "\n" + "Album: " + album + "\n"
                        + "Year: " + year + "\n" + "Title: " + genre);

                outputLabel.setText("Now Playing\n" + "Title: " + title + "\n"
                        + "Artist: " + artist + "\n" + "Album: " + album + "\n"
                        + "Year: " + year + "\n" + "Title: " + genre);
            }
        });
        
        Button pauseBtn = new Button();
        pauseBtn.setText("Pause");
        pauseBtn.setAlignment(Pos.CENTER);
        pauseBtn.setOnAction(new EventHandler<ActionEvent>() {
           @Override
           public void handle(ActionEvent event)
           {
               mp.pause();
               System.out.println("Paused.");
           }
        });
        
        Button stopBtn = new Button();
        stopBtn.setText("Stop");
        stopBtn.setAlignment(Pos.CENTER);
        stopBtn.setOnAction(new EventHandler<ActionEvent>() {
           @Override
           public void handle(ActionEvent event)
           {
               mp.stop();
               System.out.println("Stopped.");
           }
        });
        
        GridPane grid = new GridPane();
        grid.setMinSize(400, 400);
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(5);
        grid.setHgap(5);
        grid.setAlignment(Pos.CENTER);
        grid.add(playBtn, 0, 2);
        grid.add(pauseBtn, 1, 2);
        grid.add(stopBtn, 3, 2);
        grid.add(outputLabel,0,0);
        grid.add(volumeSlider,4,2);
        Scene scene = new Scene(grid, 500, 500);
        
        primaryStage.setTitle("Bowerbird");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
