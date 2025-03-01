// Generated for: VitalEdit-Paper
// Sat Mar  1 03:02:34 AM EST 2025
package com.poixson.vitaledit;

import org.bukkit.plugin.java.JavaPlugin;


public class PoiXsonPluginModAdapter_Paper_VitalEdit extends JavaPlugin implements PoiXsonPluginModAdapter {

	public final VitalEditPlugin xplugin;



	public PoiXsonPluginModAdapter_Paper_VitalEdit() {
		super();
		this.xplugin = new VitalEditPlugin();
	}



	@Override public void onLoad() {    super.onLoad();    this.xplugin.onLoad();    }
	@Override public void onEnable() {  super.onEnable();  this.xplugin.onEnable();  }
	@Override public void onDisable() { super.onDisable(); this.xplugin.onDisable(); }



}
