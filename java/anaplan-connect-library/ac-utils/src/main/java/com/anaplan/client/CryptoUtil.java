package com.anaplan.client;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import com.google.crypto.tink.Aead;
import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.JsonKeysetWriter;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AeadKeyTemplates;

/**
 * CryptoUtil class is used for encrypting and decryption
 */
public class CryptoUtil {

	private static final String KEY_FILE = "aes_keyset.json";

	static {
		try {
			AeadConfig.register();
		} catch (GeneralSecurityException e) {
			throw new RuntimeException("Failed to initialize Tink", e);
		}
	}

	public static void generateAndStoreKey() throws GeneralSecurityException, IOException {
		KeysetHandle keysetHandle = KeysetHandle.generateNew(AeadKeyTemplates.AES256_GCM);
		CleartextKeysetHandle.write(keysetHandle, JsonKeysetWriter.withFile(new File(KEY_FILE)));
	}

	private static Aead getAead() throws GeneralSecurityException, IOException {
		KeysetHandle keysetHandle = CleartextKeysetHandle.read(JsonKeysetReader.withFile(new File(KEY_FILE)));
		return keysetHandle.getPrimitive(Aead.class);
	}

	public static byte[] encrypt(byte[] data)
			throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
			InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException {

		try {
			Aead aead = getAead();

			byte[] ciphertext = aead.encrypt(data, null);
			return ciphertext; // Return salt and ciphertext combined
		} catch (GeneralSecurityException | IOException e) {
			throw new RuntimeException("Unable to encrypt value", e);
		}
	}

	public static byte[] decrypt(byte[] encryptedData)
			throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
			InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException {

		try {
			Aead aead = getAead();
			byte[] ciphertext = encryptedData;
			byte[] decrypted = aead.decrypt(ciphertext, null);
			return decrypted;
		} catch (GeneralSecurityException | IOException e) {
			throw new RuntimeException("Unable to decrypt value", e);
		}
	}

}
