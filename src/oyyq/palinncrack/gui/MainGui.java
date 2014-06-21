package oyyq.palinncrack.gui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import oyyq.palinncrack.algo.Checksum;

import com.github.sarxos.winreg.HKey;
import com.github.sarxos.winreg.RegistryException;
import com.github.sarxos.winreg.WindowsRegistry;

public class MainGui extends JFrame implements ActionListener {

    private static final long serialVersionUID = 5517819703253784310L;
    private static final int  TEXTFIELD_WIDTH  = 280;

    public static void main(String[] args) throws Exception {
        new MainGui();
    }

    private JButton     action;
    private JPanel      crcFile;
    private JButton     crcFileButton;

    private JLabel      crcFileLabel;
    private JTextField  crcFilePath;
    private JPanel      saveFile;
    private JButton     saveFileButton;

    private JLabel      saveFileLabel;

    private JTextField  saveFilePath;

    private String      systemCharsetName = System.getProperty("sun.jnu.encoding");
    private String      defaultPath       = "";


    private MainGui() throws Exception {
        initData();
        initSelf();
        initComponents();
        addActions();
        showSelf();
    }

    private void initData() {
        String path;
        Preferences prefs = Preferences.userRoot();
        path = prefs.node("/" + getClass().getPackage().getName()).get("default.path", null);
        if (path != null && new File(path).isDirectory()) {
            defaultPath = path;
            return;
        }
        String system = System.getProperty("os.name", "unknown");
        if (system == null || !system.toLowerCase().contains("windows")) {
            return;
        }
        WindowsRegistry registry = WindowsRegistry.getInstance();
        try {
            path = registry.readString(HKey.HKCU, "Software\\SOFTSTAR\\PalInn", "DIRECTORY");
            if (path != null && new File(path).isDirectory()) {
                defaultPath = path;
                return;
            }
        } catch (RegistryException e1) {}
        try {
            path = registry.readString(HKey.HKLM, "Software\\大宇资讯集团软星科技(北京)有限公司\\仙剑客栈\\1.00.000",
                    "TargetPath");
            if (path != null && new File(path).isDirectory()) {
                defaultPath = path;
                return;
            }
        } catch (RegistryException e1) {}
    }

    private void addActions() {
        saveFilePath.addActionListener(this);
        saveFileButton.addActionListener(this);
        crcFileButton.addActionListener(this);
        action.addActionListener(this);
    }

    private void chooseCrcFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("选择加密文件");
        chooser.setCurrentDirectory(new File(defaultPath));
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter("仙剑存档加密文件(*.crc)", "crc"));
        int result = chooser.showSaveDialog(this);
        try {
            if (result == JFileChooser.APPROVE_OPTION) {
                String filename = chooser.getSelectedFile().getCanonicalPath();
                crcFilePath.setText(filename);
                defaultPath = chooser.getCurrentDirectory().getCanonicalPath();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void chooseSaveFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("打开存档文件");
        chooser.setCurrentDirectory(new File(defaultPath));
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter("仙剑存档文件(*.sav)", "sav"));
        int result = chooser.showOpenDialog(this);
        try {
            if (result == JFileChooser.APPROVE_OPTION) {
                String filename = chooser.getSelectedFile().getCanonicalPath();
                saveFilePath.setText(filename);
                saveFilePath.postActionEvent();
                defaultPath = chooser.getCurrentDirectory().getCanonicalPath();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doEncrypt() {
        File saveFile = new File(saveFilePath.getText());
        if (!saveFile.exists()) {
            JOptionPane.showMessageDialog(this, "存档文件不存在！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        File crcFile = new File(crcFilePath.getText());
        if (crcFile.exists()) {
            if (crcFile.getAbsolutePath().equals(saveFile.getAbsolutePath())) {
                JOptionPane
                        .showMessageDialog(this, "存档文件和加密文件相同！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int result = JOptionPane.showConfirmDialog(this, "加密文件已存在，是否覆盖？", "文件已存在",
                    JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }
        int result = Checksum.encrypt(saveFile, crcFile);
        if (result == 0) {
            JOptionPane.showMessageDialog(this, "加密成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "加密失败...", "失败", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generateDefaultCrcFile() {
        String filename = saveFilePath.getText();
        int suffixPos = filename.toLowerCase().lastIndexOf(".sav");
        if (suffixPos + 4 == filename.length()) {
            filename = filename.substring(0, suffixPos) + ".crc";
        } else {
            filename = "";
        }
        crcFilePath.setText(filename);
    }

    private void initComponents() throws Exception {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception e1) {
                throw e1;
            }
        }
        saveFile = new JPanel(new FlowLayout());
        saveFileLabel = new JLabel("存档文件：");
        saveFilePath = new JTextField();

        // 计算应当多少列才能把宽度保持在TEXTFIELD_WIDTH左右
        // width1 + (width2 - width1) * (x - 1) = TEXTFIELD_WIDTH
        // x = (TEXTFIELD_WIDTH - width1) / (width2 - width1) + 1
        saveFilePath.setColumns(1);
        final int width1 = saveFilePath.getPreferredSize().width;
        saveFilePath.setColumns(2);
        final int width2 = saveFilePath.getPreferredSize().width;
        final int columns = (int) ((TEXTFIELD_WIDTH - width1) * 1.0 / (width2 - width1) + 1.5);
        saveFilePath.setColumns(columns);
        saveFilePath.setEditable(false);
        saveFileButton = new JButton("浏览");
        saveFile.add(saveFileLabel);
        saveFile.add(saveFilePath);
        saveFile.add(saveFileButton);

        crcFile = new JPanel(new FlowLayout());
        crcFileLabel = new JLabel("CRC文件：");
        crcFilePath = new JTextField();
        crcFilePath.setColumns(columns);
        crcFilePath.setEditable(false);
        crcFileButton = new JButton("浏览");
        crcFile.add(crcFileLabel);
        crcFile.add(crcFilePath);
        crcFile.add(crcFileButton);

        JPanel actionPanel = new JPanel(new FlowLayout());
        action = new JButton("加密");
        actionPanel.add(action);

        Box box = Box.createVerticalBox();
        box.add(saveFile);
        box.add(crcFile);
        box.add(actionPanel);

        this.add(box);
    }

    private void initSelf() {
        setBounds(300, 300, 480, 300);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("仙剑客栈存档加密工具");
        setResizable(false);
    }

    private void showSelf() {
        this.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == saveFileButton) {
            chooseSaveFile();
            return;
        }
        if (e.getSource() == saveFilePath) {
            generateDefaultCrcFile();
            return;
        }
        if (e.getSource() == crcFileButton) {
            chooseCrcFile();
            return;
        }
        if (e.getSource() == action) {
            doEncrypt();
        }
    }

}
