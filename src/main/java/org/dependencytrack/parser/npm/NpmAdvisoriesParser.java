/*
 * This file is part of Dependency-Track.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) Steve Springett. All Rights Reserved.
 */
package org.dependencytrack.parser.npm;

import alpine.logging.Logger;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.dependencytrack.parser.npm.model.Advisory;
import org.dependencytrack.parser.npm.model.AdvisoryResults;

/**
 * Parser for NPM Advisory objects returned from the NPM Advisory API.
 *
 * @author Steve Springett
 * @since 3.2.1
 */
public class NpmAdvisoriesParser extends BaseAdvisoryParser {

    private static final Logger LOGGER = Logger.getLogger(NpmAdvisoriesParser.class);

    /**
     * Parses the JSON response from the NPM Advisory API.
     *
     * @param jsonNode the JSON node to parse
     * @return an AdvisoryResults object
     */
    public AdvisoryResults parse(final JsonNode jsonNode) {
        LOGGER.debug("Parsing JSON node");

        final AdvisoryResults advisoryResults = new AdvisoryResults();
        final JSONObject root = jsonNode.getObject();
        advisoryResults.setTotal(root.getInt("total"));

        final JSONObject urls = root.optJSONObject("urls");
        if (urls != null) {
            advisoryResults.setNext(urls.optString("next", null));
            advisoryResults.setPrevious(urls.optString("prev", null));
        }
        final JSONArray advisories = root.getJSONArray("objects");

        if (advisories != null) {
            for (int i = 0; i < advisories.length(); i++) {
                final Advisory advisory = parse(advisories.getJSONObject(i));
                advisoryResults.add(advisory);
            }
        }
        return advisoryResults;
    }
}
