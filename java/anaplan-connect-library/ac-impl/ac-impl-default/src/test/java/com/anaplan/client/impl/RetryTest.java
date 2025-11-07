package com.anaplan.client.impl;


import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

import com.anaplan.client.AnaplanApiProviderImpl;
import com.anaplan.client.Constants;
import com.anaplan.client.DefaultServiceProvider;
import com.anaplan.client.FeignAuthenticationAPIProvider;
import com.anaplan.client.Service;
import com.anaplan.client.api.AnaplanAuthenticationAPI;
import com.anaplan.client.auth.BasicAuthenticator;
import com.anaplan.client.auth.Credentials;
import com.anaplan.client.dto.TokenInfo;
import com.anaplan.client.dto.responses.AuthenticationResp;
import com.anaplan.client.dto.responses.ChunksResponse;
import com.anaplan.client.transport.ConnectionProperties;
import feign.Client;
import feign.Request;
import feign.Request.HttpMethod;
import feign.Request.Options;
import feign.Response;
import feign.Response.Builder;
import java.io.IOException;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Created by Spondon Saha User: spondonsaha Date: 2/13/18 Time: 3:24 AM
 */
public class RetryTest {

  private final String mockAuthServiceUrl = "http://mock-auth.anaplan.com";
  private final String mockUsername = "someusername";
  private final String mockPassword = "asdasdasdsa";

  @Mock
  private Client mockClient;

  @Mock
  Client clientSupplierApi;


  private FeignAuthenticationAPIProvider feignAuthenticationAPIProvider;
  private AnaplanAuthenticationAPI authClient;

  @Mock
  private BasicAuthenticator basicAuthenticator;

  Service service;

  @BeforeEach
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    ConnectionProperties properties = new ConnectionProperties();
    properties.setApiCredentials(new Credentials(mockUsername, mockPassword));
    properties.setAuthServiceUri(new URI(mockAuthServiceUrl));
    properties.setMaxRetryCount(9);
    properties.setRetryTimeout(Constants.MIN_RETRY_TIMEOUT_SECS);
    properties.setHttpTimeout(Constants.MIN_HTTP_CONNECTION_TIMEOUT_SECS);
    Supplier<Client> clientSupplier = () -> mockClient;
    feignAuthenticationAPIProvider = new FeignAuthenticationAPIProvider(properties, clientSupplier, "", "");

    when(basicAuthenticator.authenticate()).thenReturn("bla".getBytes());

    Supplier<Client> clientApi = () -> clientSupplierApi;

    AnaplanApiProviderImpl apiProvider = new AnaplanApiProviderImpl(properties, clientApi, basicAuthenticator, "", "");

    service = DefaultServiceProvider.getService(properties, "INFORMATICA", "VERSION");
    service.setAuthProvider(basicAuthenticator);
    service.setApiProvider(apiProvider);

  }

  @Test
  public void testGetRetry() throws IOException {
    AuthenticationResp re = new AuthenticationResp();
    TokenInfo tokenInfo = new TokenInfo();
    tokenInfo.setTokenId("id");
    tokenInfo.setTokenValue("value");
    re.setItem(tokenInfo);

    Builder builder = Response.builder();

    builder.status(200);
    builder.body("",Charset.defaultCharset());
    builder.headers(new HashMap<>());
    builder.request(Request.create(HttpMethod.GET,"get", new HashMap<>(),"ggg".getBytes(),Charset.defaultCharset()));

    when(clientSupplierApi.execute(Mockito.any(Request.class), Mockito.any(Options.class))).thenThrow(new UnknownHostException())
        .thenThrow(new UnknownHostException()).thenReturn(builder.build());

    ChunksResponse res= service.getApiProvider().get().getChunks("","","");
    assertNull(res);
  }
}



