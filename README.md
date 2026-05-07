# TCP-to-HTTP Translator Service

## Overview

This project is a Spring Boot based TCP-to-HTTP Translator Service.

The application accepts plain text messages over TCP, validates and converts them into JSON format, forwards the data to an HTTP endpoint, and returns acknowledgements back to the TCP client.

The service also stores the last 100 processed messages in memory and exposes them through a REST API.

---

# Technologies Used

- Java 21
- Spring Boot
- Reactor Netty TCP Server
- Spring WebFlux WebClient
- JUnit 5
- Mockito

---

# Features

- TCP server running on configurable port
- Message validation
- TCP message to JSON conversion
- HTTP forwarding using WebClient
- Retry once if HTTP call fails
- Stores last 100 processed messages
- REST API to retrieve processed messages
- Unit tests for parsing and HTTP forwarding

---

# TCP Message Format

Input message format:

```text
deviceId|metric|value
