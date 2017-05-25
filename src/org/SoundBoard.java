package org;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;

import org.ui.ControlPanel;
import org.util.Converter;
import org.util.Settings;


public class SoundBoard {
	//http://vbaudio.jcedeveloppement.com/Download_CABLE/VBCABLE_Driver_Pack43.zip -- CABLE driver for virtual audio card
	
	//TODO: Build a launcher/classloader to release along-side this main Jar, to enable automatic update downloading.
	public static void main(String[] args){
		checkOutput();
		Converter.checkSounds();
		
		ControlPanel ctrl = new ControlPanel();
		ctrl.create();
	}
	
	
	private static void checkOutput(){
		Mixer.Info[] mixers = AudioSystem.getMixerInfo();
		System.out.println("Loading...");
		while(Settings.getSetting("SpeakerOutputLine")==null){
			String[] options = new String[mixers.length];
			for(int i=0;i<options.length;i++){
				options[i]=mixers[i].getName();
			}
			Settings.prompt("SpeakerOutputLine", "Select Output Speaker", "Select the digital speaker line to output sound over:", options);
		}
	}
}
