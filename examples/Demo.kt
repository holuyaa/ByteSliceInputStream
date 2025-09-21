import java.io.*

// Minimal demo with essential output only
fun main() {
    println("ByteSliceInputStream Demo")
    
    // Create temporary file for demo
    val tempFile = createTempFile("demo", ".txt")
    tempFile.writeText("Hello World! This is a test file for ByteSliceInputStream demonstration.")
    
    try {
        val fileInputStream = tempFile.inputStream()
        val sliceStream = ByteSliceInputStream(fileInputStream)
        
        println("\n1. Basic Reading:")
        val slice1 = sliceStream.read(length = 20)
        println("Read ${slice1?.length} bytes: \"${slice1?.let { String(it.toByteArray()) }}\"")
        
        println("\n2. Range-based Slicing:")
        val slice2 = sliceStream.read(length = 30)
        if (slice2 != null) {
            val subSlice = slice2[5..15]
            println("Sub-slice [5..15]: \"${String(subSlice.toByteArray())}\"")
        }
        
        println("\n3. Direct ByteArray extraction:")
        val slice3 = sliceStream.read(length = 20)
        if (slice3 != null) {
            val bytes = slice3.toByteArray(0..9)
            println("First 10 bytes as array: \"${String(bytes)}\"")
        }
        
        sliceStream.close()
        println("\nDemo completed successfully!")
        
    } catch (e: Exception) {
        println("Error: ${e.message}")
    } finally {
        tempFile.delete()
    }
}
