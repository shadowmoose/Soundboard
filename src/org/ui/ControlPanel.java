package org.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.ui.input.HotkeyListener;
import org.ui.modules.LocalSoundModule;
import org.ui.modules.MicModule;
import org.ui.modules.UIModule;
import org.util.Converter;
import org.util.DirectoryWatcher;
import org.util.Settings;

public class ControlPanel extends JPanel{
	private static final long serialVersionUID = -1405633901274153343L;
	public static final double VERSION = 2.1;
	
	private int ROWS = 4, COLUMNS = 5;
	private CopyOnWriteArrayList<UIModule> modules = new CopyOnWriteArrayList<UIModule>();
	private HotkeyListener hkl;
	
	/** Creates and sets up listeners for a new GUI Control Panel */
	public ControlPanel(){
		if(hkl==null){
			hkl = new HotkeyListener(modules);
			hkl.register();
		}
		loadModules();
	}
	
	/** Display everything. */
	public void create(){
		final JFrame f = new JFrame("Soundboard "+VERSION);
		f.setSize(500, 500);
		
		f.add(this);
		
		f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		f.setLocationRelativeTo(null);
		f.setVisible(true);
		f.addWindowListener(new WindowAdapter(){
		    public void windowClosing(WindowEvent e){
		    	System.err.println("WINDOW CLOSED!");
		    	killModules();
		    	hkl.cleanup();
		    }
		});
		
		f.addMouseWheelListener(new MouseWheelListener(){
			public void mouseWheelMoved(MouseWheelEvent e) {
				int notches = e.getWheelRotation();
				for(UIModule m : modules){
					if(!(m instanceof LocalSoundModule))
						continue;
					LocalSoundModule lsm = (LocalSoundModule)m;
					
					lsm.setVolume((int) (5*Math.signum(notches)*-1) );//Mousewheel ticks are inverted.
				}
			}
		});
		this.addMouseListener(createMouseListener());
		
		new Thread("Repainter"){
			public void run(){
				try{
					while(ControlPanel.this.isValid()){
						ControlPanel.this.repaint();
						
						for(UIModule m : modules){
							if(m instanceof LocalSoundModule){
								//Should probably find a better way to update the title.
								f.setTitle("Soundboard "+VERSION+" - Volume: "+(int)((((LocalSoundModule) m).getVolume()/100d)*100)+"%");
								break;
							}
						}
						
						Thread.sleep(200);
					}
					System.out.println("Rendering completed.");
				}catch(Exception e){}
			}
		}.start();
		
		// Refresh whenever we detect a file change.
		new DirectoryWatcher( new File(Settings.getSetting("SOUND_DIR")) ){
			public void handle(File f, Event e){
				if(e==Event.DELETE)
					return;
				if(!f.getName().endsWith(".wav"))
					Converter.checkSounds();
				loadModules();
			}
		};
	}
	
	/** Triggers the kill event on all loaded modules. */
	private void killModules(){
		System.out.println("Terminating all modules.");
		
    	for(UIModule o : modules){
    		try{
    			o.kill();
    		}catch(Exception e){
    			e.printStackTrace();
    			System.exit(1);
    		}
    	}
	}
	
	/** Create a MouseListener for this panel. */
	private MouseListener createMouseListener(){
		MouseListener ml = new MouseListener(){
			public void mousePressed(MouseEvent me) {
				int x = me.getX()/(ControlPanel.this.getWidth()/ROWS);
				int y = me.getY()/(ControlPanel.this.getHeight()/COLUMNS);
				int selection = y*ROWS+x;
				if(selection<modules.size())
					modules.get(selection).onClick();
			}
			public void mouseEntered(MouseEvent arg0) {}
			public void mouseExited(MouseEvent arg0) {}
			public void mouseClicked(MouseEvent arg0) {}
			public void mouseReleased(MouseEvent arg0) {}
			
		};
		return ml;
	}
	
	/** Populate SoundOption list with all available files. */
	public void loadModules(){
		killModules();
		modules.clear();
		modules.add(new MicModule());
		
		File dir = new File(Settings.getSetting("SOUND_DIR"));
		if(!dir.exists()){
			System.err.println("Invalid directory: '"+Settings.getSetting("SOUND_DIR")+"'!");
			return;
		}
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
			for (File child : directoryListing) {
				try{
					if(child.getName().endsWith("wav"))
						modules.add(new LocalSoundModule(child));
					else{
						//modules.add(new SoundModule(child));
						//Support for non-wavs not yet implemented.
					}
				}catch(Exception e){
					e.printStackTrace();
					System.err.println("Failed to add file to list.");
				}
			}
		}
		ROWS = (int) Math.floor(Math.sqrt(modules.size()));
		COLUMNS = (int) Math.ceil((double)modules.size()/(double)ROWS);
		System.out.println("Modules loaded: "+modules.size());
	}

	public void paint(Graphics gg){
		int width = getWidth(), height = getHeight();
		BufferedImage b = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) b.getGraphics();
		g.setColor(Color.gray);
		g.fillRect(0, 0, width, height);
		
		int sw=(int)Math.ceil((double)width/(double)ROWS), sh = (int)Math.ceil((double)height/(double)COLUMNS);
		
		for(int i=0; i<modules.size(); i++){
			int bx = (i%ROWS)*(sw);
			int by = (i/ROWS)*(sh);
			BufferedImage o = modules.get(i).draw(sw, sh);
			g.drawImage(o, bx, by, null);
		}
		
		grid(g, width, height);
		
		gg.drawImage(b, 0, 0, null);
	}
	
	
	/** Render a grid. **/
	private void grid(Graphics2D g, int width, int height){
		int wPer = (int)Math.ceil((double)width/(double)ROWS), hPer = (int)Math.ceil((double)height/(double)COLUMNS);
		g.setColor(Color.BLACK);
		
		for(int x=0; x<=ROWS; x++){
			g.drawLine(x*wPer, 0, x*wPer, height);
		}
		
		for(int y=0; y<=COLUMNS; y++){
			g.drawLine(0, y*hPer, width, y*hPer);
		}
	}
	

}
