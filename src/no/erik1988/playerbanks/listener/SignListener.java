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
	//line 2:
	//line 3: amount
	if((event.getLine(0).equalsIgnoreCase(c.getString("sign.allias.borrow", "[borrow]")))){
		
		Player player = event.getPlayer();
		//check if bankname was entered. 
		if (event.getLine(1).isEmpty()) {
			//TODO: replace msg.
			player.sendMessage("You need to type the name of your bank");
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
				// Invalid input on second line, it has to be a NUMBER!
				//TODO: replace msg.
				player.sendMessage("The amount you entered is not a valid number!");
				event.setCancelled(true);
				return;
			}
		}
		//check if player is owner or manager of Bank.
        
        //creating the sign:
    	event.setLine(0, c.getString("sign.allias.borrow", "[borrow]"));
        event.setLine(1, ChatColor.DARK_BLUE + BankName);
        event.setLine(2, player.getName());
        event.setLine(3, amountastring);
        //TODO:Change msg.
        player.sendMessage("Sign created.");
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
