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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class Bowerbird extends Application {

    public static final Double MIN_DURATION_CHANGE = 0.5;
    
    private String artist = "unknown", title = "-", album = "-", year = "-", genre = "N/A", filename = "";

    private Media media;
    private MediaPlayer mediaPlayer;

    private Slider timeSlider, volumeSlider;
    private Button playBtn, pauseBtn, stopBtn, addSong;

    private BowerbirdDB bowerbirdDB;

    private List<MusicRecord> musicRecordList;
    private List<AlbumRecord> albumRecordList;

    @Override
    public void start(Stage primaryStage)
    {
        BorderPane layout = new BorderPane();
        HBox hbox = topMenu(primaryStage);
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

    public void init()
    {
        playBtn = new Button();
        pauseBtn = new Button();
        stopBtn = new Button();
        addSong = new Button();

        timeSlider = new Slider();
        volumeSlider = new Slider();

        bowerbirdDB = new BowerbirdDB();

        musicRecordList = bowerbirdDB.getAllMusicRecords();
        for(int i = 0; i < musicRecordList.size(); i++)
        {
            System.out.println(musicRecordList.get(i).get_title());
        }
    }

    public TabPane leftSideMenu()
    {
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

    public HBox topMenu(Stage stg)
    {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 10, 12));
        hbox.setSpacing(10);
        hbox.setStyle("-fx-background-color: #D3D3D3;");
        Label outputLabel = new Label();

        final FileChooser fc = new FileChooser();

        addSong.setText("Pick Song");
        addSong.setAlignment(Pos.TOP_RIGHT);
        addSong.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event)
            {
                java.io.File f = fc.showOpenDialog(stg);

                if(f != null)
                {
                    filename = f.toURI().toASCIIString();
                    System.out.println("Filename: " + filename);
                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                        mediaPlayer.dispose();
                    }
                    UpdateMedia(filename);

                    playBtn.setDisable(false);
                    pauseBtn.setDisable(false);
                    stopBtn.setDisable(false);
                    outputLabel.setText("");

                }
                else
                    System.out.println("Chosen file is null.");
            }
        });

        playBtn.setText(">");
        playBtn.setAlignment(Pos.CENTER);
        playBtn.setDisable(true);
        playBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                mediaPlayer.play();
                outputLabel.setText("Now Playing\n" + "Title: " + title + "\n"
                        + "Artist: " + artist + "\n" + "Album: " + album + "\n"
                        + "Year: " + year + "\n" + "Genre: " + genre);
                timeSlider.setDisable(false);
                timeSlider.setValue(0);
                playBtn.setDisable(true);
                pauseBtn.setDisable(false);
                stopBtn.setDisable(false);
            }
        });

        pauseBtn.setText("||");
        pauseBtn.setAlignment(Pos.CENTER);
        pauseBtn.setDisable(true);
        pauseBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event)
            {
                mediaPlayer.pause();
                playBtn.setDisable(false);
                pauseBtn.setDisable(true);
                System.out.println("Paused.");
            }
        });

        stopBtn.setText("S");
        stopBtn.setAlignment(Pos.CENTER);
        stopBtn.setDisable(true);
        stopBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event)
            {
                mediaPlayer.stop();
                pauseBtn.setDisable(true);
                stopBtn.setDisable(true);
                timeSlider.setDisable(true);
                System.out.println("Stopped.");
                outputLabel.setText("");
            }
        });

        volumeSlider.setValue(100);
        volumeSlider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable)
            {
                if(mediaPlayer != null)
                {
                    mediaPlayer.setVolume(volumeSlider.getValue() / 100);
                }
            }
        });

        timeSlider.setDisable(true);
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

        hbox.getChildren().addAll(playBtn, pauseBtn, stopBtn, outputLabel, volumeSlider, timeSlider, addSong);

        return hbox;
    }

    public void UpdateMedia(String filename)
    {
        artist = "unknown"; title = "-"; album = "-"; year = "-"; genre = "N/A";

        media = new Media(filename);
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

        mediaPlayer.totalDurationProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                timeSlider.setMax(newValue.toSeconds());
            }
        });

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                databaseInsert();
            }
        });
    }

    public void databaseInsert()
    {
        System.out.println("Attempting database insert.");

        MusicRecord musicRecord = new MusicRecord();
        musicRecord.set_album(album);
        musicRecord.set_artist(artist);
        musicRecord.set_filePath(filename);
        musicRecord.set_genre(genre);
        musicRecord.set_title(title);
        musicRecord.set_year(year);

        bowerbirdDB.insert(musicRecord);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        launch(args);
    }
    
}
