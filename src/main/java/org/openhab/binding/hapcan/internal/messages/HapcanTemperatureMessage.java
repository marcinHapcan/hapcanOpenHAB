/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hapcan.internal.messages;

import static org.openhab.binding.hapcan.HapcanBindingConstants.*;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.hapcan.internal.exceptions.HapcanException;

/**
 * @author Marcin Wiatrak - Initial contribution
 */
public class HapcanTemperatureMessage extends HapcanStateMessage {

    public HapcanTemperatureMessage(byte[] data, int len) throws HapcanException {
        encodeMessage(data, len);
    }

    @Override
    public void encodeMessage(byte[] buf, int len) throws HapcanException {
        super.encodeMessage(buf, len);

        byte frameSubType = buf[payloadPos + 2];

        switch (frameSubType) {
            case (byte) 0x11: // temperature frame
                short temperatureValue = (short) (((buf[payloadPos + 3] & 0xFF) << 8) | (buf[payloadPos + 4] & 0xFF));
                channelStates.put(CHANNEL_TEMPERATURE, convertShortToTemperature(temperatureValue));
                break;
            case (byte) 0x12: // thermostat frame
                byte thermPosition = buf[payloadPos + 3];
                byte thermState = buf[payloadPos + 7];

                // 0x00 - temperature below setpoint
                // 0xFF - temperature above setpoint
                // 0x80 – power up value
                if (thermPosition == (byte) 0x00) {
                    channelStates.put(CHANNEL_THERMOSTAT_POSITION, OpenClosedType.OPEN);
                } else if (thermPosition == (byte) 0xFF) {
                    channelStates.put(CHANNEL_THERMOSTAT_POSITION, OpenClosedType.CLOSED);
                }

                // 0x00 - turned off,
                // 0xFF - turned on,
                channelStates.put(CHANNEL_THERMOSTAT_POSITION,
                        (thermState == (byte) 0xFF) ? OpenClosedType.CLOSED : OpenClosedType.OPEN);

                break;

            default:
                throw new HapcanException("Unknown temperature frame type");
        }

    }

    State convertShortToTemperature(short value) {
        boolean minus = ((value & 0xFFFF) >> 11) != 0;

        int temp = ((value & 0xFFFF) >> 4) & 0x7F;
        float fraction = 0.0625f * ((value & 0xFFFF) & 0xF);
        if (minus) {
            temp -= 128;
        }

        return new DecimalType(temp + fraction);
    }

}
