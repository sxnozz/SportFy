// ---------------------------------------------------------
// THEME.JS
// Responsável por:
//  - Alternar entre modo claro e escuro (classe .dark-mode no <html>)
//  - Salvar a escolha no localStorage, assim ela persiste em todas as páginas
//  - Redesenhar o gráfico radar (se já estiver visível) para atualizar as cores
// ---------------------------------------------------------

document.addEventListener('DOMContentLoaded', () => {

    // Checkbox do header que liga/desliga o tema (o "switch" de lua/sol)
    const toggleSwitch = document.querySelector('#checkbox');

    // Ajusta visualmente o estado inicial do switch:
    // se "dark-mode" estiver salvo no navegador, marca o checkbox.
    if (localStorage.getItem('theme') === 'dark-mode') {
        toggleSwitch.checked = true;
    }

    // Quando o usuário clica para trocar o tema
    toggleSwitch.addEventListener('change', function () {

        // Alterna a classe 'dark-mode' no elemento <html>
        // O CSS usa essa classe pra mudar todas as cores da interface
        document.documentElement.classList.toggle('dark-mode');

        // Atualiza o localStorage para manter a preferência nas próximas páginas
        if (this.checked) {
            localStorage.setItem('theme', 'dark-mode');
        } else {
            localStorage.setItem('theme', 'light-mode');
        }

        // Se um gráfico radar já foi desenhado, redesenha com nova paleta de cores
        // (drawRadarChart está definida em main.js)
        if (
            typeof drawRadarChart === 'function' &&
            window.myRadarChart &&
            window.currentChartData
        ) {
            drawRadarChart(window.currentChartData);
        }
    });
});
