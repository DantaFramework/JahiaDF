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

package layerx.jahia.services.impl;

import layerx.api.ContentModel;
import layerx.api.ContextProcessorEngine;
import layerx.core.execution.ExecutionContextImpl;
import layerx.jahia.services.ContentModelFactoryService;
import layerx.jahia.templating.TemplateContentModel;
import org.apache.felix.scr.annotations.*;
import org.jahia.services.render.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static layerx.Constants.ENGINE_RESOURCE;
import static layerx.jahia.Constants.*;

/**
 * Content Model Factory Service Implementer
 *
 * @author      neozilon
 * @version     1.0.0
 * @since       2017-04-16
 */
@Component
@Service(ContentModelFactoryService.class)
public class ContentModelFactoryServiceImpl implements ContentModelFactoryService {

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY, policy = ReferencePolicy.STATIC)
    protected ContextProcessorEngine contextProcessorEngine;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * This method gets the Content Model of a resource.
     *
     * @param request
     * @param response
     * @return the Content Model of a resource
     */
    @Override
    public ContentModel getContentModel (final HttpServletRequest request, final HttpServletResponse response,
                                         RenderContext renderContext, Resource resource) {
        final ContentModel contentModel = new TemplateContentModel(request, response);
        try {
            View view = (View) request.getAttribute(JAHIA_SCRIPT_VIEW);

            if (resource != null) {
                ExecutionContextImpl executionContext = new ExecutionContextImpl();
                executionContext.put(HTTP_REQUEST, request);
                executionContext.put(JAHIA_RESOURCE, resource);
                executionContext.put(JAHIA_RENDER_CONTEXT, renderContext);
                executionContext.put(ENGINE_RESOURCE, resource);
                if (view != null){
                    executionContext.put(JAHIA_SCRIPT_VIEW, view);
                    executionContext.put(ENGINE_RESOURCE, view);
                }

                contextProcessorEngine.execute(executionContext, contentModel);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return contentModel;
    }
}
