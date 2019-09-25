package com.sylvcraft.commands;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.sylvcraft.PerWorldRules;

public class Rules implements TabExecutor {
	PerWorldRules plugin;
	
	public Rules(PerWorldRules plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
    List<String> tabs = new ArrayList<String>();
		if (!sender.hasPermission("perworldrules.admin")) return tabs;
		if (args.length == 0) return tabs;

		if (args.length == 1) {
			tabs.add("insert");
			tabs.add("show");
			tabs.add("append");
			tabs.add("del");
			tabs.add("set");
			return tabs;
		}
		
		switch (args[0].toLowerCase()) {
		case "insert":
		case "show":
		case "append":
		case "del":
		case "set":
			if (args.length == 2) {
				tabs.add("_global_");
				tabs.add("_default_");
				for (World w : plugin.getServer().getWorlds()) {
					tabs.add(w.getName());
				}
			}
			break;
		}
		return tabs;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission("perworldrules.view")) {
			plugin.msg("access-denied", sender);
			return true;
		}
		
		if (args.length == 0 || (args.length > 0 && !sender.hasPermission("perworldrules.admin"))) {
			if (!(sender instanceof Player)) {
				plugin.msg("no-console", sender);
				return true;
			}
			showRules((Player)sender);
			return true;
		}
		
		if (!sender.hasPermission("perworldrules.admin")) {
			plugin.msg("access-denied", sender);
			return true;
		}

		
		List<String> rules = new ArrayList<String>();
		switch (args[0].toLowerCase()) {
		case "append":
			rules = appendRules(sender, args);
			if (rules == null) return true;
			break;
			
		case "insert":
			rules = insertRules(sender, args);
			if (rules == null) return true;
			break;
		
		case "set":
			rules = setRules(sender, args);
			if (rules == null) return true;
			break;
	
		case "del":
			rules = delRules(sender, args);
			if (rules == null) return true;
			break;
			
		case "show":
			showWorldRules(sender, args);
			return true;
			
		default:
			plugin.msg("help-append", sender);
			plugin.msg("help-insert", sender);
			plugin.msg("help-set", sender);
			plugin.msg("help-del", sender);
			plugin.msg("help-show", sender);
			return true;
		}
		
		plugin.getConfig().set("rules." + args[1], rules);
		plugin.saveConfig();
		plugin.msg("rules-set", sender);
		return true;
	}
	
	int getIndex(String indexInput) {
		try {
			return Integer.parseInt(indexInput);
		} catch (NumberFormatException ex) {
			return -9;
		}
	}
	
	List<String> appendRules(CommandSender sender, String[] args) {
		if (args.length < 3) {
			plugin.msg("help-append", sender);
			return null;
		}
		
		String world = args[1];
		List<String> rules = plugin.getConfig().getStringList("rules." + world);
		String textContent = StringUtils.join(args, ' ', 2, args.length);
		rules.add(textContent);
		return rules;
	}
	
	List<String> insertRules(CommandSender sender, String[] args) {
		if (args.length < 4) {
			plugin.msg("help-insert", sender);
			return null;
		}

		int index = getIndex(args[2]);
		if (index < 1) {
			plugin.msg("invalid-index", sender);
			return null;
		}

		String world = args[1];
		List<String> rules = plugin.getConfig().getStringList("rules." + world);
		if (index-1 < 0 || index > rules.size()) {
			plugin.msg("invalid-index", sender);
			return null;
		}

		String textContent = StringUtils.join(args, ' ', 3, args.length);
		rules.add(index-1, textContent);
		return rules;
	}
	
	List<String> setRules(CommandSender sender, String[] args) {
		if (args.length < 4) {
			plugin.msg("help-set", sender);
			return null;
		}

		int index = getIndex(args[2]);
		if (index < 1) {
			plugin.msg("invalid-index", sender);
			return null;
		}

		String world = args[1];
		List<String> rules = plugin.getConfig().getStringList("rules." + world);
		if (index-1 < 0 || index > rules.size()) {
			plugin.msg("invalid-index", sender);
			return null;
		}

		String textContent = StringUtils.join(args, ' ', 3, args.length);
		rules.set(index-1, textContent);
		return rules;
	}
	
	List<String> delRules(CommandSender sender, String[] args) {
		if (args.length < 3) {
			plugin.msg("help-del", sender);
			return null;
		}

		int index = getIndex(args[2]);
		if (index < 1) {
			plugin.msg("invalid-index", sender);
			return null;
		}

		String world = args[1];
		List<String> rules = plugin.getConfig().getStringList("rules." + world);
		if (index-1 < 0 || index > rules.size()) {
			plugin.msg("invalid-index", sender);
			return null;
		}

		rules.remove(index-1);
		return rules;
	}
	
	void showRules(Player p) {
		List<String> rulesWorld = plugin.getConfig().getStringList("rules." + p.getWorld().getName());
		for (String rule : rulesWorld) p.sendMessage(ChatColor.translateAlternateColorCodes('&', rule));
		
		List<String> rulesDefault = plugin.getConfig().getStringList("rules._default_");
		if (rulesWorld.size() == 0) {
			for (String rule : rulesDefault) p.sendMessage(ChatColor.translateAlternateColorCodes('&', rule));			
		}
		
		List<String> rulesGlobal = plugin.getConfig().getStringList("rules._global_");
		for (String rule : rulesGlobal) p.sendMessage(ChatColor.translateAlternateColorCodes('&', rule));

		if (rulesDefault.size() + rulesWorld.size() + rulesGlobal.size() == 0) {
			plugin.msg("no-rules", p);
			return;
		}
}
	
	void showWorldRules(CommandSender sender, String[] args) {
		if (args.length < 2) {
			plugin.msg("help-show", sender);
			return;
		}

		String world = args[1];
		List<String> rules = plugin.getConfig().getStringList("rules." + world);
		if (rules.size() == 0) {
			plugin.msg("no-rules", sender);
			return;
		}
		
		for (int x=0; x<rules.size(); x++) {
			int index = x+1;
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e" + index + ":&f " + rules.get(x)));
		}
	}
}
