package no.erik1988.playerbanks.objects;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
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
//currently not used
public class BankObject {
	Main plugin;
	private int bankid;
	private List<String> managers = new ArrayList<>();

	
	public BankObject(Main plugin) {
		this.plugin = plugin;
	}

	
//set variables:
    public void setPackedManagers(String packedManagers)
    {
        this.managers = fromArray(packedManagers.split("[|]"));
    }
	
    
    
	
	public void setbankid(int bankid) {
        this.bankid = bankid;
    }
    private void addManager(String man)
    {
    	managers.add(man);
    }
    private boolean removeManager(String man)
    {
        if (!managers.contains(man))
        {
            return false;
        }

        managers.remove(man);
        return true;
    }
	
//get variables:
	private int getbankid() {
		return bankid;
		
	}
    public List<String> getManagers()
    {
        return Collections.unmodifiableList(managers);
    }
    public String getPackedManagers()
    {
        return toMessage(managers, "|");
    }



//other
    public static List<String> fromArray(String... values)
    {
        List<String> results = new ArrayList<>();
        Collections.addAll(results, values);
        results.remove("");
        return results;
    }
    
    public static String toMessage(List<String> args, String sep)
    {
        String out = "";

        for (String arg : args)
        {
            out += arg + sep;
        }

        return stripTrailing(out, sep);
    }
    public static String stripTrailing(String msg, String sep)
    {
        if (msg.length() < sep.length())
        {
            return msg;
        }

        String out = msg;
        String first = msg.substring(0, sep.length());
        String last = msg.substring(msg.length() - sep.length(), msg.length());

        if (first.equals(sep))
        {
            out = msg.substring(sep.length());
        }

        if (last.equals(sep))
        {
            out = msg.substring(0, msg.length() - sep.length());
        }

        return out;
    }

}
