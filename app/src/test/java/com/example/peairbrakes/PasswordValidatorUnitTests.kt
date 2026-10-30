package com.example.peairbrakes

import org.junit.Assert
import org.junit.Test

class PasswordValidatorUnitTests {

    @Test
    fun whenPasswordIsValid_shouldReturnTrue() {
        val password = "Password1!"
        val result = PasswordValidator.isValidPassword(password)
        Assert.assertTrue(result)
    }

    @Test
    fun whenPasswordIsInvalid_shouldReturnFalse() {
        val password = "password1!"
        val result = PasswordValidator.isValidPassword(password)
        Assert.assertFalse(result)
    }
}
