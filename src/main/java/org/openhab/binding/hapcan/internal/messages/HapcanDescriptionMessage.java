/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hapcan.internal.messages;

import org.openhab.binding.hapcan.internal.exceptions.HapcanException;

/**
 * @author Marcin Wiatrak - Initial contribution
 */
public class HapcanDescriptionMessage extends HapcanRecvMessage implements HapcanSystemMessage {
    String description;

    public HapcanDescriptionMessage(byte[] data, int len) throws HapcanException {
        encodeMessage(data, len);
    }

    @Override
    public void encodeMessage(byte[] buf, int len) throws HapcanException {
        super.encodeMessage(buf, len);

        description = new String(buf, payloadPos, 8).trim();
        logger.debug("description len: {}", description.length());
        logger.debug("Hapcan description: {}", description);
    }

    public String getDescription() {
        return description;
    }
}
