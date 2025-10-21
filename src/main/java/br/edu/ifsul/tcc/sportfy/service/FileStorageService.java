// Novo arquivo: br/edu/ifsul/tcc/sportfy/service/FileStorageService.java
package br.edu.ifsul.tcc.sportfy.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    // Define o diretório onde as imagens serão salvas
    private final Path rootLocation = Paths.get("uploads");

    public FileStorageService() {
        try {
            // Cria o diretório se ele não existir
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    public String store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("Failed to store empty file.");
        }
        try {
            // Gera um nome de arquivo único para evitar conflitos
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFilename = UUID.randomUUID().toString() + extension;

            Path destinationFile = this.rootLocation.resolve(Paths.get(uniqueFilename))
                                                    .normalize().toAbsolutePath();

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
            // Retorna apenas o nome do arquivo salvo
            return uniqueFilename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file.", e);
        }
    }
}