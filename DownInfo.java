package no.erik1988.donation;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class DownInfo {
	private int bankid;
	private int loanid;
	private String borrower;
	private double interest;
	private double borrowed;
	private double payments;
	public DownInfo(int bankid, int loanid, String borrower, double interest, double borrowed, double payments){
		this.bankid = bankid;
		this.loanid = loanid;
		this.borrower = borrower;
		this.interest = interest;
		this.borrowed = borrowed;
		this.payments = payments;
	}
	

}
