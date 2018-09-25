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
 * Exception to indicate that a request was received for an unsupported channel
 *
 * @author Marcin Wiatrak - Initial contribution
 */
public class HapcanUnsupportedChannelException extends HapcanException {

    private static final long serialVersionUID = -7787761820275071870L;

    public HapcanUnsupportedChannelException(String message) {
        super(message);
    }
}
