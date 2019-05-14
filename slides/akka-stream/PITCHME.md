
# Akka-stream

---

## Naive data processing
```scala
def saveToDb(input: String): Future[String]

val lines: Iterator[String] = Source.fromFile("big-input.csv").getLines()

while (lines.nonEmpty) {
  saveToDb(lines.next())
}
```

---

Two different scenarios might present themselves

- The consumer is faster than the producer
- The producer is faster than the consumer

---

### Slow producer - fast consumer

In the case, data streaming will work smoothly.

This is rarely given in time for any big enough system. Production and/or consumption might vary because of external actors. 

---

### Fast producer - slow consumer

For a big enough dataset, one of the following errors will eventually arise.

- Out of memory
- Out of threads
- A safeguard error (jdbc connexions, http connexions, open file-descriptors, etc.)

---

### The issue

The **throughput** of the data flow is controlled by the producer.

The consumer **endure** the producer's speed

---

### Mauvaises solutions

- "artificial" throttling of the producteur
- Tedius manual scaling of underlying hardware
- Increasing the buffer sizes

---

### Solution

The data flow must be regulated according to the **consumers** capacity.

The is called **backpressure**.

When not regulating the latter, correct data processing depends on **dynamic external factors** (dataset, service speeds, etc.)

---

## Flow API

The first usage level of akka-stream.

Will suffice for most use-cases while being very expressive.

This kind of API is found in many other reactive streaming libraries.

---

### Flow API - limitations

Only models **linear** dataflows.


```
Source ==> transformation ==> transormation ==> Sink
```

---

## Graph API

Second usage level of akka-stream.

As it name suggests, this API allows to model graphs.

```
Source1 ==>                ==> transormation1 
Source2 ==> transformation ==> transormation2 ==> Sink
Source3 ==>                
```

---

### Graph API

- Mutable API
- Only partially compile-time verified
- Can model deadlocks
- Very powerful (Broadcast, Partition, Bidirectional, etc.)

---

## GraphStage Api

Third usage level of akka-stream.

If **existing stages** of the graph api are not enough.

Custom stagers are usually stateful (mutable).

Usage can be very tricky
- Callbacks
- Side-effects
- Manual pull/push
- Manual completion (success/failure)
- Etc.

---

### GraphStage Api - examples

- Element counter
- Velocity monitoring
- Aggregation of any kind

---

## Closing notes

- **Always** consider backpressure
- **Always** use the simplest api (mainly h=the flow api)
- **Never** block (just like akka-actors)
- **Do not** start by optimising, a simple flow with default parameters will be enough 
