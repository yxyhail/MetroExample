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

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.yxyhail.metroexample.react.JsLoaderUtil;
import com.yxyhail.metroexample.ui.Business1Activity;
import com.yxyhail.metroexample.ui.Business2Activity;
import com.yxyhail.metroexample.ui.Business3Activity;
import com.yxyhail.metroexample.ui.Business4Activity;
import com.yxyhail.metroexample.ui.Common1Activity;
import com.yxyhail.metroexample.ui.Common2Activity;
import com.yxyhail.metroexample.utils.FileUtil;
import com.yxyhail.metroexample.utils.SpConfig;

import java.io.File;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private int index = 0;
    private TextView common1;
    private TextView common2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.business1).setOnClickListener(this);
        findViewById(R.id.business2).setOnClickListener(this);
        findViewById(R.id.business3).setOnClickListener(this);
        findViewById(R.id.business4).setOnClickListener(this);
        JsLoaderUtil.jsState.isFilePrior = true;

        //模拟下载Bundle到本地File
        JsLoaderUtil.setDefaultFileDir(getApplication());
        File file = FileUtil.copyAssetsFile(this, "bundle.zip");
        FileUtil.unZip(file, getFilesDir().getAbsolutePath());

        SpConfig.setIndex(0);

        common1 = findViewById(R.id.intent_to_common1);
        common1.setOnClickListener(this);

        common2 = findViewById(R.id.intent_to_common2);
        common2.setOnClickListener(this);

        Switch aSwitch = findViewById(R.id.runtime_switch);

        aSwitch.setChecked(SpConfig.getLoadRuntime());
        setCheck(SpConfig.getLoadRuntime());
        aSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SpConfig.setLoadRuntime(isChecked);
            setCheck(isChecked);
        });

        JsLoaderUtil.jsState.bundleName = "business3";
        JsLoaderUtil.load(getApplication());
    }

    private void setCheck(boolean isChecked) {
        TextView rtText = findViewById(R.id.runtime_text);
        if (isChecked) {
            rtText.setText("LoadRuntime--on");
        } else {
            rtText.setText("LoadRuntime-off");
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.business1:
                windowIntent(Business1Activity.class);
                break;
            case R.id.business2:
                JsLoaderUtil.jsState.isFilePrior = true;
                JsLoaderUtil.setJsBundle("business2", "App2");
                windowIntent(Business2Activity.class);
                break;
            case R.id.business3:
                windowIntent(Business3Activity.class);
                break;
            case R.id.business4:
                JsLoaderUtil.setJsBundle("business4", "App4");
                JsLoaderUtil.load(getApplication(), () -> windowIntent(Business4Activity.class));
                break;
            case R.id.intent_to_common1:
                JsLoaderUtil.addBundles(getApplication(),
                        Arrays.asList("business1", "business2", "business3", "business4"),
                        () -> configComponentName(Common1Activity.class));
                break;
            case R.id.intent_to_common2:
                JsLoaderUtil.jsState.neededBundle.addAll(Arrays.asList("business1", "business2", "business3", "business4"));
                configComponentName(Common2Activity.class);
                break;
            default:
                break;
        }
    }


    private void configComponentName(Class<?> goalClass) {
        String text;
        if (goalClass == Common1Activity.class) {
            text = common1.getText().toString();
        } else {
            text = common2.getText().toString();
        }
        index = Integer.parseInt(text.substring(text.length() - 1)) - 1;

        if (index == 4) {
            index = 1;
        } else {
            index++;
        }
        JsLoaderUtil.jsState.componentName = "App" + index;
        windowIntent(goalClass);
        new Handler().postDelayed(() -> {
            int show = 0;
            if (index == 4) {
                show = 1;
            } else {
                show = index + 1;
            }
            if (goalClass == Common1Activity.class) {
                common1.setText("提前loadBundle后跳转到同一个页面-Business" + show);
            } else {
                common2.setText("先设置bundle名称，进入时loadBundle-Business" + show);
            }

        }, 1000);
    }

    private void windowIntent(Class<?> goalClass) {
        Intent intent = new Intent(this, goalClass);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.exit(0);

    }
}
