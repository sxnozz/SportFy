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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Controller
public class MetricaController {

    private static final Logger logger = LoggerFactory.getLogger(MetricaController.class);

    @Autowired
    private MetricaRepository metricaRepository;

    // Lista histórico e prepara formulário
    @GetMapping("/minhas-metricas")
    public String exibirMinhasMetricas(Model model, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) return "redirect:/login";

        List<Metrica> metricas = metricaRepository.findByUsuarioOrderByDiaMetricaDesc(usuarioLogado);

        model.addAttribute("metricas", metricas);
        model.addAttribute("novaMetrica", new Metrica());
        model.addAttribute("activePage", "minhasMetricas");
        model.addAttribute("chartHelper", new ChartDataHelper());

        return "minhas-metricas";
    }

    // Salva nova métrica para o usuário logado
    @PostMapping("/metricas/nova")
    public String salvarNovaMetrica(@ModelAttribute Metrica novaMetrica,
                                    HttpSession session,
                                    RedirectAttributes ra) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) return "redirect:/login";

        novaMetrica.setUsuario(usuarioLogado);
        metricaRepository.save(novaMetrica);

        ra.addFlashAttribute("toastMessage", "Métrica salva com sucesso!");
        return "redirect:/minhas-metricas";
    }

    // Exclui métrica se for do usuário
    @PostMapping("/metricas/excluir/{id}")
    public String excluirMetrica(@PathVariable Long id,
                                 HttpSession session,
                                 RedirectAttributes ra) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) return "redirect:/login";

        metricaRepository.findById(id).ifPresent(m -> {
            if (m.getUsuario().getId_usuario().equals(usuarioLogado.getId_usuario())) {
                metricaRepository.deleteById(id);
            }
        });

        ra.addFlashAttribute("toastMessage", "Métrica excluída.");
        return "redirect:/minhas-metricas";
    }

    // Formulário de edição (somente dono)
    @GetMapping("/metricas/editar/{id}")
    public String exibirFormularioEditar(@PathVariable Long id,
                                         Model model,
                                         HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) return "redirect:/login";

        Optional<Metrica> metricaOpt = metricaRepository.findById(id);
        if (metricaOpt.isPresent()
                && metricaOpt.get().getUsuario().getId_usuario().equals(usuarioLogado.getId_usuario())) {

            Metrica metrica = metricaOpt.get();
            logger.info("Editando Métrica ID: {} | Esporte: {} | Data: {}",
                    metrica.getId_metrica(), metrica.getEsporte(), metrica.getDiaMetrica());

            model.addAttribute("metrica", metrica);
            model.addAttribute("activePage", "minhasMetricas");
            return "editar-metrica";
        }

        return "redirect:/minhas-metricas";
    }

    // Atualiza métrica (somente dono)
    @PostMapping("/metricas/editar/{id}")
    public String processarEdicaoMetrica(@PathVariable Long id,
                                         @ModelAttribute Metrica metricaAtualizada,
                                         HttpSession session,
                                         RedirectAttributes ra) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) return "redirect:/login";

        Optional<Metrica> metricaOpt = metricaRepository.findById(id);
        if (metricaOpt.isPresent()
                && metricaOpt.get().getUsuario().getId_usuario().equals(usuarioLogado.getId_usuario())) {

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

            ra.addFlashAttribute("toastMessage", "Métrica atualizada com sucesso!");
        }

        return "redirect:/minhas-metricas";
    }
}
