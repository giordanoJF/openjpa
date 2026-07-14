## Zero-shot (2)

### Z1 — baseline
As a professional software tester who writes Java test methods, generate a complete JUnit 4 test
file (LoginDialogTest.java) that covers 10 methods of your choice in the following class named
LoginDialog, using at most 20 @Test-annotated methods in total. Your output file must start with
###Test START## and finish with ###Test END##. The source code is attached.

### Z2 — focus su test funzionali, non basati sull'implementazione, con nota sull'ambiente
As a professional software tester who writes Java test methods, generate a complete JUnit 4 test
file (LoginDialogTest.java) that covers 10 methods of your choice in the following class named
LoginDialog, using at most 20 test methods in total. Focus specifically on functional, black-box
test cases based on the documented and expected behavior of each method (including edge cases and
exception handling), not on internal implementation details. {ENVIRONMENT} Your output file must
start with ###Test START## and finish with ###Test END##. The source code is attached.

---

## Nota sull'ambiente (inserita in Z2/F1/F2/C1/C2 al posto di {ENVIRONMENT})

Environment note: LoginDialog extends GWT's PopupPanel and can only be constructed and exercised
inside GWT's simulated test environment, not in a plain JUnit runner. The test class must extend
com.google.gwt.junit.client.GWTTestCase (not use @Test annotations) and override
public String getModuleName() to return "org.apache.openjpa.trader.LoginDialogTest". Test methods
are plain no-argument public void methods named testXxx() (JUnit 3 style: this version of GWT
predates JUnit 4 annotation support). Mockito cannot be used inside these test methods, since GWT
compiles them to JavaScript. Two hand-written substitute classes are already on the classpath:
OpenTrader, with public fields initCalled, initTrader, initServerURI, initStocks, lastError,
errorCount and methods getService()/setService(TradingServiceAdapterAsync), stands in for the
real collaborator LoginDialog's constructor requires; NoOpServiceAsync implements
TradingServiceAdapterAsync, with only getStocks(AsyncCallback) as a no-op and every other method
throwing UnsupportedOperationException. GetServerURI and InitializeStocks are public non-static
inner classes of LoginDialog, and can be instantiated directly from a test as
dialog.new GetServerURI().

---

## Few-shot (2)

```java
public class Calculator {
    public int add(int a, int b) { return a + b; }
}

public class CalculatorTest {
    @Test
    public void testAdd() {
        Calculator calc = new Calculator();
        assertEquals(5, calc.add(2, 3));
    }
}
```

```java
public class Calculator {
    public int divide(int a, int b) {
        if (b == 0) {
            throw new ArithmeticException("division by zero");
        }
        return a / b;
    }
}

public class CalculatorTest {
    @Test(expected = ArithmeticException.class)
    public void testDivideByZero() {
        Calculator calc = new Calculator();
        calc.divide(10, 0);
    }
}
```

### F1 — baseline, un esempio, con nota sull'ambiente
As a professional software tester who writes Java test methods, consider the attached example.
Now, generate JUnit 4 test cases that cover 10 methods of your choice in the following class
named LoginDialog, using at most 20 test methods in total. {ENVIRONMENT} Your output file must
start with ###Test START## and finish with ###Test END##. The source code is attached.

### F2 — più esempi, con nota sull'ambiente
As a professional software tester who writes Java test methods, consider the two attached
examples of well-written JUnit 4 test cases, one with a boundary value and one that expects an
exception. Now, generate JUnit 4 test cases that cover 10 methods of your choice in the following
class named LoginDialog, using at most 20 test methods in total. {ENVIRONMENT} Your output file
must start with ###Test START## and finish with ###Test END##. The source code is attached.

---

## CoT / Guided ToT (2)

### C1 — baseline, con nota sull'ambiente
You are a professional software tester. Before writing any code, think step by step:
1. Choose 10 methods of the following class named LoginDialog that you consider worth testing,
   and briefly explain why for each one.
2. For each chosen method, write down what you expect it to do and one edge case or exception
   that could occur.
3. Only after completing steps 1 and 2, write a complete JUnit 4 test file (LoginDialogTest.java)
   using at most 20 test methods in total, one or more per method chosen in step 1, based on what
   you wrote down in step 2.

{ENVIRONMENT}

Show your reasoning for steps 1 and 2 before the code. Your output file must start with ###Test
START## and finish with ###Test END##. The Java class is attached.

### C2 — stesso schema: esempio incluso con nota sull'ambiente
You are a professional software tester. An example of a well-written JUnit 4 test case is
attached as a style reference. Before writing any code, think step by step:
1. Choose 10 methods of the following class named LoginDialog that you consider worth testing,
   and briefly explain why for each one.
2. For each chosen method, write down what you expect it to do and one edge case or exception
   that could occur.
3. Only after completing steps 1 and 2, write a complete JUnit 4 test file (LoginDialogTest.java)
   using at most 20 test methods in total, one or more per method chosen in step 1, based on what
   you wrote down in step 2.

{ENVIRONMENT}

Show your reasoning for steps 1 and 2 before the code. Your output file must start with ###Test
START## and finish with ###Test END##. The Java class is attached.

---
