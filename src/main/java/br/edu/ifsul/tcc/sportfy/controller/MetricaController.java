package br.edu.ifsul.tcc.sportfy.controller;

import br.edu.ifsul.tcc.sportfy.model.Metrica;
import br.edu.ifsul.tcc.sportfy.model.Usuario;
import br.edu.ifsul.tcc.sportfy.repository.MetricaRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Optional;

@Controller
public class MetricaController {

    @Autowired
    private MetricaRepository metricaRepository;

    @GetMapping("/minhas-metricas")
    public String exibirMinhasMetricas(Model model, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        List<Metrica> metricas = metricaRepository.findByUsuarioOrderByDiaMetricaDesc(usuarioLogado);
        
        model.addAttribute("metricas", metricas);
        model.addAttribute("novaMetrica", new Metrica());
        model.addAttribute("activePage", "minhasMetricas");
        model.addAttribute("chartHelper", new ChartDataHelper());
        
        return "minhas-metricas";
    }

    @PostMapping("/metricas/nova")
    public String salvarNovaMetrica(@ModelAttribute Metrica novaMetrica, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }
        novaMetrica.setUsuario(usuarioLogado);
        metricaRepository.save(novaMetrica);
        return "redirect:/minhas-metricas";
    }

    @PostMapping("/metricas/excluir/{id}")
    public String excluirMetrica(@PathVariable Long id, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        metricaRepository.findById(id).ifPresent(metrica -> {
            if (metrica.getUsuario().getId_usuario().equals(usuarioLogado.getId_usuario())) {
                metricaRepository.deleteById(id);
            }
        });

        return "redirect:/minhas-metricas";
    }

    // --- MÉTODOS PARA EDITAR MÉTRICA ---

    @GetMapping("/metricas/editar/{id}")
public String exibirFormularioEditar(@PathVariable Long id, Model model, HttpSession session) {
    Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
    if (usuarioLogado == null) {
        return "redirect:/login";
    }

    Optional<Metrica> metricaOpt = metricaRepository.findById(id);
    if (metricaOpt.isPresent() && metricaOpt.get().getUsuario().getId_usuario().equals(usuarioLogado.getId_usuario())) {
        Metrica metrica = metricaOpt.get(); // Pega a métrica para facilitar o debug

        // ADICIONE ESTA LINHA PARA DEBUGAR
        System.out.println("--- DEBUG SPORTFY --- Editando Métrica ID: " + metrica.getId_metrica() + " | Esporte: " + metrica.getEsporte() + " | Data: " + metrica.getDiaMetrica());

        model.addAttribute("metrica", metrica);
        model.addAttribute("activePage", "minhasMetricas");
        return "editar-metrica";
    }
    
    return "redirect:/minhas-metricas";
}

    @PostMapping("/metricas/editar/{id}")
    public String processarEdicaoMetrica(@PathVariable Long id, @ModelAttribute Metrica metricaAtualizada, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        Optional<Metrica> metricaOpt = metricaRepository.findById(id);
        if (metricaOpt.isPresent() && metricaOpt.get().getUsuario().getId_usuario().equals(usuarioLogado.getId_usuario())) {
            Metrica metricaOriginal = metricaOpt.get();
            
            metricaOriginal.setDiaMetrica(metricaAtualizada.getDiaMetrica());
            metricaOriginal.setEsporte(metricaAtualizada.getEsporte());
            
            metricaOriginal.setGols(metricaAtualizada.getGols());
            metricaOriginal.setAssistencias_futebol(metricaAtualizada.getAssistencias_futebol());
            metricaOriginal.setDesarmes(metricaAtualizada.getDesarmes());
            
            metricaOriginal.setPontos_basquete(metricaAtualizada.getPontos_basquete());
            metricaOriginal.setAssistencias_basquete(metricaAtualizada.getAssistencias_basquete());
            metricaOriginal.setRebotes(metricaAtualizada.getRebotes());

            metricaOriginal.setPontos_volei(metricaAtualizada.getPontos_volei());
            metricaOriginal.setAces(metricaAtualizada.getAces());
            metricaOriginal.setBloqueios(metricaAtualizada.getBloqueios());
            
            metricaRepository.save(metricaOriginal);
        }

        return "redirect:/minhas-metricas";
    }


    
}