/*Source: https://github.com/sk89q/CommandBook/blob/f3c2636b358be29a1f408bf254432f4f2a53fe4f/src/main/java/com/sk89q/commandbook/commands/PaginatedResult.java
*/
package no.erik1988.playerbanks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;

public abstract class PaginatedResult<T> {

    private final String header;
    private final String footer;

    protected static final int PER_PAGE = 9;
    
    public PaginatedResult(String header, String footer) {
        this.header = header;
        this.footer = footer;
    }

    
    public void display(CommandSender sender, Collection<? extends T> results, int page) throws CommandException {
        display(sender, new ArrayList<T>(results), page);
    }
    
    public void display(CommandSender sender, List<? extends T> results, int page) throws CommandException {
        if (results.size() == 0) throw new CommandException("No results match!");
        --page;

        int maxPages = results.size() / PER_PAGE;

        // If the content divides perfectly, eg (18 entries, and 9 per page)
        // we end up with a blank page this handles this case
        if (results.size() % PER_PAGE == 0) {
            maxPages--;
        }

        page = Math.max(0, Math.min(page, maxPages));
        //String head = plugin.getMessager().get("Page.Header").replace("%header%", header).replace("%page%", Integer.toString(page + 1)).replace("%maxpages%", Integer.toString(maxPages + 1));
        sender.sendMessage(ChatColor.YELLOW + header + ChatColor.YELLOW + " - Page (" + (page + 1) + "/" + (maxPages + 1) + ")");
        for (int i = PER_PAGE * page; i < PER_PAGE * page + PER_PAGE  && i < results.size(); i++) {
            sender.sendMessage(ChatColor.YELLOW.toString() + format(results.get(i)));
        }
        if(!footer.isEmpty()){
        sender.sendMessage(ChatColor.YELLOW + footer);
        }
    }
    
    public abstract String format(T entry);

}
