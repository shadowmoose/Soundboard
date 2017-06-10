package org.ui.input.hotkeys;

import java.util.concurrent.CopyOnWriteArrayList;

import org.ui.modules.MicModule;
import org.ui.modules.UIModule;

public class HKMute extends Hotkey{

	@Override
	public String[] keys() {
		return new String[]{"hold", "ctrl", "m"};
	}

	@Override
	public String name() {
		return "Mic_Mute";
	}

	@Override
	public void trigger(CopyOnWriteArrayList<UIModule> modules) {
		for(UIModule m : modules){
			if(m instanceof MicModule){
				m.onClick();
			}
		}
	}

}
