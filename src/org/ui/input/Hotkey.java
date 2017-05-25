package org.ui.input;

import java.util.concurrent.CopyOnWriteArrayList;

import org.ui.wrapper.UIModule;

public abstract class Hotkey {
	/** The keys required (combod, if more than one) to activate this Hotkey's trigger function. */
	public abstract String[] keys();
	
	/** Get the name of this hotkey, as stored in preferences. */
	public abstract String name();
	
	/** Called if the keycombo that matches this Hotkey has been pressed. */
	public abstract void trigger(CopyOnWriteArrayList<UIModule> modules);
	
	/** Built to simplify building the key string for this Hotkey, if needed. */
	public String keyString(){
		String ret = "";
		for(String k : this.keys())
			ret+=k.toLowerCase().trim()+",";
		return ret;
	}
}
