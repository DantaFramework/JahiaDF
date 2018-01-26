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

package danta.jahia.servlets;

import danta.api.ContextProcessorEngine;
import danta.api.TemplateContentModel;
import danta.api.configuration.ConfigurationProvider;
import danta.core.execution.ExecutionContextImpl;
import danta.jahia.services.ContentModelFactoryService;
import danta.jahia.util.JahiaUtils;
import net.minidev.json.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

import static danta.Constants.*;
import static danta.jahia.Constants.*;

/**
 * This servlet returns the executing CPs for a given resource
 *
 * @author      jarriola
 * @version     1.0.0
 * @since       2018-01-18
 */
public class StatisticsServlet extends Action {

    private static Logger LOG = LoggerFactory.getLogger(StatisticsServlet.class);

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

        JSONArray CPsList = new JSONArray();
        final HttpServletResponse response = renderContext.getResponse();

        try {
            View view = null;
            TemplateContentModel contentModel;
            boolean checkInTemplateNode = false;

            // Resource is a page
            if ( resource.getNode() != null && resource.getNode().getPrimaryNodeType() != null &&
                    StringUtils.startsWithAny(resource.getNode().getPrimaryNodeType().getName(),
                            LIST_TEMPLATE_OPTION_TYPES) ){
                view = JahiaUtils.resolveTemplateResourceView(request, resource, renderContext);
                // Set view in the request since the view is needed by the contentModelFactoryService
                request.setAttribute(JAHIA_SCRIPT_VIEW, view);

                // Reassign the current resource to the template's resource
                resource = JahiaUtils.resolveTemplateResource(resource, renderContext);

                checkInTemplateNode = true;
            }

            contentModel = (TemplateContentModel) contentModelFactoryService.getContentModel(
                    request, response, renderContext, resource);

            ExecutionContextImpl executionContext = new ExecutionContextImpl();
            executionContext.put(HTTP_REQUEST, request);
            executionContext.put(JAHIA_RESOURCE, resource);
            executionContext.put(JAHIA_RENDER_CONTEXT, renderContext);
            executionContext.put(ENGINE_RESOURCE, resource);
            if (checkInTemplateNode){
                executionContext.put(JAHIA_SCRIPT_VIEW, view);
                executionContext.put(ENGINE_RESOURCE, view);
            }

            List<String> currentProcessorChain = contextProcessorEngine.execute(executionContext, contentModel);

            for (String CP : currentProcessorChain) {
                CPsList.add(CP);
            }

        } catch (Exception ew) {

            throw new ServletException(ew);
        }

        response.setContentType(SERVER_RESPONSE_CONTENT_TYPE);
        response.getWriter().write( CPsList.toString() );

        return ActionResult.OK;
    }
}
