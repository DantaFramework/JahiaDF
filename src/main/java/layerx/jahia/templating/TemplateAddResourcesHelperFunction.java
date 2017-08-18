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

import com.github.jknack.handlebars.Handlebars;
import layerx.jahia.templating.tagsupport.AddResourcesTag;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

import javax.script.Bindings;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * This helper allows the developer to add resournces in the html content.
 * It is a replacement for the <template:addResources /> tag
 * <p>
 * Examples:
 * <pre><blockquote>
 *      {%addResources "my_javascript.js" type="javascript" %}
 * </blockquote></pre>
 * <pre><blockquote>
 *      {%addResources "my_css.css" type="css" media="print" %}
 * </blockquote></pre>
 * <pre><blockquote>
 *      {%addResources "css_1.css,css_2.css" type="css" %}
 * </blockquote></pre>
 * </p>
 * The first argument are the resources to be added.
 * Currently supported parameters from the <template:addResources /> tag:
 *  type
 *  rel
 *  media
 *  condition
 *  insert
 *  targetTag
 *
 * @author      jarriola
 * @version     1.0.0
 * @since       2016-10-06
 */
@Component
@Service
public class TemplateAddResourcesHelperFunction
        extends AbstractJahiaHelperFunction<String> {

    protected static final String NAME = "addResources";

    public TemplateAddResourcesHelperFunction() { super(NAME); }

    public TemplateAddResourcesHelperFunction(
            Bindings bindings,
            HttpServletRequest request) {

        super(NAME);

        this.bindings = bindings;
        this.request = request;
    }

    @Override
    public CharSequence execute(final String resources)
            throws Exception {
        this.builder = new StringBuilder("");

        AddResourcesTag addResources = new AddResourcesTag();
        addResources.setCallerHelper(this);

        addResources.setResources(resources);
        if(params() != null) {
            Map<String, Object> paramsMap = paramsMap();
            for (Map.Entry<String, Object> entry : paramsMap.entrySet()) {
                String param = entry.getKey();
                String value = (String) entry.getValue();

                if(param.equals("type")) {
                    addResources.setType(value);

                } else if(param.equals("rel")){
                    addResources.setRel(value);

                } else if(param.equals("media")){
                    addResources.setMedia(value);

                } else if(param.equals("condition")){
                    addResources.setCondition(value);

                } else if(param.equals("insert")){
                    addResources.setInsert(Boolean.valueOf(value));

                } else if(param.equals("targetTag")){
                    addResources.setTargetTag(value);
                }
            }

        } else {
            throw new Exception ("Invalid parameters for helper: " + NAME);
        }

        addResources.doEndTag();

        return new Handlebars.SafeString(this.builder.toString());
    }
}
