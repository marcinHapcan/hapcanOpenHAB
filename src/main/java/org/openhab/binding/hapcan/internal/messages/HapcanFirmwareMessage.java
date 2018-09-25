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
public class HapcanFirmwareMessage extends HapcanRecvMessage implements HapcanSystemMessage {

    private short hardwareType;
    private byte hardwareVersion;
    private byte applicationType;
    private byte applicationVersion;
    private byte firmwareVersion;
    private short bootloaderVersion;

    public HapcanFirmwareMessage(byte[] data, int len) throws HapcanException {
        encodeMessage(data, len);
    }

    @Override
    public void encodeMessage(byte[] buf, int len) throws HapcanException {
        super.encodeMessage(buf, len);

        hardwareType = (short) (((buf[payloadPos] & 0xFF) << 8) | (buf[payloadPos + 1] & 0xFF));
        hardwareVersion = buf[payloadPos + 2];
        applicationType = buf[payloadPos + 3];
        applicationVersion = buf[payloadPos + 4];
        firmwareVersion = buf[payloadPos + 5];
        bootloaderVersion = (short) (((buf[payloadPos + 6] & 0xFF) << 8) | (buf[payloadPos + 7] & 0xFF));

        logger.debug("Hapcan hardwareType: {}", String.format("%04X", hardwareType));
        logger.debug("Hapcan hardwareVersion: {}", String.format("%02X", hardwareVersion));
        logger.debug("Hapcan applicationType: {}", String.format("%02X", applicationType));
        logger.debug("Hapcan applicationVersion: {}", String.format("%02X", applicationVersion));
        logger.debug("Hapcan firmwareVersion: {}", String.format("%02X", firmwareVersion));
        logger.debug("Hapcan bootloaderVersion: {}", String.format("%04X", bootloaderVersion));
    }

    public short getHardwareType() {
        return hardwareType;
    }

    public byte getHardwareVersion() {
        return hardwareVersion;
    }

    public byte getApplicationType() {
        return applicationType;
    }

    public byte getApplicationVersion() {
        return applicationVersion;
    }

    public byte getFirmwareVersion() {
        return firmwareVersion;
    }

    public short getBootloaderVersion() {
        return bootloaderVersion;
    }
}
