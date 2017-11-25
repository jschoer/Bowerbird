package Bowerbird;

import javafx.application.Platform;
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
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioSpectrumListener;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import javax.swing.text.*;
import java.util.*;

public class MediaManager {

    public static final Double MIN_DURATION_CHANGE = 0.5;

    @FXML private Slider timeSlider;
    @FXML private Slider volumeSlider;
    @FXML private Label songInfo, currentTime, totalTime;
    @FXML private Button playButton, pauseButton, stopButton, addButton, toVisuals, toLibrary;
    @FXML private VBox songTab;
    @FXML private Accordion playlistTab;
    @FXML private VBox visuals;
    @FXML private ComboBox<String> searchType;
    private TableView library;

    public BowerbirdDB bowerbirdDB;

    private Media media;
    private MediaPlayer mediaPlayer;

    private String artist, title, album, year, genre, lyrics;
    private int track;

    public List<MusicRecord> musicRecordList;
    public List<Playlist> playlists;

    public MediaManager(Slider volumeSlider, Slider timeSlider,
                        Label songInfo, Label currentTime, Label totalTime,
                        Button playButton, Button pauseButton, Button stopButton, Button addButton, Button toVisuals,
                        VBox songTab, Accordion playlistTab, VBox visuals)
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
        this.visuals = visuals;

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

    public void ImportNewMedia(String path)
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

                        lyrics = bowerbirdDB.getLyrics(path);
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
                databaseInsert(path);
                UpdateLabel();
                SetTimeStamps();
                CreateVisualizations();
            }
        });
    }

    public void UpdateMedia(MusicRecord m)
    {
        artist = "unknown"; title = "-"; album = "-"; year = "-"; genre = "N/A"; track = 0; lyrics = "none";

        playButton.setDisable(false); pauseButton.setDisable(true); stopButton.setDisable(false);

        media = new Media(m.getFilePath());

        media.getMetadata().addListener(new MapChangeListener<String, Object>() {
            @Override
            public void onChanged(MapChangeListener.Change<? extends String, ? extends Object> change) {
                if(change.wasAdded())
                {
                    title = m.getTitle();
                    artist = m.getArtist();
                    album = m.getAlbum();
                    year = m.getYear();
                    genre = m.getGenre();
                    track = m.getTrackNum();
                    lyrics = m.getLyrics();
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
                UpdateLabel();
                SetTimeStamps();
                CreateVisualizations();
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

        library = new TableView();
        ObservableList<MusicRecord> musicRecsObs = FXCollections.observableArrayList(musicRecordList);

        TableColumn titleCol = new TableColumn("Name");
        titleCol.setCellValueFactory(new PropertyValueFactory<MusicRecord, String>("Title"));

        TableColumn artistCol = new TableColumn("Artist");
        artistCol.setCellValueFactory(new PropertyValueFactory<MusicRecord, String>("Artist"));

        TableColumn albumCol = new TableColumn("Album");
        albumCol.setCellValueFactory(new PropertyValueFactory<MusicRecord, String>("Album"));

        TableColumn genreCol = new TableColumn("Genre");
        genreCol.setCellValueFactory(new PropertyValueFactory<MusicRecord, String>("Genre"));

        TableColumn yearCol = new TableColumn("Year");
        yearCol.setCellValueFactory(new PropertyValueFactory<MusicRecord, String>("Year"));

        library.getColumns().addAll(titleCol, artistCol, albumCol, genreCol, yearCol);
        library.setItems(musicRecsObs);

        SetLibraryForSongPlaying();

        songTab.getChildren().addAll(library);
    }

    public Button songButton(int indexOfCurrentSong, Playlist plst, VBox v, TitledPane plstEntry)
    {
        Button newButton = new Button(plst.get_playlistContent().get(indexOfCurrentSong-1).getTitle());

        newButton.setOnAction(event ->
        {
            UpdateMedia(plst.get_playlistContent().get(indexOfCurrentSong-1));
            Play();

            mediaPlayer.setOnEndOfMedia(new Runnable() {
                @Override
                public void run() {
                    mediaPlayer.stop();

                    for(int i = indexOfCurrentSong; i < plst.get_playlistContent().size(); i++)
                    {
                        UpdateMedia(plst.get_playlistContent().get(i));
                        Play();
                    }
                }
            });
        });

        newButton.setOnDragDetected(event ->
        {
            if(newButton != null)
            {
                Dragboard db = newButton.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent cc = new ClipboardContent();
                cc.putString(String.valueOf(indexOfCurrentSong));
                db.setContent(cc);

                event.consume();
            }
        });

        newButton.setOnDragOver(event ->
        {
            if(event.getDragboard().hasString())
            {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        newButton.setOnDragDropped(event ->
        {
            Dragboard d = event.getDragboard();
            if(d.hasString())
            {
                int movedSongPos = Integer.parseInt(d.getString());
                int movedSongID = plst.get_playlistContent().get(movedSongPos-1).getSongID();

                bowerbirdDB.reorderPlaylist(plst.get_playlistName(), movedSongID, movedSongPos, indexOfCurrentSong);

                AddContentToPlaylistTab(plst, v, plstEntry);
                plstEntry.setContent(v);

                event.setDropCompleted(true);
                event.consume();
            }
        });

        return newButton;
    }

    public void SetLibraryForSongPlaying()
    {
        library.setRowFactory(tv -> {
            TableRow<MusicRecord> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && !row.isEmpty()) {
                    MusicRecord rowData = row.getItem();
                    UpdateMedia(rowData);
                }
            });

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    MusicRecord rowData = row.getItem();
                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                        mediaPlayer.dispose();
                    }

                    library.setStyle("-fx-background-color: rgb(47, 145, 132)");
                    row.setStyle("-fx-background-color: gray");

                    UpdateMedia(rowData);
                    Play();
                }
            });

            row.setOnDragDetected(event -> {
                if(!row.isEmpty())
                {
                    Dragboard db = row.startDragAndDrop(TransferMode.COPY);
                    ClipboardContent cc = new ClipboardContent();
                    cc.putString(String.valueOf(row.getItem().getSongID()));
                    db.setContent(cc);

                    event.consume();
                }
            });

            return row;
        });

        library.setOnDragOver(event -> {
            if(event.getGestureSource() != library && event.getDragboard().hasString())
            {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });
    }

    public void CreateVisualizations()
    {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis(0,50,10);

        BarChart<String, Number> visualizerChart = new BarChart<>(xAxis, yAxis);
        visualizerChart.setLegendVisible(false);
        visualizerChart.setAnimated(false);
        visualizerChart.setBarGap(0);
        visualizerChart.setCategoryGap(0);
        visualizerChart.setVerticalGridLinesVisible(false);
        visualizerChart.setVerticalZeroLineVisible(false);
        visualizerChart.setHorizontalGridLinesVisible(false);
        visualizerChart.setHorizontalZeroLineVisible(false);

        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis, null, "dB"));
        yAxis.setTickLabelFill(Color.TRANSPARENT);

        xAxis.setTickLabelFill(Color.TRANSPARENT);

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        for (int i = 0; i < 128; i++)
        {
            series.getData().add(new XYChart.Data<>(Integer.toString(i + 1), 50));
        }

        visualizerChart.getData().add(series);
        visuals.getChildren().clear();
        visuals.getChildren().add(visualizerChart);

        mediaPlayer.setAudioSpectrumListener(new AudioSpectrumListener() {
            @Override
            public void spectrumDataUpdate(double timestamp, double duration, float[] magnitudes, float[] phases) {
                for(int i = 0; i < magnitudes.length; i++)
                {
                    visualizerChart.getData().get(0).getData().get(i).setYValue(magnitudes[i] + 60);
                }
            }
        });
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
        visuals.getChildren().clear();

        currentTime.setText("00.00.00");
        totalTime.setText("00.00.00");
        songInfo.setText("Song Information");
    }

    public void RemoveSongFromLibrary()
    {
        if(mediaPlayer != null)
        {
            Alert removeSongFromLibrary = new Alert(Alert.AlertType.CONFIRMATION);
            removeSongFromLibrary.setTitle("Confirm Removal of Song From Library");
            removeSongFromLibrary.setContentText("Are you sure you want to remove \"" + title + "\" from your library?");

            java.util.Optional<ButtonType> result = removeSongFromLibrary.showAndWait();

            if(result.get() == ButtonType.OK)
            {
                Stop();
                bowerbirdDB.removeSong(title, album, artist);
                musicRecordList = bowerbirdDB.getAllMusicRecords();
                AddSongsToLibrary();
            }
            else
            {
                removeSongFromLibrary.showAndWait();
            }
        }
    }

    public void EditSongInfo()
    {
        if(mediaPlayer != null)
        {
            Dialog editSong = new Dialog();
            ButtonType saveChanges = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            editSong.getDialogPane().getButtonTypes().addAll(saveChanges, ButtonType.CANCEL);

            TabPane songInfoContent = new TabPane();
            Tab songData = new Tab("Song Data");
            Tab lyricTab = new Tab("Lyrics");

            VBox songDataVBox = new VBox();
            HBox songTitleRow = new HBox();
            HBox songArtistRow = new HBox();
            HBox songAlbumRow = new HBox();
            HBox songTrackRow = new HBox();
            HBox songYearRow = new HBox();
            HBox songGenreRow = new HBox();
            VBox songLyricsVBox = new VBox();

            TextField songTitleField = new TextField(title);
            TextField songArtistField = new TextField(artist);
            TextField songAlbumField = new TextField(album);
            TextField songTrackField = new TextField(String.valueOf(track));
            TextField songYearField = new TextField(year);
            TextField songGenreField = new TextField(genre);
            TextArea songLyricsArea = new TextArea(lyrics);

            songTitleRow.getChildren().addAll(new Label("Title: "), songTitleField);
            songArtistRow.getChildren().addAll(new Label("Artist: "), songArtistField);
            songAlbumRow.getChildren().addAll(new Label("Album: "), songAlbumField);
            songTrackRow.getChildren().addAll(new Label("Track#: "), songTrackField);
            songYearRow.getChildren().addAll(new Label("Year: "), songYearField);
            songGenreRow.getChildren().addAll(new Label("Genre: "), songGenreField);

            songDataVBox.getChildren().addAll(songTitleRow, songArtistRow, songAlbumRow, songTrackRow, songYearRow, songGenreRow);
            songLyricsVBox.getChildren().addAll(songLyricsArea);
            songData.setContent(songDataVBox);
            lyricTab.setContent(songLyricsVBox);
            songInfoContent.getTabs().addAll(songData, lyricTab);

            editSong.getDialogPane().setContent(songInfoContent);

            java.util.Optional<ButtonType> result = editSong.showAndWait();

            try
            {
                if(result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE)
                {
                    int trk = Integer.parseInt(songTrackField.getText());

                    bowerbirdDB.editSong(title, album, artist, songTitleField.getText(), songAlbumField.getText(), songArtistField.getText(),
                            trk, songYearField.getText(), songGenreField.getText(), songLyricsArea.getText());

                    UpdateLabel();
                    musicRecordList = bowerbirdDB.getAllMusicRecords();
                    AddSongsToLibrary();
                }
                else
                {
                    editSong.close();
                }
            }
            catch(NumberFormatException e)
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Dialog");
                alert.setContentText("Please enter an integer value for the track number.");

                alert.showAndWait();
            }
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

    //region Playlists
    public boolean createNewPlaylist(String playlistName)
    {
        if(!bowerbirdDB.newPlaylist(playlistName))
            return false;

        playlists = bowerbirdDB.getAllPlaylists();
        AddPlaylistsToTab();

        return true;
    }

    private void AddPlaylistsToTab()
    {
        playlistTab.getPanes().clear();

        for (int i = 0; i < playlists.size(); i++)
        {
            TitledPane plstEntry = new TitledPane();
            VBox v = PlaylistAccordionVBox(playlists.get(i), plstEntry);

            plstEntry.setText(playlists.get(i).get_playlistName());

            AddContentToPlaylistTab(playlists.get(i), v, plstEntry);
            plstEntry.setContent(v);
            playlistTab.getPanes().add(plstEntry);
        }
    }

    private VBox PlaylistAccordionVBox(Playlist plst, TitledPane plstEntry)
    {
        VBox v = new VBox();

        v.setOnDragOver(event -> {
            if(event.getGestureSource() != v && event.getDragboard().hasString())
            {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        v.setOnDragEntered(event ->
        {
            if(event.getTransferMode() == TransferMode.COPY)
            {

            }
        });

        v.setOnDragDropped(event -> {
            Dragboard d = event.getDragboard();
            if(d.hasString() && event.getTransferMode() == TransferMode.COPY)
            {
                int songID = Integer.parseInt(d.getString());
                bowerbirdDB.addToPlaylist(plst.get_playlistName(), songID);

                AddContentToPlaylistTab(plst, v, plstEntry);
                plstEntry.setContent(v);
            }
        });

        return v;
    }

    private void AddContentToPlaylistTab(Playlist plst, VBox playlistContent, TitledPane plstEntry)
    {
        playlistContent.getChildren().clear();
        plst.set_playlistContent(bowerbirdDB.getPlaylistContent(plst.get_playlistName()));

        HBox controlRow = new HBox();
        Button rename = RenameThisPlaylistButton(plst);
        Button delete = DeleteThisPlaylistButton(plst, playlistContent, plstEntry);
        controlRow.getChildren().addAll(rename, delete);
        playlistContent.getChildren().add(controlRow);
        rename.setVisible(true);
        delete.setVisible(true);

        for (int i = 1; i < plst.get_playlistContent().size() + 1; i++)
        {
            Button newButton = songButton(i, plst, playlistContent, plstEntry);
            newButton.getStyleClass().add("tab-button");

            playlistContent.getChildren().add(newButton);
        }
    }

    private Button RenameThisPlaylistButton(Playlist plst)
    {
        Button r = new Button("Rename");

        r.setOnAction(event -> {
            TextInputDialog dialog = new TextInputDialog("newPlaylist");
            dialog.setTitle("Rename Playlist");
            dialog.setHeaderText("Rename the playlist \"" + plst.get_playlistName() + "\" to: ");

            Optional<String> result = dialog.showAndWait();

            if(result.isPresent())
            {
                bowerbirdDB.renamePlaylist(plst.get_playlistName(), result.get());
                playlists = bowerbirdDB.getAllPlaylists();
                AddPlaylistsToTab();
            }
        });

        return r;
    }

    private Button DeleteThisPlaylistButton(Playlist plst, VBox v, TitledPane plstEntry)
    {
        Button d = new Button("Delete");

        d.setOnAction(event -> {
            Alert wannaDelete = new Alert(Alert.AlertType.CONFIRMATION);
            wannaDelete.setTitle("Confirm Playlist Delete");
            wannaDelete.setContentText("Are you sure you want to delete the playlist \"" + plst.get_playlistName() + "\"?");

            java.util.Optional<ButtonType> result = wannaDelete.showAndWait();

            if(result.get() == ButtonType.OK)
            {
                bowerbirdDB.deletePlaylist(plst.get_playlistName());
                playlists = bowerbirdDB.getAllPlaylists();
                AddPlaylistsToTab();
            }
            else
            {
                wannaDelete.close();
            }
        });

        d.setOnDragOver(event -> {
            if(event.getGestureSource() != d && event.getDragboard().hasString())
            {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        d.setOnDragEntered(event -> {
            if(event.getTransferMode() == TransferMode.MOVE)
            {

            }
        });

        d.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if(db.hasString() && event.getTransferMode() == TransferMode.MOVE)
            {
                int movedSongPos = Integer.parseInt(db.getString());
                bowerbirdDB.removeFromPlaylist(plst.get_playlistName(), movedSongPos);

                AddContentToPlaylistTab(plst, v, plstEntry);
                plstEntry.setContent(v);
            }
        });

        return d;
    }

    //endregion Playlists
}
