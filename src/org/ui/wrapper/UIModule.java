package org.ui.wrapper;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public abstract class UIModule {
	/** The method run when this module is clicked. */
	public abstract void onClick();
	/** Requests a rendered image representative of this Module, as it should be displayed. */
	public abstract BufferedImage draw(int w, int h);
	/** Called when the SoundBoard screen is closed, expects that this module cleans up anything it started before, here. */
	public abstract void kill();
	
	/** Renders the given text at (x, y) to fit withing box (w, h), trimming lines at the nearest space. 
	 * @return The original y + the added height of the text after splitting & rendering, to ease drawing multiple strings this way.*/
 	public int drawString(Graphics2D g, String text, int x, int y, int w, int h){
		String l = text;
		int max_char = w/g.getFontMetrics().charWidth('A');
		int line=0;
		while(l.length()>0){
			String ch = l.substring(0,(max_char>l.length()?l.length():max_char));
			if(ch.length()==max_char && ch.contains(" "))ch = ch.substring(0, ch.lastIndexOf(" ")).trim();
			if(ch.contains("\n"))ch = ch.substring(0, ch.indexOf("\n")).trim();
			l=l.replace(ch, "").trim();
			line++;
		}
		double height = line*g.getFontMetrics().getHeight();
		int offset=0;
		y-=height/4;

		l = text;
		line=0;
		while(l.length()>0){
			String ch = l.substring(0,(max_char>l.length()?l.length():max_char));
			if(ch.length()==max_char && ch.contains(" "))ch = ch.substring(0, ch.lastIndexOf(" ")).trim();
			if(ch.contains("\n"))ch = ch.substring(0, ch.indexOf("\n")).trim();
			g.drawString(ch, (x+w/2)-g.getFontMetrics().stringWidth(ch)/2,y+h/2+(g.getFontMetrics().getHeight()*line));
			//System.out.println(ch);
			l=l.replace(ch, "").trim();
			line++;
		}
		return y+line*g.getFontMetrics().getHeight()-offset;
	}
}
