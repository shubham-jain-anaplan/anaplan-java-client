package com.anaplan.client;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.junit.jupiter.api.Test;

public class CryptoUtilTest {

  @Test
  void shouldEncryptValue() throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException {
   // String encryptedValue = CryptoUtil.encrypt("SomePassword".getBytes());
	  byte[] encryptedValue = CryptoUtil.encrypt("SomePassword".getBytes());
    assertThat(encryptedValue, notNullValue());
   // String decryptedValue = CryptoUtil.decrypt(encryptedValue);
    byte[] decryptedValue = CryptoUtil.decrypt(encryptedValue);
    assertThat(decryptedValue, notNullValue());
    assertThat(decryptedValue, equalTo("SomePassword"));
  }

  @Test
  void shouldThrowExceptionForEmptyValue() {
    AnaplanCyptoException thrown = assertThrows(
        AnaplanCyptoException.class,
        () -> CryptoUtil.encrypt("    ".getBytes()),
        "Expected encrypt() to throw exception, but it didn't"
    );
    assertThat(thrown.getMessage(), is("Empty value to encrypt."));
  }

}
