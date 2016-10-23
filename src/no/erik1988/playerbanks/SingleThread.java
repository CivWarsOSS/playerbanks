package no.erik1988.playerbanks;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import no.erik1988.playerbanks.Main;

public class SingleThread{
	Main plugin;
	public SingleThread(Main plugin)
	{
		this.plugin = plugin;
	}
	public final ExecutorService exec = Executors.newSingleThreadExecutor();


	public void cleanUpThreaded() {
		exec.execute(new Runnable(){
			public void run(){
				plugin.cleanUp();
			}
		});
	}
	public void MakeLogThreaded(int type, int contract,int amount,OfflinePlayer borrowerplayer, int bankid, int seen, UUID uuid) {
		exec.execute(new Runnable(){
			public void run(){
				plugin.sql.MakeLog(type, contract, amount, borrowerplayer, bankid, seen);
				if(seen == 0){
					plugin.ShowTransIfOnline(uuid);
				}
			}
		});
	}
	public void AddMSGThreaded(OfflinePlayer player, String msg, UUID uuid) {
		exec.execute(new Runnable(){
			public void run(){
				plugin.sql.AddMSG(player, msg, uuid);
				plugin.ShowMsgIfOnline(uuid);
			}
		});
	}
	public void CheckifLoanIsPayedThreaded(int loanid) {
		exec.execute(new Runnable(){
			public void run(){
				plugin.CheckifLoanIsPayed(loanid);
			}
		});
	}
	public void MarkTransAsSeenThreaded(Player p) {
		exec.execute(new Runnable(){
			public void run(){
				plugin.sql.MarkTransAsSeen(p);
			}
		});
	}
	public void MarkMSGAsSeenThreaded(Player player) {
		exec.execute(new Runnable(){
			public void run(){
				plugin.sql.MarkMSGAsSeen(player);
			}
		});
	}
	public void ShowMsgIfOnlineThreaded(UUID UUID) {
		exec.execute(new Runnable(){
			public void run(){
				plugin.ShowMsgIfOnline(UUID);
			}
		});
	}
	public void ShowTransIfOnlineThreaded(UUID UUID) {
		exec.execute(new Runnable(){
			public void run(){
				plugin.ShowTransIfOnline(UUID);
			}
		});
	}
	public void ListMSGThreaded(Player player) {
		exec.execute(new Runnable(){
			public void run(){
				plugin.sql.ListMSG(player);
			}
		});
	}
	public void MakeBankThreaded(Player player, String BankName, int interest, int maxloan, int fee) {
		exec.execute(new Runnable(){
			public void run(){
				plugin.sql.MakeBank(player, BankName, interest, maxloan, fee);
			}
		});
	}
	public void WithdrawThreaded(Player player, int BankID, int ammount2) {
		exec.execute(new Runnable(){
			public void run(){
				plugin.sql.Withdraw(player, BankID, ammount2);
			}
		});
	}
	public void DepositThreaded(Player player, int BankID, int ammount2) {
		exec.execute(new Runnable(){
			public void run(){
				plugin.sql.Deposit(player, BankID, ammount2);
			}
		});
	}
	public void DownpayThreaded(int code, int ammount2) {
		exec.execute(new Runnable(){
			public void run(){
				plugin.sql.Downpay(code,ammount2);
			}
		});
	}
	public void ReqLoanThreaded(Player player,String BankName,int interestrate , int ammount2, int fee) {
		exec.execute(new Runnable(){
			public void run(){
				plugin.sql.ReqLoan(player, BankName, interestrate, ammount2, fee);
			}
		});
	}
	public void DeleteLoanThreaded(int code) {
		exec.execute(new Runnable(){
			public void run(){
				plugin.sql.DeleteLoan(code);
			}
		});
	}
	public void MarkContractPayedThreaded(int loanid) {
		exec.execute(new Runnable(){
			public void run(){
				plugin.sql.MarkContractPayed(loanid);
			}
		});
	}
	public void AutochargeAndInterestThreaded() {
		exec.execute(new Runnable(){
			public void run(){
				FileConfiguration c = plugin.getConfig();
				boolean autocharge = c.getBoolean("interest.autocharge.enable");
				LogHandler.info("NEW DAY!");
				Bukkit.broadcastMessage(plugin.getMessager().get("Main.NewDay"));
				plugin.loanobject.AddInterestToAll();
				if(autocharge){
					plugin.loanobject.DownPayForAll();	
				}
			}
		});
	}
	public void closeexec(){
		exec.shutdown();
		
	}
}
