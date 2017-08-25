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

package danta.jahia.contextprocessors.webservices;

import com.google.common.collect.Sets;
import danta.api.ExecutionContext;
import danta.api.configuration.Configuration;
import danta.api.configuration.Mode;
import danta.api.exceptions.ProcessException;
import danta.core.contextprocessors.AbstractCheckComponentCategoryContextProcessor;
import danta.jahia.templating.TemplateContentModel;
import org.apache.commons.codec.digest.DigestUtils;
import org.jahia.services.render.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static danta.Constants.*;
import static danta.core.Constants.*;
import static danta.jahia.Constants.*;

/**
 * This Context Processor Generates Client Component from Content Model
 *
 * @author      neozilon
 * @version     1.0.0
 * @since       2017-05-25
 */
public class GenerateClientComponentContentModelContextProcessor extends AbstractCheckComponentCategoryContextProcessor<TemplateContentModel> {

    private static Logger LOG = LoggerFactory.getLogger(GenerateClientComponentContentModelContextProcessor.class);

    @Override
    public Set<String> anyOf() {
        return Sets.newHashSet(COMPONENT_CATEGORY);
    }

    @Override
    public int priority() {
        return LOW_PRIORITY + 100 + 5;
    } // TODO FIX to use Page properties Context Processor

    @Override
    public void process(final ExecutionContext executionContext, final TemplateContentModel contentModel)throws ProcessException {
        try {
            Resource resource = (Resource) executionContext.get(JAHIA_RESOURCE);
            HttpServletRequest request = (HttpServletRequest) executionContext.get(HTTP_REQUEST);

            if (resource != null && configurationProvider != null) {

                Configuration configuration = configurationProvider.getFor(resource);
                Map<String, Object> config = contentModel.getAs(CONFIG_PROPERTIES_KEY, Map.class);
                Map<String, Object> filteredContentMap = new TreeMap<>();
                String componentContentId = DigestUtils.md5Hex(resource.getPath());

                boolean clientAccessible = Boolean.valueOf(config.get(XK_CLIENT_ACCESSIBLE_CP) + BLANK);
                if (clientAccessible) {
                    filteredContentMap.put(XK_CONTENT_ID_CP, componentContentId);
                    Collection<String> props = configuration.asStrings(XK_CLIENT_MODEL_PROPERTIES_CP, Mode.MERGE);
                    for (String propName : props) {
                        Object value = contentModel.get(propName);
                        if (value != null) {
                            filteredContentMap.put(propName, value);
                        }
                    }
                }
                //request.setAttribute(CLIENT_COMPONENT_CONTENT_MODEL_REQ_AN, filteredContentMap);
            }
        } catch (Exception e) {
            LOG.error(LOG_PRE + " Error : ",e);
            throw new ProcessException(e);
        }
    }
}
