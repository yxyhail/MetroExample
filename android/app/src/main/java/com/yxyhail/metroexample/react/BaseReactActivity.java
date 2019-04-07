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

package com.yxyhail.metroexample.react;

import android.os.Bundle;
import android.text.TextUtils;

import com.facebook.react.ReactActivity;
import com.facebook.react.ReactActivityDelegate;
import com.yxyhail.metroexample.react.JsLoaderUtil;

import javax.annotation.Nullable;

public class BaseReactActivity extends ReactActivity {
    @Override
    protected ReactActivityDelegate createReactActivityDelegate() {
        String localBundleName = getBundleName();
        if (!TextUtils.isEmpty(localBundleName)) {
            JsLoaderUtil.jsState.bundleName = localBundleName;
        }
        return new ReactActivityDelegate(this, getMainComponentName()) {
            @Override
            protected void onCreate(Bundle savedInstanceState) {
                JsLoaderUtil.load(getApplication(),
                        () -> super.onCreate(savedInstanceState));
            }
        };
    }


    @Nullable
    @Override
    protected String getMainComponentName() {
        return JsLoaderUtil.jsState.componentName;
    }

    protected String getBundleName() {
        return "";
    }

}
