/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hapcan.internal.exceptions;

/**
 * Exception for when HAPCAN messages have a value that we don't understand.
 *
 * @author Marcin Wiatrak - Initial contribution
 */
public class HapcanUnsupportedValueException extends HapcanException {

    private static final long serialVersionUID = 402781611495845169L;

    public HapcanUnsupportedValueException(Class<?> enumeration, String value) {
        super("Unsupported value '" + value + "' for " + enumeration.getSimpleName());
    }

    public HapcanUnsupportedValueException(Class<?> enumeration, int value) {
        this(enumeration, String.valueOf(value));
    }
}
