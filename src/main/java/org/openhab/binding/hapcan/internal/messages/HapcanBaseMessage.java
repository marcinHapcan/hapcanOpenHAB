/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hapcan.internal.messages;

/**
 * @author Marcin Wiatrak - Initial contribution
 */
public abstract class HapcanBaseMessage {

    protected boolean bridgeMessage = false;
    protected int frameId;
    protected HapcanFrameType frameType;

    public HapcanBaseMessage() {
    }

    public HapcanBaseMessage(HapcanFrameType frameType, boolean bridgeMessage) {
        this.frameType = frameType;
        this.frameId = frameType.getTypeId();
        this.bridgeMessage = bridgeMessage;
    }

    public byte calcChecksum(byte[] buf) {
        byte checksum = 0;
        int len = bridgeMessage ? 2 : 12;

        for (int i = 1; i < len + 1; i++) {
            checksum += buf[i];
        }

        return checksum;
    }

    public HapcanFrameType getFrameType() {
        return frameType;
    }

    public boolean isBridgeMessage() {
        return bridgeMessage;
    }

}
