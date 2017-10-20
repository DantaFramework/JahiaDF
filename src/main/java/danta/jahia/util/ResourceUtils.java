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

package danta.jahia.util;

import org.apache.commons.lang3.StringUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import java.util.Map;

/**
 * Resource utility class
 *
 * @author      josecarlos
 * @version     1.0.0
 * @since       2017-05-30
 */
public class ResourceUtils {

    protected static final Logger log = LoggerFactory.getLogger(ResourceUtils.class);

    /**
     * Forbid instantiation
     */
    private ResourceUtils() {}

    /**
     * Get property value using node and property name
     *
     * @param node The node to get the property from
     * @param property The property name
     * @param defaultValue The default value if property doesn't exist
     * @return propertyValue
     * @throws RepositoryException
     */
    public static String getProperty(JCRNodeWrapper node, String property, String defaultValue)
            throws RepositoryException {
        Map<String, String> properties = node.getPropertiesAsString();
        if(properties.containsKey(property)) {
            return properties.get(property);
        }

        return defaultValue;
    }

    /**
     * Get resource node path by session and uuid
     *
     * @param session The session used to get resource node path
     * @param resourceNodeUUID The uuid used to get resource node path
     * @return resourceNodePath
     * @throws RepositoryException
     */
    public static String getResourceNodePath(final Session session,
                                             final String resourceNodeUUID,
                                             final RenderContext renderContext,
                                             final Resource resource)
            throws RepositoryException {
        String resourcePath = "";
        if (StringUtils.isNotEmpty(resourceNodeUUID)) {

            Node n = session.getNodeByIdentifier(resourceNodeUUID);
            try {
                URLGenerator urlGenerator = new URLGenerator(renderContext, resource);
                String base = urlGenerator.getBase();
                String nodeType = n.getPrimaryNodeType().getName();
                if(nodeType.equals("jnt:page")) {
                    resourcePath = base + n.getPath() + ".html";
                } else {
                    resourcePath = base + n.getPath();
                }

            } catch (Exception e){
                log.error("Error casting Node: " + n + " to JCRNodeWrapper, using path only", e);
                resourcePath = n.getPath();
            }
        }

        return resourcePath;
    }

    /**
     * Get path from url
     *
     * @param url The url used to get path from
     * @param renderContext The RenderContext used to render resource to obtain path
     * @return path
     */
    public static String getPathFromURL(String url, RenderContext renderContext) {
        String path = "";
        URLResolverFactory urlResolverFactory = new URLResolverFactory();
        URLResolver urlResolver = urlResolverFactory.createURLResolver(url, renderContext);
        if(urlResolver != null) {
            path = urlResolver.getPath();
            path = path.replace(".html", "");
        }
        return path;
    }
}