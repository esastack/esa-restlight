ESA Restlight is a lightweight and rest-oriented web framework, which supports annotations of SpringMVC and JAX-RS. 

## Why Restlight?

In microservices, it is generally expected that service is a lightweight application, and the Spring-Web framework we often use is really a good framework that allows us to develop a variety of applications easily, but it is too bloated for applications in microservices, which just need a rest supports and do not need the session, ModelAndview, JSP and so on... Restlight aims to serve as a web framework that helps users to build a high performance and lightweight microservice.

## What kind of web application is Restlight suitable for?

- Rest application
- High-performance requirements
- Middleware
- HTTP proxy
- Any application that needs HTTP services

## What kind of web application is Restlight not suitable for?

- All in one application
- Servlet requirements: Restlight does not support servlet standards

## Env Requirements

| Name        | Version        |
| ----------- | -------------- |
| Java        | JDK8+          |
| Spring Boot | 2.1.0.RELEASE+ |

## Features

- Annotations of SpringMVC and JAX-RS supports
- High performance
- Reactive：`CompletableFuture`、`ListenableFuture(Guava)`， `Future(Netty)`
- Threading-model: Flexible scheduling between IO EventLoopGroup and Biz Schedulers.
- Http1/Http2/H2c/Https
- HAProxy
- Filter
- Interceptor
- JSR-303: hibernate-validator
- Self-protection: Connection creation limit, Cpu Load protection
- more ...

## Release Notes

[Releases](https://github.com/esastack/esa-restlight/releases)
