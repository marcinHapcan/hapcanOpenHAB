/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hapcan.internal.util;

import org.openhab.binding.hapcan.internal.exceptions.HapcanUnsupportedValueException;

/**
 * An Utility class to handle {@link ShortEnumWrapper} instances
 *
 * @author Marcin Wiatrak - Initial contribution
 */
public class ShortEnumUtil {

    public static <T extends ShortEnumWrapper> T fromShort(Class<T> typeClass, int input)
            throws HapcanUnsupportedValueException {
        // TODO Java Stream ??
        for (T enumValue : typeClass.getEnumConstants()) {
            if (enumValue.toShort() == input) {
                return enumValue;
            }
        }

        throw new HapcanUnsupportedValueException(typeClass, input);
    }

}
