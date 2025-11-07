package com.anaplan.client;

import com.anaplan.client.auth.Authenticator;
import com.anaplan.client.auth.AuthenticatorFactoryUtil;
import com.anaplan.client.auth.DeviceAuthenticator;
import com.anaplan.client.auth.UnknownAuthenticationException;
import com.anaplan.client.transport.ConnectionProperties;
import com.anaplan.client.transport.client.OkHttpFeignClientProvider;
import feign.Client;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.function.Supplier;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class DefaultServiceProvider {

  private DefaultServiceProvider(){}

  public static Service getService(ConnectionProperties properties, String clientKey, String clientValue)
      throws UnknownAuthenticationException, NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException {

    OkHttpFeignClientProvider okHttpClientProvider = new OkHttpFeignClientProvider();
    Supplier<Client> clientSupplier = () -> okHttpClientProvider.createFeignClient(properties);

    FeignAuthenticationAPIProvider authApiProvider = new FeignAuthenticationAPIProvider(properties,
        clientSupplier, clientKey, clientValue);
    Authenticator authenticator = AuthenticatorFactoryUtil
        .getAuthenticator(properties, authApiProvider.getAuthClient());

    AnaplanApiProviderImpl apiProvider = new AnaplanApiProviderImpl(properties, clientSupplier, authenticator, clientKey, clientValue);

    return new Service(properties, authenticator, apiProvider);
  }

  /**
   * Get the authenticator based on properties
   * @param properties the connection properties
   * @return {@link DeviceAuthenticator}
   */
  public static DeviceAuthenticator getDeviceAuthenticator(ConnectionProperties properties, String clientKey, String clientValue)
      throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
    OkHttpFeignClientProvider okHttpClientProvider = new OkHttpFeignClientProvider();
    Supplier<Client> clientSupplier = () -> okHttpClientProvider.createFeignClient(properties);

    FeignAuthenticationAPIProvider authApiProvider = new FeignAuthenticationAPIProvider(properties,
        clientSupplier, clientKey, clientValue);
    return new DeviceAuthenticator(properties, authApiProvider.getAuthClient());
  }
}
