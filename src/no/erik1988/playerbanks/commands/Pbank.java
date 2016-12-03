package no.erik1988.playerbanks.commands;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import net.milkbowl.vault.economy.Economy;
import no.erik1988.playerbanks.LogHandler;
import no.erik1988.playerbanks.Main;
import no.erik1988.playerbanks.PaginatedResult;
import no.erik1988.playerbanks.objects.PageObject;

public class Pbank 
implements CommandExecutor
{
	private Main plugin;
	public Pbank(Main plugin)
	{
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender s,
			Command command,
			String label,
			String[] args) {
		FileConfiguration c = plugin.getConfig();
		if ((s instanceof Player)) {
			Player p = (Player)s;

			if (command.getName().equalsIgnoreCase("pbank")) {
				if (!s.hasPermission("pbank.user")) {
					s.sendMessage("No Permissions");

					return false;
				}
				if (args.length == 0) {
					p.sendMessage(this.plugin.getMessager().get("Main.Divider"));
					p.sendMessage(this.plugin.getMessager().get("Main.MoreInfo"));
					if (s.hasPermission("pbank.makebank")) {
						p.sendMessage(this.plugin.getMessager().get("cmd.MakeBank"));
						p.sendMessage(this.plugin.getMessager().get("cmd.deposit"));
						p.sendMessage(this.plugin.getMessager().get("cmd.withdraw"));
						p.sendMessage(this.plugin.getMessager().get("cmd.me"));
						p.sendMessage(this.plugin.getMessager().get("cmd.contracts"));
						p.sendMessage(this.plugin.getMessager().get("cmd.reports"));
						p.sendMessage(this.plugin.getMessager().get("cmd.RemBank"));
						p.sendMessage(this.plugin.getMessager().get("cmd.managers"));
						p.sendMessage(this.plugin.getMessager().get("cmd.log"));
					}
					p.sendMessage(this.plugin.getMessager().get("cmd.list"));
					p.sendMessage(this.plugin.getMessager().get("cmd.info"));
					p.sendMessage(this.plugin.getMessager().get("cmd.borrow"));
					p.sendMessage(this.plugin.getMessager().get("cmd.loans"));
					p.sendMessage(this.plugin.getMessager().get("cmd.pay"));
					p.sendMessage(this.plugin.getMessager().get("cmd.transactions"));
					p.sendMessage(this.plugin.getMessager().get("cmd.msg"));

					return true;

				}
				if(args[0].equalsIgnoreCase("makebank") || args[0].equalsIgnoreCase("mb")){
					if (!s.hasPermission("pbank.makebank")) {
						s.sendMessage("No Permissions");

						return false;
					}
					if (args.length == 5) {
						String arg1 = args[1].replaceAll("[^a-zA-Z0-9]", "");
						String BankName = arg1.toLowerCase();
						if ((BankName.length() > 15 || BankName.length() < 3)){
							p.sendMessage(this.plugin.getMessager().get("cmd.NameLength"));
							return false;
						}

						if (!isInt(args[2]) || !isInt(args[3]) || !isInt(args[4])) {
							p.sendMessage(this.plugin.getMessager().get("cmd.Wrong"));
							p.sendMessage(this.plugin.getMessager().get("cmd.MakeBank"));
							p.sendMessage(this.plugin.getMessager().get("eg.MakeBank"));
							return false;
						}
						int interest = Integer.parseInt(args[2]);
						if (interest > 3){
							p.sendMessage(this.plugin.getMessager().get("cmd.Wrong"));
							p.sendMessage(this.plugin.getMessager().get("cmd.MakeBank"));
							p.sendMessage(this.plugin.getMessager().get("eg.MakeBank"));
							return false;
						}
						int fee = Integer.parseInt(args[4]);
						int maxfee = c.getInt("loan.maxfee");
						if (fee > maxfee){
							p.sendMessage(this.plugin.getMessager().get("MakeBank.maxfee").replace("%nr%", Integer.toString(maxfee)));
							return false;
						}

						if (isBlackListed(arg1)) {
							p.sendMessage(this.plugin.getMessager().get("cmd.BlackList"));
							return false;
						}

						if (plugin.sql.CheckIfBankNameExsist(BankName)) {
							p.sendMessage(this.plugin.getMessager().get("MakeBank.NameAlreadyExsist"));
							return false;
						}
						int banksallowed = c.getInt("bank.banksPerPlayer");
						if (plugin.sql.CountBanksByPlayer(p) >= banksallowed) {
							p.sendMessage(this.plugin.getMessager().get("MakeBank.ToManyBanks").replace("%nr%", Integer.toString(banksallowed)));
							return false;
						}

						int maxloan = Integer.parseInt(args[3]); 
						int minborrow = c.getInt("loan.minBorrow.value");
						if(maxloan < minborrow){
							p.sendMessage(this.plugin.getMessager().get("MakeBank.minborrow").replace("%minborrow%", Integer.toString(minborrow)));
							return false;
						}

						plugin.sql.MakeBank(p, BankName, interest, maxloan, fee);
						s.sendMessage(plugin.getMessager().get("Mybank.Bank.Created").replace("%bank%", BankName));
						s.sendMessage(plugin.getMessager().get("cmd.deposit").replace("BANK", BankName));
						
						int bankid = plugin.sql.GetBankID(BankName);
						String timestamp = new SimpleDateFormat("dd.MM.yy").format(System.currentTimeMillis());
						String log = plugin.getMessager().get("log.BankMade").replace("%time%", timestamp).replace("%bank%", BankName);


						plugin.sql.AddLog(bankid, log);						

						return true;
					}
					if (args.length == 1) {
						String low = c.getString("interest.low")+ "%";
						String med = c.getString("interest.med")+ "%";
						String hi = c.getString("interest.high")+ "%";
						p.sendMessage(this.plugin.getMessager().get("Main.Divider"));
						p.sendMessage(this.plugin.getMessager().get("desc.MakeBank0"));
						p.sendMessage(this.plugin.getMessager().get("desc.MakeBank1"));
						p.sendMessage(this.plugin.getMessager().get("desc.MakeBank2").replace("%low%", low).replace("%med%", med).replace("%hi%", hi));
						p.sendMessage(this.plugin.getMessager().get("desc.MakeBank3"));
						p.sendMessage(this.plugin.getMessager().get("desc.MakeBank4"));
						p.sendMessage(this.plugin.getMessager().get("cmd.MakeBank"));
						p.sendMessage(this.plugin.getMessager().get("eg.MakeBank"));
						return true;
					}
					p.sendMessage(this.plugin.getMessager().get("cmd.Wrong"));
					p.sendMessage(this.plugin.getMessager().get("cmd.MakeBank"));
				}
				//pbank d BANK AMOUNT
				if(args[0].equalsIgnoreCase("deposit")|| args[0].equalsIgnoreCase("d")){
					if (!s.hasPermission("pbank.makebank")) {
						s.sendMessage("No Permissions");

						return false;
					}
					if (args.length == 3) {
						if (!isInt(args[2])) {
							p.sendMessage(this.plugin.getMessager().get("cmd.Wrong"));
							p.sendMessage(this.plugin.getMessager().get("cmd.deposit"));
							return false;
						}
						int arg2 = Integer.parseInt(args[2]);
						if(arg2 <= 0){
							p.sendMessage(this.plugin.getMessager().get("cmd.Wrong"));
							p.sendMessage(this.plugin.getMessager().get("cmd.deposit"));
							return false;
						}
						String BankName = args[1].toLowerCase(); 
						if (!plugin.sql.CheckIfBankNameExsist(BankName)) {
							p.sendMessage(this.plugin.getMessager().get("cmd.BankDoesNotExsist"));
							return false;
						}
						//p.sendMessage("Checking if owner..");
						if (!plugin.sql.CheckIfOwner(p, BankName) && !plugin.sql.CheckIfManager(p, BankName)) {
							p.sendMessage(this.plugin.getMessager().get("cmd.YouCannotManage"));
							return false;
						}
						//p.sendMessage("Checking if enougth money..");
						int amount2 = Integer.parseInt(args[2]);
						if (!checkMoney(p, amount2)) {
							return false;
						}

						Economy e = plugin.getEconomy();
						e.withdrawPlayer(p.getPlayer(), amount2);
						Integer BankID = plugin.sql.GetBankID(BankName);
						//plugin.st.DepositThreaded(p, BankID, amount2);
						plugin.sql.Deposit(p, BankID, amount2);
						//0.deposit
						//1.withdraw
						//2.borrow/loan
						//3.payment(manual)
						//4.interest
						//5.Autopayment
						int logtype = 0; 
						int code = 0; 
						UUID uuid = p.getUniqueId();
						//plugin.st.MakeLogThreaded(logtype, code, amount2, p, BankID,1,uuid);
						plugin.LogTransPre(logtype, code, amount2, p, BankID,1,uuid);
						
						String amountasstring = Integer.toString(amount2);
						p.sendMessage(plugin.getMessager().get("eco.TransferedToBank").replace("%money%", amountasstring));
						return true;
					}
					p.sendMessage(this.plugin.getMessager().get("desc.Deposit"));
					p.sendMessage(this.plugin.getMessager().get("cmd.deposit"));
					return false;
				}
				//pbank w BANK AMOUNT
				if(args[0].equalsIgnoreCase("withdraw") || args[0].equalsIgnoreCase("w")){
					if (!s.hasPermission("pbank.makebank")) {
						s.sendMessage("No Permissions");

						return false;
					}
					if (args.length == 3) {
						if (!isInt(args[2])) {
							p.sendMessage(this.plugin.getMessager().get("cmd.Wrong"));
							p.sendMessage(this.plugin.getMessager().get("cmd.withdraw"));
							return false;
						}
						int arg2 = Integer.parseInt(args[2]);
						if(arg2 <= 0){
							p.sendMessage(this.plugin.getMessager().get("cmd.Wrong"));
							p.sendMessage(this.plugin.getMessager().get("cmd.withdraw"));
							return false;
						}

						String BankName = args[1].toLowerCase(); 
						if (!plugin.sql.CheckIfBankNameExsist(BankName)) {
							p.sendMessage(this.plugin.getMessager().get("cmd.BankDoesNotExsist"));
							return false;
						}
						//p.sendMessage("Checking if owner..");
						if (!plugin.sql.CheckIfOwner(p, BankName)) {
							p.sendMessage(this.plugin.getMessager().get("cmd.YouDontOwnThisBank"));
							return false;
						}
						int amount2 = Integer.parseInt(args[2]);
						if (!plugin.sql.CheckMoneyBank(p, BankName,amount2)) {
							p.sendMessage(plugin.getMessager().get("eco.NotEnougthMoney"));
							return false;
						}
						Integer BankID = plugin.sql.GetBankID(BankName);
						//plugin.st.WithdrawThreaded(p, BankID, amount2);
						plugin.sql.Withdraw(p, BankID, amount2);
						Economy e = plugin.getEconomy();
						e.depositPlayer(p.getPlayer(), amount2);
						//0.deposit
						//1.withdraw
						//2.borrow/loan
						//3.payment(manual)
						//4.interest
						//5.Autopayment
						int logtype = 1; 
						int code = 0; 
						UUID uuid = p.getUniqueId();
						//plugin.st.MakeLogThreaded(logtype, code, amount2, p, BankID,1,uuid);
						plugin.LogTransPre(logtype, code, amount2, p, BankID,1,uuid);
						String amountasstring = Integer.toString(amount2);
						p.sendMessage(plugin.getMessager().get("eco.TransferedFromBank").replace("%money%", amountasstring));
						return true;
					}
					p.sendMessage(this.plugin.getMessager().get("desc.Withdraw"));
					p.sendMessage(this.plugin.getMessager().get("cmd.withdraw"));
					return false;
				}
				//pbank rem BANK
				if(args[0].equalsIgnoreCase("rem") || args[0].equalsIgnoreCase("remove")){
					if (!s.hasPermission("pbank.makebank")) {
						s.sendMessage("No Permissions");

						return false;
					}
					if (args.length == 2) {

						String BankName = args[1].toLowerCase(); 
						if (!plugin.sql.CheckIfBankNameExsist(BankName)) {
							p.sendMessage(this.plugin.getMessager().get("cmd.BankDoesNotExsist"));
							return false;
						}
						//p.sendMessage("Checking if owner..");
						if (!plugin.sql.CheckIfOwner(p, BankName)) {
							p.sendMessage(this.plugin.getMessager().get("cmd.YouDontOwnThisBank"));
							return false;
						}
						//check if zero
						if (!plugin.sql.CheckMoneyBankEmpty(BankName)) {
							p.sendMessage(plugin.getMessager().get("Mybank.Remove.NotEmpty"));
							return false;
						}
						Integer BankID = plugin.sql.GetBankID(BankName);
						//check if no active loans.
						if (plugin.sql.CheckIfBankHasLoans(BankID)) {
							p.sendMessage(this.plugin.getMessager().get("Mybank.Remove.HasActiveLoans"));
							return false;
						}
						plugin.sql.RemBank(BankID);
						plugin.sql.RemAllLoansFromBank(BankID);
						LogHandler.info("Bank " + BankName + "was removed by " + p.getName().toString());
						p.sendMessage(plugin.getMessager().get("Mybank.Remove.RemSuccess").replace("%bank%", BankName));
						return true;
					}
					p.sendMessage(this.plugin.getMessager().get("desc.RemBank"));
					p.sendMessage(this.plugin.getMessager().get("cmd.RemBank"));
					
					return false;
				}
				//pbank b BANK AMOUNT
				if(args[0].equalsIgnoreCase("borrow") || args[0].equalsIgnoreCase("b")){
					if (args.length == 3) {
						if (!isInt(args[2])) {
							p.sendMessage(this.plugin.getMessager().get("cmd.Wrong"));
							p.sendMessage(this.plugin.getMessager().get("cmd.borrow"));
							return false;
						}
						int amount2 = Integer.parseInt(args[2]);
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
						String BankName = args[1].toLowerCase(); 
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
						String owner = info[0];
						int interestrate = Integer.parseInt(info[1]);
						//int inviteonly = Integer.parseInt(info[2],0);
						int value = Integer.parseInt(info[3]);
						//double recivable = Integer.parseInt(info[4]);
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
						if (!s.hasPermission(groupUnlimitedperm)) {
							String group3perm = c.getString("loan.maxBorrow.group3.perm");
							String group2perm = c.getString("loan.maxBorrow.group2.perm");
							if (s.hasPermission(group3perm)) {
								int group3 = c.getInt("loan.maxBorrow.group3.value");
								if(amount2 > group3){
									p.sendMessage(plugin.getMessager().get("borrow.MaxLoanPerm").replace("%maxloan%", Integer.toString(group3)));
									return false;
								}
							}
							else if (s.hasPermission(group2perm)) {
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
						/* Inviteonly is currently an unused feature.
						if (inviteonly == 1)
						{
							p.sendMessage(plugin.getMessager().get("borrow.InviteOnly"));
							return false;
						}
						*/
						//plugin.st.ReqLoanThreaded(p, BankName, interestrate, amount2, fee);
						plugin.sql.ReqLoan(p, BankName, interestrate, amount2, fee);
						p.sendMessage(plugin.getMessager().get("borrow.RequestSendt").replace("%money%", Integer.toString(amount2)).replace("%bank%", BankName));
						//OfflinePlayer Ownerplayer = Bukkit.getPlayer(owneruuid);
						//Player Ownerplayer = (Player) Bukkit.getOfflinePlayer(owneruuid);
						String timestamp = new SimpleDateFormat("dd.MM.yy").format(System.currentTimeMillis());
						String msg = plugin.getMessager().get("log.Request").replace("%money%", Integer.toString(amount2)).replace("%player%", p.getName().toString()).replace("%time%", timestamp).replace("%bank%", BankName);
						//TODO: Check if log.request should be replaced.

						plugin.sql.AddLog(bankid, msg);

						
						
						UUID managerUUID = plugin.sql.GetManagerUUID(BankName);
						plugin.SendMsgIfOnline(owneruuid, msg);
						if (managerUUID != null){
							plugin.SendMsgIfOnline(managerUUID, msg);
						}
						return true;

					}
					p.sendMessage(this.plugin.getMessager().get("desc.Borrow"));
					p.sendMessage(this.plugin.getMessager().get("cmd.borrow"));
					return false;
				}
				//pbank pay LOANID AMOUNT
				if(args[0].equalsIgnoreCase("pay")|| args[0].equalsIgnoreCase("p")){
					if (args.length == 3) {
						if (!isInt(args[1]) || !isInt(args[2])) {
							p.sendMessage(this.plugin.getMessager().get("cmd.Wrong"));
							p.sendMessage(this.plugin.getMessager().get("cmd.pay"));
							p.sendMessage(this.plugin.getMessager().get("eg.LoanId"));
							return false;
						}
						int arg2 = Integer.parseInt(args[2]);
						int arg1 = Integer.parseInt(args[1]);
						if(arg2 <= 0 || arg1 <= 0){
							p.sendMessage(this.plugin.getMessager().get("cmd.Wrong"));
							p.sendMessage(this.plugin.getMessager().get("cmd.pay"));
							return false;
						}
						int code = Integer.parseInt(args[1]); 
						//check if loan exsist AND if the player is the borrower. 
						if (!plugin.sql.CheckIfLoanExsistCodeBorrower(p, code)) {
							p.sendMessage(this.plugin.getMessager().get("Loans.Pay.NoloanWithId"));

							return false;
						}
						//p.sendMessage("Checking if enougth money..");
						int amount2 = Integer.parseInt(args[2]);
						if (!checkMoney(p, amount2)) {
							return false;
						}
						//check how much is left of loan.
						int paymentsleft = plugin.sql.CheckMoneyLeftLoan(code);
						if (paymentsleft < amount2) {
							amount2 = paymentsleft;
						}
						int bankid = plugin.sql.GetBankIDFromLoan(code);
						//plugin.st.DownpayThreaded(code,amount2);
						plugin.sql.Downpay(code,amount2);
						Economy e = plugin.getEconomy();
						e.withdrawPlayer(p.getPlayer(), amount2);
						//plugin.st.DepositThreaded(p, bankid, amount2);
						plugin.sql.Deposit(p, bankid, amount2);
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
						plugin.LogTransPre(logtype, code, amount2, p, bankid,1,uuid);
						plugin.ShowTransIfOnline(uuid);
						
						p.sendMessage(plugin.getMessager().get("Loans.Downpayment").replace("%money%", Integer.toString(amount2)).replace("%id%", Integer.toString(code)));
						
						//plugin.st.CheckifLoanIsPayedThreaded(code);
						plugin.CheckifLoanIsPayed(code);
						return true;
					}
					p.sendMessage(this.plugin.getMessager().get("desc.Pay"));
					p.sendMessage(this.plugin.getMessager().get("cmd.pay"));
					return false;

				}
				if(args[0].equalsIgnoreCase("me")){
					
					List<PageObject> loglist = plugin.sql.MyBanks(p);
					if(loglist.isEmpty()){
						p.sendMessage(this.plugin.getMessager().get("Mybank.Bank.Null"));
						return false;
					}
					int arg1 = 1;
					if (args.length == 2) {
						if (!isInt(args[1])) {
							p.sendMessage(this.plugin.getMessager().get("cmd.Wrong"));
							return false;
						}
						arg1 = Integer.parseInt(args[1]);
					}
					new PaginatedResult<PageObject>(plugin.getMessager().get("Mybank.Bank.Header"),plugin.getMessager().get("info.bankinfo")){

						@Override
						public String format(PageObject entry) {
							return entry.getmsg();
						}
						
					}.display(p, loglist, arg1);
					
					return true;
				}
				
				if(args[0].equalsIgnoreCase("msg")){
					int arg1 = 1;
					if (args.length == 2) {
						if (!isInt(args[1])) {
							p.sendMessage(this.plugin.getMessager().get("cmd.Wrong"));
							return false;
						}
						arg1 = Integer.parseInt(args[1]);
					}

					List<PageObject> loglist = plugin.sql.ListMSGAll(p);
					if(loglist.isEmpty()){
						p.sendMessage(this.plugin.getMessager().get("msg.null"));
						return false;
					}
					new PaginatedResult<PageObject>(plugin.getMessager().get("msg.msg.head").replace("%player%", p.getDisplayName()),""){

						@Override
						public String format(PageObject entry) {
							return entry.getmsg();
						} 
					}.display(p, loglist, arg1);
					return true;

				}
				//pbank trans [bank] [page]
				if(args[0].equalsIgnoreCase("transactions") || args[0].equalsIgnoreCase("trans")){
					if (args.length >= 2 && !isInt(args[1])) {
						String BankName = args[1].toLowerCase(); 
						if (!plugin.sql.CheckIfBankNameExsist(BankName)) {
							p.sendMessage(this.plugin.getMessager().get("cmd.BankDoesNotExsist"));
							return false;
						}
						//p.sendMessage("Checking if owner..");
						if (!plugin.sql.CheckIfOwner(p, BankName) && !plugin.sql.CheckIfManager(p, BankName)) {
							p.sendMessage(this.plugin.getMessager().get("cmd.YouDontOwnThisBank"));
							return false;
						} 
						Integer bankid = plugin.sql.GetBankID(BankName);
						int arg2 = 1;
						if (args.length == 3) {
							if (!isInt(args[2])) {
								p.sendMessage(this.plugin.getMessager().get("cmd.Wrong"));
								return false;
							}
							arg2 = Integer.parseInt(args[2]);
						}

						List<PageObject> loglist = plugin.sql.GetTransBank(p, bankid);
						if(loglist.isEmpty()){
							p.sendMessage(this.plugin.getMessager().get("transactions.null"));
							return false;
						}
						new PaginatedResult<PageObject>("Transactions for " + BankName + " Bank",this.plugin.getMessager().get("desc.Log")){

							@Override
							public String format(PageObject entry) {
								return entry.getmsg();
							}
						}.display(p, loglist, arg2);
						return true;
					}
					int arg1 = 1;
					if (args.length == 2) {
						if (!isInt(args[1])) {
							p.sendMessage(this.plugin.getMessager().get("cmd.Wrong"));
							return false;
						}
						arg1 = Integer.parseInt(args[1]);
					}
					List<PageObject> loglist = plugin.sql.GetTransPlayer(p);
					if(loglist.isEmpty()){
						p.sendMessage(this.plugin.getMessager().get("transactions.null"));
						return false;
					}
					new PaginatedResult<PageObject>("Transactions for " + p.getDisplayName() + "",""){

						@Override
						public String format(PageObject entry) {
							return entry.getmsg();
						}
					}.display(p, loglist, arg1);
					return true;
					
				}
				//pbank log BANK [page]
				if(args[0].equalsIgnoreCase("log")){
					if (args.length >= 2) {
						String BankName = args[1].toLowerCase(); 
						if (!plugin.sql.CheckIfBankNameExsist(BankName)) {
							p.sendMessage(this.plugin.getMessager().get("cmd.BankDoesNotExsist"));
							return false;
						}
						//p.sendMessage("Checking if owner..");
						if (!plugin.sql.CheckIfOwner(p, BankName) && !plugin.sql.CheckIfManager(p, BankName)) {
							p.sendMessage(this.plugin.getMessager().get("cmd.YouDontOwnThisBank"));
							return false;
						} 

						int arg2 = 1;
						if (args.length == 3) {
							if (!isInt(args[2])) {
								p.sendMessage(this.plugin.getMessager().get("cmd.Wrong"));
								return false;
							}
							arg2 = Integer.parseInt(args[2]);
						}

						Integer BankID = plugin.sql.GetBankID(BankName);
						List<PageObject> loglist = plugin.sql.GetLogObject(BankID);
						if(loglist.isEmpty()){
							p.sendMessage(this.plugin.getMessager().get("log.null"));
							return false;
						}
						new PaginatedResult<PageObject>(this.plugin.getMessager().get("log.Header").replace("%bank%", BankName),this.plugin.getMessager().get("desc.Trans")){

							@Override
							public String format(PageObject entry) {
								return entry.getmsg();
							}
						}.display(p, loglist, arg2);
						return true;
					}
					p.sendMessage(this.plugin.getMessager().get("cmd.log"));
					return false;
					
				}

				//pbank manager BANK [add|clear] [PLAYER]
				if(args[0].equalsIgnoreCase("manager") || args[0].equalsIgnoreCase("man")){
					if (args.length >= 2) {
						String BankName = args[1].toLowerCase(); 
						if (!plugin.sql.CheckIfBankNameExsist(BankName)) {
							p.sendMessage(this.plugin.getMessager().get("cmd.BankDoesNotExsist"));
							return false;
						}
						if (args.length >= 3) {
						if(args[2].equalsIgnoreCase("clear")){
							if (!plugin.sql.CheckIfOwner(p, BankName)) {
								p.sendMessage(this.plugin.getMessager().get("cmd.YouDontOwnThisBank"));
								return false;
							}
							//remove manager
							plugin.sql.ClearManager(BankName);
							p.sendMessage(this.plugin.getMessager().get("Mybank.Manager.Clear"));
							return true;
						}
						
						if (args.length == 4) {
							if(args[2].equalsIgnoreCase("add")){
								if (!plugin.sql.CheckIfOwner(p, BankName)) {
									p.sendMessage(this.plugin.getMessager().get("cmd.YouDontOwnThisBank"));
									return false;
								}
								//add manager

								String managername = args[3];

								OfflinePlayer managerplayer = FindPlayerByName(managername);
							    if (managerplayer == null || !managerplayer.hasPlayedBefore()){
							    	p.sendMessage(this.plugin.getMessager().get("Mybank.Manager.UnknownPlayer"));
							    	return false;
							    }
							    if (managerplayer == p){
							    	p.sendMessage(this.plugin.getMessager().get("Mybank.Manager.NotYourself"));
							    	return false;
							    }
								if (plugin.sql.CheckIfLoanExsist(managerplayer, BankName)) {
									p.sendMessage(this.plugin.getMessager().get("Mybank.Manager.PlayerHasLoan"));
									return false;
								}
							    
							    String mname = managerplayer.getName();
								plugin.sql.AddManager(managerplayer,BankName);
								p.sendMessage(this.plugin.getMessager().get("Mybank.Manager.Added").replace("%player%", mname));
								return true;
							}

						}
						}
						//list manager
						String manager2 = plugin.sql.ListManager(BankName);
						if (manager2 == null){
							p.sendMessage(this.plugin.getMessager().get("Mybank.Manager.NoManager"));
							return false;
						}
						p.sendMessage(this.plugin.getMessager().get("Mybank.Manager.List").replace("%player%", manager2));
						return true;
						

					}
					p.sendMessage(this.plugin.getMessager().get("Mybank.Manager.Manager"));
					p.sendMessage(this.plugin.getMessager().get("Mybank.Manager.CanDo"));
					p.sendMessage(this.plugin.getMessager().get("Mybank.Manager.CanNotDo"));
					p.sendMessage(this.plugin.getMessager().get("cmd.managers"));
					return false;
				}
				
				//admin cmd bellow
				if(args[0].equalsIgnoreCase("cleanup")){
					if (!s.hasPermission("pbank.admin")) {
						s.sendMessage("No Permissions");
						return false;
					}
					//plugin.st.cleanUpThreaded();
					plugin.cleanUp();
					return true;
				}
				//pbank c [app|del] [id]
				if(args[0].equalsIgnoreCase("contracts") || args[0].equalsIgnoreCase("c")){
					if (args.length >= 2 ) {
						if(args[1].equalsIgnoreCase("approve") || args[1].equalsIgnoreCase("app")){
							if (args.length == 3) {
								if (!isInt(args[2])) {
									p.sendMessage(this.plugin.getMessager().get("cmd.Wrong"));
									p.sendMessage(this.plugin.getMessager().get("eg.LoanId"));
									return false;
								}
								int code = Integer.parseInt(args[2]);
								if(!plugin.sql.CheckIfLoanExsistCodeManage(p,code)){
									p.sendMessage(this.plugin.getMessager().get("Mybank.Contracts.CantfindContract"));
									return false;
								}
								String[] info = plugin.sql.GetLoanInfo(code);
								//String borrower = info[0];
								//int interestrate = Integer.parseInt(info[1]);
								//double interest = Integer.parseInt(info[2]);
								int borrowed = Integer.parseInt(info[3]);
								//double payments = Integer.parseInt(info[4]);
								int active = Integer.parseInt(info[5]);
								//long timestamp = Long.parseLong(info[6]);
								int value = Integer.parseInt(info[7]);

								UUID borroweruuid = UUID.fromString(info[8]);
								//int fee = Integer.parseInt(info[9]);
								int bankid = Integer.parseInt(info[11]);
								OfflinePlayer borrowerplayer = Bukkit.getOfflinePlayer(borroweruuid);


								if(active == 1){
									p.sendMessage(this.plugin.getMessager().get("Mybank.Contracts.AlreadyActive"));
									return false;
								}
								if(borrowed > value){
									p.sendMessage(this.plugin.getMessager().get("Mybank.Contracts.NotEnougthMoney"));
									return false;
								}
								Economy e = plugin.getEconomy();
								if (!e.hasAccount(borrowerplayer)) {
									plugin.getMessager().sendMessage("eco.AccNotExisting", p);
									return false;
								}

								//plugin.st.WithdrawThreaded(p, code, borrowed);
								plugin.sql.Withdraw(p, code, borrowed);

								e.depositPlayer(borrowerplayer, borrowed);
								plugin.sql.MarkContractActive(p,code);
								//0.deposit
								//1.withdraw
								//2.borrow/loan
								//3.payment(manual)
								//4.interest
								//5.Autopayment
								int logtype = 2; 
								//plugin.st.MakeLogThreaded(logtype, code, borrowed, borrowerplayer, bankid,0,borroweruuid);
								plugin.LogTransPre(logtype, code, borrowed, borrowerplayer, bankid,0,borroweruuid);

								p.sendMessage(this.plugin.getMessager().get("Mybank.Contracts.Acitvated").replace("%contract%", Integer.toString(code)));
								String timestamp = new SimpleDateFormat("dd.MM.yy").format(System.currentTimeMillis());
							 
								String msg = plugin.getMessager().get("borrow.Approved").replace("%money%", Integer.toString(borrowed)).replace("%player%", p.getName().toString()).replace("%borrower%", borrowerplayer.getName()).replace("%time%", timestamp);
								//plugin.st.AddMSGThreaded(borrowerplayer, msg,borroweruuid);
								plugin.sql.AddMSG(borrowerplayer, msg,borroweruuid);
								plugin.ShowMsgIfOnline(borroweruuid);
								plugin.ShowTransIfOnline(borroweruuid);
								
								String log = plugin.getMessager().get("borrow.Approved").replace("%money%", Integer.toString(borrowed)).replace("%player%", p.getName().toString()).replace("%borrower%", borrowerplayer.getName()).replace("%time%", timestamp);
								plugin.sql.AddLog(bankid, log);
								
								return true;
							}


						}
						if(args[1].equalsIgnoreCase("del") || args[1].equalsIgnoreCase("deny")){
							if (args.length >= 3) {
								if (!isInt(args[2])) {
									p.sendMessage(this.plugin.getMessager().get("cmd.Wrong"));
									p.sendMessage(this.plugin.getMessager().get("eg.LoanId"));
									return false;
								}
								int code = Integer.parseInt(args[2]);
								if(!plugin.sql.CheckIfLoanExsistCodeOwner(p,code)){
									p.sendMessage(this.plugin.getMessager().get("Mybank.Contracts.CantfindContract"));
									return false;
								}
								if (args.length == 4) {
									if (args[3].equalsIgnoreCase("-f")) {
										String[] info = plugin.sql.GetLoanInfo(code);
										int borrowed = Integer.parseInt(info[3]);
										UUID borroweruuid = UUID.fromString(info[8]);
										int bankid = Integer.parseInt(info[11]);
										OfflinePlayer borrowerplayer = Bukkit.getOfflinePlayer(borroweruuid);
										String timestamp = new SimpleDateFormat("dd.MM.yy").format(System.currentTimeMillis());
										
										String msg = plugin.getMessager().get("borrow.Pardoned").replace("%money%", Integer.toString(borrowed)).replace("%player%", p.getName().toString()).replace("%borrower%", borrowerplayer.getName().toString()).replace("%time%", timestamp);
										
										
										
										plugin.sql.DeleteLoan(code);

										plugin.sql.AddMSG(borrowerplayer, msg, borroweruuid);
										plugin.ShowMsgIfOnline(borroweruuid);
										
										String log = plugin.getMessager().get("log.Pardoned").replace("%money%", Integer.toString(borrowed)).replace("%player%", p.getName().toString()).replace("%borrower%", borrowerplayer.getName().toString()).replace("%time%", timestamp);
										plugin.sql.AddLog(bankid, log);
										
										LogHandler.info("loanid: "+ code + " was deleted by "+ p.getName().toString() + " (bankmanager)");
										p.sendMessage(this.plugin.getMessager().get("Mybank.Contracts.DelSuccess").replace("%id%", Integer.toString(code)));
										return true;

									}
									return false;
									
								}
								if(plugin.sql.CheckIfLoanIsActive(code)){
									p.sendMessage(this.plugin.getMessager().get("Mybank.Contracts.DelIsActive").replace("%id%", Integer.toString(code)));
									return false;
								}

								String[] info = plugin.sql.GetLoanInfo(code);
								int borrowed = Integer.parseInt(info[3]);
								UUID borroweruuid = UUID.fromString(info[8]);
								int bankid = Integer.parseInt(info[11]);
								OfflinePlayer borrowerplayer = Bukkit.getOfflinePlayer(borroweruuid);
								
								String timestamp = new SimpleDateFormat("dd.MM.yy").format(System.currentTimeMillis());
								String msg = plugin.getMessager().get("borrow.Denied").replace("%money%", Integer.toString(borrowed)).replace("%player%", p.getName().toString()).replace("%borrower%", borrowerplayer.getName().toString()).replace("%time%", timestamp);
								//plugin.st.AddMSGThreaded(borrowerplayer, msg,borroweruuid);
								plugin.sql.AddMSG(borrowerplayer, msg,borroweruuid);
								
								String log = plugin.getMessager().get("log.Denied").replace("%money%", Integer.toString(borrowed)).replace("%player%", p.getName().toString()).replace("%borrower%", borrowerplayer.getName().toString()).replace("%time%", timestamp);
								plugin.sql.AddLog(bankid, log);
								
								plugin.ShowMsgIfOnline(borroweruuid);

								plugin.sql.DeleteLoan(code);
								LogHandler.info("loanid: "+ code + " was deleted by "+ p.getName().toString() + " (bankmanager)");
								p.sendMessage(this.plugin.getMessager().get("Mybank.Contracts.DelSuccess").replace("%id%", Integer.toString(code)));
								return true;
							}

						}
						if(args[1].equalsIgnoreCase("info") || args[1].equalsIgnoreCase("i")){
							if (args.length == 3) {
								if (!isInt(args[2])) {
									p.sendMessage(this.plugin.getMessager().get("cmd.Wrong"));
									return false;
								}
								int code = Integer.parseInt(args[2]);
								if(!plugin.sql.CheckIfLoanExsistCodeManage(p,code)){
									p.sendMessage(this.plugin.getMessager().get("Mybank.Contracts.CantfindContract"));
									return false;
								}
								//info about contract.
								return true;
							}
						}
					}
					List<PageObject> loglist = plugin.sql.ListContracts(p);
					if(loglist.isEmpty()){
						p.sendMessage(this.plugin.getMessager().get("Mybank.Contracts.Null"));
						return false;
					}
					int arg1 = 1;
					if (args.length == 2) {
						if (!isInt(args[1])) {
							p.sendMessage(this.plugin.getMessager().get("cmd.Wrong"));
							return false;
						}
						arg1 = Integer.parseInt(args[1]);
					}
					new PaginatedResult<PageObject>(plugin.getMessager().get("Mybank.Contracts.Header"),plugin.getMessager().get("cmd.contracts")){

						@Override
						public String format(PageObject entry) {
							return entry.getmsg();
						}
						
					}.display(p, loglist, arg1);
					
					return true;
				}
				if(args[0].equalsIgnoreCase("loans") || args[0].equalsIgnoreCase("requests")){
					if (args.length == 3){
						if(args[1].equalsIgnoreCase("del")){
							if (!isInt(args[2])) {
								p.sendMessage(this.plugin.getMessager().get("cmd.Wrong"));
								p.sendMessage(this.plugin.getMessager().get("eg.LoanId"));
								return false;
							}
							int code = Integer.parseInt(args[2]);
							if(!plugin.sql.CheckIfLoanExsistCodeBorrower(p,code)){
								p.sendMessage(this.plugin.getMessager().get("Mybank.Contracts.CantfindContract"));
								return false;
							}
							if(plugin.sql.CheckIfLoanIsActive(code)){
								p.sendMessage(this.plugin.getMessager().get("Loans.Del.IsActive"));
								return false;
							}
							//plugin.st.DeleteLoanThreaded(code);
							plugin.sql.DeleteLoan(code);
							
							LogHandler.info("loanid: "+ code + " was deleted by "+ p.getName().toString()  + " (borrower)");
							p.sendMessage(this.plugin.getMessager().get("Mybank.Contracts.DelSuccess").replace("%id%", Integer.toString(code)));
							return true;
						}
					}
					List<PageObject> loglist = plugin.sql.ListRequests(p);
					if(loglist.isEmpty()){
						p.sendMessage(this.plugin.getMessager().get("borrow.Loans.Null"));
						return false;
					}
					int arg1 = 1;
					if (args.length == 2) {
						if (!isInt(args[1])) {
							p.sendMessage(this.plugin.getMessager().get("cmd.Wrong"));
							return false;
						}
						arg1 = Integer.parseInt(args[1]);
					}
					String footer = plugin.getMessager().get("cmd.loans") + "\n" + plugin.getMessager().get("cmd.pay");
					new PaginatedResult<PageObject>(plugin.getMessager().get("borrow.Loans.Header"),footer){

						@Override
						public String format(PageObject entry) {
							return entry.getmsg();
						}
						
					}.display(p, loglist, arg1);
					
					return true;
				}
				if(args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("l")){
					
					List<PageObject> loglist = plugin.sql.BankList(p);
					if(loglist.isEmpty()){
						p.sendMessage(this.plugin.getMessager().get("Banks.Bank.Null"));
						return false;
					}
					int arg1 = 1;
					if (args.length == 2) {
						if (!isInt(args[1])) {
							p.sendMessage(this.plugin.getMessager().get("cmd.Wrong"));
							return false;
						}
						arg1 = Integer.parseInt(args[1]);
					}

					new PaginatedResult<PageObject>(plugin.getMessager().get("Banks.Bank.Header"),plugin.getMessager().get("info.bankinfo")){

						@Override
						public String format(PageObject entry) {
							return entry.getmsg();
						}
						
					}.display(p, loglist, arg1);
					
					
					
					
					//p.sendMessage(plugin.getMessager().get("Banks.Bank.Header")); 
					//p.sendMessage(plugin.getMessager().get("info.bankinfo")); 
					return true; 
				}
				if(args[0].equalsIgnoreCase("NewDay")){
					if (!s.hasPermission("pbank.admin")) {
						s.sendMessage("No Permissions");
						return false;
					}
					//LogHandler.info("NEW DAY!");
					//plugin.st.AutochargeAndInterestThreaded();
					plugin.AutochargeAndInterest();
					//p.sendMessage("New Day!");
					return true;
				}
				//pbank rep [read] [#] [page]
				if(args[0].equalsIgnoreCase("reports") || args[0].equalsIgnoreCase("rep")){
					if (args.length == 3) {
						if(args[1].equalsIgnoreCase("read")){

						if (!isInt(args[2])) {
							p.sendMessage(this.plugin.getMessager().get("cmd.Wrong"));
							return false;
						}
						int loanid = Integer.parseInt(args[2]);
						if(!plugin.sql.CheckIfReportExsistCodeManage(p,loanid)){ 
							p.sendMessage(this.plugin.getMessager().get("Mybank.Report.CantfindReport"));
							return false;
						}

						String[] info = plugin.sql.GetLoanInfo(loanid);
						String borrower = info[0];
						int interestrate = Integer.parseInt(info[1]);
						int interest = Integer.parseInt(info[2]);
						int borrowed = Integer.parseInt(info[3]);
						int payments = Integer.parseInt(info[4]);
						//int active = Integer.parseInt(info[5]);
						long requestdate = Long.parseLong(info[6]);
						//int value = Integer.parseInt(info[7]);
					
						//UUID borroweruuid = UUID.fromString(info[8]);
						int fee = Integer.parseInt(info[9]);
						long activateddate = Long.parseLong(info[10]);
						//int bankid = Integer.parseInt(info[11]);
						String nameofbank = info[12];
						long downpayeddate = Long.parseLong(info[14]);
						UUID appuuid = UUID.fromString(info[15]);
						String manager = Bukkit.getOfflinePlayer(appuuid).getName();
						
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
						int profit = payments - borrowed;
					
						String requestdateFormat = new SimpleDateFormat("dd.MM.yy").format(requestdate);
						String activateddateFormat = new SimpleDateFormat("dd.MM.yy").format(activateddate);
						String downpayeddateFormat = new SimpleDateFormat("dd.MM.yy").format(downpayeddate);
						p.sendMessage(plugin.getMessager().get("Main.Divider"));
						p.sendMessage(plugin.getMessager().get("Mybank.Report.Head").replace("%loanid%", Integer.toString(loanid)).replace("%borrower%", borrower));
						p.sendMessage(plugin.getMessager().get("Mybank.Report.line1").replace("%bank%", nameofbank).replace("%interestrate%", interestrateAsString));
						p.sendMessage(plugin.getMessager().get("Mybank.Report.line2").replace("%borrowed%", Integer.toString(borrowed)).replace("%interest%", Integer.toString(interest)).replace("%fee%", Integer.toString(fee)));
						p.sendMessage(plugin.getMessager().get("Mybank.Report.line3").replace("%payments%", Integer.toString(payments)).replace("%profit%", Integer.toString(profit)));
						p.sendMessage(plugin.getMessager().get("Mybank.Report.line4").replace("%requested%", requestdateFormat));
						p.sendMessage(plugin.getMessager().get("Mybank.Report.line5").replace("%approved%", activateddateFormat).replace("%manager%", manager));
						p.sendMessage(plugin.getMessager().get("Mybank.Report.line6").replace("%downpayed%", downpayeddateFormat));
						p.sendMessage(plugin.getMessager().get("Main.Divider"));
						return true;
						}
						p.sendMessage(this.plugin.getMessager().get("cmd.Wrong"));
						return false;
					}
					List<PageObject> loglist = plugin.sql.ListReports(p);;
					if(loglist.isEmpty()){
						p.sendMessage(this.plugin.getMessager().get("Mybank.ReportList.Null"));
						return false;
					}
					int arg1 = 1;
					if (args.length == 2) {
						if (!isInt(args[1])) {
							p.sendMessage(this.plugin.getMessager().get("cmd.Wrong"));
							return false;
						}
						arg1 = Integer.parseInt(args[1]);
					}
					new PaginatedResult<PageObject>(plugin.getMessager().get("Mybank.ReportList.Header"),plugin.getMessager().get("cmd.reports")){

						@Override
						public String format(PageObject entry) {
							return entry.getmsg();
						}
						
					}.display(p, loglist, arg1);
					return true;
					}
				
				
				if(args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("i")){
					if (args.length == 2) {
						String BankName = args[1].toLowerCase(); 
						if (!plugin.sql.CheckIfBankNameExsist(BankName)) {
							p.sendMessage(this.plugin.getMessager().get("cmd.BankDoesNotExsist"));
							return false;
						}
						String[] info = plugin.sql.GetBankInfo(BankName);
						String owner = info[0];
						int interestrate = Integer.parseInt(info[1]);
						//int inviteonly = Integer.parseInt(info[2]);
						int value = Integer.parseInt(info[3]);
						//double receivables = Integer.parseInt(info[4]);
						int bankid = Integer.parseInt(info[8]);
						int receivables = plugin.sql.GetRecivables(bankid);
						int maxloan = Integer.parseInt(info[5]);
						//UUID owneruuid = UUID.fromString(info[6]);
						int fee = Integer.parseInt(info[7]);
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
						String manager = info[9];
						if(manager == null){
							manager = "-";
						}
						p.sendMessage(plugin.getMessager().get("Main.Divider")); 
						p.sendMessage(plugin.getMessager().get("Banks.Info.Name").replace("%bank%", BankName));
						p.sendMessage(plugin.getMessager().get("Banks.Info.Management").replace("%owner%", owner).replace("%manager%", manager));
						p.sendMessage(plugin.getMessager().get("Banks.Info.Value").replace("%value%", Integer.toString(value)).replace("%receivables%", receivablesasstirng));
						p.sendMessage(plugin.getMessager().get("Banks.Info.Interest").replace("%maxloan%", Integer.toString(maxloan)).replace("%interest%", interestrateAsString));
						p.sendMessage(plugin.getMessager().get("Banks.Info.Fee").replace("%fee%", Integer.toString(fee)));


						//plugin.sql.BankInfo(p, BankName);
						return true;
					}
					p.sendMessage(this.plugin.getMessager().get("cmd.info"));
					return true;
				}
				p.sendMessage(this.plugin.getMessager().get("cmd.Wrong"));
				return false;

			}

		}
		s.sendMessage("Ingame command");
		return false;
	}
	public static boolean isInt(String s) {
		try {
			Integer.parseInt(s);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}
	public List<String> getBlackList()
	{
		FileConfiguration c = plugin.getConfig();
		List<String> BlackList;
		BlackList = c.getStringList("bank.name.blacklist");
		return Collections.unmodifiableList(BlackList);
	}
	public boolean isBlackListed(String name)
	{
		for (Object t : getBlackList())
		{
			if (((String) t).equalsIgnoreCase(name))
			{
				return true;
			}
		}

		return false;
	}
	public boolean checkMoney(Player p, double amount)
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
	@SuppressWarnings("deprecation")
	public OfflinePlayer FindPlayerByName(String player) 
	{
		Player targetPlayer = Bukkit.getPlayerExact(player);
		if(targetPlayer != null) return targetPlayer;
		
		targetPlayer = Bukkit.getPlayer(player);
        if(targetPlayer != null) return targetPlayer;

		return Bukkit.getOfflinePlayer(player);
	}

}

