package bowerbird;

import java.util.List;

public class AlbumRecord
{
    private List<MusicRecord> musicRecordList;

    public List<MusicRecord> getMusicRecordList() {
        return musicRecordList;
    }

    public void setMusicRecordList(List<MusicRecord> musicRecordList) {
        this.musicRecordList = musicRecordList;
    }
}
