const exec = require("../exec");
const { err, warn, log } = require("../logs");

const IS_GITFLOW = true;
const REFERENCE_BRANCH = IS_GITFLOW ? "origin/develop" : "origin/master";
const MAX_COMMITS_BEHIND = 3;

checkBranch(REFERENCE_BRANCH, MAX_COMMITS_BEHIND, ["^master$", "^develop$", "^release/", "^hotfix/"]);

async function checkBranch(refBranch, maxCommitsBehind = 1, excludedBranches) {
  const fetchedBranch = await exec("git rev-parse --abbrev-ref HEAD");
  const currentBranch = `${fetchedBranch.stdout}`.trim();

  if (excludedBranches.find((pattern) => new RegExp(pattern, "i").exec(currentBranch))) {
    log(`✔  skipped for ${currentBranch}`);
    process.exit(0);
    return;
  }

  // its not lightweight?
  log("Fetching...");
  await exec("git fetch origin");

  const fetchCommitsBehind = await exec(`git rev-list --left-right --count ${refBranch}...HEAD`);
  const commitsBehind = parseInt(`${fetchCommitsBehind.stdout}`.split("\t")[0], 10);

  if (commitsBehind > maxCommitsBehind) {
    const logFormat = '--format="%h %cd %s %an"';
    const fetchHistory = await exec(`git --no-pager log ${logFormat} --date=short HEAD...${refBranch}`);
    warn(`${fetchHistory.stdout}`);
    log();

    err(`Your branch ${currentBranch} is ${commitsBehind} commits behind.`);
    warn(`To rebase, run 'git rebase ${refBranch}'`);

    process.exit(1);
  }

  log(`✔  ok! your branch is ${commitsBehind} only commits behind ${refBranch}`);
  process.exit(0);
}
