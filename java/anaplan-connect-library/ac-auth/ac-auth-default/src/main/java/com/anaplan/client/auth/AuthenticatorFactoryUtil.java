package com.anaplan.client.auth;

import com.anaplan.client.api.AnaplanAuthenticationAPI;
import com.anaplan.client.transport.ConnectionProperties;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by Spondon Saha User: spondonsaha Date: 12/12/17 Time: 1:17 AM
 */
public class AuthenticatorFactoryUtil {

  private AuthenticatorFactoryUtil() {}

  public static Authenticator getAuthenticator(ConnectionProperties properties,
      AnaplanAuthenticationAPI authClient)
      throws UnknownAuthenticationException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
    switch (properties.getApiCredentials().getScheme()) {
      case BASIC:
      case NTLM:
        return new BasicAuthenticator(properties, authClient);
      case CA_CERTIFICATE:
        return new CertificateAuthenticator(properties, authClient);
      case DEVICE:
        return new DeviceAuthenticator(properties, authClient);
      default:
        throw new UnknownAuthenticationException(
            "Unknown authentication scheme: " + properties.getApiCredentials().getScheme());
    }
  }

}
