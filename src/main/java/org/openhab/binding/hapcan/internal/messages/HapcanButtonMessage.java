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

import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.thing.CommonTriggerEvents;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.hapcan.internal.exceptions.HapcanException;

/**
 * @author Marcin Wiatrak - Initial contribution
 */
public class HapcanButtonMessage extends HapcanStateMessage {

    private static final String HELD_400MS = "HELD400_MS";
    private static final String HELD_4S = "HELD_400S";

    public HapcanButtonMessage(byte[] data, int len) throws HapcanException {
        encodeMessage(data, len);
    }

    @Override
    public void encodeMessage(byte[] buf, int len) throws HapcanException {
        super.encodeMessage(buf, len);

        int channel = buf[payloadPos + 2] & 0xFF;
        byte eventType = buf[payloadPos + 3];

        if (channel >= 0x20) {
            // TODO TOUCH BUTTON PARAMETERS FRAME ??
            return;
        }

        String inputChannelId = CHANNEL_INPUT + channel;
        String triggerChannelId = CHANNEL_TRIGGER + channel;
        if (eventType == (byte) 0x00 || eventType == (byte) 0xFF) {
            State state = eventType == (byte) 0xFF ? OpenClosedType.CLOSED : OpenClosedType.OPEN;
            channelStates.put(inputChannelId, state);
        }

        if (!isResponse) {
            switch (eventType) {
                case (byte) 0x00:
                    channelTriggers.put(triggerChannelId, CommonTriggerEvents.RELEASED);
                    break;
                case (byte) 0xFF:
                    channelTriggers.put(triggerChannelId, CommonTriggerEvents.PRESSED);
                    break;
                case (byte) 0xFC:
                    channelTriggers.put(triggerChannelId, CommonTriggerEvents.SHORT_PRESSED);
                    break;
                case (byte) 0xFB:
                    channelTriggers.put(triggerChannelId, CommonTriggerEvents.DOUBLE_PRESSED);
                    break;
                case (byte) 0xFA:
                    channelTriggers.put(triggerChannelId, CommonTriggerEvents.LONG_PRESSED);
                    break;
                case (byte) 0xFE:
                    channelTriggers.put(triggerChannelId, HELD_400MS);
                    break;
                case (byte) 0xFD:
                    channelTriggers.put(triggerChannelId, HELD_4S);
                    break;
                default:
                    throw new HapcanException("Unknown button event type");
            }
        }
    }

}
