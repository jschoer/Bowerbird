package bowerbird;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class BowerbirdDB
{
    private String dbName = "music.db";
    private String url = "jdbc:sqlite:";

    public BowerbirdDB()
    {
        newDB(dbName);
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

    //only used to see if database is present
    public void newDB(String filename)
    {
        url += System.getProperty("user.dir").replace("\\", "/") + "/resources/" + filename;

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
        String sql = "INSERT INTO music (ID, FilePath, Title, Artist, Album, Genre, Year) VALUES(?, ?, ?, ?, ?, ?, ?)";

        try(Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(2, musicRecord.get_filePath());
            ps.setString(3, musicRecord.get_title());
            ps.setString(4, musicRecord.get_artist());
            ps.setString(5, musicRecord.get_album());
            ps.setString(6, musicRecord.get_genre());
            ps.setString(7, musicRecord.get_year());

            ps.executeUpdate();

            System.out.println("Successful insert!");
        }
        catch(SQLException e)
        {
            System.out.println("insert error: " + e.getMessage());
        }
    }
}
