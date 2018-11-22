package com.br.authutil;

public interface AuthCallback {

    void authSucceeded();

    void authFailed(String error);

    void authCancelled();
}
