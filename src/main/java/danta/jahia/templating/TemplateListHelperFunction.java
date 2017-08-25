/**
 * Danta Jahia Bundle
 * (danta.jahia)
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

package danta.jahia.templating;

import com.github.jknack.handlebars.Handlebars;
import danta.jahia.templating.tagsupport.ListTag;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

import javax.script.Bindings;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * This helper allows the developer to include areas in the html content.
 * It can be used to include jahia or custom components. It is a replacement for the <template:area /> tag
 * <p>
 * Examples:
 * <pre><blockquote>
 *      {%includeList path="headerNews" %}
 * </blockquote></pre>
 * <pre><blockquote>
 *      {%includeList path="headerNews" nodeTypes="d:NewsItemContainer" %}
 * </blockquote></pre>
 * </p>
 * Currently supported parameters from the <template:list /> tag:
 *  path
 *  editable
 *  nodeTypes
 *
 * @author      jarriola
 * @version     1.0.0
 * @since       2016-08-11
 */
@Component
@Service
public class TemplateListHelperFunction
        extends AbstractJahiaHelperFunction<Object> {

    protected static final String NAME = "includeList";

    public TemplateListHelperFunction() { super(NAME); }

    public TemplateListHelperFunction(
            Bindings bindings,
            HttpServletRequest request,
            HttpServletResponse response) {

        super(NAME);

        this.bindings = bindings;
        this.request = request;
        this.response = response;
    }

    @Override
    public CharSequence execute(final Object obj)
            throws Exception {
        this.builder = new StringBuilder("");

        ListTag list = new ListTag();
        list.setCallerHelper(this);

        if(params() != null) {
            Map<String, Object> paramsMap = paramsMap();
            for (Map.Entry<String, Object> entry : paramsMap.entrySet()) {
                String param = entry.getKey();
                String value = (String) entry.getValue();

                if(param.equals("path")) {
                    list.setPath(value);

                } else if(param.equals("nodeTypes")){
                    list.setNodeTypes(value);

                } else if(param.equals("editable")){
                    list.setEditable(Boolean.valueOf(value));

                }
            }

        } else {
            throw new Exception ("Invalid parameters for helper: " + NAME);
        }

        list.doEndTag();

        return new Handlebars.SafeString(this.builder.toString());
    }
}
