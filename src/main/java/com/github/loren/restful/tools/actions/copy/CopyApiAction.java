package com.github.loren.restful.tools.actions.copy;

import com.github.loren.restful.tools.utils.Bundle;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class CopyApiAction extends AnAction implements CopyOption {

    public CopyApiAction() {
        getTemplatePresentation().setText(Bundle.message("action.CopyApi.text"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        copyPath(e, false);
    }
}
