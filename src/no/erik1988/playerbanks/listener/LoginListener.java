package no.erik1988.playerbanks.listener;

import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import no.erik1988.playerbanks.Main;

public final class LoginListener implements Listener {
	public LoginListener(Main plugin) {
		this.plugin = plugin;
	}
	private Main plugin;
    @EventHandler
    @SuppressWarnings("ucd")
    public void normalLogin(PlayerJoinEvent  event) {
    	Player player = event.getPlayer();
    	
    	int count = plugin.sql.CountPendingContracts(player);
    	if(count>0){
    		player.sendMessage(plugin.getMessager().get("Mybank.Contracts.Pending").replace("%count%", Integer.toString(count))); 
    	}
    	
    	if (plugin.sql.ListMSG(player)) {
			plugin.sql.MarkMSGAsSeen(player); 
		}
    	if (plugin.sql.CheckIfNewTrans(player)){
    		player.sendMessage(plugin.getMessager().get("transactions.newtrans")); 
    	}
    	
    	/*
    	plugin.st.exec.execute(new Runnable(){
			public void run(){
		    	Player player = event.getPlayer();
		    	//player.sendMessage("messages was displayed");
		    	
		    	if (plugin.sql.ListMSG(player)) {
					plugin.sql.MarkMSGAsSeen(player);
					//p.sendMessage("messages was displayed");
				}
		    	if (plugin.sql.CheckIfNewTrans(player)){
		    		player.sendMessage(plugin.getMessager().get("transactions.newtrans")); 
		    	}
			}
		});*/

    }

}