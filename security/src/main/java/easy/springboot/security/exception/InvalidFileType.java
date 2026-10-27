package easy.springboot.security.exception;

public class InvalidFileType extends EasySecurityException {

    public InvalidFileType() {
        super("Invalid file type");
    }
}
