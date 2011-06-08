package com.sycoprime.movecraft;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class MoveCraftTurnEvent extends Event implements Cancellable {
    
    private static final long serialVersionUID = 1L;
    private int degrees;
    private boolean cancelled;
    private final Craft craft;
    
    public MoveCraftTurnEvent(Craft craft, int degrees) {
        super("MoveCraftTurnEvent");
        this.craft = craft;
        this.setDegrees(degrees);
        this.cancelled = false;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public void setDegrees(int degrees) {
        this.degrees = degrees;
    }

    public int getDegrees() {
        return degrees;
    }

    public Craft getCraft() {
        return craft;
    }
}
