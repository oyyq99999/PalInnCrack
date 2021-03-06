package oyyq.palinncrack.algo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import oyyq.utils.cipher.CipherUtils;
import oyyq.utils.io.FileUtil;

public class Checksum {

    /**
     * 被加密的字符串
     */
    private static final byte[] HOLY_SHIT = "Des for PalInn".getBytes();

    /**
     * 计算存档文件的校验和，以此作为加密的key
     * 
     * @param saveFile
     *            存档文件
     * @return 校验和的字符串形式
     */
    private static String calcChecksum(File saveFile) {
        try (FileInputStream fis = new FileInputStream(saveFile)) {
            byte[] buffer = new byte[4096];
            long checksum = 0;
            int readSize = -1;
            while ((readSize = fis.read(buffer)) != -1) {
                for (int i = 0; i < readSize; i++) {
                    checksum += buffer[i] & 0xff; // in case of negatives make trouble
                }
            }
            return checksum + "";
        } catch (IOException e) {
            return null;
        }
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
        try {
            FileUtil.copyFile(oldFilename, newFilename);
        } catch (Exception e) {
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
            System.arraycopy(bytes, 0, key, 0, Math.min(bytes.length, key.length));
            byte[] src = new byte[16];
            System.arraycopy(HOLY_SHIT, 0, src, 0, Math.min(HOLY_SHIT.length, src.length));
            byte[] res = CipherUtils.desEcbNoPaddingEncrypt(src, key);
            String got = "";
            for (int i = 0; i < res.length; i++) {
                got += String.format("%02X", res[i]);
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
