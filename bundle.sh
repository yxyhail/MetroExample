#打基础包
#react-native bundle --platform android --dev false --entry-file src/basics/basics.js --bundle-output ./android/app/src/main/assets/basics.android.bundle --assets-dest android/app/src/main/res/ --config basics.config.js

#打业务1包
#react-native bundle --platform android --dev false --entry-file src/index/index1.js --bundle-output ./android/app/src/main/assets/business1.android.bundle --assets-dest android/app/src/main/res/ --config business.config.js

#打业务2包
#react-native bundle --platform android --dev false --entry-file src/index/index2.js --bundle-output ./android/app/src/main/assets/business2.android.bundle --assets-dest android/app/src/main/res/ --config business.config.js

#打业务3包
#react-native bundle --platform android --dev false --entry-file src/index/index3.js --bundle-output ./android/app/src/main/assets/business3.android.bundle --assets-dest android/app/src/main/res/ --config business.config.js

#打业务4包
#react-native bundle --platform android --dev false --entry-file src/index/index4.js --bundle-output ./android/app/src/main/assets/business4.android.bundle --assets-dest android/app/src/main/res/ --config business.config.js

react-native bundle --platform android --dev false --entry-file src/basics/basics.js --bundle-output ./android/app/src/main/assets/basics.android.bundle --assets-dest android/app/src/main/res/ --config basics.config.js && react-native bundle --platform android --dev false --entry-file src/index/index1.js --bundle-output ./android/app/src/main/assets/business1.android.bundle --assets-dest android/app/src/main/res/ --config business.config.js && react-native bundle --platform android --dev false --entry-file src/index/index2.js --bundle-output ./android/app/src/main/assets/business2.android.bundle --assets-dest android/app/src/main/res/ --config business.config.js && react-native bundle --platform android --dev false --entry-file src/index/index3.js --bundle-output ./android/app/src/main/assets/business3.android.bundle --assets-dest android/app/src/main/res/ --config business.config.js && react-native bundle --platform android --dev false --entry-file src/index/index4.js --bundle-output ./android/app/src/main/assets/business4.android.bundle --assets-dest android/app/src/main/res/ --config business.config.js