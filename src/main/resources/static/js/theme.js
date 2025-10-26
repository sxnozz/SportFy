/**
 * Espera o documento HTML ser completamente carregado para
 * inicializar a lógica de troca de tema (Dark/Light Mode).
 */
document.addEventListener('DOMContentLoaded', () => {
    
    // Seleciona o <input type="checkbox"> que funciona como botão "toggle"
    const toggleSwitch = document.querySelector('#checkbox');

    // Verifica no carregamento da página se o tema 'dark-mode'
    // está salvo no localStorage (armazenamento do navegador).
    if (localStorage.getItem('theme') === 'dark-mode') {
        // Se estiver, marca o checkbox como "ativo"
        toggleSwitch.checked = true;
    }

    // Adiciona um "ouvinte" para o evento 'change' (quando o checkbox é clicado)
    toggleSwitch.addEventListener('change', function() {
        
        // Adiciona ou remove a classe 'dark-mode' da tag <html> (document.documentElement)
        // Isso permite que o CSS aplique os estilos do tema correto.
        document.documentElement.classList.toggle('dark-mode');

        // Verifica se o checkbox foi marcado ou desmarcado
        if (this.checked) {
            // Se foi marcado (agora está em modo escuro), salva a preferência
            localStorage.setItem('theme', 'dark-mode');
        } else {
            // Se foi desmarcado (agora está em modo claro), salva a preferência
            localStorage.setItem('theme', 'light-mode');
        }

        // --- Atualização do Gráfico ---
        
        // Verifica se:
        // 1. A função 'drawRadarChart' existe.
        // 2. Um gráfico ('window.myRadarChart') já foi desenhado.
        // 3. Os dados ('window.currentChartData') estão disponíveis.
        if (typeof drawRadarChart === 'function' && window.myRadarChart && window.currentChartData) {
            
            // Se tudo for verdadeiro, chama a função para redesenhar o gráfico
            // A função 'drawRadarChart' vai detectar o novo tema e usar as cores corretas.
            drawRadarChart(window.currentChartData);
        }
    });
});