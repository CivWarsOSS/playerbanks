package no.erik1988.playerbanks.listener;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.PluginManager;

import no.erik1988.playerbanks.Main;

public class SignListener implements Listener{
	private Main plugin;
	
    public SignListener(Main plugin){
        this.plugin = plugin;
    }
    public void registerEvents(){
        PluginManager pm = this.plugin.getServer().getPluginManager();
        pm.registerEvents(this, this.plugin);
    }
	
@EventHandler 	// Player creates a sign
public void onSignChange(SignChangeEvent event){
	FileConfiguration c = plugin.getConfig();
	//if player creates a borrow sign
	//line 0: [borrow]
	//line 1: bankname
	//line 2: fee: # | i: #
	//line 3: amount
	if((event.getLine(0).equalsIgnoreCase(c.getString("sign.allias.borrow", "[borrow]")))){
		
		Player player = event.getPlayer();
		//check if bankname was entered. 
		if (event.getLine(1).isEmpty()) {
			player.sendMessage(this.plugin.getMessager().get("sign.NameOfBank"));
            event.setCancelled(true);
            return;
		 }
		String BankName = event.getLine(1);
		if (!plugin.sql.CheckIfBankNameExsist(BankName)) {
			player.sendMessage(this.plugin.getMessager().get("cmd.BankDoesNotExsist"));
            event.setCancelled(true);
            return;
		}
		if (!plugin.sql.CheckIfOwner(player, BankName) && !plugin.sql.CheckIfManager(player, BankName)) {
			player.sendMessage(this.plugin.getMessager().get("cmd.YouCannotManage"));
            event.setCancelled(true);
            return;
		}
		//check if amount was entered.
		String amountastring = c.getString("loan.minBorrow.value","50");
		if (!event.getLine(3).isEmpty()) {
			amountastring = event.getLine(3);
			try {
				Integer.parseInt(event.getLine(3));
			}
			catch (NumberFormatException e) {
				player.sendMessage(this.plugin.getMessager().get("sign.NotANumber"));
				event.setCancelled(true);
				return;
			}
		}
		//gather information about bank.
		String[] info = plugin.sql.GetBankInfo(BankName);
		//String owner = info[0];
		int interestrate = Integer.parseInt(info[1]);
		//int inviteonly = Integer.parseInt(info[2]);
		//int value = Integer.parseInt(info[3]);
		//double receivables = Integer.parseInt(info[4]);
		//int bankid = Integer.parseInt(info[8]);
		//int receivables = plugin.sql.GetRecivables(bankid);
		//int maxloan = Integer.parseInt(info[5]);
		//UUID owneruuid = UUID.fromString(info[6]);
		int fee = Integer.parseInt(info[7]);
		String nameofbank = info[10];
		//String receivablesasstirng = Integer.toString(receivables);
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
        
        //creating the sign:
    	event.setLine(0, c.getString("sign.allias.borrow", "[borrow]"));
        event.setLine(1, ChatColor.DARK_BLUE + nameofbank);
        //event.setLine(2, player.getName());
        //event.setLine(2, ChatColor.BOLD +"fee:"+Integer.toString(fee)+" | i:"+interestrateAsString);
        event.setLine(2, ChatColor.BOLD + this.plugin.getMessager().get("sign.feeandinterest").replace("%fee%", Integer.toString(fee)).replace("%interest%", interestrateAsString));
        
        event.setLine(3, amountastring); 
        player.sendMessage(this.plugin.getMessager().get("sign.created"));
	}
	//if player creates a payloan sign
		//line 0: [payloan]
		//line 1: bankname
		//line 2:
		//line 3: amount
		if((event.getLine(0).equalsIgnoreCase(c.getString("sign.allias.payloan", "[payloan]")))){
			
			Player player = event.getPlayer();
			//check if bankname was entered. 
			if (event.getLine(1).isEmpty()) {
				player.sendMessage(this.plugin.getMessager().get("sign.NameOfBank"));
	            event.setCancelled(true);
	            return;
			 }
			String BankName = event.getLine(1);
			if (!plugin.sql.CheckIfBankNameExsist(BankName)) {
				player.sendMessage(this.plugin.getMessager().get("cmd.BankDoesNotExsist"));
	            event.setCancelled(true);
	            return;
			}
			if (!plugin.sql.CheckIfOwner(player, BankName) && !plugin.sql.CheckIfManager(player, BankName)) {
				player.sendMessage(this.plugin.getMessager().get("cmd.YouCannotManage"));
	            event.setCancelled(true);
	            return;
			}
			//check if amount was entered.
			String amountastring = c.getString("loan.minBorrow.value","50");
			if (!event.getLine(3).isEmpty()) {
				amountastring = event.getLine(3);
				try {
					Integer.parseInt(event.getLine(3));
				}
				catch (NumberFormatException e) {
					player.sendMessage(this.plugin.getMessager().get("sign.NotANumber"));
					event.setCancelled(true);
					return;
				}
			}
			String[] info = plugin.sql.GetBankInfo(BankName);
			String nameofbank = info[10];
			
	        //creating the sign:
	    	event.setLine(0, c.getString("sign.allias.payloan", "[PayLoan]"));
	        event.setLine(1, ChatColor.DARK_BLUE + nameofbank);
	        //event.setLine(2, player.getName());
	        event.setLine(3, amountastring);
	        player.sendMessage(this.plugin.getMessager().get("sign.created"));
		}
	
}
@EventHandler 	// Player interacts with a block.
public void onSignInteract(PlayerInteractEvent event) {

	FileConfiguration c = plugin.getConfig();
	if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
		
		Material type = event.getClickedBlock().getType();
        if ((type == Material.SIGN_POST) || (type == Material.WALL_SIGN)) {
        	
        	Sign sign = (Sign)event.getClickedBlock().getState();
        	
        	
        	//check if its a borrow sign
        	//line 0: [borrow]
        	//line 1: bankname
        	//line 2:
        	//line 3: amount
            if ((sign.getLine(0).equalsIgnoreCase(c.getString("sign.allias.borrow", "[borrow]")))) {
            	
            	Player p = event.getPlayer();
                String[] delimit = sign.getLine(3).split(" ");
                int amount = Integer.valueOf(Integer.valueOf(delimit[0].trim()).intValue());
                
                String BankName = ChatColor.stripColor(sign.getLine(1));
                
				if(plugin.actions.TryBorrow(p,amount,BankName)){
					//p.sendMessage("tryborrow return true");
					return;
				}
				else
				{
					//p.sendMessage("tryborrow return false");
					return;
				}
            }
            
        	//check if its a payloan sign
        	//line 0: [PayLoan]
        	//line 1: bankname
        	//line 2:
        	//line 3: amount
            if ((sign.getLine(0).equalsIgnoreCase(c.getString("sign.allias.pay", "[PayLoan]")))) {
            	
            	Player p = event.getPlayer();
                String[] delimit = sign.getLine(3).split(" ");
                int amount = Integer.valueOf(Integer.valueOf(delimit[0].trim()).intValue());
                
                String BankName = ChatColor.stripColor(sign.getLine(1));
        		if (!plugin.sql.CheckIfBankNameExsist(BankName)) {
        			p.sendMessage(this.plugin.getMessager().get("cmd.BankDoesNotExsist"));
        			return;
        		}
                int BankID = plugin.sql.GetBankID(BankName);
                int LoanId = plugin.sql.GetLoanId(BankID,p);
                plugin.actions.TryPay(p, amount, LoanId);

            }
        }
	}
}
}
