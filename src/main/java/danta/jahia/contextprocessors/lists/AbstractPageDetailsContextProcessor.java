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

package danta.jahia.contextprocessors.lists;

import danta.api.ExecutionContext;
import danta.api.configuration.Configuration;
import danta.jahia.templating.TemplateContentModel;
import danta.jahia.util.ResourceUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.Resource;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static danta.Constants.*;
import static danta.jahia.Constants.JAHIA_RESOURCE;
import static danta.jahia.Constants.JCR_DESCRIPTION;
import static danta.jahia.Constants.JCR_CREATED;
import static danta.jahia.Constants.JCR_TITLE;

/**
 * The abstraction for extracting page properties
 *
 * @author      josecarlos
 * @version     1.0.0
 * @since       2017-05-26
 */
@Component(componentAbstract = true)
@Service
public abstract class AbstractPageDetailsContextProcessor extends
        AbstractItemListContextProcessor<TemplateContentModel> {

    protected Map<String, Object> extractBasicPageDetails(final ExecutionContext executionContext, JCRNodeWrapper page, String currentPage)
            throws Exception{
        Map<String, Object> pageDetails = new HashMap<>();
        if(null != page) {
            pageDetails.put(TITLE, page.getDisplayableName());
            pageDetails.put(NAME, page.getName());
            pageDetails.put(PATH, page.getPath());
            pageDetails.put(LINK, page.getUrl());

            pageDetails.put(DESCRIPTION, ResourceUtils.getProperty(page, JCR_DESCRIPTION, ""));
            pageDetails.put(CREATED, ResourceUtils.getProperty(page, JCR_CREATED, ""));
            pageDetails.put(PAGE_TITLE,ResourceUtils.getProperty(page, JCR_TITLE, ""));

            // This properties are not yet supported by Jahia UI for page properties
            pageDetails.put(SUBTITLE, ResourceUtils.getProperty(page, "", ""));
            pageDetails.put(NAVIGATION_TITLE, ResourceUtils.getProperty(page, "", ""));
            pageDetails.put(VANITY_PATH, ResourceUtils.getProperty(page, "", ""));

            if (currentPage.equals(page.getPath())) {
                pageDetails.put(IS_CURRENT_PAGE, true);
            }

            Resource resource = (Resource) executionContext.get(JAHIA_RESOURCE);
            Configuration configuration = configurationProvider.getFor(resource);
            //Extra properties
            Collection<String> extraPropertyNames = configuration.asStrings(EXTRA_LIST_PROPERTIES_CONFIG_KEY);
            for (String extraPropertyName : extraPropertyNames) {
                pageDetails.put(extraPropertyName, ResourceUtils.getProperty(page, extraPropertyName, ""));
            }
        }

        return pageDetails;
    }

}
