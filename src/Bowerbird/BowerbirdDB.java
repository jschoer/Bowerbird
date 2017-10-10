package Bowerbird;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BowerbirdDB
{
    private String dbName = "music.db";
    private String url = "jdbc:sqlite:";

    public BowerbirdDB()
    {
        newDB();
        newTable();
    }

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
        String sql = "CREATE TABLE IF NOT EXISTS music (" +
                "ID integer PRIMARY KEY," +
                "FilePath text NOT NULL," +
                "Title text," +
                "Artist text," +
                "Album text," +
                "Genre text," +
                "Year text" +
                ");";

        try
        {
            Connection conn = connect();
            Statement stmt = conn.createStatement();

            stmt.execute(sql);
            System.out.println("Table 'music' created.");
        }
        catch(SQLException e)
        {
            System.out.println("newTable error: " + e.getMessage());
        }
    }

    public void insert(MusicRecord musicRecord)
    {
        String sql = "INSERT INTO music (FilePath, Title, Artist, Album, Genre, Year) " +
                "SELECT ?, ?, ?, ?, ?, ? " +
                "WHERE NOT EXISTS" +
                "(SELECT 1 FROM music WHERE Title = ? AND Artist = ? AND Album = ?)";  //unique to a song so no dupes

        try(Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql))
        {
            //Setting the values
            ps.setString(1, musicRecord.get_filePath());
            ps.setString(2, musicRecord.get_title());
            ps.setString(3, musicRecord.get_artist());
            ps.setString(4, musicRecord.get_album());
            ps.setString(5, musicRecord.get_genre());
            ps.setString(6, musicRecord.get_year());

            //Setting the duplicate values
            ps.setString(7, musicRecord.get_title());
            ps.setString(8, musicRecord.get_artist());
            ps.setString(9, musicRecord.get_album());

            ps.executeUpdate();

            System.out.println("Successful insert!");
        }
        catch(SQLException e)
        {
            System.out.println("insert error: " + e.getMessage());
        }
    }

    public List<MusicRecord> getAllMusicRecords()
    {
        String sql = "SELECT * FROM music";
        List<MusicRecord> musicRecords = new ArrayList<>();

        try(Connection conn = connect(); Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY))
        {
            ResultSet rs = st.executeQuery(sql);

            while(rs.next())
            {
                MusicRecord musicRecord = new MusicRecord();
                musicRecord.set_filePath(rs.getString("FilePath"));
                musicRecord.set_title(rs.getString("Title"));
                musicRecord.set_artist(rs.getString("Artist"));
                musicRecord.set_album(rs.getString("Album"));
                musicRecord.set_genre(rs.getString("Genre"));
                musicRecord.set_year(rs.getString("Year"));

                if(musicRecord != null)
                {
                    musicRecords.add(musicRecord);
                }
            }
        }
        catch (SQLException e)
        {
            System.out.print(e.getMessage());
        }

        return musicRecords;
    }
}