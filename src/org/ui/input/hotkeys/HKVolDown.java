package org.ui.input.hotkeys;

import java.util.concurrent.CopyOnWriteArrayList;

import org.ui.input.Hotkey;
import org.ui.wrapper.LocalSoundModule;
import org.ui.wrapper.UIModule;

public class HKVolDown extends Hotkey{

	@Override
	public String[] keys() {
		return new String[]{"ctrl", "down"};
	}

	@Override
	public String name() {
		return "Volume_Down";
	}

	@Override
	public void trigger(CopyOnWriteArrayList<UIModule> modules) {
		for(UIModule m : modules){
			if(!(m instanceof LocalSoundModule))
				continue;
			LocalSoundModule lsm = (LocalSoundModule)m;
			lsm.setVolume(-1);
		}
	}

}
