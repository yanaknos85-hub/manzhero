const exec = require("../exec");
const { err, log } = require("../logs");

checkBranch(["^master$", "^develop$", "^release/"]);

async function checkBranch(protectedBranches) {
  const fetchedBranch = await exec("git rev-parse --abbrev-ref HEAD");
  const currentBranch = `${fetchedBranch.stdout}`.trim();

  if (protectedBranches.find((pattern) => new RegExp(pattern, "i").exec(currentBranch))) {
    err(`You are on branch ${currentBranch}. Are you sure you want to commit to this branch?`);
    log("If so, commit with -n or use env LEFTHOOK_EXCLUDE to bypass this pre-commit hook.");

    process.exit(1);
  }

  log("✔  ok!");
  process.exit(0);
}
