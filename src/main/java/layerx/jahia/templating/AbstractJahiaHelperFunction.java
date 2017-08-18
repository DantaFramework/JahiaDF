/**
 * LayerX Jahia Bundle
 * (layerx.jahia)
 *
 * Copyright (C) 2017 Tikal Technologies, Inc. All rights reserved.
 *
 * Licensed under GNU Affero General Public License, Version v3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied;
 * without even the implied warranty of MERCHANTABILITY.
 * See the License for more details.
 */

package layerx.jahia.templating;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Options;
import org.apache.felix.scr.annotations.Component;

import javax.script.Bindings;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

import static layerx.jahia.Constants.HTTP_REQUEST;
import static layerx.jahia.templating.LayerXScriptEngine.TEMPLATE_CONTENT_MODEL_ATTR_NAME;

/**
 * The abstraction for Jahia Helper Function
 *
 * @author      jarriola
 * @version     1.0.0
 * @since       2017-08-11
 */
@Component(componentAbstract = true)
public abstract class AbstractJahiaHelperFunction<T> implements HelperFunction<T> {

    private static final ThreadLocal<Options> threadLocal = new ThreadLocal<>();
    private final String name;

    protected AbstractJahiaHelperFunction(String name) {
        this.name = name;
    }

    /* These objects are used by the implementation of some helpers (e.g. module, area). Since such implementations
     * are based on their JSP counterparts, these objects are used to replace objects retrieved by the PageContext object
     * commonly used in custom JSP tag libraries.
     */
    protected Bindings bindings;
    protected HttpServletResponse response;
    protected HttpServletRequest request;
    public StringBuilder builder;

    public Bindings getBindings() { return bindings; }

    public HttpServletResponse getResponse() { return response; }

    public HttpServletRequest getRequest() { return request; }

    @Override
    public final String name() {
        return name;
    }

    @Override
    public abstract CharSequence execute(final T valueObj)
            throws Exception;

    protected final CharSequence transclude(final Object valueObj)
            throws Exception {
        return options().fn(valueObj);
    }

    protected final CharSequence transclude()
            throws Exception {
        return options().fn();
    }

    protected final Options options() {
        return threadLocal.get();
    }

    protected final Object paramAt(final int index) {
        return options().param(index);
    }

    protected final <T> T paramAt(final int index, final T defaultValue) {
        return options().param(index);
    }

    protected final Object[] params() {
        return options().params;
    }

    protected final Map<String, Object> paramsMap() {
        return options().hash;
    }

    protected final Object param(String name) {
        return options().hash(name);
    }

    protected final <T> T param(String name, final T defaultValue) {
        return options().hash(name);
    }

    protected final HttpServletRequest request() {
        Options options = options();
        if (options != null) {
            Context context = options.context;
            HttpServletRequest result = (HttpServletRequest) context.get(HTTP_REQUEST);
            return result;
        }
        return null;
    }

    protected final TemplateContentModel contentModel() {
        return request() != null? (TemplateContentModel) request().getAttribute(TEMPLATE_CONTENT_MODEL_ATTR_NAME) : null;
    }

    @Override
    public final CharSequence apply(final T value, final Options options)
            throws IOException {
        try {
            threadLocal.set(options);
            return execute(value);
        } catch (Exception ew) {
            throw new RuntimeException(ew);
        } finally {
            threadLocal.remove();
        }
    }
}
