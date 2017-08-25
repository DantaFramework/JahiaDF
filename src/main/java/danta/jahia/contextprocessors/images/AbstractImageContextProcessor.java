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

package danta.jahia.contextprocessors.images;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import danta.api.ContentModel;
import danta.core.contextprocessors.AbstractCheckComponentCategoryContextProcessor;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

import java.util.Set;

import static danta.Constants.DOT;
import static danta.Constants.IMAGE_CATEGORY;

/**
 * The abstraction for Image Context Processor
 *
 * @author      jarriola
 * @version     1.0.0
 * @since       2017-08-11
 */
@Component(componentAbstract = true)
@Service
public abstract class AbstractImageContextProcessor<C extends ContentModel>
        extends AbstractCheckComponentCategoryContextProcessor<C>
        implements ImageContextProcessor<C> {

    @Override
    public Set<String> anyOf() {
        return Sets.newHashSet(IMAGE_CATEGORY);
    }

    // TODO: Make this list configurable via osgi
    static final ImmutableList<String> IMAGE_EXTENSIONS = ImmutableList.of("png", "jpg", "jpeg", "gif");

    static boolean checkImage(String resource) {
        //TODO: Find a more robust way to validate references to images other than the extension
        if (resource.contains(DOT) &&
                IMAGE_EXTENSIONS.contains(resource.substring(resource.lastIndexOf(DOT) + 1))) {
            return true;
        }

        return false;
    }
}
