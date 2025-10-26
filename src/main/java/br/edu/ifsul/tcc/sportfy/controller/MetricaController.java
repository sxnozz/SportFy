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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Controller
public class MetricaController {

    // Logger para mensagens de debug/info/warn.
    private static final Logger logger = LoggerFactory.getLogger(MetricaController.class);

    // Repositório para salvar, buscar e excluir métricas.
    @Autowired
    private MetricaRepository metricaRepository;

    // Exibe as métricas do usuário logado ordenadas da mais recente para a mais antiga.
    // Também prepara um objeto vazio para o formulário de nova métrica e o helper de gráficos.
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

    // Salva uma nova métrica associada ao usuário logado.
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

    // Exclui uma métrica se ela pertencer ao usuário logado.
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

    // Mostra o formulário de edição para uma métrica, somente se o usuário for o dono.
    @GetMapping("/metricas/editar/{id}")
    public String exibirFormularioEditar(@PathVariable Long id, Model model, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        Optional<Metrica> metricaOpt = metricaRepository.findById(id);
        if (metricaOpt.isPresent() && metricaOpt.get().getUsuario().getId_usuario().equals(usuarioLogado.getId_usuario())) {
            Metrica metrica = metricaOpt.get();

            logger.info("--- DEBUG SPORTFY --- Editando Métrica ID: {} | Esporte: {} | Data: {}",
                    metrica.getId_metrica(), metrica.getEsporte(), metrica.getDiaMetrica());

            model.addAttribute("metrica", metrica);
            model.addAttribute("activePage", "minhasMetricas");
            return "editar-metrica";
        }

        return "redirect:/minhas-metricas";
    }

    // Processa a edição da métrica — atualiza campos permitidos e salva.
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
