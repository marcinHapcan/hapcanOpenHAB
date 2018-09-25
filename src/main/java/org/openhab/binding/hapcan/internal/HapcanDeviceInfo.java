/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hapcan.internal;

import static org.openhab.binding.hapcan.HapcanBindingConstants.*;

import java.util.Map;

import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.hapcan.internal.messages.HapcanDescriptionMessage;
import org.openhab.binding.hapcan.internal.messages.HapcanFirmwareMessage;
import org.openhab.binding.hapcan.internal.messages.HapcanHardwareMessage;
import org.openhab.binding.hapcan.internal.messages.HapcanSystemMessage;
import org.openhab.binding.hapcan.internal.messages.commands.HapcanControlCommand.CpuVersion;

/**
 * @author Marcin Wiatrak - Initial contribution
 */
public class HapcanDeviceInfo {
    private static final String NOT_AVAILABLE = "Not available";

    private boolean secondDescrPart = false;

    private String moduleName;
    private Short hardwareType;
    private Byte hardwareVersion;
    private Integer serialNumber;
    private Byte applicationType;
    private Byte applicationVersion;
    private Byte firmwareVersion;
    private Short bootloaderVersion;
    private CpuVersion cpuVersion = CpuVersion.UNKNOWN;

    boolean hardwareSet = false;
    boolean firmwareSet = false;
    boolean descriptionSet = false;

    public static String getPropertyString(Object object) {
        if (object == null) {
            return NOT_AVAILABLE;
        }

        if (object instanceof Byte) {
            return String.format("%02X", object);
        } else if (object instanceof Short) {
            return String.format("%04X", object);
        } else if (object instanceof Integer) {
            return String.format("%08X", object);
        } else {
            return object.toString();
        }
    }

    public void processSystemMessage(HapcanSystemMessage message) {
        if (message instanceof HapcanHardwareMessage) {
            HapcanHardwareMessage systemMessage = (HapcanHardwareMessage) message;

            hardwareType = systemMessage.getHardwareType();
            hardwareVersion = systemMessage.getHardwareVersion();
            serialNumber = systemMessage.getSerialNumber();
            refreshCpuVersion();
            hardwareSet = true;
        } else if (message instanceof HapcanFirmwareMessage) {
            HapcanFirmwareMessage systemMessage = (HapcanFirmwareMessage) message;

            hardwareType = systemMessage.getHardwareType();
            hardwareVersion = systemMessage.getHardwareVersion();
            applicationType = systemMessage.getApplicationType();
            applicationVersion = systemMessage.getApplicationVersion();
            firmwareVersion = systemMessage.getFirmwareVersion();
            bootloaderVersion = systemMessage.getBootloaderVersion();
            refreshCpuVersion();
            firmwareSet = true;
        } else if (message instanceof HapcanDescriptionMessage) {
            HapcanDescriptionMessage systemMessage = (HapcanDescriptionMessage) message;

            if (!secondDescrPart || moduleName == null) {
                moduleName = systemMessage.getDescription();
            } else {
                moduleName += systemMessage.getDescription();
                descriptionSet = true;
            }
            secondDescrPart = !secondDescrPart;
        }
    }

    private void refreshCpuVersion() {
        if (hardwareType == (short) 0x3000) {
            if (hardwareVersion == (byte) 0x01) {
                cpuVersion = CpuVersion.UNIV1;
            } else if (hardwareVersion == (byte) 0x03) {
                cpuVersion = CpuVersion.UNIV3;
            }
        }
    }

    private String getCpuInfo() {
        if (hardwareType == null || hardwareVersion == null) {
            return NOT_AVAILABLE;
        }

        if (hardwareType == (short) 0x3000) {
            return String.format("UNIV %d", hardwareVersion & 0xFF);
        } else {
            return String.format("(0x%04X,0x%02X)", hardwareType & 0xFFFF, hardwareVersion & 0xFF);
        }
    }

    private String getBootloaderVersion() {
        if (bootloaderVersion == null) {
            return NOT_AVAILABLE;
        }

        return String.format("%d.%d", bootloaderVersion >> 8, bootloaderVersion & 0xFF);
    }

    private String getFirmwareVersion() {
        if (hardwareType == null || hardwareVersion == null || applicationType == null || applicationVersion == null
                || firmwareVersion == null) {
            return NOT_AVAILABLE;
        }
        return getCpuInfo()
                + String.format(".%d.%d.%d", applicationType & 0xFF, applicationVersion & 0xFF, firmwareVersion & 0xFF);
    }

    public String getDeviceModel() {
        if (hardwareType == null || hardwareVersion == null || applicationType == null || applicationVersion == null
                || firmwareVersion == null) {
            return NOT_AVAILABLE;
        }
        String cpuInfo = getCpuInfo();
        String name = cpuInfo + String.format(".%d.%d.", applicationType & 0xFF, applicationVersion & 0xFF);
        if (cpuInfo.equals("UNIV1")) {
            name += (firmwareVersion & 0xFF);
        } else {
            name += 'x';
        }

        return name;
    }

    public Map<String, String> updateProperties(Map<String, String> properties) {
        properties.put(PROPERTY_SERIAL_NUMBER, HapcanDeviceInfo.getPropertyString(serialNumber));
        properties.put(PROPERTY_MODULE_NAME, HapcanDeviceInfo.getPropertyString(moduleName));
        properties.put(PROPERTY_CPU_INFO, getCpuInfo());
        properties.put(PROPERTY_BOOTLOADER_VERSION, getBootloaderVersion());
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, getFirmwareVersion());
        return properties;
    }

    public String getModuleName() {
        return moduleName;
    }

    public CpuVersion getCpuVersion() {
        return cpuVersion;
    }

    public void clearSecondDescrPart() {
        this.secondDescrPart = false;
    }

    public boolean hasWholeInfo() {
        return (hardwareSet && firmwareSet && descriptionSet);
    }

}
