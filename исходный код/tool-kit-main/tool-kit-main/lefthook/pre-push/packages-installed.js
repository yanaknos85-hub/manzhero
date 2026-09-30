const rotPkg = require("rot-pkg").default;

// const { err, log } = require('../logs');

rotPkg(process.cwd());

// if (Object.values(result.packages).find(deps => Object.keys(deps).length)) {
//     err('Please `npm ci` and check your code is ok');
//     process.exit(1);
// }
//
// log('✔ ok! npm up-to-date');
