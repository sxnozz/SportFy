package br.edu.ifsul.tcc.sportfy.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.edu.ifsul.tcc.sportfy.model.Metrica;

import java.util.List;

public class ChartDataHelper {

    // Retorna um JSON (string) com os rótulos apropriados para o esporte.
    // Entrada: nome do esporte (String). Saída: String contendo um array JSON de labels.
    public String getLabelsAsJson(String esporte) {
        if ("Futebol".equals(esporte)) return "[\"Gols\", \"Assistências\", \"Desarmes\"]";
        if ("Basquete".equals(esporte)) return "[\"Pontos\", \"Assistências\", \"Rebotes\"]";
        if ("Vôlei".equals(esporte)) return "[\"Pontos\", \"Aces\", \"Bloqueios\"]";
        return "[]";
    }

    // Constrói uma lista de valores inteiros a partir do objeto Metrica (substitui nulos por 0)
    // e a serializa para JSON. Retorna "[]" se ocorrer erro na serialização.
    // Entrada: Metrica metrica — contém valores por esporte. Saída: String JSON com os valores.
    public String getValuesAsJson(Metrica metrica) {
        List<Integer> values;
        // CORREÇÃO: Verifica se os valores são nulos e, se forem, usa 0
        if ("Futebol".equals(metrica.getEsporte())) {
            values = List.of(
                metrica.getGols() != null ? metrica.getGols() : 0,
                metrica.getAssistencias_futebol() != null ? metrica.getAssistencias_futebol() : 0,
                metrica.getDesarmes() != null ? metrica.getDesarmes() : 0
            );
        } else if ("Basquete".equals(metrica.getEsporte())) {
            values = List.of(
                metrica.getPontos_basquete() != null ? metrica.getPontos_basquete() : 0,
                metrica.getAssistencias_basquete() != null ? metrica.getAssistencias_basquete() : 0,
                metrica.getRebotes() != null ? metrica.getRebotes() : 0
            );
        } else if ("Vôlei".equals(metrica.getEsporte())) {
            values = List.of(
                metrica.getPontos_volei() != null ? metrica.getPontos_volei() : 0,
                metrica.getAces() != null ? metrica.getAces() : 0,
                metrica.getBloqueios() != null ? metrica.getBloqueios() : 0
            );
        } else {
            values = List.of();
        }
        
        try {
            return new ObjectMapper().writeValueAsString(values);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
}
