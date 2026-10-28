const exec = require("../exec");
const { warn, log } = require("../logs");

checkBranch("^(feat|fix|hotfix|docs|refactor|perf|ci)/[A-Z]+-\\d+.*", ["^master$", "^develop$", "^release/"]);

async function checkBranch(conventionalPattern, allowedPatterns) {
  const fetchedBranch = await exec("git rev-parse --abbrev-ref HEAD");
  const currentBranch = `${fetchedBranch.stdout}`.trim();

  if (
    !new RegExp(conventionalPattern).exec(currentBranch) &&
    !allowedPatterns.find((pattern) => new RegExp(pattern, "i").exec(currentBranch))
  ) {
    warn(`You are on branch ${currentBranch} and it's not match`);
    warn(`❌ conventional branch pattern: ${conventionalPattern.replace(/\/.+$/, "/ISSUE-101[-anything]")}.`);
    warn("Be sure you use right branch name before push.");
    process.exit(0);
  }

  log("✔  ok!");
  process.exit(0);
}
