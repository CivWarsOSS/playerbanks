package no.erik1988.playerbanks;

import java.util.Calendar;

import org.bukkit.Bukkit;

import no.erik1988.playerbanks.Main;

class Schedule{

	/**
	 * Schedules a task to run at a certain hour every day.
	 * @param plugin The plugin associated with this task
	 * @param task The task to run
	 * @param hour [0-23] The hour of the day to run the task
	 * @return Task id number (-1 if scheduling failed)
	 * Author: Courier (https://bukkit.org/threads/performing-actions-at-specific-times.103357/)
	 */
	static int scheduleRepeatAtTime(Main plugin, Runnable task, int hour)
	{
	    //Calendar is a class that represents a certain time and date.
	    Calendar cal = Calendar.getInstance(); //obtains a calendar instance that represents the current time and date
	 
	    //time is often represented in milliseconds since the epoch,
	    //as a long, which represents how many milliseconds a time is after
	    //January 1st, 1970, 00:00.
	 
	    //this gets the current time
	    long now = cal.getTimeInMillis();
	    //you could also say "long now = System.currentTimeMillis()"
	 
	    //since we have saved the current time, we need to figure out
	    //how many milliseconds are between that and the next
	    //time it is 7:00pm, or whatever was passed into hour
	    //we do this by setting this calendar instance to the next 7:00pm (or whatever)
	    //then we can compare the times
	 
	    //if it is already after 7:00pm,
	    //we will schedule it for tomorrow,
	    //since we can't schedule it for the past.
	    //we are not time travelers.
	    if(cal.get(Calendar.HOUR_OF_DAY) >= hour)
	        cal.add(Calendar.DATE, 1); //do it tomorrow if now is after "hours"
	 
	    //we need to set this calendar instance to 7:00pm, or whatever.
	    cal.set(Calendar.HOUR_OF_DAY, hour);
	    cal.set(Calendar.MINUTE, 0);
	    cal.set(Calendar.SECOND, 0);
	    cal.set(Calendar.MILLISECOND, 0);
	 
	    //cal is now properly set to the next time it will be 7:00pm
	 
	    long offset = cal.getTimeInMillis() - now;
	    long ticks = offset / 50L; //there are 50 milliseconds in a tick
	 
	    //we now know how many ticks are between now and the next time it is 7:00pm
	    //we schedule an event to go off the next time it is 7:00pm,
	    //and repeat every 24 hours.
	    return Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, task, ticks, 1728000L);
	    //24 hrs/day * 60 mins/hr * 60 secs/min * 20 ticks/sec = 1728000 ticks
	}


}

