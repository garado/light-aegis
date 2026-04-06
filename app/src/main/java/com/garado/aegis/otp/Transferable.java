package com.garado.aegis.otp;

import android.net.Uri;

public interface Transferable {
    Uri getUri() throws GoogleAuthInfoException;
}
