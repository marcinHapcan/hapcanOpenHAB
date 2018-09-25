/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hapcan.internal.util;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.PercentType;

/**
 * @author Marcin Wiatrak - Initial contribution
 */
public class HapcanUtils {

    // used for hex conversions
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes, int len, @Nullable CharSequence delimiter) {
        return Arrays.stream(toObjects(bytes, len)).map(b -> {
            int v = b & 0xFF;
            return "" + HEX_ARRAY[v >>> 4] + HEX_ARRAY[v & 0x0F];
        }).collect(Collectors.joining(delimiter != null ? delimiter : ""));
    }

    public static String bytesToHex(byte[] bytes, int len) {
        return bytesToHex(bytes, len, null);
    }

    private static Byte[] toObjects(byte[] bytes, int len) {
        Byte[] bytesObjects = new Byte[len];
        Arrays.setAll(bytesObjects, n -> bytes[n]);
        return bytesObjects;
    }

    public static PercentType convertByteToPercentType(byte value) {
        return new PercentType((int) Math.round(((value & 0xFF) / 255.0) * 100.0));
    }

}
