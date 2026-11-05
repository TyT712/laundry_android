package com.example.laundrymanager;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.laundrymanager.util.PasswordHasher;

import org.junit.Test;

public class PasswordHasherTest {

    @Test
    public void testHashFormat() {
        String hashed = PasswordHasher.hash("123456");
        assertNotNull(hashed);
        assertTrue("Hash phải chứa ký tự phân cách '$' ngăn cách salt và digest", hashed.contains("$"));
        String[] parts = hashed.split("\\$");
        assertFalse("Salt không được để trống", parts[0].isEmpty());
        assertFalse("Digest không được để trống", parts[1].isEmpty());
    }

    @Test
    public void testVerifyCorrectPassword() {
        String password = "MySecurePassword@2026";
        String hashed = PasswordHasher.hash(password);
        assertTrue("Mật khẩu chính xác phải khớp", PasswordHasher.matches(password, hashed));
    }

    @Test
    public void testVerifyWrongPassword() {
        String password = "CorrectPassword";
        String hashed = PasswordHasher.hash(password);
        assertFalse("Mật khẩu sai không được khớp", PasswordHasher.matches("WrongPassword", hashed));
    }

    @Test
    public void testSaltUniqueness() {
        String password = "SamePassword";
        String hash1 = PasswordHasher.hash(password);
        String hash2 = PasswordHasher.hash(password);
        assertNotEquals("Hai lần băm cùng một mật khẩu phải tạo ra muối ngẫu nhiên khác nhau", hash1, hash2);
        assertTrue(PasswordHasher.matches(password, hash1));
        assertTrue(PasswordHasher.matches(password, hash2));
    }
}
