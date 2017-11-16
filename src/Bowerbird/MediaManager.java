package Bowerbird;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.util.List;
import java.util.Optional;

public class MediaManager {

    public static final Double MIN_DURATION_CHANGE = 0.5;

    @FXML private Slider timeSlider;
    @FXML private Slider volumeSlider;
    @FXML private Label songInfo, currentTime, totalTime;
    @FXML private Button playButton, pauseButton, stopButton, addButton, toVisuals, toLibrary;
    @FXML private VBox songTab;
    @FXML private Accordion playlistTab;

    private BowerbirdDB bowerbirdDB;

    private Media media;
    private MediaPlayer mediaPlayer;

    private String artist, title, album, year, genre, lyrics;
    private int track;

    public List<MusicRecord> musicRecordList;
    public List<Playlist> playlists;

    public MediaManager(Slider volumeSlider, Slider timeSlider,
                        Label songInfo, Label currentTime, Label totalTime,
                        Button playButton, Button pauseButton, Button stopButton, Button addButton, Button toVisuals,
                        VBox songTab, Accordion playlistTab)
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
        this.playlistTab = playlistTab;
        this.toVisuals = toVisuals;

        bowerbirdDB = new BowerbirdDB();

        SetSliders();

        musicRecordList = bowerbirdDB.getAllMusicRecords();
        AddSongsToLibrary();

        playlists = bowerbirdDB.getAllPlaylists();
        AddPlaylistsToTab();
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
        artist = "unknown"; title = "-"; album = "-"; year = "-"; genre = "N/A"; track = 0; lyrics = "none";

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
                        case "track number": track = Integer.parseInt(value.toString());
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
        musicRecord.setAlbum(album);
        musicRecord.setArtist(artist);
        musicRecord.setTrackNum(track);
        musicRecord.setFilePath(path);
        musicRecord.setGenre(genre);
        musicRecord.setTitle(title);
        musicRecord.setYear(year);

        bowerbirdDB.importSong(musicRecord);
        musicRecordList = bowerbirdDB.getAllMusicRecords();
        AddSongsToLibrary();
    }

    public void UpdateLabel()
    {
        songInfo.setText("Name: " + title + "\n" + "Artist: " + artist + "\n" + "Album: " + album + "\n" + "Track#: "
                + track + "\n" + "Year: " + year + "\n" + "Genre: " + genre + "\n" + "Lyrics: \n\n" + lyrics);
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

    public void AddSongsToLibrary()
    {
        songTab.getChildren().clear();

        TableView library = new TableView();
        ObservableList<MusicRecord> musicRecsObs = FXCollections.observableArrayList(musicRecordList);

        TableColumn titleCol = new TableColumn("Name");
        titleCol.setMinWidth(100);
        titleCol.setCellValueFactory(new PropertyValueFactory<MusicRecord, String>("Title"));

        TableColumn artistCol = new TableColumn("Artist");
        titleCol.setMinWidth(100);
        titleCol.setCellValueFactory(new PropertyValueFactory<MusicRecord, String>("Artist"));

        TableColumn albumCol = new TableColumn("Album");
        titleCol.setMinWidth(100);
        titleCol.setCellValueFactory(new PropertyValueFactory<MusicRecord, String>("Album"));

        TableColumn genreCol = new TableColumn("Genre");
        titleCol.setMinWidth(100);
        titleCol.setCellValueFactory(new PropertyValueFactory<MusicRecord, String>("Genre"));

        TableColumn yearCol = new TableColumn("Year");
        titleCol.setMinWidth(100);
        titleCol.setCellValueFactory(new PropertyValueFactory<MusicRecord, String>("Year"));

        library.setItems(musicRecsObs);
        library.getColumns().addAll(titleCol, artistCol, albumCol, genreCol, yearCol);

        library.setRowFactory(tv -> {
            TableRow<MusicRecord> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if(event.getClickCount() == 2 && !row.isEmpty())
                {
                    MusicRecord rowData = row.getItem();
                    if(mediaPlayer != null)
                    {
                        mediaPlayer.stop();
                        mediaPlayer.dispose();


                        row.setStyle("-fx-background-color: yellow");
                        //newButton.setStyle("-fx-background-color: yellow");
                    }
                    UpdateMedia(rowData.getFilePath(), true);
                    mediaPlayer.play();
                }
            });
            return row;
        });

        songTab.getChildren().addAll(library);

//        for (int i = 1; i < musicRecordList.size() + 1; i++)
//        {
//            Button newButton = songButton(musicRecordList.get(i - 1));
//            newButton.getStyleClass().add("tab-button");
//
//            songTab.getChildren().add(newButton);
//        }
    }

    public Button songButton(MusicRecord musicRecord)
    {
        Button newButton = new Button(musicRecord.getTitle());
        newButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(mediaPlayer != null)
                {
                    mediaPlayer.stop();
                    mediaPlayer.dispose();
                }
<<<<<<< HEAD
                UpdateMedia(musicRecord.getFilePath(), true);
                mediaPlayer.play();
=======
                UpdateMedia(musicRecord.get_filePath(), true);
                Play();
>>>>>>> master
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
        stopButton.setDisable(false);
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
        stopButton.setDisable(true);
        timeSlider.setDisable(true);

        currentTime.setText("00.00.00");
        totalTime.setText("00.00.00");
        songInfo.setText("Song Information");
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

    //region Playlists
    public boolean createNewPlaylist(String playlistName)
    {
        if(!bowerbirdDB.newPlaylist(playlistName))
            return false;

        playlists = bowerbirdDB.getAllPlaylists();
        AddPlaylistsToTab();

        return true;
    }

    public void AddPlaylistsToTab()
    {
        playlistTab.getPanes().clear();

        for (int i = 1; i < playlists.size() + 1; i++)
        {
            TitledPane plstEntry = new TitledPane();
            VBox playlistContent = new VBox();
            playlistContent.getChildren().add(GetPlaylistControls(playlists.get(i-1)));

            plstEntry.setText(playlists.get(i-1).get_playlistName());
            playlists.get(i-1).set_playlistContent(bowerbirdDB.getPlaylistContent(playlists.get(i-1).get_playlistName()));
            AddSongsToPlaylistTab(plstEntry, playlists.get(i-1), playlistContent);

            plstEntry.setContent(playlistContent);
            playlistTab.getPanes().add(plstEntry);
        }
    }

    public ComboBox<String> GetPlaylistControls(Playlist plst)
    {
        ComboBox<String> plstControls = new ComboBox<String>();
        plstControls.getItems().addAll("Add Songs", "Remove Songs", "Reorder Songs", "Rename Playlist", "Delete Playlist");
        plstControls.setValue("Playlist options...");

        System.out.println("Playlist option: " + plstControls.getValue());

        plstControls.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                switch(newValue)
                {
                    case "Add Songs":
                        AddSongsToThisPlaylist();
                        break;
                    case "Remove Songs":
                        RemoveSongsFromThisPlaylist();
                        break;
                    case "Reorder Songs":
                        ReorderSongsInThisPlaylist();
                        break;
                    case "Rename Playlist":
                        RenameThisPlaylist(plst);
                        break;
                    case "Delete Playlist":
                        DeleteThisPlaylist(plst);
                        break;
                }
            }
        });

        return plstControls;
    }

    public void AddSongsToPlaylistTab(TitledPane plstEntry, Playlist plst, VBox playlistContent)
    {
        for (int i = 1; i < plst.get_playlistContent().size() + 1; i++)
        {
            Button newButton = songButton(plst.get_playlistContent().get(i - 1));
            newButton.getStyleClass().add("tab-button");

            playlistContent.getChildren().add(newButton);
        }
    }

    public void AddSongsToThisPlaylist()
    {

    }

    public void RemoveSongsFromThisPlaylist()
    {

    }

    public void ReorderSongsInThisPlaylist()
    {

    }

    public void RenameThisPlaylist(Playlist plst)
    {
        TextInputDialog dialog = new TextInputDialog("newPlaylist");
        dialog.setTitle("Rename Playlist");
        dialog.setHeaderText("Rename the playlist \"" + plst.get_playlistName() + "\" to: ");

        Optional<String> result = dialog.showAndWait();

        if(result.isPresent())
        {
            System.out.println("result = " + result.toString());
            bowerbirdDB.renamePlaylist(plst.get_playlistName(), result.get());

            playlists = bowerbirdDB.getAllPlaylists();
            AddPlaylistsToTab();
        }
    }

    public void DeleteThisPlaylist(Playlist plst)
    {
        Alert wannaDelete = new Alert(Alert.AlertType.CONFIRMATION);

        wannaDelete.setTitle("Confirm Playlist Delete");
        wannaDelete.setContentText("Are you sure you want to delete the playlist \"" + plst.get_playlistName() + "\"?");

        java.util.Optional<ButtonType> result = wannaDelete.showAndWait();
        System.out.println("button choice: " + result.get().toString());

        if(result.get() == ButtonType.OK)
        {
            bowerbirdDB.deletePlaylist(plst.get_playlistName());

            playlists = bowerbirdDB.getAllPlaylists();
            AddPlaylistsToTab();
        }
        else
        {
            wannaDelete.showAndWait();
        }
    }

    //endregion Playlists
}
