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

import com.google.common.collect.Sets;
import danta.api.ExecutionContext;
import danta.api.TemplateContentModel;
import danta.api.exceptions.ProcessException;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.Resource;

import java.util.*;

import static danta.Constants.*;
import static danta.jahia.Constants.JAHIA_RESOURCE;

/**
 * This Context Processor adds to the content model a list of page paths in 'list.pageRefs' and
 * add their page details in 'list.pages'.
 *
 * @author      Danta Team
 * @version     1.0.0
 * @since       2017-08-11
 */
@Component
@Service
public class AddPageDetailsContextProcessor
        extends AbstractPageDetailsContextProcessor {

    protected static final int PRIORITY = AddCuratedPageReferencesContextProcessor.PRIORITY - 20;

    @Override
    public Set<String> allOf() {
        return Sets.newHashSet(PAGE_DETAILS_CATEGORY);
    }

    @Override
    public Set<String> anyOf() {
        return Sets.newHashSet(CURATED_LIST_CATEGORY, TRAVERSED_LIST_CATEGORY);
    }

    @Override
    public int priority() {
        return PRIORITY;
    }

    @Override
    public void process(final ExecutionContext executionContext, TemplateContentModel contentModel)
            throws ProcessException {
        try {
            Resource resource = (Resource) executionContext.get(JAHIA_RESOURCE);
            JCRSessionWrapper jcrSessionWrapper = resource.getNode().getSession();

            if (contentModel.has(LIST_PROPERTIES_KEY + DOT + PAGEREFS_CONTENT_KEY_NAME)) {
                Collection<Map<String,Object>> pathList = contentModel.getAs(LIST_PROPERTIES_KEY + DOT + PAGEREFS_CONTENT_KEY_NAME, Collection.class);
                List<Map<String, Object>> allPageDetailList = new ArrayList<>();
                String currentPage = contentModel.getAsString(PAGE + DOT + PATH);
                for (Map<String,Object> pathInfo: pathList) {
                    allPageDetailList.add(extractPageDetails(executionContext, jcrSessionWrapper, pathInfo, currentPage));
                }
                contentModel.set(PAGE_DETAILS_LIST_CONTEXT_PROPERTY_NAME, allPageDetailList);
            }
        } catch (Exception e) {
            throw new ProcessException(e);
        }
    }

    public Map<String, Object> extractPageDetails(final ExecutionContext executionContext, JCRSessionWrapper jcrSessionWrapper, Map<String, Object> pathInfo, String currentPage) throws Exception{
        Map<String, Object> pageDetails = new HashMap<>();
        String path = pathInfo.get(PATH_DETAILS_LIST_PATH_PROPERTY_NAME).toString();
        JCRNodeWrapper page = jcrSessionWrapper.getNode(path);

        if (null != page){
            pageDetails = extractBasicPageDetails(executionContext, page, currentPage);

            Collection<Map<String, Object>> paths = (Collection<Map<String,Object>>)pathInfo.get(PATH_DETAILS_LIST_PATHS_PROPERTY_NAME);
            if(paths != null) {
                Collection<Map<String, Object>> pageChildrenDetails = new ArrayList<>();
                for (Map<String, Object> childPathInfo : paths) {
                    pageChildrenDetails.add(extractPageDetails(executionContext, jcrSessionWrapper, childPathInfo, currentPage));
                }
                pageDetails.put(PAGE_LIST_CONTEXT_PROPERTY_NAME,pageChildrenDetails);
            }
        }
        return pageDetails;
    }

}