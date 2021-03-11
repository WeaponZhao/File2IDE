package com.weapon.zhao.plugin.file2ide.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.weapon.zhao.plugin.file2ide.dialog.DownloadDialog;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author WeaponZhao
 * @since 2021/3/8 16:56
 */
public class MainMenuAction extends AnAction {
    private static final Logger LOG = Logger.getInstance(MainMenuAction.class);

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(project != null && !project.isDefault() && StringUtils.isNotBlank(project.getBasePath()));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        String place = e.getPlace();
        LOG.info("File2IDE:" + place);
        Project project = e.getProject();
        if (project != null) {
            // open dialog
            DownloadDialog dialog = new DownloadDialog(project);
            dialog.pack();
            dialog.showAndGet();
        }
    }

    @Override
    public boolean isDumbAware() {
        return false;
    }
}
