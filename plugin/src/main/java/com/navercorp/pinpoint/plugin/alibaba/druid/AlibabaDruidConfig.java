/*
 * Copyright 2016 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.alibaba.druid;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * @author Taejin Koo
 */
public class AlibabaDruidConfig {

    static final String DRUID_PLUGIN_ENABLE = "profiler.jdbc.druid";
    static final String DRUID_PROFILE_CONNECTIONCLOSE_ENABLE = "profiler.jdbc.druid.connectionclose";

    private final boolean pluginEnable;
    private final boolean profileClose;

    public AlibabaDruidConfig(ProfilerConfig config) {
        pluginEnable = config.readBoolean(DRUID_PLUGIN_ENABLE, false);
        profileClose = config.readBoolean(DRUID_PROFILE_CONNECTIONCLOSE_ENABLE, false);
    }

    public boolean isPluginEnable() {
        return pluginEnable;
    }

    public boolean isProfileClose() {
        return profileClose;
    }

}
