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

import danta.jahia.templating.tagsupport.AreaTag;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import com.github.jknack.handlebars.Handlebars;

import javax.script.Bindings;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * This helper allows the developer to include areas in the html content.
 * It can be used to include jahia or custom components. It is a replacement for the <template:area /> tag
 * <p>
 * Examples:
 * <pre><blockquote>
 *      {%includeArea "footer" %}
 * </blockquote></pre>
 * <pre><blockquote>
 *      {%includeArea "footer" nodeTypes="jdmix:footerWidgets" moduleType="absoluteArea" level="0" %}
 * </blockquote></pre>
 * </p>
 * The first argument is the path of the area.
 * Currently supported parameters from the <template:area /> tag:
 *  moduleType
 *  level
 *  nodeTypes
 *  editable
 *  listLimit
 *
 * @author      jarriola
 * @version     1.0.0
 * @since       2016-10-06
 */
@Component
@Service
public class TemplateAreaHelperFunction
        extends AbstractJahiaHelperFunction<String> {

    protected static final String NAME = "includeArea";

    public TemplateAreaHelperFunction() { super(NAME); }

    public TemplateAreaHelperFunction(
            Bindings bindings,
            HttpServletRequest request,
            HttpServletResponse response) {

        super(NAME);

        this.bindings = bindings;
        this.request = request;
        this.response = response;
    }

    @Override
    public CharSequence execute(final String path)
            throws Exception {
        this.builder = new StringBuilder("");

        AreaTag area = new AreaTag();
        area.setCallerHelper(this);

        area.setPath(path);
        if(params() != null) {
            Map<String, Object> paramsMap = paramsMap();
            for (Map.Entry<String, Object> entry : paramsMap.entrySet()) {
                String param = entry.getKey();
                String value = (String) entry.getValue();

                if(param.equals("moduleType")) {
                    area.setModuleType(value);

                } else if(param.equals("level")){
                    area.setLevel(new Integer(value));

                } else if(param.equals("nodeTypes")){
                    area.setNodeTypes(value);

                } else if(param.equals("editable")){
                    area.setEditable(Boolean.valueOf(value));

                } else if(param.equals("listLimit")){
                    area.setListLimit(Integer.parseInt(value));
                }
            }

        } else {
            throw new Exception ("Invalid parameters for helper: " + NAME);
        }

        area.doEndTag();

        return new Handlebars.SafeString(this.builder.toString());
    }
}
