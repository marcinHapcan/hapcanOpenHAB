/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hapcan.internal.messages;

import static org.openhab.binding.hapcan.internal.util.ShortEnumUtil.fromShort;

import org.openhab.binding.hapcan.internal.exceptions.HapcanException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marcin Wiatrak - Initial contribution
 */
public abstract class HapcanRecvMessage extends HapcanBaseMessage {
    protected final Logger logger = LoggerFactory.getLogger(HapcanRecvMessage.class);

    private int srcNodeId;
    protected boolean isResponse = false;
    protected int messageLength;
    protected int payloadPos;

    public void encodeMessage(byte[] buf, int len) throws HapcanException {
        this.messageLength = len;
        this.bridgeMessage = (len == 13);
        this.frameId = ((buf[1] & 0xFF) << 4) | ((buf[2] & 0xFF) >> 4);
        // this.frameType = HapcanFrameType.getFrameType(intFrameType);
        this.frameType = fromShort(HapcanFrameType.class, this.frameId);
        this.isResponse = (buf[2] & 1) == 1;

        if (len == 13) {
            this.payloadPos = 3;
            this.srcNodeId = -1;
        } else {
            this.payloadPos = 5;
            this.srcNodeId = ((buf[4] & 0xFF) << 8) | (buf[3] & 0xFF);
        }

        // logger.debug("Hapcan intFrameType: {}", String.format("%04X", frameId));
        // logger.debug("Hapcan frameType: {}", String.format("%04X", this.frameType.getTypeId()));
        // logger.debug("Hapcan responseFlag: {}", this.responseFlag);
        // logger.debug("Hapcan srcNodeId: {}", String.format("%04X", this.srcNodeId));

    }

    public int getSrcNodeId() {
        return srcNodeId;
    }

}
