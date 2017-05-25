package org.ui.wrapper;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

import org.util.Settings;

public class LocalSoundModule extends UIModule{
	private String name= null;
	private File file = null;
	private Color color = null;
	private int fontSize = 12, fontStyle = 1, offset=0;
	private String font = "Arial";
	private double volume = 1;
	
	private Thread audioThread = null;
	private boolean playing = false;
	
	/** Loads and parses this sound file using the fomratted file name.<br> 
	 * Syntax for naming:<br>
	 * FILENAME.#color.fontsize.fontstyle.fontname.ext*/
	public LocalSoundModule(File file) throws Exception{
		this.file = file;
		this.color = generateRandomColor();
		if(!this.file.exists())throw new Exception();

		String[] sp = this.file.getName().split("\\.");
		this.name = sp[0];
		try{
			if(sp.length>=3){
				this.color = Color.decode(sp[1]);
			}
			if(sp.length>=4){
				this.fontSize = Integer.parseInt(sp[2].trim());
			}
			if(sp.length>=5){
				this.fontStyle = Integer.parseInt(sp[3].trim());
			}
			if(sp.length>=6){
				this.font = sp[4].trim();
			}
			if(sp.length>=7){
				this.offset = Integer.parseInt(sp[5].trim());
			}
		}catch(Exception ignored){}
	}

	/** Attempts to play this sound. */
	public void onClick(){
		if(isPlaying()){
			stopPlaying();
			return;
		}
		try{
			System.out.println("Playing '"+file+"'.");
			this.playing = true;
			
			String outputLineName = null;
			if((outputLineName=Settings.getSetting("SpeakerOutputLine"))==null){
				System.err.println("Unset output line! Cannot activate mic stream! Check settings, or MicStreamer.java!");
				return;//I'd put GUI-based debugging here, but this shouldn't be possible as long as it's checked on startup.
			}
			this.audioThread = createPlayer(outputLineName);
			this.audioThread.start();
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public boolean isPlaying(){
		return this.audioThread!=null && this.audioThread.isAlive() && this.playing;
	}
	
	/** Terminates the playing sound, if it is playing. */
	public void stopPlaying(){
		this.playing = false;
	}
	
	//override.
	public void kill(){
		stopPlaying();
	}
	
	/** Requests a rendered image representative of this SoundOption, as it should be displayed. */
	public BufferedImage draw(int w, int h){
		BufferedImage b = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = b.createGraphics();
		g.setFont(new Font(font, fontStyle, fontSize));
		g.setColor(color!=null? color:Color.WHITE);
		g.fillRect(0, 0, w, h);
		g.setColor(Color.BLACK);
		g.setRenderingHint(
		        RenderingHints.KEY_TEXT_ANTIALIASING,
		        RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		drawString(g, name, 0, 0+offset, w, h);

		if(this.isPlaying()){
			g.setColor(new Color(0f,0f,0f,.7f));
			g.fillRect(0, 0, w, h);
		}

		g.dispose();
		return b;
	}
	
	/** Generates a random, neutral pastel color that's aesthetically pleasing. */
	private Color generateRandomColor(){
		Random random = new Random();
		int red = random.nextInt(256);
		int green = random.nextInt(256);
		int blue = random.nextInt(256);

		// mix the color with White (255,255,255)
		red = (red + 255) / 2;
		green = (green + 255) / 2;
		blue = (blue + 255) / 2;

		Color color = new Color(red, green, blue);
		return color;
	}
	
	
	private Thread createPlayer(final String outputLineName){
		Thread t = new Thread(){
			public void run(){
				try{
					AudioFormat format = new AudioFormat(96000.0f, 16, 1, true, true);
					SourceDataLine sourceDataLine = null, primeDataLine = null;
					Mixer.Info[] mixers = AudioSystem.getMixerInfo();
					
					for (Mixer.Info mixerInfo : mixers){
					    //System.out.println(mixerInfo);
					    if(mixerInfo.getName().startsWith(outputLineName)){
					    	//System.out.println("Swapping mixer lines.");
					    	Mixer m = AudioSystem.getMixer(mixerInfo);
					    	sourceDataLine = (SourceDataLine)m.getLine(m.getSourceLineInfo()[0]);
					    	break;
					    }
					}
					
					for (Mixer.Info mixerInfo : mixers){//We need the Primary Data Line (As it's called in Windows AFAIK) to play back locally.
					    if(mixerInfo.getName().startsWith("Primary")){
					    	Mixer m = AudioSystem.getMixer(mixerInfo);
					    	primeDataLine = (SourceDataLine)m.getLine(m.getSourceLineInfo()[0]);
					    	break;
					    }
					}
					
					sourceDataLine.open(format);
					sourceDataLine.start();
					
					List<FloatControl> volumes = new ArrayList<FloatControl>();
					if (sourceDataLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
						System.out.println("Setting volume on out...");
						FloatControl volume = (FloatControl) sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);
						volumes.add(volume);
					}else{
						System.err.println("Unable to hook output volume control!");
					}
					
					primeDataLine.open(format);
					primeDataLine.start();

					if (primeDataLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
						System.out.println("Setting volume on Prime...");
						FloatControl volume = (FloatControl) primeDataLine.getControl(FloatControl.Type.MASTER_GAIN);
						volumes.add(volume);
					}else{
						System.err.println("Unable to hook client-side output volume control.");
					}

					AudioInputStream audioStream =  AudioSystem.getAudioInputStream(format, AudioSystem.getAudioInputStream(file));//We open the file here, and convert to the required format.
					
					int r=0;
					byte[] buff = new byte[1024];
					while((r=audioStream.read(buff))!=-1 && LocalSoundModule.this.playing){
						scaleVolumes(volumes, volume);
						sourceDataLine.write(buff, 0, r);//We're taking one input stream and copying it over to both the outgoing and the local speaker lines.
						primeDataLine.write(buff, 0, r);
					}
					
					System.out.println("Finished playing");
					sourceDataLine.drain();//Shut it all down.
					sourceDataLine.close();
					primeDataLine.drain();
					primeDataLine.close();
					audioStream.close();
					LocalSoundModule.this.playing = false;
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		};
		return t;
	}
	
	public void setVolume(double volume){
		this.volume = volume;
	}
	
	/**
	 * Adjust all the supplied controls to (roughly) the desired volume, scaling based off original clip volume.
	 * @param controls - The audio lone FloatControls to adjust.
	 * @param volume - 0-1, the % the clips should play at.
	 */
	private static final void scaleVolumes(List<FloatControl> controls, double volume){
		for(FloatControl c : controls){
			float min = c.getMinimum() / 4;
			if (volume != 1) {
				c.setValue(min * (1 - (float)volume));
			}else{
				c.setValue(0);
			}
		}
	}

}
