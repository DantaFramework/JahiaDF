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

package layerx.jahia.templating.tagsupport;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.apache.taglibs.standard.tag.common.core.ParamParent;
import org.jahia.services.content.*;
import org.jahia.services.render.*;

import javax.jcr.*;
import javax.jcr.nodetype.ConstraintViolationException;
import java.io.IOException;

/**
 * This is the implementation of the JSP tag: <template:list /> adapted to LayerX.
 *
 * @author      jarriola
 * @version     1.0.0
 * @since       2017-08-11
 */
public class ListTag extends ModuleTag implements ParamParent {

    private static final long serialVersionUID = -3608856316200861402L;

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(ListTag.class);

    private String listType = "jnt:contentList";

    public void setListType(String listType) {
        this.listType = listType;
    }

    @Override
    protected String getModuleType(RenderContext renderContext) throws RepositoryException {
        return "area";
    }

    @Override
    protected void missingResource(RenderContext renderContext, Resource currentResource) throws RepositoryException, IOException {
        try {
            if (renderContext.isEditMode()) {
                JCRSessionWrapper session = currentResource.getNode().getSession();
                if (!path.startsWith("/")) {
                    JCRNodeWrapper nodeWrapper = currentResource.getNode();
                    if(!nodeWrapper.isCheckedOut())
                        nodeWrapper.checkout();
                    node = nodeWrapper.addNode(path, listType);
                    session.save();
                } else {

                    JCRNodeWrapper parent = session.getNode(StringUtils.substringBeforeLast(path, "/"));
                    if(!parent.isCheckedOut())
                        parent.checkout();
                    node = parent.addNode(StringUtils.substringAfterLast(path, "/"), listType);
                    session.save();
                }
            }
        } catch (ConstraintViolationException e) {
            super.missingResource(renderContext, currentResource);
        } catch (RepositoryException e) {
            logger.error("Cannot create area",e);
        }
    }
}