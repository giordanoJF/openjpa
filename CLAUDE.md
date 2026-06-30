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
8. **Oracle problem (mandatory awareness):** una failure è un comportamento osservabile che devia dal comportamento atteso. In assenza di specifica o oracolo non è possibile stabilire se un output è corretto o scorretto — e quindi non si può nemmeno affermare l'esistenza di una failure. Durante la **fase di progettazione** dei casi di test, il progettista (lo studente) deve produrre esplicitamente le tuple input→output atteso che fungono da oracolo per ogni test frame. Questo lavoro appartiene al progettista, non all'implementatore: chi scrive il codice JUnit traduce quelle tuple in assert, ma non inventa i valori attesi.

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
- **Mockito** — stub/mock for unit and integration tests; use `mock()`, `@Mock`, `when().thenReturn()`, `verify()`; runner: `MockitoJUnitRunner` or `MockitoJUnit.rule()`. Rule: mock everything that is **not the subject of the current test** — in a unit test that means all dependencies of the SUT; in an integration test it means all modules outside the group being integrated (e.g. external services, DB). The tool is the same; what changes is how many real modules are left inside the test.
- **JaCoCo** — measures **branch coverage** (covered branches / total branches, not just line coverage); HTML report in `target/site/jacoco/`; Maven plugin: `jacoco-maven-plugin`
- **PITest v1.5.1** — mutation testing; goal `mutationCoverage`; report in `target/pit-reports/`; mutation score = |D| / (|M| − |E|) where D=killed, M=total mutants, E=equivalent
- **EvoSuite** — evolutionary test generation via genetic algorithm applied to the **test suite** (NOT to the SUT); operates at bytecode level; fitness = branch coverage + test suite compactness; stand-alone JAR or Maven plugin
- **Randoop** — random test generation via random sequences of API method calls on the compiled class
- **LLM (Claude/GPT)** — prompt-based test generation; multiple strategies (zero-shot, few-shot, CoT, ToT); document every prompt + result + pass/fail
- **CI** (GitHub Actions or TravisCI) — **required, impacts final grade**; trigger on every commit: build → test → report

When adding any plugin or dependency to a `pom.xml`, flag it here and propose a LaTeX update.

## Professor's Code Examples (use as reference when writing tests)

All examples are in `examples/` and are written by Prof. De Angelis for this course. Always consult them before writing tests to match the expected style and patterns.

### Lezione14esempiInClasse — JUnit 4 patterns

SUT: `Calculator` (stateless, two methods: `add(double,double)`, `foo(int)`).

- **CalculatorTest**: baseline SEEV structure, `Assert.assertEquals`. Fixed oracle values.
- **BetterCalculatorTest**: adds `@BeforeClass`/`@AfterClass` (once per class) and `@Before`/`@After` (once per test); shows proper Setup/Teardown lifecycle.
- **BeforeAfterCalculatorTest**: inherits `@Test` methods from `CalculatorTest` and overrides Setup/Teardown — shows test class inheritance.
- **ParametrizedCalculatorTestAdd / ParametrizedCalculatorTestFoo**: **canonical oracle pattern** — the designer's input→expected tuples (e.g. `{1,2}, {3,3}, {-2,-4}, {0,0}, {-17,-17}`) are hardcoded in `@Parameters`. This is how Regola 8 is materialized in code: tuples defined by the designer, `assertEquals` written by the implementer.

Key JUnit 4 constructs used: `@RunWith(Parameterized.class)`, `@Parameters`, constructor with N args, `Assert.assertEquals`.

### Lezione17esempiInClasse — Mockito patterns

SUT: `MyAgenda implements SimpleAgenda`; the private `Map<String,String> appointments` field is the mocked dependency.

- **AgendaTest**: `@RunWith(MockitoJUnitRunner.class)` + `@InjectMocks` + `@Mock`. Shows `when().thenReturn()`, `lenient().when()` (stub not required to be called), and `verify()`. **Critical insight**: `simpleTest()` shows that even though the mocked map has `size()=2`, `getAppointments()` returns 0 items because `keySet()` was not mocked — mock only what you control, not the whole object graph.
- **SimpleAgendaTest**: mocks the interface directly (not the implementation). Shows `thenAnswer()` for dynamic/non-deterministic behavior. Shows that multiple `@Before` methods have **no guaranteed execution order**.

### Lezione29-32EsempiInClasse — Unreachable paths

- **UnreachableCodeSimpleExample**: `catch(Exception2)` and `catch(Exception)` blocks are structurally unreachable — `op1`/`op2` only throw `Exception1`. Demonstrates that 100% branch coverage is not always achievable due to SUT structure, and this must be documented (not treated as a coverage failure).

## Mockito Quick Reference (for test writing)

### Dichiarazione di mock e spy

```java
// Programmatica
MyClass mock = mock(MyClass.class);   // mock: tutti i metodi ritornano default (null/0/false/empty)
MyClass spy  = spy(new MyClass());    // spy: chiama i metodi reali, a meno che non siano stubbati

// Via annotazioni (richiedono @RunWith(MockitoJUnitRunner.class) o MockitoJUnit.rule())
@Mock    MyDependency dep;            // equivalente a mock()
@Spy     MyDependency dep;            // equivalente a spy()
@Captor  ArgumentCaptor<String> cap; // cattura argomenti passati al mock per asserirli dopo
@InjectMocks MyClass sut;            // crea l'istanza e inietta @Mock/@Spy nei campi per tipo (poi per nome)
```

`@InjectMocks` funziona per tipo: se hai due mock dello stesso tipo, Mockito li abbina per nome del campo.

### Stubbing (definire comportamento)

```java
// Valore fisso
when(mock.method(arg)).thenReturn(value);

// Eccezione
when(mock.method(arg)).thenThrow(new RuntimeException());

// Comportamento dinamico (l'output dipende dagli argomenti o da logica custom)
when(mock.method(arg)).thenAnswer(invocation -> {
    String a = invocation.getArgument(0);
    return "computed: " + a;
});

// Matcher: accetta qualsiasi istanza di Date
when(mock.method(any(Date.class))).thenReturn(value);

// lenient: rilassa lo strict stubbing — lo stub può non essere mai chiamato senza errore
lenient().when(mock.method(arg)).thenReturn(value);

// Sintassi alternativa (necessaria per spy, evita di chiamare il metodo reale)
doReturn(value).when(spy).method(arg);
doThrow(new RuntimeException()).when(spy).method(arg);

// BDD style (equivalente funzionale)
BDDMockito.given(mock.method(arg)).willReturn(value);
```

**Strict stubbing** (default in `MockitoJUnitRunner`): se uno stub viene dichiarato ma mai invocato, Mockito lancia `UnnecessaryStubbingException`. Usare `lenient()` per i casi in cui è intenzionale.

### Verification (verificare interazioni)

```java
verify(mock).method(arg);                  // chiamato esattamente 1 volta
verify(mock, times(3)).method(arg);        // chiamato esattamente 3 volte
verify(mock, never()).method(arg);         // mai chiamato
verify(mock, atLeastOnce()).method(arg);   // almeno 1 volta

// BDD style
BDDMockito.then(mock).should().method(arg);
BDDMockito.then(mock).should(never()).method(arg);
```

`verify()` fallisce se il metodo non è stato chiamato sul mock **con quegli esatti argomenti**.

### Comportamento default dei mock

| Tipo ritorno | Default |
|---|---|
| Oggetto | `null` |
| `int` / `long` / `double` | `0` |
| `boolean` | `false` |
| `Collection` / `List` / `Set` | collezione vuota (non null) |

Questo spiega `AgendaTest.simpleTest()`: `keySet()` non è stubbato → ritorna `Set` vuoto → il loop non esegue → `getAppointments()` ritorna lista vuota.

### Quando usare mock vs spy

- **mock**: dipendenza che non vuoi mai chiamare davvero (DB, servizio esterno, unità non ancora implementata)
- **spy**: oggetto reale di cui vuoi stubbare **solo alcuni** metodi, lasciando il resto al comportamento reale; usare `doReturn()` per lo stubbing (non `when()`, che chiamerebbe il metodo reale prima dello stub)

## Randoop Quick Reference (for test generation)

- **Versione**: 4.3.3 (`randoop-all-4.3.3.jar`); richiede Java 8+
- **Documentazione completa**: https://randoop.github.io/randoop/manualindex.html#running_randoop

### Comandi principali

```bash
# Genera test (comando principale)
java -Xmx3000m -classpath myclasspath:${RANDOOP_JAR} randoop.main.Main gentests \
  --testclass=org.apache.openjpa.kernel.BrokerImpl \
  --output-limit=100

# Minimizza una suite JUnit che fallisce
java -cp ${RANDOOP_JAR} randoop.main.Main minimize \
  --suitepath=ErrorTest0.java --suiteclasspath=myclasspath

# Help
java -classpath ${RANDOOP_JAR} randoop.main.Main help
java -classpath ${RANDOOP_JAR} randoop.main.Main help gentests
```

### Flag utili per `gentests`

| Flag | Descrizione |
|---|---|
| `--testclass` | Singola classe da testare (fully qualified name) |
| `--classlist` | File con lista di classi da testare |
| `--testjar` | JAR contenente le classi da testare |
| `--methodlist` | File con lista di metodi specifici da testare |
| `--omit-methods` | Esclude metodi specifici dalla generazione |
| `--omit-methods-file` | File con lista di metodi da escludere |
| `--junit-package-name` | Package dei test generati (serve anche per includere nel classpath la classe, il suo package e il tipo di ritorno dei metodi) |
| `--output-limit` | Numero massimo di test generati |
| `--timelimit` | Timeout in secondi (default 100s) |
| `--junit-output-dir` | Directory di output dei test generati |

### Condizioni necessarie perché Randoop generi test per un metodo M

Randoop genera test per M **solo se tutte e tre queste condizioni sono soddisfatte**:
1. M è referibile tramite uno di: `--testjar`, `--classlist`, `--testclass`, `--methodlist`
2. M non è stato esplicitamente escluso tramite `--omit-methods` o `--omit-methods-file`
3. La classe di M, il suo package e il tipo di ritorno di M sono inclusi nel classpath di Randoop (tipicamente via `--junit-package-name`)

**Implicazione pratica**: se Randoop genera zero test o pochi test, verificare prima queste tre condizioni prima di cercare altri problemi.

**Nota su Windows/WSL**: usare `;` invece di `:` come separatore del classpath.

Per i flag completi fare riferimento alla documentazione online.

## Exam Procedure (9 Steps)

### Step 1 — Project
Apply all techniques on **2 classes** of Apache OpenJPA (open-source, Apache Software Foundation, sources on GitHub).

### Step 2 — Work environment
Fork on GitHub + CI framework (GitHub Actions / TravisCI). **CI impacts final grade** — treat it as mandatory.

CI+CT cycle: Source control → trigger → Build server (config + build + test) → report → Development → commit → loop. Development does NOT stop while CI runs (asynchronous notification).

### Step 3 — Test experimentation

**3a. Class selection** — done (see Target Classes above). Avoid trivially simple classes.

**3b. Manual tests via Category Partition (→ Test BB)**

**Category partition è un metodo, non una tecnica** (slide 52). Può essere combinato con:
- **manual software testing** — approccio iniziale per capire il contesto; è il lavoro richiesto da questo corso (→ Test BB)
- **automatic software testing** — strumenti basati su euristiche o AI che applicano lo stesso metodo in modo automatico (→ Test RND/ES/LLM)

Questo spiega perché nel progetto si usano entrambi: il metodo category partition guida il design in ogni caso; ciò che cambia è chi genera i test (lo studente manualmente, o uno strumento automatico).

Black-box: derive everything from documentation, specs, and declared API. The professor says "**strongly recommended**" BB and that inferring from the source code is allowed **only if strictly necessary** (Lezione 2, slide 17). In practice: always start from docs; look at code only as a last resort when no documentation exists, and document that choice explicitly in the report.

**Scope of the SUT**: in this project the SUT is a whole class, not a single method. Input dimensions include: formal method parameters + object state (attribute values before the call) + persistence state + state of other instances in the system. Design equivalence classes for all of these, not just the method signature.

---

**BB sources — where categories and partitions come from**

In black-box you have exactly four legitimate sources. Nothing else.

1. **Javadoc and documentation of the method** — preconditions, postconditions, declared exceptions, behavioral description. If the doc says "throws EntityNotFoundException when entity is not found", that gives you a partition.
2. **Declared type of each parameter** — gives the syntactic starting point (String → null/empty/non-empty; int → negative/zero/positive). Always refine with semantics.
3. **Problem domain** — what does this parameter represent in the real world? A `String email` is not a generic string; the domain tells you syntactically-valid-but-user-not-found is a distinct partition from syntactically-valid-and-user-exists.
4. **Documented implementation choices** — if the doc says "maximum 100 elements" or "buffer of size N", those constants define partition boundaries even though they are not domain concepts.

**What you CANNOT do in BB:** open the source code and look at `if` statements. If you see `if (x > 0)` in the body and build a partition from it, you are doing white-box, not black-box. The professor penalizes this.

**External dependencies (DB, other classes) become state categories**

If the SUT depends on a DB or another class, that external state is a category to partition, exactly like a method parameter. You derive the partitions from the documentation of what the SUT does with that dependency — not from the dependency's implementation.

Example: BrokerImpl depends on a persistence context. The JPA spec says "find() returns null if the entity does not exist" → partition: {entity exists, entity does not exist, persistence context not active}.

In the JUnit test you control those partitions via Mockito:
- partition "entity not found" → `when(mockCtx.find(...)).thenReturn(null)`
- partition "entity found" → `when(mockCtx.find(...)).thenReturn(entityInstance)`
- partition "not active" → `when(mockCtx.find(...)).thenThrow(new IllegalStateException())`

If documentation is absent for a method, document it in the report as "missing oracle" and state the assumption you used explicitly.

---

**Step 1 — Identify input domains**

Sources (in priority order):
- Key abstractions, features, needs, requirements from specs/docs
- Data types linked to the declared interface (BB only — not from implementation body)
- Explicit or implicit conditions on inputs

---

**Step 2 — Identify equivalence classes per parameter**

For each parameter/dimension, partition its value space. Apply these guidelines by type — but always prioritize **semantics** over syntax:

| Type | Guideline |
|---|---|
| **range** (numeric) | one value IN range + at least two values OUTSIDE (below min, above max) |
| **string** | set of all-valid strings; set of all-invalid strings; empty string ""; null |
| **enumeration** | one equivalence class per distinct enum value |
| **array / collection** | all-legal elements; all-empty; all elements exceed max allowed length |
| **complex object** | apply criteria iteratively to each field; always include null |
| **boolean** | {true}, {false} |

**CRITICAL — semantics over syntax (p.31):** type-based guidelines are a starting point, not the final word. Significant equivalence classes come from the MEANING of the parameter in context. Example:
- `String password` syntactically → {"", null, non-empty}
- `String password` semantically → {"" (empty), null, valid-in-domain+correct, valid-in-domain+incorrect, invalid-in-domain}

**CRITICAL — validity ≠ correctness (p.32):** do not confuse syntactic validity of a value with its correctness relative to the SUT's current state. Example: `email="mrfoo@nothing.org"` is VALID (well-formed), but may be CORRECT (user exists) or INCORRECT (user not found) depending on SUT data. These are separate equivalence classes.

**CRITICAL — never exclude "invalid_instance" for complex types (p.43):** you cannot justify excluding invalid object instances with "the constructor always returns correct instances" because: (1) constructor code could change; (2) polymorphism may allow subclass instances from different constructors. Always include invalid instances unless the API makes them structurally impossible.

**CRITICAL — valid tests can expect failure (p.20):** a test whose expected output is an exception or error is valid and meaningful. Document it explicitly as such in the oracle.

**Reference example from professor (slide 41) — `asyncReadEntriesInternal(long firstEntry, long lastEntry, ReadCallback cb, Object ctx, boolean isRecoveryRead)`:**

| Parameter | Equivalence classes |
|---|---|
| `isRecoveryRead` | {false}, {true} |
| `cb` | {null}, {"valid_instance"}, {"invalid_instance"} |
| `ctx` | {null}, {"valid_instance"}, {"invalid_instance"} |
| `firstEntry` | {≤0}, {>0} |
| `lastEntry` | {<firstEntry}, {=firstEntry}, {>firstEntry} |
| `LedgerHandle` (SUT state) | {vuoto}, {ha sufficienti entry}, {non ha sufficienti entry} |

Key patterns to notice:
- `lastEntry` partitions are defined **relative to another parameter** (`firstEntry`), not as absolute values — cross-parameter semantics
- `LedgerHandle` is a state category of the SUT, not a method parameter — confirms that SUT state is always a category
- complex types (`cb`, `ctx`) always include {null}, {"valid_instance"}, {"invalid_instance"} — never drop invalid_instance

**Note:** the professor left `LedgerHandle` partitions ({vuoto}, {ha sufficienti entry}, {non ha sufficienti entry}) deliberately incomplete as a class exercise. They are a starting point, not the final answer. When designing partitions for a stateful dependency, always ask whether there are more semantically distinct states not yet captured.

---

**Step 3 — Combine equivalence classes**

Two strategies:
- **Unidimensional**: each parameter treated independently; select tests to cover all equivalence classes of each parameter. Fewer tests, but may miss cross-parameter interactions.
- **Multidimensional**: Cartesian product across all parameters. More thorough but combinatorially expensive — use Step 4 to reduce.

Do not default to unidimensional to limit cost — the professor penalizes this. Start multidimensional and eliminate only truly non-admissible combinations.

---

**Step 4 — Eliminate non-admissible combinations**

Remove combinations that are logically impossible or semantically meaningless per the spec. Document every elimination with a justification — never silently drop a combination.

---

**Step 5 — Apply BVA (Boundary Value Analysis)**

Empirical evidence: most faults appear at boundary values of equivalence classes. After defining classes, identify boundaries and select values at them.

BVA schema (slide 45):
1. Partizionare il dominio per ogni parametro osservato (already done in Step 2)
2. Identificare i confini di ogni partizione
3. Selezionare i valori in modo che ogni confine occorra:
   - **multidimensionale**: ogni confine in tutte le possibili combinazioni degli altri parametri (più thorough, più test)
   - **unidimensionale**: ogni confine in almeno una tupla di input (meno test, ma può perdere failure cross-parametro)

Example (firstEntry, lastEntry as indices):
- `firstEntry` BV: -1; 0; 1
- `lastEntry` BV relative to firstEntry: firstEntry-1; firstEntry; firstEntry+1

**CRITICAL (p.51):** unidimensional BVA (each boundary in at least one tuple) may limit failure exposure. No a priori reason to exclude combinations like `(firstEntry=1, lastEntry=0)` — they could reveal important failures. When in doubt, add the cross-combination rather than drop it.

---

**Oracle and test structure**

- Expected output comes from the specification. If none exists, document the assumption explicitly.
- Valid oracle values: return value, state change, exception type, side effect.
- Each test follows SEEV: Setup → Exercise → Verify (assert) → Teardown.
- Materialize oracle tuples in `@ParameterizedTest` / `@Parameters` (canonical pattern from professor's examples).

Do NOT justify a low test count with cost considerations — the professor explicitly penalizes this.

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
- Scale reali: Google 800K builds/day, 150M test runs/day; Bookkeeper 617 test classes, 3228 @Test
- ATTENZIONE: NON giustificare un numero basso di test con "mantenere i costi bassi" — il prof penalizza questa motivazione

**Category Partition (BB) — 5 step:**
1. Identificare dominio di input (parametri + stato oggetto + persistenza + altri oggetti)
2. Identificare classi di equivalenza per parametro (linee guida per tipo: range → 1 in + 2 out; string → valida/invalida/vuota/null; enum → 1 classe per valore; array → legale/vuoto/overflow; complex → iterativo + null)
3. Combinare classi: unidimensionale (indipendente per parametro) o multidimensionale (prodotto cartesiano); default a multidimensionale e ridurre solo per combinazioni non ammissibili giustificate
4. Eliminare combinazioni non ammissibili (documentare ogni eliminazione)
5. Applicare BVA: per ogni boundary → valore-al-boundary, valore-sotto, valore-sopra

**Regole critiche equivalence class:**
- **Semantica sopra sintassi**: le classi devono riflettere il SIGNIFICATO del parametro, non solo il tipo. Esempio: `String password` → {vuota, null, valida+corretta, valida+scorretta, invalida}
- **Validità ≠ correttezza**: un valore sintatticamente valido può essere CORRETTO (esiste nel SUT) o INCORRETTO (non esiste). Sono classi separate
- **Non escludere invalid_instance**: non giustificare l'esclusione con "il costruttore garantisce correttezza" — polimorfismo può bypassarlo
- **Test validi possono aspettarsi fallimenti**: un test con oracolo = eccezione è valido e significativo
- **SUT = intera classe**: dominio di input include stato, persistenza, altri oggetti — non solo parametri del metodo chiamato
- **BVA unidimensionale avverte**: coprire ogni boundary in almeno un test può non bastare; le combinazioni cross-parametro possono rivelare failure importanti

**Randoop:**
- Genera sequenze casuali di chiamate API; esegue; controlla violazioni di contratti Java (NullPointerException non attesa, equals non riflessivo, ecc.) — NON verifica correttezza logica del SUT
- "Contract violation" = violazione di invariante Java universale (non bug di logica applicativa)
- Sequenza senza violazioni → regression test (cristallizza comportamento attuale, non correttezza vs spec)
- 3 condizioni necessarie per testare metodo M: (1) M deve poter essere chiamato nella sequenza; (2) il receiver deve essere in uno stato valido per M; (3) gli argomenti devono essere ammissibili
- Comandi: `randoop.jar randoop.main.Main gentests --testclass=<FQN> --time-limit=<sec> --output-limit=<n>`

**LLM prompting — pure-prompting schema (slide 55, 59):**
PUT (Program Under Test) → Processed PUT → Prompt + Context → LLM → Raw generated tests → Validator → Selected and repaired tests. CoT aggiunge "chain of thoughts" al contesto prima del prompt.

**Template dei prompt dal professore (da usare come riferimento):**

**(a) Zero-shot (slide 56):**
```
As a professional software tester who writes Java test methods, generate a complete
JUnit 4 test file ({class_name}Test.java) to comprehensively test all methods in the
following class named {class_name}. Your output file must start with ###Test START##
and finish with ###Test END##. Here is the source code:\n{source_code}.
```
Variante interessante (slide 57): rimuovere `{source_code}` dal prompt — studiare come cambia l'output e cosa rivela sull'LLM utilizzato.

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
NOTA: Guided ToT può includere esempi come nel few-shot (non inclusi in questo template).

**Numero di prompt — proposta esercizio preliminare (slide 64):** il professore propone un insieme di **10 prompt** come esercizio strutturato consigliato (non requisito stretto, ma raccomandazione operativa concreta):
- **4 zero-shot**: variare le informazioni qualitative del contesto — livello di dettaglio dei test, chi li sta chiedendo ("professional software tester"), quanto estensiva la ricerca ("comprehensively"), numero di test da generare, ecc.
- **4 few-shot**: stesse variazioni dello zero-shot + aggiungere un numero arbitrario di esempi nel prompt
- **2 CoT/ToT**: stesse variazioni + aggiungere indicazioni operative su come procedere step by step

**Documentazione richiesta per ogni prompt:** testo esatto del prompt, codice generato, esito compile + pass/fail, ogni aspetto del processo di generazione.

**Piano di descrizione nel report (slide 64):** documentare come sono state identificate le partizioni e come è stata condotta la boundary analysis; i prompt con le loro variazioni; suggerimenti preliminari di test da includere con risultato atteso.

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
