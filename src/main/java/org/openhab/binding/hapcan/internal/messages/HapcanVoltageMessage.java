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

import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.openhab.binding.hapcan.internal.exceptions.HapcanException;

/**
 * @author Marcin Wiatrak - Initial contribution
 */
public class HapcanVoltageMessage extends HapcanStateMessage {

    private float busVoltage;
    private float cpuVoltage;

    public HapcanVoltageMessage(byte[] data, int len) throws HapcanException {
        encodeMessage(data, len);
    }

    @Override
    public void encodeMessage(byte[] buf, int len) throws HapcanException {
        super.encodeMessage(buf, len);

        int voltageTmp = ((buf[payloadPos] & 0xFF) << 8) | (buf[payloadPos + 1] & 0xFF);
        busVoltage = voltageTmp * 30.5f / 65472.0f;
        channelStates.put(CHANNEL_BUS_VOLTAGE, new QuantityType<>(busVoltage, SmartHomeUnits.VOLT));

        voltageTmp = ((buf[payloadPos + 2] & 0xFF) << 8) | (buf[payloadPos + 3] & 0xFF);
        cpuVoltage = voltageTmp * 5.0f / 65472.0f;
        channelStates.put(CHANNEL_CPU_VOLTAGE, new QuantityType<>(cpuVoltage, SmartHomeUnits.VOLT));

        logger.debug("Hapcan busVoltage: {}", String.format("%.2f", busVoltage));
        logger.debug("Hapcan cpuVoltage: {}", String.format("%.2f", cpuVoltage));
    }

    public float getBusVoltage() {
        return busVoltage;
    }

    public float getCpuVoltage() {
        return cpuVoltage;
    }
}
