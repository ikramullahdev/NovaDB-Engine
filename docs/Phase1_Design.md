# NovaDB Engine: Phase 1 Design Document

## 1. Introduction to NovaDB Engine

NovaDB Engine is envisioned as an AI-ready, open-source database engine designed from scratch in Java. Our primary goal is to create a modern, high-performance, and scalable database solution that avoids the architectural limitations and legacy complexities found in older systems like PostgreSQL or Oracle. By leveraging Java, we aim for platform independence, robust concurrency, and a rich ecosystem of development tools. The 
engine will be built with AI integration in mind, allowing for advanced analytics and machine learning capabilities directly within the database.

## 2. Project Folder Structure

A well-defined and consistent project structure is crucial for maintainability, scalability, and collaborative development. We will adopt a modular approach, separating concerns into distinct packages and directories. This structure facilitates clear ownership, reduces coupling, and simplifies navigation for developers.

```
NovaDB-Engine/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── novadb/
│   │   │           ├── common/             # Common utilities, data structures, constants
│   │   │           ├── storage/            # Storage Manager components (Buffer Pool, Page, Disk I/O)
│   │   │           ├── catalog/            # Metadata management (schemas, tables, indexes)
│   │   │           ├── query/              # Query processing (parser, optimizer, executor)
│   │   │           ├── transaction/        # Transaction management (concurrency control, recovery)
│   │   │           ├── index/              # Indexing structures (B-trees, hash indexes)
│   │   │           ├── network/            # Client-server communication
│   │   │           ├── server/             # Main database server entry point
│   │   │           └── util/               # General-purpose utilities
│   │   └── resources/        # Configuration files, default data, logging configs
│   └── test/
│       ├── java/
│       │   └── com/
│       │       └── novadb/
│       │           ├── common/
│       │           ├── storage/
│       │           ├── catalog/
│       │           ├── query/
│       │           ├── transaction/
│       │           ├── index/
│       │           ├── network/
│       │           └── server/
│       └── resources/
├── lib/                      # Third-party libraries (e.g., logging frameworks, testing libraries)
├── docs/                     # Documentation (design docs, API docs, user manuals)
├── scripts/                  # Build scripts, deployment scripts, utility scripts
├── config/                   # Deployment-specific configurations
├── data/                     # Default data directory for database files
├── .gitignore
├── pom.xml                   # Maven project configuration
└── README.md
```

**Explanation of Structure:**

*   **`src/main/java/com/novadb/`**: This is the core source code directory, organized by functional modules. Each module represents a key component of the database system.
    *   **`common/`**: Houses fundamental data structures, interfaces, and constants used across multiple modules. This promotes code reuse and consistency.
    *   **`storage/`**: Dedicated to the Storage Manager, which is the focus of Phase 1. It will contain sub-packages for buffer management, page management, and disk I/O.
    *   **`catalog/`**: Manages the database's metadata, including schema definitions, table structures, index information, and statistics. This separation ensures that the database can understand and interpret its own data.
    *   **`query/`**: Encompasses all aspects of query processing, from parsing SQL statements to optimizing execution plans and finally executing them. This is a complex module that will be broken down further in later phases.
    *   **`transaction/`**: Handles the ACID properties of transactions, including concurrency control (e.g., locking, multi-version concurrency control) and recovery mechanisms (e.g., logging, checkpointing). This is critical for data integrity and reliability.
    *   **`index/`**: Contains implementations of various indexing structures to accelerate data retrieval, such as B-trees, hash indexes, and potentially more advanced structures for specific use cases.
    *   **`network/`**: Manages all network communication, including client connections, data serialization/deserialization, and protocol handling. This ensures that clients can connect and interact with the database server.
    *   **`server/`**: The main entry point for the database server, responsible for initialization, managing modules, and handling incoming client requests.
    *   **`util/`**: A catch-all for general-purpose utilities that don't fit neatly into other core modules but are essential for the database's operation.
*   **`src/test/`**: Mirrors the `src/main/` structure for unit and integration tests. A strong test suite is paramount for a robust database system.
*   **`lib/`**: Stores any external JAR files or third-party libraries that are not managed by Maven dependencies (though we will primarily rely on Maven).
*   **`docs/`**: Central repository for all project documentation, including design specifications, API documentation generated from Javadoc, and user guides.
*   **`scripts/`**: Contains shell scripts for common development tasks, such as building, testing, deployment, and database initialization.
*   **`config/`**: Holds configuration files that might vary between deployment environments (e.g., production, development, testing).
*   **`data/`**: The default location for database files, logs, and other persistent data.
*   **`.gitignore`**: Specifies files and directories that should be ignored by Git.
*   **`pom.xml`**: The Project Object Model file for Maven, defining project dependencies, build plugins, and project metadata.
*   **`README.md`**: Provides a high-level overview of the project, build instructions, and contribution guidelines.

## 3. Coding Standards

Adhering to strict coding standards is essential for maintaining code quality, readability, and consistency across a large open-source project. We will follow established Java best practices and leverage automated tools to enforce these standards.

### 3.1. General Principles

*   **Readability**: Code should be easy to understand, even for developers unfamiliar with the specific module.
*   **Consistency**: Follow established patterns and conventions throughout the codebase.
*   **Modularity**: Design components with clear responsibilities and minimal dependencies.
*   **Testability**: Write code that is easy to test, promoting unit and integration testing.
*   **Performance**: Optimize critical paths for performance, but prioritize correctness and readability first.
*   **Security**: Implement secure coding practices to prevent vulnerabilities.

### 3.2. Naming Conventions

*   **Packages**: Lowercase, separated by dots (e.g., `com.novadb.storage`).
*   **Classes/Interfaces**: PascalCase (e.g., `BufferPoolManager`, `Page`).
*   **Methods/Variables**: camelCase (e.g., `readPage`, `pageSize`).
*   **Constants**: SCREAMING_SNAKE_CASE (e.g., `DEFAULT_PAGE_SIZE`).
*   **Enums**: PascalCase for enum type, SCREAMING_SNAKE_CASE for enum values.

### 3.3. Formatting

*   **Indentation**: 4 spaces, no tabs.
*   **Line Length**: Max 120 characters per line.
*   **Braces**: K&R style (opening brace on the same line as the declaration).
*   **Whitespace**: Use judiciously to improve readability (e.g., around operators, after commas).

### 3.4. Documentation

*   **Javadoc**: All public classes, interfaces, methods, and significant fields MUST be documented using Javadoc. This includes a brief description, parameter explanations (`@param`), return value descriptions (`@return`), and exception declarations (`@throws`).
*   **Inline Comments**: Use sparingly for explaining complex logic or non-obvious decisions. Avoid commenting on obvious code.

### 3.5. Error Handling

*   **Exceptions**: Use checked exceptions for recoverable errors and unchecked exceptions (runtime exceptions) for programming errors or unrecoverable conditions.
*   **Logging**: Utilize a robust logging framework (e.g., SLF4J with Logback) for debugging, monitoring, and auditing. Avoid `System.out.println()` in production code.

### 3.6. Concurrency

*   **Thread Safety**: All shared mutable state MUST be protected by appropriate synchronization mechanisms (e.g., `synchronized` blocks/methods, `java.util.concurrent` package utilities).
*   **Immutability**: Favor immutable objects where possible to simplify concurrency management.

### 3.7. Tools for Enforcement

*   **Maven**: For dependency management and build automation.
*   **Checkstyle**: To enforce coding style and conventions.
*   **PMD/SpotBugs**: For static code analysis to detect common programming flaws and potential bugs.
*   **JaCoCo**: For code coverage analysis to ensure adequate test coverage.
*   **Git Hooks**: Potentially use pre-commit hooks to run formatters and linters automatically.

## 4. High-Level Architecture Overview

NovaDB Engine will follow a layered architecture, a common and effective design pattern for complex systems like database management systems. This approach promotes modularity, separation of concerns, and allows for independent development and testing of each layer. The core layers will include:

1.  **Storage Manager**: Responsible for persistent storage of data on disk and efficient access to it. This layer abstracts away the complexities of the underlying file system.
2.  **Buffer Manager**: Manages the main memory buffer pool, caching frequently accessed data pages to minimize disk I/O.
3.  **Page Manager**: Handles the structure and manipulation of data within individual pages, including record storage and free space management.
4.  **Catalog Manager**: Stores and manages all metadata about the database, such as table schemas, index definitions, and user permissions.
5.  **Transaction Manager**: Ensures the ACID properties (Atomicity, Consistency, Isolation, Durability) of transactions, handling concurrency control and recovery.
6.  **Query Processor**: Parses, optimizes, and executes SQL queries, translating high-level requests into low-level storage operations.
7.  **Network Manager**: Facilitates communication between clients and the database server.

This layered approach ensures that changes in one layer have minimal impact on others, making the system more robust and easier to evolve. The Storage Manager and Buffer Manager will be the foundational components developed in Phase 1.

## 5. Storage Manager Architecture

The Storage Manager is the lowest layer of the database system, directly responsible for managing the persistent storage of data on disk and providing an efficient interface for higher-level components to access this data. Its primary goal is to abstract away the complexities of the underlying file system and hardware, presenting a consistent view of data pages to the Buffer Pool Manager. This layer is critical for both performance and data durability.

### 5.1. Disk I/O Layer

The Disk I/O layer is the interface between the database system and the physical storage. It handles reading and writing fixed-size data blocks (pages) to and from disk files. To ensure robustness and efficiency, we will implement a dedicated `DiskManager` component.

#### 5.1.1. Design Decisions

*   **Page-Oriented I/O**: All disk operations will be performed in terms of fixed-size pages (e.g., 4KB, 8KB, or 16KB). This aligns with typical operating system block sizes and simplifies buffer management. We will start with a default page size of 4KB, configurable at compile time.
*   **File Abstraction**: Each database table or index will correspond to one or more physical files on disk. The `DiskManager` will manage these files, opening, closing, and performing read/write operations on them. We will use Java's `RandomAccessFile` for direct byte manipulation and seeking within files.
*   **Direct I/O Consideration**: While Java's standard I/O is buffered by the OS, for high-performance scenarios, direct I/O (bypassing the OS cache) can be beneficial to avoid double buffering. However, direct I/O is platform-dependent and more complex to manage. For initial development, we will rely on standard buffered I/O, with a clear path to integrate direct I/O (e.g., via `java.nio.channels.FileChannel` with `ByteBuffer.allocateDirect()`) if performance benchmarks necessitate it.
*   **Error Handling**: Robust error handling for disk operations is paramount. `IOException`s will be caught and wrapped in custom `NovaDBException`s, providing more context-specific error messages to higher layers.
*   **File Allocation**: Initially, we will use simple heap files where pages are appended sequentially. For more advanced scenarios, we will consider extent-based allocation to reduce fragmentation and improve sequential I/O performance.

#### 5.1.2. Key Components

*   `DiskManager`: Manages physical files, responsible for reading and writing pages. It will maintain a mapping of file names to `RandomAccessFile` instances.
    *   `readPage(PageId pageId, byte[] data)`: Reads a page from disk into the provided byte array.
    *   `writePage(PageId pageId, byte[] data)`: Writes a page from the byte array to disk.
    *   `allocatePage(String filename)`: Allocates a new page in a file and returns its `PageId`.
    *   `deallocatePage(PageId pageId)`: Marks a page as free for reuse.
    *   `flushFile(String filename)`: Ensures all buffered writes for a file are committed to disk.
*   `PageId`: A simple value object (e.g., `(int fileId, int pageNum)`) to uniquely identify a page on disk.

### 5.2. Page Management

Page Management defines the internal structure of a data page and how records are stored and managed within it. A page is the smallest unit of data transfer between disk and memory.

#### 5.2.1. Design Decisions

*   **Fixed Page Size**: As mentioned, pages will have a fixed size (e.g., 4KB). This simplifies buffer management and disk I/O.
*   **Page Header**: Each page will include a header containing metadata about the page itself. This metadata is crucial for managing the page's contents.
    *   `PageType`: (e.g., `DATA_PAGE`, `INDEX_PAGE`, `FREE_SPACE_PAGE`).
    *   `PageId`: Unique identifier of the page.
    *   `LSN (Log Sequence Number)`: Used for recovery and ensuring write-ahead logging (WAL) principles.
    *   `FreeSpacePointer`: Points to the start of free space within the page.
    *   `NumRecords`: Number of records currently stored in the page.
    *   `Checksum`: For data integrity verification.
*   **Slot Directory for Records**: For flexible record management, especially with variable-length records, we will use a slot directory at the end of the page. Each slot will store the offset and length of a record. Records themselves will be stored contiguously from the beginning of the page after the header. When records are deleted or updated, the free space can be compacted, and slot directory entries updated.
*   **RecordId**: A unique identifier for a record, typically `(PageId pageId, int slotNum)`.

#### 5.2.2. Key Components

*   `Page`: An abstract base class or interface representing a generic database page. It will provide methods for reading/writing header information and accessing the raw byte array.
*   `DataPage`: A concrete implementation of `Page` for storing actual table records. It will manage the slot directory and provide methods for inserting, deleting, and updating records.
    *   `insertRecord(byte[] recordData)`: Inserts a new record into the page.
    *   `deleteRecord(RecordId rid)`: Deletes a record and potentially compacts free space.
    *   `getRecord(RecordId rid)`: Retrieves a record's data.
    *   `getFreeSpace()`: Returns the amount of free space available.

### 5.3. Buffer Pool Manager

The Buffer Pool Manager (BPM) is a critical component that acts as a cache between the Disk I/O layer and the higher-level database components. Its main purpose is to minimize disk I/O by keeping frequently accessed data pages in main memory.

#### 5.3.1. Design Decisions

*   **Fixed-Size Buffer Pool**: The buffer pool will consist of a fixed number of `frames`, each capable of holding one `Page`. The size of the buffer pool will be configurable.
*   **Page Table**: A hash map (`Map<PageId, FrameId>`) will be used to quickly locate a page in the buffer pool given its `PageId`. This allows for O(1) average-case lookup time.
*   **Free List**: A list or queue of available `FrameId`s for new pages. When a frame is needed, it's taken from the free list.
*   **Replacer**: When the buffer pool is full and a new page needs to be brought in, a page replacement policy is used to select a victim page to be evicted. We will start with a **LRU (Least Recently Used)** replacement policy due to its simplicity and generally good performance. This will be implemented using a combination of a `DoublyLinkedList` and a `HashMap` for O(1) access to list nodes.
*   **Pinning Mechanism**: Pages in the buffer pool can be 
pinned" to prevent them from being evicted. Each page will have a `pin_count`. A page can only be evicted if its `pin_count` is zero. This is crucial for ensuring that pages actively being used by transactions are not prematurely removed from memory.
*   **Dirty Pages**: Pages that have been modified in memory (i.e., are "dirty") must be written back to disk before they can be evicted. Each page will have a `is_dirty` flag. The `BufferPoolManager` will be responsible for flushing dirty pages to disk via the `DiskManager`.
*   **Concurrency**: Access to the buffer pool data structures (page table, free list, replacer) must be thread-safe. We will use Java's concurrency utilities (e.g., `ReentrantLock`, `ConcurrentHashMap`) to ensure safe concurrent access.

#### 5.3.2. Key Components

*   `BufferPoolManager`: The central component for managing the buffer pool.
    *   `fetchPage(PageId pageId)`: Retrieves a page from the buffer pool. If not present, it reads it from disk, adds it to the buffer pool, and pins it. Returns a `Page` object.
    *   `unpinPage(PageId pageId, boolean isDirty)`: Decrements the pin count of a page. If `isDirty` is true, sets the dirty flag.
    *   `newPage(PageId pageId)`: Creates a new page, allocates a frame, and pins it. Writes the new page to disk.
    *   `deletePage(PageId pageId)`: Removes a page from the buffer pool and deallocates it on disk.
    *   `flushPage(PageId pageId)`: Writes a specific page to disk if it's dirty.
    *   `flushAllPages()`: Writes all dirty pages in the buffer pool to disk.
*   `Frame`: A container within the buffer pool that holds a `Page` object, its `PageId`, `pin_count`, and `is_dirty` flag.
*   `Replacer`: An interface (e.g., `LRUReplacer`) that defines the page replacement policy. It will manage the order of pages for eviction.

### 5.4. Interaction Flow

1.  **Request for Page**: A higher-level component (e.g., Query Processor) requests a `Page` using its `PageId` from the `BufferPoolManager`.
2.  **Buffer Pool Lookup**: The `BufferPoolManager` checks its `PageTable` to see if the page is already in memory.
    *   **Cache Hit**: If found, the `pin_count` for that page's `Frame` is incremented, and the `Page` object is returned.
    *   **Cache Miss**: If not found:
        *   The `BufferPoolManager` requests a free `Frame` from the `Replacer` (or `FreeList`). If no free frames are available, the `Replacer` selects a victim `Frame` based on its policy (e.g., LRU).
        *   If the victim `Frame`'s page is dirty, the `BufferPoolManager` instructs the `DiskManager` to write the victim page back to disk.
        *   The `DiskManager` then reads the requested page from disk into the newly available `Frame`.
        *   The `PageTable` is updated, the `pin_count` is set to 1, and the `Page` object is returned.
3.  **Page Modification**: If the higher-level component modifies the `Page` data, it notifies the `BufferPoolManager` by calling `unpinPage(pageId, true)`.
4.  **Page Release**: When a component is done with a page, it calls `unpinPage(pageId, false)` (or `true` if modified) to decrement the `pin_count`. Once `pin_count` reaches zero, the page becomes a candidate for eviction by the `Replacer`.

## 6. Development Roadmap (Phase 1)

Phase 1 will focus on establishing the foundational components of the Storage Manager, ensuring a robust and efficient base for future development. The estimated timeline for these tasks is 3-4 weeks.

### Week 1: Setup and Disk I/O Layer

*   **Project Setup**: Initialize Maven project, establish basic folder structure, configure `pom.xml` with necessary dependencies (e.g., SLF4J, JUnit).
*   **Coding Standards Integration**: Integrate Checkstyle, PMD, and JaCoCo into the Maven build process. Define initial Checkstyle rules.
*   **`PageId` Implementation**: Create the `PageId` value object.
*   **`DiskManager` Implementation**: Implement `readPage`, `writePage`, `allocatePage`, `deallocatePage`, and `flushFile` methods using `RandomAccessFile`.
*   **Unit Tests**: Develop comprehensive unit tests for `DiskManager`.

### Week 2: Page Management Layer

*   **`Page` Interface/Abstract Class**: Define the `Page` contract.
*   **`DataPage` Implementation**: Implement `DataPage` with header management and initial record insertion/retrieval logic (simple append for now, without slot directory compaction).
*   **RecordId Implementation**: Create the `RecordId` value object.
*   **Unit Tests**: Develop comprehensive unit tests for `DataPage`.

### Week 3-4: Buffer Pool Manager

*   **`Frame` Implementation**: Create the `Frame` class to hold page data and metadata.
*   **`LRUReplacer` Implementation**: Implement the LRU page replacement policy using a `DoublyLinkedList` and `HashMap`.
*   **`BufferPoolManager` Implementation**: Implement `fetchPage`, `unpinPage`, `newPage`, `deletePage`, `flushPage`, and `flushAllPages`.
*   **Concurrency Control**: Integrate basic `ReentrantLock` for thread-safe access to `BufferPoolManager` internal data structures.
*   **Integration Tests**: Develop integration tests to verify the interaction between `DiskManager`, `DataPage`, and `BufferPoolManager`.
*   **Documentation**: Update Javadoc for all implemented components.

## 7. Conclusion of Phase 1

Upon completion of Phase 1, NovaDB Engine will have a solid and tested Storage Manager foundation. This includes a reliable Disk I/O layer, a well-defined Page Management system, and an efficient Buffer Pool Manager with an LRU replacement policy. This robust base will enable the development of higher-level components in subsequent phases, ensuring data persistence, integrity, and performance. The adherence to strict coding standards and a modular project structure will facilitate collaborative development and long-term maintainability. This concludes the Phase 1 design document for NovaDB Engine.
