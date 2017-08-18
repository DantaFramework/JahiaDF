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

import layerx.api.ContextProcessorEngine;

import layerx.jahia.templating.TemplateContentModel;
import layerx.api.configuration.Configuration;
import layerx.api.configuration.ConfigurationProvider;
import layerx.api.configuration.Mode;
import layerx.jahia.services.ContentModelFactoryService;

import net.minidev.json.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Map;
import java.util.Collection;
import java.util.List;

import static layerx.Constants.BLANK;
import static layerx.Constants.CONFIG_PROPERTIES_KEY;
import static layerx.core.Constants.XK_CLIENT_ACCESSIBLE_CP;
import static layerx.core.Constants.XK_CLIENT_MODEL_PROPERTIES_CP;
import static layerx.core.Constants.XK_CONTENT_ID_CP;

import static layerx.jahia.Constants.SERVER_RESPONSE_CONTENT_TYPE;

/**
 * This class takes component's content model and turns into action result
 *
 * @author      josecarlos
 * @version     1.0.0
 * @since       2017-06-29
 */
public class ComponentContentModelToJSONActionServlet extends Action{

    private static Logger LOG = LoggerFactory.getLogger(ComponentContentModelToJSONActionServlet.class);

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
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource,
                                  JCRSessionWrapper session, Map<String, List<String>> parameters,
                                  URLResolver urlResolver) throws Exception {
        JSONObject jsonObject = new JSONObject();
        final HttpServletResponse response = renderContext.getResponse();

        try {
            TemplateContentModel contentModel = (TemplateContentModel) contentModelFactoryService.getContentModel(
                    req, response, renderContext, resource);
            Configuration configuration = configurationProvider.getFor(resource);
            Map<String, Object> config = contentModel.getAs(CONFIG_PROPERTIES_KEY, Map.class);
            boolean clientAccessible = Boolean.valueOf(config.get(XK_CLIENT_ACCESSIBLE_CP) + BLANK);
            if (clientAccessible) {
                Collection<String> props = configuration.asStrings(XK_CLIENT_MODEL_PROPERTIES_CP, Mode.MERGE);
                String[] contexts = props.toArray(new String[props.size()]);

                // Get content model json with the XK_CLIENT_MODEL_PROPERTIES_CP contexts
                jsonObject = contentModel.toJSONObject(contexts);

                // Add component id
                String componentContentId = DigestUtils.md5Hex(resource.getPath());
                jsonObject.put(XK_CONTENT_ID_CP, componentContentId);
            }
        } catch (Exception ew) {
                throw new ServletException(ew);
        }

        response.setContentType(SERVER_RESPONSE_CONTENT_TYPE);
        response.getWriter().write( jsonObject.toString() );

        return ActionResult.OK;
    }
}
