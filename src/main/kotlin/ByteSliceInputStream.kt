/*
 * Copyright (c) 2025 Hyoungho Choi <holuyaa@gmail.com>
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import java.io.*

/**
 * Zero-Copy InputStream that returns ByteSlice Each read() call allocates a new independent buffer
 * ensuring complete data safety
 */
class ByteSliceInputStream(private val inputStream: InputStream) : AutoCloseable {

  private var isEOF = false

  /**
   * Reads data of specified length and returns it as an independent ByteSlice
   *
   * @param offset Number of bytes to skip from the stream (default: 0)
   * @param length Maximum number of bytes to read
   * @return ByteSlice of actually read data, null if EOF
   */
  fun read(offset: Int = 0, length: Int = 8192): ByteSlice? {
    require(offset >= 0) { "Offset must be non-negative" }
    require(length > 0) { "Length must be positive" }

    if (isEOF) return null

    // Skip the specified number of bytes
    var remainingOffset = offset
    while (remainingOffset > 0) {
      val skipBuffer = ByteArray(minOf(remainingOffset, 8192))
      val skipped = inputStream.read(skipBuffer)

      if (skipped == -1) {
        isEOF = true
        return null
      }

      remainingOffset -= skipped
    }

    // Allocate new independent buffer - core concept!
    val buffer = ByteArray(length)
    val bytesRead = inputStream.read(buffer)

    if (bytesRead == -1) {
      isEOF = true
      return null
    }

    // Each ByteSlice owns a unique buffer, making it completely safe
    return ByteSlice(buffer, 0, bytesRead)
  }

  /** Reads exactly the specified length (throws exception if insufficient data) */
  fun readExactly(length: Int): ByteSlice {
    require(length > 0) { "Length must be positive" }

    val buffer = ByteArray(length)
    var totalRead = 0

    while (totalRead < length) {
      val bytesRead = inputStream.read(buffer, totalRead, length - totalRead)
      if (bytesRead == -1) {
        throw EOFException("Unexpected end of stream. Expected $length bytes, got $totalRead")
      }
      totalRead += bytesRead
    }

    return ByteSlice(buffer, 0, length)
  }

  /** Reads one byte at a time (compatibility with existing InputStream) */
  fun readByte(): Int {
    if (isEOF) return -1

    val result = inputStream.read()
    if (result == -1) {
      isEOF = true
    }

    return result
  }

  /** Reads all data into one large ByteSlice */
  fun readAll(): ByteSlice? {
    val chunks = mutableListOf<ByteArray>()
    var totalSize = 0

    while (true) {
      val chunk = ByteArray(8192)
      val bytesRead = inputStream.read(chunk)

      if (bytesRead == -1) break

      if (bytesRead == chunk.size) {
        chunks.add(chunk)
      } else {
        chunks.add(chunk.copyOf(bytesRead))
      }

      totalSize += bytesRead
    }

    if (totalSize == 0) {
      isEOF = true
      return null
    }

    // Combine all chunks into a single buffer
    val combinedBuffer = ByteArray(totalSize)
    var offset = 0

    for (chunk in chunks) {
      System.arraycopy(chunk, 0, combinedBuffer, offset, chunk.size)
      offset += chunk.size
    }

    isEOF = true
    return ByteSlice(combinedBuffer, 0, totalSize)
  }

  /** Estimated number of available bytes */
  fun available(): Int {
    return if (isEOF) 0 else inputStream.available()
  }

  override fun close() {
    inputStream.close()
  }
}
