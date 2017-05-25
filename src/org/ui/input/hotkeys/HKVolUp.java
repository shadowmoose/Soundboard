package org.ui.input.hotkeys;

import java.util.concurrent.CopyOnWriteArrayList;

import org.ui.input.Hotkey;
import org.ui.wrapper.LocalSoundModule;
import org.ui.wrapper.UIModule;

public class HKVolUp extends Hotkey{

	@Override
	public String[] keys() {
		return new String[]{"ctrl", "up"};
	}

	@Override
	public String name() {
		return "Volume_Up";
	}

	@Override
	public void trigger(CopyOnWriteArrayList<UIModule> modules) {
		for(UIModule m : modules){
			if(!(m instanceof LocalSoundModule))
				continue;
			LocalSoundModule lsm = (LocalSoundModule)m;
			lsm.setVolume(1);
		}
	}

}
