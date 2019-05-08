
# Akka-stream


## Consommation naïve
```scala
def saveToDb(input: String): Future[String] = ???

val lines: Iterator[String] = Source.fromFile("big-input.csv").getLines()

while (lines.nonEmpty) {
saveToDb(lines.next())
}
```

## Problèmes rencontrés