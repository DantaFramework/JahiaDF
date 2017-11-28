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

package danta.jahia.templating;

import danta.core.templating.AbstractTemplateContentModelImpl;
import net.minidev.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import static danta.jahia.Constants.HTTP_REQUEST;

/**
 * Template Content Model
 *
 * @author      Danta Team
 * @version     1.0.0
 * @since       2016-10-06
 */
public class TemplateContentModelImpl
        extends AbstractTemplateContentModelImpl {

    private final HttpServletRequest request;
    private final HttpServletResponse response;

    public TemplateContentModelImpl(final HttpServletRequest request, final HttpServletResponse response) {
        this(request, response, new JSONObject());
    }

    public TemplateContentModelImpl(final HttpServletRequest request, final HttpServletResponse response, final Map<String, Object> initialModelData) {
        super(initialModelData);
        getRootContext().data(HTTP_REQUEST, request);

        this.request = request;
        this.response = response;
    }

    public HttpServletRequest request()
            throws Exception {
        return (has(HTTP_REQUEST)) ? getAs(HTTP_REQUEST, HttpServletRequest.class) : null;
    }
}