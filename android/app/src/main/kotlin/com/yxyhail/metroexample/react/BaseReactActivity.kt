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

package com.yxyhail.metroexample.react

import android.os.Bundle
import android.text.TextUtils

import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate

open class BaseReactActivity : ReactActivity() {

    protected open val bundleName: String
        get() = ""

    override fun createReactActivityDelegate(): ReactActivityDelegate {
        val localBundleName = bundleName
        if (!TextUtils.isEmpty(localBundleName)) {
            JsLoaderUtil.jsState.bundleName = localBundleName
        }
        return object : ReactActivityDelegate(this, mainComponentName) {
            override fun onCreate(savedInstanceState: Bundle?) {
                JsLoaderUtil.load(application) { super.onCreate(savedInstanceState) }
            }
        }
    }


    override fun getMainComponentName(): String? {
        return JsLoaderUtil.jsState.componentName
    }

}
