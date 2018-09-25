/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hapcan.internal.config;

/**
 * The {@link HapcanBridgeConfig} class contains fields mapping thing configuration parameters.
 *
 * @author Marcin Wiatrak - Initial contribution
 */
public class HapcanBridgeConfig {

    /**
     * @return IP or hostname of the HAPCAN Ethernet Interface
     */
    public String host;

    /**
     * @return TCP port of the Interface
     */
    public int port;

    public int moduleNumber;
    public int groupNumber;
    public int connectionTimeout;
    public int keepAliveDelay;
    public int initDelay;

}
