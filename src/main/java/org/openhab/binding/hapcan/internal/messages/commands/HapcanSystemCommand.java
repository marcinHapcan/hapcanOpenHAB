/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hapcan.internal.messages.commands;

import org.openhab.binding.hapcan.internal.messages.HapcanBaseMessage;
import org.openhab.binding.hapcan.internal.messages.HapcanFrameType;

/**
 * @author Marcin Wiatrak - Initial contribution
 */
public class HapcanSystemCommand extends HapcanBaseMessage implements HapcanCommand {

    private int srcNodeId, destNodeId;

    public HapcanSystemCommand(HapcanFrameType frameType) {
        super(frameType, true);
    }

    public HapcanSystemCommand(HapcanFrameType frameType, int destNodeId) {
        super(frameType, false);
        this.destNodeId = destNodeId;
    }

    @Override
    public byte[] decodeMessage() {
        int len = bridgeMessage ? 5 : 15;
        byte[] message = new byte[len];

        message[0] = (byte) 0xAA;
        message[1] = (byte) (frameId >> 4);
        message[2] = (byte) ((frameId & 0xF) << 4);

        if (!bridgeMessage) {
            message[3] = (byte) (srcNodeId & 0xFF); // module ID
            message[4] = (byte) (srcNodeId >> 8); // group ID

            message[5] = (byte) 0xFF;
            message[6] = (byte) 0xFF;

            message[7] = (byte) (destNodeId & 0xFF); // module ID
            message[8] = (byte) (destNodeId >> 8); // group ID

            message[9] = (byte) 0xFF;
            message[10] = (byte) 0xFF;
            message[11] = (byte) 0xFF;
            message[12] = (byte) 0xFF;
        }

        message[len - 2] = calcChecksum(message);
        message[len - 1] = (byte) 0xA5;

        return message;
    }

    @Override
    public void setSrcNodeId(int srcNodeId) {
        this.srcNodeId = srcNodeId;
    }

}
