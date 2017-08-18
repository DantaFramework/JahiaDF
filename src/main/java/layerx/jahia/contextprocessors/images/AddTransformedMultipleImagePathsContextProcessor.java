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
import layerx.jahia.templating.TemplateContentModel;
import layerx.api.exceptions.ProcessException;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.Resource;

import javax.jcr.PathNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static layerx.Constants.*;
import static layerx.jahia.Constants.JAHIA_RESOURCE;

/**
 * The context processor for adding tansformed multiple image paths to content model
 *
 * @author      jarriola
 * @version     1.0.0
 * @since       2017-08-11
 */
@Component
@Service
public class AddTransformedMultipleImagePathsContextProcessor
        extends AbstractImageContextProcessor<TemplateContentModel> {

    @Override
    public Set<String> anyOf() {
        return Sets.newHashSet(MULTIPLE_IMAGE_CATEGORY);
    }

    @Override
    public void process(final ExecutionContext executionContext, TemplateContentModel contentModel)
            throws ProcessException {
        try {
            Collection<String> imagePathList = new ArrayList<>();
            Resource resource = (Resource) executionContext.get(JAHIA_RESOURCE);
            JCRSessionWrapper jcrSessionWrapper = resource.getNode().getSession();

            Object obj = contentModel.get(RESOURCE_CONTENT_KEY);
            if (obj instanceof Map) {
                Map<String, Object> contentMap = (Map) obj;
                if (contentModel.has(RESOURCE_CONTENT_KEY)) {
                    for (Map.Entry<String, Object> entry : contentMap.entrySet()) {

                        Object propertyObj = entry.getValue();
                        if (propertyObj instanceof ArrayList) {
                            for (Object item : (ArrayList) propertyObj) {

                                if (item instanceof String) {
                                    String path = (String) item;
                                    try {
                                        JCRNodeWrapper image = jcrSessionWrapper.getNode(path);
                                        String url = image.getUrl();
                                        if (checkImage(path)) {
                                            imagePathList.add(url);
                                        }
                                    } catch (PathNotFoundException e) { } // String was not a valid path
                                }
                            }
                        }
                    }
                }
            }

            contentModel.set(CONTENT + DOT + IMAGE_PATHS, imagePathList);
        } catch (Exception e) {
            throw new ProcessException(e);
        }
    }
}
