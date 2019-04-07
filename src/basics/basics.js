import 'react';
import 'react-native';

import React, { Component } from "react";

import { Text } from "react-native";


var _ = require('lodash');
Text.render = _.wrap(Text.render, function (func, ...args) {
  let originText = func.apply(this, args)
  return React.cloneElement(originText, { allowFontScaling: false })
})
