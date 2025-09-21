package dev.hh46/*
 * Copyright (c) 2025 Hyoungho Choi <holuyaa@gmail.com>
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

/**
 * Immutable dev.hh46.ByteSlice - A view that references a specific region of an independent buffer Each
 * dev.hh46.ByteSlice owns a unique buffer, ensuring complete isolation from other slices
 */
class ByteSlice
internal constructor(private val buffer: ByteArray, private val startOffset: Int, val length: Int) {
  init {
    require(startOffset >= 0) { "Start offset must be non-negative" }
    require(length >= 0) { "Length must be non-negative" }
    require(startOffset + length <= buffer.size) { "Slice bounds exceed buffer size" }
  }

  /**
   * Reads a byte at the specified position within the slice
   * @param pos Relative position within the slice (0-based)
   */
  operator fun get(pos: Int): Byte {
    require(pos in 0 until length) { "Position $pos out of bounds [0, $length)" }
    return buffer[startOffset + pos]
  }

  /**
   * Range-based slicing (Python/Rust style)
   * @param range Index range to slice
   * @return New dev.hh46.ByteSlice (sub-slice)
   */
  operator fun get(range: IntRange): ByteSlice {
    val start = range.first
    val end = range.last

    require(start >= 0) { "Range start must be non-negative" }
    require(end < length) { "Range end exceeds slice bounds" }
    require(start <= end) { "Range start must not exceed end" }

    val rangeLength = end - start + 1
    return slice(start, rangeLength)
  }

  /** Copies slice data to another array (only when needed) */
  fun copyTo(dest: ByteArray, destOffset: Int = 0, srcOffset: Int = 0, copyLength: Int = length) {
    require(srcOffset + copyLength <= length) { "Copy range exceeds slice bounds" }
    require(destOffset + copyLength <= dest.size) { "Destination array too small" }

    System.arraycopy(buffer, startOffset + srcOffset, dest, destOffset, copyLength)
  }

  /** Converts the slice to a new ByteArray by copying */
  fun toByteArray(): ByteArray {
    return buffer.copyOfRange(startOffset, startOffset + length)
  }

  /**
   * Converts a sub-slice to ByteArray by copying
   * @param offset Starting offset
   * @param length Length to copy
   */
  fun toByteArray(offset: Int, length: Int): ByteArray {
    require(offset >= 0 && length >= 0) { "Offset and length must be non-negative" }
    require(offset + length <= this.length) { "Range exceeds slice bounds" }

    return buffer.copyOfRange(startOffset + offset, startOffset + offset + length)
  }

  /**
   * Converts a range to ByteArray by copying
   * @param range Index range to copy
   */
  fun toByteArray(range: IntRange): ByteArray {
    val start = range.first
    val end = range.last

    require(start >= 0) { "Range start must be non-negative" }
    require(end < length) { "Range end exceeds slice bounds" }
    require(start <= end) { "Range start must not exceed end" }

    val rangeLength = end - start + 1
    return buffer.copyOfRange(startOffset + start, startOffset + start + rangeLength)
  }

  /** Creates a sub-slice of this slice */
  fun slice(offset: Int, length: Int): ByteSlice {
    require(offset >= 0 && length >= 0) { "Offset and length must be non-negative" }
    require(offset + length <= this.length) { "Slice bounds exceed current slice" }

    return ByteSlice(buffer, startOffset + offset, length)
  }

  /** String representation for debugging */
  override fun toString(): String {
    return "dev.hh46.ByteSlice(offset=$startOffset, length=$length)"
  }

  /** Checks if two slices reference the same data */
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ByteSlice) return false

    if (length != other.length) return false

    for (i in 0 until length) {
      if (this[i] != other[i]) return false
    }
    return true
  }

  override fun hashCode(): Int {
    var result = length
    for (i in 0 until length) {
      result = 31 * result + this[i]
    }
    return result
  }
}
