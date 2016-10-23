package no.erik1988.playerbanks;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;

public class LogHandler {

	private static final Logger logger = Bukkit.getLogger();

	  public static void info(String msg) {
	    logger.log(Level.INFO, "[PlayerBanks] " + msg);
	  }

	  public static void warning(String msg) {
	    logger.log(Level.WARNING, "[PlayerBanks] " + msg);
	  }

	  public static void severe(String msg) {
	    logger.log(Level.SEVERE, "[PlayerBanks] " + msg);
	  }

	  public static void info(String msg, Throwable e) {
	    logger.log(Level.INFO, "[PlayerBanks] " + msg, e);
	  }

	  public static void warning(String msg, Throwable e) {
	    logger.log(Level.WARNING, "[PlayerBanks] " + msg, e);
	  }

	  public static void severe(String msg, Throwable e) {
	    logger.log(Level.SEVERE, "[PlayerBanks] " + msg, e);
	  }
	}
