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

package layerx.jahia.contextprocessors.lists;

import org.jahia.services.render.Resource;

import layerx.api.ExecutionContext;
import layerx.jahia.templating.TemplateContentModel;
import layerx.api.configuration.Configuration;
import layerx.api.configuration.Mode;
import layerx.api.exceptions.ProcessException;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import java.util.Collection;

import static layerx.Constants.*;
import static layerx.jahia.Constants.*;

/**
 * This Context Processor adds to the content model a list of item list contained in the configuration.
 *
 * @author      LayerX Team
 * @version     1.0.0
 * @since       2017-08-11
 */
@Component
@Service
public class AddItemListConfigsContextProcessor
        extends AbstractItemListContextProcessor<TemplateContentModel> {

    protected static final int PRIORITY = AddItemListContextProcessor.PRIORITY - 10;

    @Override
    public int priority() {
        return PRIORITY;
    }

    @Override
    public void process(final ExecutionContext executionContext, TemplateContentModel contentModel)
            throws ProcessException {
        try {
            Resource resource = (Resource) executionContext.get(JAHIA_RESOURCE);
            Configuration configuration = configurationProvider.getFor(resource);

            Collection<String> listClasses = configuration.asStrings(LIST_CLASSES_CONFIG_PROP, Mode.MERGE);
            contentModel.set(LIST_PROPERTIES_KEY + "." + LIST_CLASSES_PROP, listClasses);
            Collection<String> itemClasses = configuration.asStrings(LIST_ITEM_CLASSES_CONFIG_PROP, Mode.MERGE);
            contentModel.set(LIST_PROPERTIES_KEY + "." + LIST_ITEM_CLASSES_PROP, itemClasses);

        } catch (Exception e) {
            throw new ProcessException(e);
        }
    }
}