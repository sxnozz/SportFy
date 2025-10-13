package br.edu.ifsul.tcc.sportfy.controller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan; // NOVO IMPORT
import org.springframework.data.jpa.repository.config.EnableJpaRepositories; // NOVO IMPORT
import org.springframework.boot.autoconfigure.domain.EntityScan; // NOVO IMPORT

@SpringBootApplication
// --- ADICIONE ESTAS 3 ANOTAÇÕES ---
@ComponentScan(basePackages = {"br.edu.ifsul.tcc.sportfy"})
@EnableJpaRepositories(basePackages = {"br.edu.ifsul.tcc.sportfy.repository"})
@EntityScan(basePackages = {"br.edu.ifsul.tcc.sportfy.model"})
public class SportfyApplication {

    public static void main(String[] args) {
        SpringApplication.run(SportfyApplication.class, args);
    }

}


//teste do commit