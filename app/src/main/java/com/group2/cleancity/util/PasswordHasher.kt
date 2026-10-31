package com.group2.cleancity.util

import java.security.MessageDigest
import java.security.SecureRandom

object PasswordHasher {

    // Turns "pass1234" into "salt:hash" like a1b2c3...salt...:9f8e7d...hash...  (both as hex text)
    fun hash(password: String): String {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return toHex(salt) + ":" + toHex(sha256(salt, password))
    }

    // Re-hashes the typed password with the stored salt and compares
    fun verify(password: String, stored: String): Boolean {
        val parts = stored.split(":")
        if (parts.size != 2) return false
        val salt = fromHex(parts[0])
        return toHex(sha256(salt, password)) == parts[1]
    }
// actual hashing occurs
    private fun sha256(salt: ByteArray, password: String): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt)
        return digest.digest(password.toByteArray(Charsets.UTF_8))
    }
// converts bytes to hex text
    private fun toHex(bytes: ByteArray): String =
        //joint ot string combines all converted bytes into 1 string w/o sepertaors
       // %02x formats each byte as a 2 digit hex value
        bytes.joinToString("") { "%02x".format(it) }
// reverse of hex
    private fun fromHex(hex: String): ByteArray =
        ByteArray(hex.length / 2) { i ->
            hex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
}