# React Native Android端 使用Metro拆包及原生加载 学习研究

## 一.拆包

拆包的方式一般有三种，分别为facebook的[Metro][metro]、携程的[moles-packer][moles]和diff patch（可以使用Google的[diff-match-patch][diff]）。但目前最好的方式可能还是[Metro][metro]。在调研的过程中，接触最早的，也是最全的的例子为[react-native-multibundler][multibundler]，这个例子甚至开发了可视化工具，进行拆包打包。同样在学习的时候也是基于这个例子进行修改的。

bundle代码拆分类型：基础包与业务包。  
基础包:将一些重复的js代码与第三方依赖库打成一个包。  
业务包：根据应用内的不同业务逻辑，拆分出一个或多个包。

### 1.Metro安装

实际在运行`npm install`时React Native已经安装Metro了，只不过可能并不是最新版（跟React Native版本有关），想使用最新版Metro,需要单独安装。

    npm install --save-dev metro metro-core
    
或

    yarn add --dev metro metro-core

### 2.Metro配置

配置Metro有三种方法，分别为`metro.config.js`、`metro.config.json`和`package.json`中添加`metro`字段，常用的方式为 `metro.config.js`。

[Metro配置][metroConfig]内部结构大致像这样：

    module.exports = {
      resolver: {
        /* resolver options */
      },
      transformer: {
        /* transformer options */
      },
      serializer: {
        /* serializer options */
      },
      server: {
        /* server options */
      }

      /* general options */
    };

每个optoins内都有很多配置选项，而对于我们这些初学者来说，最重要的是`serializer`选项内的`createModuleIdFactory`与`processModuleFilter`。

如图：

![][metro-config]

`createModuleIdFactory` :在[v0.24.1][customid]后,Metro支持了通过此方法配置自定义模块ID，同样支持字符串类型ID，用于生成`require`语句的模块ID，其类型为`() => (path: string) => number`(带有返回参数的返回函数的函数)，其中`path`为各个module的完整路径。此方法的另一个用途就是多次打包时，对于同一个模块生成相同的ID，下次更新发版时，不会因ID不同找不到Module。

`processModuleFilter`:根据给出的条件，对Module进行过滤，将不需要的模块过滤掉。其类型为`(module: Array<Module>) => boolean`，其中`module`为输出的模块，里面带着相应的参数，根据返回的波尔值判断是否过滤当前模块。返回`false`为过滤，不打入bundle。

接下来上代码：

    function createModuleIdFactory() {
      //获取命令行执行的目录，__dirname是nodejs提供的变量
      const projectRootPath = __dirname;
      return (path) => {
        let name = '';
        // 如果需要去除react-native/Libraries路径去除可以放开下面代码
        // if (path.indexOf('node_modules' + pathSep + 'react-native' + pathSep + 'Libraries' + pathSep) > 0) {
        //   //这里是react native 自带的库，因其一般不会改变路径，所以可直接截取最后的文件名称
        //   name = path.substr(path.lastIndexOf(pathSep) + 1);
        // }
        if (path.indexOf(projectRootPath) == 0) {
          /*
            这里是react native 自带库以外的其他库，因是绝对路径，带有设备信息，
            为了避免重复名称,可以保留node_modules直至结尾
            如/{User}/{username}/{userdir}/node_modules/xxx.js 需要将设备信息截掉
          */
          name = path.substr(projectRootPath.length + 1);
        }
        //js png字符串 文件的后缀名可以去掉
        // name = name.replace('.js', '');
        // name = name.replace('.png', '');
        //最后在将斜杠替换为下划线
        let regExp = pathSep == '\\' ? new RegExp('\\\\', "gm") : new RegExp(pathSep, "gm");
        name = name.replace(regExp, '_');
        //名称加密
        if (isEncrypt) {
          name = md5(name);
        }
        return name;
      };
    }

需要生成什么样的模块ID，可以根据自己的情况与喜好而定，无论是加密，拼接，甚至可以直接将获取到的`path`返回，唯一注意的是规则要统一，否则会无法找到相应的模块，当然模块ID定的越长，最终的bundle文件就越大，ID长短还是要适中，不过通过MD5加密后，长短已经无所谓了。

在打业务包时，可以使用filter对基础包内已有模块进行过滤，减小bundle文件大小。

    function processModuleFilter(module) {
      //过滤掉path为__prelude__的一些模块（基础包内已有）
      if (module['path'].indexOf('__prelude__') >= 0) {
        return false;
      }
      //过滤掉node_modules内的模块（基础包内已有）
      if (module['path'].indexOf(pathSep + 'node_modules' + pathSep) > 0) {
        /*
          但输出类型为js/script/virtual的模块不能过滤，一般此类型的文件为核心文件，
          如InitializeCore.js。每次加载bundle文件时都需要用到。
        */
        if ('js' + pathSep + 'script' + pathSep + 'virtual' == module['output'][0]['type']) {
          return true;
        }
        return false;
      }
      //其他就是应用代码
      return true;
    }

在xxx.config.js文件内添加上述两个方法后，将方法引入到`module.exports`内的`serializer`options内。

    module.exports = {
      serializer: {
        createModuleIdFactory: config.createModuleIdFactory,
        processModuleFilter: config.processModuleFilter
        /* serializer options */
      }
    }


### 3.Metro使用

根据基础包业务包的不同，添加 `--config <path/to/config>` 参数对相应入口文件打包。Metro官文虽然标明支持其他路径的配置文件，但至今没有成功过，只能在项目根目录添加配置文件，可能是我添加路径的方式不对，如果你知道如何添加其他路径config.js，请在issue中偷偷告诉我:sweat_smile:。

基础包：  
将需要的第三方依赖包与React Native的包、js文件等，可以通过`import`方式引入到一个js文件内，如[basics.js][basics.js]，再使用[basics.config.js][basics.config.js]当做参数传入到`--confg`后。


使用终端切换到项目根目录，执行命令：

    react-native bundle --platform android --dev false --entry-file src/basics/basics.js --bundle-output ./android/app/src/main/assets/basics.android.bundle --assets-dest android/app/src/main/res/ --config basics.config.js

业务包：  
根据自己应用的业务逻辑，分出不同的业务入口，并使用`AppRegistry`注册业务的主Component，如[index1.js][index1.js]，使用[business.config.js][business.config.js]传入到`--config`后。

命令如下：

    react-native bundle --platform android --dev false --entry-file src/index/index1.js --bundle-output ./android/app/src/main/assets/business1.android.bundle --assets-dest android/app/src/main/res/ --config business.config.js


将上述两种命令中的路径，替换为自己的路径，分了几个业务包就需要执行几次命令，可以将命令使用`&&`连接，写入到脚本文件内，如Linux的`.sh`或Windows的`.bat`文件，执行脚本文件即可。

通过`react-native bundle -h`命令可以查看相应的参数配置选项，其中`--entry-file`为加载的入口文件，如图：

![][bundle_help]

接下来看下`createModuleIdFactory`的log输出结果:

应用内的js:
![][img-app]
react native的js:
![][img-core]
三方依赖库的js:
![][img-thirdlib]

第一行为方法内的`path`路径  
第二行为根据是React Native自带文件还是三方库文件截取名称  
第三行是去除后缀的的名称  
第四行是替换斜杠的名称  
第五行是加密后的字符串

如果不加密的话，可以去除项目的目录，否则bundle文件会将项目结构暴露。

加密前：

![][img-bundle-name]

加密后：

![][img-bundle-name-encrypt]

## 二.Android 原生加载

:sparkles:目前Demo中使用的是Koltin语言，如果需要Java语言，可以切换[build.gradle][build.gradle]中`isUseKotlin`的值为false后点击Sync按钮进行同步。

### 1.源码浅析

React Native 加载bundle文件有三种方式，分别是从assets目录，本地File目录与Metro本地Server的delta bundle 。而平时用模拟器开发运行,更新文件双击`R`键时，使用的就是delta bundle。接下来就需要寻找加载bundle的接口文件，调用接口完成对不同业务包的加载，而基础包会在调用`createReactContextInBackground`时加载。

每个React Native页面都会继承`ReactActivity`，在onCreate方法内，会调用mDelegate.onCreate，在此方法内创建RootView，并设置到ContentView上。

看下源码逻辑：

    Delegate.loadApp->ReactRootView.startReactApplication->
    attachToReactInstanceManager->ReactInstanceManager.attachRootView->
    attachRootViewToInstance->ReactRootView.runApplication->
    catalystInstance.getJSModule(AppRegistry.class).runApplication

最后会通过CatalystInstance调用runApplication方法进行页面的呈现，如果在没有加载对应的bundle文件时，会报`Application xxx has not been registered.`之类的错误，只需在调用runApplication前将bundle文件加载即可。而接口CatalystInstance继承了一个名为`JSBundleLoaderDelegate`的接口，此接口中的三个方法分别为`loadScriptFromAssets`、`loadScriptFromFile`、`loadScriptFromDeltaBundle`，通过名称可看出是用来load不同位置的bundle的。

在ReactRootView的runApplication内，CatalystInstance是调用ReactContext.getCatalystInstance方法获取，而ReactContext内的CatalystInstance是在其创建时从ReactInstanceManager.createReactContext方法内由CatalystInstanceImpl的Builder新建。

ReactContext可以通过ReactApplication.getReactNativeHost.getReactInstanceManager.getCurrentReactContext获取，因此可以直接自己写一个工具类，在工具类内将需要加载的bundle文件提前加载好即可。

### 2.功能实现

实现此功能，Demo中用了两种方式，两种方式都需要使用工具类[JsLoaderUtil][JsLoaderUtil]。

一种是新建一个类作为基类，它继承`ReactActivity`,并重写了`createReactActivityDelegate`与`getMainComponentName`两个方法，在`createReactActivityDelegate`方法内新建`ReactActivityDelegate`时的`onCreate`方法调用super前，通过工具类将约定的组件加载好。这种方式的好处是，在子类内或进入子类前不用关心加载bundle过程的代码，基类中已经写好了，只需要告诉基类加载哪个业务的bundle文件，如[Business1Activity][Business1Activity]。这种方式的另一个用法就是在进入子类前直接告诉工具类需要加载的bundle文件，而在子类中则无需增加任何代码，仅仅继承`BaseReactActivity`,如[Business2Activity][Business2Activity]。

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

Demo中另一种方式是，让子类直接继承`ReactActivity`,而在进入子类前就用工具类加载好需要的业务bundle文件。这种方式的好处是不用拘泥于继承的父类，但需要注意是在进入页面前，一定要对业务包加载，否则会报错。
如[Business3Activity][Business3Activity]与[Business4Activity][Business4Activity]。

### 3.Double Tap R

到此我们的bundle文件已经加载好了，但不可能总是进行打包调试，平时开发时还是需要双击`R`进行热更新加载的。但JS代码都已经进行了业务拆分，并且Application中只对ReactNative返回了基础包的bundle，业务包分散在各个业务逻辑上。这时就需要一个开关来控制到底是加载文件bundle还是delta bundle，这大致分为三步或四步完成。

第一步，在[index.js][index.js]文件内将拆分出来的业务包导入，相当于一次性将业务模块全部注册。

    import './src/index/index1';
    import './src/index/index2';
    import './src/index/index3';
    import './src/index/index4';

第二步，在`JsLoaderUtil`工具类内增加判断，如果是Dev模式，直接返回，不加载bundle并且不调用`createReactContextInBackground`。

第三步，在[MainApplication][MainApplication]内`ReactNativeHost`的`getUseDeveloperSupport`方法内返回是否为Dev模式标志，并在`getJSMainModuleName`方法内返回之前的`index.js`名称，告诉React Native 此为入口文件。

这时就可以进入一个业务页面后，双击`R`更新页面内容了，但在切换开关时重启应用，会无法正常reload，就算进入页面，也会报错崩溃致使被杀掉进程，再进入应用就可以了。与其让它崩溃，不如要么将应用进程杀掉重启，要么增加第四步内容。

第四步，在`MainActivity`的`onDestroy`内，调用System.exit(0)，切换开关后重启应用就可以正常使用了。

### 4.特殊说明

每一个js文件都相当于一个Module，而React Native对加载过的Module不会再次加载，也就是说，如果先加载assets内的的bundle再加载本地File的bundle文件，呈现的还会是assets内的bundle文件，除非杀掉进程重启后，先加载本地File的bundle文件，才会生效，并没找到很好的解决方法。如果你知道如何解决请在issue中告诉我。

`assets`目录下的`bundle.zip`压缩包为带有`File`文字的业务包，用来测试从本地File加载功能。而`assets`内其他的业务bundle文件，如[business1.android.bundle][business1.android.bundle]，是带有`Assets`文字的bundle包，用来测试从`assets`加载功能。JS代码中，如[Business1.js][Business1.js]，是带有`Runtime`文字的业务，用来测试开发过程中双击`R`键热更新功能。

### 5.效果演示：
<img src="https://raw.githubusercontent.com/yxyhail/MetroExample/master/imgs//metro.gif" width="350" alt="Metro Example"/>
<img src="https://raw.githubusercontent.com/yxyhail/MetroExample/master/android/app/src/main/ic_launcher-web.png" width="80" alt="Metro Launcher"/> 

## 三.iOS 原生加载
### 1.源码接入
   相对于Android，iOS加载多个bundle文件较简单，只需要对RCTBridge扩展暴露以下接口即可：

   [-(void)executeSourceCode:(NSData *)sourceCode sync:(BOOL)sync;][ios_bridge]
   
### 2.实践（以下是以一个基础包和一个业务包测试）
1.将打包好的基础包和业务包导入项目中

图1：

![][ios_img1]

2.在App启动时加载基础包
图2：

![][ios_img2] 

3.在详情页或者加载基础包之后预加载业务包

图3：

![][ios_img3]

4.输出信息：先加载了基础包，后成功加载业务包，且页面&逻辑正常

图4：

![][ios_img4]


## 四. 功能展望

以上就是Demo中的全部内容了，对于下一步的功能展望就是，通过向工具类中传递不同的与服务器定好的模块Key，去下载不同的bundle内容，同样可以根据Key的不同，下载需要更新的图片资源，由工具类拷贝到指定的本地目录，供应用进行更新加载。


## GitHub地址：[https://github.com/yxyhail/MetroExample][example]









[example]:https://github.com/yxyhail/MetroExample
[metro]:https://github.com/facebook/metro
[moles]:https://github.com/ctripcorp/moles-packer
[diff]:https://github.com/google/diff-match-patch
[multibundler]:https://github.com/smallnew/react-native-multibundler
[customid]:https://github.com/facebook/metro/issues/6
[License]:http://www.apache.org/licenses/LICENSE-2.0
[metroConfig]:https://facebook.github.io/metro/docs/en/configuration

[index.js]:https://github.com/yxyhail/MetroExample/tree/master/index.js
[basics.js]:https://github.com/yxyhail/MetroExample/tree/master/src/basics/basics.js
[index1.js]:https://github.com/yxyhail/MetroExample/tree/master/src/index/index1.js
[business.config.js]:https://github.com/yxyhail/MetroExample/tree/master/business.config.js
[basics.config.js]:https://github.com/yxyhail/MetroExample/tree/master/basics.config.js
[Business1.js]:https://github.com/yxyhail/MetroExample/tree/master/src/business/Business1.js

[build.gradle]:https://github.com/yxyhail/MetroExample/tree/master/android/app/build.gradle
[Business1Activity]:https://github.com/yxyhail/MetroExample/tree/master/android/app/src/main/kotlin/com/yxyhail/metroexample/ui/Business1Activity.kt
[Business2Activity]:https://github.com/yxyhail/MetroExample/tree/master/android/app/src/main/kotlin/com/yxyhail/metroexample/ui/Business2Activity.kt
[Business3Activity]:https://github.com/yxyhail/MetroExample/tree/master/android/app/src/main/kotlin/com/yxyhail/metroexample/ui/Business3Activity.kt
[Business4Activity]:https://github.com/yxyhail/MetroExample/tree/master/android/app/src/main/kotlin/com/yxyhail/metroexample/ui/Business4Activity.kt
[JsLoaderUtil]:https://github.com/yxyhail/MetroExample/tree/master/android/app/src/main/kotlin/com/yxyhail/metroexample/react/JsLoaderUtil.kt
[MainApplication]:https://github.com/yxyhail/MetroExample/tree/master/android/app/src/main/kotlin/com/yxyhail/metroexample/MainApplication.kt
[business1.android.bundle]:https://github.com/yxyhail/MetroExample/tree/master/android/app/src/main/assets/business1.android.bundle

[ios_bridge]:https://github.com/chenzhe555/HHZRNRouteManager/blob/master/ios/subpackage_project_test/classes/HHZRNRouteManager/RCTBridge%2BHHZLoadOtherJS.h

[ios_img1]:https://raw.githubusercontent.com/yxyhail/MetroExample/master/imgs/ios_img1.png
[ios_img2]:https://raw.githubusercontent.com/yxyhail/MetroExample/master/imgs/ios_img2.png
[ios_img3]:https://raw.githubusercontent.com/yxyhail/MetroExample/master/imgs/ios_img3.png
[ios_img4]:https://raw.githubusercontent.com/yxyhail/MetroExample/master/imgs/ios_img4.png

[img-app]:https://raw.githubusercontent.com/yxyhail/MetroExample/master/imgs/moduleid-app.png
[img-core]:https://raw.githubusercontent.com/yxyhail/MetroExample/master/imgs/moduleid-core.png
[img-thirdlib]:https://raw.githubusercontent.com/yxyhail/MetroExample/master/imgs/moduleid-thirdlib.png
[img-bundle-name]:https://raw.githubusercontent.com/yxyhail/MetroExample/master/imgs/bundle-name.png
[img-bundle-name-encrypt]:https://raw.githubusercontent.com/yxyhail/MetroExample/master/imgs/bundle-name-encrypt.png
[bundle_help]:https://raw.githubusercontent.com/yxyhail/MetroExample/master/imgs/bundle_help.png
[metro-config]:https://raw.githubusercontent.com/yxyhail/MetroExample/master/imgs/metro_config.png
