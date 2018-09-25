/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hapcan.internal.connection;

import org.eclipse.smarthome.core.thing.ThingStatusDetail;

/**
 * @author Marcin Wiatrak - Initial contribution
 */
public class ConnectionStatusEvent {

    private boolean connected;

    private ThingStatusDetail statusDetail;
    private String description;

    private boolean tryToReconnect = false;

    public ConnectionStatusEvent(boolean connected) {
        this(connected, false, ThingStatusDetail.NONE, null);
    }

    public ConnectionStatusEvent(boolean connected, boolean tryToReconnect) {
        this(connected, tryToReconnect, ThingStatusDetail.NONE, null);
    }

    public ConnectionStatusEvent(boolean connected, boolean tryToReconnect, ThingStatusDetail statusDetail,
            String description) {
        this.connected = connected;
        this.statusDetail = statusDetail;
        this.description = description;
        this.tryToReconnect = tryToReconnect;
    }

    public boolean isConnected() {
        return this.connected;
    }

    public boolean tryToReconnect() {
        return this.tryToReconnect;
    }

    public String getDescription() {
        return description;
    }

    public ThingStatusDetail getStatusDetail() {
        return statusDetail;
    }

    @Override
    public String toString() {
        return String.format("%s: connected = %b, detail = %s, reason = %s", this.getClass().getName(), this.connected,
                this.statusDetail, this.description);
    }
}
