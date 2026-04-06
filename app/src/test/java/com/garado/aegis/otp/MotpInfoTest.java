package com.garado.aegis.otp;

import static org.junit.Assert.assertEquals;

import com.garado.aegis.crypto.otp.MOTPTest;
import com.garado.aegis.encoding.EncodingException;
import com.garado.aegis.encoding.Hex;

import org.junit.Test;

public class MotpInfoTest {
    @Test
    public void testMotpInfoOtp() throws OtpInfoException, EncodingException {
        for (MOTPTest.Vector vector : MOTPTest.VECTORS) {
            MotpInfo info = new MotpInfo(Hex.decode(vector.Secret), vector.Pin);
            assertEquals(vector.OTP, info.getOtp(vector.Time));
        }
    }
}
