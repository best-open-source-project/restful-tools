/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: Bundle
  Author:   ZhangYuanSheng
  Date:     2020/9/4 23:15
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.loren.restful.tools.utils;

import com.intellij.AbstractBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class Bundle extends AbstractBundle {

    @NonNls
    public static final String BUNDLE = "messages.RestfulToolBundle";
    private static final Bundle INSTANCE = new Bundle();

    private Bundle() {
        super(BUNDLE);
    }

    public static String message(@PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
        return INSTANCE.getMessage(key, params);
    }
}
