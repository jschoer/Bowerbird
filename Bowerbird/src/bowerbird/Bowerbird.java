/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bowerbird;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableObjectValue;
import javafx.scene.layout.VBox;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import javafx.util.Duration;

public class Bowerbird extends Application {
    
    private String artist, title, album, year, genre;
    private Duration duration;
    private MediaPlayer mp;
    private Slider timeSlider;
    @Override
    public void start(Stage primaryStage) {

        //Slider timeSlider = new Slider();
        //timeSlider.setValue(mp.getVolume()*100);
        //timeSlider.valueProperty().addListener(new InvalidationListener() {

        //    @Override
        //    public void invalidated(Observable observable) {
                //mp.seek(duration.multiply(timeSlider.getValue() / 100));

        //    }
        //});

        //GridPane grid = new GridPane();
        BorderPane layout = new BorderPane();
        HBox hbox = topMenu();
        TabPane tabPane = leftSideMenu();
        layout.setTop(hbox);
        layout.setLeft(tabPane);
        layout.setMinSize(400, 400);
        layout.setPadding(new Insets(10, 10, 10, 10));
        //grid.setVgap(10);
        //grid.setHgap(10);
        //grid.setAlignment(Pos.CENTER);
        //grid.add(playBtn, 0, 2);
        //grid.add(pauseBtn, 1, 2);
        //grid.add(stopBtn, 3, 2);
        //grid.add(outputLabel,0,0);
        //grid.add(timeSlider,3,3);
        //grid.add(volumeSlider,8,3);
        Scene scene = new Scene(layout, 600, 600);

        primaryStage.setTitle("Bowerbird");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public TabPane leftSideMenu() {
        TabPane leftSideMenu = new TabPane();
        leftSideMenu.setPrefWidth(165);

        Tab songTab = new Tab();
        songTab.setText("Songs");
        songTab.setContent(new Label("\nList of Songs\n will go here..."));

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

        Media media = new Media("file:///Users/Josh5/OneDrive/Documents/GitHub/Bowerbird/Bowerbird/resources/test.mp3");
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

        mp = new MediaPlayer(media);

        mp.currentTimeProperty().addListener(new InvalidationListener()
        {
            public void invalidated(Observable ov) {
                update();
            }
        });

        mp.setOnReady(new Runnable() {
            @Override
            public void run() {
                duration = mp.getMedia().getDuration();
                update();
            }
        });

        Label outputLabel = new Label();

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

        Slider volumeSlider = new Slider();
        volumeSlider.setValue(mp.getVolume()*100);
        volumeSlider.valueProperty().addListener(new InvalidationListener() {

            @Override
            public void invalidated(Observable observable) {
                mp.setVolume(volumeSlider.getValue() / 100);

            }
        });

        timeSlider = new Slider();
        timeSlider.setMinWidth(50);
        timeSlider.setMaxWidth(Double.MAX_VALUE);
        timeSlider.valueProperty().addListener(new InvalidationListener() {
            public void invalidated(Observable ov) {
                if (timeSlider.isValueChanging()) {
                    // multiply duration by percentage calculated by slider position
                    mp.seek(duration.multiply(timeSlider.getValue() / 100.0));
                }
            }
        });

        hbox.getChildren().addAll(playBtn, pauseBtn, stopBtn, outputLabel, volumeSlider, timeSlider);

        return hbox;
    }

    protected void update()
    {
        Platform.runLater(new Runnable() {
            public void run() {
                Duration currentTime = mp.getCurrentTime();
                timeSlider.setDisable(duration.isUnknown());
                if (!timeSlider.isDisabled()
                        && duration.greaterThan(Duration.ZERO)
                        && !timeSlider.isValueChanging()) {
                    timeSlider.setValue(currentTime.divide(duration).toMillis() * 100.0);
                }
            }
        });
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
