package org.ui.input;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.ui.input.hotkeys.HKMute;
import org.ui.input.hotkeys.HKVolDown;
import org.ui.input.hotkeys.HKVolUp;
import org.ui.input.hotkeys.Hotkey;
import org.ui.modules.UIModule;
import org.util.Settings;

/** Handles listening for hotkey presses. */
public class HotkeyListener implements NativeKeyListener{
	
	private CopyOnWriteArrayList<String> pressed = new CopyOnWriteArrayList<String>();
	/** Passed to this hotkey handler by ControlPanel, serves as a reference backwards for hotkey events.*/
	CopyOnWriteArrayList<UIModule> modules;
	
	/** All available hotkeys should be written in here. */
	private Hotkey[] hotkeys = new Hotkey[]{new HKMute(), new HKVolUp(), new HKVolDown()};
	
	public HotkeyListener(CopyOnWriteArrayList<UIModule> modules) {
		this.modules = modules;
		for(Hotkey h : hotkeys){
			if(Settings.getSetting(h.name()+"_hotkey")==null){
				Settings.addSetting(h.name()+"_hotkey", h.keyString());
			}
		}
	}

	public void nativeKeyPressed(NativeKeyEvent e) {
		String code = NativeKeyEvent.getKeyText(e.getKeyCode()).toLowerCase();
		pressed.addIfAbsent(code);
		
		for(Hotkey h : hotkeys){
			if(Settings.getSetting(h.name()+"_hotkey")==null){
				Settings.addSetting(h.name()+"_hotkey", h.keyString());
			}
			String[] keys = Settings.getSetting(h.name()+"_hotkey").trim().split(",");
			boolean active = true;
			for(String k : keys){
				if(!pressed.contains(k) && !"hold".equals(k)){
					active = false;
					break;
				}
			}
			if(active){
				if(Hotkey.isToggle(keys)){
					if(h.active)continue;
					h.active = true;
				}
				System.out.println("Triggered Hotkey: "+h.name());
				h.trigger(this.modules);
			}
		}
	}
	
	public void nativeKeyReleased(NativeKeyEvent e) {
		String code = NativeKeyEvent.getKeyText(e.getKeyCode()).toLowerCase();
		pressed.remove(code);
		
		for(Hotkey h : hotkeys){
			String[] keys = Settings.getSetting(h.name()+"_hotkey").trim().split(",");
			if(!Hotkey.isToggle(keys) || !h.active)
				continue;
			boolean cancel = false;
			for(String k : keys){
				if(!pressed.contains(k)){
					cancel = true;
					break;
				}
			}
			if(cancel){
				h.active = false;
				h.trigger(this.modules);
				System.out.println("Killed Hotkey: "+h.name());
			}
		}
	}
	
	public void nativeKeyTyped(NativeKeyEvent e) {}

	/** Required that this be called, tells the HKL to register and start listening/processing. */
	public void register() {
		try {
			//Disable verbose logging from keyboard hook.
			Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
			logger.setLevel(Level.OFF);
			logger.setUseParentHandlers(false);
			GlobalScreen.registerNativeHook();
			
			/** Register to globally accept key events. */
			GlobalScreen.addNativeKeyListener(this);
		} catch (NativeHookException e1) {
			e1.printStackTrace();
		}
	}
	
	/** Terminates the HKL and cleans up by unregistering the System Hooks. 
	 * Important that this be called, to terminate the JVM cleanly.*/
	public void cleanup(){
		try {
			GlobalScreen.unregisterNativeHook();
		} catch (NativeHookException e1) {
			System.exit(2);
			e1.printStackTrace();
		}
	}

}
