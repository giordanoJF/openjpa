## Zero-shot (2)

### Z1 — baseline (template del corso)
As a professional software tester who writes Java test methods, generate a complete JUnit 4 test
file (BrokerImplTest.java) that covers 10 methods of your choice in the following class named
BrokerImpl, using at most 20 @Test-annotated methods in total. Your output file must start with
###Test START## and finish with ###Test END##. The source code is attached.

### Z2 — focus su test funzionali, non basati sull'implementazione
As a professional software tester who writes Java test methods, generate a complete JUnit 4 test
file (BrokerImplTest.java) that covers 10 methods of your choice in the following class named
BrokerImpl, using at most 20 @Test-annotated methods in total. Focus specifically on functional,
black-box test cases based on the documented and expected behavior of each method (including
edge cases and exception handling), not on internal implementation details. Your output file
must start with ###Test START## and finish with ###Test END##. The source code is attached.

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

### F1 — baseline
As a professional software tester who writes Java test methods, consider the attached example.
Now, generate JUnit 4 test cases that cover 10 methods of your choice in the following class
named BrokerImpl, using at most 20 @Test-annotated methods in total. Your output file must start
with ###Test START## and finish with ###Test END##. The source code is attached.

### F2 — più esempi
As a professional software tester who writes Java test methods, consider the two attached
examples of well-written JUnit 4 test cases, one with a boundary value and one that expects an
exception. Now, generate JUnit 4 test cases that cover 10 methods of your choice in the
following class named BrokerImpl, using at most 20 @Test-annotated methods in total. Your output
file must start with ###Test START## and finish with ###Test END##. The source code is attached.

---

## CoT / Guided ToT (2)

### C1 — baseline
You are a professional software tester. Before writing any code, think step by step:
1. Choose 10 methods of the following class named BrokerImpl that you consider worth testing,
   and briefly explain why for each one.
2. For each chosen method, write down what you expect it to do and one edge case or exception
   that could occur.
3. Only after completing steps 1 and 2, write a complete JUnit 4 test file (BrokerImplTest.java)
   using at most 20 @Test-annotated methods in total, one or more per method chosen in step 1,
   based on what you wrote down in step 2.

Show your reasoning for steps 1 and 2 before the code. Your output file must start with ###Test
START## and finish with ###Test END##. The Java class is attached.

### C2 — stesso schema, con esempio incluso
You are a professional software tester. An example of a well-written JUnit 4 test case is
attached as a style reference. Before writing any code, think step by step:
1. Choose 10 methods of the following class named BrokerImpl that you consider worth testing,
   and briefly explain why for each one.
2. For each chosen method, write down what you expect it to do and one edge case or exception
   that could occur.
3. Only after completing steps 1 and 2, write a complete JUnit 4 test file (BrokerImplTest.java)
   using at most 20 @Test-annotated methods in total, one or more per method chosen in step 1,
   based on what you wrote down in step 2.

Show your reasoning for steps 1 and 2 before the code. Your output file must start with ###Test
START## and finish with ###Test END##. The Java class is attached.
