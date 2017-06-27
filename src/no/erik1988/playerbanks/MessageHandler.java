package no.erik1988.playerbanks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import no.erik1988.playerbanks.Main;
import no.erik1988.playerbanks.LogHandler;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class MessageHandler {

	private final Main plugin;
	private final String fileName = "messages.yml";
	private final File file;
	private FileConfiguration config = null;

	public MessageHandler(Main plugin) {
		this.plugin = plugin;
		this.file = new File(plugin.getDataFolder().getAbsolutePath(), fileName);
		this.config = YamlConfiguration.loadConfiguration(this.file);
		setDefaults();
		reloadConfig();
	}

	public FileConfiguration getConfig() {
		if (this.config == null) {
			reloadConfig();
		}
		return this.config;
	}

	public void reloadConfig() {
	    this.config = YamlConfiguration.loadConfiguration(this.file);
	    //InputStream defConfigStream = this.plugin.getResource("messages.yml");
	    if (this.file != null) {
		      YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(this.file);
		      this.config.setDefaults(defConfig);
		    }
	  }

	public void saveConfig() {
		try {
			getConfig().save(this.file);
		} catch (IOException e) {
			LogHandler.severe("Could not save message config to " + this.file, e);
		}
	}

	public void sendMessage(String node, CommandSender sender) {
		sendMessage(node, sender, null);
	}

	public void sendMessage(String node, CommandSender sender, String targetName) {
		if (sender != null) {
			String message = get(node, sender.getName(), targetName);

			for (String line : message.split("\n"))
				sender.sendMessage(line);
		}
	}

	public String get(String node) {
		return get(node, null, null);
	}

	private String get(String node, String playerName, String targetName) {
		return replace(this.config.getString(node, node), playerName, targetName);
	}

	private String replace(String message, String playerName, String targetName) {
		message = message.replace("&", "§");

		if (playerName != null) {
			message = message.replace("%name%", playerName);
		}

		if (targetName != null) {
			message = message.replace("%target%", targetName);
		}

		return message;
	}

	private void setDefaults()
	{
		this.config.addDefault("Main.NoPermissions", "&cYou are not allowed to do this.");

		this.config.addDefault("Main.Divider", "&8---------------------------");
		this.config.addDefault("Main.Notification", "&8[!]&c%msg%");
		this.config.addDefault("Main.MoreInfo", "&eType cmd without arguments for more information.");
		this.config.addDefault("Main.NewDay", "&8[!]&9Today's interest and autopay has been added.");
		this.config.addDefault("Mybank.Bank.Created", "&7Bank &3%bank% &7has been created.");
		this.config.addDefault("Mybank.Bank.Header", "&eBANK &6VALUE &eMAXLOAN &6FEE");
		this.config.addDefault("Mybank.Bank.List", "&7 &9%bank%&7(%interest%) &2%value% &7%maxloan% %fee%");
		this.config.addDefault("Mybank.Bank.Null", "&8[!]&7 You do not own any banks");
		
		this.config.addDefault("Status.Pending", "&ePending");
		this.config.addDefault("Status.Active", "&2Active");
		this.config.addDefault("Status.Frozen", "&3Frozen");
		
		this.config.addDefault("Banks.Bank.Header", "&eBANK &6VALUE &eOWNER &6FEE &eINTEREST");
		this.config.addDefault("Banks.Bank.List", "&7 &9%bank% &7 &2%value% &7 &5%owner% &7 %fee% &7 %interest%");
		this.config.addDefault("Banks.Bank.Null", "&8[!]&7 There is no banks yet.");
		
		this.config.addDefault("Banks.Info.Name", "&7Name: &9%bank%");
		this.config.addDefault("Banks.Info.Management", "&7Owner: &5%owner% &7Manager: &5%manager%");
		this.config.addDefault("Banks.Info.Value", "&7Value: &2%value% &7Receivables: &3%receivables%");
		this.config.addDefault("Banks.Info.Interest", "&7Interest: &3%interest% &7MaxLoan: &3%maxloan%");
		this.config.addDefault("Banks.Info.Fee", "&7Fee to sign loan: &3%fee%");
		
		this.config.addDefault("desc.MakeBank0", "&e/pbank mb &3BANK &a0|1|2|3 &dMAXLOAN &9FEE");
		this.config.addDefault("desc.MakeBank1", "&3 Second &7argument is the name of the bank.");
		this.config.addDefault("desc.MakeBank2", "&a Third &7argument is the interest rate: \n  0=none, 1 = low[%low%], 2 = medium[%med%], 3 = high[%hi%]");
		this.config.addDefault("desc.MakeBank3", "&d Fourth &7argument is the maximum amount \n  a player can loan from you");
		this.config.addDefault("desc.MakeBank4", "&9 Fifth &7argument is the fee the player will \n  be charged for signing a loan with you");
		
		this.config.addDefault("desc.Withdraw", "&7This command allows you to withdraw/get money from your bank.");
		this.config.addDefault("desc.Deposit", "&7This command allows you to deposit/add money to your bank.");
		this.config.addDefault("desc.Pay", "&7You can find the loan ID by typing &3/pbank loans");
		this.config.addDefault("desc.Borrow", "&7You can see a list of banks by typing &3/pbank list");
		this.config.addDefault("desc.RemBank", "&7This will permanently delete your Bank.");
		this.config.addDefault("desc.Manager", "&7You can assign a manager to help you out with your bank.");
		this.config.addDefault("desc.Trans", "&7You can also use &3/pbank trans BANK &7to see different types of information.");
		this.config.addDefault("desc.Log", "&7You can also use &3/pbank log BANK&7to see different types of information.");
		
		this.config.addDefault("info.bankinfo", "&7 Use &3/pbank info BANK &7to see more information.");
		
		this.config.addDefault("cmd.NameLength", "&7 The name needs to be more than &33 &7and less than &315 &7letters.");
		
		this.config.addDefault("cmd.MakeBank", "&7 Use &3/pbank makebank&7|mb &3BANK 0|1|2|3 MaxLoan Fee"); 
		this.config.addDefault("cmd.RemBank", "&7 Use &3/pbank remove&7|rem &3BANK");
		this.config.addDefault("cmd.me", "&7 Use &3/pbank me");
		this.config.addDefault("cmd.deposit", "&7 Use &3/pbank deposit&7|d &3BANK AMOUNT");
		this.config.addDefault("cmd.withdraw", "&7 Use &3/pbank withdraw&7|w &3BANK AMOUNT");
		this.config.addDefault("cmd.contracts", "&7 Use &3/pbank contracts&7|c [app|del] [id]");
		this.config.addDefault("cmd.reports", "&7 Use &3/pbank reports&7|rep [[read] [id]] [page]");
		this.config.addDefault("cmd.managers", "&7 Use &3/pbank manager BANK &7[add|clear] [PLAYER]"); 
		this.config.addDefault("cmd.log", "&7 Use &3/pbank log BANK &7[page]");
		this.config.addDefault("cmd.sign", "&7 Use &3/pbank sign &7[type]");
		
		this.config.addDefault("cmd.info", "&7 Use &b/pbank info&7|i &3BANK");
		this.config.addDefault("cmd.list", "&7 Use &b/pbank list&7|l &b");
		this.config.addDefault("cmd.borrow", "&7 Use &b/pbank borrow&7|b &bBANK AMOUNT");
		this.config.addDefault("cmd.loans", "&7 Use &b/pbank loans&7 [del] [id]");
		this.config.addDefault("cmd.pay", "&7 Use &b/pbank pay&7|p &bLOANID|BANK AMOUNT&7");
		this.config.addDefault("cmd.transactions", "&7 Use &b/pbank trans&7 [bank] [page]");
		this.config.addDefault("cmd.msg", "&7 Use &b/pbank msg");
		
		

		
		
		this.config.addDefault("eg.MakeBank", "&7 Eg. &3/pbank makebank Bank1 1 500 10");
		this.config.addDefault("eg.LoanId", "&7 You can find the loan id by typing &3/pbank c &7or &3/pbank loans");
		
		this.config.addDefault("cmd.MinBorrow", "&8[!]&c Your request needs to be minimum &3%minborrow%");
		this.config.addDefault("cmd.Wrong", "&8[!]&c You did not type the command right");
		this.config.addDefault("cmd.BlackList", "&8[!]&c You are not allowed to use that name"); 
		
		this.config.addDefault("MakeBank.NameAlreadyExsist", "&8[!]&c This bank already exist, pick a different name");
		this.config.addDefault("MakeBank.ToManyBanks", "&8[!]&c You already have the maximum allowed number of banks(%nr%)");
		this.config.addDefault("MakeBank.maxfee", "&8[!]&c You cannot have a fee larger than %nr%");
		this.config.addDefault("MakeBank.minborrow", "&8[!]&c Your maxloan can not be lower than the minimum loan (&3%minborrow%&7)");
		
		this.config.addDefault("cmd.BankDoesNotExsist", "&8[!]&c The Bank does not exist, make sure you wrote the right name.");
		this.config.addDefault("cmd.YouDontOwnThisBank", "&8[!]&c You do not own this bank.");
		this.config.addDefault("cmd.YouCannotManage", "&8[!]&c You are not allowed to manage this Bank");
		
		this.config.addDefault("eco.NotEnougthMoney", "&8[!]&c You do not have that much money");
		this.config.addDefault("eco.AccNotExisting", "&8[!]&c This player account does not exist");
		this.config.addDefault("eco.TransferedFromBank", "&3%money% &7has been transferred from the bank to your account."); 
		this.config.addDefault("eco.TransferedToBank", "&3%money% &7has been transferred from your account to the bank.");
		
		this.config.addDefault("borrow.YouOwnThisBank", "&8[!]&c You cannot borrow from a bank you own or manage.");
		this.config.addDefault("borrow.RequestSendt", "&7You have sent bank &5%bank% &7a request to borrow &3%money%.");
		
		this.config.addDefault("borrow.Approved", "&7%time% - &5%player% &2has approved your loan of &3%money%.");
		this.config.addDefault("borrow.Denied", "&7%time% - &5%player% &chas denied your loan of &3%money%.");
		this.config.addDefault("borrow.Pardoned", "&7%time% - &5%player% &ahas pardoned your loan of &3%money%.");
		this.config.addDefault("borrow.Finished", "&7%time% - &2Loan of &3%amount% &7(+ &3%interest% &7interest and &3%fee% &7fee) \n &2from &3%bank% &2has been payed down.");
		
		//this.config.addDefault("Page.Header", "&e%header% - Page (%page%/%maxpages%) - New is first");
		this.config.addDefault("log.Header", "&eLog for bank %bank%");
		this.config.addDefault("log.BankMade", "&7%time% - &3%bank% &7bank has been established.");
		this.config.addDefault("log.Request", "&7%time% - &5%player% &7has requested to borrow &3%money%.");
		this.config.addDefault("log.Approved", "&7%time% - &5%player% &2has approved &5%borrower%s loan of &3%money%.");
		this.config.addDefault("log.Denied", "&7%time% - &5%player% &chas denied &5%borrower%s request for &3%money%.");
		this.config.addDefault("log.Pardoned", "&7%time% - &5%player% &ahas pardoned &5%borrower%s loan of &3%money%.");
		this.config.addDefault("log.Finished", "&7%time% - &2Loan of &3%amount% &2from &5%borrower%& &2has been payed down.");
		this.config.addDefault("log.null", "&7There is no log to display.");
		this.config.addDefault("log.NewManager", "&7%time% - &5%player% &7was added as a manager by &5%owner%.");
		this.config.addDefault("log.ClearManager", "&7%time% - &5%owner% &7removed the manager.");
		
		this.config.addDefault("transactions.newtrans", "&7You have unread transactions, &7type &3/pbank trans &7to see them.");
		this.config.addDefault("transactions.deposit", "&7%time% - &5%player% &7deposited &3%amount% &7to bank &3%bank%");
		this.config.addDefault("transactions.withdraw", "&7%time% - &5%player% &7withdrawed &3%amount% &7from bank &3%bank%");
		this.config.addDefault("transactions.borrow", "&7%time% - &5%player% &7borrowed &3%amount% &7(+fee) &7from bank &3%bank%");
		this.config.addDefault("transactions.payment", "&7%time% - &5%player% &7paid &3%amount% &7to bank &3%bank%");
		this.config.addDefault("transactions.interest", "&7%time% - &3%amount% &7interest added to &5%player%'s &7loan (&3%bank%&7)");
		this.config.addDefault("transactions.autopay", "&7%time% - &3%amount% &7was auto paid on &5%player%'s &7loan (&3%bank%&7)");
		this.config.addDefault("transactions.null", "&7There is no transactions to display.");
		
		//this.config.addDefault("borrow.InviteOnly", "&7This bank does not allow request, ask the owner to invite you.");
		this.config.addDefault("borrow.NotEnougthMoney", "&8[!]&c The bank does not have that much money");
		this.config.addDefault("borrow.MaxLoan", "&8[!]&c The bank only allows a max loan of &3%maxloan%");
		this.config.addDefault("borrow.MaxLoanPerm", "&8[!]&c Your rank only allows a max loan of &3%maxloan%");
		this.config.addDefault("borrow.MaxContract", "&8[!]&c Your rank only allows a maximum of &3%contracts% at the time. \n &7pay down contracts or delete requests before making another request.");
		this.config.addDefault("borrow.AlreadyLoan", "&8[!]&c You already have a loan or request for this bank."); 
		this.config.addDefault("borrow.Loans.Header", "&eID &6BANK &eLOAN &6OWNER &eSTATUS");
		this.config.addDefault("borrow.Loans.List", "&7 &9%code% &3%bank%&7(%interest%) &3%req% &5%owner% %status%");
		this.config.addDefault("borrow.Loans.Null", "&8[!]&7 You do not have any loans yet.");
		
		
		this.config.addDefault("Mybank.Contracts.Acitvated", "&7Contract &3%contract% &7has been activated");
		this.config.addDefault("Mybank.Contracts.CantfindContract", "&8[!]&c Cannot find a contract with this id.");
		this.config.addDefault("Mybank.Contracts.AlreadyActive", "&8[!]&c The contract is already active.");
		this.config.addDefault("Mybank.Contracts.NotEnougthMoney", "&8[!]&c The bank does not have enough money for this contract.");
		this.config.addDefault("Mybank.Contracts.Header", "&eID &6BANK &eLOAN &6PLAYER &eSTATUS");
		this.config.addDefault("Mybank.Contracts.List", "&7 &9%code% &3%bank%&7(%interest%) &3%req% &5%player% %status%");
		this.config.addDefault("Mybank.Contracts.Null", "&7You do not have any contracts.");
		this.config.addDefault("Mybank.Contracts.DelIsActive", "&cThe contract you are trying to delete is active. \n &c If you delete this contact the borrower will be pardoned (you dont get your money). \n &c If you want to proceed you can type &3/pbank c del %id% -f");
		this.config.addDefault("Mybank.Contracts.DelSuccess", "&2You deleted contract &3%id%");
		this.config.addDefault("Mybank.Contracts.Finished", "&2%time% - &5%borrower%'s &7loan of &3%amount% &7(+ &3%interest% &7interest and + &3%fee% &7fee) \n &7was payed down (&3%bank%&7).");
		
		this.config.addDefault("Mybank.Contracts.Pending", "&7You have &3%count% &7pending contract(s). &3/pbank c"); 
		this.config.addDefault("Mybank.Contracts.New", "&7You have a new contract pending. &3/pbank c"); 
		
		this.config.addDefault("Mybank.Remove.NotEmpty", "&cYour bank still has money in it, withdraw it before removing the account.&3/pbank w BANK AMOUNT");
		this.config.addDefault("Mybank.Remove.HasActiveLoans", "&cYour bank still has active contracts, delete them before removing the bank.");
		this.config.addDefault("Mybank.Remove.RemSuccess", "&2You removed bank &3%bank%");
		
		this.config.addDefault("Mybank.Report.Head", "&2##REPORT FOR LOANID: &3%loanid% &2- &5%borrower%&2##");
		this.config.addDefault("Mybank.Report.line1", "&7Bank: &9%bank% &7InterestRate: &3%interestrate%");
		this.config.addDefault("Mybank.Report.line2", "&7Borrowed: &3%borrowed%&7(+&3%fee%&7 fee) &7Interest: &3%interest%");
		this.config.addDefault("Mybank.Report.line3", "&7Paid: &3%payment% &7Profit: &2%profit%");
		this.config.addDefault("Mybank.Report.line4", "&7Requested: &3%requested%"); 
		this.config.addDefault("Mybank.Report.line5", "&7Approved: &3%approved% &7- &5%manager%");
		this.config.addDefault("Mybank.Report.line6", "&7Down Paid: &3%downpayed%");
		
		this.config.addDefault("Mybank.ReportList.Header", "&eID &6BANK &eBORROWER &6DATE");  
		this.config.addDefault("Mybank.ReportList.List", "&9%loanid% &7%bank% &5%borrower% &7%date%");
		this.config.addDefault("Mybank.ReportList.Null", "&7You do not have any reports yet.");
		
		this.config.addDefault("Mybank.Report.CantfindReport", "&8[!]&cCannot find a report on loan with this id.");
		this.config.addDefault("Mybank.Report.NewReport", "&2You have a new report available. Type &3/pbank rep read %loanid%");
		
		this.config.addDefault("Mybank.Manager.NotYourself", "&8[!]&cYou cannot assign yourself as a manager");
		this.config.addDefault("Mybank.Manager.UnknownPlayer", "&8[!]&cUnknown player. Make sure the player has been online and typed correct.");
		this.config.addDefault("Mybank.Manager.PlayerHasLoan", "&8[!]&cPlayer has contract/request in your bank. Remove the loans before making the player a manager.");
		this.config.addDefault("Mybank.Manager.NoManager", "&7This Bank does not have a manager.");
		this.config.addDefault("Mybank.Manager.Clear", "&7Manager cleared/removed");
		this.config.addDefault("Mybank.Manager.Added", "&7Manager &5%player% &7was assigned to your Bank");
		this.config.addDefault("Mybank.Manager.List", "&5%player% &7is the manager of your Bank");
		this.config.addDefault("Mybank.Manager.CanDo", "&eA manager &2CAN&e:&7 \n - App/Del requests \n - Read transactions,log and reports \n - Deposit money");
		this.config.addDefault("Mybank.Manager.CanNotDo", "&eA manager &cCANNOT&e:&7 \n - Disband the Bank \n - Pardon loans \n - Have/request loans in the Bank \n - Withdraw money");
		
		this.config.addDefault("Mybank.Manager.YouAreNowManager", "&7%time% - &2You have been assigned as a manager for bank &3%bank%.");
		
		this.config.addDefault("Loans.Pay.NoloanWithId", "&7 Cannot find loan, make sure the ID/name is correct.");
		this.config.addDefault("Loans.Downpayment", "&7You have down paid &3%money% &7on your loan(&9%id%&7)");
		this.config.addDefault("Loans.Del.IsActive", "&8[!]&cYou cannot delete active contracts."); 
		
		this.config.addDefault("msg.null", "&7There is no messages to display");
		this.config.addDefault("msg.trans.notrans", "&7There is no transactions to display.");
		this.config.addDefault("msg.trans.head", "&f Latest transactions (newest on top)");
		this.config.addDefault("msg.msg.head", "&eMSG for %player%");
		
		this.config.addDefault("sign.NotANumber", "&8[!]&c Line 4 needs to be a number.");
		this.config.addDefault("sign.NameOfBank", "&8[!]&c Line 2 needs to be the name of your bank.");
		this.config.addDefault("sign.desc.borrow", "##############\n#   [Borrow]   \n#     BANK     \n#              \n#    AMOUNT    \n##############\n&8 You can make a sign like this to allow players to request a loan from you by clicking the sign.");
		this.config.addDefault("sign.desc.payloan", "##############\n#   [PayLoan]  \n#     BANK     \n#              \n#    AMOUNT    \n##############\n&8 You can make a sign like this to allow players to pay down on their loan by clicking the sign.");
		this.config.addDefault("sign.desc.list", "&eType this to see sign instructions:\n&8 - &3/pbank sign Borrow \n&8 - &3/pbank sign PayLoan");
		this.config.addDefault("sign.created", "&8Sign created.");
		this.config.addDefault("sign.feeandinterest", "fee:%fee% | i:%interest%");
		
		
		this.config.options().copyDefaults(true);
		saveConfig();
	}
}
