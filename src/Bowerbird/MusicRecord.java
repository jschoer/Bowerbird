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
    //songdata
    private String _filePath;
    //endregion PrivateVariables

    //region GetSet
    public String get_title() {
        return _title;
    }

    public void set_title(String _title) {
        this._title = _title;
    }

    public String get_album() {
        return _album;
    }

    public void set_album(String _album) {
        this._album = _album;
    }

    public String get_artist() {
        return _artist;
    }

    public void set_artist(String _artist) {
        this._artist = _artist;
    }

    public String get_genre() {
        return _genre;
    }

    public void set_genre(String _genre) {
        this._genre = _genre;
    }

    public String get_year() {
        return _year;
    }

    public void set_year(String _year) {
        this._year = _year;
    }

    public String get_filePath() {
        return _filePath;
    }

    public void set_filePath(String _filePath) {
        this._filePath = _filePath;
    }

    //endregion GetSet
}