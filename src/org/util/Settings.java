package org.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JOptionPane;

/** Global settings, as loaded from the settings file. */
public class Settings {
	private final static String FILE = "settings.ini";
	private final static ConcurrentHashMap<String, String> SETTINGS = new ConcurrentHashMap<String, String>();
	
	
	/** Prompt a user to select an option, which is then stored and saved in SETTINGS under <i>settingKey</i>. */
	public static void prompt(String settingKey, String title, String prompt, String[] options){
		String s = (String) JOptionPane.showInputDialog(
                null,
                prompt,
                title,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);
		addSetting(settingKey, s);
	}

	/** Prompt the user for a Y/N answer. No data is saved or added here. */
	public static boolean prompt(String title, String text){
		return JOptionPane.showConfirmDialog(null, text, title, 0)==0;
	}
	
	
	/** Add a setting to the map. This also triggers saving the settings again. */
	public static void addSetting(String key, String value){
		SETTINGS.put(key, value);
		storeAll();
	}
	
	/** Returns the saved setting, if it exists. */
	public static String getSetting(String key){
		if(SETTINGS.size()==0)loadSettings();
		return SETTINGS.get(key);
	}

	/** Store all the current settings. */
	private static void storeAll(){
		try{
			FileOutputStream o = new FileOutputStream(FILE);
			for(String key : SETTINGS.keySet()){
				o.write((key+"="+SETTINGS.get(key)+"\r\n").getBytes());
			}
			o.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}


	/** Load all settings stored in the settings file. */
	private static void loadSettings(){
		SETTINGS.clear();
		try{
			if(new File(FILE).exists()){
				BufferedReader in = new BufferedReader(new FileReader(FILE));
				String s;
				while((s=in.readLine())!=null){
					String k,v;
					if(!s.contains("="))continue;
					k = s.substring(0,s.indexOf('='));
					v = s.substring(s.indexOf('=')+1);
					SETTINGS.put(k, v);
				}
				in.close();
			}
			if(SETTINGS.get("SOUND_DIR")==null){//hard-code the sound directory.
				SETTINGS.put("SOUND_DIR", "Sounds");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
}
