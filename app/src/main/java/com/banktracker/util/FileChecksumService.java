package com.banktracker.util;

import com.banktracker.exceptions.CsvImportException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
import java.util.HexFormat;

@Service
public class FileChecksumService {
    public String sha256(MultipartFile file) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(file.getBytes());

            return HexFormat.of().formatHex(hash);

        } catch (Exception e) {
            throw new CsvImportException(
                    "Could not calculate file checksum",
                    e
            );
        }
    }
}
