# Esecuzione dei test

Il progetto non utilizza un sistema di build preconfigurato, quindi è stato incluso un runner minimale basato su un'annotazione `@Test` compatibile con JUnit 5 per eseguire i casi di test senza dipendenze esterne.

## Compilazione
```bash
javac -d out/test-classes \
  src/test/java/org/junit/jupiter/api/*.java \
  src/it/unicas/project/template/address/util/BudgetNotificationPreferences.java \
  src/it/unicas/project/template/address/util/DateUtil.java \
  src/test/java/it/unicas/project/template/address/util/*.java \
  src/test/java/TestRunner.java
```

## Esecuzione
```bash
java -cp out/test-classes TestRunner
```

Il runner stampa l'esito di ciascun test e restituisce un codice di uscita diverso da zero in caso di fallimento.
