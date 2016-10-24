package no.erik1988.playerbanks.sql;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import no.erik1988.playerbanks.sql.Error; 
import no.erik1988.playerbanks.sql.Errors;
import no.erik1988.playerbanks.Main; 
import no.erik1988.playerbanks.objects.LoanObject;

public abstract class Database {
	Main plugin;
    Connection connection;
    // The name of the table we created back in SQLite class.
    public String banks = "pbank_banks";
    public String loans = "pbank_loans";
    public String transactions = "pbank_transactions";
    public String msgtable = "pbank_msg";
    //private ExecutorService executor = Executors.newSingleThreadExecutor();
    public Database(Main instance){
        plugin = instance;
    }

    public abstract Connection getSQLConnection();

    public abstract void load();

    public void initialize(){
        connection = getSQLConnection();
        try{
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + banks + " WHERE id = ?");
            ResultSet rs = ps.executeQuery();
            close(ps,rs);
         
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
        }
    }

    // These are the methods you can use to get things out of your database. You of course can make new ones to return different things in the database.
    public void MyBanks(Player player) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        FileConfiguration c = plugin.getConfig();
        String myuuid = player.getUniqueId().toString(); 
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + banks + " WHERE owner = '"+myuuid+"' ");
         
            rs = ps.executeQuery();
            while(rs.next()){
                if(rs.getString("owner") != null){
                	String bankname = rs.getString("nameofbank");
                	int interestrate = rs.getInt("interestrate");
                	int value = rs.getInt("value");
                	//int receivables = rs.getDouble("receivables");
                	int maxloan = rs.getInt("maxloan");
                	int fee = rs.getInt("fee");
                	String valueasstirng = Integer.toString(value);
                	//String receivablesasstirng = Double.toString(receivables);
                	String maxloanasstirng = Integer.toString(maxloan);
                	String interestrateAsString = "0%";
                	if(interestrate == 1){
                		interestrateAsString = c.getString("interest.low")+ "%";
                	}
                	else if(interestrate == 2){
                		interestrateAsString = c.getString("interest.med")+ "%";
                	}
                	else if(interestrate == 3){
                		interestrateAsString = c.getString("interest.high")+ "%";
                	}
                	player.sendMessage(plugin.getMessager().get("Mybank.Bank.List").replace("%bank%", bankname).replace("%value%", valueasstirng).replace("%maxloan%", maxloanasstirng).replace("%interest%", interestrateAsString).replace("%fee%", Integer.toString(fee)));
                	
                }else {
               	 player.sendMessage(plugin.getMessager().get("Mybank.Bank.Null")); 
               }
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }     
    }
    public void BankList(Player player) {
    	Connection conn = null;
    	PreparedStatement ps = null;
    	ResultSet rs = null;
    	FileConfiguration c = plugin.getConfig();
    	int count = 0;
    	try {
    		
    		conn = getSQLConnection();
    		ps = conn.prepareStatement("SELECT nameofbank,owner,value,fee,interestrate FROM " + banks + " ORDER BY value DESC LIMIT 18");
    		rs = ps.executeQuery();

    		while(rs.next()){
    			count++;
    			String bankname = rs.getString("nameofbank");
    			String uuid = rs.getString("owner");
    			UUID uuid2 = UUID.fromString(uuid);
    			String owner = Bukkit.getOfflinePlayer(uuid2).getName();
    			int value = rs.getInt("value");
    			int fee = rs.getInt("fee");
    			int interestrate = rs.getInt("interestrate");
    			String interestrateAsString = "0%";
    			if(interestrate == 1){
    				interestrateAsString = c.getString("interest.low")+ "%";
    			}
    			else if(interestrate == 2){
    				interestrateAsString = c.getString("interest.med")+ "%";
    			}
    			else if(interestrate == 3){
    				interestrateAsString = c.getString("interest.high")+ "%";
    			}

    			player.sendMessage(plugin.getMessager().get("Banks.Bank.List").replace("%owner%", owner).replace("%bank%", bankname).replace("%value%", Integer.toString(value)).replace("%fee%", Integer.toString(fee)).replace("%interest%", interestrateAsString));
    		}
    		if (count < 1){
    			player.sendMessage(plugin.getMessager().get("Banks.Bank.Null")); 

    		}
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }     
    }
    public void BankInfo(Player player, String Bank) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        FileConfiguration c = plugin.getConfig();
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + banks + " WHERE nameofbank = '"+Bank+"'");
         
            rs = ps.executeQuery();
            while(rs.next()){
                if(rs.getString("owner") != null){
                	String bankname = rs.getString("nameofbank");
                	String uuid = rs.getString("owner");
                	UUID uuid2 = UUID.fromString(uuid);
                	String owner = Bukkit.getOfflinePlayer(uuid2).getName();
                	int interestrate = rs.getInt("interestrate");
                	int value = rs.getInt("value");
                	int receivables = rs.getInt("receivables");
                	int maxloan = rs.getInt("maxloan");
                	int fee = rs.getInt("fee");
                	String receivablesasstirng = Integer.toString(receivables);
                	String interestrateAsString = "0%";
                	if(interestrate == 1){
                		interestrateAsString = c.getString("interest.low")+ "%";
                	}
                	else if(interestrate == 2){
                		interestrateAsString = c.getString("interest.med")+ "%";
                	} 
                	else if(interestrate == 3){
                		interestrateAsString = c.getString("interest.high")+ "%";
                	}
                	//player.sendMessage(plugin.getMessager().get("Banks.Bank.List").replace("%owner%", owner).replace("%bank%", bankname).replace("%value%", valueasstirng).replace("%receivables%", receivablesasstirng).replace("%maxloan%", maxloanasstirng).replace("%interest%", interestrateAsString));
                	player.sendMessage(plugin.getMessager().get("Banks.Info.Name").replace("%owner%", owner).replace("%bank%", bankname));
                	player.sendMessage(plugin.getMessager().get("Banks.Info.Value").replace("%value%", Integer.toString(value)).replace("%receivables%", receivablesasstirng));
                	player.sendMessage(plugin.getMessager().get("Banks.Info.Interest").replace("%maxloan%", Integer.toString(maxloan)).replace("%interest%", interestrateAsString));
                	player.sendMessage(plugin.getMessager().get("Banks.Info.Fee").replace("%fee%", Integer.toString(fee)));
                }else {
               	 player.sendMessage(plugin.getMessager().get("Banks.Bank.Null")); 
               }
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }     
    }
    public void MakeBank(Player player, String name, int interestrate, int maxloan, int fee) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();

            //String timestamp = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(System.currentTimeMillis()); 
            long timestamp = System.currentTimeMillis();

            
            ps = conn.prepareStatement("REPLACE INTO " + banks + " (id,nameofbank,owner,managers,interestrate,inviteonly,value,maxloan,fee,timestamp) VALUES(?,?,?,?,?,?,?,?,?,?)"); 
            ps.setString(2, name); 
            ps.setString(3, player.getUniqueId().toString());                                            
                                                                                 
            ps.setInt(5, interestrate);
            //ps.setInt(6, inviteonly);
            //ps.setInt(7, value);
            ps.setInt(8, maxloan);
            ps.setInt(9, fee);
            ps.setLong (10, timestamp);
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return;             
    }
    public void MakeLog(int type, int contract,int amount,OfflinePlayer borrowerplayer, int bankid, int seen) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();

            //String timestamp = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(System.currentTimeMillis()); 
            long timestamp = System.currentTimeMillis();

            
            ps = conn.prepareStatement("REPLACE INTO " + transactions + " (id,type,contract,amount,playeruuid,bankid,timestamp,seen) VALUES(?,?,?,?,?,?,?,?)"); 
            ps.setInt(2, type); 
            ps.setInt(3, contract); 
            ps.setInt(4, amount);
            ps.setString(5, borrowerplayer.getUniqueId().toString());                                            
                                                                                 
            ps.setInt(6, bankid);
            ps.setLong (7, timestamp);
            ps.setInt(8, seen);
            ps.executeUpdate();

        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return;             
    }
    public void AddMSG(OfflinePlayer player, String msg, UUID uuid) {
        Connection conn = null;
        PreparedStatement ps = null;
        String uuidstring = uuid.toString();
        try {
            conn = getSQLConnection();

            //String timestamp = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(System.currentTimeMillis()); 
            long timestamp = System.currentTimeMillis();

            
            ps = conn.prepareStatement("REPLACE INTO " + msgtable + " (id,playeruuid,msg,timestamp,seen) VALUES(?,?,?,?,?)"); 
            ps.setString(2, uuidstring); 
            ps.setString(3, msg); 
            ps.setLong (4, timestamp);
            ps.executeUpdate();

        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return;             
    }
    public void ReqLoan(Player player, String Bank, int interestrate, int Amount, int fee) {
        Connection conn = null;
        PreparedStatement ps = null;
        Integer BankID = plugin.sql.GetBankID(Bank);
        try {
            conn = getSQLConnection();
            
            //String timestamp = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(System.currentTimeMillis());
            long timestamp = System.currentTimeMillis();
            
            ps = conn.prepareStatement("REPLACE INTO " + loans + " (id,bankid,borrower,interestrate,interest,borrowed,payments,active,fee,requestdate,activateddate,downpayeddate,approvedby) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)");  
            ps.setInt(2, BankID);  
            ps.setString(3, player.getUniqueId().toString());                                            
                                                                                 
            ps.setInt(4, interestrate);
            ps.setInt(6, Amount);
            ps.setInt(9, fee);
            ps.setLong (10, timestamp);
            ps.executeUpdate();

        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return;             
    }
    public void ListContracts(Player player) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        FileConfiguration c = plugin.getConfig();
        String myuuid = player.getUniqueId().toString();
        int count = 0;
        try {
            conn = getSQLConnection();
            
            //String timestamp = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(System.currentTimeMillis());
            //long timestamp = System.currentTimeMillis();
            
            ps = conn.prepareStatement("Select (loans.interest + loans.borrowed - loans.payments) AS total, loans.id, banks.nameofbank, banks.owner, loans.borrower, loans.borrowed, loans.interestrate, loans.active, loans.requestdate, loans.fee " +
			"From pbank_banks AS banks " +
			"LEFT JOIN pbank_loans AS loans " +
			"ON banks.id = loans.bankid " +
			"where banks.owner = '" + myuuid + "' AND loans.borrower IS NOT NULL AND loans.active IS NOT 3 " + 
			"ORDER BY loans.requestdate DESC");
            rs = ps.executeQuery();
            while(rs.next()){
            	count++;
            		String bankname = rs.getString("nameofbank");
                	int interestrate = rs.getInt("interestrate");
                	int code = rs.getInt("id");
                	int status = rs.getInt("active");
                	int fee = rs.getInt("fee");
                	long timestamp2 = rs.getInt("requestdate");
                	String time = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(timestamp2);
                	String buuid = rs.getString("borrower");
                	UUID buuid2 = UUID.fromString(buuid);
                	String borrower = Bukkit.getOfflinePlayer(buuid2).getName();
                	int amount = rs.getInt("total");
                	amount = Math.round(amount * 100);
                	amount = amount/100;
                	String status2 = c.getString("Status.Pending","§ePending");
                	if (status == 1){
                		status2 = c.getString("Status.Active","§2Active");
                		amount = amount + fee;
                	}
                	String interest = "0%";
                	if(interestrate == 1){
                		interest = c.getString("interest.low")+ "%";
                	}
                	else if(interestrate == 2){
                		interest = c.getString("interest.med")+ "%";
                	}
                	else if(interestrate == 3){
                		interest = c.getString("interest.high")+ "%";
                	}
                	player.sendMessage(plugin.getMessager().get("Mybank.Contracts.List").replace("%code%", Integer.toString(code)).replace("%bank%", bankname).replace("%req%", Integer.toString(amount)).replace("%interest%",interest).replace("%player%", borrower).replace("%time%", time).replace("%status%", status2));
            	
            }
    		if (count < 1){
    			player.sendMessage(plugin.getMessager().get("Mybank.Contracts.Null")); 

    		}
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return;             
    }
    public void ListReports(Player player) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String myuuid = player.getUniqueId().toString();
        int count = 0;
        try {
            conn = getSQLConnection();
            
            //String timestamp = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(System.currentTimeMillis());
            //long timestamp = System.currentTimeMillis();
            
            ps = conn.prepareStatement("Select loans.id, loans.borrower, loans.downpayeddate, banks.nameofbank " +
			"From pbank_banks AS banks " +
			"LEFT JOIN pbank_loans AS loans " +
			"ON banks.id = loans.bankid " +
			"where banks.owner = '" + myuuid + "' AND loans.borrower IS NOT NULL AND loans.active IS 3 " + 
			"ORDER BY loans.downpayeddate DESC");
            rs = ps.executeQuery();
            while(rs.next()){
            	count++;
            		String bankname = rs.getString("nameofbank");
            		int loanid = rs.getInt("id");
                	long timestamp2 = rs.getInt("downpayeddate");
                	String date = new SimpleDateFormat("dd.MM.yy").format(timestamp2);
                	String buuid = rs.getString("borrower");
                	UUID buuid2 = UUID.fromString(buuid);
                	String borrower = Bukkit.getOfflinePlayer(buuid2).getName();

                	
                	player.sendMessage(plugin.getMessager().get("Mybank.ReportList.List").replace("%loanid%", Integer.toString(loanid)).replace("%bank%", bankname).replace("%borrower%", borrower).replace("%date%", date));
            	
            }
    		if (count < 1){
    			player.sendMessage(plugin.getMessager().get("Mybank.ReportList.Null")); 

    		}
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return;             
    }
    public boolean ListMSG(Player player) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String myuuid = player.getUniqueId().toString();
        Boolean out = false; 
        try {
            conn = getSQLConnection();
            
            //String timestamp = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(System.currentTimeMillis());
            //long timestamp = System.currentTimeMillis();
            
            ps = conn.prepareStatement("Select msg " +
			"From pbank_msg " +
			"where playeruuid = '" + myuuid + "' AND seen = 0 " + 
			"ORDER BY timestamp DESC "
			+ "LIMIT 18");
            rs = ps.executeQuery();
            int count = 0;
            while(rs.next()){
            		out = true;
            		String msg = rs.getString("msg");
                	player.sendMessage(msg);
                	count++;
            } if(count>17){
            	player.sendMessage("...List only shows the latest 18.");
            }

        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return out;             
    }
    public boolean ListMSGAll(Player player) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String myuuid = player.getUniqueId().toString();
        Boolean out = false; 
        try {
            conn = getSQLConnection();
            
            //String timestamp = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(System.currentTimeMillis());
            //long timestamp = System.currentTimeMillis();
            
            ps = conn.prepareStatement("Select msg " +
			"From pbank_msg " +
			"where playeruuid = '" + myuuid + "' " + 
			"ORDER BY timestamp DESC");
            rs = ps.executeQuery();
            int count = 0;
            while(rs.next()){
            		out = true;
            		String msg = rs.getString("msg");
                	player.sendMessage(msg);
                	count++;
            } if(count>17){
            	player.sendMessage("...List only shows the latest 18.");
            }

        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return out;             
    }
    public boolean ListTransAll(Player player) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String myuuid = player.getUniqueId().toString();
        Boolean out = false; 
        try {
            conn = getSQLConnection();
            
            //String timestamp = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(System.currentTimeMillis());
            //long timestamp = System.currentTimeMillis();
            
            ps = conn.prepareStatement("Select trans.type, trans.contract, trans.amount, trans.bankid, trans.timestamp, trans.playeruuid, banks.nameofbank " +
			"From pbank_transactions AS trans " +
			"LEFT JOIN pbank_banks AS banks " +
			"ON banks.id = bankid " +
			"where trans.playeruuid = '" + myuuid + "' " + 
			"ORDER BY trans.timestamp DESC "
			+ "LIMIT 18");
            rs = ps.executeQuery();
            int count = 0;
            while(rs.next()){
            		out = true;
            		int type = rs.getInt("type");
            		//int contract = rs.getInt("contract");
            		int amount = rs.getInt("amount");
            		//int bankid = rs.getInt("bankid");
            		long timestamp = rs.getLong("timestamp");
            		String nameofbank = rs.getString("nameofbank");
            		String time = new SimpleDateFormat("dd.MM.yy").format(timestamp);
            		UUID uuid = UUID.fromString(rs.getString("playeruuid"));
            		OfflinePlayer offlineplayer = Bukkit.getPlayer(uuid);
            		String playername = offlineplayer.getName().toString();
            		String msg = "";
            		if(type == 0){
            		msg = this.plugin.getMessager().get("transactions.deposit").replace("%time%",time).replace("%player%",playername).replace("%amount%",Integer.toString(amount)).replace("%bank%",nameofbank);
            		}
            		else if(type == 1){
            		msg = this.plugin.getMessager().get("transactions.withdraw").replace("%time%",time).replace("%player%",playername).replace("%amount%",Integer.toString(amount)).replace("%bank%",nameofbank);
            		}
            		else if(type == 2){
            		msg = this.plugin.getMessager().get("transactions.borrow").replace("%time%",time).replace("%player%",playername).replace("%amount%",Integer.toString(amount)).replace("%bank%",nameofbank);
            		}
            		else if(type == 3){
            		msg = this.plugin.getMessager().get("transactions.payment").replace("%time%",time).replace("%player%",playername).replace("%amount%",Integer.toString(amount)).replace("%bank%",nameofbank);
            		}
            		else if(type == 4){
            		msg = this.plugin.getMessager().get("transactions.interest").replace("%time%",time).replace("%player%",playername).replace("%amount%",Integer.toString(amount)).replace("%bank%",nameofbank);
            		}
            		else if(type == 5){
            		msg = this.plugin.getMessager().get("transactions.autopay").replace("%time%",time).replace("%player%",playername).replace("%amount%",Integer.toString(amount)).replace("%bank%",nameofbank);
            		}
                	player.sendMessage(msg);
                	count++;
            } if(count>17){
            	player.sendMessage("...List only shows the latest 18.");
            }


        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return out;             
    }
    public boolean ListTransBank(Player player,Integer Bankid) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        //String myuuid = player.getUniqueId().toString();
        Boolean out = false; 
        try {
            conn = getSQLConnection();
            
            //String timestamp = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(System.currentTimeMillis());
            //long timestamp = System.currentTimeMillis();
            
            ps = conn.prepareStatement("Select trans.type, trans.contract, trans.amount, trans.bankid, trans.timestamp, trans.playeruuid, banks.nameofbank " +
			"From pbank_transactions AS trans " +
			"LEFT JOIN pbank_banks AS banks " +
			"ON banks.id = bankid " +
			"where trans.bankid = '" + Bankid + "' " + 
			"ORDER BY trans.timestamp DESC "
			+ "LIMIT 18");
            rs = ps.executeQuery();
            int count = 0;
            while(rs.next()){
            		out = true;
            		int type = rs.getInt("type");
            		//int contract = rs.getInt("contract");
            		int amount = rs.getInt("amount");
            		//int bankid = rs.getInt("bankid");
            		long timestamp = rs.getLong("timestamp");
            		String nameofbank = rs.getString("nameofbank");
            		String time = new SimpleDateFormat("dd.MM.yy").format(timestamp);
            		UUID uuid = UUID.fromString(rs.getString("playeruuid"));
            		OfflinePlayer offlineplayer = Bukkit.getPlayer(uuid);
            		String playername = offlineplayer.getName().toString();
            		String msg = "";
            		if(type == 0){
            		msg = this.plugin.getMessager().get("transactions.deposit").replace("%time%",time).replace("%player%",playername).replace("%amount%",Integer.toString(amount)).replace("%bank%",nameofbank);
            		}
            		else if(type == 1){
            		msg = this.plugin.getMessager().get("transactions.withdraw").replace("%time%",time).replace("%player%",playername).replace("%amount%",Integer.toString(amount)).replace("%bank%",nameofbank);
            		}
            		else if(type == 2){
            		msg = this.plugin.getMessager().get("transactions.borrow").replace("%time%",time).replace("%player%",playername).replace("%amount%",Integer.toString(amount)).replace("%bank%",nameofbank);
            		}
            		else if(type == 3){
            		msg = this.plugin.getMessager().get("transactions.payment").replace("%time%",time).replace("%player%",playername).replace("%amount%",Integer.toString(amount)).replace("%bank%",nameofbank);
            		}
            		else if(type == 4){
            		msg = this.plugin.getMessager().get("transactions.interest").replace("%time%",time).replace("%player%",playername).replace("%amount%",Integer.toString(amount)).replace("%bank%",nameofbank);
            		}
            		else if(type == 5){
            		msg = this.plugin.getMessager().get("transactions.autopay").replace("%time%",time).replace("%player%",playername).replace("%amount%",Integer.toString(amount)).replace("%bank%",nameofbank);
            		}
                	player.sendMessage(msg);
                	count++;
            } if(count>17){
            	player.sendMessage("...List only shows the latest 18.");
            }

        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return out;             
    }
    public boolean CheckIfNewTrans(Player player) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String myuuid = player.getUniqueId().toString();
        Boolean out = true; 
        try {
            conn = getSQLConnection();
            
            ps = conn.prepareStatement("Select trans.type " +
			"From pbank_transactions AS trans " +
			"where trans.playeruuid = '" + myuuid + "' And seen = 0 ");
            rs = ps.executeQuery();
            if(!rs.next()){
            	out = false;
            }

        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return out;             
    }
    public void MarkMSGAsSeen(Player player) {
        Connection conn = null;
        PreparedStatement ps = null;
        String myuuid = player.getUniqueId().toString();
        try {
            conn = getSQLConnection();
            
            //String timestamp = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(System.currentTimeMillis());
            //long timestamp = System.currentTimeMillis();
            
            ps = conn.prepareStatement("UPDATE pbank_msg SET seen = 1 " +
			"where playeruuid = '" + myuuid + "' AND seen = 0 ");
            ps.executeUpdate();
            


        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return;             
    }
    public void MarkTransAsSeen(Player player) {
        Connection conn = null;
        PreparedStatement ps = null;
        String myuuid = player.getUniqueId().toString();
        try {
            conn = getSQLConnection();
            
            //String timestamp = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(System.currentTimeMillis());
            //long timestamp = System.currentTimeMillis();
            
            ps = conn.prepareStatement("UPDATE pbank_transactions SET seen = 1 " +
			"where playeruuid = '" + myuuid + "' AND seen = 0 ");
            ps.executeUpdate();
            


        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return;             
    }
   
    public void ListRequests(Player player) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        FileConfiguration c = plugin.getConfig();
        String myuuid = player.getUniqueId().toString();
        try {
            conn = getSQLConnection();
            
            //String timestamp = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(System.currentTimeMillis());
            //long timestamp = System.currentTimeMillis();
            
            ps = conn.prepareStatement("Select (loans.interest + loans.borrowed + loans.fee - loans.payments) AS total, loans.id, banks.nameofbank, banks.owner, loans.borrower, loans.borrowed, loans.interestrate, loans.active, loans.requestdate " +
			"From pbank_banks AS banks " +
			"LEFT JOIN pbank_loans AS loans " +
			"ON banks.id = loans.bankid " +
			"where loans.borrower = '" + myuuid + "' AND loans.active IS NOT 3 " +
			"ORDER BY loans.requestdate DESC");
            rs = ps.executeQuery();
            int count = 0;
            while(rs.next()){
            	count++;
            	String bankname = rs.getString("nameofbank");
            	int interestrate = rs.getInt("interestrate");
            	int code = rs.getInt("id");
            	int status = rs.getInt("active");
            	long timestamp2 = rs.getInt("requestdate");
            	String time = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(timestamp2);
            	String ouuid = rs.getString("owner");
            	UUID ouuid2 = UUID.fromString(ouuid);
            	String owner = Bukkit.getOfflinePlayer(ouuid2).getName();
            	int amount = rs.getInt("total");
            	amount = Math.round(amount * 100);
            	amount = amount/100;
            	String amountasstirng = Integer.toString(amount);
            	String status2 = "§ePending";
            	if (status == 1){
            		status2 = "§2Active";
            	}
            	String interest = "0%";
            	if(interestrate == 1){
            		interest = c.getString("interest.low")+ "%";
            	}
            	else if(interestrate == 2){
            		interest = c.getString("interest.med")+ "%";
            	}
            	else if(interestrate == 3){
            		interest = c.getString("interest.high")+ "%";
            	}
            	player.sendMessage(plugin.getMessager().get("borrow.Loans.List").replace("%code%", Integer.toString(code)).replace("%bank%", bankname).replace("%req%", amountasstirng).replace("%interest%",interest).replace("%owner%", owner).replace("%time%", time).replace("%status%", status2));
            }
    		if (count < 1){
    			player.sendMessage(plugin.getMessager().get("borrow.Loans.Null")); 

    		}
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return;             
    }
    public void Deposit(OfflinePlayer player, int bankid, int ammount) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();

            ps = conn.prepareStatement("UPDATE " + banks + " SET value = value + " + ammount + " WHERE id = '"+ bankid + "'" );
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return;             
    }
    public void Downpay(int loanid, int ammount) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();

            ps = conn.prepareStatement("UPDATE " + loans + " SET payments = payments + " + ammount + " WHERE id = '"+ loanid + "'" );
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return;             
    }
    public void UpdateInterest(int loanid, int ammount) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();

            ps = conn.prepareStatement("UPDATE " + loans + " SET interest = ROUND(" + ammount + " + interest,2) WHERE id = '"+ loanid + "'" );
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return;             
    }
    public void DeleteLoan(int loanid) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();

            ps = conn.prepareStatement("DELETE FROM " + loans + " WHERE id = '"+ loanid + "'" );
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return;             
    }
    public void RemBank(int bankid) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();

            ps = conn.prepareStatement("DELETE FROM " + banks + " WHERE id = '"+ bankid + "'" );
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return;             
    }
    public void RemAllLoansFromBank(int bankid) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();

            ps = conn.prepareStatement("DELETE FROM " + loans + " WHERE bankid = '"+ bankid + "'" );
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return;             
    }
    public void CleanUpMSG() {
        Connection conn = null;
        PreparedStatement ps = null;
        long timestamp = System.currentTimeMillis();
        FileConfiguration c = plugin.getConfig();
        int hours = c.getInt("cleanup.msg.olderthan.hours",24);
        try {
            conn = getSQLConnection();

            ps = conn.prepareStatement("DELETE FROM " + msgtable + " WHERE seen = 1 AND timestamp < ('" + timestamp + "' - ("+hours+" * 60 * 60 * 1000))");
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return;             
    }
    public void CleanUpTransactions() {
        Connection conn = null;
        PreparedStatement ps = null;
        FileConfiguration c = plugin.getConfig();
        long timestamp = System.currentTimeMillis();
        int hours = c.getInt("cleanup.transactions.olderthan.hours",168);
        try {
            conn = getSQLConnection();

            ps = conn.prepareStatement("DELETE FROM " + transactions + " WHERE seen = 1 AND timestamp < ('" + timestamp + "' - ("+hours+" * 60 * 60 * 1000))");
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return;             
    }
    public void MarkContractActive(Player player, int code) {
        Connection conn = null;
        PreparedStatement ps = null;
        long timestamp = System.currentTimeMillis();
        String uuid = player.getUniqueId().toString();
        try {
            conn = getSQLConnection();

            ps = conn.prepareStatement("UPDATE " + loans + " SET active = 1, activateddate ="+timestamp+", approvedby ='"+uuid+"' WHERE id = '"+ code + "'");
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
		return;            
    }
    public void MarkContractPayed(int code) {
        Connection conn = null;
        PreparedStatement ps = null;
        long timestamp = System.currentTimeMillis();
        try {
            conn = getSQLConnection();

            ps = conn.prepareStatement("UPDATE " + loans + " SET active = 3, downpayeddate ="+timestamp+" WHERE id = '"+ code + "'");
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return;             
    }
    public List<LoanObject> GetLoanObject() throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
		List<LoanObject> out = new ArrayList<>();
		LoanObject lo = null;
		
        try {
            conn = getSQLConnection();

            ps = conn.prepareStatement("SELECT * FROM "+loans+" WHERE active = 1");
            rs = ps.executeQuery();
            while(rs.next()){
            	lo = new LoanObject(plugin);
            	lo.setbankid(rs.getInt("bankid"));
            	lo.setloanid(rs.getInt("id"));
            	lo.setborrower(rs.getString("borrower"));
            	lo.setinterest(rs.getInt("interest"));
            	lo.setborrowed(rs.getInt("borrowed"));
            	lo.setpayments(rs.getInt("payments"));
            	lo.setfee(rs.getInt("fee"));
            	lo.setinterestrate(rs.getInt("interestrate"));
        		out.add(lo);
            }   
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
		return out;            
    }
    public void Withdraw(Player player, int bankid, int ammount) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();

            ps = conn.prepareStatement("UPDATE " + banks + " SET value = value - " + ammount + " WHERE id = '"+ bankid + "'" );
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return;             
    }
   
    public void RemRecivables(int bankid, int ammount) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();

            ps = conn.prepareStatement("UPDATE " + banks + " SET receivables = receivables - " + ammount + " WHERE id = '"+ bankid + "'" );
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return;             
    }
    public boolean CheckIfBankNameExsist(String bankname) {
    	bankname = bankname.toLowerCase(); 
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean exsist= false; 
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT nameofbank FROM " + banks + " WHERE nameofbank = '"+bankname+"' ;");
            rs = ps.executeQuery();
            	if(rs.next()){
            		exsist = true;
            	}else{
            		exsist = false;
            	}
            
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return exsist;             
    }
    public boolean CheckIfLoanExsist(Player player,String bankname) {
    	bankname = bankname.toLowerCase(); 
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean exsist= false; 
        String myuuid = player.getUniqueId().toString();
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT banks.nameofbank, loans.borrower " +
			"FROM pbank_loans as loans " +
			"LEFT JOIN pbank_banks as banks " +
			"ON banks.id = loans.bankid " + 
			"WHERE banks.nameofbank = '" + bankname + "' " +
			"AND loans.borrower = '" + myuuid + "' AND loans.active IS NOT 3;");
            rs = ps.executeQuery();
            	if(rs.next()){
            		exsist = true;
            	}else{
            		exsist = false;
            	}
            
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return exsist;             
    }
    public boolean CheckIfBankHasLoans(int bankid) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean exsist= false; 
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT loans.borrower " +
			"FROM pbank_loans as loans " + 
			"WHERE loans.bankid = '" + bankid + "' " +
			"AND loans.active IS NOT 3 OR 0;");
            rs = ps.executeQuery();
            	if(rs.next()){
            		exsist = true;
            	}else{
            		exsist = false;
            	}
            
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return exsist;             
    }
    public boolean CheckIfLoanExsistCodeOwner(Player player,int code) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean exsist= false; 
        String myuuid = player.getUniqueId().toString();
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT banks.nameofbank, loans.borrower " +
			"FROM pbank_loans as loans " +
			"LEFT JOIN pbank_banks as banks " +
			"ON banks.id = loans.bankid " + 
			"WHERE loans.id = '" + code + "' " +
			"AND banks.owner = '" + myuuid + "' AND loans.active IS NOT 3 ;");
            rs = ps.executeQuery();
            	if(rs.next()){
            		exsist = true;
            	}else{
            		exsist = false;
            	}
            
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return exsist;             
    }
    public boolean CheckIfReportExsistCodeOwner(Player player,int code) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean exsist= false; 
        String myuuid = player.getUniqueId().toString();
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT banks.nameofbank, loans.borrower " +
			"FROM pbank_loans as loans " +
			"LEFT JOIN pbank_banks as banks " +
			"ON banks.id = loans.bankid " + 
			"WHERE loans.id = '" + code + "' " +
			"AND banks.owner = '" + myuuid + "' AND loans.active IS 3 ;");
            rs = ps.executeQuery();
            	if(rs.next()){
            		exsist = true;
            	}else{
            		exsist = false;
            	}
            
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return exsist;             
    }
    public boolean CheckIfLoanExsistCodeBorrower(Player player,int code) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean exsist= false; 
        String myuuid = player.getUniqueId().toString();
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT loans.id, loans.borrower " +
			"FROM pbank_loans as loans " +
			"WHERE loans.id = '" + code + "' " +
			"AND loans.borrower = '" + myuuid + "' AND loans.active IS NOT 3;");
            rs = ps.executeQuery();
            	if(rs.next()){
            		exsist = true;
            	}else{
            		exsist = false;
            	}
            
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return exsist;             
    }

    public boolean CheckIfLoanIsActive(int code) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean exsist= false; 
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT loans.id, loans.borrower " +
			"FROM pbank_loans as loans " + 
			"WHERE loans.id = '" + code + "' AND loans.active = 1 ");
            rs = ps.executeQuery();
            	if(rs.next()){
            		exsist = true;
            	}else{
            		exsist = false;
            	}
            
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return exsist;             
    }
    public boolean CheckMoneyBank(Player player,String bankname, int ammount) {
    	bankname = bankname.toLowerCase(); 
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean money= false; 
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT value FROM " + banks + " WHERE nameofbank = '"+bankname+"' ;");
            rs = ps.executeQuery();
            rs.next();
        	int sqlmoney = rs.getInt("value"); 
            	if(ammount <= sqlmoney){
            		money = true;
            	}else{
            		money = false;
            	}
            
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return money;             
    }
    public boolean CheckMoneyBankEmpty(String bankname) {
    	bankname = bankname.toLowerCase(); 
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean money= false; 
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT value FROM " + banks + " WHERE nameofbank = '"+bankname+"' ;");
            rs = ps.executeQuery();
            rs.next();
        	int sqlmoney = rs.getInt("value"); 
            	if(sqlmoney <= 0){
            		money = true;
            	}else{
            		money = false;
            	}
            
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return money;             
    }
    public int CheckMoneyLeftLoan(int code) { 
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int money= 0; 
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT (interest+borrowed+fee-payments)AS money FROM " + loans + " WHERE id = '"+code+"' ;");
            rs = ps.executeQuery();
            rs.next();
        	money = rs.getInt("money"); 

            
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return money;             
    }
    public int CountBanksByPlayer(Player player) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null; 
        String myuuid = player.getUniqueId().toString();
        int count = 0;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT COUNT(*) AS rowcount FROM " + banks + " WHERE owner = '"+myuuid+"' ;");
            rs = ps.executeQuery();
        	rs.next();
        	count = rs.getInt("rowcount"); 
            
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return count;             
    }
    public int CountLoansByPlayer(Player player) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null; 
        String myuuid = player.getUniqueId().toString();
        int count = 0;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT COUNT(*) AS rowcount FROM " + loans + " WHERE borrower = '"+myuuid+"' ;");
            rs = ps.executeQuery();
        	rs.next();
        	count = rs.getInt("rowcount"); 
            
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return count;             
    }
    public boolean CheckIfOwner(Player player, String BankName) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null; 
        String myuuid = player.getUniqueId().toString();
        boolean isowner = false;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT owner FROM " + banks + " WHERE nameofbank = '"+BankName+"' ;");
            rs = ps.executeQuery();
        	rs.next();
        	String owner = rs.getString("owner"); 
        	if(myuuid.equalsIgnoreCase(owner)){
        		isowner = true;
        	}
            
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return isowner;             
    }
    public String[] GetBankInfo(String BankName) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null; 
        String info[] = new String[9];
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + banks + " WHERE nameofbank = '"+BankName+"' ;");
            rs = ps.executeQuery();
        	rs.next();
        	String uuid = rs.getString("owner");
        	int bankid = rs.getInt("id"); 
        	UUID uuid2 = UUID.fromString(uuid);
        	info[0] = Bukkit.getOfflinePlayer(uuid2).getName();
        	info[1] = rs.getString("interestrate");
        	info[2] = rs.getString("inviteonly");
        	info[3] = rs.getString("value");
        	//info[4] = int.toString(GetRecivables(bankid));
        	info[5] = rs.getString("maxloan");
        	info[6] = uuid;
        	info[7] = rs.getString("fee");
        	info[8] = Integer.toString(bankid);
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return info;             
    }
    public String[] GetLoanInfo(int code) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null; 
        String info[] = new String[16];
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT loans.*, banks.value, banks.nameofbank, banks.owner FROM " + loans + " AS loans"
            		+ " LEFT JOIN " + banks + " as banks"
    				+ " ON loans.bankid = banks.id"
            		+ " WHERE loans.id = '" + code + "' ;");
            rs = ps.executeQuery();
        	rs.next();
        	String uuid = rs.getString("borrower"); 
        	UUID uuid2 = UUID.fromString(uuid);
        	info[0] = Bukkit.getOfflinePlayer(uuid2).getName();
        	info[1] = rs.getString("interestrate");
        	info[2] = rs.getString("interest");
        	info[3] = rs.getString("borrowed");
        	info[4] = rs.getString("payments");
        	info[5] = rs.getString("active");
        	info[6] = rs.getString("requestdate");
        	info[7] = rs.getString("value");
        	info[8] = uuid;
        	info[9] = rs.getString("fee");
        	info[10] = rs.getString("activateddate");
        	info[11] = rs.getString("bankid");
        	info[12] = rs.getString("nameofbank");
        	info[13] = rs.getString("owner");
        	
        	info[14] = rs.getString("downpayeddate");
        	info[15] = rs.getString("approvedby");
        	
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return info;             
    }
    public int GetRecivables(int BankId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null; 
        int recivables = 0;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT TOTAL(fee + interest + borrowed - payments) AS recivables FROM " + loans + " WHERE bankid = '" + BankId + "' ;");

            rs = ps.executeQuery();
        	recivables = rs.getInt("recivables"); 

            
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return recivables;             
    }
    public Integer GetBankID(String BankName) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null; 
        Integer BankID = 0;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT id FROM " + banks + " WHERE nameofbank = '"+BankName+"' ;");
            rs = ps.executeQuery();
        	rs.next();
        	BankID = rs.getInt("id"); 
            
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return BankID;             
    }
    public Integer GetBankIDFromLoan(int loanid) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null; 
        Integer BankID = 0;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT bankid FROM " + loans + " WHERE id = '"+loanid+"' ;");
            rs = ps.executeQuery();
        	rs.next();
        	BankID = rs.getInt("bankid"); 
             
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return BankID;             
    }
    public void close(PreparedStatement ps,ResultSet rs){
        try {
            if (ps != null)
                ps.close();
            if (rs != null)
                rs.close();
        } catch (SQLException ex) {
            Error.close(plugin, ex);
        }
    }
}