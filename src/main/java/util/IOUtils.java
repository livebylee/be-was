package util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class IOUtils {
    private IOUtils() {
    }

    public static byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024]; // 1kb

        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    // 파일을 읽어 String으로 반환
    public static String readResourceAsString(String resourcePath) {
        try (InputStream is = IOUtils.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                return null;
            }
            byte[] bytes = readAllBytes(is);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read resource: " + resourcePath, e);
        }
    }

    // 파일을 읽어 byte[]로 반환
    public static byte[] readResourceAsBytes(String resourcePath) {
        try (InputStream is = IOUtils.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                return null;
            }
            return readAllBytes(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read resource: " + resourcePath, e);
        }
    }

}
