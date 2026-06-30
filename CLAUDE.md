# OpenJPA — University Software Testing Project

## Collaboration Rules (read first)

1. **Never commit or push without explicit user approval.** Always show what would be committed and ask first.
2. **No AI traces anywhere.** No comments, variable names, commit messages, or report text that could suggest AI assistance or external collaboration. Code and tests must read as written entirely by the student. The course mandates LLM use for specific tasks (Test LLM, class variants via Copilot) — those are documented *as part of the methodology* — but the surrounding code, structure, and report prose must not betray external tooling.
3. **Commits must be grouped by task and authored by the student only.** Each commit should correspond to a meaningful, self-contained task. Never add Claude as co-author or mention AI in commit messages. The only author is giordanoJF.

## Testing Methodology Rules (mandatory, never override)

4. **Always separate test case DESIGN from test case IMPLEMENTATION.** Design comes first and must be complete before writing any JUnit code. Design = categories, choices, constraints, test frames, abstract test specifications. Implementation = translating those specs into runnable JUnit 5 tests. Never collapse the two phases into one.
5. **Always respect the analysis type (black-box vs white-box) as an absolute constraint.** If the current task is black-box: derive test cases exclusively from documentation, specs, and declared interfaces — never from the implementation body. If white-box: base decisions on the actual code structure (CFG, branches, paths). Never silently mix approaches or ignore this distinction when it appears in a task description.
6. **Use precise terminology in all code, comments, and report prose.** The professor distinguishes: *error* (human mistake) → *fault* (defect in the code) → *failure* (observable wrong behavior). A fault is necessary but not sufficient for a failure. Never use "bug" generically in the report. Testing finds failures; debugging locates and removes faults.
7. **Every test must follow the SEEV structure:** Setup → Exercise → Verify (assert) → Teardown. Oracle values (expected outputs) must come from the specification or documentation, never inferred from the code under test. This applies to all 6 suite types.

## Project Context

This is a **university software testing project**. The goal is to design and execute a comprehensive testing campaign on the Apache OpenJPA codebase, covering two target classes, and to document everything in a detailed LaTeX/PDF report.

The campaign covers the 3 orthogonal dimensions of testing (Lezione 14):
- **Level**: Unit (primary) + Integration where interactions between modules are under test
- **Method**: Black-box (BB) for manual design from specs; white-box (WB) when iterating on coverage metrics
- **Type**: Manual (Test BB/CF/MT) + Automated (Test RND/ES/LLM)

Every test activity in the report must be classified along all three dimensions.

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

Each tool corresponds to a specific phase and metric. Do not swap them.

- **Maven + Surefire** — unit test runner (fase `test`); naming patterns `Test*/*Test/*Tests/*TestCase`; reports in `target/surefire-reports/TEST-*.xml`
- **Maven Failsafe** — integration test runner (fase `integration-test`/`verify`); naming patterns `IT*/*IT/*ITCase`; reports in `target/failsafe-reports/`; goals: `integration-test` + `verify`
- **JUnit 5** — test framework (`openjpa-junit5` module already present); course reference is JUnit 4 annotations (@Test, @Before/@After, @BeforeClass/@AfterClass, @RunWith) but JUnit 5 is the implementation target
- **Mockito** — stub/mock for unit and integration tests; use `mock()`, `@Mock`, `when().thenReturn()`, `verify()`; runner: `MockitoJUnitRunner` or `MockitoJUnit.rule()`
- **JaCoCo** — measures **branch coverage** (covered branches / total branches, not just line coverage); HTML report in `target/site/jacoco/`; Maven plugin: `jacoco-maven-plugin`
- **PITest v1.5.1** — mutation testing; goal `mutationCoverage`; report in `target/pit-reports/`; mutation score = |D| / (|M| − |E|) where D=killed, M=total mutants, E=equivalent
- **EvoSuite** — evolutionary test generation via genetic algorithm applied to the **test suite** (NOT to the SUT); operates at bytecode level; fitness = branch coverage + test suite compactness; stand-alone JAR or Maven plugin
- **Randoop** — random test generation via random sequences of API method calls on the compiled class
- **LLM (Claude/GPT)** — prompt-based test generation; multiple strategies (zero-shot, few-shot, CoT, ToT); document every prompt + result + pass/fail
- **CI** (GitHub Actions or TravisCI) — **required, impacts final grade**; trigger on every commit: build → test → report

When adding any plugin or dependency to a `pom.xml`, flag it here and propose a LaTeX update.

## Exam Procedure (9 Steps)

### Step 1 — Project
Apply all techniques on **2 classes** of Apache OpenJPA (open-source, Apache Software Foundation, sources on GitHub).

### Step 2 — Work environment
Fork on GitHub + CI framework (GitHub Actions / TravisCI). **CI impacts final grade** — treat it as mandatory.

CI+CT cycle: Source control → trigger → Build server (config + build + test) → report → Development → commit → loop. Development does NOT stop while CI runs (asynchronous notification).

### Step 3 — Test experimentation

**3a. Class selection** — done (see Target Classes above). Avoid trivially simple classes.

**3b. Manual tests via Category Partition (→ Test BB)**

Black-box: derive everything from documentation, specs, and declared API — never from the implementation body.

Category Partition process (Lezioni 20-23-26), in order:
1. **Identify categories**: independent input dimensions (method parameters, environment conditions, object state before call)
2. **Enumerate choices** per category: define equivalence classes + boundary values (min, max, boundary−1, boundary+1, interior representative)
3. **Define constraints** between choices: `[if C]` (choice valid only if C holds), `[error]` (marks invalid input), `[single]` (combine with at most one other choice), `[property P]` (requires P)
4. **Generate test frames**: cross-product of choices reduced by constraints; each frame = one choice per category
5. **Instantiate with concrete values** → one concrete test case per frame (or group compatible frames when identical oracle)

Oracle rule: the expected output for each test must come from the specification. If no spec is available, explicitly document the assumption used as oracle. Never derive expected values from the code under test.

Each test must follow SEEV: Setup → Exercise → Verify (assert) → Teardown.

Do NOT justify a low test count with cost considerations — the professor explicitly penalizes this motivation. Document the design rationale for every test.

**3c. Automatic test generation — 3 separate and independent approaches:**

- **Test RND (Randoop)**: randomly generates and executes sequences of API method calls on the compiled class; detects failures via exception/contract violations. Document: Randoop version, configuration (timeout, method limits, seed), number of tests generated, pass/fail counts, any notable failures found.

- **Test LLM**: use multiple prompt strategies. Minimum: **10 documented prompts** (4 zero-shot, 4 few-shot, 2 CoT or ToT). For each prompt, document: the exact prompt text, the generated test code, and whether the tests compile and pass. Vary strategies to enable comparison in the report.

- **Test ES (EvoSuite)**: genetic algorithm applied to the test suite — crossover and mutation are operators on test cases (completely different from PITest's mutation on the SUT). Maximizes branch coverage, minimizes test suite size. Operates at bytecode level. Document: EvoSuite version, configuration, branch coverage achieved, number of tests generated.

**3d. Integrate tests into build**
- **Disable/remove the project's native tests** — delete or skip them so only the campaign tests run.
- Unit tests via Surefire; integration tests via Failsafe; both triggered on every CI commit.
- Verify the CI pipeline passes before proceeding to Step 4.

### Step 4 — Quality validation

**4a. Two adequacy metrics — branch coverage (JaCoCo) + mutation score (PITest)**

Coverage criteria hierarchy from weakest to strongest (Lezioni 29-32):
statement → block → branch/decision → condition → BC → MC (2^N tests) → **MC/DC** (N+1 tests, DO-178C avionics standard).
JaCoCo measures **branch coverage** (branch/decision level). Use it as the primary white-box adequacy metric.

Iteration process for the manual suite (BB → CF → MT):
1. Run JaCoCo on Test BB → open the HTML report → identify uncovered branches (red arcs in CFG) → add targeted tests that exercise those branches → rerun JaCoCo → verify branch coverage **increased** → checkpoint: this is **Test CF**
2. Run PITest on Test CF → open the HTML report → identify survived mutants (operator, location, mutant text) → add tests that **kill** them (the test must reach the mutant = C1, cause a state change = C2, and propagate the change to an assert = C3) → rerun PITest → verify mutation score **increased** → checkpoint: this is **Test MT**

For automatic suites (RND, ES, LLM): compute both metrics but do NOT iterate — each is generated once and measured as-is.

Compare all 6 suites on both metrics. Justify differences in the report with concrete reasoning (not vague statements).

**4b. Mutation testing details**

PITest mutation operators: AOR (arithmetic operator replacement), ROR (relational operator replacement), COR (conditional operator replacement), SOR (shift operator replacement), LOR (logical operator replacement), ASR (assignment operator replacement), SDL (statement deletion), SVR (variable replacement).

Conditions for a test to **kill** a mutant (strong mutation, required by PITest):
- **C1 reachability**: the mutated statement is executed by the test
- **C2 infection**: the program state after the mutation differs from the original
- **C3 propagation**: the state difference reaches an observable output that the assert catches

Equivalent mutants (same observable behavior as original) are excluded from the denominator: score = |D| / (|M| − |E|). Equivalence is undecidable in general — use the Coupling Hypothesis (CPH) to argue that killing simple mutants is sufficient.

**4c. Reliability estimation**
- Uniform operational profile: all test inputs treated as equally likely (no weighting by usage frequency)
- Reliability = passed tests / total tests = 1 − PFD (probability of failure on demand)
- Compute on the final test set after Step 4b (Test MT for the manual suite; one-shot for automatic suites)
- Disable failing tests only when strictly necessary for the build to pass; always document which tests and why

### Step 5 — LLM class variants (Falessi Milestone 4)

Generate **4 refactored variants** (C\_1…C\_4) of each original class C\_0 using **Microsoft Copilot** (university account — NOT Claude). The refactoring goal: improve maintainability by removing SonarCloud smells, without changing functionality.

**Copilot prompt structure** (from Falessi slides):
1. "You are an expert Java developer. I want to improve the maintainability of the attached C\_0 class…"
2. "Create C\_X without changing C\_0 functionality and by removing the following smells… (report SonarCloud diagnostic)"
3. "Make sure C\_X passes the following tests…" ← **this is the key variable** (which tests are included changes per variant, see Table A)
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
| Test BB | P (o bug) | P/F? | P/F? | P/F? | P/F? |
| Test CF | P (o bug) | P/F? | P/F? | P/F? | P/F? |
| Test MT | P (o bug) | P/F? | P/F? | P/F? | P/F? |
| Test LLM | P (o bug) | P/F? | P/F? | P/F? | P/F? |
| Test RND | P (o bug) | P/F? | P/F? | P/F? | P/F? |
| Test ES | P (o bug) | P/F? | P/F? | P/F? | P/F? |

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

| Sigla | Tipo | Come si produce | Metrica target | Iterata? |
|---|---|---|---|---|
| **Test BB** | Black-Box (Category Partition) | Manual; Category Partition + BVA da specs/docs | N test frames dal design | No |
| **Test CF** | BB + branch coverage improvement | BB + JaCoCo: aggiungi test per rami scoperti | Branch coverage ↑ (JaCoCo) | Sì (da BB) |
| **Test MT** | CF + mutation score improvement | CF + PITest: aggiungi test che uccidono mutanti sopravvissuti | Mutation score ↑ (PITest) | Sì (da CF) |
| **Test RND** | Random automatic | Randoop: sequenze casuali di chiamate API; one-shot | Branch coverage + mutation score (misurati) | No |
| **Test ES** | EvoSuite evolutionary automatic | Algoritmo genetico sul test suite (bytecode); one-shot | Branch coverage (EvoSuite interno) | No |
| **Test LLM** | LLM-prompted automatic | 10+ prompt documentati (zero-shot/few-shot/CoT/ToT); one-shot | Branch coverage + mutation score (misurati) | No |

Key insights:
- **BB → CF → MT** è una progressione iterativa della stessa suite manuale; ogni step produce una versione più raffinata e supersede la precedente. Test MT è il più completo.
- **RND, ES, LLM** sono tre suite automatiche indipendenti, generate una volta sola; non si iterano.
- L'algoritmo evolutivo di EvoSuite (crossover + mutazione sul test suite) non ha nulla a che fare con il mutation testing di PITest (che opera sul SUT).
- Le 3 suite automatiche esistono per confrontare coverage/mutation score rispetto alla suite manuale — non per sostituirla.

---

## Report Requirements (LaTeX/PDF)

The final deliverable is a **PDF report** written in LaTeX, covering all testing activities for **both target classes**. Strict formatting rules:

- Length: ~12 A4 pages, single line spacing, Arial 10pt
- **No excessive titles, margins, or typographic decoration**
- Figures, tables, and code listings: include them but **append at the end** (they do not count toward the 12 pages)
- Figures/tables/listings must have **no explanatory text of their own** — all discussion goes in the main body
- The report must be **detailed** and must **describe and justify** all activities and decisions:
  - What was done, in what context, what problems were being addressed
  - The methodology followed and **why** (connect to the professor's frameworks: Category Partition, CFG criteria, Offutt mutation schema, etc.)
  - The results obtained as **concrete numbers** (e.g., "branch coverage increased from 42% to 67%"), not vague statements

**Terminology (mandatory precision, never use "bug" generically):**
- *error* = human mistake; *fault* = code defect introduced by an error; *failure* = observable wrong behavior caused by a fault
- Testing detects failures; debugging localizes and removes the fault; fault is necessary but not sufficient for failure
- Cite Dijkstra when discussing testing limits: *"testing can show the presence of bugs, not their absence"*

**Classify every test activity along the 3 orthogonal dimensions:** Level (Unit / Integration / System / Acceptance) + Method (BB / WB / Non-functional) + Type (Manual / Automated).

**Adequacy section must address:** what branch coverage and mutation score measure, why they were chosen, what their limits are (e.g., 100% branch coverage ≠ absence of faults), and what the measured values mean specifically for the class under test.

### Workflow

- After each meaningful commit, discuss with the user before updating the LaTeX source
- Never update the LaTeX silently — always propose changes and get approval first
- The LaTeX source lives in `report/`

## Lecture Context — Professor De Angelis (ISW2 — Software Testing)

This section accumulates key concepts from each lecture so that report writing and test design remain aligned with what the professor expects.

---

### Lezione 5 — Introduzione e Concetti Generali

- **V&V**: Verification = conforme alle specifiche; Validation = conforme alle attese utente (Boehm)
- **Modello D-P-S-C**: P soddisfa S sse ∀d∈D, P(d)=S(d). Testing non può provarla → esplora un campione di D
- **Terminologia (usarla precisa nel report)**: error (causa umana) → fault (difetto nel codice) → failure (comportamento scorretto osservabile). Fault è condizione necessaria ma non sufficiente per failure
- **Tre strategie**: defect prevention (rimuovi errori umani) | defect reduction (testing+debugging) | defect containment (failure prevention)
- **Testing vs Debugging**: testing trova le failure; debugging localizza e rimuove il fault
- **Struttura test (SEEV)**: Setup → Exercise → Verify (assert) → Teardown
- **3 domande fondamentali**: quali input? (partizione dominio, classi equivalenza) | quando smettere? (criteri copertura) | come verifico? (oracle problem → valori da specifica)
- **Correttezza vs Reliability**: correttezza = proprietà assoluta (prove formali); reliability = attributo statistico = 1 − PFD (probability of failure on demand)
- **Operational Profile**: distribuzione di probabilità sugli input reali. Profilo uniforme (Step 4c del progetto) → reliability = test_passati / test_totali
- **Dijkstra**: "testing can show the presence of bugs, not their absence" — citarlo nel report quando si discutono i limiti

---

### Lezione 2 — Panoramica del corso

- Sillabo: Intro+GC → AT+CT → Unit+Integration → Approaches to Test Generation → Adequacy (CF Coverage) → Adequacy (Mutation Testing) → Coverage-based Test Generation
- Testi: Mathur "Foundations of SW Testing" 2/e (Pearson 2013); Garcia "Mastering SW Testing with JUnit 5" (Packt 2017); Tahchiev "JUnit in Action" 2a ed. (Manning 2010); Lewis "SW testing and continuous quality improvement" (CRC 2017); Humble & Farley "Continuous Delivery" (Addison-Wesley 2011)
- Report: ~12 pp A4, singola interlinea, Arial 10pt; non esagerare con titoli/margini/fronzoli; figure/tabelle/listati in fondo NON contano verso le 12 pp e NON devono avere testo esplicativo (tutto nel body)
- CI impatta sulla valutazione finale; Step 3a: fortemente raccomandato approccio BB, non da codice se non strettamente necessario; Step 4c: profilo operazionale uniforme; disabilitare failing test solo quando strettamente necessario per il build

---

### Lezione 8 — SQA, Automated Testing, Continuous Testing

- SQA: "systematic activities providing evidence of fitness for use of total software product" — 3 componenti: Software Testing + Quality Control + SW Configuration Management; Testing è SOLO UNO degli aspetti di SQA
- 2^32 possibili test per `int myBuggyFact(int p)` — impossibile testare tutto → selezione obbligatoria (needle in haystack)
- Automated Testing (AT): automazione in esecuzione, report, selezione/prioritizzazione, valutazione bontà test set; obiettivi: efficienza+efficacia; benefici: riduce costi/durata, aumenta ripetibilità e attendibilità statistica
- Continuous Testing (CT): integrazione testing con ambienti sviluppo+build+versioning; test automaticamente dopo ogni evoluzione; politiche prioritizzazione/selezione/orchestrazione; notifiche asincrone (sviluppo NON si interrompe)
- Ciclo CI+CT: Source control → (trigger) → Build server (config+build+test) → (report) → Development → (commit) → loop
- Git: VCS distribuito open-source; 3 workflow (shared repo, integration manager/fork, dictator+lieutenants); GitHub fork+PR
- Maven: fasi default lifecycle: validate→compile→test→package→integration-test→verify→install→deploy; più clean e site
- JUnit 4.X (di riferimento nel corso): elementi dichiarati con annotazioni, setup/teardown, runners; JUnit 5.X: più modulare, composizione multipli runners, 2 use-case (API per scrittura test program + SPI per discovery/execution)
- CI framework: Travis-CI, GitHub Actions, GitLab CI-CD, Circle-CI — tutti gratuiti per open-source; no subscription necessaria
- CI+CT visione complessiva: blessed repo ← PR ← integration manager ← FORK developer public ← PUSH ← developer private (local Maven)

---

### Lezione 11 — Q&A: Maven, GitHub, CI Set-up and Configurations

- Lezione di Q&A: reminder link Maven quickstart, Git quickstart, GitHub Actions quickstart
- Esercizio preliminare (9 step): install MVN → archetype Java → configura pom.xml → crea classe Java → build + inspect target/ → versiona su GitHub → agganciare CI (e.g. travis.yaml) → build automatico su ogni commit → controlla esito build
- Secondo esercizio: fork Apache Bookkeeper, workspace locale, rimuovi tutti i test (cancella bookkeeper/\<modulo\>/src/test/), build locale, inspect target/

---

### Lezione 14 — Unit and Integration Testing: Concepts

- 3 dimensioni ortogonali del test: Level (Unit/Integration/System/Acceptance), Method (BB/WB/Non-functional), Type (Manual/Automated)
- Piramide V&V: Unit+Integration = Development Testing (Verification); System+Acceptance = User Testing (Validation)
- V-model: ogni artefatto di design ha un test corrispondente (Code↔Unit Test; Subsystem Design↔Integration Test; System Requirements↔System Test; Concept of Operations↔Acceptance Test)
- V-model + CT: esecuzione automatica dei test ad ogni commit/PR, al rilascio di nuova funzionalità, ad ogni nuova versione
- Test di unità: rivela malfunzionamenti singolo modulo in isolamento; white-box o black-box; può precedere il codice (TDD); adeguatezza = N funzionalità/requisiti controllati + metriche copertura
- JUnit: framework per IMPLEMENTARE ed ESEGUIRE unit test Java; NON dà indicazioni su strategia di test, quali test usare, quali valori di input selezionare
- JUnit 4: @Test (metodo pubblico), @Before/@After (ogni test, ordine tra più metodi NON specificato), @BeforeClass/@AfterClass (una volta sola per tutti i @Test, ordine NON specificato); test parametrizzato: @RunWith(Parameterized.class), @Parameters→Collection, costruttore con N argomenti
- Responsabilità designer: identificare valori attesi (oracolo o valori puntuali); Responsabilità programmatore: implementare Assert
- Test di integrazione: malfunzionamenti da interazione 2+ moduli; assunzione = unit test già passati; stub = componente del test environment che mima unità mancante (non soggetta a test); test driver = configurazione ambiente + coordinamento risorse + clean-up + logica integrazione

---

### Lezione 17 — Unit and Integration Testing: Frameworks

- Strategie integration testing: big bang (tutto in 1 passo), top-down (interfacce+stub per mancanti, scenari d'uso), bottom-up (unità elementari+test driver, stub per funzionalità mancanti); trovare soluzione ottima è NP-complete
- Strumenti OO per integration testing: JUnit come test driver + Mockito per stubbing/mocking
- Mockito: framework stub/mock Java; dichiarare mock, definire return values, ridefinire comportamenti; supporta TDD/BDD
- Mockito costrutti: mock(), spy(); annotazioni @Mock, @Spy, @Captor, @InjectMocks; Mockito.when().thenReturn(), BDDMockito.given().willReturn(); thenAnswer per comportamenti custom; Mockito.verify(), BDDMockito.then().should(); MockitoJUnitRunner, MockitoJUnit.rule()
- Maven Surefire Plugin: fase `test`; pattern classi: Test*/*Test/*Tests/*TestCase; report in `target/surefire-reports/TEST-*.xml`; pom.xml: `maven-surefire-plugin` 3.0.0-M4; dipendenza JUnit con `<scope>test</scope>`
- Maven Failsafe Plugin: fase `integration-test/verify`; pattern: IT*/*IT/*ITCase; report in `target/failsafe-reports/failsafe-summary.xml`; goals: integration-test + verify
- Raccomandazioni esplicite del prof: unit test via Surefire+CI; integration test via Failsafe+CI; mock via Mockito per unit+integration test

---

### Lezioni 20-23-26 — Test Generation Approaches

- 3 domande fondamentali: quali input? → quando smettere? → come verifico? (oracle problem)
- Category Partition (BB): identificare categorie → scelte per categoria → vincoli tra scelte → frame di test → test concreti
- Classi di equivalenza + BVA: partizionare input in classi equivalenti; testare boundary values (min, max, boundary±1, valore interno)
- Randoop: random test generation automatica; genera sequenze di chiamate API casuali; non LLM-based
- LLM prompting: zero-shot, few-shot, CoT (Chain of Thought), ToT (Tree of Thought); pure-prompting con RAG; esercizio preliminare: 10 prompt (4 zero-shot, 4 few-shot, 2 CoT/ToT) da documentare nel report
- Scale reali: Google 800K builds/day, 150M test runs/day; Bookkeeper 617 test classes, 3228 @Test
- ATTENZIONE: NON giustificare un numero basso di test con "mantenere i costi bassi" — il prof penalizza questa motivazione

---

### Lezioni 29-32 — Test Adequacy: Control Flow Coverage

- Adequacy problem: quanti test sono abbastanza? → risposta tramite criteri di copertura del CFG
- CFG (Control Flow Graph): nodi = blocchi base, archi = trasferimento controllo
- Criteri in ordine crescente di forza: statement coverage → block coverage → branch/decision coverage → condition coverage → BC (Branch+Condition) → MC (Multiple Condition, 2^N test) → MC/DC (N+1 test, standard aviazione DO-178C)
- MC/DC: ogni condizione individuale deve indipendentemente influenzare l'esito della decisione composta; richiede coppie di test che differiscono solo per quella condizione e cambiano l'esito della decisione
- JaCoCo: misura branch coverage; report HTML in `target/site/jacoco/`; integrazione Maven: `jacoco-maven-plugin`
- Procedura MC/DC: T1 (statement/branch adeguato) → T2 (condition adeguato, aggiungi test per condizioni non coperte) → T3 (MC/DC adeguato, aggiungi coppie per ogni condizione indipendente)

---

### Lezione 33 — Mutation Testing (introduzione)

- Mutation testing: terza categoria di criteri di adeguatezza (ortogonale a CF coverage e functional testing)
- Idea: iniettare difetti artificiali (mutanti) nel codice; un test "uccide" un mutante se produce output diverso dal programma originale
- Operatori di mutazione: AOR (aritm.), ROR (rel.), COR (cond.), SOR (shift), LOR (logico), ASR (assign.), SDL (statement deletion), SVR (variable replacement), …
- Schema Offutt: programma originale → operatori → mutanti → test suite → esecuzione → confronto output → killed/survived/equivalent
- Un test suite è mutation-adeguato se uccide tutti i mutanti non equivalenti

---

### Lezione 35 — Mutation Testing Frameworks

- Mutation score formula #1: |D|/(|L|+|D|); formula #2: |D|/(|M|−|E|) dove D=killed, L=survived, M=totale mutanti, E=equivalenti
- Condizioni per killing: C1 raggiungibilità (la mutazione viene eseguita) + C2 infezione (stato del programma cambia) + C3 propagazione (cambiamento arriva all'output)
- Strong mutation: richiede C1+C2+C3; weak mutation: solo C1+C2 (più facile ma meno robusto)
- Equivalenza dei mutanti: indecidibile in generale; CPH (Coupling Hypothesis): test che uccidono mutanti semplici uccidono anche mutanti complessi
- PITest: `org.pitest:pitest-maven:1.5.1:maven-plugin`; goal `mutationCoverage`; report in `target/pit-reports`

---

### Lezione 38 — Coverage-based Approaches to Test Generation

- SBSE (Search-Based Software Engineering): usa metaeuristiche (GA, …) per ottimizzare la generazione automatica di test
- EvoSuite: implementa SBSE; massimizza copertura codice, minimizza numero asserzioni; opera a bytecode Java (no sorgenti SUT); versioni stand-alone, Eclipse, IntelliJ, Maven
- Algoritmo evolutivo: popolazione di test suite → fitness = copertura + lunghezza test; operatori: crossover + mutazione sul test suite — DISTINTO da mutation testing (PITest) che opera sul SUT
- Riferimenti: Fraser & Arcuri ESEC/FSE 2011 (EvoSuite); Fraser & Arcuri QSIC 2011; Fraser & Zeller TSE 2012 (μTEST); Xie 2006

---

### μTEST (Fraser & Zeller TSE 2012) — riferimento L38

- Genera unit test automatici mirati a rilevare mutanti (non solo coprire codice)
- Pipeline: Mutation Analysis + Unit Identification → Test Case Generation (fitness-based) → Oracle Generation
- Fitness function: 1/(Df+Dm) + Im; Df = distanza alla calling function; Dm = approach/branch/necessity distance verso la mutazione; Im = impatto = (c·|C|+r·|A|)/(1+|t|)
- 4 tipi asserzione generati: Primitive (return values), Comparison (compareTo/equals), Inspector (metodi no-side-effect), Field (public primitive fields)
- Sperimentazione: Joda-Time mutation score 80.36% (vs 70.36% test manuali); Commons-Math 65.87% (vs 44.50%)

---

### esempioMCDC (Mathur) — riferimento L29-32

- P7.14 con 3 condizioni: C1 semplice (`while(!done)`); C2 = (x<y) AND (z*z>y) AND (prev=="East"); C3 = (x<y) AND (z*z≤y) OR (current=="South")
- T1 (4 test): statement/branch adeguato, NON condition adeguato
- T2 (5 test = T1+t5): condition adeguato, NON MC/DC adeguato
- T3 (9 test = T2+t6-t9): MC/DC adeguato — ogni condizione ha coppie che variano SOLO quella condizione cambiando l'esito della decisione composta
- MC/DC richiede minimo N+1 test per N condizioni (molto più efficiente di MC = 2^N)

---

## Repository Structure Notes

- Multi-module Maven project; root `pom.xml` aggregates all submodules
- `openjpa-junit5/` — JUnit 5 integration module already present
- `openjpa-lib/`, `openjpa-kernel/`, `openjpa-jdbc/`, `openjpa-persistence/` — core modules likely to contain target classes
- `scripts/` — utility scripts (mostly Windows `.bat`; prefer adding shell equivalents when needed)
