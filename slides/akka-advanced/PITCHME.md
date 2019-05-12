# Akka advanced

---

## Akka logging

It is not recommended to log using blocking facilities.

Akka provides an asynchronous logging backend for minimal performance impact.

---

## Akka logging

```scala
class MyActor extends Actor with ActorLogging {
  override def receive = {
    case msg => log.info(s"Received $msg")
  }
}
```

---

### Akka logging - options

```
akka {
  loglevel = "DEBUG"
  ...
}
```

---

### Akka logging - dead letters

Messages going to the dead letter generate warning messages.

Dead letter might or not indicate a problem and are thus can be disabled.

```
akka {
  log-dead-letters = 10 // on-off-number
  log-dead-letters-during-shutdown = on
}
```

---

# Pattern ask

---

Two communication patterns are available in akka.

- Tell
```
TargetActor ! Message // Unit
```

- Ask
```
TargetActor ! Message // Future[Any]
```

---

# Scheduling



---

# Error handling

 
---
 
 
 
