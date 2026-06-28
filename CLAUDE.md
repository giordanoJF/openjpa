# OpenJPA — University Software Testing Project

## Collaboration Rules (read first)

1. **Never commit or push without explicit user approval.** Always show what would be committed and ask first.
2. **No AI traces anywhere.** No comments, variable names, commit messages, or report text that could suggest AI assistance or external collaboration. Code and tests must read as written entirely by the student. The course mandates LLM use for specific tasks (Test LLM, class variants via Copilot) — those are documented *as part of the methodology* — but the surrounding code, structure, and report prose must not betray external tooling.
3. **Commits must be grouped by task and authored by the student only.** Each commit should correspond to a meaningful, self-contained task. Never add Claude as co-author or mention AI in commit messages. The only author is giordanoJF.

## Project Context

This is a **university software testing project**. The goal is to design and execute a comprehensive testing campaign on the Apache OpenJPA codebase, covering two target classes, and to document everything in a detailed LaTeX/PDF report.

## Development Environment

- Primary dev environment: **Windows 11 with WSL2** (Ubuntu), but all work must remain **cross-platform** (Linux, macOS, Windows/WSL).
- Before introducing any new dependency or tool, verify it works on all three platforms or flag the limitation explicitly.
- Avoid Windows-only paths, line endings, or shell assumptions. Use POSIX paths in scripts.

## Target Classes (release 4.1.1)

Confirmed on branch `release-4.1.1` (tag `4.1.1`), both files exist:

1. `openjpa-kernel/src/main/java/org/apache/openjpa/kernel/BrokerImpl.java`
2. `openjpa-examples/opentrader/src/main/java/org/apache/openjpa/trader/client/LoginDialog.java`

For `classes.txt` submission (alphabetical order):
```
org.apache.openjpa.kernel.BrokerImpl
org.apache.openjpa.trader.client.LoginDialog
```

Selected via the Falessi project (ISW2) Milestone 4 algorithm output.

## Testing Toolchain

- **Maven + Surefire** — build and test runner
- **JUnit 5** — test framework (`openjpa-junit5` already present)
- **Mockito** — mocking for unit tests
- **JaCoCo** — control flow coverage measurement
- **PITest** — mutation testing
- **EvoSuite** — automated evolutionary test generation (ES)
- **Randoop or similar** — random test generation (RND)
- **LLM (Claude/GPT)** — prompt-based test generation (LLM)
- **CI** (GitHub Actions or TravisCI) — **required**, impacts final grade

When adding any plugin or dependency to a `pom.xml`, flag it here and propose a LaTeX update.

## Exam Procedure (9 Steps)

### Step 1 — Project
Apply all techniques on **2 classes** of Apache OpenJPA (open-source, Apache Software Foundation, sources on GitHub).

### Step 2 — Work environment
Fork on GitHub + CI framework (GitHub Actions / TravisCI). **CI impacts final grade** — treat it as mandatory.

### Step 3 — Test experimentation

**3a. Class selection** — done (see Target Classes above). Avoid trivially simple classes.

**3b. Manual tests via Category Partition (→ Test BB)**
- Black-box approach from specs/docs (not from reading the code)
- Define, implement, document N tests

**3c. Automatic test generation — 3 separate approaches:**
- **Test RND**: random generation (e.g., Randoop). Document the approach.
- **Test LLM**: LLM prompting (various prompt strategies). Document every prompt and result in detail.
- **Test ES**: EvoSuite — coverage-guided evolutionary generation (genetic algorithms with crossover/mutation *operators* on the test suite — unrelated to mutation score). Document in detail.

**3d. Integrate tests into build**
- **Disable/remove the project's native tests**, keep only the ones being developed.
- Use CI integrations.

### Step 4 — Quality validation

**4a. Two adequacy metrics** (natural choices: branch coverage via JaCoCo + mutation score via PITest):
- Compute for each test suite
- Compare and argue differences
- For **manual tests (BB)**: improve iteratively until both metrics increase → this produces **Test CF** (after CF coverage improvement) and **Test MT** (after mutation score improvement)

**4b. Mutation testing** on the two classes:
- Evaluate how tests react to mutations
- Add new tests to improve robustness (both for manual and automatic suites)

**4c. Reliability estimation**:
- Uniform operational profile (all test inputs equally likely)
- Based on the final test set after 4a and 4b
- Disable failing tests only when strictly necessary for the build to complete

### Step 5 — LLM class variants (Falessi Milestone 4)

Generate **4 refactored variants** (C\_1…C\_4) of each original class C\_0 using **Microsoft Copilot** (university account). The refactoring goal is to improve maintainability by removing SonarCloud smells, without changing functionality.

**Copilot prompt structure** (from Falessi slides):
1. "You are an expert Java developer. I want to improve the maintainability of the attached C\_0 class…"
2. "Create C\_X without changing C\_0 functionality and by removing the following smells… (report SonarCloud diagnostic)"
3. "Make sure C\_X passes the following tests…" ← **this is the key variable**
4. "Do not include in C\_X changes different from what I asked. C\_X should replace C\_0 and work with the other components of the system as C\_0 currently does."
5. "This is an important request; take all the time you need to provide a complete and accurate answer."

There are **three distinct tables** produced in this step:

---

**Table A — Generation constraints (Yes/No, from Falessi M4):**
Documents which tests were included as constraints in the Copilot prompt when generating each variant. Yes/No does NOT mean the variant passes those tests.

| Variant | Test BB | Test CF | Test MT | Test RND | Test ES | Test LLM |
|---|---|---|---|---|---|---|
| C\_0 | N/A | N/A | N/A | N/A | N/A | N/A |
| C\_1 | No | No | No | No | No | No |
| C\_2 | **Yes** | No | No | No | No | No |
| C\_3 | **Yes** | **Yes** | No | No | No | No |
| C\_4 | **Yes** | **Yes** | **Yes** | No | No | No |

---

**5a.** Generate new automatic tests (RND, ES, LLM) for each variant C\_1…C\_4.

**5b.** Fill the following two matrices (for **each** of the 2 classes):

**Table B — Pass/Fail (functionality preservation):**
Tests developed on C\_0 are **executed** on all versions. Verifies whether the refactored variants preserved the original behaviour or introduced regressions.

| Tests on C\_0 \ Version | C\_0 | C\_1 | C\_2 | C\_3 | C\_4 |
|---|---|---|---|---|---|
| Test BB | P | P/F? | P/F? | P/F? | P/F? |
| Test CF | P | P/F? | P/F? | P/F? | P/F? |
| Test MT | P | P/F? | P/F? | P/F? | P/F? |
| Test LLM | P | P/F? | P/F? | P/F? | P/F? |
| Test RND | P | P/F? | P/F? | P/F? | P/F? |
| Test ES | P | P/F? | P/F? | P/F? | P/F? |

**Table C — Delta (test quality comparison):**
For each variant, *new* automatic tests (LLM, RND, ES) are generated specifically for that variant and compared to those generated for C\_0. Measures how test quality changes across versions.
- Delta metrics: Coverage, Mutation Score, Chiarezza, Manutenibilità, Smell, SonarCloud smell categories

**Analysis per variant (Falessi):**
1. Does C\_X compile (alone and with the system)?
2. Does C\_X have smells? (old removed? new introduced?)
3. Are features positively correlated with bugginess higher in C\_X than C\_0? → maintainability may not have improved
4. Are features negatively correlated with bugginess higher in C\_X than C\_0? → maintainability may have improved

### Step 6 — Report PDF (~12 pages) — see below

### Step 7 — `classes.txt`
Plain text file, 2 classes in alphabetical order, format: `<package>.<subpackage>.<ClassName>`

### Step 8 — Submission
Email to `guglielmo.deangelis@iasi.cnr.it` by deadline on TEAMS calendar.

### Step 9 — Oral presentation
Discussion on course topics + GitHub repo and CI evaluated.

---

## The 6 Test Suite Types

| Sigla | Tipo | Come si produce |
|---|---|---|
| **Test BB** | Black-Box (Category Partition) | Manual, from specs/docs |
| **Test CF** | BB tests improved via Control Flow coverage | BB + JaCoCo iteration |
| **Test MT** | CF tests improved via Mutation Score | CF + PITest iteration |
| **Test RND** | Random automatic | Randoop or similar |
| **Test ES** | EvoSuite evolutionary automatic | EvoSuite (genetic algorithm) |
| **Test LLM** | LLM-prompted automatic | Various prompts, documented |

Key insight: BB → CF → MT is a **progression of the same manual suite** through two improvement iterations. RND, ES, LLM are **three independent automatic suites** generated from scratch.
EvoSuite uses evolutionary mutation/crossover *on the test suite itself* — this has nothing to do with software mutation testing (PITest).

---

## Report Requirements (LaTeX/PDF)

The final deliverable is a **PDF report** written in LaTeX, covering all testing activities for **both target classes**. Strict formatting rules:

- Length: ~12 A4 pages, single line spacing, Arial 10pt
- **No excessive titles, margins, or typographic decoration**
- Figures, tables, and code listings: include them but **append at the end** (they do not count toward the 12 pages)
- Figures/tables/listings must have **no explanatory text of their own** — all discussion goes in the main body
- The report must be **detailed** and must **describe and justify** all activities and decisions:
  - What was done
  - In what context
  - What problems were being addressed
  - The methodology followed
  - The results obtained

### Workflow

- After each meaningful commit, discuss with the user before updating the LaTeX source
- Never update the LaTeX silently — always propose changes and get approval first
- The LaTeX source lives in (TBD — to be created under `src/` or a `report/` directory)

## Repository Structure Notes

- Multi-module Maven project; root `pom.xml` aggregates all submodules
- `openjpa-junit5/` — JUnit 5 integration module already present
- `openjpa-lib/`, `openjpa-kernel/`, `openjpa-jdbc/`, `openjpa-persistence/` — core modules likely to contain target classes
- `scripts/` — utility scripts (mostly Windows `.bat`; prefer adding shell equivalents when needed)
