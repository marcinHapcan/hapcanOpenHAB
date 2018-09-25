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
 * Exception for HAPCAN errors.
 *
 * @author Marcin Wiatrak - Initial contribution
 */
public class HapcanException extends Exception {

    private static final long serialVersionUID = 2975102966905930260L;

    public HapcanException() {
        super();
    }

    public HapcanException(String message) {
        super(message);
    }

    public HapcanException(String message, Throwable cause) {
        super(message, cause);
    }

    public HapcanException(Throwable cause) {
        super(cause);
    }

}
