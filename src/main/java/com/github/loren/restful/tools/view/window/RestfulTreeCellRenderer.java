/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: RestfulTreeCellRenderer
  Author:   ZhangYuanSheng
  Date:     2020/5/6 15:41
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.loren.restful.tools.view.window;

import com.github.loren.restful.tools.beans.ClassTree;
import com.github.loren.restful.tools.beans.ModuleTree;
import com.github.loren.restful.tools.beans.Request;
import com.github.loren.restful.tools.view.window.frame.ServiceTree;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import javax.swing.JTree;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class RestfulTreeCellRenderer extends ColoredTreeCellRenderer {

    @Override
    public void customizeCellRenderer(
            @NotNull JTree tree, Object value,
            boolean selected,
            boolean expanded,
            boolean leaf,
            int row, boolean hasFocus) {
        if (value instanceof ClassTree classTree) {
            // 在读操作中获取类的限定名
            String qualifiedName = classTree.getQualifiedName();
            append(qualifiedName != null ? qualifiedName : "Unknown",
                    SimpleTextAttributes.REGULAR_ATTRIBUTES);
        } else if (value instanceof ServiceTree.ModuleNode node) {
            ModuleTree data = node.getData();
            setIcon(data.getIcon());
            append(data.toString());
        } else if (value instanceof ServiceTree.RequestNode node) {
            Request data = node.getData();
            setMethodTypeAndPath(data, selected);
        } else if (value instanceof ServiceTree.ControllerNode node) {
            ClassTree data = node.getData();
            setIcon(data.getIcon());
            append(data.getName());
            append(" - " + data.getQualifiedName(), SimpleTextAttributes.GRAYED_ATTRIBUTES);
        } else if (value instanceof ServiceTree.TreeNode<?> node) {
            append(node.toString());
        }
    }

    private void setMethodTypeAndPath(@Nullable Request node, boolean selected) {
        if (node == null) {
            return;
        }
        if (selected) {
            setIcon(node.getSelectIcon());
        } else {
            setIcon(node.getIcon());
        }
        String path = node.getPath();
        if (path != null) {
            append(path);
        }
    }
}
