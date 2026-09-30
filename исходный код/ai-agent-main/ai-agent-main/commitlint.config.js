module.exports = {
  extends: ['./commitlint'],
  parserPreset: {
    parserOpts: {
      issuePrefixes: ['TRANSPORT-'],
    },
  },
  rules: {
    'references-empty': [1, 'never'],
  },
};
