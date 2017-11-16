package Bowerbird;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

//class used for setting all information of media file to insert into database
public class MusicRecord
{
    //region PrivateVariables

    //metadata
    private final SimpleStringProperty _title;
    private final SimpleStringProperty _album;
    private final SimpleStringProperty _artist;
    private final SimpleStringProperty _genre;
    private final SimpleStringProperty _year;
    private final SimpleStringProperty _lyrics;
    //songdata
    private final SimpleStringProperty _filePath;
    private final SimpleIntegerProperty _songID;
    private final SimpleIntegerProperty _trackNum;

    //endregion PrivateVariables

    public MusicRecord()
    {
        this._title = new SimpleStringProperty();
        this._album = new SimpleStringProperty();
        this._artist = new SimpleStringProperty();
        this._genre = new SimpleStringProperty();
        this._year = new SimpleStringProperty();
        this._lyrics = new SimpleStringProperty();
        this._filePath = new SimpleStringProperty();
        this._songID = new SimpleIntegerProperty();
        this._trackNum = new SimpleIntegerProperty();
    }

    //region GetSet

    public String getTitle() { return _title.get(); }

    public void setTitle(String _title) {
        this._title.set(_title);
    }

    public String getAlbum() { return _album.get(); }

    public void setAlbum(String _album) { this._album.set(_album); }

    public String getArtist() { return _artist.get(); }

    public void setTrackNum(int _trackNum) { this._trackNum.set(_trackNum); }

    public int getTrackNum() { return _trackNum.get(); }

    public void setArtist(String _artist) { this._artist.set(_artist); }

    public String getGenre() {
        return _genre.get();
    }

    public void setGenre(String _genre) {
        this._genre.set(_genre);
    }

    public String getYear() { return _year.get(); }

    public void setYear(String _year) { this._year.set(_year); }

    public String getFilePath() {
        return _filePath.get();
    }

    public void setFilePath(String _filePath) { this._filePath.set(_filePath); }

    public int getSongID() { return _songID.get(); }

    public void setSongID(int _songID) { this._songID.set(_songID); }

    public String getLyrics() { return _lyrics.get(); }

    public void setLyrics(String _lyrics) { this._lyrics.set(_lyrics); }

    //endregion GetSet
}