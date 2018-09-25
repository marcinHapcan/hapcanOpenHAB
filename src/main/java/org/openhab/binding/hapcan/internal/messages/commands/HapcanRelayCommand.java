/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hapcan.internal.messages.commands;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.hapcan.internal.exceptions.HapcanUnsupportedChannelException;

/**
 * @author Marcin Wiatrak - Initial contribution
 */
public class HapcanRelayCommand extends HapcanControlCommand {

    public enum RelayCommand {
        OFF(0),
        ON(1),
        TOGGLE(2);

        private final byte command;

        RelayCommand(int command) {
            this.command = (byte) command;
        }

        public byte toByte() {
            return command;
        }
    }

    private RelayCommand command;
    private short channel;
    private boolean extendedChannels;

    public HapcanRelayCommand(CpuVersion cpuVersion, int destNodeId, boolean extendedChannels) {
        super(cpuVersion, destNodeId);
        this.extendedChannels = extendedChannels;
    }

    @Override
    protected void generateInstructions() {
        instr[0] = command.toByte();
        instr[1] = (byte) (channel & 0xFF);

        if (extendedChannels) {
            instr[2] = (byte) ((channel & 0xFF) >> 8);
            instr[3] = (byte) 0x00; // TIMER
        } else {
            instr[2] = (byte) 0x00; // TIMER
        }
    }

    @Override
    public void convertFromState(String channelId, Type type) throws HapcanUnsupportedChannelException {
        boolean channelOk = false;
        int channelNumber = -1;

        if (channelId.startsWith("output")) {
            channelNumber = Integer.parseInt(channelId.substring(6));
        } else if (channelId.startsWith("led")) {
            channelNumber = Integer.parseInt(channelId.substring(3));
        }
        if (channelNumber >= 1
                && ((!extendedChannels && channelNumber <= 8) || (extendedChannels && channelNumber <= 16))) {
            channelOk = true;
        }

        if (!channelOk) {
            throw new HapcanUnsupportedChannelException("Channel {}" + channelId + " unsupported ");
        }

        channelNumber--;
        channel = (short) (1 << channelNumber);

        if (type instanceof OnOffType) {
            command = (type == OnOffType.ON ? RelayCommand.ON : RelayCommand.OFF);
        } else {
            throw new HapcanUnsupportedChannelException("Channel " + channelId + " does not accept " + type);
        }
    }

}
