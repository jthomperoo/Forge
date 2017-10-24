package me.jamiethompson.forge;

import me.jamiethompson.forge.Data.ForgeAccount;

/**
 * Created by jamie on 29/09/17.
 * Used for interaction between storage and generator, to load generator with loaded values
 */

public interface LoadInterface {
    /**
     * Loads an account into the generator activity
     *
     * @param account the account to load into the generator
     */
    void load(ForgeAccount account);
}
