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
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.jahia.services.render.Resource;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

import static layerx.Constants.*;
import static layerx.core.util.ObjectUtils.wrap;
import static layerx.jahia.Constants.HTTP_REQUEST;
import static layerx.jahia.Constants.JAHIA_RESOURCE;

/**
 * The context processor for adding component properties to content model
 *
 * @author      neozilon
 * @version     1.0.0
 * @since       2016-09-28
 */
@Component
@Service
public class AddComponentPropertiesContextProcessor extends AbstractCheckComponentCategoryContextProcessor<TemplateContentModel> {

    @Override
    public Set<String> anyOf() {
        return Sets.newHashSet(COMPONENT_CATEGORY);
    }

    @Override
    public int priority() {
        // This processor must be one of the first processors executed.
        return HIGHEST_PRIORITY;
    }

    @Override
    public void process(final ExecutionContext executionContext, TemplateContentModel contentModel)throws ProcessException {
        try {
            Resource resource = (Resource) executionContext.get(JAHIA_RESOURCE);
            if (resource != null) {
                Configuration configuration = configurationProvider.getFor(resource);
                contentModel.setAsIsolated(CONFIG_PROPERTIES_KEY, wrap(configuration.toMap()));

                // TODO add Component Properties
            }
        } catch (Exception e) {
            throw new ProcessException(e);
        }
    }
}
