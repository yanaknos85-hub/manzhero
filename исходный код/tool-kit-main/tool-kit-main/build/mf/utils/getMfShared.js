const mfConfigShared = require('../mf.config.shared');

module.exports = (isRemote) =>
  Object.keys(mfConfigShared).reduce(
    (shared, pkgName) => ({
      ...shared,
      [pkgName]: {
        ...mfConfigShared[pkgName],
        singleton: true, // only a single version of the shared module is allowed
        strictVersion: true, // don't use shared version when version isn't valid. Singleton or modules without fallback will throw, otherwise fallback is used
      },
    }),
    {}
  );
