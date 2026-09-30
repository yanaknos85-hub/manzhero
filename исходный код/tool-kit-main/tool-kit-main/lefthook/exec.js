const util = require('util');
const childProcessExec = require('child_process').exec;

module.exports = util.promisify(childProcessExec);
