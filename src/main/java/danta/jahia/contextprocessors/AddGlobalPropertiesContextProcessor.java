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

package danta.jahia.contextprocessors;

import com.google.common.collect.Sets;
import danta.api.ExecutionContext;
import danta.api.exceptions.ProcessException;
import danta.core.contextprocessors.AbstractCheckComponentCategoryContextProcessor;
import danta.jahia.templating.TemplateContentModelImpl;
import danta.jahia.util.PropertyUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashMap;

import static danta.Constants.*;
import static danta.jahia.Constants.*;

/**
 * The context processor for adding global properties to content model
 *
 * @author      neozilon
 * @version     1.0.0
 * @since       2017-04-08
 */
@Component
@Service
public class AddGlobalPropertiesContextProcessor  extends AbstractCheckComponentCategoryContextProcessor<TemplateContentModelImpl> {

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
    public void process(final ExecutionContext executionContext, final TemplateContentModelImpl contentModel)
            throws ProcessException {
        try {
            Resource resource = (Resource) executionContext.get(JAHIA_RESOURCE);
            RenderContext renderContext = (RenderContext) executionContext.get(JAHIA_RENDER_CONTEXT);

            if (resource != null && renderContext != null) {
                String sitePath = renderContext.getSite().getPath();
                JCRSessionWrapper jcrSession = resource.getNode().getSession();

                // TODO Move this to a configuration PATH so every site can decide where to find it
                String dantaConfigurationNode = sitePath + DANTA_CONFIGURATION_DEFAULT_SUFFIX_PATH;
                if (jcrSession.nodeExists(dantaConfigurationNode)) {
                    JCRNodeWrapper dantaNode = jcrSession.getNode(dantaConfigurationNode);
                    if (dantaNode != null && dantaNode.hasNode(DANTA_CONFIGURATION_GLOBAL_NODE_NAME)) {

                        JCRNodeWrapper globalPropertiesNode = dantaNode.getNode(DANTA_CONFIGURATION_GLOBAL_NODE_NAME);
                        Map globalContext = processNode(globalPropertiesNode, renderContext, resource);
                        contentModel.set(GLOBAL_PROPERTIES_KEY , globalContext);

                    } else {
                        LOG.error("Danta Configuration Global Not found: " + dantaConfigurationNode + " For resource: " + resource.getPath());
                    }
                }else{
                    LOG.error("Danta Configuration Global Not found: " + dantaConfigurationNode + " For resource: " + resource.getPath());
                }
            }
        } catch (Exception e) {
            throw new ProcessException(e);
        }
    }

    protected Map processNode(JCRNodeWrapper node, RenderContext renderContext, Resource resource)throws Exception{
        if (node.hasNodes()){
            // Start a new level
            LinkedHashMap level = new LinkedHashMap();

            Iterator<JCRNodeWrapper> iterator = node.getNodes().iterator();
            while( iterator.hasNext()){
                JCRNodeWrapper childNode = iterator.next();
                level.put(childNode.getName(), this.processNode(childNode, renderContext, resource));
            }

            // Add current node properties (if any)
            Map<String, Object> currentProperties = PropertyUtils.propsToMap(
                    node.getProperties(), renderContext, resource);
            level.putAll(currentProperties);

            return level;

        } else {
            // Leaf node reached
            Map<String, Object> properties = PropertyUtils.propsToMap(
                    node.getProperties(), renderContext, resource);

            return properties;
        }
    }
}
