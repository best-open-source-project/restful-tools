/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: GotoRequestAction
  Author:   ZhangYuanSheng
  Date:     2020/5/12 16:22
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.loren.restful.tools.view.search;

import com.github.loren.restful.tools.beans.HttpMethod;
import com.github.loren.restful.tools.utils.Bundle;
import com.github.loren.restful.tools.view.icon.Icons;
import com.intellij.featureStatistics.FeatureUsageTracker;
import com.intellij.icons.AllIcons;
import com.intellij.ide.actions.GotoActionBase;
import com.intellij.ide.util.gotoByName.ChooseByNameFilter;
import com.intellij.ide.util.gotoByName.ChooseByNameItemProvider;
import com.intellij.ide.util.gotoByName.ChooseByNameModel;
import com.intellij.ide.util.gotoByName.ChooseByNamePopup;
import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import java.util.Arrays;
import java.util.List;
import javax.swing.Icon;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class GotoRequestAction extends GotoActionBase implements DumbAware {

    public GotoRequestAction() {
        getTemplatePresentation().setText(Bundle.message("action.Search.text"));
        getTemplatePresentation().setDescription(Bundle.message("action.Search.description"));
        getTemplatePresentation().setIcon(AllIcons.Actions.Search);
    }

    @Override
    protected void gotoActionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        // 显示featureId对应的Tips
        FeatureUsageTracker.getInstance().triggerFeatureUsed("navigation.popup.service");

        ChooseByNameContributor[] contributors = {
                new GotoRequestContributor(e.getData(LangDataKeys.MODULE)),
        };

        RequestFilteringGotoByModel model = new RequestFilteringGotoByModel(project, contributors);

        GotoActionCallback<HttpMethod> callback = new GotoActionCallback<>() {

            @NotNull
            @Contract("_ -> new")
            @Override
            protected ChooseByNameFilter<HttpMethod> createFilter(
                    @NotNull ChooseByNamePopup popup) {
                return new GotoRequestMappingFilter(popup, model, project);
            }

            @Override
            public void elementChosen(ChooseByNamePopup chooseByNamePopup, Object element) {
                if (element instanceof RestServiceItem navigationItem) {
                    if (navigationItem.canNavigate()) {
                        navigationItem.navigate(true);
                    }
                }
            }
        };

        GotoRequestProvider provider = new GotoRequestProvider(getPsiContext(e));
        showNavigationPopup(
                e, model, callback,
                Bundle.message("search.FindUsagesTitle"),
                true,
                true,
                provider
        );
    }

    @Override
    protected <T> void showNavigationPopup(@NotNull AnActionEvent e,
            @NotNull ChooseByNameModel model,
            final GotoActionCallback<T> callback,
            @Nullable final String findUsagesTitle,
            boolean useSelectionFromEditor,
            final boolean allowMultipleSelection,
            final ChooseByNameItemProvider itemProvider) {
        final Project project = e.getData(CommonDataKeys.PROJECT);
        //noinspection ConstantConditions
        boolean mayRequestOpenInCurrentWindow = model.willOpenEditor() &&
                FileEditorManagerEx.getInstanceEx(project).hasSplitOrUndockedWindows();
        Pair<String, Integer> start = getInitialText(useSelectionFromEditor, e);
        showNavigationPopup(callback, findUsagesTitle,
                RestChooseByNamePopup.createPopup(project, model, itemProvider, start.first,
                        mayRequestOpenInCurrentWindow,
                        start.second),
                allowMultipleSelection);
    }

    protected static class GotoRequestMappingFilter extends ChooseByNameFilter<HttpMethod> {


        GotoRequestMappingFilter(final ChooseByNamePopup popup,
                RequestFilteringGotoByModel model, final Project project) {
            super(popup, model, GotoRequestConfiguration.getInstance(project), project);
        }



        @Override
        @NotNull
        protected List<HttpMethod> getAllFilterValues() {
            return Arrays.asList(HttpMethod.values());
        }

        @Override
        protected String textForFilterValue(@NotNull HttpMethod value) {
            return value.name();
        }

        @Override
        protected Icon iconForFilterValue(@NotNull HttpMethod value) {
            return Icons.getMethodIcon(value);
        }
    }
}
