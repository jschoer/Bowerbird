package Bowerbird;

//class used for setting all information of media file to insert into database
public class MusicRecord
{
    //region PrivateVariables
    //metadata
    private String _title;
    private String _album;
    private String _artist;
    private String _genre;
    private String _year;
    private String _lyrics;
    //songdata
    private String _filePath;
    private int _songID;
    private int _trackNum;
    //endregion PrivateVariables

    //region GetSet
    public String getTitle() { return _title; }

    public void setTitle(String _title) {
        this._title = _title;
    }

    public String getAlbum() {
        return _album;
    }

    public void setAlbum(String _album) { this._album = _album; }

    public String getArtist() {
        return _artist;
    }

    public void setTrackNum(int _trackNum) { this._trackNum = _trackNum; }

    public int getTrackNum() { return _trackNum; }

    public void setArtist(String _artist) { this._artist = _artist; }

    public String getGenre() {
        return _genre;
    }

    public void setGenre(String _genre) {
        this._genre = _genre;
    }

    public String getYear() {
        return _year;
    }

    public void setYear(String _year) {
        this._year = _year;
    }

    public String getFilePath() {
        return _filePath;
    }

    public void setFilePath(String _filePath) {
        this._filePath = _filePath;
    }

    public int getSongID() { return _songID; }

    public void setSongID(int _songID) { this._songID = _songID; }

    public void setLyrics(String _lyrics) { this._lyrics = _lyrics; }

    public String getLyrics() { return _lyrics; }

    //endregion GetSet
}