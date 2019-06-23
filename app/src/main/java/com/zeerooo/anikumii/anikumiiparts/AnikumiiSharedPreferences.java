package com.zeerooo.anikumii.anikumiiparts;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.security.auth.x500.X500Principal;

import static android.content.Context.MODE_PRIVATE;

/*
 * Thanks!! https://gist.github.com/StelianMorariu/0b5c0f854b827c4ec491
 */
public class AnikumiiSharedPreferences implements SharedPreferences {

    private final Context mContext;
    private final SharedPreferences sharedPreferences;

    public AnikumiiSharedPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences("ZeeRooo@Anikumii!!", MODE_PRIVATE);
        mContext = context;
    }

    public final void encrypt(String key, String value) {
        edit().putString(key, encryptString(value)).apply();
    }

    public final String decrypt(String key, String defValue) {
        return decryptString(getString(key, defValue), defValue);
    }

    private String encryptString(String toEncrypt) {
        try {
            final KeyStore.PrivateKeyEntry privateKeyEntry = getPrivateKey();
            if (privateKeyEntry != null) {
                // Encrypt the text
                final Cipher input = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                input.init(Cipher.ENCRYPT_MODE, privateKeyEntry.getCertificate().getPublicKey());

                final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                final CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, input);
                cipherOutputStream.write(toEncrypt.getBytes("UTF-8"));
                outputStream.close();
                cipherOutputStream.close();

                return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
            }
        } catch (IllegalStateException illegal) {
            Toast.makeText(mContext, "Ha ocurrido un error al encriptar sus datos. Se recomienda reiniciar el dispositivo.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String decryptString(String encrypted, String defValue) {
        if (encrypted != null && !encrypted.equals(defValue)) {
            try {
                final KeyStore.PrivateKeyEntry privateKeyEntry = getPrivateKey();
                if (privateKeyEntry != null) {
                    final Cipher output = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                    output.init(Cipher.DECRYPT_MODE, privateKeyEntry.getPrivateKey());

                    final CipherInputStream cipherInputStream = new CipherInputStream(new ByteArrayInputStream(Base64.decode(encrypted, Base64.DEFAULT)), output);
                    final ArrayList<Byte> values = new ArrayList<>();
                    int nextByte;
                    while ((nextByte = cipherInputStream.read()) != -1) {
                        values.add((byte) nextByte);
                    }

                    cipherInputStream.close();

                    final byte[] bytes = new byte[values.size()];
                    for (int i = 0; i < bytes.length; i++) {
                        bytes[i] = values.get(i);
                    }

                    return new String(bytes, 0, bytes.length, "UTF-8");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return defValue;
    }

    private KeyStore.PrivateKeyEntry getPrivateKey() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableEntryException, NoSuchProviderException, InvalidAlgorithmParameterException {
        KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
        ks.load(null);

        KeyStore.Entry entry;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            if (ks.getCertificate("ZeeRooo@Anikumii!!") == null)
                createKeys();

            entry = ks.getEntry("ZeeRooo@Anikumii!!", null);
        } else {
            PrivateKey privateKey = (PrivateKey) ks.getKey("ZeeRooo@Anikumii!!", null);

            if (privateKey == null) {
                createKeys();
                privateKey = (PrivateKey) ks.getKey("ZeeRooo@Anikumii!!", null);
            }
            Certificate certificate = ks.getCertificate("ZeeRooo@Anikumii!!");
            entry = new KeyStore.PrivateKeyEntry(privateKey, new Certificate[]{certificate});
        }

        return (KeyStore.PrivateKeyEntry) entry;
    }

    /**
     * Creates a public and private key and stores it using the Android Key Store, so that only
     * this application will be able to access the keys.
     */
    private void createKeys() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        final Calendar start = new GregorianCalendar(), end = new GregorianCalendar();
        end.add(Calendar.YEAR, 25);
        final KeyPairGenerator kpGenerator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
        final AlgorithmParameterSpec spec;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            spec = new KeyPairGeneratorSpec.Builder(mContext)
                    .setAlias("ZeeRooo@Anikumii!!")
                    .setSubject(new X500Principal("CN=ZeeRooo@Anikumii!!"))
                    .setSerialNumber(BigInteger.ONE)
                    .setStartDate(start.getTime())
                    .setEndDate(end.getTime())
                    .build();
        } else {
            spec = new KeyGenParameterSpec.Builder("ZeeRooo@Anikumii!!", KeyProperties.PURPOSE_DECRYPT | KeyProperties.PURPOSE_ENCRYPT)
                    .setCertificateSubject(new X500Principal("CN=ZeeRooo@Anikumii!!"))
                    .setDigests(KeyProperties.DIGEST_SHA256)
                    .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                    .setCertificateSerialNumber(BigInteger.ONE)
                    .setCertificateNotBefore(start.getTime())
                    .setCertificateNotAfter(end.getTime())
                    .setKeySize(2048)
                    .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
                    .build();
        }

        kpGenerator.initialize(spec);
        kpGenerator.generateKeyPair();
    }

    @Override
    public Map<String, ?> getAll() {
        return sharedPreferences.getAll();
    }

    @Nullable
    @Override
    public String getString(String key, @Nullable String defValue) {
        return sharedPreferences.getString(key, defValue);
    }

    @Nullable
    @Override
    public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
        return sharedPreferences.getStringSet(key, defValues);
    }

    @Override
    public int getInt(String key, int defValue) {
        return sharedPreferences.getInt(key, defValue);
    }

    @Override
    public long getLong(String key, long defValue) {
        return sharedPreferences.getLong(key, defValue);
    }

    @Override
    public float getFloat(String key, float defValue) {
        return sharedPreferences.getFloat(key, defValue);
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        return sharedPreferences.getBoolean(key, defValue);
    }

    @Override
    public boolean contains(String key) {
        return sharedPreferences.contains(key);
    }

    @Override
    public Editor edit() {
        return sharedPreferences.edit();
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }
}
