const pathSep = require('path').sep;
//是否加密
const isEncrypt = true;

/**
 * ROT13加密
 * 
 * 需要先将字符串转成大写字母
 * 方法中的 toUpperCase/toLowerCase 为自己添加的规则
 */
function rot13(str) {
  if (!isEncrypt) return str;
  str = str.toUpperCase();
  let newarr = [];
  for (let i = 0; i < str.length; i++) {
    if (str.charCodeAt(i) < 65 || str.charCodeAt(i) > 90) {
      newarr.push(str.charAt(i));
      // 非字母形式的字符，直接跳过，存入数组newarr中
    } else if (str.charCodeAt(i) > 77) {
      newarr.push(String.fromCharCode(str.charCodeAt(i) - 13).toLowerCase());
      // 77 —— 第13个大写字母“M”
      // 后13个大写字母，减去13
    } else {
      newarr.push(String.fromCharCode(str.charCodeAt(i) + 13));
      // 前13个大写字母，加上13 
    }
  }
  let encryptName = newarr.join("");
  console.log('encryptName:' + encryptName);
  return encryptName;
}

function createModuleIdFactory() {
  //获取命令行执行的目录，__dirname是nodejs提供的变量
  const projectRootPath = __dirname;
  return (path) => {
    console.log('');
    console.log(path);
    let name = '';
    if (path.indexOf('node_modules' + pathSep + 'react-native' + pathSep + 'Libraries' + pathSep) > 0) {
      //这里是react native 自带的库，因其一般不会改变路径，所以可直接截取最后的文件名称
      name = path.substr(path.lastIndexOf(pathSep) + 1);
      console.log('react libraries:' + name);
    } else if (path.indexOf(projectRootPath) == 0) {
      /*
        这里是react native 自带库以外的其他库，因是绝对路径，带有设备信息，
        为了避免重复名称,可以保留node_modules直至结尾
        如/{User}/{username}/{userdir}/node_modules/xxx.js 需要将设备信息截掉
      */
      name = path.substr(projectRootPath.length + 1);
      console.log('root libraries:' + name);
    }
    //js png字符串 文件的后缀名可以去掉
    name = name.replace('.js', '');
    name = name.replace('.png', '');
    console.log('replace name:' + name);
    //最后在将斜杠替换为空串或下划线
    let regExp = pathSep == '\\' ? new RegExp('\\\\', "gm") : new RegExp(pathSep, "gm");
    name = name.replace(regExp, '');
    console.log('regExp name:' + name);
    //名称加密
    name = rot13(name);
    return name;
  };
}

function processModuleFilter(module) {
  // console.log(module)
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
  console.log(module.path)
  //其他就是应用代码
  return true;
}


module.exports = {
  createModuleIdFactory,
  processModuleFilter
}

