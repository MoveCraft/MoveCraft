package com.bukkit.yogoda.movecraft;
//import java.awt.Toolkit;
import java.util.Timer;
import java.util.TimerTask;

public class ReminderBeep {
  //Toolkit toolkit;

  Timer timer;

  public ReminderBeep(int seconds) {
    //toolkit = Toolkit.getDefaultToolkit();
    timer = new Timer();
    timer.schedule(new RemindTask(), seconds * 1000);
  }

  class RemindTask extends TimerTask {
    public void run() {
      System.out.println("Time's up!");
      timer.cancel(); //Not necessary because we call System.exit
      //System.exit(0); //Stops the AWT thread (and everything else)
    }
  }

  public static void main(String args[]) {
    //System.out.println("About to schedule task.");
    new ReminderBeep(5);
    //System.out.println("Task scheduled.");
  }
}
           
        