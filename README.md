# NovaDB Engine

An AI-Ready Open Source Database Engine built from scratch in Java.

## Vision
NovaDB Engine is an open-source database engine project. Our goal is to build a fast, secure, intelligent, and modern database engine from the ground up.

## Project Goals
- Build our own storage engine
- Design our own SQL parser
- Develop a high-performance query engine
- Integrate AI features
- Build a cross-platform database system

## Project Status
🚧 **Phase 1: Storage Manager Design & Scaffolding**

## Architecture
NovaDB Engine follows a layered architecture:
1. **Storage Manager**: Handles disk I/O, page management, and buffer pooling.
2. **Catalog Manager**: Manages metadata and schemas.
3. **Transaction Manager**: Ensures ACID properties.
4. **Query Processor**: Parses, optimizes, and executes SQL.

For detailed design, see [docs/Phase1_Design.md](docs/Phase1_Design.md).

## Getting Started
### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

### Build
```bash
mvn clean install
```

## Founder
**Muhammad Ikram Ullah**
- GitHub: [@ikramullahdev](https://github.com/ikramullahdev)
- Started: July 2026

## License
TBD
