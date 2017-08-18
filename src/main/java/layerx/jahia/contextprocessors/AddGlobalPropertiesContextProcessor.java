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

package layerx.jahia.contextprocessors;

import com.google.common.collect.Sets;
import layerx.api.ExecutionContext;
import layerx.api.configuration.Configuration;
import layerx.api.exceptions.ProcessException;
import layerx.core.contextprocessors.AbstractCheckComponentCategoryContextProcessor;
import layerx.jahia.templating.TemplateContentModel;
import layerx.jahia.util.PropertyUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.jahia.services.content.JCRNodeIteratorWrapper;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.notification.templates.Link;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.SiteInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashMap;

import static layerx.Constants.*;
import static layerx.core.util.ObjectUtils.wrap;
import static layerx.jahia.Constants.*;

/**
 * The context processor for adding global properties to content model
 *
 * @author      neozilon
 * @version     1.0.0
 * @since       2017-04-08
 */
@Component
@Service
public class AddGlobalPropertiesContextProcessor  extends AbstractCheckComponentCategoryContextProcessor<TemplateContentModel> {

    private static Logger LOG = LoggerFactory.getLogger(AddGlobalPropertiesContextProcessor.class);

    @Override
    public Set<String> anyOf() {
        return Sets.newHashSet(GLOBAL_CATEGORY);
    }

    @Override
    public int priority() {
        // This processor must be one of the first processors executed.
        return HIGHEST_PRIORITY;
    }

    @Override
    public void process(final ExecutionContext executionContext, final TemplateContentModel contentModel)
            throws ProcessException {
        try {
            Resource resource = (Resource) executionContext.get(JAHIA_RESOURCE);
            RenderContext renderContext = (RenderContext) executionContext.get(JAHIA_RENDER_CONTEXT);

            if (resource != null && renderContext != null) {
                String sitePath = renderContext.getSite().getPath();
                JCRSessionWrapper jcrSession = resource.getNode().getSession();

                // TODO Move this to a configuration PATH so every site can decide where to find it
                String layerXConfigurationNode = sitePath + LAYERX_CONFIGURATION_DEFAULT_SUFFIX_PATH;
                if (jcrSession.nodeExists(layerXConfigurationNode)) {
                    JCRNodeWrapper layerXNode = jcrSession.getNode(layerXConfigurationNode);
                    if (layerXNode != null && layerXNode.hasNode(LAYERX_CONFIGURATION_GLOBAL_NODE_NAME)) {

                        JCRNodeWrapper globalPropertiesNode = layerXNode.getNode(LAYERX_CONFIGURATION_GLOBAL_NODE_NAME);
                        // TODO: Remove old version
                        //processNode(globalPropertiesNode, null, contentModel);
                        Map globalContext = processNode(globalPropertiesNode);
                        contentModel.set(GLOBAL_PROPERTIES_KEY , globalContext);

                    } else {
                        LOG.error("LayerX Configuration Global Not found: " + layerXConfigurationNode + " For resource: " + resource.getPath());
                    }
                }else{
                    LOG.error("LayerX Configuration Global Not found: " + layerXConfigurationNode + " For resource: " + resource.getPath());
                }
            }
        } catch (Exception e) {
            throw new ProcessException(e);
        }
    }

    protected Map processNode(JCRNodeWrapper node)throws Exception{
        if (node.hasNodes()){
            // Start a new level
            LinkedHashMap globalContext = new LinkedHashMap();

            Iterator<JCRNodeWrapper> iterator = node.getNodes().iterator();
            while( iterator.hasNext()){
                JCRNodeWrapper childNode = iterator.next();
                globalContext.put(childNode.getName(), this.processNode(childNode));
            }

            return globalContext;

        } else {
            // Leaf node reached
            Map<String, Object> globalProperties = PropertyUtils.propsToMap(node);

            return globalProperties;
        }
    }
}
