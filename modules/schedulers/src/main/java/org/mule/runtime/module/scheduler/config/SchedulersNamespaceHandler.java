/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.scheduler.config;

import org.mule.runtime.config.spring.handlers.AbstractMuleNamespaceHandler;
import org.mule.runtime.config.spring.parsers.generic.ChildDefinitionParser;
import org.mule.runtime.module.scheduler.cron.CronScheduler;


public class SchedulersNamespaceHandler extends AbstractMuleNamespaceHandler {

  @Override
  public void init() {
    registerBeanDefinitionParser("cron-scheduler", new ChildDefinitionParser("scheduler", CronScheduler.class));
  }
}
