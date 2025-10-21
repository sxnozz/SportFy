// Em /js/theme.js

document.addEventListener('DOMContentLoaded', () => {
    const toggleSwitch = document.querySelector('#checkbox');

    // Verifica o estado atual para marcar ou desmarcar o botão na carga da página
    if (localStorage.getItem('theme') === 'dark-mode') {
        toggleSwitch.checked = true;
    }

    // Adiciona o evento de clique no botão
    toggleSwitch.addEventListener('change', function() {
        // Adiciona ou remove a classe da tag <html>
        document.documentElement.classList.toggle('dark-mode');

        if (this.checked) {
            localStorage.setItem('theme', 'dark-mode');
        } else {
            localStorage.setItem('theme', 'light-mode');
        }

        // ### INÍCIO DA CORREÇÃO ###
        // Se um gráfico estiver sendo exibido na tela (identificado pelas variáveis globais),
        // chama a função para redesenhá-lo com as cores do novo tema.
        if (typeof drawRadarChart === 'function' && window.myRadarChart && window.currentChartData) {
            drawRadarChart(window.currentChartData);
        }
        // ### FIM DA CORREÇÃO ###
    });
});