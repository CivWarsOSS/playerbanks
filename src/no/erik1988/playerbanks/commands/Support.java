package no.erik1988.playerbanks.commands;

import java.text.SimpleDateFormat;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import no.erik1988.playerbanks.LogHandler;
import no.erik1988.playerbanks.Main;

public class Support {
	private Main plugin;
	public Support(Main plugin)
	{
		this.plugin = plugin;
	}
	public boolean pardonLoan(int loanId, Player p, int source){
		String[] info = plugin.sql.GetLoanInfo(loanId);
		int borrowed = Integer.parseInt(info[3]);
		UUID borroweruuid = UUID.fromString(info[8]);
		int bankid = Integer.parseInt(info[11]);
		OfflinePlayer borrowerplayer = Bukkit.getOfflinePlayer(borroweruuid);
		String timestamp = new SimpleDateFormat("dd.MM.yy").format(System.currentTimeMillis());

		String msg = plugin.getMessager().get("borrow.Pardoned").replace("%money%", Integer.toString(borrowed)).replace("%player%", p.getName().toString()).replace("%borrower%", borrowerplayer.getName().toString()).replace("%time%", timestamp);



		plugin.sql.DeleteLoan(loanId);

		plugin.sql.AddMSG(borrowerplayer, msg, borroweruuid);
		plugin.ShowMsgIfOnline(borroweruuid);
		String bankmanger = "Bankmanager";
		if (source == 1){
			bankmanger = "Admin";
		}

		String log = plugin.getMessager().get("log.Pardoned").replace("%money%", Integer.toString(borrowed)).replace("%player%", p.getName().toString()).replace("%borrower%", borrowerplayer.getName().toString()).replace("%time%", timestamp);
		plugin.sql.AddLog(bankid, log);

		LogHandler.info("loanid: "+ loanId + " was deleted by "+ p.getName().toString() + " ("+ bankmanger +")");
		p.sendMessage(this.plugin.getMessager().get("Mybank.Contracts.DelSuccess").replace("%id%", Integer.toString(loanId)));
		return true;
	}
}
