package com.github.loren.restful.tools.service;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public interface ToolWindowService {

    /**
     * getInstance
     *
     * @param project project
     * @return obj
     */
    static ToolWindowService getInstance(@NotNull Project project) {
        return project.getService(ToolWindowService.class);
    }

    /**
     * get view content
     *
     * @return ContentView
     */
    JComponent getContent();

    /**
     * init window
     *
     * @param toolWindow toolWindow
     */
    default void init(@NotNull ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(getContent(), "", false);

        toolWindow.getContentManager().addContent(content);
    }
}
