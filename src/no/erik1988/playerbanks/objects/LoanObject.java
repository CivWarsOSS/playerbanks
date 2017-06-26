package no.erik1988.playerbanks.objects;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;

import net.milkbowl.vault.economy.Economy;
import no.erik1988.playerbanks.sql.Errors;
import no.erik1988.playerbanks.LogHandler;
import no.erik1988.playerbanks.Main;

public class LoanObject {
	private Main plugin;
	private int bankid;
	private int loanid;
	private int fee;
	//private int interestrate;
	private String borrower;
	private int interest;
	private int borrowed;
	private int payments;
	
	public LoanObject(Main plugin) {
		this.plugin = plugin;
	}


	public void DownPayForAll()
	{
	    FileConfiguration c = plugin.getConfig();
	    Economy e = plugin.getEconomy();
		int autocharge = c.getInt("interest.autocharge.FixedAmount");
		List<LoanObject> lo;
		try {
			lo = plugin.sql.GetLoanObject();
			if (lo == null){
				LogHandler.info("lo is null");
				return;
			}
			for (LoanObject loanobject: lo)
			{
				int bankid = loanobject.getbankid();
				int loanid = loanobject.getloanid();
				int fee = loanobject.getfee();
				int interest = loanobject.getinterest();
				int borrowed = loanobject.getborrowed();
				int payments = loanobject.getpayments();
				String borrower = loanobject.getborrower();
				
				
				UUID uuid = UUID.fromString(borrower); 
				OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
				double curmoney = e.getBalance(player);
				int paymentsleft = (interest + borrowed + fee) - payments;
				if (paymentsleft < autocharge){
					autocharge = paymentsleft;
	        	}
	        	if (curmoney < autocharge){
	        		
	        		autocharge = (int) curmoney;
	        	}
	        	plugin.sql.Downpay(loanid,autocharge);
	    		e.withdrawPlayer(player, autocharge);
	    		plugin.sql.Deposit(player, bankid, autocharge);
	            //0.deposit
	            //1.withdraw 
	            //2.borrow/loan 
	            //3.payment(manual)
	            //4.interest
	            //5.Autopayment
	            int logtype = 5;  
	            //plugin.st.MakeLogThreaded(logtype, loanid, autocharge, player, bankid, 0,uuid);

	            if (autocharge > 0){
		            plugin.LogTransPre(logtype, loanid, autocharge, player, bankid, 0,uuid);
		            LogHandler.info("loan id: " + loanid +" downpayed with: "+ autocharge +" from: "+ player.getName().toString());
		            //plugin.st.CheckifLoanIsPayedThreaded(loanid);
		            plugin.CheckifLoanIsPayed(loanid);
		            //plugin.ShowTransIfOnline(uuid);
		            if (autocharge >= c.getInt("interest.autocharge.FixedAmount")){
			            plugin.sql.MarkLoanAsMissed(loanid,2);
		            }
	            } else {
	            	LogHandler.info(player.getName().toString() + " did not have enough money to pay down loan (loan Id: " + loanid);
	            	plugin.sql.MarkLoanAsMissed(loanid,1);
	            }
	            

			}
		} catch (SQLException e1) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e1);
			//e1.printStackTrace();
		}
	}
		public void DownPayForPlayer(OfflinePlayer player)
		//TODO: Add a downpaying event when player is getting money to their account, if they are marked as bad payers.
		{
		    FileConfiguration c = plugin.getConfig();
		    Economy e = plugin.getEconomy();
			int autocharge = c.getInt("interest.autocharge.FixedAmount");
			List<LoanObject> lo;
			try {
				lo = plugin.sql.GetLoanObjectPlayer(player);
				if (lo == null){
					LogHandler.info("lo is null");
					return;
				}
				for (LoanObject loanobject: lo)
				{
					int bankid = loanobject.getbankid();
					int loanid = loanobject.getloanid();
					int fee = loanobject.getfee();
					int interest = loanobject.getinterest();
					int borrowed = loanobject.getborrowed();
					int payments = loanobject.getpayments();
					String borrower = loanobject.getborrower();
					
					
					UUID uuid = UUID.fromString(borrower); 
					double curmoney = e.getBalance(player);
					int paymentsleft = (interest + borrowed + fee) - payments;
					if (paymentsleft < autocharge){
						autocharge = paymentsleft;
		        	}
		        	if (curmoney < autocharge){
		        		
		        		autocharge = (int) curmoney;
		        	}
		        	plugin.sql.Downpay(loanid,autocharge);
		    		e.withdrawPlayer(player, autocharge);
		    		plugin.sql.Deposit(player, bankid, autocharge);
		            //0.deposit
		            //1.withdraw 
		            //2.borrow/loan 
		            //3.payment(manual)
		            //4.interest
		            //5.Autopayment
		            int logtype = 5;  
		            //plugin.st.MakeLogThreaded(logtype, loanid, autocharge, player, bankid, 0,uuid);

		            if (autocharge > 0){
			            plugin.LogTransPre(logtype, loanid, autocharge, player, bankid, 0,uuid);
			            LogHandler.info("loan id: " + loanid +" downpayed with: "+ autocharge +" from: "+ player.getName().toString());
			            plugin.CheckifLoanIsPayed(loanid);
			            plugin.ShowTransIfOnline(uuid);
			            plugin.sql.MarkLoanAsMissed(loanid,2);
		            } else {
		            	//LogHandler.info(player.getName().toString() + " did not have enough money to pay down loan (loan Id: " + loanid);
		            }
		            

				}
			} catch (SQLException e1) {
				plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e1);
				//e1.printStackTrace();
			}		
	}
	public void AddInterestToAll()
	{
	    FileConfiguration c = plugin.getConfig();
		List<LoanObject> lo;
		try {
			lo = plugin.sql.GetLoanObject();
			if (lo == null){
				LogHandler.info("lo is null");
				return;
			}
			for (LoanObject loanobject: lo)
			{
				int bankid = loanobject.getbankid();
				int loanid = loanobject.getloanid();
				int interestrate = loanobject.getinterestrate();
				int fee = loanobject.getfee();
				int interest = loanobject.getinterest();
				int borrowed = loanobject.getborrowed();
				int payments = loanobject.getpayments();
				String borrower = loanobject.getborrower();
				
				
				UUID uuid = UUID.fromString(borrower); 
				OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
				int paymentsleft = (interest + borrowed + fee) - payments;
        		
        		int intrate = 0;
        		
        		if (interestrate == 1){
        			intrate = c.getInt("interest.low");
        		}
        		else if (interestrate == 2){
        			intrate = c.getInt("interest.med");
        		}
        		else if (interestrate == 3){
        			intrate = c.getInt("interest.high");
        		}
        		int transacton = paymentsleft * intrate /100;
        		plugin.sql.UpdateInterest(loanid,transacton);

        		LogHandler.info("Added interest of "+ transacton +" to loanid: " + loanid +" from: "+ player.getName().toString());
                //0.deposit
                //1.withdraw
                //2.borrow/loan
                //3.payment(manual)
                //4.interest
                //5.Autopayment
                int logtype = 4; 
                plugin.sql.LogTrans(logtype, loanid, transacton, player, bankid,0);
                //plugin.ShowTransIfOnline(uuid);
			}
		} catch (SQLException e1) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e1);
			//e1.printStackTrace();
		}
		
	}
//set variables:
	public void setbankid(int bankid) {
        this.bankid = bankid;
    }

	public void setloanid(int loanid) {
        this.loanid = loanid;
	}

	public void setborrower(String borrower) {
		this.borrower = borrower;
		
	}

	public void setinterest(int interest) {
		this.interest = interest;
		
	}

	public void setborrowed(int borrowed) {
		this.borrowed = borrowed;
		
	}

	public void setpayments(int payments) {
		this.payments = payments;
		
	}

	public void setfee(int fee) {
		this.fee = fee;
		
	}
	public void setinterestrate(int interestrate) {
		this.fee = interestrate;
		
	}
	
//get variables:
	private int getbankid() {
		return bankid;
		
	}
	private int getloanid() {
		return loanid;
		
	}
	private String getborrower() {
		return borrower;
		
	}
	private int getinterest() {
		return interest;
		
	}
	private int getborrowed() {
		return borrowed;
		
	}
	private int getpayments() {
		return payments;
		
	}
	private int getfee() {
		return fee;
		
	}
	private int getinterestrate() {
		return fee;
		
	}
}
