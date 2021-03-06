package org.util;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;

/** 
 * Java won't support files which aren't in WAV format, so we offer an automatic built-in conversion using FFmpeg, if available.
 */
public class Converter {
	
	/** Prompt the user to see if they'd like to convert */
	private static boolean prompt(){
		ArrayList<File> conversions = getConvertibleFiles();
		
		if(conversions.size()==0 || !Settings.prompt("File Conversion Available", "The filetype '.wav' is required in order to be played. Would you like to convert non-matching filetypes using FFMpeg?"))
			return false;
		return true;
	}
	
	public static void checkSounds(){
		if(!prompt())return;
		System.out.println("Converting...");

		JFrame dialog = new JFrame("Converting in Progress");
		dialog.add(new JLabel("Conversion is in progress. This window will close automatically on completion."));
		dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		dialog.setLocationRelativeTo(null);
		dialog.pack();
		dialog.setVisible(true);
		
		for(File f : getConvertibleFiles()){
			System.out.println("Converting "+f.getName());
			try{
				String newName = f.getName().substring(0, f.getName().lastIndexOf('.'))+".wav";
				Process pb = new ProcessBuilder("ffmpeg","-y", "-i","\""+f.getAbsolutePath()+"\"", f.getParentFile().getAbsolutePath()+File.separator+newName).start();
				pb.waitFor();
				System.out.println("Exit: "+pb.exitValue());
				if(pb.exitValue()==0)
					f.delete();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		dialog.dispose();
	}
	
	private static ArrayList<File>getConvertibleFiles(){
		ArrayList<File> conversions = new ArrayList<File>();
		File dir = new File(Settings.getSetting("SOUND_DIR"));
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
			for (File child : directoryListing) {
				if(!child.getName().endsWith("wav")){
					conversions.add(child);
				}

			}
		}
		return conversions;
	}
	
}
