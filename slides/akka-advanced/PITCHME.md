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

All the usual methods: `debug`, `info`, `warning`, `error`

---

### Akka logging - options

Akka configuration resides inside `application.conf`

```
akka {
  loglevel = "DEBUG"
  log-config-on-start = on
  actor.debug {
    receive = on
    lifecycle = on
  }
  ...
}
```

---

### Akka logging - dead letters

Messages going to the dead letter generate warning messages.

Dead letter might or not indicate a problem and thus logging can be disabled.

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
// Fire-and-forget
targetActor ! Message // Unit
```

- Ask
```
// Waiting
targetActor ? Message // Future[Any]
```

---

### Usage

```scala
import akka.pattern.ask

// Timeout is mandatory, no default value
implicit val timeout: Timeout = Timeout(5.seconds)
targetActor ? Message
```

---

### Completing the *ask future*

```scala
override def receive = {
  case Person(name) => 
    doingSomething(name)
    sender() ! Finished // Completes the senders future
}
```

If no response is sent, the future will timeout

---

### Ask response

The ask response is of type **Any**
```scala
(targetActor ? Message)
  .map {
    case Person(name) => doSomething(name)
  }

```

```scala
(targetActor ? Message).mapTo[Person].map(person => doSomething(person.name))
```

---

### Ask performance

Ask has performance implications
- Simulating an ActorRef for future completion
- Timer for the eventual timeout

Using ask is mostly justified when calling an actor **outside** an actor context.

```scala
object Start extends App {
  // Not inside an actor, we do not have an ActorRef to respond to !
  (targetActor ? Message).map(???)
}
```

---

# Scheduling

---

Akka provides an efficient scheduling facility.

- Very low overhead
- Handles high amounts of timers in parallel

---

### Scheduling - limitations

- Not for long term triggers
- Not precise (events are batch-triggered)

Default tick is **10ms**, going below is not recommended (needs JVM and OS tuning).
```
// Increase time to increase throughput
akka.schedular.tick-duration = 50ms
```

Never rely on exact scheduling.

---

### Scheduling - Usage

Available through the ActorSystem.
```scala
context.system.scheduler.schedule(
  initialDelay = 0.seconds,
  interval = 1.second,
  receiver = self,
  message = Heartbeat
)

context.system.scheduler.scheduleOnce(...)
```

---

# Error handling
 
---


### Let-it-crash

Akka embraces failures as a fact of life.

You shall not try to avoid failures but handle them. Create a *self-healing* system.

---

### SupervisionStrategy
 
- Each actor has a *supervisor*
- Each supervisor has a *supervision strategy*
- The supervision strategy handles failures of child actors

---

### SupervisionStrategy

The supervision strategy can handle failures in different ways
- Restart: The failed child actor is replaced by a new one  
- Resume: The child actor continues processing new messages
- Stop: The child actor is stopped permanently
- Escalate: Let the error bubble up the hierarchy

---

### Restart and Resume

- When restarting or resuming the message that caused the failure is skipped.
- The failure causing message is available when restarting (preRestart)
- Restart and resume are invisible to the rest of the world, the same ActorRef is used. 

---

### Strategy types

- One-for-one: Each actor is handled separately
- All-for-one: When a child actor fails all children are affected

You will mostly use **one-for-all**. **all-for-one** makes sense if the actors states are coupled.

---

### Default strategy

- ActorInitializationException: stop the failing child actor
- ActorKilledException: stop the failing child actor
- DeathPactException: stop the failing child actor
- Exception: restart the failing child actor
- Throwable: escalated to parent actor

---

### Usage

```scala
class Supervisor extends Actor {
  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1.minute) {
      case TimeoutException => Resume 
      case _                => Escalate
    }
}
```

---

### Final words

- **Do not block**, not even for logging
- Use **tell** over **ask** whenever possible
- Do not rely on scheduling precision
- **Let-it-crash** and recover