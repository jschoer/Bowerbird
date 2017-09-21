/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bowerbird;

import javafx.application.Application;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import javafx.util.Duration;

public class Bowerbird extends Application {

    public static final Double MIN_DURATION_CHANGE = 0.5;
    
    private String artist = "unknown", title = "-", album = "-", year = "-", genre = "N/A";
    private Duration duration;
    private MediaPlayer mediaPlayer;
    private Slider timeSlider;

    @Override
    public void start(Stage primaryStage) {

        BorderPane layout = new BorderPane();
        HBox hbox = topMenu();
        TabPane tabPane = leftSideMenu();
        layout.setTop(hbox);
        layout.setLeft(tabPane);
        layout.setMinSize(400, 400);
        layout.setPadding(new Insets(10, 10, 10, 10));
        Scene scene = new Scene(layout, 600, 600);

        primaryStage.setTitle("Bowerbird");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public TabPane leftSideMenu() {
        TabPane leftSideMenu = new TabPane();
        leftSideMenu.setPrefWidth(165);

        Tab songTab = new Tab();
        songTab.setText("Song");
        songTab.setContent(new Label("\nSong info\n will go here..."));

        Tab albumTab = new Tab();
        albumTab.setText("Albums");
        albumTab.setContent(new Label("\nList of Album\n will go here..."));

        Tab sortTab = new Tab();
        sortTab.setText("Sort");
        sortTab.setContent(new Label("\nSorting options\n  will go here..."));

        leftSideMenu.getTabs().addAll(songTab, albumTab, sortTab);

        return leftSideMenu;
    }

    public HBox topMenu() {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 10, 12));
        hbox.setSpacing(10);
        hbox.setStyle("-fx-background-color: #D3D3D3;");

        //file:///Users/christinaach/Documents/SandBox/Bowerbird/Bowerbird/resources/test.mp3
        //Media media = new Media("file:///Users/cryst/Documents/GitHub/Bowerbird/Bowerbird/resources/test.mp3");

        Media media = new Media("file:///Users/Josh5/OneDrive/Documents/GitHub/Bowerbird/Bowerbird/resources/test2.mp3");

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

        mediaPlayer = new MediaPlayer(media);

        mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                if(!timeSlider.isValueChanging()) {
                    timeSlider.setValue(newValue.toSeconds());
                }
            }
        });

        Label outputLabel = new Label();

        Button playBtn = new Button();
        playBtn.setText("Play");
        playBtn.setAlignment(Pos.CENTER);
        playBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                mediaPlayer.play();
                outputLabel.setText("Now Playing\n" + "Title: " + title + "\n"
                        + "Artist: " + artist + "\n" + "Album: " + album + "\n"
                        + "Year: " + year + "\n" + "Title: " + genre);
                timeSlider.setDisable(false);
                timeSlider.setValue(0);
            }
        });

        Button pauseBtn = new Button();
        pauseBtn.setText("Pause");
        pauseBtn.setAlignment(Pos.CENTER);
        pauseBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event)
            {
                mediaPlayer.pause();
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
                mediaPlayer.stop();
                timeSlider.setDisable(true);
                System.out.println("Stopped.");
            }
        });

        Slider volumeSlider = new Slider();
        volumeSlider.setValue(mediaPlayer.getVolume()*100);
        volumeSlider.valueProperty().addListener(new InvalidationListener() {

            @Override
            public void invalidated(Observable observable) {
                mediaPlayer.setVolume(volumeSlider.getValue() / 100);
            }
        });

        timeSlider = new Slider();
        timeSlider.setDisable(true);

        mediaPlayer.totalDurationProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                timeSlider.setMax(newValue.toSeconds());
            }
        });

        timeSlider.valueChangingProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(!newValue) {
                    mediaPlayer.seek(Duration.seconds(timeSlider.getValue()));
                }
            }
        });

        timeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                double currentTime = mediaPlayer.getCurrentTime().toSeconds();
                double sliderTime = newValue.doubleValue();
                if (Math.abs(currentTime - sliderTime) > MIN_DURATION_CHANGE) {
                    mediaPlayer.seek(Duration.seconds(sliderTime));
                }
            }
        });

        hbox.getChildren().addAll(playBtn, pauseBtn, stopBtn, outputLabel, volumeSlider, timeSlider);

        return hbox;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        launch(args);
    }
    
}
