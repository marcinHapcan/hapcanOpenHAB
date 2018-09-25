/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hapcan.internal.messages.commands;

import java.util.Arrays;

import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.hapcan.internal.exceptions.HapcanUnsupportedChannelException;
import org.openhab.binding.hapcan.internal.messages.HapcanBaseMessage;
import org.openhab.binding.hapcan.internal.messages.HapcanFrameType;

/**
 * @author Marcin Wiatrak - Initial contribution
 */
public abstract class HapcanControlCommand extends HapcanBaseMessage implements HapcanCommand {

    public enum CpuVersion {
        UNIV1,
        UNIV3,
        UNKNOWN
    };

    private int srcNodeId, destNodeId;
    protected byte[] instr = new byte[6];
    protected CpuVersion cpuVersion;

    public HapcanControlCommand(CpuVersion cpuVersion, int destNodeId) {
        super(HapcanFrameType.ControlMessage, false);
        this.destNodeId = destNodeId;
        this.cpuVersion = cpuVersion;
        Arrays.fill(instr, (byte) 0xFF);
    }

    @Override
    public byte[] decodeMessage() {
        byte[] message = new byte[15];

        generateInstructions();

        message[0] = (byte) 0xAA;

        message[1] = (byte) (frameId >> 4);
        message[2] = (byte) ((frameId & 0xF) << 4);

        message[3] = (byte) (srcNodeId & 0xFF); // module ID
        message[4] = (byte) (srcNodeId >> 8); // group ID

        message[7] = (byte) (destNodeId & 0xFF); // module ID
        message[8] = (byte) (destNodeId >> 8); // group ID

        if (cpuVersion != CpuVersion.UNIV1) {
            message[5] = instr[0];
            message[6] = instr[1];
            message[9] = instr[2];
            message[10] = instr[3];
            message[11] = instr[4];
            message[12] = instr[5];
        } else {
            message[5] = (byte) 0xFF;
            message[6] = (byte) 0xFF;
            message[9] = (byte) 0xFF;
            message[10] = instr[0];
            message[11] = instr[1];
            message[12] = instr[2];
        }

        message[13] = calcChecksum(message);
        message[14] = (byte) 0xA5;

        return message;
    }

    protected void generateInstructions() {
    }

    @Override
    public void setSrcNodeId(int srcNodeId) {
        this.srcNodeId = srcNodeId;
    }

    public abstract void convertFromState(String channelId, Type type) throws HapcanUnsupportedChannelException;

}
