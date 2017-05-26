package org.util;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

public class DirectoryWatcher {
	private File dir;
	private Thread thread;
	
	/** Creates and starts a Threaded watcher, that will trigger this DirectoryWatcher's {@link #handle()} method with any files that are created or modified within the directory. <br>
	 * This watcher runs as a daemon, and will terminate when all other non-daemon Threads are complete.*/
	public DirectoryWatcher(File dir){
		this.dir = dir;
		this.thread = new Thread("FileWatcher-"+dir.getAbsolutePath()+"-"+Math.random()){
			public void run(){
				watchDirectoryPath();
			}
		};
		this.thread.setDaemon(true);
		this.thread.start();
	}
	
	private void watchDirectoryPath() {
		Path path = this.dir.toPath();
		// Sanity check - Check if path is a folder
		try {
			Boolean isFolder = (Boolean) Files.getAttribute(path,"basic:isDirectory", NOFOLLOW_LINKS);
			if (!isFolder) {
				throw new IllegalArgumentException("Path: " + path + " is not a folder");
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
		FileSystem fs = path.getFileSystem ();
		try(WatchService service = fs.newWatchService()) {
			path.register(service, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
			
			// Start infinite polling loop
			WatchKey key = null;
			while(true) {
				key = service.take();
				
				for(WatchEvent<?> watchEvent : key.pollEvents()) {
					if (watchEvent.kind() == OVERFLOW) {
						//Ignore misc events.
						continue;
					} else {
						//File was modified/created.
						//Event context is FUBAR and uses relative paths, so we assemble a full absolute path:
						Path dir = (Path)key.watchable();
						Path fullPath = dir.resolve((Path)watchEvent.context());
						
						// pass fully-built absolute filepath off to save handler:
						this.handle(fullPath.toFile());
					}
				}

				if(!key.reset()) {
					break;
				}
			}
		} catch(IOException | InterruptedException ioe) {
			ioe.printStackTrace();
		}

	}
	
	/**
	 * Called automatically whenever a File is deleted, modified, or created within this Watcher's Directory.
	 * @param f - The File that was added or changed. <b>This file may not exist on a delete.</b>
	 */
	public void handle(File f){}
}
