const extractUrlAndGlobal = require('webpack/lib/util/extractUrlAndGlobal');
const { RawSource } = require('webpack-sources');

const MODULE_NAME = "MFDynamicRemotes";

module.exports = class {
  apply(compiler) {
    compiler.hooks.make.tap(MODULE_NAME, compilation => {
      const scriptExternalModules = [];

      compilation.hooks.buildModule.tap(MODULE_NAME, module => {
        if (module.constructor.name === 'ExternalModule' && module.externalType === 'script') {
          scriptExternalModules.push(module);
        }
      });

      compilation.hooks.afterCodeGeneration.tap(MODULE_NAME, () => {
        scriptExternalModules.map(module => {
          const urlTemplate = extractUrlAndGlobal(module.request)[0];
          const sourceMap = compilation.codeGenerationResults.get(module).sources;
          sourceMap.set(
            'javascript',
            new RawSource(
              compilation.codeGenerationResults
                .get(module).sources
                .get('javascript')
                .source()
                .replace(
                  `"${urlTemplate}"`,
                  toExpression(urlTemplate)
                )
            )
          );
        });
      });
    });
  }
};

const toExpression = (templateUrl) => {
  const result = [],
    current = [];

  let isExpression = false,
    invalid = false;

  for (const c of templateUrl) {
    if (c === '[') {
      if (isExpression) {
        invalid = true;
        break;
      }

      isExpression = true;

      if (current.length) {
        result.push(`"${current.join('')}"`);
        current.length = 0;
      }
    } else if (c === ']') {
      if (!isExpression) {
        invalid = true;
        break;
      }

      isExpression = false;

      if (current.length) {
        result.push(`${current.join('')}`);
        current.length = 0;
      }

      current.length = 0;
    } else {
      current.push(c);
    }
  }

  if (isExpression || invalid) {
    throw new Error(`Invalid template URL "${templateUrl}"`);
  }

  if (current.length) {
    result.push(`"${current.join('')}"`);
  }

  return result.join(' + ');
};
