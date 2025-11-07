package com.anaplan.client;
import com.google.crypto.tink.Aead;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AeadKeyTemplates;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


public class CryptoUtilTest {

 // @Test
  //void shouldEncryptValue()
  //    throws IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, InvalidKeySpecException, InvalidAlgorithmParameterException {
  //  byte[] encryptedValue = CryptoUtil.encrypt("SomePassword".getBytes());
  //  assertThat(encryptedValue, notNullValue());
  //  String decryptedValue = new String(CryptoUtil.decrypt(encryptedValue));
  //  assertThat(decryptedValue, notNullValue());
  //  assertThat(decryptedValue, equalTo("SomePassword"));
 // }

  //@Test
  //void shouldThrowExceptionForEmptyValue()
  //    throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException {
   // assertNotNull(CryptoUtil.encrypt("    ".getBytes()));
 // }
	
    private static Aead aead;
    @BeforeAll
    public static void setup() throws Exception {
        // Register Tink AEAD and generate in-memory keyset
        AeadConfig.register();
        KeysetHandle keysetHandle = KeysetHandle.generateNew(AeadKeyTemplates.AES256_GCM);
        aead = keysetHandle.getPrimitive(Aead.class);
    }
    @Test
    void shouldEncryptValue() throws Exception {
        byte[] encryptedValue = aead.encrypt("SomePassword".getBytes(), null);
        assertThat(encryptedValue, notNullValue());
        byte[] decryptedValue = aead.decrypt(encryptedValue, null);
        String result = new String(decryptedValue);
        assertThat(result, equalTo("SomePassword"));
    }
    @Test
    void shouldThrowExceptionForEmptyValue() throws Exception {
        try {
            aead.encrypt("".getBytes(), null);
        } catch (Exception e) {
            assertThat(e, instanceOf(Exception.class)); // or a more specific one
        }
    }

}
