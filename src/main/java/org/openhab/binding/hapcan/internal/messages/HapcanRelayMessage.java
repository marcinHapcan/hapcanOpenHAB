/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hapcan.internal.messages;

import static org.openhab.binding.hapcan.HapcanBindingConstants.CHANNEL_OUTPUT;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.hapcan.internal.exceptions.HapcanException;

/**
 * @author Marcin Wiatrak - Initial contribution
 */
public class HapcanRelayMessage extends HapcanStateMessage {

    public HapcanRelayMessage(byte[] data, int len) throws HapcanException {
        encodeMessage(data, len);
    }

    @Override
    public void encodeMessage(byte[] buf, int len) throws HapcanException {
        super.encodeMessage(buf, len);

        int channel = buf[payloadPos + 2] & 0xFF;
        byte status = buf[payloadPos + 3];

        String channelId = CHANNEL_OUTPUT + channel;
        if (status == (byte) 0x00 || status == (byte) 0xFF) {
            State state = status == (byte) 0xFF ? OnOffType.ON : OnOffType.OFF;
            channelStates.put(channelId, state);
        }
    }

}
