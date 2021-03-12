package com.weapon.zhao.plugin.file2ide.dialog;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.UI;
import com.weapon.zhao.plugin.file2ide.util.UrlHttpUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author WeaponZhao
 * @since 2021/3/9 10:19
 */
public class DownloadDialog extends DialogWrapper {
    private static final Logger LOG = Logger.getInstance(DownloadDialog.class);

    private final Project currentProject;
    private JPanel gridLayout;
    private JBTextField url;
    private JPanel urlComment;
    private JTextField folder;
    private TextFieldWithBrowseButton browseButton;

    public DownloadDialog(Project project) {
        super(project);
        this.currentProject = project;
        String name = project.getName();
        this.init();
        this.setTitle("Download Code File To " + name);
        this.setOKButtonText("Download");
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return this.gridLayout;
    }

    @Override
    protected void doOKAction() {
        String urlPath = this.url.getText();
        String folderPath = this.folder.getText();
        String[] split = urlPath.split("/");
        String target = folderPath.concat(folderPath.endsWith(File.separator)? "" : File.separator)
            .concat(split[split.length - 1]);
        UrlHttpUtil.downloadRes(urlPath, target);
        if (target.toLowerCase().endsWith(".zip")) {
            unzipFile(target, folderPath);
        }
        super.doOKAction();
    }

    static void unzipFile(String target, String folderPath) {
        //获取当前压缩文件
        File srcFile = new File(target);
        // 判断源文件是否存在
        if (srcFile.exists()) {
            try (ZipFile zipFile = new ZipFile(srcFile)) {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if (entry.isDirectory()) {
                        String dirPath = folderPath.concat(File.separator).concat(entry.getName());
                        File file = new File(dirPath);
                        if (!file.exists()) file.mkdirs();
                    } else {
                        // 如果类型是文件，先创建一个文件，然后用IO流复制内容
                        File targetFile = new File(folderPath.concat(File.separator).concat(entry.getName()));
                        // 保证文件的父文件夹存在
                        File parentFile = targetFile.getParentFile();
                        if (!parentFile.exists()) parentFile.mkdirs();
                        // 如果文件存在则先删除后新建
                        if (targetFile.exists()) targetFile.delete();
                        targetFile.createNewFile();
                        // 将压缩包中文件内容写入到目标文件中
                        copyFileFromZip(zipFile, entry, targetFile);
                    }
                }
            } catch (IOException e) {
                LOG.warn(e.getMessage(), e);
            }
        } else {
            LOG.warn("源文件不存在: ".concat(target));
        }
    }

    static void copyFileFromZip(ZipFile zipFile, ZipEntry entry, File targetFile) {
        try (InputStream is = zipFile.getInputStream(entry);
             FileOutputStream fos = new FileOutputStream(targetFile)) {
            int len;
            byte[] buf = new byte[1024];
            while ((len = is.read(buf)) != -1) {
                fos.write(buf, 0, len);
            }
        } catch (IOException e) {
            LOG.warn(e.getMessage(), e);
        }
    }

    private void createUIComponents() {
        // URL Input
        this.url = new JBTextField();
        this.urlComment = UI.PanelFactory.panel(this.url).withComment("Support http/https/ftp protocol").createPanel();
        // Folder Select
        String basePath = this.currentProject.getBasePath();
        this.folder = new JTextField(basePath);
        FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(false, true, false, false, false, false);
        fileChooserDescriptor.setForcedToUseIdeaFileChooser(true);
        if (basePath != null) {
            VirtualFile virtualFile = VfsUtil.findFileByIoFile(new File(basePath), true);
            if (virtualFile != null) fileChooserDescriptor.setRoots(virtualFile);
        }
        this.browseButton = new TextFieldWithBrowseButton(this.folder);
        this.browseButton.addBrowseFolderListener(null, null, this.currentProject, fileChooserDescriptor, TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT);
    }
}
