/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.oauth2.internal.tokenmanager;

import static java.lang.String.format;

import org.mule.extension.oauth2.internal.authorizationcode.state.ConfigOAuthContext;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.context.MuleContextAware;
import org.mule.runtime.core.api.registry.RegistrationException;
import org.mule.runtime.core.api.store.ListableObjectStore;
import org.mule.runtime.core.util.store.MuleObjectStoreManager;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.dsl.xml.XmlHints;
import org.mule.runtime.extension.api.annotation.param.ConfigName;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.oauth.api.state.ResourceOwnerOAuthContext;
import org.mule.runtime.oauth.api.state.DefaultResourceOwnerOAuthContext;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Token manager stores all the OAuth State (access token, refresh token).
 *
 * It can be referenced to access the state inside a flow for custom processing of oauth dance content.
 */
@Alias("token-manager-config")
@XmlHints(allowTopLevelDefinition = true)
public class TokenManagerConfig implements Initialisable, MuleContextAware {

  public static AtomicInteger defaultTokenManagerConfigIndex = new AtomicInteger(0);

  /**
   * Identifier for the token manager configuration.
   */
  // TODO MULE-11424 Move to OAuthExtension
  @ConfigName
  private String name;

  /**
   * References an object store to use for storing oauth context data
   */
  // TODO MULE-11424 Move to OAuthExtension
  @Parameter
  @Optional
  private ListableObjectStore<DefaultResourceOwnerOAuthContext> objectStore;

  private MuleContext muleContext;

  private ConfigOAuthContext configOAuthContext;

  private boolean initialised;

  public ListableObjectStore<DefaultResourceOwnerOAuthContext> getObjectStore() {
    return objectStore;
  }

  public void setObjectStore(ListableObjectStore<DefaultResourceOwnerOAuthContext> objectStore) {
    this.objectStore = objectStore;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public synchronized void initialise() throws InitialisationException {
    if (initialised) {
      return;
    }
    if (objectStore == null) {
      objectStore =
          (ListableObjectStore<DefaultResourceOwnerOAuthContext>) ((MuleObjectStoreManager) muleContext.getObjectStoreManager())
              .getUserObjectStore("token-manager-store-" + this.name, true);
    }
    configOAuthContext = new ConfigOAuthContext(muleContext.getLockFactory(), objectStore, name);
    initialised = true;
  }

  public static TokenManagerConfig createDefault(final MuleContext context) throws InitialisationException {
    final TokenManagerConfig tokenManagerConfig = new TokenManagerConfig();
    final String tokenManagerConfigName = "default-token-manager-config-" + defaultTokenManagerConfigIndex.getAndIncrement();
    tokenManagerConfig.setName(tokenManagerConfigName);
    try {
      context.getRegistry().registerObject(tokenManagerConfigName, tokenManagerConfig);
    } catch (RegistrationException e) {
      throw new InitialisationException(e, tokenManagerConfig);
    }
    return tokenManagerConfig;
  }

  public ConfigOAuthContext getConfigOAuthContext() {
    return configOAuthContext;
  }

  /**
   * Provides support for the oauthContext MEL function for this configuration
   *
   * @param params function parameters without the config name parameter
   * @return the result of the function call
   */
  public ResourceOwnerOAuthContext processOauthContextFunctionACall(Object... params) {
    if (params.length > 1) {
      throw new IllegalArgumentException(format("oauthContext for config type %s does not accepts more than two arguments",
                                                "authorization-code"));
    }
    String resourceOwnerId = ResourceOwnerOAuthContext.DEFAULT_RESOURCE_OWNER_ID;
    if (params.length == 1) {
      resourceOwnerId = (String) params[0];
    }
    return getConfigOAuthContext().getContextForResourceOwner(resourceOwnerId);
  }

  @Override
  public void setMuleContext(MuleContext muleContext) {
    this.muleContext = muleContext;
  }

}
