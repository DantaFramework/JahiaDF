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

package layerx.jahia.servlets;

import java.util.List;
import java.util.Map;
import java.util.Collection;

import layerx.jahia.util.JahiaUtils;
import net.minidev.json.JSONObject;

import javax.jcr.Node;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.jcr.NodeIterator;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.URLResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.services.render.Resource;
import org.jahia.services.render.View;

import layerx.api.ContextProcessorEngine;
import layerx.api.configuration.Configuration;
import layerx.api.configuration.ConfigurationProvider;
import layerx.api.configuration.Mode;
import layerx.jahia.services.ContentModelFactoryService;
import layerx.jahia.templating.TemplateContentModel;

import static layerx.Constants.*;
import static layerx.core.Constants.*;
import static layerx.jahia.Constants.*;

/**
 * This class takes page's content model and turns into action result
 *
 * @author      jarriola
 * @version     1.0.0
 * @since       2017-06-29
 */
public class PageContentModelToJSONServlet extends Action {

    private static Logger LOG = LoggerFactory.getLogger(PageContentModelToJSONServlet.class);

    private ContentModelFactoryService contentModelFactoryService;

    private ContextProcessorEngine contextProcessorEngine;

    private ConfigurationProvider configurationProvider;

    public ContextProcessorEngine getContextProcessorEngine() {
        return contextProcessorEngine;
    }

    public void setContextProcessorEngine(ContextProcessorEngine contextProcessorEngine) {
        this.contextProcessorEngine = contextProcessorEngine;
    }

    public ContentModelFactoryService getContentModelFactoryService() {
        return contentModelFactoryService;
    }

    public void setContentModelFactoryService(ContentModelFactoryService contentModelFactoryService) {
        this.contentModelFactoryService = contentModelFactoryService;
    }

    public ConfigurationProvider getConfigurationProvider() {
        return configurationProvider;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider) {
        this.configurationProvider = configurationProvider;
    }

    @Override
    public ActionResult doExecute(HttpServletRequest request, RenderContext renderContext, Resource resource,
                                  JCRSessionWrapper session, Map<String, List<String>> parameters,
                                  URLResolver urlResolver) throws Exception {

        JSONObject filteredContentMap = new JSONObject();
        final HttpServletResponse response = renderContext.getResponse();

        try {
            View view = null;
            if ( resource.getNode() != null && resource.getNode().getPrimaryNodeType() != null &&
                    StringUtils.startsWithAny(resource.getNode().getPrimaryNodeType().getName(),
                            LIST_TEMPLATE_OPTION_TYPES) ){
                view = JahiaUtils.resolveTemplateResourceView(request, resource, renderContext);
                // Set view in the request since the view is needed by the contentModelFactoryService service
                request.setAttribute(JAHIA_SCRIPT_VIEW, view);
            }
            if (view != null) {
                TemplateContentModel templateContentModel = (TemplateContentModel)
                        contentModelFactoryService.getContentModel(request, response, renderContext, resource);

                boolean clientAccessible =
                        (Boolean) templateContentModel.get(CONFIG_PROPERTIES_KEY + DOT + XK_CLIENT_ACCESSIBLE_CP);

                if (clientAccessible) {
                    // get list of contexts
                    Configuration configuration = configurationProvider.getFor(view);

                    Collection<String> props = configuration.asStrings(XK_CLIENT_MODEL_PROPERTIES_CP, Mode.MERGE);
                    String[] contexts = props.toArray(new String[0]);

                    // get content model json with the XK_CLIENT_MODEL_PROPERTIES_CP contexts
                    filteredContentMap = templateContentModel.toJSONObject(contexts);

                    // add component id
                    String componentContentId = DigestUtils.md5Hex(resource.getPath());
                    filteredContentMap.put(XK_CONTENT_ID_CP, componentContentId);
                }

                // add component list with clientaccessible as true on the resource page
                filteredContentMap.put(PAGE_COMPONENT_RESOURCE_LIST_AN, getComponentList(
                        resource, request, response, renderContext));
            }
        } catch (Exception ew) {

            throw new ServletException(ew);
        }

        response.setContentType(SERVER_RESPONSE_CONTENT_TYPE);
        response.getWriter().write( filteredContentMap.toString() );

        return ActionResult.OK;
    }


    /**
     * This method gets the component list with the clientaccessible property as true on the current page resource
     *
     * @param resource
     * @return a jsonobject with the component list
     */
    private JSONObject getComponentList(Resource resource, HttpServletRequest request, HttpServletResponse response,
                                        RenderContext renderContext) throws Exception {
        JSONObject componentContentModels = new JSONObject();

        JCRNodeWrapper node = resource.getNode();
        NodeIterator iterator = node.getNodes();

        while (iterator.hasNext()) {
            Node child = iterator.nextNode();
            if (!child.getPrimaryNodeType().getName().equals(JAHIA_JNT_PAGE)) {

                if (child.getPrimaryNodeType().getName().equals(JAHIA_JNT_CONTENT_LIST)) {
                    NodeIterator childIterator = child.getNodes();
                    while (childIterator.hasNext()) {
                        Node listChild = childIterator.nextNode();
                        Resource listChildResource = new Resource((JCRNodeWrapper) listChild, LX, null,
                                Resource.CONFIGURATION_WRAPPER
                        );

                        // TODO: ConfigurationProvider should have implemented asBoolean to avoid creating a new content model
                        // Configuration configuration = configurationProvider.getFor(listChildResource);
                        // Boolean clientAccessible = configuration.asBoolean(XK_CLIENT_ACCESSIBLE_CP);

                        TemplateContentModel templateContentModel = (TemplateContentModel)
                                contentModelFactoryService.getContentModel(request, response, renderContext, resource);

                        boolean clientAccessible =
                                (Boolean) templateContentModel.get(CONFIG_PROPERTIES_KEY + DOT + XK_CLIENT_ACCESSIBLE_CP);

                        if (clientAccessible) {
                            int prefixLength = resource.getPath().substring(0, resource.getPath().indexOf(DOT)).length();
                            String trimmedPath = listChildResource.getPath().substring(
                                    prefixLength, listChildResource.getPath().indexOf(DOT)
                            );
                            componentContentModels.put(DigestUtils.md5Hex(listChildResource.getPath()), trimmedPath);
                        }
                    }
                }
            }
        }

        return componentContentModels;
    }
}
