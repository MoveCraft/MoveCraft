package com.bukkit.yogoda.movecraft;
//import java.awt.Toolkit;
import java.util.Timer;
import java.util.TimerTask;

public class MoveCraft_Timer {
	//Toolkit toolkit;

	Timer timer;
	Craft craft;

	public MoveCraft_Timer(int seconds, Craft vehicle) {
		//toolkit = Toolkit.getDefaultToolkit();
		this.craft = vehicle;
		timer = new Timer();
		timer.schedule(new ReleaseTask(), seconds * 1000);
	}
	
	public void Destroy() {
		timer.cancel();
	}

	class ReleaseTask extends TimerTask {
		public void run() {
			if(craft != null) {
				craft.player.sendMessage("§e" + craft.type.sayOnRelease);
				Craft.removeCraft(craft);
			}
			timer.cancel(); //Not necessary because we call System.exit
			//System.exit(0); //Stops the AWT thread (and everything else)
			return;
		}
	}
}

