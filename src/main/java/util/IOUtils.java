package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;


public class IOUtils {
    private static final Logger logger = LoggerFactory.getLogger(IOUtils.class);

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

    // 바이트 배열에서 특정 패턴의 시작 위치를 찾는 메서드
    public static int indexOf(byte[] data, byte[] pattern, int start) {
        for (int i = start; i <= data.length - pattern.length; i++) {
            boolean match = true;
            for (int j = 0; j < pattern.length; j++) {
                if (data[i + j] != pattern[j]) {
                    match = false;
                    break;
                }
            }
            if (match) return i;
        }
        return -1;
    }

    public static String saveFile(String fileName, byte[] data) {
        try {
            String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;

            String uploadDir = "img_uploads/";
            java.nio.file.Path path = java.nio.file.Paths.get(uploadDir + uniqueFileName);

            java.nio.file.Files.createDirectories(path.getParent());
            java.nio.file.Files.write(path, data);

            logger.debug("파일 저장 완료: {}", uniqueFileName);
            return uniqueFileName;
        } catch (IOException e) {
            logger.error("파일 저장 실패: {}", e.getMessage());
            return "";
        }

    }
}
