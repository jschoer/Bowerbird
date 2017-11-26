package Bowerbird;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.IOException;
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

    private MediaManager mediaManager;

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
        mediaManager.UpdateLabel();
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

    @FXML private void handleViewSwitchAction(ActionEvent event) throws IOException
    {
        FXMLLoader loader;

        if(event.getSource() == toVisuals)
            loader = new FXMLLoader(getClass().getResource("VisualizationsView.fxml"));
        else
            loader = new FXMLLoader(getClass().getResource("Bowerbird.fxml"));

        loader.setRoot(this);
        loader.setController(this);
        loader.load();
    }

    @FXML protected void handleSearchAction(ActionEvent event) {
        BowerbirdDB.SearchType st = BowerbirdDB.SearchType.valueOf(searchType.getSelectionModel().getSelectedItem());

        if (mediaManager.bowerbirdDB.search(fieldSearch.getText().toLowerCase(), st) == null) {
            System.out.println("Not Found");
        }
        else
            mediaManager.bowerbirdDB.search(fieldSearch.getText().toLowerCase(), st);
    }

    //endregion Handlers
}
