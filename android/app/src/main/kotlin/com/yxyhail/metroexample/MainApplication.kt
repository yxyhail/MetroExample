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

package com.yxyhail.metroexample

import android.app.Application

import com.facebook.react.ReactApplication
import com.facebook.react.ReactNativeHost
import com.facebook.react.ReactPackage
import com.facebook.react.shell.MainReactPackage
import com.facebook.soloader.SoLoader

import java.util.Arrays

import com.yxyhail.metroexample.react.JsLoaderUtil
import com.yxyhail.metroexample.utils.SpConfig

class MainApplication : Application(), ReactApplication {

    private val mReactNativeHost = object : ReactNativeHost(this) {
        override fun getUseDeveloperSupport(): Boolean {
            JsLoaderUtil.jsState.isDev = SpConfig.loadRuntime
            return SpConfig.loadRuntime
        }


        override fun getPackages(): List<ReactPackage> {
            return Arrays.asList<ReactPackage>(
                    MainReactPackage())
        }

        override fun getJSBundleFile(): String? {
            return JsLoaderUtil.getBasicBundle(application, bundleAssetName!!)
        }

        override fun getBundleAssetName(): String? {
            return "basics.android.bundle"
        }

        override fun getJSMainModuleName(): String {
            return "index"
        }
    }

    override fun getReactNativeHost(): ReactNativeHost {
        return mReactNativeHost
    }

    override fun onCreate() {
        super.onCreate()
        app = this
        SoLoader.init(this, /* native exopackage */ false)
    }

    companion object {
        var app: Application? = null
            private set
    }

}
