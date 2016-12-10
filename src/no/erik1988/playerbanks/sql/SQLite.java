package no.erik1988.playerbanks.sql;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import no.erik1988.playerbanks.sql.Database; // import the database class.
import no.erik1988.playerbanks.Main; // import your main class


public class SQLite extends Database{
    private String dbname;
    public SQLite(Main instance){
        super(instance);
        dbname = "pbank"; // Set the table name here e.g player_kills
    }

    private String SQLiteCreateBanksTable = "CREATE TABLE IF NOT EXISTS pbank_banks (" + // make sure to put your table name in here too.
    		"`id` INTEGER," +
            "`nameofbank` varchar(32) NOT NULL," +
    		"`owner` varchar(36) NOT NULL," +
            "`manager` varchar(36)," +
            "`interestrate` int(1) NOT NULL," +
            "`inviteonly` int(1) NOT NULL DEFAULT 0," +
            "`value` int NOT NULL DEFAULT 0," +
            "`maxloan` int NOT NULL," +
            "`fee` int NOT NULL DEFAULT 10," +
            "`timestamp` INTEGER NOT NULL," +
            "PRIMARY KEY (`id`)" +  
            ");";
    private String SQLiteCreateLoansTable = "CREATE TABLE IF NOT EXISTS pbank_loans (" + 
    		"`id` INTEGER," +
            "`bankid` INTEGER NOT NULL," +
    		"`borrower` varchar(36) NOT NULL," +
            "`interestrate` int(1) NOT NULL," +
            "`interest` int NOT NULL DEFAULT 0," +
            "`borrowed` int NOT NULL DEFAULT 0," +
            "`payments` int NOT NULL DEFAULT 0," +
            "`active` int(1) NOT NULL DEFAULT 0," +
            "`fee` int NOT NULL DEFAULT 10," +
            "`requestdate` INTEGER NOT NULL," +
            "`activateddate` INTEGER DEFAULT 0," +
            "`downpayeddate` INTEGER DEFAULT 0," +
            "`approvedby` varchar(36)," +
            "PRIMARY KEY (`id`)" +  
            ");";
    private String SQLiteCreatePaymentsTable = "CREATE TABLE IF NOT EXISTS pbank_transactions (" + 
    		"`id` INTEGER," +
    		"`type` int(1) NOT NULL," +
    		"`contract` INTEGER DEFAULT 0," +
    		"`amount` int NOT NULL DEFAULT 0," +
    		"`playeruuid` varchar(36) NOT NULL," +
            "`bankid` INTEGER NOT NULL," +
            "`timestamp` INTEGER NOT NULL," +
    		"`seen` int(1) NOT NULL DEFAULT 0," +
            "PRIMARY KEY (`id`)" +  
            ");";
    private String SQLiteCreateMSGTable = "CREATE TABLE IF NOT EXISTS pbank_msg (" + 
    		"`id` INTEGER," +
    		"`playeruuid` varchar(36) NOT NULL," +
    		"`msg` TEXT NOT NULL," +
    		"`timestamp` INTEGER NOT NULL," +
    		"`seen` int(1) NOT NULL DEFAULT 0," +
            "PRIMARY KEY (`id`)" +  
            ");";
    private String SQLiteCreateLogTable = "CREATE TABLE IF NOT EXISTS pbank_log (" + 
    		"`id` INTEGER," +
    		"`bankid` INTEGER NOT NULL," +
    		"`log` TEXT NOT NULL," +
    		"`timestamp` INTEGER NOT NULL," +
            "PRIMARY KEY (`id`)" +  
            ");";
    // SQL creation stuff, You can leave the blow stuff untouched.
    public Connection getSQLConnection() {
        File dataFolder = new File(plugin.getDataFolder(), dbname+".db");
        if (!dataFolder.exists()){
            try {
                dataFolder.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "File write error: "+dbname+".db");
            }
        }
        try {
            if(connection!=null&&!connection.isClosed()){
                return connection;
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
            return connection;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE,"SQLite exception on initialize", ex);
        } catch (ClassNotFoundException ex) {
            plugin.getLogger().log(Level.SEVERE, "You need the SQLite JBDC library. Google it. Put it in /lib folder.");
        }
        return null;
    }
 
    public void load() {
        connection = getSQLConnection();     
        try {
            Statement s = connection.createStatement();
            s.executeUpdate(SQLiteCreateBanksTable);
            s.executeUpdate(SQLiteCreateLoansTable);
            s.executeUpdate(SQLiteCreatePaymentsTable);
            s.executeUpdate(SQLiteCreateMSGTable);
            s.executeUpdate(SQLiteCreateLogTable);
            s.executeUpdate("PRAGMA journal_mode=WAL");
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        initialize();
    }
}