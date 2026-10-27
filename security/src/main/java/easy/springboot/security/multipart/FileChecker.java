package easy.springboot.security.multipart;

import easy.springboot.security.config.BaseSecurityConfigurationSpec;
import jakarta.servlet.http.Part;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

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
            String detectedMimeType = tika.detect(inputStream);
            return validFileMimeTypes
                    .stream()
                    .anyMatch(mimeType -> mimeType.getMimeTypes().contains(detectedMimeType));
        } catch (Exception e) {
            return false;
        }
    }

}
