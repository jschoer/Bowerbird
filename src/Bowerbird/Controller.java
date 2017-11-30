package Bowerbird;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class Controller extends BorderPane{

    @FXML private Slider timeSlider;
    @FXML private Slider volumeSlider;
    @FXML private Label songInfo, currentTime, totalTime, instructions, lyricsLabel;
    @FXML private Button playButton, pauseButton, stopButton, addButton, toVisuals, toLibrary, editSongButton, removeFromLibraryButton;
    @FXML private TabPane tabPane;
    @FXML private VBox songTab;
    @FXML private Accordion playlistTab;
    @FXML private VBox visuals;
    @FXML private ComboBox<String> searchType;
    @FXML private Button searchButton;
    @FXML private TextField fieldSearch;
    @FXML private ScrollPane lyricsPane;
    @FXML private VBox searchOutput;

    private MediaManager mediaManager;
    private boolean isVisuals = false;

    public Controller()
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Bowerbird.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try
        {
            loader.load();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @FXML public void initialize()
    {
        mediaManager = new MediaManager(volumeSlider, timeSlider,
                songInfo, currentTime, totalTime, instructions, lyricsLabel,
                playButton, pauseButton, stopButton, addButton, toVisuals,
                songTab, playlistTab, visuals);

        visuals.setVisible(false);
        visuals.managedProperty().bind(visuals.visibleProperty());
    }

    //region Handlers
    @FXML protected void handlePlayButtonAction(ActionEvent event) {
        mediaManager.Play();
    }

    @FXML protected void handlePauseButtonAction(ActionEvent event) {
        mediaManager.Pause();
    }

    @FXML protected void handleStopButtonAction(ActionEvent event) {
        mediaManager.Stop();
    }

    @FXML private void handleEditSongAction(ActionEvent event) {
        mediaManager.EditSongInfo();
    }

    @FXML private void handleRemoveSongAction(ActionEvent event) {
        mediaManager.RemoveSongFromLibrary();
    }

    @FXML protected void handleAddSongButtonAction(ActionEvent event) {

        final FileChooser fc = new FileChooser();
        java.io.File f = fc.showOpenDialog(this.getScene().getWindow());

        if(f != null)
        {
            String filePath = f.toURI().toASCIIString();
            System.out.println("Filename: " + filePath);

            if (mediaManager.getMediaPlayer() != null)
            {
                mediaManager.Stop();
                mediaManager.getMediaPlayer().dispose();
            }

            mediaManager.ImportNewMedia(filePath);
        }
        else
            System.out.println("Chosen file is null.");
    }

    @FXML protected void handleVolumeScrollEvent(ScrollEvent event)
    {
        double scrollSpeed = event.getDeltaY();
        volumeSlider.setValue(volumeSlider.getValue() + scrollSpeed / 10);
    }

    @FXML protected void handleTimeScrollEvent(ScrollEvent event)
    {
        double scrollSpeed = event.getDeltaY();
        timeSlider.setValue(timeSlider.getValue() + scrollSpeed / 10);
    }

    @FXML protected void handleNewPlaylistAction(ActionEvent event)
    {
        TextInputDialog dialog = new TextInputDialog("newPlaylist");
        dialog.setTitle("New Playlist");
        dialog.setHeaderText("Name your new playlist.");

        Optional<String> result = dialog.showAndWait();

        if(result.isPresent())
        {
            if(!mediaManager.createNewPlaylist(result.get()))
                dialog.setContentText("You've already made a playlist called that.");
        }
    }

    @FXML private void handleViewSwitchAction(ActionEvent event)
    {
        songTab.managedProperty().bind(songTab.visibleProperty());
        visuals.managedProperty().bind(visuals.visibleProperty());

        if(!isVisuals)
        {
            songTab.setVisible(false);
            visuals.setVisible(true);
            toVisuals.setText("Library");
            isVisuals = true;
        }
        else
        {
            songTab.setVisible(true);
            visuals.setVisible(false);
            toVisuals.setText("Visuals");
            isVisuals = false;
        }
    }

    @FXML protected void handleSearchAction(ActionEvent event) {
        String item = searchType.getSelectionModel().getSelectedItem();
        if(item != null && !item.isEmpty()) {
            BowerbirdDB.SearchType st = BowerbirdDB.SearchType.valueOf(item);
            List<MusicRecord> results = mediaManager.bowerbirdDB.search(fieldSearch.getText().toLowerCase(), st);
            if (results == null) {
                System.out.println("Not Found");
            }

            searchOutput.getChildren().clear();

            for (int i = 1; i < results.size() + 1; i++) {
                Button newButton = getSearchResultsButton(results.get(i - 1));
                newButton.getStyleClass().add("tab-button");

                searchOutput.getChildren().add(newButton);
            }
        }
    }
    
    public Button getSearchResultsButton(MusicRecord musicRecord)
    {
        Button newButton = new Button(musicRecord.getTitle());
        newButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(mediaManager.getMediaPlayer() != null)
                {
                    mediaManager.Stop();
                    mediaManager.getMediaPlayer().dispose();
                }
                mediaManager.ImportNewMedia(musicRecord.getFilePath());
                mediaManager.Play();
            }
        });

        return newButton;
    }
    //endregion Handlers
}
