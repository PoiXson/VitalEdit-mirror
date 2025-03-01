// Generated for: {{{TITLE}}}
// {{{TIMESTAMP}}}
package com.poixson.{{{NAME-LOWER}}};

import org.bukkit.plugin.java.JavaPlugin;


public class PoiXsonPluginModAdapter_Paper_{{{NAME}}} extends JavaPlugin implements PoiXsonPluginModAdapter {

	public final {{{NAME}}}Plugin xplugin;



	public PoiXsonPluginModAdapter_Paper_{{{NAME}}}() {
		super();
		this.xplugin = new {{{NAME}}}Plugin();
	}



	@Override public void onLoad() {    super.onLoad();    this.xplugin.onLoad();    }
	@Override public void onEnable() {  super.onEnable();  this.xplugin.onEnable();  }
	@Override public void onDisable() { super.onDisable(); this.xplugin.onDisable(); }



}
