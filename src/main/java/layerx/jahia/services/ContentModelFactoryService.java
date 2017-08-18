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

package layerx.jahia.services;

import layerx.api.ContentModel;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Content Model Factory Service Interface
 *
 * @author      neozilon
 * @version     1.0.0
 * @since       2017-04-16
 */
public interface ContentModelFactoryService {

    /**
     *
     * @param request This is a HttpServletRequest
     * @param response This is a HttpServletResponse
     * @return the Content Model of a resource
     */
    public ContentModel getContentModel(final HttpServletRequest request, final HttpServletResponse response,
                                        RenderContext renderContext, Resource resource);

}
