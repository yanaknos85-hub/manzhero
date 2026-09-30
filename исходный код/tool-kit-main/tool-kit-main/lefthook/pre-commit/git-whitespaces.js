const exec = require("../exec");
const { err, warn, log } = require("../logs");

checkBranch();

async function checkBranch() {
  let checkString;
  try {
    checkString = await exec("git diff --check --cached");
  } catch (result) {
    checkString = result;
  }
  const incorretWhitespaceInFiles = `${checkString.stdout}`.split("\n").filter(Boolean).length;

  // trailing spaces and LF?
  if (incorretWhitespaceInFiles) {
    const str = checkString.stdout.split("\n").filter((v, key) => key % 2 === 0);
    log(str.join("\n"));
    log();

    // prettier-ignore
    // eslint-disable-next-line prettier/prettier
    err('Your spaces don\'t agree with your core.whitespace rules.');
    warn("Please run `git diff --check HEAD` to see your errors.");
    warn("You can commit with -n or use env LEFTHOOK_EXCLUDE to bypass this pre-commit hook.");

    process.exit(1);
  }

  // пустой EOL-last будет в eslint, плюс можно проверить по всему проекту, например:
  // git ls-tree -r -z --name-only HEAD | xargs -0 file | grep text | cut -d: -f1 | xargs -I {} bash -c 'if [ -n "`tail -c 1 "{}"`" ]; then echo {}; fi'

  log("✔  ok!");
  process.exit(0);
}
