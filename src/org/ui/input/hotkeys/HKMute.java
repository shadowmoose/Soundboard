package org.ui.input.hotkeys;

import java.util.concurrent.CopyOnWriteArrayList;

import org.ui.input.Hotkey;
import org.ui.wrapper.MicModule;
import org.ui.wrapper.UIModule;

public class HKMute extends Hotkey{

	@Override
	public String[] keys() {
		return new String[]{"ctrl", "m"};
	}

	@Override
	public String name() {
		return "Mute";
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
