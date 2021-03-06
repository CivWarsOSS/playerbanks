package no.erik1988.playerbanks;

import java.text.SimpleDateFormat;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import no.erik1988.playerbanks.LogHandler;
import no.erik1988.playerbanks.Schedule;
import no.erik1988.playerbanks.MessageHandler;
import no.erik1988.playerbanks.sql.SQLite;
import no.erik1988.playerbanks.commands.Pbank;
import no.erik1988.playerbanks.commands.Support;
import no.erik1988.playerbanks.objects.LoanObject;
import no.erik1988.playerbanks.listener.LoginListener;
import no.erik1988.playerbanks.listener.SignListener;

public class Main extends JavaPlugin {
	private FileConfiguration config = getConfig();
	private MessageHandler m;
	public SQLite sql;
	public Support support;
	public Actions actions;
	private LoanObject loanobject;
	private Economy economy;

	@Override
	public void onEnable() {
		this.m = new MessageHandler(this);
		this.sql = new SQLite(this);
		this.support = new Support(this);
		this.loanobject = new LoanObject(this);
		this.actions = new Actions(this);
		Pbank pbank = new Pbank(this);
		getCommand("pbank").setExecutor(pbank);
		config.addDefault("interest.autocharge.enable", Boolean.valueOf(true));
		config.addDefault("interest.timeofday.hour", Integer.valueOf(5));
		config.addDefault("interest.autocharge.FixedAmount", Integer.valueOf(10));
		config.addDefault("interest.low", Integer.valueOf(2));
		config.addDefault("interest.med", Integer.valueOf(5));
		config.addDefault("interest.high", Integer.valueOf(10));
		config.addDefault("cleanup.enable", Boolean.valueOf(true));	
		config.addDefault("cleanup.timeofday.hour", Integer.valueOf(4));
		config.addDefault("cleanup.msg.enable", Boolean.valueOf(true));
		config.addDefault("cleanup.log.enable", Boolean.valueOf(false));
		config.addDefault("cleanup.transactions.enable", Boolean.valueOf(false));
		config.addDefault("cleanup.msg.olderthan.hours", Integer.valueOf(168));
		config.addDefault("cleanup.log.olderthan.hours", Integer.valueOf(720));
		config.addDefault("cleanup.transactions.olderthan.hours", Integer.valueOf(720));
		//config.addDefault("cleanup.removeInactive", Boolean.valueOf(false));
		config.addDefault("bank.name.blacklist", new String[] { "server", "admin", "bank" });
		//config.addDefault("bank.allowmanagers", Boolean.valueOf(true));
		config.addDefault("bank.banksPerPlayer", Integer.valueOf(3));
		config.addDefault("loan.minBorrow.value", Integer.valueOf(50));
		//config.addDefault("loan.maxBorrow.enabled", Boolean.valueOf(true));
		config.addDefault("loan.maxBorrow.group1.value", Integer.valueOf(100)); 
		config.addDefault("loan.maxBorrow.group1.maxcontracts", Integer.valueOf(3));
		//config.addDefault("loan.maxBorrow.group1.perm", "pbank.group1"); 
		config.addDefault("loan.maxBorrow.group2.value", Integer.valueOf(500));
		config.addDefault("loan.maxBorrow.group2.perm", "pbank.group2");
		config.addDefault("loan.maxBorrow.group2.maxcontracts", Integer.valueOf(4));
		config.addDefault("loan.maxBorrow.group3.value", Integer.valueOf(1000));
		config.addDefault("loan.maxBorrow.group3.perm", "pbank.group3");
		config.addDefault("loan.maxBorrow.group3.maxcontracts", Integer.valueOf(5));
		config.addDefault("loan.maxBorrow.unlimited.perm.value", "pbank.unlimited.value");
		config.addDefault("loan.maxBorrow.unlimited.perm.maxcontracts", "pbank.unlimited.maxcontract");
		config.addDefault("loan.maxfee", Integer.valueOf(20));
		config.addDefault("other.notifyOnLogin", Boolean.valueOf(true)); 
		config.addDefault("sign.active", Boolean.valueOf(true));
		config.addDefault("sign.allias.borrow", "[Borrow]");
		config.addDefault("sign.allias.pay", "[PayLoan]");
		config.options().copyDefaults(true);

		saveConfig(); 
		reloadConfig();

		if (!pluginChecks()) {
			LogHandler.severe("Disabling PlayerBanks.");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		//starts login listener
		boolean onlogin = config.getBoolean("other.notifyOnLogin",true);
		if(onlogin){
			getServer().getPluginManager().registerEvents(new LoginListener(this), this);
		}
		//starts sign listener
		boolean onsign = config.getBoolean("sign.active",true);
		if(onsign){
			getServer().getPluginManager().registerEvents(new SignListener(this), this);
		}
		
		
		sql.load();
		int timeofday = config.getInt("interest.timeofday.hour",4);
		int timeofdaycleanup = config.getInt("cleanup.timeofday.hour",5);

		Schedule.scheduleRepeatAtTime(this, new Runnable() {
			@Override
			public void run() {
				AutochargeAndInterest();
			}
		}, timeofday);
		boolean cleanup = config.getBoolean("cleanup.enable");
		if(cleanup){
			Schedule.scheduleRepeatAtTime(this, new Runnable() {
				@Override
				public void run() {
					cleanUp();
				}
			}, timeofdaycleanup);
		}

	}

	@Override
	public void onDisable() {
		this.m = null;
		this.sql = null;
		this.loanobject = null;
		this.actions = null;

	}
	public MessageHandler getMessager() 
	{
		return this.m;
	}
	public Economy getEconomy()
	{
		return this.economy;
	}
	private boolean pluginChecks()
	{

		if (getServer().getPluginManager().getPlugin("Vault") == null){
			LogHandler.severe("Vault not found!");
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			LogHandler.severe("Economy provider not found!");
			return false;
		}
		economy = rsp.getProvider();
		LogHandler.info("Vault and economy provider found!");

		return true;
	}
	public void cleanUp(){
		boolean cleanup = config.getBoolean("cleanup.enable");
		if(cleanup){
			boolean msg = config.getBoolean("cleanup.msg.enable");
			boolean transactions = config.getBoolean("cleanup.transactions.enable");
			boolean log = config.getBoolean("cleanup.log.enable");
			int thours = config.getInt("cleanup.transactions.olderthan.hours",720);
			int mhours = config.getInt("cleanup.msg.olderthan.hours",168);
			int lhours = config.getInt("cleanup.log.olderthan.hours",720);
			if(msg){
				sql.CleanUpMSG();
				LogHandler.info("Removed seen msg older than "+mhours+" hours.");
			}
			if(transactions){
				sql.CleanUpTransactions();
				LogHandler.info("Removed seen transaction logs older than "+thours+" hours.");
			}
			if(log){
				sql.CleanUpLog();
				LogHandler.info("Removed log entries older than "+lhours+" hours.");
			}
		} 


	}
	public void CheckifLoanIsPayed(int loanid){
		int paymentleft = sql.CheckMoneyLeftLoan(loanid);
		if(paymentleft <= 0){
			FinishLoan(loanid);
		}
	}

	private void FinishLoan(int loanid){
		String[] info = sql.GetLoanInfo(loanid);
		int bankid = Integer.parseInt(info[11]);
		int interest = Integer.parseInt(info[2]);
		int borrowed = Integer.parseInt(info[3]);
		//int payments = Integer.parseInt(info[4]);
		String nameofbank = info[12];
		String timestamp = new SimpleDateFormat("dd.MM.yy").format(System.currentTimeMillis());

		UUID borroweruuid = UUID.fromString(info[8]);
		UUID owneruuid = UUID.fromString(info[13]);
		int fee = Integer.parseInt(info[9]);
		OfflinePlayer borrowerplayer = Bukkit.getOfflinePlayer(borroweruuid);
		OfflinePlayer ownerplayer = Bukkit.getOfflinePlayer(owneruuid);
		String borrowerplayername = borrowerplayer.getName();

		sql.MarkContractPayed(loanid);
		LogHandler.info("loanid "+loanid+" is paid down");

		String newreport = getMessager().get("Mybank.Report.NewReport").replace("%loanid%", Integer.toString(loanid));

		String msgowner = getMessager().get("Mybank.Contracts.Finished").replace("%amount%", Integer.toString(borrowed)).replace("%time%", timestamp).replace("%bank%", nameofbank).replace("%borrower%", borrowerplayername).replace("%interest%", Integer.toString(interest)).replace("%fee%", Integer.toString(fee));

		sql.AddMSG(ownerplayer, newreport, owneruuid);
		sql.AddMSG(ownerplayer, msgowner, owneruuid);

		String msgborrower = getMessager().get("borrow.Finished").replace("%amount%", Integer.toString(borrowed)).replace("%time%", timestamp).replace("%bank%", nameofbank).replace("%borrower%", borrowerplayername).replace("%interest%", Integer.toString(interest)).replace("%fee%", Integer.toString(fee));	
		sql.AddMSG(borrowerplayer, msgborrower, borroweruuid);

		String log = getMessager().get("log.Finished").replace("%loanid%", Integer.toString(loanid)).replace("%time%", timestamp).replace("%bank%", nameofbank).replace("%borrower%", borrowerplayername).replace("%amount%", Integer.toString(borrowed));
		sql.AddLog(bankid, log);

		ShowMsgIfOnline(borroweruuid);
		ShowMsgIfOnline(owneruuid);

	}
	public void ShowMsgIfOnline(UUID UUID){
		OfflinePlayer player = Bukkit.getOfflinePlayer(UUID);
		if (player.isOnline()){
			Player player2 = Bukkit.getPlayer(UUID);
			if (sql.ListMSG(player2)) {
				sql.MarkMSGAsSeen(player2);
			}
		}

	}

	public void SendMsgIfOnline(UUID UUID, String msg){
		OfflinePlayer player = Bukkit.getOfflinePlayer(UUID);
		if (player.isOnline()){
			Player player2 = Bukkit.getPlayer(UUID);
			player2.sendMessage(msg);
		}

	}
	public void ShowTransIfOnline(UUID UUID){
		OfflinePlayer player = Bukkit.getOfflinePlayer(UUID);
		if (player.isOnline()){
			Player player2 = Bukkit.getPlayer(UUID);
			player2.sendMessage(getMessager().get("transactions.newtrans")); 
		}

	}
	public void AutochargeAndInterest(){
		FileConfiguration c = getConfig();
		boolean autocharge = c.getBoolean("interest.autocharge.enable");
		LogHandler.info("NEW DAY!");
		Bukkit.broadcastMessage(getMessager().get("Main.NewDay"));
		loanobject.AddInterestToAll();
		if(autocharge){
			loanobject.DownPayForAll();	
		}
	}
	public void LogTransPre(int type, int contract,int amount,OfflinePlayer borrowerplayer, int bankid, int seen, UUID uuid){
		sql.LogTrans(type, contract, amount, borrowerplayer, bankid, seen);
		if(seen == 0){
			ShowTransIfOnline(uuid);
		}
	}
}
