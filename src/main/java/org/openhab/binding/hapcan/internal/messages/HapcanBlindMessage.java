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

import org.eclipse.smarthome.core.library.types.PercentType;
import org.openhab.binding.hapcan.internal.BlindMoveType;
import org.openhab.binding.hapcan.internal.exceptions.HapcanException;

/**
 * @author Marcin Wiatrak - Initial contribution
 */
public class HapcanBlindMessage extends HapcanStateMessage {

    public HapcanBlindMessage(byte[] data, int len) throws HapcanException {
        encodeMessage(data, len);
    }

    @Override
    public void encodeMessage(byte[] buf, int len) throws HapcanException {
        super.encodeMessage(buf, len);

        int channel = buf[payloadPos + 2] & 0xFF;
        byte status = buf[payloadPos + 3]; // estimated blind status (0x00-0xFF)
        byte moveStatus = buf[payloadPos + 4]; // defines movement: 0x00 – blind stopped,
                                               // 0x01 – going down, 0x02 – going up

        String channelDimmerId = CHANNEL_BLIND + channel;
        String channelDimmerMoveId = CHANNEL_BLIND_MOVE + channel;
        PercentType statusState = convertByteToPercentType(status);
        logger.debug("BLIND {} state: {}", channelDimmerId, statusState.intValue());
        channelStates.put(channelDimmerId, statusState);

        BlindMoveType moveStatusState;
        switch (moveStatus) {
            case (byte) 0x00:
                moveStatusState = BlindMoveType.STOPPED;
                break;
            case (byte) 0x01:
                moveStatusState = BlindMoveType.GOING_DOWN;
                break;
            case (byte) 0x02:
                moveStatusState = BlindMoveType.GOING_UP;
                break;
            default:
                throw new HapcanException(
                        "Channel " + channelDimmerMoveId + " - unknown state " + String.format("%02X", moveStatus));
        }
        channelStates.put(channelDimmerMoveId, moveStatusState);
    }

    private PercentType convertByteToPercentType(byte value) {
        return new PercentType((int) Math.round(((value & 0xFF) / 255.0) * 100.0));
    }

}
