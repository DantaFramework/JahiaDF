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

import com.github.jknack.handlebars.Helper;

/**
 * The Helper Function interface
 *
 * @author      neozilon
 * @version     1.0.0
 * @since       2016-09-15
 */
interface HelperFunction<T> extends Helper<T> {

        public String name();
        public CharSequence execute(T valueObj)
                throws Exception;
}

