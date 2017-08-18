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

import com.google.common.collect.Sets;
import layerx.api.ExecutionContext;
import layerx.api.configuration.Configuration;
import layerx.api.exceptions.ProcessException;
import layerx.jahia.templating.TemplateContentModel;
import layerx.jahia.util.ResourceUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.jahia.services.content.JCRNodeIteratorWrapper;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

import javax.jcr.ItemNotFoundException;
import java.util.*;

import static layerx.Constants.*;
import static layerx.jahia.Constants.JAHIA_RENDER_CONTEXT;
import static layerx.jahia.Constants.JAHIA_RESOURCE;

/**
 * This Context Processor adds to the content model a list of page paths in 'list.pageRefs'
 *
 * @author      josecarlos
 * @version     1.0.0
 * @since       2017-05-25
 */
@Component
@Service
public class AddTraversedPageReferencesContextProcessor
        extends AbstractItemListContextProcessor<TemplateContentModel> {

    protected static final int PRIORITY = AddItemListContextProcessor.PRIORITY - 20;

    @Override
    public Set<String> allOf() {
        return Sets.newHashSet(LIST_CATEGORY, TRAVERSED_LIST_CATEGORY);
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
            RenderContext renderContext = (RenderContext) executionContext.get(JAHIA_RENDER_CONTEXT);
            JCRSessionWrapper jcrSessionWrapper = resource.getNode().getSession();

            if(resource != null) {
                String pathRefListContentKeyName = getPathRefListKeyName(resource);

                if (contentModel.has(pathRefListContentKeyName)) {
                    String pathRef = contentModel.getAsString(pathRefListContentKeyName);

                    Collection<Map<String, Object>> pathList = new ArrayList<>();
                    if (pathRef != null) {
                        int depth = LIST_DEFAULT_DEPTH;
                        String depthListContentKeyName = getDepthKeyName(resource);
                        if (contentModel.has(depthListContentKeyName)) {
                            depth =  Integer.parseInt(contentModel.getAsString(depthListContentKeyName));
                        }

                        JCRNodeWrapper pageNode = null;
                        try {
                            String path = ResourceUtils.getPathFromURL(pathRef, renderContext);
                            pageNode = jcrSessionWrapper.getNode(path);
                        } catch (ItemNotFoundException e) {
                        }
                        if (null != pageNode) {
                            String currentPage = contentModel.getAsString(PAGE + DOT + PATH);
                            boolean removeCurrentPage = false;
                            if (contentModel.has(REMOVE_CURRENT_PAGE_PATH_CONFIG_KEY) &&
                                    contentModel.getAsString(REMOVE_CURRENT_PAGE_PATH_CONFIG_KEY).equals(TRUE)){
                                removeCurrentPage = true;
                            }
                            pathList = extractPathList(pageNode, depth, currentPage, removeCurrentPage);
                        }
                    }
                    contentModel.set(LIST_PROPERTIES_KEY + DOT + PAGEREFS_CONTENT_KEY_NAME, pathList);
                }
            }
        } catch (Exception e) {
            throw new ProcessException(e);
        }
    }

    /**
     * Looks for the key name under config. If not found, gets the default one
     * @param resource the request resource
     * @return the name of the key where the traversed list base path is stored
     * @throws Exception
     */
    protected String getPathRefListKeyName(Resource resource) throws Exception {
        Configuration configuration = configurationProvider.getFor(resource);
        String configurationPathRefListKeyName = configuration.asString(PATHREF_CONFIGURATION_PROPERTY_NAME);

        return (StringUtils.isNotEmpty(configurationPathRefListKeyName) ?
                configurationPathRefListKeyName : PATHREF_LIST_CONTENT_KEY);
    }

    /**
     * Looks for the depth under config. If not found, gets the default one
     * @param resource the request resource
     * @return the depth of the traversed tree
     * @throws Exception
     */
    protected String getDepthKeyName(Resource resource) throws Exception {
        Configuration configuration = configurationProvider.getFor(resource);
        String configurationDepthKeyName = configuration.asString(DEPTH_CONFIGURATION_PROPERTY_NAME);

        return (StringUtils.isNotEmpty(configurationDepthKeyName)) ?
                configurationDepthKeyName : DEPTH_LIST_CONTENT_KEY;
    }

    public static Collection<Map<String, Object>> extractPathList
            (JCRNodeWrapper pageNode, int depth, String currentPage, boolean removeCurrentPage) throws Exception {

        Collection<Map<String, Object>> pathList = new ArrayList<>();

        JCRNodeIteratorWrapper children = pageNode.getNodes();
        if (depth > 0) {
            while (children.hasNext()) {
                JCRNodeWrapper child = (JCRNodeWrapper) children.nextNode();
                if(child.getPrimaryNodeType().toString().equals("jnt:page")) {
                    Map<String, Object> currentPath = new HashMap<>();
                    Collection<Map<String, Object>> childPaths = extractPathList(child, depth - 1, currentPage);

                    String path = child.getPath();
                    String link = child.getUrl();

                    if (!path.equals(currentPage)) {
                        currentPath.put(PATH_DETAILS_LIST_PATH_PROPERTY_NAME, path);
                        currentPath.put(LINK, link);
                        currentPath.put(PATH_DETAILS_LIST_PATHS_PROPERTY_NAME, childPaths);
                        pathList.add(currentPath);

                    } else if (!removeCurrentPage) {
                        currentPath.put(PATH_DETAILS_LIST_PATH_PROPERTY_NAME, path);
                        currentPath.put(LINK, link);
                        currentPath.put(PATH_DETAILS_LIST_PATHS_PROPERTY_NAME, childPaths);
                        currentPath.put(IS_CURRENT_PAGE, true);
                        pathList.add(currentPath);
                    }
                }
            }
        }
        return pathList;
    }

    public static Collection<Map<String, Object>> extractPathList
            (JCRNodeWrapper pageNode, int depth, String currentPage) throws Exception {

        Collection<Map<String, Object>> pathList = new ArrayList<>();
        JCRNodeIteratorWrapper children = pageNode.getNodes();
        if (depth > 0) {
            while (children.hasNext()) {
                JCRNodeWrapper child = (JCRNodeWrapper) children.nextNode();
                if(child.getPrimaryNodeType().toString().equals("jnt:page")) {
                    Map<String, Object> currentPath = new HashMap<>();
                    Collection<Map<String, Object>> childPaths = extractPathList(child, depth - 1, currentPage);

                    String path = child.getPath();
                    String link = child.getUrl();
                    currentPath.put(PATH_DETAILS_LIST_PATH_PROPERTY_NAME, path);
                    currentPath.put(LINK, link);
                    currentPath.put(PATH_DETAILS_LIST_PATHS_PROPERTY_NAME, childPaths);
                    if (path.equals(currentPage)) {
                        currentPath.put(IS_CURRENT_PAGE, true);
                    }
                    pathList.add(currentPath);
                }
            }
        }
        return pathList;
    }
}