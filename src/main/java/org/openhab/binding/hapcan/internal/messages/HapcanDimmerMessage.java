/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hapcan.internal.messages;

import org.eclipse.smarthome.core.library.types.PercentType;
import org.openhab.binding.hapcan.internal.exceptions.HapcanException;
import org.openhab.binding.hapcan.internal.util.HapcanUtils;

/**
 * @author Marcin Wiatrak - Initial contribution
 */
public class HapcanDimmerMessage extends HapcanStateMessage {

    public HapcanDimmerMessage(byte[] data, int len) throws HapcanException {
        encodeMessage(data, len);
    }

    @Override
    public void encodeMessage(byte[] buf, int len) throws HapcanException {
        super.encodeMessage(buf, len);

        int channel = buf[payloadPos + 2] & 0xFF;
        byte status = buf[payloadPos + 3];

        String channelId = "dimmer" + channel;
        PercentType state = HapcanUtils.convertByteToPercentType(status);
        logger.debug("RGB {} state: {}", channelId, state.intValue());
        channelStates.put(channelId, state);
    }

}
