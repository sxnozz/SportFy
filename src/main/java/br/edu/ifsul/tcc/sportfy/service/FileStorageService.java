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

    // Diretório onde as imagens serão salvas
    private final Path rootLocation = Paths.get("uploads");

    public FileStorageService() {
        try {
            // Cria a pasta uploads se não existir
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível inicializar o storage", e);
        }
    }

    public String store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("Não é possível salvar arquivo vazio.");
        }
        try {
            // Gera nome único para evitar sobrescrever arquivos
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFilename = UUID.randomUUID().toString() + extension;

            Path destinationFile = this.rootLocation.resolve(Paths.get(uniqueFilename))
                                                    .normalize()
                                                    .toAbsolutePath();

            // Copia o arquivo para o destino
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            // Retorna o nome do arquivo salvo
            return uniqueFilename;
        } catch (IOException e) {
            throw new RuntimeException("Falha ao salvar arquivo.", e);
        }
    }
}
