package org.ui.wrapper;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.mic.MicStreamer;

public class MicModule extends UIModule{
	ArrayList<Integer>points = new ArrayList<Integer>();
	
	public void onClick() {
		MicStreamer.toggle();
		points.clear();
	}

	public BufferedImage draw(int w, int h) {
		BufferedImage b = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = b.createGraphics();
		g.setColor(Color.decode("#a5adb0"));
		g.fillRect(0, 0, w, h);
		g.setColor(Color.BLACK);
		drawString(g, MicStreamer.isStreaming()?"Mic On":"Mic Off", 0, 0, w, h);

		if(!MicStreamer.streaming){
			g.setColor(new Color(1f,1f,1f,.7f));
			g.fillRect(0, 0, w, h);
		}else{
			int maxPoints = 50;
			int offy = 15;
			int mh = h/2-offy;
			int bx = w/2-maxPoints/2;
			
			
			g.setColor(Color.DARK_GRAY);
			g.fillRect(w/2-maxPoints/2, h-offy-mh, maxPoints, mh);
			g.setColor(Color.gray);
			g.drawLine(bx, h-offy-mh/2, bx+maxPoints, h-offy-mh/2);
			g.setColor(Color.GREEN);
			for(int i=0; i<points.size();i++){
				g.drawLine(bx+i, h-offy, bx+i, h-offy- (int)((double)mh*(points.get(i)/100d)) );
			}
			
			g.setColor(Color.BLACK);
			g.drawRect(w/2-maxPoints/2, h-offy-mh, maxPoints, mh);
			points.add(MicStreamer.audioLevel);
			if(points.size()>maxPoints)points.remove(0);
		}

		g.dispose();
		return b;
	}

	
	public void kill() {
		System.out.println("Stopping mic stream.");
		MicStreamer.stop();
	}
	
}
