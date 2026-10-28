const path = require('path');
const pleaseUpgradeNode = require('please-upgrade-node');

// check qg-kit package.json node version restrictions

// eslint-disable-next-line import/no-dynamic-require
const pkg = require(path.join(__dirname, '..', '..', 'package.json'));

pleaseUpgradeNode(pkg);
