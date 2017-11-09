package Bowerbird;
import java.util.List;

public class Playlist
{
    //region PrivateVariables
    //metadata
    private String _name;
    private List<MusicRecord> _songs;
    //endregion PrivateVariables

    public String get_playlistName() { return _name; }

    public void set_playlistName(String _name) {
        this._name = _name;
    }

    public List<MusicRecord> get_playlistContent() { return _songs; }

    public void set_playlistContent(List<MusicRecord> _songs) { this._songs = _songs; }

}
