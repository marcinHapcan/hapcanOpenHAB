/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hapcan.internal.messages.commands;

import java.math.BigDecimal;

import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.hapcan.internal.exceptions.HapcanUnsupportedChannelException;

/**
 * @author Marcin Wiatrak - Initial contribution
 */
public class HapcanRgbCommand extends HapcanControlCommand {

    public enum RgbControllerCommand {
        SET_VALUE(0),
        TOGGLE(1),
        STEP_DOWN(2),
        STEP_UP(3),
        SET_VALUE_SOFTLY(4);

        private final byte command;

        RgbControllerCommand(int command) {
            this.command = (byte) command;
        }

        public byte toByte() {
            return command;
        }
    }

    private RgbControllerCommand command;
    private byte channel;
    private byte value;
    private int upDownChange;

    public HapcanRgbCommand(CpuVersion cpuVersion, int destNodeId, int upDownChange) {
        super(cpuVersion, destNodeId);
        this.upDownChange = upDownChange;
    }

    @Override
    protected void generateInstructions() {
        instr[0] = (byte) ((command.toByte() << 2) | channel);
        instr[1] = value;
        instr[2] = (byte) 0x00; // TIMER
    }

    @Override
    public void convertFromState(String channelId, Type type) throws HapcanUnsupportedChannelException {
        channel = -1;
        if (channelId.startsWith("dimmer")) {
            channel = Byte.parseByte(channelId.substring(6));
        }
        if (channel < 1 || channel > 4) {
            throw new HapcanUnsupportedChannelException("Channel " + channelId + " unsupported");
        }

        channel--;

        if (type instanceof OnOffType) {
            if (type == OnOffType.ON) {
                command = RgbControllerCommand.SET_VALUE;
                value = (byte) 0xFF;
            } else {
                command = RgbControllerCommand.SET_VALUE;
                value = (byte) 0x00;
            }
        } else if (type instanceof IncreaseDecreaseType) {
            command = type == IncreaseDecreaseType.INCREASE ? RgbControllerCommand.STEP_UP
                    : RgbControllerCommand.STEP_DOWN;
            value = (byte) upDownChange; // Ignored in UNIV1
        } else if (type instanceof PercentType) {
            command = RgbControllerCommand.SET_VALUE;
            value = convertPercentTypeToByte((PercentType) type);
        } else {
            throw new HapcanUnsupportedChannelException(
                    "Channel " + channelId + " does not accept " + type.toFullString());
        }
    }

    private byte convertPercentTypeToByte(PercentType percent) {
        return percent.toBigDecimal().multiply(BigDecimal.valueOf(255))
                .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP).byteValue();
    }

    // public byte[] colorTemperature(PercentType percentage) {
    // byte[] command = COLOR_TEMPERATURE_COMMAND.clone();
    // command[4] = percentage.byteValue();
    // command[5] = (byte) (100 - percentage.intValue());
    // return command;
    // }

}
