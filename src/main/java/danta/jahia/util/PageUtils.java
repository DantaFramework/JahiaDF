/**
 * Danta AEM Bundle
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

import org.jahia.services.content.JCRNodeWrapper;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.List;

import static danta.jahia.Constants.JAHIA_VANITY_URL_MAPPING;

/**
 * Page Utility class, contained generic methods for handling a JahiaDF Page.
 *
 * @author      Danta Team
 * @version     1.0.0
 * @since       2018-01-17
 */
public class PageUtils {

    /**
     * Returns the vanity URL mappings stored under the page node.
     *
     * @param mainNode The page node
     * @return vanityPaths The vanityURLs either as a string or list.
     * @throws RepositoryException
     */
    public static Object getVanityURLs(JCRNodeWrapper mainNode) throws RepositoryException {
        List vanityPaths = new ArrayList();
        JCRNodeWrapper vanityUrls = mainNode.getNode(JAHIA_VANITY_URL_MAPPING);
        if(vanityUrls != null) {
            for (Node vanityUrl : vanityUrls.getNodes()) {
                vanityPaths.add(vanityUrl.getName());
            }
        }
        if(vanityPaths.size() > 0 ) {
            if (vanityPaths.size() == 1) {

                return vanityPaths.get(0);
            } else {

                return vanityPaths;
            }
        }

        return null;
    }
}
