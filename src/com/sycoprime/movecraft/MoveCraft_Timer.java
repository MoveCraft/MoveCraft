package com.sycoprime.movecraft;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.entity.Player;

public class MoveCraft_Timer {
	//needs to be migrated to bukkitscheduler. meh.
	
	Timer timer;
	Craft craft;
	//public String state = "";
	public static HashMap<Player, MoveCraft_Timer> playerTimers = new HashMap<Player, MoveCraft_Timer>();

	public MoveCraft_Timer(int seconds, Craft vehicle, String state, boolean forward) {
		//toolkit = Toolkit.getDefaultToolkit();
		this.craft = vehicle;
		timer = new Timer();
		if(state.equals("engineCheck"))
			timer.scheduleAtFixedRate(new EngineTask(), 1000, 1000);
		else if(state.equals("engineCheck"))
			timer.schedule(new AutoMoveTask(forward), 1000);
		else
			timer.schedule(new ReleaseTask(), seconds * 1000);
	}
	
	public void SetState(String newState) {
		//state = newState;
	}
	
	public void Destroy() {
		timer.cancel();
		craft = null;
	}
	
	class EngineTask extends TimerTask {
		public void run() {
			if(craft == null)
				timer.cancel();
			else
				craft.engineTick();
			return;
		}
	}
	
	class AutoMoveTask extends TimerTask {
		boolean MovingForward = false;
		
		public void run() {
			craft.WayPointTravel(MovingForward);
			timer.schedule(new AutoMoveTask(MovingForward), 1000);
		}
		
		public AutoMoveTask(boolean Forward) {
			MovingForward = Forward;
		}
	}

	class ReleaseTask extends TimerTask {
		public void run() {
			/*
			if(state.equals("engineCheck")) {
				craft.engineTick();
			}else
			if(state.equals("abandonCheck")) {
			*/				
				if(craft != null) {
					MoveCraft.instance.releaseCraft(craft.player, craft);
				}
				timer.cancel();
				return;
				
			//}
		}
	}
}

