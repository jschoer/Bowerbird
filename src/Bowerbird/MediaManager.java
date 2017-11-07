package Bowerbird;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class MediaManager {

    public static final Double MIN_DURATION_CHANGE = 0.5;

    @FXML private Slider timeSlider;
    @FXML private Slider volumeSlider;
    @FXML private Label songInfo, currentTime, totalTime;
    @FXML private Button playButton, pauseButton, stopButton, addButton, toVisuals, toLibrary;
    @FXML private VBox songTab;

    private BowerbirdDB bowerbirdDB;

    private Media media;
    private MediaPlayer mediaPlayer;

    private String artist, title, album, year, genre, lyrics;
    private int trackNumber;

    public List<MusicRecord> musicRecordList;

    public MediaManager(Slider volumeSlider, Slider timeSlider,
                        Label songInfo, Label currentTime, Label totalTime,
                        Button playButton, Button pauseButton, Button stopButton, Button addButton, Button toVisuals,
                        VBox songTab)
    {
        this.volumeSlider = volumeSlider;
        this.timeSlider = timeSlider;
        this.songInfo = songInfo;
        this.currentTime = currentTime;
        this.totalTime = totalTime;
        this.playButton = playButton;
        this.pauseButton = pauseButton;
        this.stopButton = stopButton;
        this.addButton = addButton;
        this.songTab = songTab;
        this.toVisuals = toVisuals;

        bowerbirdDB = new BowerbirdDB();

        SetSliders();

        musicRecordList = bowerbirdDB.getAllMusicRecords();
        AddSongsToTab();
    }

    public void SetSliders()
    {
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
    }

    public void UpdateMedia(String path, boolean fromList)
    {
        artist = "unknown"; title = "-"; album = "-"; year = "-"; genre = "N/A"; trackNumber = 0; lyrics = "none";

        playButton.setDisable(false); pauseButton.setDisable(true); stopButton.setDisable(false);

        media = new Media(path);
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
                        case "trackNumber": trackNumber = Integer.parseInt(value.toString());
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

        mediaPlayer.setOnReady(new Runnable() {
            @Override
            public void run() {
                if(!fromList)
                    databaseInsert(path);
                UpdateLabel();
                SetTimeStamps();
            }
        });
    }

    public void databaseInsert(String path)
    {
        System.out.println("Attempting database insert.");

        MusicRecord musicRecord = new MusicRecord();
        musicRecord.set_album(album);
        musicRecord.set_artist(artist);
        musicRecord.set_filePath(path);
        musicRecord.set_genre(genre);
        musicRecord.set_title(title);
        musicRecord.set_year(year);

        bowerbirdDB.importSong(musicRecord);
        musicRecordList = bowerbirdDB.getAllMusicRecords();
        AddSongsToTab();
    }

    public void UpdateLabel()
    {
        songInfo.setText("Title: " + title + "\n" + "Artist: " + artist + "\n" + "Album: " + album + "\n" + "Track#: "
                + trackNumber + "\n" + "Year: " + year + "\n" + "Genre: " + genre + "\n" + "Lyrics: \n\n" + lyrics);
    }

    public void SetTimeStamps()
    {
        Duration totalDuration = mediaPlayer.getTotalDuration();
        double totalTimeInSeconds = totalDuration.toSeconds();

        int[] songTime = splitTime(totalTimeInSeconds);

        totalTime.setText(String.format("%02d.%02d.%02d", songTime[0], songTime[1], songTime[2]));

        mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                int[] time = splitTime(newValue.toSeconds());
                currentTime.setText(String.format("%02d.%02d.%02d", time[0], time[1], time[2]));
            }
        });
    }

    public int[] splitTime(double totalTimeInSeconds)
    {
        int hour = (int)totalTimeInSeconds / 3600;
        int remainder = (int)totalTimeInSeconds - hour * 3600;
        int min = remainder / 60;
        remainder = remainder - min * 60;
        int sec = remainder;

        return new int[] {hour, min, sec};
    }

    public void AddSongsToTab()
    {
        if(songTab != null)
            songTab.getChildren().clear();

        for (int i = 1; i < musicRecordList.size() + 1; i++)
        {
            Button newButton = songButton(i, musicRecordList.get(i - 1));
            newButton.getStyleClass().add("tab-button");

            songTab.getChildren().add(newButton);
        }
    }

    public Button songButton(int index, MusicRecord musicRecord)
    {
        Button newButton = new Button(index + ". " + musicRecord.get_title());
        newButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(mediaPlayer != null)
                {
                    mediaPlayer.stop();
                    mediaPlayer.dispose();

                    newButton.setStyle("-fx-background-color: yellow");
                }
                UpdateMedia(musicRecord.get_filePath(), true);
                mediaPlayer.play();
            }
        });

        return newButton;
    }

    //region MediaPlayer
    public void Play()
    {
        mediaPlayer.play();
        playButton.setDisable(true);
        pauseButton.setDisable(false);
        timeSlider.setDisable(false);
    }

    public void Pause()
    {
        mediaPlayer.pause();
        pauseButton.setDisable(true);
        playButton.setDisable(false);
    }

    public void Stop()
    {
        mediaPlayer.stop();
        playButton.setDisable(false);
        pauseButton.setDisable(true);
        timeSlider.setDisable(true);

        currentTime.setText("00.00.00");
        totalTime.setText("00.00.00");
        songInfo.setText("Song Information");

        VBox vbox = songTab;
        for(Node node : vbox.getChildren())
        {
            node.setStyle("-fx-background-color: white");
        }
    }
    //endregion MediaPlayer

    //region GetSet
    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }
    //endregion GetSet
}
