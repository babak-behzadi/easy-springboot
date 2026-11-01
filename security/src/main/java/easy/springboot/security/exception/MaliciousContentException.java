package easy.springboot.security.exception;

public class MaliciousContentException extends EasySecurityException {

    public MaliciousContentException() {
        super("Malicious content detected");
    }
}
