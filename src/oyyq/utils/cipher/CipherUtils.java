package oyyq.utils.cipher;

import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CipherUtils {

    public static byte[] aesCbcPkcs5Encrypt(String src, String key, String iv) {
        return aesCbcPkcs5Encrypt(src.getBytes(), key.getBytes(), iv.getBytes());
    }

    public static byte[] aesCbcPkcs5Encrypt(byte[] src, byte[] key, byte[] iv) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivParam = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParam);
            return cipher.doFinal(src);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static byte[] desEcbNoPaddingEncrypt(byte[] src, byte[] key) {
        try {
            KeySpec ks = new DESKeySpec(key);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
            SecretKey sk = skf.generateSecret(ks);
            Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, sk);
            return cipher.doFinal(src);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
