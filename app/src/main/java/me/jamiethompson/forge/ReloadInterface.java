package me.jamiethompson.forge;


/**
 * Created by jamie on 29/09/17.
 * Used for interaction between generator and storage, to reload storage with newly saved values
 */

public interface ReloadInterface {
    /**
     * Called when the storage needs to reload its current values
     */
    void reload();
}
