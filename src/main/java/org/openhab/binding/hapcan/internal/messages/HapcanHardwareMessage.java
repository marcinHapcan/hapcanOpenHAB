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
public class HapcanHardwareMessage extends HapcanRecvMessage implements HapcanSystemMessage {

    private short hardwareType;
    private byte hardwareVersion;
    private int serialNumber;

    public HapcanHardwareMessage(byte[] data, int len) throws HapcanException {
        encodeMessage(data, len);
    }

    @Override
    public void encodeMessage(byte[] buf, int len) throws HapcanException {
        super.encodeMessage(buf, len);

        hardwareType = (short) (((buf[payloadPos] & 0xFF) << 8) | (buf[payloadPos + 1] & 0xFF));
        hardwareVersion = buf[payloadPos + 2];
        serialNumber = ((buf[payloadPos + 4] & 0xFF) << 24) | ((buf[payloadPos + 5] & 0xFF) << 16)
                | ((buf[payloadPos + 6] & 0xFF) << 8) | (buf[payloadPos + 7] & 0xFF);

        logger.debug("Hapcan hardwareType: {}", String.format("%04X", hardwareType));
        logger.debug("Hapcan hardwareVersion: {}", String.format("%02X", hardwareVersion));
        logger.debug("Hapcan serialNumber: {}", String.format("%08X", serialNumber));
    }

    // @Override
    // public void refreshDevice(HapcanDeviceInfo deviceInfo) {
    // deviceInfo.setHardwareType(hardwareType);
    // deviceInfo.setHardwareVersion(hardwareVersion);
    // deviceInfo.setSerialNumber(serialNumber);
    // }

    public short getHardwareType() {
        return hardwareType;
    }

    public byte getHardwareVersion() {
        return hardwareVersion;
    }

    public int getSerialNumber() {
        return serialNumber;
    }
}
