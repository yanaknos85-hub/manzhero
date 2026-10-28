module.exports = {
  extends: ["@commitlint/config-conventional"],
  rules: {
    "scope-case": [0],
    "subject-case": [0],
    "scope-empty": [1, "never"],
    "scope-max-length": [2, "always", 20],
    "subject-empty": [2, "never"],
    "type-empty": [2, "never"],
    "header-max-length": [1, "always", 120],
    "type-enum": [
      2,
      "always",
      [
        "build",
        "chore",
        "docs",
        "feat",
        "fix",
        "hotfix", // for hotfix actually
        "refactor",
        "revert",
        "style",
        "test",
      ],
    ],
  },
};
