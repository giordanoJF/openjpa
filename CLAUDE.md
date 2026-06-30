# OpenJPA — University Software Testing Project

## Collaboration Rules

1. **Never commit or push without explicit user approval.** Always show what would be committed and ask first.
2. **No AI traces anywhere.** No comments, variable names, commit messages, or report text suggesting AI/external collaboration. LLM use for Test LLM and Copilot variants is documented as methodology — but surrounding code, structure, and report prose must not betray external tooling.
3. **Commits by student only (giordanoJF).** Grouped by task, self-contained. No Claude co-author, no AI in commit messages.

## Testing Methodology Rules (mandatory)

4. **Separate DESIGN from IMPLEMENTATION.** Design (categories, choices, constraints, test frames, abstract specs) must be complete before writing any JUnit. Never collapse phases.
5. **Respect BB vs WB absolutely.** BB: derive from docs/specs/declared interfaces only. WB: base on CFG/branches/paths. Never mix silently.
6. **Precise terminology always.** error (human mistake) → fault (code defect) → failure (observable wrong behavior). Fault necessary but not sufficient for failure. No generic "bug" in report. Testing finds failures; debugging removes faults.
7. **Every test follows SEEV:** Setup → Exercise → Verify (assert) → Teardown. Oracle values from spec/docs, never inferred from code. Applies to all 6 suites.
8. **Oracle problem:** failure = behavior deviating from expected. Without spec/oracle, cannot confirm failure exists. Designer produces input→expected output tuples per test frame. Implementer translates to asserts, never invents values.

## Project Context

University testing campaign on Apache OpenJPA, 2 target classes → detailed LaTeX/PDF report.

3 orthogonal dimensions (Lezione 14): **Level** (Unit primary + Integration) | **Method** (BB manual; WB for coverage iteration) | **Type** (Manual: BB/CF/MT; Automated: RND/ES/LLM). Every test activity classified along all three.

## Development Environment

- **Windows 11 + WSL2** (Ubuntu); all work cross-platform (Linux/macOS/WSL). POSIX paths in scripts.
- Before adding any dependency: verify cross-platform or flag explicitly.

## Target Classes (branch `release-4.1.1`, tag `4.1.1`)

1. `openjpa-kernel/src/main/java/org/apache/openjpa/kernel/BrokerImpl.java`
2. `openjpa-examples/opentrader/src/main/java/org/apache/openjpa/trader/client/LoginDialog.java`

`classes.txt` (alphabetical): `org.apache.openjpa.kernel.BrokerImpl` / `org.apache.openjpa.trader.client.LoginDialog`. Selected via Falessi ISW2 Milestone 4.

## Testing Toolchain

Each tool = specific phase + metric. Do not swap.

- **Maven + Surefire** — unit tests (fase `test`); patterns `Test*/*Test/*Tests/*TestCase`; reports `target/surefire-reports/TEST-*.xml`
- **Maven Failsafe** — integration tests (fase `integration-test/verify`); patterns `IT*/*IT/*ITCase`; reports `target/failsafe-reports/`; goals: `integration-test` + `verify`
- **JUnit 5** — test framework (`openjpa-junit5` module present); course reference = JUnit 4 annotations but JUnit 5 is implementation target
- **Mockito** — stub/mock; `mock()`, `@Mock`, `when().thenReturn()`, `verify()`; `MockitoJUnitRunner`/`MockitoJUnit.rule()`. Mock everything not the SUT in unit tests; in integration tests, mock only modules outside the group being integrated.
- **JaCoCo** — branch coverage (covered/total branches); HTML report `target/site/jacoco/`; plugin: `jacoco-maven-plugin`
- **PITest v1.5.1** — mutation testing; goal `mutationCoverage`; report `target/pit-reports/`; score = |D|/(|M|−|E|) where D=killed, M=total, E=equivalent
- **EvoSuite** — evolutionary test generation via GA on the **test suite** (NOT SUT); bytecode level; fitness = branch coverage + suite compactness; stand-alone JAR or Maven plugin
- **Randoop** — random test generation via API call sequences on compiled class
- **LLM** — prompt-based; multiple strategies (zero-shot/few-shot/CoT/ToT); document every prompt + result + pass/fail
- **CI** (GitHub Actions/TravisCI) — **required, impacts grade**; every commit: build → test → report

When adding any plugin/dependency to `pom.xml`, flag here and propose LaTeX update.

## Professor's Code Examples (`examples/`)

### Lezione14esempiInClasse — JUnit 4 (SUT: stateless `Calculator`)
- **CalculatorTest**: baseline SEEV, `Assert.assertEquals`, fixed oracle values
- **BetterCalculatorTest**: `@BeforeClass`/`@AfterClass` (once/class) + `@Before`/`@After` (once/test)
- **BeforeAfterCalculatorTest**: inherits `@Test` from `CalculatorTest`, overrides Setup/Teardown
- **ParametrizedCalculatorTestAdd/Foo**: canonical oracle pattern — designer's tuples hardcoded in `@Parameters` (e.g. `{1,2},{3,3},{-2,-4},{0,0},{-17,-17}`); implementer writes `assertEquals`. Constructs: `@RunWith(Parameterized.class)`, `@Parameters`, N-arg constructor, `Assert.assertEquals`.

### Lezione17esempiInClasse — Mockito (SUT: `MyAgenda`)
- **AgendaTest**: `@RunWith(MockitoJUnitRunner.class)` + `@InjectMocks` + `@Mock`. Shows `when().thenReturn()`, `lenient().when()`, `verify()`. Critical: `keySet()` not mocked → empty Set → `getAppointments()` returns 0 despite `size()=2`.
- **SimpleAgendaTest**: mocks interface (not impl); `thenAnswer()` for dynamic behavior; multiple `@Before` have no guaranteed order.

### Lezione29-32EsempiInClasse — Unreachable paths
- **UnreachableCodeSimpleExample**: `catch(Exception2)`/`catch(Exception)` unreachable (`op1`/`op2` only throw `Exception1`). 100% branch coverage not always achievable — document, not a coverage failure.

## Mockito Quick Reference

### Dichiarazione
```java
MyClass mock = mock(MyClass.class);   // default: null/0/false/empty
MyClass spy  = spy(new MyClass());    // chiama metodi reali, a meno che stubbati
@Mock    MyDependency dep;            // equivalente a mock()
@Spy     MyDependency dep;            // equivalente a spy()
@Captor  ArgumentCaptor<String> cap; // cattura argomenti per asserirli dopo
@InjectMocks MyClass sut;            // inietta @Mock/@Spy per tipo poi per nome
```

### Stubbing
```java
when(mock.method(arg)).thenReturn(value);
when(mock.method(arg)).thenThrow(new RuntimeException());
when(mock.method(arg)).thenAnswer(inv -> "computed: " + inv.getArgument(0));
when(mock.method(any(Date.class))).thenReturn(value);
lenient().when(mock.method(arg)).thenReturn(value); // stub può non essere chiamato
doReturn(value).when(spy).method(arg);              // per spy: evita chiamata reale
doThrow(new RuntimeException()).when(spy).method(arg);
BDDMockito.given(mock.method(arg)).willReturn(value);
```
**Strict stubbing** (default `MockitoJUnitRunner`): stub non invocato → `UnnecessaryStubbingException`. Usare `lenient()`.

### Verification
```java
verify(mock).method(arg);                  // esattamente 1 volta
verify(mock, times(3)).method(arg);
verify(mock, never()).method(arg);
verify(mock, atLeastOnce()).method(arg);
BDDMockito.then(mock).should().method(arg);
BDDMockito.then(mock).should(never()).method(arg);
```
`verify()` fallisce se metodo non chiamato con **quegli esatti argomenti**.

### Default mock values
| Tipo | Default |
|---|---|
| Object | `null` |
| int/long/double | `0` |
| boolean | `false` |
| Collection/List/Set | vuota (non null) |

**mock** = dipendenza mai da chiamare davvero. **spy** = oggetto reale, stubba solo alcuni metodi; usare `doReturn()` (non `when()`, che chiamerebbe il reale prima dello stub).

## Randoop Quick Reference

- **v4.3.3** (`randoop-all-4.3.3.jar`); Java 8+; docs: https://randoop.github.io/randoop/manualindex.html#running_randoop

```bash
java -Xmx3000m -cp myclasspath:${RANDOOP_JAR} randoop.main.Main gentests \
  --testclass=org.apache.openjpa.kernel.BrokerImpl --output-limit=100

java -cp ${RANDOOP_JAR} randoop.main.Main minimize \
  --suitepath=ErrorTest0.java --suiteclasspath=myclasspath

java -cp ${RANDOOP_JAR} randoop.main.Main help gentests
```

| Flag | Descrizione |
|---|---|
| `--testclass` | FQN singola classe |
| `--classlist` | file con lista classi |
| `--testjar` | JAR classi da testare |
| `--methodlist` | file lista metodi specifici |
| `--omit-methods` | escludi metodi |
| `--omit-methods-file` | file metodi da escludere |
| `--junit-package-name` | package test generati (include nel classpath classe+package+tipi di ritorno) |
| `--output-limit` | max test generati |
| `--timelimit` | timeout sec (default 100) |
| `--junit-output-dir` | output directory |

**3 condizioni per generare test per M:** (1) M referibile via `--testjar/--classlist/--testclass/--methodlist`; (2) M non escluso; (3) classe di M, package e tipo di ritorno nel classpath. Se 0 test generati: verificare queste tre. **WSL**: `;` invece di `:` come separatore classpath.

## Exam Procedure (9 Steps)

### Step 1 — Project
Apply all techniques on **2 classes** of Apache OpenJPA.

### Step 2 — Work environment
GitHub fork + CI (GitHub Actions/TravisCI). **CI impacts grade.** CI+CT: Source control → trigger → Build server (config+build+test) → report → Development → commit → loop. Development does NOT stop during CI (async notification).

### Step 3 — Test experimentation

**3a.** Class selection — done (see Target Classes). Avoid trivial classes.

**3b. Manual tests via Category Partition (→ Test BB)**

**Category partition = metodo, non tecnica** (slide 52): combinabile con manual (→ Test BB) o automatic (→ RND/ES/LLM). Il metodo guida il design; cambia chi genera i test.

**BB rule:** "fortemente raccomandato" (Lezione 2 slide 17). Inferire dal codice ammesso **solo se strettamente necessario** — documentare come eccezione nel report.

**SUT scope = intera classe.** Input dimensions: formal params + object state + persistence state + other instances. Design equivalence classes for ALL.

---

**4 BB sources (legitimate only, in priority order):**
1. **Javadoc/docs** — preconditions, postconditions, declared exceptions, behavior
2. **Declared parameter types** — syntactic starting point; always refine with semantics
3. **Problem domain** — real-world meaning of each parameter
4. **Documented implementation choices** — constants/limits declared in docs

**BB proibito:** aprire sorgente e guardare `if` statements → white-box. Il prof penalizza.

**External dependencies = state categories:** derive partitions from SUT documentation, control via Mockito. Example — BrokerImpl + JPA spec: partition {entity exists / not exists / ctx not active} →
```java
when(mockCtx.find(...)).thenReturn(entityInstance); // entity exists
when(mockCtx.find(...)).thenReturn(null);            // entity not found
when(mockCtx.find(any(), any())).thenThrow(new IllegalStateException()); // ctx not active
```
Missing documentation → "missing oracle" + explicit assumption in report.

---

**Step 1 — Identify input domains:** specs/docs abstractions; declared interface types (BB only); explicit/implicit input conditions.

**Step 2 — Identify equivalence classes** (semantics over syntax always):

| Type | Partitions |
|---|---|
| range (numeric) | in-range + below-min + above-max |
| string | null / "" / valid+correct / valid+incorrect / invalid |
| enum | one class per value |
| array/collection | legal / empty / overflow |
| complex object | null / valid_instance / invalid_instance |
| boolean | {true}, {false} |

**CRITICAL rules:**
- **Semantics > syntax (p.31):** `String password` → {empty, null, valid+correct, valid+incorrect, invalid-domain}
- **Validity ≠ correctness (p.32):** `email="mrfoo@nothing.org"` is VALID but may be CORRECT (user exists) or INCORRECT (user not found) — separate classes
- **Never drop invalid_instance (p.43):** constructor safety not sufficient (code can change; polymorphism bypasses)
- **Tests expecting exceptions are valid (p.20)**

**Professor reference (slide 41) — `asyncReadEntriesInternal(long firstEntry, long lastEntry, ReadCallback cb, Object ctx, boolean isRecoveryRead)`:**

| Parameter | Equivalence classes |
|---|---|
| `isRecoveryRead` | {false}, {true} |
| `cb` | {null}, {valid_instance}, {invalid_instance} |
| `ctx` | {null}, {valid_instance}, {invalid_instance} |
| `firstEntry` | {≤0}, {>0} |
| `lastEntry` | {<firstEntry}, {=firstEntry}, {>firstEntry} |
| `LedgerHandle` (SUT state) | {vuoto}, {ha sufficienti entry}, {non ha sufficienti entry} |

Note: `lastEntry` relative to `firstEntry` (cross-parameter). `LedgerHandle` = SUT state, not a method parameter. LedgerHandle partitions left incomplete by professor (class exercise) — always ask if more semantically distinct states exist.

---

**Step 3 — Combine:** default to **multidimensional** (Cartesian product). Eliminate only logically impossible/semantically meaningless combinations — **document every elimination**. Never default to unidimensional to reduce cost (prof penalizes).

**Step 4 — Eliminate non-admissible:** justification required for every dropped combination.

**Step 5 — BVA (slide 45):**
1. Partizioni già definite (Step 2)
2. Identificare confini di ogni partizione
3. Per ogni confine: valore-al-confine, valore-sotto, valore-sopra
   - **Multidimensionale:** ogni confine in tutte le combinazioni degli altri parametri
   - **Unidimensionale:** ogni confine in almeno una tupla (rischia failure cross-parametro)

Example: `firstEntry` BV: -1; 0; 1. `lastEntry` BV: firstEntry-1; firstEntry; firstEntry+1.
**CRITICAL (p.51):** no a priori reason to exclude e.g. `(firstEntry=1, lastEntry=0)`. When in doubt, add the cross-combination.

---

**Oracle:** expected output from spec. Valid values: return value / state change / exception type / side effect. SEEV structure. Materialize in `@ParameterizedTest`/`@Parameters`. **Do NOT justify low test count with cost** — prof penalizes.

---

**3c. Automatic test generation (3 independent approaches):**

- **Test RND (Randoop):** random API call sequences; detects contract violations. Document: version, config (timeout/limits/seed), tests generated, pass/fail, notable failures.
- **Test LLM:** multiple prompt strategies. Suggested: 10 prompts (4 zero-shot, 4 few-shot, 2 CoT/ToT) from slide 64 — concrete recommendation, not hard requirement (official spec slide 17: "vari tipi di interrogazioni"). For each prompt document: exact text, generated code, compile+pass/fail.
- **Test ES (EvoSuite):** GA on test suite (≠ PITest mutation on SUT); maximizes branch coverage, minimizes suite size; bytecode level. Document: version, config, branch coverage, tests generated.

**3d. Integrate tests into build:** disable/remove native project tests. Unit → Surefire; integration → Failsafe; both on every CI commit. CI must pass before Step 4.

### Step 4 — Quality validation

**4a. Metrics: branch coverage (JaCoCo) + mutation score (PITest)**

Coverage hierarchy: statement → block → branch/decision → condition → BC → MC (2^N) → **MC/DC** (N+1, DO-178C). JaCoCo = branch/decision level.

Iteration (manual suite only):
1. BB → JaCoCo → find red branches → add tests → rerun → coverage ↑ → **Test CF**
2. CF → PITest → find survived mutants (operator/location/text) → add kill tests (C1+C2+C3) → rerun → score ↑ → **Test MT**

Automatic suites (RND/ES/LLM): compute both metrics, no iteration. Compare all 6 suites with concrete reasoning.

**4b. Mutation operators:** AOR, ROR, COR, SOR, LOR, ASR, SDL, SVR.

Kill conditions (strong mutation, PITest): **C1** reachability (statement executed) + **C2** infection (state differs) + **C3** propagation (difference reaches assert). Score = |D|/(|M|−|E|). Equivalence undecidable → use CPH (killing simple mutants sufficient).

**4c. Reliability:** uniform operational profile → reliability = passed/total = 1−PFD. Compute after Step 4b. Disable failing tests only when necessary; always document.

### Step 5 — LLM class variants (Falessi Milestone 4)

Generate **4 variants** (C\_1…C\_4) per class using **Microsoft Copilot** (university account, NOT Claude). Goal: remove SonarCloud smells, preserve functionality.

**Copilot prompt structure:**
1. "You are an expert Java developer. I want to improve the maintainability of the attached C\_0 class…"
2. "Create C\_X without changing C\_0 functionality and by removing the following smells… (SonarCloud diagnostic)"
3. "Make sure C\_X passes the following tests…" ← **key variable** (varies per variant per Table A)
4. "Do not include changes different from what I asked. C\_X should replace C\_0 and work with other components as C\_0 currently does."
5. "This is an important request; take all the time you need for a complete and accurate answer."

**Table A — Generation constraints** (Yes/No = tests included in prompt, NOT pass/fail):

| Variant | BB | CF | MT | RND | ES | LLM |
|---|---|---|---|---|---|---|
| C\_0 | N/A | N/A | N/A | N/A | N/A | N/A |
| C\_1 | No | No | No | No | No | No |
| C\_2 | **Yes** | No | No | No | No | No |
| C\_3 | **Yes** | **Yes** | No | No | No | No |
| C\_4 | **Yes** | **Yes** | **Yes** | No | No | No |

**5a.** Generate new RND/ES/LLM tests for each C\_1…C\_4.

**5b.** Fill (for **each** of the 2 classes):

**Table B — Pass/Fail** (C\_0 tests executed on all versions):

| Tests on C\_0 \ Version | C\_0 | C\_1 | C\_2 | C\_3 | C\_4 |
|---|---|---|---|---|---|
| Test BB | P (o bug) | P/F? | P/F? | P/F? | P/F? |
| Test CF | P (o bug) | P/F? | P/F? | P/F? | P/F? |
| Test MT | P (o bug) | P/F? | P/F? | P/F? | P/F? |
| Test LLM | P (o bug) | P/F? | P/F? | P/F? | P/F? |
| Test RND | P (o bug) | P/F? | P/F? | P/F? | P/F? |
| Test ES | P (o bug) | P/F? | P/F? | P/F? | P/F? |

**Table C — Delta** (new auto tests per variant vs C\_0): Coverage, Mutation Score, Chiarezza, Manutenibilità, Smell, SonarCloud categories.

**Analysis per C\_X:** (1) compila solo e col sistema? (2) ha smell? (rimossi vecchi? nuovi introdotti?) (3) feature positive correlate a bugginess maggiori di C\_0? (4) feature negative correlate a bugginess maggiori di C\_0?

### Steps 6–9
- **Step 6** — Report PDF (~12 pages) — see below
- **Step 7** — `classes.txt`: 2 classi alfabetiche, formato `<package>.<ClassName>`
- **Step 8** — Email a `guglielmo.deangelis@iasi.cnr.it` by deadline su TEAMS
- **Step 9** — Oral presentation; GitHub repo + CI evaluated

---

## The 6 Test Suite Types

| Sigla | Tipo | Come si produce | Metrica | Iterata? |
|---|---|---|---|---|
| **BB** | Black-Box Category Partition | Manual; CP + BVA da specs/docs | N test frames design | No |
| **CF** | BB + branch coverage | BB + JaCoCo: aggiungi test rami scoperti | Branch coverage ↑ | Sì (da BB) |
| **MT** | CF + mutation score | CF + PITest: kill mutanti sopravvissuti | Mutation score ↑ | Sì (da CF) |
| **RND** | Random automatic | Randoop: sequenze casuali API; one-shot | Branch cov + mut score | No |
| **ES** | EvoSuite evolutionary | GA sul test suite (bytecode); one-shot | Branch cov (interno) | No |
| **LLM** | LLM-prompted | 10+ prompt (zero-shot/few-shot/CoT/ToT); one-shot | Branch cov + mut score | No |

Key: BB→CF→MT = same manual suite, 3 maturity stages; MT supersedes. RND/ES/LLM = 3 independent auto suites, one-shot, for comparison. EvoSuite GA ≠ PITest mutation (EvoSuite on test suite; PITest on SUT).

---

## Report Requirements (LaTeX/PDF)

- ~12 A4 pages, single spacing, Arial 10pt. No excessive titles/margins/decoration.
- Figures/tables/listings: append at end, NOT counted toward 12 pp, no inline explanation (discussion in body only).
- Must describe+justify all activities: what/context/problems/methodology (cite professor's frameworks)/results as **concrete numbers** (e.g. "branch coverage 42% → 67%").
- **Terminology:** error / fault / failure (precise, no generic "bug"). Cite Dijkstra: *"testing can show the presence of bugs, not their absence"*.
- **Classify every activity:** Level (Unit/Integration/System/Acceptance) + Method (BB/WB/Non-functional) + Type (Manual/Automated).
- **Adequacy section:** what branch coverage + mutation score measure, why chosen, limits (100% coverage ≠ no faults), what measured values mean for the specific class.

**Workflow:** After each commit, discuss LaTeX changes first — never update silently. Source in `report/`.

---

## LLM Prompt Templates (Professor De Angelis)

**(a) Zero-shot (slide 56):**
```
As a professional software tester who writes Java test methods, generate a complete
JUnit 4 test file ({class_name}Test.java) to comprehensively test all methods in the
following class named {class_name}. Your output file must start with ###Test START##
and finish with ###Test END##. Here is the source code:\n{source_code}.
```
Variante (slide 57): rimuovere `{source_code}` — osservare come cambia output e cosa rivela sull'LLM.

**(b) Few-shot (slide 58):**
```
As a professional software tester who writes Java test methods, consider the following
examples:{fewshot_example}\nNow, generate JUnit 4 test cases to comprehensively test
all methods in the following class named {class_name}. Your output file must start with
###Test START## and finish with ###Test END##. Here is the source code:\n{source_code}.
```

**(c) Guided Tree-of-Thoughts (slide 60):**
```
Imagine three different experts in software testing who are tasked with developing
comprehensive JUnit 4 test cases for the following Java class. To comprehensively test
all methods in the following class named {class_name} they must following these steps:\n
- Extract and list all the public methods including their signatures\n
- For each methods, generate a basic JUnit 4 test case that checks the method's functionality\n
- Given the source code of the class and the listed methods, identify potential edge cases
  and exception handling scenarios that should be tested\n
- Generate JUnit 4 test cases that specifically test for the identified edge cases and exceptions\n
- Merge all the individual test cases into a complete JUnit 4 test file ({class_name}Test.java)
All experts will propose one test case for each method, share it with the group, and then
proceed to the next step. If any expert realizes they're wrong at any point, they leave.\n
The Java class is:\n{source_code}\nAt the end they must propose one complete JUnit 4 test
file. The complete JUnit test file must start with ###Test START## and finish with ###Test END##
```
Guided ToT può includere esempi come nel few-shot.

**Slide 64 — proposta esercizio (10 prompt consigliati, non requisito obbligatorio):**
- 4 zero-shot: variare livello dettaglio, ruolo ("professional software tester"), estensività ("comprehensively"), N test da generare
- 4 few-shot: stesse variazioni + esempi nel prompt
- 2 CoT/ToT: stesse variazioni + indicazioni operative step-by-step

**Documentazione per ogni prompt:** testo esatto, codice generato, compile+pass/fail, ogni aspetto del processo.
**Report (slide 64):** documentare come identificate partizioni e boundary analysis; i prompt con variazioni; suggerimenti test con risultato atteso.

---

## Lecture Context — Prof. De Angelis (ISW2)

### Lezione 5 — Concetti Generali
- V&V: Verification = conforme alle specifiche; Validation = conforme alle attese utente (Boehm)
- D-P-S-C: P soddisfa S sse ∀d∈D, P(d)=S(d). Testing non può provarla → esplora campione di D
- error → fault → failure; fault necessario ma non sufficiente per failure
- 3 strategie: defect prevention | defect reduction (testing+debugging) | defect containment
- SEEV; 3 domande: quali input? (EQ classes) | quando smettere? (coverage) | come verifico? (oracle)
- Correttezza = assoluta (prove formali); reliability = statistico = 1−PFD. Profilo uniforme → reliability = passati/totali
- Dijkstra: "testing can show the presence of bugs, not their absence" — citare nel report

### Lezione 2 — Panoramica
- Testi: Mathur (Pearson 2013); Garcia (Packt 2017); Tahchiev (Manning 2010); Lewis (CRC 2017); Humble & Farley (Addison-Wesley 2011)
- Report: ~12 pp A4, singola interlinea, Arial 10pt; figure/tabelle/listati in fondo, no testo esplicativo inline
- CI impatta valutazione; BB fortemente raccomandato; codice solo se strettamente necessario; profilo operazionale uniforme

### Lezione 8 — SQA, AT, CT
- SQA = Software Testing + Quality Control + SW Config Mgmt; Testing è solo uno degli aspetti
- AT: efficienza+efficacia; riduce costi/durata, aumenta ripetibilità
- CT: test automatici dopo ogni evoluzione; notifiche asincrone (sviluppo NON si interrompe)
- CI+CT: Source control → Build server (config+build+test) → report → Development → commit → loop
- Maven lifecycle: validate→compile→test→package→integration-test→verify→install→deploy (+clean, site)
- CI: Travis-CI, GitHub Actions, GitLab CI-CD, Circle-CI — gratuiti per open-source

### Lezione 11 — Q&A Maven/GitHub/CI
- Esercizio: install MVN → archetype Java → pom.xml → classe → build → GitHub → CI → build automatico
- Secondo esercizio: fork Apache Bookkeeper, rimuovi `src/test/`, build locale, inspect `target/`

### Lezione 14 — Unit e Integration Testing
- 3 dimensioni ortogonali: Level (Unit/Integration/System/Acceptance) / Method (BB/WB/Non-functional) / Type (Manual/Automated)
- V-model: Code↔Unit; Subsystem Design↔Integration; System Req↔System; ConOps↔Acceptance. +CT = esecuzione ad ogni commit/PR/release
- JUnit: implementa ed esegue test Java; NON suggerisce strategia/input/valori. JUnit 4: @Test, @Before/@After (ordine non spec.), @BeforeClass/@AfterClass; parametrizzato: @RunWith(Parameterized.class)+@Parameters+costruttore N arg
- Designer: identifica valori attesi (oracolo); Programmatore: implementa Assert
- Integration test: 2+ moduli; stub = mima unità mancante; test driver = env setup + coord + cleanup + logica integrazione

### Lezione 17 — Framework
- Strategie integration: big bang / top-down (stub per mancanti) / bottom-up (test driver); NP-complete trovare ottimo
- Surefire: fase `test`, pattern Test*/*Test/*Tests/*TestCase, report `target/surefire-reports/TEST-*.xml`
- Failsafe: fase `integration-test/verify`, pattern IT*/*IT/*ITCase, report `target/failsafe-reports/failsafe-summary.xml`
- Raccomandazione prof: unit → Surefire+CI; integration → Failsafe+CI; mock → Mockito in entrambi

### Lezioni 20-23-26 — Test Generation
- Scale reali: Google 800K builds/day, 150M test runs/day; Bookkeeper 617 test classes, 3228 @Test
- **NON** giustificare basso N test con costi — il prof penalizza
- Category Partition = metodo (non tecnica); 5 step: input domain → EQ classes → combina multidim → elimina non ammissibili → BVA → vedi Step 3b per dettaglio completo
- Randoop: sequenze casuali API; controlla violazioni contratti Java (NullPointerException, equals non riflessivo) — NON verifica correttezza logica. Senza violazioni → regression test (cristallizza comportamento). 3 condizioni per M: (1) M referibile; (2) receiver in stato valido; (3) argomenti ammissibili
- LLM pure-prompting schema: PUT → Processed PUT → Prompt+Context → LLM → Raw tests → Validator → Selected/repaired. CoT aggiunge chain-of-thoughts al contesto → vedi sezione LLM Prompt Templates

### Lezioni 29-32 — Control Flow Coverage
- Adequacy: quanti test bastano? → criteri CFG (nodi = blocchi base, archi = trasferimento controllo)
- Gerarchia: statement → block → branch/decision → condition → BC → MC (2^N) → MC/DC (N+1, DO-178C)
- MC/DC: ogni condizione influenza indipendentemente l'esito; richiede coppie che variano SOLO quella condizione
- JaCoCo: `jacoco-maven-plugin`, HTML in `target/site/jacoco/`
- MC/DC procedure: T1 (branch adequate) → T2 (condition adequate) → T3 (MC/DC adequate)

### Lezione 33 — Mutation Testing
- Iniettare difetti artificiali; test "uccide" mutante se output diverso dall'originale
- Operatori: AOR, ROR, COR, SOR, LOR, ASR, SDL, SVR
- Schema Offutt: originale → operatori → mutanti → test suite → esecuzione → killed/survived/equivalent
- Suite mutation-adeguata = uccide tutti i non-equivalenti

### Lezione 35 — Mutation Frameworks
- Score: |D|/(|L|+|D|) oppure |D|/(|M|−|E|); D=killed, L=survived, M=totale, E=equivalenti
- Kill: C1 raggiungibilità + C2 infezione (stato cambia) + C3 propagazione (cambia output)
- Strong mutation: C1+C2+C3; weak: C1+C2 (meno robusto)
- Equivalenza indecidibile; CPH: killing semplici sufficiente
- PITest: `org.pitest:pitest-maven:1.5.1`; goal `mutationCoverage`; report `target/pit-reports`

### Lezione 38 — Coverage-based Test Generation
- SBSE: metaeuristiche (GA) per ottimizzare generazione automatica test
- EvoSuite: massimizza coverage, minimizza asserzioni; bytecode; stand-alone/Eclipse/IntelliJ/Maven
- GA: popolazione test suite → fitness = coverage + lunghezza; crossover + mutazione sul test suite (≠ PITest che opera sul SUT)
- Refs: Fraser & Arcuri ESEC/FSE 2011; QSIC 2011; Fraser & Zeller TSE 2012 (μTEST); Xie 2006

### μTEST (Fraser & Zeller TSE 2012)
- Pipeline: Mutation Analysis + Unit Identification → Test Case Generation → Oracle Generation
- Fitness: 1/(Df+Dm) + Im; Df=dist calling function; Dm=approach/branch/necessity dist verso mutazione; Im=(c·|C|+r·|A|)/(1+|t|)
- 4 tipi asserzione: Primitive (return), Comparison (equals/compareTo), Inspector (no-side-effect), Field (public primitive)
- Risultati: Joda-Time 80.36% (vs 70.36% manuali); Commons-Math 65.87% (vs 44.50%)

### esempioMCDC (Mathur P7.14)
- 3 condizioni: C1=`while(!done)`; C2=(x<y)AND(z*z>y)AND(prev=="East"); C3=(x<y)AND(z*z≤y)OR(current=="South")
- T1 (4 test): branch adequate, NOT condition adequate
- T2 (5=T1+t5): condition adequate, NOT MC/DC adequate
- T3 (9=T2+t6-t9): MC/DC adequate — coppie variano SOLO una condizione cambiando l'esito della decisione
- MC/DC: minimo N+1 test per N condizioni (vs MC=2^N)

---

## Repository Structure
- Multi-module Maven; root `pom.xml` aggrega tutti i submoduli
- `openjpa-junit5/` — JUnit 5 integration module present
- `openjpa-lib/`, `openjpa-kernel/`, `openjpa-jdbc/`, `openjpa-persistence/` — core modules
- `scripts/` — utility scripts (Windows `.bat`; aggiungere shell equivalents se necessario)
