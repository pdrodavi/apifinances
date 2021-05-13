package br.com.pedrodavi.financesapi.exception;

public class AuthError extends RuntimeException {

    public AuthError(String msg) {
        super(msg);
    }

}
