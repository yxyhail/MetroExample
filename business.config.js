const config = require('./metro.config')

module.exports = {

  serializer: {
    createModuleIdFactory: config.createModuleIdFactory,
    processModuleFilter: config.processModuleFilter
    /* serializer options */
  }
};
