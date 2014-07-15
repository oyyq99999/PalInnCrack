package oyyq.utils.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class FileUtil {

    /**
     * 读入文本文件内容
     * 
     * @param filePath
     *            文件路径
     * @return 文件内容
     * @throws IOException
     */
    public static String readFile(String filePath) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            char[] buf = new char[4096];
            int numRead = 0;
            while ((numRead = reader.read(buf)) != -1) {
                sb.append(String.valueOf(buf, 0, numRead));
            }
        }
        return sb.toString();
    }

    /**
     * 复制文件
     * 
     * @param src
     *            源文件
     * @param dst
     *            目标文件
     * @throws IOException
     */
    public static void copyFile(File src, File dst) throws IOException {
        try (FileInputStream fis = new FileInputStream(src);
                FileOutputStream fos = new FileOutputStream(dst);
                FileChannel in = fis.getChannel();
                FileChannel out = fos.getChannel()) {
            in.transferTo(0, in.size(), out);
        }
    }

    /**
     * 复制文件
     * 
     * @param src
     *            源文件路径
     * @param dst
     *            目标文件路径
     * @throws IOException
     */
    public static void copyFile(String src, String dst) throws IOException {
        copyFile(new File(src), new File(dst));
    }

    /**
     * 把文本内容写入到文件
     * 
     * @param filePath
     *            文件路径
     * @param content
     *            文本内容
     * @throws IOException
     * @see {@link #writeToFile(File, String)}
     */
    public static void writeToFile(String filePath, String content) throws IOException {
        writeToFile(new File(filePath), content);
    }

    /**
     * 把文本内容写入到文件
     * 
     * @param file
     *            要写入的文件
     * @param content
     *            文本内容
     * @throws IOException
     * @see {@link #writeToFile(File, String, boolean)}
     */
    public static void writeToFile(File file, String content) throws IOException {
        writeToFile(file, content, false);
    }

    /**
     * 把文本内容写入或追加到文件
     * 
     * @param filePath
     *            文件路径
     * @param content
     *            文本内容
     * @param append
     *            是否追加
     * @throws IOException
     * @see {@link #writeToFile(File, String, boolean)}
     */
    public static void writeToFile(String filePath, String content, boolean append)
            throws IOException {
        writeToFile(new File(filePath), content, append);
    }

    /**
     * 把文件内容写入或追加到文件
     * 
     * @param file
     *            要写入的文件
     * @param content
     *            文本内容
     * @param append
     *            是否追加
     * @throws IOException
     */
    public static void writeToFile(File file, String content, boolean append) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }
        try (OutputStream fos = new FileOutputStream(file, append)) {
            if (content == null) {
                return;
            }
            fos.write(content.getBytes());
            fos.flush();
        }
    }

    /**
     * 递归获取一个路径下的文件名列表
     * 
     * @param path
     *            要获取文件名列表的路径
     * @return 文件名列表
     * @throws IOException
     * @see {@link #getFileList(String, String)}
     */
    public static String[] getFileList(String path) throws IOException {
        return getFileList(path, null);
    }

    /**
     * 递归获取一个路径下的文件名列表
     * 
     * @param path
     *            要获取文件名列表的路径
     * @param suffix
     *            文件后缀限制
     * @return 文件名列表
     * @throws IOException
     */
    public static String[] getFileList(String path, String suffix) throws IOException {
        ArrayList<String> result = new ArrayList<String>();
        getFileList(result, path, suffix);
        String[] resultArray = new String[result.size()];
        result.toArray(resultArray);
        return resultArray;
    }

    private static void getFileList(ArrayList<String> result, String path, String suffix)
            throws IOException {
        if (path == null) {
            return;
        }
        File file = new File(path);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File oneFile : files) {
                    getFileList(result, oneFile.getCanonicalPath(), suffix);
                }
            }
        } else if (file.isFile()) {
            if (suffix == null || file.getName().endsWith(suffix)) {
                result.add(file.getCanonicalPath());
            }
        }
    }

    public static boolean checkExtension(String filename, String extension, boolean ignoreCase) {
        if (extension == null || extension.length() == 0) {
            return true;
        }
        if (filename == null) {
            return false;
        }
        int idx = filename.lastIndexOf(".");
        if (idx < 0) {
            return false;
        }
        String ext = filename.substring(idx + 1);
        return ignoreCase ? extension.equalsIgnoreCase(ext) : extension.equals(ext);
    }

    public static boolean checkExtension(String filename, String extension) {
        return checkExtension(filename, extension, true);
    }

}
