/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.email.api.exception;

import org.mule.runtime.extension.api.exception.ModuleException;

public class EmailInvalidCredentialsException extends ModuleException {

  public <T extends Enum<T>> EmailInvalidCredentialsException(Exception e) {
    super(e, EmailError.INVALID_CREDENTIALS);
  }
}
