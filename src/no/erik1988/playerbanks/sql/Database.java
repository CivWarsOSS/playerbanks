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
import no.erik1988.playerbanks.objects.PageObject;

abstract class Database {
	Main plugin;
	Connection connection;
	// The name of the table we created back in SQLite class.
	private String banks = "pbank_banks";
	private String loans = "pbank_loans";
	private String transactions = "pbank_transactions";
	private String msgtable = "pbank_msg";
	private String logtable = "pbank_log";
	Database(Main instance){
		plugin = instance;
	}

	public abstract Connection getSQLConnection();

	public abstract void load();

	void initialize(){
		connection = getSQLConnection();
		try{
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + banks + " WHERE id = ?");
			ps.setInt(1, 0);
			ResultSet rs = ps.executeQuery();
			close(ps,rs);

		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
		}
	}

	public List<PageObject> MyBanks(Player player) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		FileConfiguration c = plugin.getConfig();
		String myuuid = player.getUniqueId().toString(); 
		List<PageObject> out = new ArrayList<>();
		PageObject ll = null;
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("SELECT * FROM " + banks + " WHERE (owner = ? OR manager = ?) ");
			ps.setString(1, myuuid);
			ps.setString(2, myuuid);

			rs = ps.executeQuery();
			while(rs.next()){
				if(rs.getString("owner") != null){
					String bankname = rs.getString("nameofbank");
					int interestrate = rs.getInt("interestrate");
					int value = rs.getInt("value");
					int maxloan = rs.getInt("maxloan");
					int fee = rs.getInt("fee");
					String valueasstirng = Integer.toString(value);
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
					ll = new PageObject(plugin);
					ll.setmsg(plugin.getMessager().get("Mybank.Bank.List").replace("%bank%", bankname).replace("%value%", valueasstirng).replace("%maxloan%", maxloanasstirng).replace("%interest%", interestrateAsString).replace("%fee%", Integer.toString(fee)));
					out.add(ll);
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
		return out;     
	}
	public List<PageObject> BankList(Player player) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		FileConfiguration c = plugin.getConfig();
		List<PageObject> out = new ArrayList<>();
		PageObject ll = null;
		try {

			conn = getSQLConnection();
			ps = conn.prepareStatement("SELECT nameofbank,owner,value,fee,interestrate FROM " + banks + " ORDER BY value DESC");
			rs = ps.executeQuery();

			while(rs.next()){
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
				ll = new PageObject(plugin);
				ll.setmsg(plugin.getMessager().get("Banks.Bank.List").replace("%owner%", owner).replace("%bank%", bankname).replace("%value%", Integer.toString(value)).replace("%fee%", Integer.toString(fee)).replace("%interest%", interestrateAsString));
				out.add(ll);
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
	public void MakeBank(Player player, String name, int interestrate, int maxloan, int fee) {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getSQLConnection();

			//String timestamp = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(System.currentTimeMillis()); 
			long timestamp = System.currentTimeMillis();


			ps = conn.prepareStatement("REPLACE INTO " + banks + " (id,nameofbank,owner,manager,interestrate,inviteonly,value,maxloan,fee,timestamp) VALUES(?,?,?,?,?,?,?,?,?,?)"); 
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
	public void LogTrans(int type, int contract,int amount,OfflinePlayer borrowerplayer, int bankid, int seen) {
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
	public void AddLog(int bankid, String log) {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getSQLConnection();

			//String timestamp = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(System.currentTimeMillis()); 
			long timestamp = System.currentTimeMillis();


			ps = conn.prepareStatement("REPLACE INTO " + logtable + " (id,bankid,log,timestamp) VALUES(?,?,?,?)"); 
			ps.setInt(2, bankid); 
			ps.setString(3, log); 
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
	public int CountPendingContracts(Player player) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String myuuid = player.getUniqueId().toString();
		int count = 0;
		try {
			conn = getSQLConnection();

			//String timestamp = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(System.currentTimeMillis());
			//long timestamp = System.currentTimeMillis();

			ps = conn.prepareStatement("Select banks.nameofbank, loans.borrower " +
					"From pbank_banks AS banks " +
					"LEFT JOIN pbank_loans AS loans " +
					"ON banks.id = loans.bankid " +
					"WHERE (banks.owner = ? OR banks.manager = ?) AND loans.borrower IS NOT NULL AND loans.active IS 0 " + 
					"ORDER BY loans.requestdate DESC");
			ps.setString(1, myuuid);  
			ps.setString(2, myuuid);
			rs = ps.executeQuery();
			while(rs.next()){
				count++;

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
		return count;             
	}
	public List<PageObject> ListContracts(Player player) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		FileConfiguration c = plugin.getConfig();
		String myuuid = player.getUniqueId().toString();
		List<PageObject> out = new ArrayList<>();
		PageObject ll = null;
		try {
			conn = getSQLConnection();

			//String timestamp = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(System.currentTimeMillis());
			//long timestamp = System.currentTimeMillis();

			ps = conn.prepareStatement("Select (loans.interest + loans.borrowed - loans.payments) AS total, loans.id, banks.nameofbank, banks.owner, loans.borrower, loans.borrowed, loans.interestrate, loans.active, loans.requestdate, loans.fee " +
					"From pbank_banks AS banks " +
					"LEFT JOIN pbank_loans AS loans " +
					"ON banks.id = loans.bankid " +
					"where (banks.owner = ? OR banks.manager = ?) AND loans.borrower IS NOT NULL AND loans.active IS NOT 3 " + 
					"ORDER BY loans.requestdate DESC");
			ps.setString(1, myuuid);  
			ps.setString(2, myuuid);
			rs = ps.executeQuery();
			while(rs.next()){
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
				//amount = Math.round(amount * 100);
				//amount = amount/100;
				String status2 = c.getString("Status.Pending","§ePending");
				if (status == 1){
					status2 = c.getString("Status.Active","§2Active");
					amount = amount + fee;
				} else if (status == 4){
					status2 = c.getString("Status.Frozen","§3Frozen");
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
				ll = new PageObject(plugin);
				ll.setmsg(plugin.getMessager().get("Mybank.Contracts.List").replace("%code%", Integer.toString(code)).replace("%bank%", bankname).replace("%req%", Integer.toString(amount)).replace("%interest%",interest).replace("%player%", borrower).replace("%time%", time).replace("%status%", status2));

				out.add(ll);
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
	public List<PageObject> ListReports(Player player) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String myuuid = player.getUniqueId().toString();
		List<PageObject> out = new ArrayList<>();
		PageObject ll = null;
		try {
			conn = getSQLConnection();

			//String timestamp = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(System.currentTimeMillis());
			//long timestamp = System.currentTimeMillis();

			ps = conn.prepareStatement("Select loans.id, loans.borrower, loans.downpayeddate, banks.nameofbank " +
					"From pbank_banks AS banks " +
					"LEFT JOIN pbank_loans AS loans " +
					"ON banks.id = loans.bankid " +
					"where (banks.owner = '" + myuuid + "' OR banks.manager = '" + myuuid + "') AND loans.borrower IS NOT NULL AND loans.active IS 3 " + 
					"ORDER BY loans.downpayeddate DESC");
			rs = ps.executeQuery();
			while(rs.next()){
				String bankname = rs.getString("nameofbank");
				int loanid = rs.getInt("id");
				long timestamp2 = rs.getInt("downpayeddate");
				String date = new SimpleDateFormat("dd.MM.yy").format(timestamp2);
				String buuid = rs.getString("borrower");
				UUID buuid2 = UUID.fromString(buuid);
				String borrower = Bukkit.getOfflinePlayer(buuid2).getName();
				ll = new PageObject(plugin);
				ll.setmsg(plugin.getMessager().get("Mybank.ReportList.List").replace("%loanid%", Integer.toString(loanid)).replace("%bank%", bankname).replace("%borrower%", borrower).replace("%date%", date));
				out.add(ll);

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
	public List<PageObject> ListMSGAll(Player player) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String myuuid = player.getUniqueId().toString();
		List<PageObject> out = new ArrayList<>();
		PageObject ll = null;
		try {
			conn = getSQLConnection();

			//String timestamp = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(System.currentTimeMillis());
			//long timestamp = System.currentTimeMillis();

			ps = conn.prepareStatement("Select msg " +
					"From pbank_msg " +
					"where playeruuid = '" + myuuid + "' " + 
					"ORDER BY timestamp DESC");
			rs = ps.executeQuery();
			while(rs.next()){
				ll = new PageObject(plugin);
				ll.setmsg(rs.getString("msg"));
				out.add(ll);

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
	public List<PageObject> GetTransPlayer(Player player) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String myuuid = player.getUniqueId().toString();
		List<PageObject> out = new ArrayList<>();
		PageObject ll = null;
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
					+ "");
			rs = ps.executeQuery();
			while(rs.next()){
				int type = rs.getInt("type");
				//int contract = rs.getInt("contract");
				int amount = rs.getInt("amount");
				//int bankid = rs.getInt("bankid");
				long timestamp = rs.getLong("timestamp");
				String nameofbank = rs.getString("nameofbank");
				String time = new SimpleDateFormat("dd.MM.yy").format(timestamp);
				UUID uuid = UUID.fromString(rs.getString("playeruuid"));
				OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(uuid);
				String playername = offlineplayer.getName().toString();
				ll = new PageObject(plugin);
				if(type == 0){
					ll.setmsg(this.plugin.getMessager().get("transactions.deposit").replace("%time%",time).replace("%player%",playername).replace("%amount%",Integer.toString(amount)).replace("%bank%",nameofbank));
				}
				else if(type == 1){
					ll.setmsg(this.plugin.getMessager().get("transactions.withdraw").replace("%time%",time).replace("%player%",playername).replace("%amount%",Integer.toString(amount)).replace("%bank%",nameofbank));
				}
				else if(type == 2){
					ll.setmsg(this.plugin.getMessager().get("transactions.borrow").replace("%time%",time).replace("%player%",playername).replace("%amount%",Integer.toString(amount)).replace("%bank%",nameofbank));
				}
				else if(type == 3){
					ll.setmsg(this.plugin.getMessager().get("transactions.payment").replace("%time%",time).replace("%player%",playername).replace("%amount%",Integer.toString(amount)).replace("%bank%",nameofbank));
				}
				else if(type == 4){
					ll.setmsg(this.plugin.getMessager().get("transactions.interest").replace("%time%",time).replace("%player%",playername).replace("%amount%",Integer.toString(amount)).replace("%bank%",nameofbank));
				}
				else if(type == 5){
					ll.setmsg(this.plugin.getMessager().get("transactions.autopay").replace("%time%",time).replace("%player%",playername).replace("%amount%",Integer.toString(amount)).replace("%bank%",nameofbank));
				}
				out.add(ll);
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
	public List<PageObject> GetTransBank(Player player,Integer Bankid) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<PageObject> out = new ArrayList<>();
		PageObject ll = null;
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
					+ "");
			rs = ps.executeQuery();
			while(rs.next()){
				int type = rs.getInt("type");
				//int contract = rs.getInt("contract");
				int amount = rs.getInt("amount");
				//int bankid = rs.getInt("bankid");
				long timestamp = rs.getLong("timestamp");
				String nameofbank = rs.getString("nameofbank");
				String time = new SimpleDateFormat("dd.MM.yy").format(timestamp);
				UUID uuid = UUID.fromString(rs.getString("playeruuid"));
				OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(uuid);

				//Displays this instead of crashing if it fails to find the player. 
				String playername = "N/A";
				if (offlineplayer != null){
					playername = offlineplayer.getName().toString();
				}
				ll = new PageObject(plugin);
				if(type == 0){
					ll.setmsg(this.plugin.getMessager().get("transactions.deposit").replace("%time%",time).replace("%player%",playername).replace("%amount%",Integer.toString(amount)).replace("%bank%",nameofbank));
				}
				else if(type == 1){
					ll.setmsg(this.plugin.getMessager().get("transactions.withdraw").replace("%time%",time).replace("%player%",playername).replace("%amount%",Integer.toString(amount)).replace("%bank%",nameofbank));
				}
				else if(type == 2){
					ll.setmsg(this.plugin.getMessager().get("transactions.borrow").replace("%time%",time).replace("%player%",playername).replace("%amount%",Integer.toString(amount)).replace("%bank%",nameofbank));
				}
				else if(type == 3){
					ll.setmsg(this.plugin.getMessager().get("transactions.payment").replace("%time%",time).replace("%player%",playername).replace("%amount%",Integer.toString(amount)).replace("%bank%",nameofbank));
				}
				else if(type == 4){
					ll.setmsg(this.plugin.getMessager().get("transactions.interest").replace("%time%",time).replace("%player%",playername).replace("%amount%",Integer.toString(amount)).replace("%bank%",nameofbank));
				}
				else if(type == 5){
					ll.setmsg(this.plugin.getMessager().get("transactions.autopay").replace("%time%",time).replace("%player%",playername).replace("%amount%",Integer.toString(amount)).replace("%bank%",nameofbank));
				}
				out.add(ll);
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
					"where (playeruuid = '" + myuuid + "' AND seen = 0) ");
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

	public List<PageObject> ListRequests(Player player) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		FileConfiguration c = plugin.getConfig();
		String myuuid = player.getUniqueId().toString();
		List<PageObject> out = new ArrayList<>();
		PageObject ll = null;
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
			while(rs.next()){
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
				//amount = Math.round(amount * 100);
				//amount = amount/100;
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
				ll = new PageObject(plugin);
				ll.setmsg(plugin.getMessager().get("borrow.Loans.List").replace("%code%", Integer.toString(code)).replace("%bank%", bankname).replace("%req%", amountasstirng).replace("%interest%",interest).replace("%owner%", owner).replace("%time%", time).replace("%status%", status2));
				out.add(ll);
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
	public void RemTrans(int bankid) {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getSQLConnection();

			ps = conn.prepareStatement("DELETE FROM " + transactions + " WHERE bankid = '"+ bankid + "'" );
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
	public void RemLog(int bankid) {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getSQLConnection();

			ps = conn.prepareStatement("DELETE FROM " + logtable + " WHERE bankid = '"+ bankid + "'" );
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
	public void CleanUpLog() {
		Connection conn = null;
		PreparedStatement ps = null;
		FileConfiguration c = plugin.getConfig();
		long timestamp = System.currentTimeMillis();
		int hours = c.getInt("cleanup.log.olderthan.hours",168);
		try {
			conn = getSQLConnection();

			ps = conn.prepareStatement("DELETE FROM " + logtable + " WHERE timestamp < ('" + timestamp + "' - ("+hours+" * 60 * 60 * 1000))");
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
	public void MarkContractStatus(int code, int status) {
		Connection conn = null;
		PreparedStatement ps = null;
		//long timestamp = System.currentTimeMillis();
		//String uuid = player.getUniqueId().toString();
		try {
			conn = getSQLConnection();

			ps = conn.prepareStatement("UPDATE " + loans + " SET active = '"+status+"' WHERE id = '"+ code + "'");
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
	public void MarkLoanAsMissed(int loanid, int type) {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getSQLConnection();
			if (type == 1){
	ps = conn.prepareStatement("UPDATE " + loans + " SET missedpaymentsrow = missedpaymentsrow + 1 WHERE id = '"+ loanid + "'");
			} else{
	ps = conn.prepareStatement("UPDATE " + loans + " SET missedpaymentsrow = 0 WHERE id = '"+ loanid + "'");
			}
				
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

			ps = conn.prepareStatement("SELECT * FROM "+loans+" WHERE active = 1 OR active = 4");
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
				lo.setactive(rs.getInt("active"));
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
	public List<LoanObject> GetLoanObjectPlayer(OfflinePlayer player) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<LoanObject> out = new ArrayList<>();
		LoanObject lo = null;
		String uuid = player.getUniqueId().toString();
		try {
			conn = getSQLConnection();

			ps = conn.prepareStatement("SELECT * FROM "+loans+" WHERE (active = 1 OR active = 4) AND borrower = "+uuid+"");
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
	public List<PageObject> GetLogObject(int bankid){
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<PageObject> out = new ArrayList<>();
		PageObject ll = null;

		try {
			conn = getSQLConnection();

			ps = conn.prepareStatement("SELECT * FROM "+logtable+" WHERE bankid = ? ORDER BY timestamp DESC");

			ps.setInt(1, bankid);
			rs = ps.executeQuery();
			while(rs.next()){
				ll = new PageObject(plugin);
				ll.setbankid(rs.getInt("bankid"));
				ll.setmsg(rs.getString("log"));
				out.add(ll);
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

	public boolean CheckIfBankNameExsist(String bankname) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean exsist= false; 
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("SELECT nameofbank FROM " + banks + " WHERE UPPER(nameofbank) = UPPER(?) ;");
			ps.setString(1, bankname);
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
	public boolean CheckIfLoanExsist(OfflinePlayer player,String bankname) {
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
					"WHERE UPPER(banks.nameofbank) = UPPER(?) " +
					"AND loans.borrower = '" + myuuid + "' AND loans.active IS NOT 3;");
			ps.setString(1, bankname);
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
					"AND loans.active IS NOT 3 OR loans.active IS NOT 0;");
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
	public boolean CheckIfLoanExsistCodeManage(Player player,int code) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean exsist= false; 
		String myuuid = player.getUniqueId().toString();
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("SELECT banks.nameofbank, loans.borrower, banks.owner, banks.manager " +
					"FROM pbank_loans as loans " +
					"LEFT JOIN pbank_banks as banks " +
					"ON banks.id = loans.bankid " + 
					"WHERE loans.id = ? " +
					"AND loans.active IS NOT 3 ;");
			ps.setInt(1, code);
			rs = ps.executeQuery();
			if (rs.next()){
				String owneruuid = rs.getString("owner");
				String manuuid = rs.getString("manager");
				if (myuuid.equalsIgnoreCase(owneruuid) || myuuid.equalsIgnoreCase(manuuid)){
					exsist = true;
				}
			} else {
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
	public boolean CheckIfReportExsistCodeManage(Player player,int code) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean exsist= false; 
		String myuuid = player.getUniqueId().toString();
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("SELECT banks.nameofbank, loans.borrower, banks.owner, banks.manager " +
					"FROM pbank_loans as loans " +
					"LEFT JOIN pbank_banks as banks " +
					"ON banks.id = loans.bankid " + 
					"WHERE loans.id = '" + code + "' " +
					"AND loans.active IS 3 ;");
			rs = ps.executeQuery();
			rs.next();
			String owneruuid = rs.getString("owner");
			String manuuid = rs.getString("manager");
			if (myuuid.equalsIgnoreCase(owneruuid) || myuuid.equalsIgnoreCase(manuuid)){
				exsist = true;
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
					"AND loans.borrower = '" + myuuid + "' AND loans.active IS NOT 3");
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
//TODO: check for error
	public boolean CheckIfLoanIsActive(int code) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean exsist= false; 
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("SELECT loans.id, loans.borrower " +
					"FROM pbank_loans as loans " + 
					"WHERE loans.id = '" + code + "' AND (loans.active = 1 OR loans.active = 4) ");
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
	//TODO: Could be replaced. 
	public int GetMoneyBank(String bankname) {
		bankname = bankname.toLowerCase(); 
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		int sqlmoney = 0;
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("SELECT value FROM " + banks + " WHERE UPPER(nameofbank) = UPPER(?) ;");
			ps.setString(1, bankname);
			rs = ps.executeQuery();
			rs.next();
			sqlmoney = rs.getInt("value"); 

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
		return sqlmoney;             
	}
	public boolean CheckMoneyBankEmpty(String bankname) {
		bankname = bankname.toLowerCase(); 
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean money= false; 
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("SELECT value FROM " + banks + " WHERE UPPER(nameofbank) = UPPER(?) ;");
			ps.setString(1, bankname);
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
			ps = conn.prepareStatement("SELECT owner FROM " + banks + " WHERE UPPER(nameofbank) = UPPER(?) ;");
			ps.setString(1, BankName);
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
	public boolean CheckIfManager(Player player, String BankName) { 
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null; 
		String myuuid = player.getUniqueId().toString();
		boolean ismanager = false;
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("SELECT manager FROM " + banks + " WHERE UPPER(nameofbank) = UPPER(?) ;");
			ps.setString(1, BankName);
			rs = ps.executeQuery();
			rs.next();
			String manager = rs.getString("manager"); 
			if(myuuid.equalsIgnoreCase(manager)){
				ismanager = true;
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
		return ismanager;             
	}
	public String[] GetBankInfo(String BankName) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null; 
		String info[] = new String[11];
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("SELECT * FROM " + banks + " WHERE UPPER(nameofbank) = UPPER(?) ;");
			ps.setString(1, BankName);
			rs = ps.executeQuery();
			rs.next();
			String uuid = rs.getString("owner");
			String manuuid = rs.getString("manager");
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
			info[10] = rs.getString("nameofbank");

			if(manuuid != null){
				UUID uuid3 = UUID.fromString(manuuid);
				info[9] = Bukkit.getOfflinePlayer(uuid3).getName();
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
			ps = conn.prepareStatement("SELECT id FROM " + banks + " WHERE UPPER(nameofbank) = UPPER(?) ;");
			ps.setString(1, BankName);
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
	public Integer GetLoanId(int BankId, OfflinePlayer player) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null; 
		Integer LoanId = 0;
		String uuid = player.getUniqueId().toString();
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("SELECT id FROM " + loans + " WHERE bankid = '"+BankId+"' AND borrower = '"+uuid+"' AND active = 1 OR active = 4;");
			rs = ps.executeQuery();
			while (rs.next()) {
			LoanId = rs.getInt("id"); 
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
		return LoanId;             
	}
	public List<Integer> GetAllContracts(int bankId) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> ll = null;
		try {
			conn = getSQLConnection();


			ps = conn.prepareStatement("Select loans.id AS id " +
					"From pbank_banks AS banks " +
					"LEFT JOIN pbank_loans AS loans " +
					"ON banks.id = loans.bankid " +
					"where (banks.id = ?) AND loans.borrower IS NOT NULL AND loans.active IS NOT 3 " + 
					"ORDER BY loans.requestdate DESC");
			ps.setInt(1, bankId);  
			rs = ps.executeQuery();
			while(rs.next()){
				int loanId = rs.getInt("id");

				ll = new ArrayList<Integer>();
				ll.add(loanId);
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
		return ll;             
	}
	//manager related
	public void AddManager(OfflinePlayer manager, String bankname) {
		Connection conn = null;
		PreparedStatement ps = null;
		String manageruuid = manager.getUniqueId().toString();
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("UPDATE " + banks + " SET manager = ? WHERE UPPER(nameofbank) = UPPER(?) ;"); 
			ps.setString(1, manageruuid);
			ps.setString(2, bankname);
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
	public String ListManager(String bankname) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null; 
		UUID uuid = null;
		String managername = null;
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("SELECT manager FROM " + banks + " WHERE UPPER(nameofbank) = UPPER(?) ;");
			ps.setString(1, bankname);
			rs = ps.executeQuery();
			rs.next();
			String manager = rs.getString("manager");
			if(manager == null || manager.isEmpty()){
				return null;
			} 
			uuid = UUID.fromString(manager);
			managername = Bukkit.getOfflinePlayer(uuid).getName();


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
		return managername;             
	}
	public UUID GetManagerUUID(String bankname) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null; 
		UUID uuid = null;
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("SELECT manager FROM " + banks + " WHERE UPPER(nameofbank) = UPPER(?) ;");
			ps.setString(1, bankname);
			rs = ps.executeQuery();
			rs.next();
			String manager = rs.getString("manager");
			if(manager == null || manager.isEmpty()){
				return null;
			} 
			uuid = UUID.fromString(manager);


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
		return uuid;             
	}
	public void ClearManager(String bankname) {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("UPDATE " + banks + " SET manager = '' WHERE UPPER(nameofbank) = UPPER(?) ;"); 
			ps.setString(1, bankname);
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




	//close
	private void close(PreparedStatement ps,ResultSet rs){
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