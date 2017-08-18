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

import java.util.Map;

import com.github.jknack.handlebars.Handlebars;
import layerx.jahia.templating.tagsupport.ModuleTag;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

import javax.jcr.PathNotFoundException;
import javax.script.Bindings;
import javax.servlet.http.HttpServletResponse;

/**
 * The include helper allows the developer to include components in the html content.
 * It can be used to include jahia or custom components. It is a replacement for the <template:module /> tag.
 * The use of this helper is discouraged since the helper #includeArea provides a more flexible and authorable way for
 * including components.
 * <p>
 * Examples:
 * <pre><blockquote>
 *      {%include path="/modules/layerx-demo-templates-02" %}
 * </blockquote></pre>
 * </p>
 * This helper does not support a default unnamed parameter since
 * template:module allows many ways to include components (e.g. either path, node or nodeName)
 * Currently supported parameters from the <template:module /> tag:
 *  path
 *  view
 *  nodeTypes
 *  editable
 *  node
 *
 * @author      jarriola
 * @version     1.0.0
 * @since       2016-08-11
 */
@Component
@Service
public class TemplateModuleHelperFunction
        extends AbstractJahiaHelperFunction<Object> {

    protected static final String NAME = "include";

    public TemplateModuleHelperFunction() { super(NAME); }

    public TemplateModuleHelperFunction(
            Bindings bindings,
            HttpServletResponse response) {

        super(NAME);

        this.bindings = bindings;
        this.response = response;
    }

    @Override
    public CharSequence execute(final Object obj)
            throws Exception {
        this.builder = new StringBuilder("");

        ModuleTag module = new ModuleTag();
        module.setCallerHelper(this);

        if(params() != null) {
            Map<String, Object> paramsMap = paramsMap();
            for (Map.Entry<String, Object> entry : paramsMap.entrySet()) {
                String param = entry.getKey();
                String value = (String) entry.getValue();

                if(param.equals("path")) {
                    module.setPath(value);

                } else if(param.equals("view")){
                    module.setView(value);

                } else if(param.equals("nodeTypes")){
                    module.setNodeTypes(value);

                } else if(param.equals("editable")){
                    module.setEditable(Boolean.valueOf(value));

                } else if (param.equals("node")){
                    JCRNodeWrapper node = null;
                    Resource currentResource = (Resource) bindings.get("currentResource");

                    if (!value.startsWith("/")) {
                        JCRNodeWrapper nodeWrapper = currentResource.getNode();
                        if (!value.equals("*") && nodeWrapper.hasNode(value)) {
                            node = nodeWrapper.getNode(value);
                        }
                    } else if (value.startsWith("/")) {
                        JCRSessionWrapper session = currentResource.getNode().getSession();
                        try {
                            node = (JCRNodeWrapper) session.getItem(value);
                        } catch (PathNotFoundException e) { }
                    }

                    module.setNode(node);
                }
            }

        } else {
            throw new Exception ("Invalid parameters for helper: " + NAME);
        }

        module.doEndTag();

        return new Handlebars.SafeString(this.builder.toString());
    }
}
