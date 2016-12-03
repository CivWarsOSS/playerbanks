package no.erik1988.playerbanks.objects;

import no.erik1988.playerbanks.Main;

public class PageObject {
	Main plugin;
	private int bankid;
	private String msg;
	
	public PageObject(Main plugin) {
		this.plugin = plugin;
	}


//set variables:
	public void setbankid(int bankid) {
        this.bankid = bankid;
    }

	public void setmsg(String msg) {
        this.msg = msg;
	}


	
//get variables:
	public int getbankid() {
		return bankid;
	}	
	public String getmsg() {
		return msg;
	}	
}
