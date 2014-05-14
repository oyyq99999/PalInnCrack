package algo.checksum;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.security.spec.KeySpec;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class Checksum {

    /**
     * 被加密的字符串
     */
    private static final String HOLY_SHIT = "Des for PalInn";

    /**
     * 计算存档文件的校验和，以此作为加密的key
     * 
     * @param saveFile
     *            存档文件
     * @return 校验和的字符串形式
     */
    private static String calcChecksum(File saveFile) {
        try (FileInputStream fis = new FileInputStream(saveFile)) {
            long length = saveFile.length();
            long checksum = 0;
            while (length-- > 0) {
                int one = fis.read();
                checksum += one;
            }
            return checksum + "";
        } catch (IOException e) {
            return null;
        }
    }

    private static byte[] encrypt(byte[] src) throws Exception {
        byte[] data = new byte[16];
        KeySpec ks = new DESKeySpec(src);
        SecretKeyFactory kf = SecretKeyFactory.getInstance("DES");
        SecretKey key = kf.generateSecret(ks);
        Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        for (int i = 0; i < HOLY_SHIT.length(); i++) {
            data[i] = (byte) HOLY_SHIT.charAt(i);
        }
        byte[] res = cipher.doFinal(data);
        return res;
    }

    private static void save(String got, File crcFile) throws Exception {
        if (crcFile.exists()) {
            backup(crcFile);
        }
        FileOutputStream fos = new FileOutputStream(crcFile);
        byte[] bytes = new byte[got.length() + 2];
        bytes[0] = (byte) got.length();
        for (int i = 0; i < got.length(); i++) {
            bytes[i + 2] = (byte) got.charAt(i);
        }
        fos.write(bytes);
        fos.close();
    }

    private static void backup(File crcFile) {
        String oldFilename = crcFile.getAbsolutePath();
        String newFilename = oldFilename
                + (new SimpleDateFormat("'.'yyyyMMddHHmm").format(new Date()));
        Runtime rt = Runtime.getRuntime();
        try {
            rt.exec(String.format("cmd /c copy %s %s", oldFilename, newFilename));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            showHelp(System.out);
            return;
        }
        String saveFile = args[0];
        String crcFile = null;
        if (args.length > 1) {
            crcFile = args[1];
        } else {
            crcFile = saveFile.replaceFirst("\\.[^.]+$", ".crc");
            if (crcFile.equals(saveFile)) {
                throw new IllegalArgumentException(
                        "Auto generated crc file name is the same as the save file!");
            }
        }
        encrypt(saveFile, crcFile);
    }

    private static void showHelp(PrintStream out) {
        out.println("Usage: " + Checksum.class.getName() + " savefile" + " [crcFile]");
    }

    public static int encrypt(String saveFile, String crcFile) {
        return encrypt(new File(saveFile), new File(crcFile));
    }

    public static int encrypt(File saveFile, File crcFile) {
        try {
            String sum = calcChecksum(saveFile);
            byte[] bytes = sum.getBytes();
            byte[] key = new byte[8];
            for (int i = 0; i < bytes.length && i < 8; i++) {
                key[i] = bytes[i];
            }
            byte[] res = encrypt(key);
            String got = "";
            for (int i = 0; i < res.length; i++) {
                got += String.format("%02X", res[i] & 0xFF);
            }
            save(got, crcFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

}
