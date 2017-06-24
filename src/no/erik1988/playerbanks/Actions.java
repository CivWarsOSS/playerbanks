package no.erik1988.playerbanks;

import java.text.SimpleDateFormat;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import net.milkbowl.vault.economy.Economy;

//this class process actions from signs and cmd.
public class Actions {
	private Main plugin;

	public Actions(Main plugin)
	{
		this.plugin = plugin;
	}
	public boolean TryBorrow(Player p, int amount, String bank) {
		FileConfiguration c = plugin.getConfig();
		int amount2 = amount;
		//int arg2 = Integer.parseInt(args[2]);
		if(amount2 <= 0){
			p.sendMessage(this.plugin.getMessager().get("cmd.Wrong"));
			p.sendMessage(this.plugin.getMessager().get("cmd.borrow"));
			return false;
		}
		int minborrow = c.getInt("loan.minBorrow.value");
		if(amount2 < minborrow){
			p.sendMessage(this.plugin.getMessager().get("cmd.MinBorrow").replace("%minborrow%", Integer.toString(minborrow)));
			return false;
		}
		String BankName = bank.toLowerCase(); 
		if (!plugin.sql.CheckIfBankNameExsist(BankName)) {
			p.sendMessage(this.plugin.getMessager().get("cmd.BankDoesNotExsist"));
			return false;
		}
		//p.sendMessage("Checking if owner..");
		if (plugin.sql.CheckIfOwner(p, BankName) || plugin.sql.CheckIfManager(p, BankName)) {
			p.sendMessage(this.plugin.getMessager().get("borrow.YouOwnThisBank"));
			return false;
		}
		if (plugin.sql.CheckIfLoanExsist(p, BankName)) {
			p.sendMessage(this.plugin.getMessager().get("borrow.AlreadyLoan"));
			return false;
		}
		//check maximum allowed contracts

		String mcUp = c.getString("loan.maxBorrow.unlimited.perm.maxcontracts");
		if(!p.hasPermission(mcUp)) {
			int nrcontracts = plugin.sql.CountLoansByPlayer(p);
			String mc3p = c.getString("loan.maxBorrow.group3.perm");
			String mc2p = c.getString("loan.maxBorrow.group2.perm");
			//String mc1p = c.getString("loan.maxBorrow.group1.perm");

			if(p.hasPermission(mc3p)) {
				int mc3 = c.getInt("loan.maxBorrow.group3.maxcontracts");
				if(nrcontracts > mc3){
					p.sendMessage(plugin.getMessager().get("borrow.MaxContract").replace("%contracts%", Integer.toString(mc3)));
					return false;
				}
			}
			else if(p.hasPermission(mc2p)) {
				int mc2 = c.getInt("loan.maxBorrow.group2.maxcontracts");
				if(nrcontracts > mc2){
					p.sendMessage(plugin.getMessager().get("borrow.MaxContract").replace("%contracts%", Integer.toString(mc2)));
					return false;
				}
			}
			else {
				int mc1 = c.getInt("loan.maxBorrow.group1.maxcontracts");
				if(nrcontracts > mc1){
					p.sendMessage(plugin.getMessager().get("borrow.MaxContract").replace("%contracts%", Integer.toString(mc1)));
					return false;
				}
			}

		}


		String[] info = plugin.sql.GetBankInfo(BankName);
		//String owner = info[0];
		int interestrate = Integer.parseInt(info[1]);
		int value = Integer.parseInt(info[3]);
		int maxloan = Integer.parseInt(info[5]);
		UUID owneruuid = UUID.fromString(info[6]);
		int fee = Integer.parseInt(info[7]);
		int bankid = Integer.parseInt(info[8]);
		if (maxloan < amount2) {
			p.sendMessage(plugin.getMessager().get("borrow.MaxLoan").replace("%maxloan%", Integer.toString(maxloan)));
			return false;
		}
		//check how much the player is allowed to borrow.
		String groupUnlimitedperm = c.getString("loan.maxBorrow.unlimited.perm");
		if (!p.hasPermission(groupUnlimitedperm)) {
			String group3perm = c.getString("loan.maxBorrow.group3.perm");
			String group2perm = c.getString("loan.maxBorrow.group2.perm");
			if (p.hasPermission(group3perm)) {
				int group3 = c.getInt("loan.maxBorrow.group3.value");
				if(amount2 > group3){
					p.sendMessage(plugin.getMessager().get("borrow.MaxLoanPerm").replace("%maxloan%", Integer.toString(group3)));
					return false;
				}
			}
			else if (p.hasPermission(group2perm)) {
				int group2 = c.getInt("loan.maxBorrow.group2.value");
				if(amount2 > group2){
					p.sendMessage(plugin.getMessager().get("borrow.MaxLoanPerm").replace("%maxloan%", Integer.toString(group2)));
					return false;
				}
			}
			else {
				int group1 = c.getInt("loan.maxBorrow.group1.value");
				if(amount2 > group1){
					p.sendMessage(plugin.getMessager().get("borrow.MaxLoanPerm").replace("%maxloan%", Integer.toString(group1)));
					return false;
				}
			}
		}
		//checks if bank have enougth money.
		if (value < amount2) {
			p.sendMessage(plugin.getMessager().get("borrow.NotEnougthMoney"));
			return false;
		}

		plugin.sql.ReqLoan(p, BankName, interestrate, amount2, fee);
		p.sendMessage(plugin.getMessager().get("borrow.RequestSendt").replace("%money%", Integer.toString(amount2)).replace("%bank%", BankName));
		//OfflinePlayer Ownerplayer = Bukkit.getPlayer(owneruuid);
		//Player Ownerplayer = (Player) Bukkit.getOfflinePlayer(owneruuid);
		String timestamp = new SimpleDateFormat("dd.MM.yy").format(System.currentTimeMillis());
		String log = plugin.getMessager().get("log.Request").replace("%money%", Integer.toString(amount2)).replace("%player%", p.getName().toString()).replace("%time%", timestamp).replace("%bank%", BankName);

		plugin.sql.AddLog(bankid, log);

		//send owner and manager a notification that they have a new 
		plugin.SendMsgIfOnline(owneruuid,plugin.getMessager().get("Mybank.Contracts.New"));
		UUID managerUUID = plugin.sql.GetManagerUUID(BankName);
		if (managerUUID != null){
			plugin.SendMsgIfOnline(managerUUID,plugin.getMessager().get("Mybank.Contracts.New"));
		}
		return true;
}
	public boolean TryPay(Player p, int amount, int loanid) {
		//check if loan exsist AND if the player is the borrower. 
		if (!plugin.sql.CheckIfLoanExsistCodeBorrower(p, loanid)) {
			p.sendMessage(this.plugin.getMessager().get("Loans.Pay.NoloanWithId"));

			return false;
		}
		//p.sendMessage("Checking if enougth money..");
		if (!checkMoney(p, amount)) {
			return false;
		}
		//check how much is left of loan.
		int paymentsleft = plugin.sql.CheckMoneyLeftLoan(loanid);
		if (paymentsleft < amount) {
			amount = paymentsleft;
		}
		int bankid = plugin.sql.GetBankIDFromLoan(loanid);
		//plugin.st.DownpayThreaded(code,amount2);
		plugin.sql.Downpay(loanid,amount);
		Economy e = plugin.getEconomy();
		e.withdrawPlayer(p.getPlayer(), amount);
		//plugin.st.DepositThreaded(p, bankid, amount2);
		plugin.sql.Deposit(p, bankid, amount);
		//Integer BankID = plugin.sql.GetBankID(BankName);
		//plugin.sql.Deposit(p, BankID, amount2);
		//0.deposit
		//1.withdraw
		//2.borrow/loan
		//3.payment(manual)
		//4.interest
		//5.Autopayment
		int logtype = 3; 
		UUID uuid = p.getUniqueId();
		//plugin.st.MakeLogThreaded(logtype, code, amount2, p, bankid,1,uuid);
		plugin.LogTransPre(logtype, loanid, amount, p, bankid,1,uuid);

		p.sendMessage(plugin.getMessager().get("Loans.Downpayment").replace("%money%", Integer.toString(amount)).replace("%id%", Integer.toString(loanid)));

		//plugin.st.CheckifLoanIsPayedThreaded(code);
		plugin.CheckifLoanIsPayed(loanid);
		plugin.sql.MarkLoanAsMissed(loanid,2);
		return true;
		
	}
	private boolean checkMoney(Player p, double amount)
	{
		double money;
		Economy e = plugin.getEconomy();
		if (!e.hasAccount(p.getPlayer())) {
			plugin.getMessager().sendMessage("eco.AccNotExisting", p);
			return false;
		}
		money = e.getBalance(p.getPlayer());
		if (money < amount) {
			String moneyAsString = Double.toString(money);
			p.sendMessage(plugin.getMessager().get("eco.NotEnougthMoney").replace("%money%", moneyAsString));
			return false; 
		}
		return true;
	}
}
