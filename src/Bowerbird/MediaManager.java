package Bowerbird;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class MediaManager {

    public static final Double MIN_DURATION_CHANGE = 0.5;

    @FXML private Slider timeSlider;
    @FXML private Slider volumeSlider;
    @FXML private Label songInfo, currentTime, totalTime;
    @FXML private Button playButton, pauseButton, stopButton, addButton;

    private BowerbirdDB bowerbirdDB;

    private Media media;
    private MediaPlayer mediaPlayer;

    private String artist, title, album, year, genre;

    public MediaManager(Slider volumeSlider, Slider timeSlider,
                        Label songInfo, Label currentTime, Label totalTime,
                        Button playButton, Button pauseButton, Button stopButton, Button addButton)
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

        bowerbirdDB = new BowerbirdDB();

        SetSliders();
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
        artist = "unknown"; title = "-"; album = "-"; year = "-"; genre = "N/A";

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

        bowerbirdDB.insert(musicRecord);
    }

    public void UpdateLabel()
    {
        songInfo.setText("Title: " + title + "\nArtist: " + artist + "\n" + "Album: " + album + "\n"
                        + "Year: " + year + "\n" + "Genre: " + genre);
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
