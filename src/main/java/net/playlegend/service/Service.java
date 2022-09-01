package net.playlegend.service;

import net.playlegend.exception.ServiceInitializeException;
import net.playlegend.exception.ServiceShutdownException;

public interface Service {

    void initialize() throws ServiceInitializeException;

    void shutdown() throws ServiceShutdownException;

}
