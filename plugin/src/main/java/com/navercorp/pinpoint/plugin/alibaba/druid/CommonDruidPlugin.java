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

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.util.InstrumentUtils;

import java.security.ProtectionDomain;

public class CommonDruidPlugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private TransformTemplate transformTemplate;
    private CommonsDruidConfig config;

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        config = new CommonsDruidConfig(context.getConfig());
        if (!config.isPluginEnable()) {
            logger.info("Disable commons dbcp option. 'profiler.jdbc.dbcp2=false'");
            return;
        }
        addBasicDataSourceTransformer();
//        if (config.isProfileClose()) {
//            addPoolGuardConnectionWrapperTransformer();
//        }
    }

//    private void addPoolGuardConnectionWrapperTransformer() {
//        transformTemplate.transform("com.alibaba.druid.pool.DruidPooledConnection", new TransformCallback() {
//
//            @Override
//            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
//            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
//            // closeMethod
//            InstrumentMethod closeMethod = InstrumentUtils.findMethod(target, "close");
//            closeMethod.addScopedInterceptor(AlibabaDruidConstants.INTERCEPTOR_CLOSE_CONNECTION, AlibabaDruidConstants.SCOPE);
//
//            return target.toBytecode();
//            }
//        });
//    }

    private void addBasicDataSourceTransformer() {
        transformTemplate.transform("com.alibaba.druid.pool.DruidDataSource", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            if (isAvailableDataSourceMonitor(target)) {
                target.addField(AlibabaDruidConstants.ACCESSOR_DATASOURCE_MONITOR);

                // default constructor
                InstrumentMethod defaultConstructor = InstrumentUtils.findConstructor(target);
                defaultConstructor.addScopedInterceptor(AlibabaDruidConstants.INTERCEPTOR_CONSTRUCTOR, AlibabaDruidConstants.SCOPE);

                // closeMethod
                InstrumentMethod closeMethod = InstrumentUtils.findMethod(target, "close");
                closeMethod.addScopedInterceptor(AlibabaDruidConstants.INTERCEPTOR_CLOSE, AlibabaDruidConstants.SCOPE);
            }

            // getConnectionMethod
            InstrumentMethod getConnectionMethod = InstrumentUtils.findMethod(target, "getConnection");
            getConnectionMethod.addScopedInterceptor(AlibabaDruidConstants.INTERCEPTOR_GET_CONNECTION, AlibabaDruidConstants.SCOPE);
            getConnectionMethod = InstrumentUtils.findMethod(target, "getConnection", new String[]{"java.lang.String", "java.lang.String"});
            getConnectionMethod.addScopedInterceptor(AlibabaDruidConstants.INTERCEPTOR_GET_CONNECTION, AlibabaDruidConstants.SCOPE);

            return target.toBytecode();
            }
        });
    }


    private boolean isAvailableDataSourceMonitor(InstrumentClass target) {
        boolean hasMethod = target.hasMethod("getUrl");
        if (!hasMethod) {
            return false;
        }

        hasMethod = target.hasMethod("getPoolingCount");
        if (!hasMethod) {
            return false;
        }

        hasMethod = target.hasMethod("getMaxActive");
        return hasMethod;
    }
}
