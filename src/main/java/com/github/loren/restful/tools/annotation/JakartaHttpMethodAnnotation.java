package com.github.loren.restful.tools.annotation;

import com.github.loren.restful.tools.beans.HttpMethod;
import com.jetbrains.cef.remote.thrift.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

/**
 * @author luowenjie
 * @since 2025/12/31
 */
public enum JakartaHttpMethodAnnotation {

    /**
     * GET
     */
    GET("jakarta.ws.rs.GET", HttpMethod.GET),
    /**
     * POST
     */
    POST("jakarta.ws.rs.POST", HttpMethod.POST),
    /**
     * PUT
     */
    PUT("jakarta.ws.rs.PUT", HttpMethod.PUT),
    /**
     * DELETE
     */
    DELETE("jakarta.ws.rs.DELETE", HttpMethod.DELETE),
    /**
     * HEAD
     */
    HEAD("jakarta.ws.rs.HEAD", HttpMethod.HEAD),
    /**
     * PATCH
     */
    PATCH("jakarta.ws.rs.PATCH", HttpMethod.PATCH);

    private String qualifiedName;
    private HttpMethod method;

    JakartaHttpMethodAnnotation(String qualifiedName, HttpMethod method) {
        this.qualifiedName = qualifiedName;
        this.method = method;
    }

    @Nullable
    public static JakartaHttpMethodAnnotation getByQualifiedName(String qualifiedName) {
        for (JakartaHttpMethodAnnotation springRequestAnnotation : JakartaHttpMethodAnnotation.values()) {
            if (springRequestAnnotation.getQualifiedName().equals(qualifiedName)) {
                return springRequestAnnotation;
            }
        }
        return null;
    }

    public HttpMethod getMethod() {
        return this.method;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    @NotNull
    public String getShortName() {
        return qualifiedName.substring(qualifiedName.lastIndexOf(".") - 1);
    }
}
