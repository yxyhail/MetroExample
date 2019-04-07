const config = require('./metro.config')

module.exports = {

  serializer: {
    createModuleIdFactory: config.createModuleIdFactory,
    /* serializer options */
  }
};
