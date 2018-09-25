/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hapcan.internal.messages.commands;

import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.hapcan.internal.exceptions.HapcanUnsupportedChannelException;

/**
 * @author Marcin Wiatrak - Initial contribution
 */
public class HapcanBlindCommand extends HapcanControlCommand {

    public enum BlindCommand {
        STOP(0), // It stops chosen blinds. Executing of the next instruction will be possible only after 1 second.
        UP_STOP(1), // It runs blind up, if it was stopped. It stops blind, if it was running.
        DOWN_STOP(2), // It runs blind down, if it was stopped. It stops blind, if it was running.
        UP(3), // It runs blind up, if was stopped. It stops, and after 1s runs blind up, if was running down.
        DOWN(4), // It runs blind down, if was stopped. It stops, and after 1s runs blind down, if was running up.
        START(5); // It stops blind if was running. It runs blind opposite way to last running, if blind was stopped.

        private final byte command;

        BlindCommand(int command) {
            this.command = (byte) command;
        }

        public byte toByte() {
            return command;
        }
    }

    private BlindCommand command;
    private short channel;

    public HapcanBlindCommand(CpuVersion cpuVersion, int destNodeId) {
        super(cpuVersion, destNodeId);
    }

    @Override
    protected void generateInstructions() {
        instr[0] = command.toByte();
        instr[1] = (byte) (channel & 0xFF);
        instr[2] = (byte) 0x00; // TIMER
    }

    @Override
    public void convertFromState(String channelId, Type type) throws HapcanUnsupportedChannelException {
        int channelNumber = -1;
        if (channelId.startsWith("blind")) {
            channelNumber = Integer.parseInt(channelId.substring(5));
        }
        if (channelNumber < 1 || channelNumber > 8) {
            throw new HapcanUnsupportedChannelException("Channel {}" + channelId + " unsupported ");
        }
        channelNumber--;
        channel = (short) (1 << channelNumber);

        if (type instanceof UpDownType) {
            if (type == UpDownType.UP) {
                command = BlindCommand.UP;
            } else {
                command = BlindCommand.DOWN;
            }
        } else if (type instanceof StopMoveType) {
            if (type == StopMoveType.MOVE) {
                command = BlindCommand.START;
            } else {
                command = BlindCommand.STOP;
            }
        } else {
            throw new HapcanUnsupportedChannelException(
                    "Channel " + channelId + " does not accept " + type.toFullString());
        }
    }

}
