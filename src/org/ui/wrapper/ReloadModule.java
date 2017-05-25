package org.ui.wrapper;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.ui.ControlPanel;

public class ReloadModule extends UIModule{

	@Override
	public void onClick() {
		System.out.println("Reloading sounds...");
		ControlPanel.loadModules();
	}

	@Override
	public BufferedImage draw(int w, int h) {
		BufferedImage b = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = b.createGraphics();
		g.setColor(Color.GRAY);
		g.fillRect(0, 0, w, h);
		g.setColor(Color.BLACK);
		drawString(g, "Update Sounds", 0, 0, w, h);
		
		g.dispose();
		return b;
	}

	@Override
	public void kill() {
		// TODO Auto-generated method stub
		
	}

}
