package Bowerbird;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BowerbirdDB
{
    private String dbName = "music.db";
    private String url = "jdbc:sqlite:";

    private enum SearchType { TITLE, ARTIST, ALBUM, FILENAME, LYRICS }

    public BowerbirdDB()
    {
        newDB();
        newTable();
    }

    // region Standard functions

    private Connection connect()
    {
        Connection conn = null;

        try
        {
            conn = DriverManager.getConnection(url);
        }
        catch(SQLException e)
        {
            System.out.println("connect error: " + e.getMessage());
        }

        return conn;
    }

    public void newDB()
    {
        url += System.getProperty("user.dir").replace("\\", "/") + "/src/Bowerbird/resources/" + dbName;

        try
        {
            Connection conn = connect();

            if(conn != null)
            {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("New dB created; driver name: " + meta.getDriverName());
            }
        }
        catch(SQLException e)
        {
            System.out.println("newDB error: " + e.getMessage());
        }
    }

    public void newTable()
    {
        String music = "CREATE TABLE IF NOT EXISTS music (" +
                "ID integer PRIMARY KEY," +
                "FilePath text NOT NULL," +
                "Title text," +
                "Artist text," +
                "Album text," +
                "TrackNum integer," +
                "Genre text," +
                "Year text," +
                "Lyrics text" +
                ");";

        String playlists = "CREATE TABLE IF NOT EXISTS playlists (" +
                "Name text NOT NULL," +
                "SongID integer," +
                "Position integer," +
                "UNIQUE(Name)" +
                ");";

        try
        {
            Connection conn = connect();
            Statement stmt = conn.createStatement();

            stmt.execute(music);
            System.out.println("Table 'music' created.");

            stmt.execute(playlists);
            System.out.println("Table 'playlists' created.");
        }
        catch(SQLException e)
        {
            System.out.println("newTable error: " + e.getMessage());
        }
    }

    // endregion

    // region Playlist functions

    public boolean newPlaylist(String playlistName)
    {
        String sql = "INSERT INTO playlists (Name, SongID, Position)" +
                "SELECT ?, ?, ? " +
                "WHERE NOT EXISTS" +
                "(SELECT 1 FROM playlists WHERE Name = ?)";  //unique to a playlist so no dupes

        try(Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1, playlistName);
            ps.setInt(2, 0);
            ps.setInt(3, 0);
            ps.setString(4, playlistName);

            ps.executeUpdate();

            System.out.println("Playlist " + playlistName + " created.");
            return true;
        }
        catch(SQLException e)
        {
            System.out.println("new playlist error: " + e.getMessage());
        }

        return false;
    }

    public List<Playlist> getAllPlaylists()
    {
        String sql = "SELECT * FROM playlists WHERE SongID = 0 AND Position = 0";
        List<Playlist> allPlaylists = new ArrayList<>();

        try(Connection conn = connect(); Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY))
        {
            ResultSet rs = st.executeQuery(sql);

            while(rs.next())
            {
                Playlist plst = new Playlist();

                plst.set_playlistName(rs.getString("Name"));

                if(plst != null)
                    allPlaylists.add(plst);
            }
        }
        catch (SQLException e)
        {
            System.out.print("display all playlists error: " + e.getMessage());
        }

        return allPlaylists;
    }

    public List<MusicRecord> getPlaylistContent(String plstName)
    {
        String sql = "SELECT * FROM music " +
                "INNER JOIN playlists on playlists.SongID = music.ID " +
                "WHERE playlists.Name = ? ORDER BY playlists.Position ASC";
        List<MusicRecord> content = new ArrayList<>();

        try(Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1, plstName);
            ResultSet rs = ps.executeQuery();

            while(rs.next())
            {
                MusicRecord musicRecord = new MusicRecord();
                musicRecord.setFilePath(rs.getString("FilePath"));
                musicRecord.setTitle(rs.getString("Title"));
                musicRecord.setArtist(rs.getString("Artist"));
                musicRecord.setAlbum(rs.getString("Album"));
                musicRecord.setGenre(rs.getString("Genre"));
                musicRecord.setYear(rs.getString("Year"));
                musicRecord.setSongID(rs.getInt("ID"));
                musicRecord.setLyrics(rs.getString("Lyrics"));
                musicRecord.setTrackNum(rs.getInt("TrackNum"));

                content.add(musicRecord);
            }
        }
        catch(SQLException e)
        {
            System.out.println("get playlist " + plstName + " error: " + e.getMessage());
        }

        return content;
    }

    public void addToPlaylist(String playlistName, int song)
    {
        String insert = "INSERT INTO playlists (Name, SongID, Position)" +
                "VALUES (?, ?, ?)" ;

        String getName = "SELECT TOP 1 Name FROM playlists " +
                "WHERE Name = ? AND SongID = 0";

        String getLastInPlaylist = "SELECT MAX(Position) FROM playlists " +
                "WHERE Name = ?";

        try(Connection conn = connect())
        {
            PreparedStatement gn = conn.prepareStatement(getName);
            gn.setString(1, playlistName);
            ResultSet rs = gn.executeQuery();

            PreparedStatement gl = conn.prepareStatement(getLastInPlaylist);
            gl.setString(1, playlistName);
            ResultSet rs1 = gl.executeQuery();

            PreparedStatement in = conn.prepareStatement(insert);
            in.setString(1, rs.getString("Name"));
            in.setInt(2, song);
            in.setInt(3, rs1.getInt("Position") + 1);
        }
        catch(SQLException e)
        {
            System.out.println("add to playlist error: " + e.getMessage());
        }
    }

    public void removeFromPlaylist(String playlistName, int song, int songPos)
    {
        String delFromPlaylist = "DELETE FROM playlists " +
                "WHERE Name = ? AND Song = ?";

        String getLastInPlaylist = "SELECT MAX(Position) FROM playlists " +
                "WHERE Name = ?";

        String updateSuccessors = "UPDATE playlists SET Position = ? " +
                "WHERE Position = ? AND Name = ?";

        try(Connection conn = connect())
        {
            PreparedStatement dp = conn.prepareStatement(delFromPlaylist);
            dp.setString(1, playlistName);
            dp.setInt(2, song);
            dp.executeUpdate();

            PreparedStatement gl = conn.prepareStatement(getLastInPlaylist);
            gl.setString(1, playlistName);
            ResultSet rs = gl.executeQuery(getLastInPlaylist);

            for(int i = songPos; i < rs.getInt("Position"); i++)
            {
                PreparedStatement us = conn.prepareStatement(updateSuccessors);
                us.setInt(1, i);
                us.setInt(2, i+1);
                us.setString(3, playlistName);
                us.executeUpdate();
            }
        }
        catch(SQLException e)
        {
            System.out.println("remove from playlist error: " + e.getMessage());
        }
    }

    public void renamePlaylist(String oldName, String newName)
    {
        String sql = "UPDATE playlists " +
                "SET Name = ?" +
                "WHERE Name = ?";

        try(Connection conn = connect())
        {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, newName);
            ps.setString(2, oldName);
            ps.executeUpdate();
        }
        catch(SQLException e)
        {
            System.out.println("rename playlist error: " + e.getMessage());
        }
    }

    public void deletePlaylist(String playlistName)
    {
        String sql = "DELETE FROM playlists WHERE Name = ?";

        try(Connection conn = connect())
        {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, playlistName);
            ps.executeUpdate();
        }
        catch(SQLException e)
        {
            System.out.println("delete playlist error: " + e.getMessage());
        }
    }

    // endregion

    // region Library management functions

    public void importSong(MusicRecord musicRecord)
    {
        String sql = "INSERT INTO music (FilePath, Title, Artist, Album, TrackNum, Genre, Year) " +
                "SELECT ?, ?, ?, ?, ?, ?, ? " +
                "WHERE NOT EXISTS" +
                "(SELECT 1 FROM music WHERE Title = ? AND Artist = ? AND Album = ?)";  //unique to a song so no dupes

        try(Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql))
        {
            //Setting the values
            ps.setString(1, musicRecord.getFilePath());
            ps.setString(2, musicRecord.getTitle());
            ps.setString(3, musicRecord.getArtist());
            ps.setString(4, musicRecord.getAlbum());
            ps.setInt(5, musicRecord.getTrackNum());
            ps.setString(6, musicRecord.getGenre());
            ps.setString(7, musicRecord.getYear());

            //Setting the duplicate values
            ps.setString(8, musicRecord.getTitle());
            ps.setString(9, musicRecord.getArtist());
            ps.setString(10, musicRecord.getAlbum());

            ps.executeUpdate();

            System.out.println("Successful song insert!");
        }
        catch(SQLException e)
        {
            System.out.println("insert song error: " + e.getMessage());
        }
    }

    public void editSong(int modifiedID)
    {
        String sql = "UPDATE music ";
        String updateClause = "";


    }

    public void removeSong(int deletedID)
    {
        String sql = "DELETE FROM music WHERE ID = ?";

        try(Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setInt(1, deletedID);
            ps.executeUpdate();

            System.out.println("Successful song delete!");
        }
        catch(SQLException e)
        {
            System.out.println("delete song error: " + e.getMessage());
        }
    }

    public List<MusicRecord> getAllMusicRecords()
    {
        String sql = "SELECT * FROM music ORDER BY Title DESC";
        List<MusicRecord> musicRecords = new ArrayList<>();

        try(Connection conn = connect(); Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY))
        {
            ResultSet rs = st.executeQuery(sql);

            while(rs.next())
            {
                MusicRecord musicRecord = new MusicRecord();
                musicRecord.setFilePath(rs.getString("FilePath"));
                musicRecord.setTitle(rs.getString("Title"));
                musicRecord.setArtist(rs.getString("Artist"));
                musicRecord.setAlbum(rs.getString("Album"));
                musicRecord.setGenre(rs.getString("Genre"));
                musicRecord.setYear(rs.getString("Year"));
                musicRecord.setSongID(rs.getInt("ID"));
                musicRecord.setLyrics(rs.getString("Lyrics"));
                musicRecord.setTrackNum(rs.getInt("TrackNum"));

                if(musicRecord != null)
                {
                    musicRecords.add(musicRecord);
                }
            }
        }
        catch (SQLException e)
        {
            System.out.print("display all music records error: " + e.getMessage());
        }

        return musicRecords;
    }

    public List<MusicRecord> search(String term, SearchType st)
    {
        String sql = "SELECT * FROM music ";
        List<MusicRecord> searchResults = new ArrayList<>();

        switch(st)
        {
            case TITLE:
                sql += "WHERE Title LIKE '%?%'";
                break;
            case ARTIST:
                sql += "WHERE Artist LIKE '%?%'";
                break;
            case ALBUM:
                sql += "WHERE Album LIKE '%?%'";
                break;
            case FILENAME:
                sql += "WHERE FilePath LIKE '%?%'";
                break;
            case LYRICS:
                sql += "WHERE Lyrics LIKE '%?%'";
                break;
            default:
                System.out.print("Invalid search type");
                return searchResults;
        }

        try(Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql))
        {
            ResultSet rs = ps.executeQuery(sql);

            while(rs.next())
            {
                MusicRecord musicRecord = new MusicRecord();
                musicRecord.setFilePath(rs.getString("FilePath"));
                musicRecord.setTitle(rs.getString("Title"));
                musicRecord.setArtist(rs.getString("Artist"));
                musicRecord.setAlbum(rs.getString("Album"));
                musicRecord.setGenre(rs.getString("Genre"));
                musicRecord.setYear(rs.getString("Year"));
                musicRecord.setSongID(rs.getInt("ID"));
                musicRecord.setLyrics(rs.getString("Lyrics"));

                if(musicRecord != null)
                {
                    searchResults.add(musicRecord);
                }
            }
        }
        catch (SQLException e)
        {
            System.out.print("search " + st.toString() + " error: " + e.getMessage());
        }

        return searchResults;
    }

    // endregion
}