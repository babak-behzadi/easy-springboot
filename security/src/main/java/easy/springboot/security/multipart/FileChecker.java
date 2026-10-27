package easy.springboot.security.multipart;

import easy.springboot.security.config.BaseSecurityConfigurationSpec;
import jakarta.servlet.http.Part;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class FileChecker {

    private final Tika tika = new Tika();
    private final CopyOnWriteArrayList<FileMimeType> validFileMimeTypes;

    public FileChecker(BaseSecurityConfigurationSpec configurationSpec) {
        this.validFileMimeTypes = new CopyOnWriteArrayList<>(
                Arrays
                        .stream(configurationSpec.multipart().fileExtensions())
                        .map(FileMimeType::findByExtension)
                        .toList()
        );
    }

    public boolean check(Part part) {
        if (part == null || part.getSize() == 0) {
            return true;
        }
        try (InputStream inputStream = part.getInputStream()) {
            return validFileMimeTypes
                    .stream()
                    .anyMatch(mimeType -> {
                        try {
                            return mimeType.getMimeTypes().contains(tika.detect(inputStream));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (Exception e) {
            return false;
        }
    }

}
