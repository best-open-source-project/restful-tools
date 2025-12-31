/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: KotlinUtil
  Author:   ZhangYuanSheng
  Date:     2020/9/6 22:35
  Description:
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.loren.restful.tools.utils.scanner;

import com.github.loren.restful.tools.annotation.SpringHttpMethodAnnotation;
import com.github.loren.restful.tools.beans.HttpMethod;
import com.github.loren.restful.tools.beans.PropertiesKey;
import com.github.loren.restful.tools.beans.Request;
import com.github.loren.restful.tools.beans.ServiceStub;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.psi.KtAnnotationEntry;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.kotlin.psi.KtCollectionLiteralExpression;
import org.jetbrains.kotlin.psi.KtDeclaration;
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression;
import org.jetbrains.kotlin.psi.KtExpression;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtNameReferenceExpression;
import org.jetbrains.kotlin.psi.KtNamedFunction;
import org.jetbrains.kotlin.psi.KtStringTemplateExpression;
import org.jetbrains.kotlin.psi.KtValueArgument;
import org.jetbrains.kotlin.psi.KtValueArgumentName;
import org.jetbrains.kotlin.psi.ValueArgument;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class KotlinUtil {

    public final Project PROJECT;
    public final Module MODULE;

    private KotlinUtil(@NotNull Module module) {
        PROJECT = module.getProject();
        this.MODULE = module;
    }

    public static KotlinUtil create(@NotNull Module module) {
        return new KotlinUtil(module);
    }

    @NotNull
    public static List<Request> getKotlinRequests(@NotNull Project project,
            @NotNull Module module) {
        List<Request> ktRequests = new ArrayList<>();
        KotlinUtil kotlinUtil = create(module);
        List<KtClass> kotlinClasses = kotlinUtil.getRestfulKotlinClasses(
                PropertiesKey.scanServiceWithLibrary(project));
        for (KtClass kotlinClass : kotlinClasses) {
            ServiceStub clsStub = null;
            for (KtAnnotationEntry annotationEntry : kotlinClass.getAnnotationEntries()) {
                if ((clsStub = getServiceStub(annotationEntry)) != null) {
                    break;
                }
            }

            List<ServiceStub> stubs = new ArrayList<>();
            for (KtNamedFunction function : kotlinUtil.getFunctionsOnKtClass(kotlinClass)) {
                for (KtAnnotationEntry annotationEntry : function.getAnnotationEntries()) {
                    ServiceStub serviceStub = getServiceStub(annotationEntry);
                    if (serviceStub == null) {
                        continue;
                    }
                    serviceStub.setPsiElement(function);
                    stubs.add(serviceStub);
                }
            }

            List<String> parentPaths = new ArrayList<>();
            List<HttpMethod> parentMethods = new ArrayList<>();
            List<Request> children = new ArrayList<>();
            if (clsStub != null) {
                parentPaths.addAll(clsStub.getPaths());
                parentMethods.addAll(clsStub.getMethods());
            }
            for (ServiceStub stub : stubs) {
                for (HttpMethod method : stub.getMethods()) {
                    for (String path : stub.getPaths()) {
                        children.add(new Request(method, path, stub.getPsiElement()));
                    }
                }
            }

            if (parentPaths.isEmpty()) {
                ktRequests.addAll(children);
            } else {
                parentPaths.forEach(parentPath -> children.forEach(childrenRequest -> {
                    if (childrenRequest.getMethod() != null
                            && childrenRequest.getMethod() != HttpMethod.REQUEST) {
                        Request request = childrenRequest.copyWithParent(
                                new Request(null, parentPath, null)
                        );
                        ktRequests.add(request);
                    } else {
                        for (HttpMethod parentMethod : parentMethods) {
                            Request request = childrenRequest.copyWithParent(
                                    new Request(parentMethod, parentPath, null)
                            );
                            ktRequests.add(request);
                        }
                    }
                }));
            }
        }
        return ktRequests;
    }

    /**
     * 根据Kotlin的注解获取ServiceStub
     *
     * @param ktAnnotationEntry Kotlin注解
     * @return ServiceStub
     * @see ServiceStub
     */
    @Nullable
    public static ServiceStub getServiceStub(@NotNull KtAnnotationEntry ktAnnotationEntry) {
        Name shortName = ktAnnotationEntry.getShortName();
        if (shortName == null) {
            return null;
        }
        String annotationName = shortName.asString();
        if (annotationName.contains(".")) {
            // 去掉 package
            annotationName = annotationName.substring(annotationName.lastIndexOf(".") + 1);
        }
        SpringHttpMethodAnnotation springHttpMethodAnnotation;
        if ((springHttpMethodAnnotation = SpringHttpMethodAnnotation.getByShortName(annotationName))
                == null) {
            return null;
        }

        ServiceStub serviceStub = new ServiceStub(ktAnnotationEntry);
        serviceStub.method(springHttpMethodAnnotation.getMethod());

        List<? extends ValueArgument> valueArguments = ktAnnotationEntry.getValueArguments();
        if (valueArguments.isEmpty()) {
            // 未设置注解的 value：@GetMapping
            serviceStub.path(null);
        } else {
            for (ValueArgument valueArgument : valueArguments) {
                if (!(valueArgument instanceof KtValueArgument ktValueArgument)) {
                    continue;
                }
                if (ktValueArgument.isNamed()) {
                    KtValueArgumentName argumentName = ktValueArgument.getArgumentName();
                    if (argumentName == null) {
                        continue;
                    }
                    String name = argumentName.getAsName().asString();
                    if ("method".equals(name)) {
                        KtExpression argumentExpression = ktValueArgument.getArgumentExpression();
                        if (argumentExpression instanceof KtCollectionLiteralExpression expression) {
                            for (PsiElement child : expression.getChildren()) {
                                if (!(child instanceof KtDotQualifiedExpression qualifiedExpression)) {
                                    continue;
                                }
                                if (qualifiedExpression.getChildren().length != 2) {
                                    continue;
                                }
                                if (!(qualifiedExpression.getChildren()[0] instanceof KtNameReferenceExpression)
                                        || !(qualifiedExpression.getChildren()[1] instanceof KtNameReferenceExpression referenceExpression)) {
                                    continue;
                                }
                                // 获取到当前注解的 HttpMethods 的其中一个(GET|POST|...)
                                final String currHttpMethod = referenceExpression.getText();
                                serviceStub.method(HttpMethod.parse(currHttpMethod));
                            }
                        }
                    }
                    if (!("path".equals(name) || "value".equals(name))) {
                        continue;
                    }
                }
                KtExpression expression = ktValueArgument.getArgumentExpression();
                if (expression instanceof KtStringTemplateExpression stringTemplateExpression) {
                    final String currPath = stringTemplateExpression.getChildren()[0].getText();
                    // 当前 paths 的其中一个（@RequestMapping(path=["path1", "path2"])）
                    serviceStub.path(currPath);
                    continue;
                }
                if (!(expression instanceof KtCollectionLiteralExpression collectionLiteralExpression)) {
                    continue;
                }
                if (collectionLiteralExpression.getChildren().length == 0) {
                    // 未设置注解的 value：@GetMapping
                    serviceStub.path(null);
                } else {
                    for (PsiElement psiElement : collectionLiteralExpression.getChildren()) {
                        if (!(psiElement instanceof KtStringTemplateExpression stringTemplateExpression)) {
                            continue;
                        }
                        final String currPath = stringTemplateExpression.getChildren()[0].getText();
                        // 当前 paths 的其中一个（@RequestMapping(path=["path1", "path2"])）
                        serviceStub.path(currPath);
                    }
                }
            }
        }

        return serviceStub;
    }

    public List<KtClass> getRestfulKotlinClasses(boolean withLib) {
        List<KtClass> classes = new ArrayList<>();

        for (SpringHelper.Control control : SpringHelper.Control.values()) {
            String name = withLib ? control.getQualifiedName() : control.getName();
            List<KtClass> list = findKtClassByAnnotationName(name, withLib);
            if (list.isEmpty()) {
                continue;
            }
            classes.addAll(list);
        }

        return classes;
    }

    public List<KtNamedFunction> getFunctionsOnKtClass(@NotNull KtClass ktClass) {
        List<KtNamedFunction> functions = new ArrayList<>();
        for (KtDeclaration declaration : ktClass.getDeclarations()) {
            if (declaration instanceof KtNamedFunction) {
                functions.add(((KtNamedFunction) declaration));
            }
        }
        return functions;
    }

    @NotNull
    public List<KtClass> findKtClassByAnnotationName(@NotNull String name, boolean withLib) {
        List<KtClass> classes = new ArrayList<>();
        for (KtAnnotationEntry entry : findKtAnnotationEntryByName(name, withLib)) {
            PsiElement context = entry.getContext();
            if (context != null) {
                context = context.getContext();
                if (context instanceof KtClass) {
                    classes.add(((KtClass) context));
                }
            }
        }
        return classes;
    }

    @NotNull
    private Set<KtAnnotationEntry> findKtAnnotationEntryByName(@NotNull String name,
            boolean withLib) {
        String temp = name.contains(".") ? name.substring(name.lastIndexOf(".") + 1) : name;

        GlobalSearchScope scope = withLib ?
                MODULE.getModuleWithLibrariesScope() :
                MODULE.getModuleScope();

        Set<KtAnnotationEntry> collection = new HashSet<>();

        // 遍历项目中的Kotlin文件来查找注解
        Collection<VirtualFile> psiFiles = FilenameIndex.getVirtualFilesByName(
                "*.kt",
                scope
        );

        for (VirtualFile virtualFile : psiFiles) {
            try {
            PsiFile psiFile = PsiManager.getInstance(PROJECT)
                    .findFile(virtualFile);
            if (psiFile instanceof KtFile ktFile) {
                KtAnnotationEntry[] annotationEntries = PsiTreeUtil.getChildrenOfType(ktFile,
                        KtAnnotationEntry.class);
                if (annotationEntries != null) {
                    for (KtAnnotationEntry entry : annotationEntries) {
                        Name shortName = entry.getShortName();
                        if (shortName != null) {
                            String entryName = shortName.asString();
                            if (entryName.equals(temp) || entryName.equals(name)) {
                                collection.add(entry);
                            }
                        }
                    }
                }
            }
            } catch (Exception e) {
                // 忽略处理单个文件时的异常，继续处理其他文件
                continue;
            }
        }

        return collection;
    }
}
