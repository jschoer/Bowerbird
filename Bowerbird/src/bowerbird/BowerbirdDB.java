package bowerbird;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class BowerbirdDB
{
    private static String dbName = "music.db";
    private static String url = "jdbc:sqlite:F:/sqlite/db";

    public static void main(String[] args)
    {
        newDB(dbName);
        newTable();


    }

    private static Connection connect()
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

    public static void newDB(String filename)
    {
        url = System.getProperty("user.dir").replace("\\", "/") + filename;

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

    public static void newTable()
    {
        String sql = 	"CREATE TABLE IF NOT EXISTS music (\n" +
                        "ID integer PRIMARY KEY,\n" +
                        "Title text NOT NULL,\n" +
                        "Album text,\n" +
                        "Artist text\n" +
                        "FilePath text NOT NULL\n" +
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

    private static void insert()
    {
        String sql = "INSERT INTO music () VALUES(?, ?, ?)";

        try(Connection conn = BowerbirdDB.connect(); PreparedStatement ps = conn.prepareStatement(sql))
        {
            //ps.setString(1);
            //ps.setInt(2);
            //ps.setString(3);

            ps.executeUpdate();
        }
        catch(SQLException e)
        {
            System.out.println("insert error: " + e.getMessage());
        }
    }
}