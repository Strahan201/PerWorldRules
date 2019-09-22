package com.sylvcraft;

import org.bukkit.plugin.java.JavaPlugin;

import com.sylvcraft.commands.Rules;

import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class PerWorldRules extends JavaPlugin {
	@Override
	public void onEnable() {
		getCommand("rules").setExecutor(new Rules(this));
		saveDefaultConfig();
	}
	
  public void msg(String msgCode, CommandSender sender) {
  	String tmp = getConfig().getString("messages." + msgCode, msgCode) + ' ';
  	if (tmp.trim().equals("")) return;
  	for (String m : tmp.split("%br%")) {
  		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', m));
  	}
  }

  public void msg(String msgCode, CommandSender sender, Map<String, String> data) {
  	String tmp = getConfig().getString("messages." + msgCode, msgCode) + ' ';
  	if (tmp.trim().equals("")) return;
  	for (Map.Entry<String, String> mapData : data.entrySet()) {
  	  tmp = tmp.replace(mapData.getKey(), mapData.getValue());
  	}
  	msg(tmp, sender);
  }
}