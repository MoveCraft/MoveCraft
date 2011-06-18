package com.sycoprime.movecraft;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

public class MoveCraftMoveEvent extends Event implements Cancellable {

    private static final long serialVersionUID = 1L;
    private Vector movement;
    private final Craft craft;
    private boolean cancelled;
    
    protected MoveCraftMoveEvent(Craft craft, int x, int y, int z) {
        super("MoveCraftMoveEvent");
        this.movement = new Vector(x, y, z);
        this.craft = craft;
        this.cancelled = false;
    }
    
    public Vector getMovement() {
        return movement;
    }

    public void setMovement(Vector movement) {
        this.movement = movement;
    }

    public Craft getCraft() {
        return craft;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
