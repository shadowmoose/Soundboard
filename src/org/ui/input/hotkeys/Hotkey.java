package org.ui.input.hotkeys;

import java.util.concurrent.CopyOnWriteArrayList;

import org.ui.modules.UIModule;

public abstract class Hotkey {
	public boolean active = false;
	
	/** The keys required (combod, if more than one) to activate this Hotkey's trigger function. */
	public abstract String[] keys();
	
	/** Get the name of this hotkey, as stored in preferences. */
	public abstract String name();
	
	/** Called if the keycombo that matches this Hotkey has been pressed. */
	public abstract void trigger(CopyOnWriteArrayList<UIModule> modules);
	
	/** Triggered when this hotkey's combo has been released. Override for push-to-talk, etc. */
	public void onRelease(){}
	
	/** Built to simplify building the key string for this Hotkey, if needed. */
	public String keyString(){
		String ret = "";
		for(String k : this.keys())
			ret+=k.toLowerCase().trim()+",";
		return ret;
	}
	
	/** Check if this key string is a push-to-activate. */
	public static boolean isToggle(String[] keys){
		for(String s : keys)
			if(s.toLowerCase().equals("hold"))
				return true;
		return false;
	}
}
