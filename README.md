# ByteSliceInputStream

A high-performance, zero-copy InputStream implementation for Kotlin that returns immutable `ByteSlice` views instead of copying data into traditional byte arrays.

## 🚀 Key Features

- **Zero-Copy Reading**: Eliminates unnecessary memory copying operations
- **Memory Safety**: Each `ByteSlice` owns an independent buffer, preventing data corruption
- **Intuitive Slicing**: Python/Rust-style range syntax (`slice[2..5]`)
- **GC-Friendly**: Automatic memory management without buffer reuse conflicts
- **Thread-Safe**: Multiple slices can be accessed concurrently
- **Rich API**: Multiple ways to extract data (ByteSlice views or ByteArray copies)

## 📖 Why ByteSliceInputStream?

### Traditional BufferedInputStream Problems

```kotlin
ㅁ// Traditional BufferedInputStream - safe but inefficient
val buffer = ByteArray(1024)
val bytesRead = bufferedInputStream.read(buffer) // System.arraycopy happens here!
// ✅ Safe: you get your own copy of data
// ❌ Inefficient: memory copying overhead

// Attempting zero-copy views with BufferedInputStream - problematic!
class ProblematicSlice(private val buffer: ByteArray, offset: Int, length: Int) {
    // This would reference BufferedInputStream's internal buffer
}

val slice1 = ProblematicSlice(bufferedInputStream.internalBuffer, 0, 100)
bufferedInputStream.read() // Internal buffer gets overwritten!
// ❌ slice1 now references corrupted data
```

### ByteSliceInputStream Solution

```kotlin
// Our approach - zero-copy with complete safety
val slice1 = byteSliceStream.read(1024) // Independent buffer allocation
val slice2 = byteSliceStream.read(1024) // Another independent buffer
// slice1 and slice2 are completely isolated and always valid
```

## 🎯 When to Use

**Perfect for:**
- High-performance file processing
- Large binary data handling
- Stream parsing where you need to reference multiple parts simultaneously
- Memory-conscious applications
- Situations where traditional BufferedInputStream's buffer reuse causes issues

**Consider alternatives for:**
- Simple, small file reading where performance isn't critical
- Cases where you need mutable byte arrays
- Legacy code that heavily depends on traditional InputStream APIs

## 📦 Installation

Simply copy `ByteSlice` and `ByteSliceInputStream` classes into your Kotlin project. No external dependencies required!

## 🔧 Usage Examples

### Basic Reading

```kotlin
val fileInputStream = File("data.bin").inputStream()
val sliceStream = ByteSliceInputStream(fileInputStream)

// Read chunks
while (true) {
    val slice = sliceStream.read(length = 1024) ?: break
    
    // Access data without copying
    val firstByte = slice[0]
    val lastByte = slice[slice.length - 1]
    
    println("Read ${slice.length} bytes")
}

sliceStream.close()
```

### Range-Based Slicing (Python/Rust Style)

```kotlin
val slice = sliceStream.read(100)

// Create sub-slices (zero-copy views)
val header = slice[0..9]        // First 10 bytes
val payload = slice[10..49]     // Next 40 bytes
val footer = slice[50..99]      // Last 50 bytes

// Each slice is independent and safe to use
println("Header: ${header[0]}")
println("Payload size: ${payload.length}")
```

### Flexible Data Extraction

```kotlin
val slice = sliceStream.read(1000)

// Get ByteSlice (zero-copy reference)
val subSlice1 = slice[100..199]           // Range syntax
val subSlice2 = slice.slice(100, 100)     // Method syntax

// Get ByteArray (copied data)
val bytes1 = slice.toByteArray(100..199)  // Range syntax
val bytes2 = slice.toByteArray(100, 100)  // Method syntax
val bytes3 = slice.toByteArray()          // Whole slice
```

### Advanced Usage

```kotlin
// Skip data and read
val slice = sliceStream.read(offset = 1000, length = 512)

// Read exact amount (throws if insufficient data)
val exactSlice = sliceStream.readExactly(256)

// Read entire stream
val wholeFile = sliceStream.readAll()

// Traditional byte-by-byte reading (compatibility)
val singleByte = sliceStream.readByte()
```

## 🏗️ API Reference

### ByteSlice

| Method | Description | Return Type |
|--------|-------------|-------------|
| `slice[index]` | Access byte at index | `Byte` |
| `slice[range]` | Create sub-slice using range | `ByteSlice` |
| `slice(offset, length)` | Create sub-slice using offset/length | `ByteSlice` |
| `toByteArray()` | Convert whole slice to ByteArray | `ByteArray` |
| `toByteArray(range)` | Convert range to ByteArray | `ByteArray` |
| `toByteArray(offset, length)` | Convert portion to ByteArray | `ByteArray` |
| `copyTo(dest, ...)` | Copy data to existing array | `Unit` |

### ByteSliceInputStream

| Method | Description | Return Type |
|--------|-------------|-------------|
| `read(offset, length)` | Read data as ByteSlice | `ByteSlice?` |
| `readExactly(length)` | Read exact length or throw | `ByteSlice` |
| `readAll()` | Read entire stream | `ByteSlice?` |
| `readByte()` | Read single byte | `Int` |
| `available()` | Estimate available bytes | `Int` |
| `close()` | Close underlying stream | `Unit` |

## ⚡ Performance Characteristics

### Memory Usage
- **ByteSlice creation**: O(1) - no copying, just reference creation
- **Buffer allocation**: O(n) - one allocation per read() call
- **GC impact**: Minimal - unused slices are automatically collected

### Time Complexity
- **Reading**: O(n) where n = bytes read (same as traditional streams)
- **Slicing**: O(1) for ByteSlice views, O(n) for ByteArray conversion
- **Access**: O(1) for individual byte access

### Comparison with Alternatives

| Approach | Memory Copies | Zero-Copy Views | Buffer Reuse Issues | Slicing |
|----------|---------------|-----------------|---------------------|---------|
| BufferedInputStream | High | ❌ No views | ✅ Safe (copies data) | ❌ Manual |
| BufferedInputStream + Custom Views | Low | ⚠️ Possible | ❌ **Buffer reuse corruption** | ⚠️ Complex |
| ByteBuffer | Low | ✅ Built-in | ⚠️ Shared views | ✅ Built-in |
| ByteSliceInputStream | **Minimal** | ✅ **Independent buffers** | ✅ **No reuse conflicts** | ✅ **Intuitive** |

## 🔒 Thread Safety

- **ByteSlice**: Completely immutable and thread-safe
- **ByteSliceInputStream**: Not thread-safe (like standard InputStream)
- **Multiple slices**: Can be safely accessed from different threads simultaneously

## 🤝 Comparison with Existing Libraries

### vs Java NIO ByteBuffer
```kotlin
// ByteBuffer - complex position/limit management
ByteBuffer buffer = ByteBuffer.allocate(100);
buffer.put(data);
buffer.flip();
ByteBuffer slice = buffer.slice(); // Shared view - can be dangerous

// ByteSliceInputStream - intuitive and safe
val slice = stream.read(100)
val subSlice = slice[10..20] // Independent and safe
```

### vs OkHttp ByteString / Okio
```kotlin
// Okio - external dependency, different API paradigm
val source = file.source().buffer()
val byteString = source.readByteString()

// ByteSliceInputStream - no dependencies, familiar stream API
val slice = ByteSliceInputStream(file.inputStream()).read()
```

## 🛠️ Building and Testing

### File Structure
```
ByteSliceInputStream/
├── README.md
├── LICENSE
├── .gitignore
├── build.gradle.kts
├── gradle.properties
├── gradle/wrapper/
│   └── gradle-wrapper.properties
├── .github/workflows/
│   ├── ci.yml
│   └── publish.yml
├── src/
│   ├── main/kotlin/
│   │   ├── ByteSlice.kt
│   │   └── ByteSliceInputStream.kt
│   └── test/kotlin/
│       ├── ByteSliceTest.kt
│       └── ByteSliceInputStreamTest.kt
└── examples/
    └── Demo.kt
```

### Build Commands
```bash
# Run tests
./gradlew test

# Build the project
./gradlew build

# Generate documentation
./gradlew dokkaHtml

# Publish to local Maven repository
./gradlew publishToMavenLocal

# Publish to Maven Central (requires credentials)
./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository
```

### Maven Dependency
Once published to Maven Central, you can use it in your projects:

#### Gradle (Kotlin DSL)
```kotlin
dependencies {
    implementation("io.github.yourusername:bytesliceinputstream:1.0.0")
}
```

#### Gradle (Groovy DSL)
```groovy
dependencies {
    implementation 'io.github.yourusername:bytesliceinputstream:1.0.0'
}
```

#### Maven
```xml
<dependency>
    <groupId>io.github.yourusername</groupId>
    <artifactId>bytesliceinputstream</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Development Setup
```bash
# Clone the repository
git clone https://github.com/yourusername/ByteSliceInputStream.git
cd ByteSliceInputStream

# Make gradlew executable (Unix/Linux/macOS)
chmod +x gradlew

# Run tests to verify setup
./gradlew test
```

## 🧪 Testing

The project includes comprehensive test suites:

- **ByteSliceTest**: Tests all ByteSlice functionality including slicing, range operations, and data integrity
- **ByteSliceInputStreamTest**: Tests stream operations, EOF handling, and large data processing

### Running Tests
```bash
# Run all tests
./gradlew test

# Run tests with detailed output
./gradlew test --info

# Run specific test class
./gradlew test --tests "ByteSliceTest"

# Generate test report
./gradlew test jacocoTestReport
```

### Test Coverage
The test suite covers:
- ✅ All public API methods
- ✅ Edge cases and error conditions
- ✅ Large data handling (>20KB)
- ✅ Memory safety and independence
- ✅ Performance characteristics
- ✅ Thread safety aspects

This project is released under the MIT License. Feel free to use, modify, and distribute as needed.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

## 🎯 Roadmap

- [ ] Async/coroutine support for non-blocking I/O
- [ ] Memory-mapped file integration
- [ ] Compression/decompression pipeline support
- [ ] Benchmarking suite against traditional approaches
- [ ] Integration with popular serialization libraries

---

**Made with ❤️ for high-performance Kotlin applications**