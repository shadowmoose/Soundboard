package org.mic;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import org.util.Settings;

public class MicStreamer {
	
	public static boolean streaming = false;
	public static int audioLevel = 0;
	
	public static void toggle(){
		if(streaming){
			streaming=false;
		}else{
			streaming=true;
			new Thread(){
				public void run(){
					pipeMicFeed();
				}
			}.start();
		}
	}
	
	/** Kills the active mic stream, if one is active. */
	public static void stop(){
		streaming = false;
	}
	
	public static boolean isStreaming(){
		return streaming;
	}
	
	private static void pipeMicFeed() {//http://vbaudio.jcedeveloppement.com/Download_CABLE/VBCABLE_Driver_Pack43.zip -- CABLE driver for virtual audio card
		AudioFormat format = new AudioFormat(96000.0f, 16, 1, true, true);
		//System.out.println(format);
		TargetDataLine microphone;
		SourceDataLine sourceDataLine;
		try {
			microphone = AudioSystem.getTargetDataLine(format);

			DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
			microphone = (TargetDataLine) AudioSystem.getLine(info);
			microphone.open(format, 96000/10);
			
			int numBytesRead;
			int CHUNK_SIZE = 1024;
			byte[] data = new byte[microphone.getBufferSize() / 5];

			sourceDataLine = null;
			Mixer.Info[] mixers = AudioSystem.getMixerInfo();
			
			String outputLineName = null;
			if((outputLineName=Settings.getSetting("SpeakerOutputLine"))==null){
				System.err.println("Unset output line! Cannot activate mic stream! Check settings, or MicStreamer.java!");
				return;
			}
			
			for (Mixer.Info mixerInfo : mixers){
			    //System.out.println(mixerInfo);
			    if(mixerInfo.getName().startsWith(outputLineName)){
			    	System.out.println("Swapping mixer lines.");
			    	Mixer m = AudioSystem.getMixer(mixerInfo);
			    	try{
			    		sourceDataLine = (SourceDataLine)m.getLine(m.getSourceLineInfo()[0]);
			    	}catch(Exception e){
			    		e.printStackTrace();
			    	}
			    	break;
			    }
			}
			sourceDataLine.open(format);
			sourceDataLine.start();

			microphone.start();

			try {
				while (streaming) {
					numBytesRead = microphone.read(data, 0, CHUNK_SIZE);
					audioLevel = calculateRMSLevel(data);
					sourceDataLine.write(data, 0, numBytesRead);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Block and wait for internal buffer of the
			// data line to empty.
			sourceDataLine.drain();
			sourceDataLine.close();
			microphone.close();
			System.out.println("Mic stream closed.");
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}

	/** Calc the relative volume of an audio sample. */
	public static int calculateRMSLevel(byte[] audioData){
		long lSum = 0;
		for(int i=0; i < audioData.length; i++)
			lSum = lSum + audioData[i];

		double dAvg = lSum / audioData.length;
		double sumMeanSquare = 0d;

		for(int j=0; j < audioData.length; j++)
			sumMeanSquare += Math.pow(audioData[j] - dAvg, 2d);

		double averageMeanSquare = sumMeanSquare / audioData.length;

		return (int)(Math.pow(averageMeanSquare,0.5d) + 0.5);
	}
}