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
import danta.api.configuration.Configuration;
import danta.api.exceptions.ProcessException;
import danta.core.contextprocessors.AbstractCheckComponentCategoryContextProcessor;
import danta.jahia.templating.TemplateContentModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.*;
import org.jahia.services.render.scripting.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static danta.Constants.*;
import static danta.core.util.ObjectUtils.wrap;
import static danta.jahia.Constants.*;
import static danta.jahia.util.PropertyUtils.propsToMap;

import static danta.jahia.Constants.RESERVED_SYSTEM_NAME_PREFIXES;

/**
 * The context processor for adding page properties to content model
 *
 * @author      neozilon
 * @version     1.0.0
 * @since       2017-11-04
 */
@Component
@Service
public class AddPagePropertiesContextProcessor extends AbstractCheckComponentCategoryContextProcessor<TemplateContentModel>  {

    private static Logger LOG = LoggerFactory.getLogger(AddPagePropertiesContextProcessor.class);

    @Override
    public Set<String> anyOf() {
        return Sets.newHashSet(PAGE_CATEGORY);
    }

    @Override
    public int priority() {
        // This processor must be one of the first processors executed.
        return HIGHEST_PRIORITY;
    }

    @Override
    public void process(final ExecutionContext executionContext, final TemplateContentModel contentModel)throws ProcessException {
        try {

            Resource resource = (Resource) executionContext.get(JAHIA_RESOURCE);
            RenderContext renderContext = (RenderContext) executionContext.get(JAHIA_RENDER_CONTEXT);

            Map<String, Object> pageContent = new HashMap<>();

            if (resource != null && renderContext != null) {
                if (!contentModel.has(PAGE_PROPERTIES_KEY)) {

                    Resource mainResource = renderContext.getMainResource();
                    if (mainResource != null) {
                        JCRNodeWrapper mainNode = mainResource.getNode();

                        Map<String, Object> propsMap = propsToMap(mainNode.getProperties());
                        for (String propertyName : propsMap.keySet()) {
                            if (!StringUtils.startsWithAny(propertyName, RESERVED_SYSTEM_NAME_PREFIXES)) {
                                pageContent.put(propertyName, propsMap.get(propertyName));
                            }
                        }

                        //pageContent.put(PATH, mainResource.getPath());
                        pageContent.put(PATH, mainNode.getPath());
                        pageContent.put(LINK, mainNode.getUrl());
                        pageContent.put(PAGE_NAME, mainNode.getName());
                        pageContent.put(PAGE_TITLE, mainNode.getDisplayableName());
                        pageContent.put(KEYWORDS, (pageContent.containsKey(JAHIA_PAGE_KEYWORDS)) ? pageContent.get(JAHIA_PAGE_KEYWORDS) : "");
                        pageContent.put(TAGS, (pageContent.containsKey(JAHIA_PAGE_TAGS)) ? pageContent.get(JAHIA_PAGE_TAGS) : "");

                        // add interface mode
                        String mode = renderContext.getMode();
                        pageContent.put(IS_EDIT_MODE, renderContext.isEditMode());
                        pageContent.put(IS_PREVIEW_MODE, renderContext.isPreviewMode());
                        pageContent.put(IS_LIVE_MODE, renderContext.isLiveMode());
                        pageContent.put(IS_AJAX_REQUEST, renderContext.isAjaxRequest());
                        pageContent.put(JAHIA_WORKSPACE, renderContext.getWorkspace());

                        // Set Danta configuration for page resources only as it is obtained via the template
                        // associated to the view of the page (set by the script engine)
                        if ( resource.getNode() != null && resource.getNode().getPrimaryNodeType() != null &&
                                StringUtils.startsWithAny(resource.getNode().getPrimaryNodeType().getName(),
                                        LIST_TEMPLATE_OPTION_TYPES) ) {
                            View view = (View) executionContext.get(JAHIA_SCRIPT_VIEW);
                            if (view != null) {
                                Configuration configuration = configurationProvider.getFor(view);
                                contentModel.setAsIsolated(CONFIG_PROPERTIES_KEY, wrap(configuration.toMap()));
                            }
                        }

                        // Load Page Properties From Danta Functionality BEGIN ****************************************
                        String sitePath = renderContext.getSite().getPath();
                        Session jcrSession = resource.getNode().getSession();

                        // TODO Move this to a configuration PATH so every site can decide where to find it
                        String layerXConfigurationNode = sitePath + LAYERX_CONFIGURATION_DEFAULT_SUFFIX_PATH;
                        if (jcrSession.nodeExists(layerXConfigurationNode)) {
                            Node layerXNode = jcrSession.getNode(layerXConfigurationNode);
                            if (layerXNode != null && layerXNode.hasNode(LAYERX_CONFIGURATION_PAGE_NODE_NAME)) {

                                Node pagesPropertiesNode = layerXNode.getNode(LAYERX_CONFIGURATION_PAGE_NODE_NAME);
                                Node pageProperties = this.getPagePropertiesNode(pagesPropertiesNode, mainNode.getPath() );
                                if (pageProperties != null){
                                    Map<String, Object> propsMapPage = propsToMap(pageProperties);
                                    pageContent.putAll(propsMapPage);
                                }else{
                                    LOG.error("Page Properties node not found For Path: " + mainNode.getPath() );
                                }
                            } else {
                                LOG.error("Danta Configuration PageStructure Not found: " + layerXConfigurationNode + " For resource: " + mainResource.getPath());
                            }
                        }else{
                            LOG.error("Danta Configuration Structure Not found: " + layerXConfigurationNode + " For resource: " + mainResource.getPath());
                        }

                        // Load Page Properties From Danta Functionality END ******************************************
                        contentModel.set(PAGE_PROPERTIES_KEY, pageContent);

                    }
                }

            }
        } catch (Exception e) {
            LOG.error("LAYERX Exception: "+e.getMessage(),e);
            throw new ProcessException(e);
        }


    }

    private Node getPagePropertiesNode(Node pagesPropertyNode, String pagePathToFind){
        Node pageProperties = null;
        try {
            NodeIterator iterator = pagesPropertyNode.getNodes();
            while (iterator.hasNext()) {
                Node n = iterator.nextNode();
                if (n != null && n.hasProperty(LAYERX_CONFIGURATION_PAGE_PATH_PROPERTY)) {
                    Property property = n.getProperty(LAYERX_CONFIGURATION_PAGE_PATH_PROPERTY);
                    String path = property.getString();
                    if (path.equals(pagePathToFind)) {
                        pageProperties = n;
                        break;
                    }
                }
            }
        }catch (RepositoryException re){
            LOG.error("Error finding node for: "+pagePathToFind+ " in getPagePropertiesNode",re);
        }
        return pageProperties;
    }

}
