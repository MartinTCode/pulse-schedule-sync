# pulse-schedule-sync

pulse-schedule-sync is a proof-of-concept system integration tool for synchronizing finalized schedules from an external scheduling system into a calendar platform.

The application retrieves schedule data, allows a responsible user to review and adjust event details (such as location and description), and then publishes the events to a target calendar via API.

## Key Features

- Fetches schedule data from an external scheduling source

- User review and manual adjustment step before publishing

- Automated creation of calendar events via REST API

- Server-side integration layer acting as an API proxy

- Desktop UI for previewing and editing events

## Architecture Overview

- JavaFX desktop client for user interaction

- Embedded REST server (JAX-RS / Jersey) for integration logic

- Service-oriented design with clear separation between UI, domain, and integrations

- Optional persistence layer for drafts, mappings, or transfer history

## Tech Stack

- Java 21

- JavaFX

- JAX-RS (Jersey) with embedded HTTP server

- PostgreSQL (optional)

- JPA / Hibernate

- Flyway

- Maven

## Scope

This project is intended as a technical demonstration of:

- System integration

- Service-oriented architecture principles

- API orchestration

- Clean layering between UI, services, and external systems

It is not a production-ready system, but a focused and extensible integration prototype.
