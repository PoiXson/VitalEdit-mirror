// Generated for: {{{TITLE}}}
// {{{TIMESTAMP}}}
package com.poixson.{{{NAME-LOWER}}};

import net.fabricmc.api.ModInitializer;


public class PoiXsonPluginModAdapter_Fabric_{{{NAME}}} implements ModInitializer, PoiXsonPluginModAdapter {

	public final {{{NAME}}}Plugin xplugin;



	public PoiXsonPluginModAdapter_Fabric_{{{NAME}}}() {
		super();
		this.xplugin = new {{{NAME}}}Plugin();
	}



//	@Override public void onLoad() {    this.xplugin.onLoad();    }
//	@Override public void onEnable() {  this.xplugin.onEnable();  }
//	@Override public void onDisable() { this.xplugin.onDisable(); }



	@Override
	public void onInitialize() {
System.out.println("Hello from FABRIC!!!");
	}



}
