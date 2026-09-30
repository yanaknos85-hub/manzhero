/* eslint-disable no-console */
const colors = require("colors/safe");

colors.setTheme({
  ok: ["green"],
  error: ["white", "bgRed"],
  warning: ["yellow"],
});

module.exports = {
  err: (text = "") => console.error(colors.error(text)),
  warn: (text = "") => console.warn(colors.warning(text)),
  log: (text = "") => console.log(colors.ok(text)),
};
