/*
 * Copyright 2019 yxyhail
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yxyhail.metroexample;

import android.app.Application;
import android.content.Context;

import com.facebook.react.ReactApplication;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.soloader.SoLoader;
import com.yxyhail.metroexample.react.JsLoaderUtil;
import com.yxyhail.metroexample.utils.SpConfig;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

public class MainApplication extends Application implements ReactApplication {

    private static Application app;

    private final ReactNativeHost mReactNativeHost = new ReactNativeHost(this) {
        @Override
        public boolean getUseDeveloperSupport() {
            JsLoaderUtil.jsState.isDev = SpConfig.getLoadRuntime();
            return SpConfig.getLoadRuntime();
        }


        @Override
        protected List<ReactPackage> getPackages() {
            return Arrays.asList(
                    new MainReactPackage());
        }

        @Nullable
        @Override
        protected String getJSBundleFile() {
            return JsLoaderUtil.getBasicBundle(getApplication(), getBundleAssetName());
        }

        @Nullable
        @Override
        protected String getBundleAssetName() {
            return "basics.android.bundle";
        }

        @Override
        protected String getJSMainModuleName() {
            return "index";
        }
    };

    @Override
    public ReactNativeHost getReactNativeHost() {
        return mReactNativeHost;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        SoLoader.init(this, /* native exopackage */ false);
    }

    public static Application getApp() {
        return app;
    }

}
