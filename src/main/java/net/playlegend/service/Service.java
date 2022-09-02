package net.playlegend.service;

import net.playlegend.LegendPerm;
import net.playlegend.exception.ServiceInitializeException;
import net.playlegend.exception.ServiceShutdownException;

public abstract class Service {

    protected final LegendPerm plugin;

    public Service(LegendPerm plugin) {
        this.plugin = plugin;
    }

    public abstract void initialize() throws ServiceInitializeException;

    public abstract void shutdown() throws ServiceShutdownException;

}
