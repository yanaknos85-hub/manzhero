const pkgOk = require("pkg-ok");

// check run directory package.json
const rootPath = process.cwd();

pkgOk(rootPath, {
  fields: ["files", "main", "bin"],
});
