package easy.springboot.security.multipart;

import easy.springboot.security.config.BaseSecurityConfigurationSpec;
import jakarta.servlet.http.Part;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Checks a multipart part's detected MIME type against the configured allowlist.
 *
 * <p>The allowlist is configured by file extension, then resolved to MIME types.
 * Detection is performed on the part's content with Apache Tika rather than by
 * trusting the submitted filename or the part's declared content type.</p>
 */
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

    /**
     * Returns whether this part is allowed by the configured MIME-type list.
     *
     * <p>As currently implemented, null and zero-byte parts are accepted. A
     * detection or stream-reading failure rejects a non-empty part.</p>
     *
     * @param part multipart part to inspect
     * @return {@code true} when accepted by the current policy; otherwise {@code false}
     */
    public boolean check(Part part) {
        if (part == null || part.getSize() == 0) {
            return true;
        }
        try (InputStream inputStream = part.getInputStream()) {
            // Detect once: detection reads the stream, so do not repeat it for each allowlist entry.
            String detectedMimeType = tika.detect(inputStream);
            return validFileMimeTypes
                    .stream()
                    .anyMatch(mimeType -> mimeType.getMimeTypes().contains(detectedMimeType));
        } catch (Exception e) {
            // Fail closed when the part cannot be read or its content cannot be checked.
            return false;
        }
    }

}
