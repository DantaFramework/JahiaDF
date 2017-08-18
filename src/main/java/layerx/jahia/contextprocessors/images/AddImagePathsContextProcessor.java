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

package layerx.jahia.contextprocessors.images;

import com.google.common.collect.Sets;
import layerx.api.ExecutionContext;
import layerx.api.exceptions.ProcessException;
import layerx.jahia.templating.TemplateContentModel;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.jahia.services.render.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static layerx.Constants.*;
import static layerx.jahia.Constants.JAHIA_RESOURCE;

/**
 * The context processor for adding image paths to content model
 *
 * @author      jarriola
 * @version     1.0.0
 * @since       2017-08-11
 */
@Component
@Service
public class AddImagePathsContextProcessor extends AbstractImageContextProcessor<TemplateContentModel>  {

    @Override
    public Set<String> anyOf() {
        return Sets.newHashSet(IMAGES_BY_KEY_CATEGORY);
    }

    private static final Logger log = LoggerFactory.getLogger(AddImagePathsContextProcessor.class);

    @Override
    public void process(ExecutionContext executionContext, TemplateContentModel contentModel) throws ProcessException {
        Resource resource = (Resource) executionContext.get(JAHIA_RESOURCE);
        if (resource != null) {

            if (contentModel.has(RESOURCE_CONTENT_KEY)) {
                Object obj = contentModel.get(RESOURCE_CONTENT_KEY);

                if (obj instanceof Map) {
                    Map<String, Object> contentMap = (Map) obj;

                    Map<String, String> imagesMap = new HashMap<>();

                    for (Map.Entry<String, Object> entry : contentMap.entrySet()) {
                        Object propertyObj = entry.getValue();
                        if(propertyObj instanceof String) {
                            String propertyKey = (String) propertyObj;
                                if (checkImage(propertyKey)) {
                                    imagesMap.put(entry.getKey(), propertyKey);
                                }
                        } // Property might have been deleted, thus it holds a non-String object (e.g. Calendar object)
                    }

                    contentModel.set(CONTENT + DOT + IMAGE_PATHS_FROM_RESOURCES, imagesMap);
                }
            }
        }
    }
}
