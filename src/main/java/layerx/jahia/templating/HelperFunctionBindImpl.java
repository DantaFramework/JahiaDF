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

package layerx.jahia.templating;

import org.apache.felix.scr.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;

import java.util.ArrayList;
import java.util.List;

/**
 * The Helper Function Binder implementer
 *
 * @author      neozilon
 * @version     1.0.0
 * @since       2016-12-16
 */
@Component
@Service
public class HelperFunctionBindImpl implements HelperFunctionBind {

    protected BundleContext bundleContextContext;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, bind = "bindHelperFunction", unbind = "unbindHelperFunction", referenceInterface = HelperFunction.class, policy = ReferencePolicy.DYNAMIC)
    private List<HelperFunction> helpers = new ArrayList<>();

    public List<HelperFunction> getHelpers()
            throws Exception {
        return helpers;
    }

    @Activate
    protected void activate(BundleContext bundleContext)
            throws Exception {
        this.bundleContextContext = bundleContext;
    }

    private void bindHelperFunction(HelperFunction helper) {
        helpers.add(helper);
    }

    private void unbindHelperFunction(HelperFunction helper) {
        helpers.remove(helper);
    }

}
