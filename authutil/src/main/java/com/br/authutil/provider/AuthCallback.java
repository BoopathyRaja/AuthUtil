package com.br.authutil.provider;

import com.br.authutil.data.AuthData;
import com.br.authutil.data.AuthType;

public interface AuthCallback {

    void authSucceeded(AuthType authType, AuthData authData, Object... extras);

    void authFailed(String error);

    void authCancelled();

    void authTimeout();
}
