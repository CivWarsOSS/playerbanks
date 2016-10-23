package no.erik1988.playerbanks.listener;

import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import no.erik1988.playerbanks.Main;
import no.erik1988.playerbanks.sql.Database;

public final class LoginListener implements Listener {
	public LoginListener(Main plugin) {
		this.plugin = plugin;
	}
	Main plugin;
    @EventHandler
    public void normalLogin(PlayerJoinEvent  event) {
    	Player player = event.getPlayer();
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