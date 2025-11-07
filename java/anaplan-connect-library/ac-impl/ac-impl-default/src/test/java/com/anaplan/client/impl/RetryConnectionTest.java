package com.anaplan.client.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;

import com.anaplan.client.Constants;
import com.anaplan.client.FeignAuthenticationAPIProvider;
import com.anaplan.client.api.AnaplanAuthenticationAPI;
import com.anaplan.client.auth.BasicAuthenticator;
import com.anaplan.client.auth.Credentials;
import com.anaplan.client.transport.ConnectionProperties;
import feign.Client;
import feign.Request;
import feign.Request.Options;
import java.io.IOException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Created by Spondon Saha User: spondonsaha Date: 2/13/18 Time: 3:24 AM
 */
public class RetryConnectionTest {

  private final String mockAuthServiceUrl = "http://mock-auth.anaplan.com";
  private final String mockUsername = "mockusername";
  private final String mockPassword = "mockpassword";
  @Mock
  private Client mockClient;
  private FeignAuthenticationAPIProvider feignAuthenticationAPIProvider;
  private AnaplanAuthenticationAPI authClient;
  private BasicAuthenticator basicAuthenticator;

  @BeforeEach
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    ConnectionProperties properties = new ConnectionProperties();
    properties.setApiCredentials(new Credentials(mockUsername, mockPassword));
    properties.setAuthServiceUri(new URI(mockAuthServiceUrl));
    properties.setMaxRetryCount(Constants.MIN_RETRY_COUNT);
    properties.setRetryTimeout(Constants.MIN_RETRY_TIMEOUT_SECS);
    properties.setHttpTimeout(Constants.MIN_HTTP_CONNECTION_TIMEOUT_SECS);
    Supplier<Client> clientSupplier = () -> mockClient;
    feignAuthenticationAPIProvider = new FeignAuthenticationAPIProvider(properties, clientSupplier,"","");
    authClient = feignAuthenticationAPIProvider.getAuthClient();
    basicAuthenticator = new BasicAuthenticator(properties, authClient);
  }

  public void testRetry(String message) throws IOException {
    try {
      basicAuthenticator.authToken();
    } catch (Exception e) {
      assertEquals(message, e.getCause().getMessage());
    } finally {
      Mockito.verify(mockClient, Mockito.times(4))
          .execute(Mockito.any(Request.class), Mockito.any(Options.class));
    }
  }

  @Test
  public void testGetAuthTokenRetryAndFail() throws IOException {
    doThrow(new UnknownHostException())
        .when(mockClient).execute(Mockito.any(Request.class), Mockito.any(Options.class));
    testRetry("null executing POST http://mock-auth.anaplan.com/token/authenticate");
  }
}
