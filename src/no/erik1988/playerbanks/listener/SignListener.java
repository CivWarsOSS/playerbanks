package no.erik1988.playerbanks.listener;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
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
	
}
@EventHandler 	// Player interacts with a block.
public void onSignInteract(PlayerInteractEvent event) {
	
	if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
		
		Material type = event.getClickedBlock().getType();
        if ((type == Material.SIGN_POST) || (type == Material.WALL_SIGN)) {
        	
        	Sign sign = (Sign)event.getClickedBlock().getState();
        	//check if its a borrow sign
            if ((sign.getLine(0).equalsIgnoreCase("[borrow]")) || (sign.getLine(0).equalsIgnoreCase("[loan]"))) {
            	
            	Player player = event.getPlayer();
                String[] delimit = sign.getLine(3).split(" ");
                int price = Integer.valueOf(Integer.valueOf(delimit[0].trim()).intValue());
                
                String bank = ChatColor.stripColor(sign.getLine(1));
                
                //check if player owns bank.
                
                //check if bank has 
            }
        }
	}
}
}
