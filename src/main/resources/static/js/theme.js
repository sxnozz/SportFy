// Theme toggle: alterna claro/escuro, salva em localStorage e redesenha gráfico se necessário
document.addEventListener('DOMContentLoaded', () => {

    const toggleSwitch = document.querySelector('#checkbox'); 

    if (localStorage.getItem('theme') === 'dark-mode') {
        toggleSwitch.checked = true;
    }

    toggleSwitch.addEventListener('change', function () {

        document.documentElement.classList.toggle('dark-mode'); 

        if (this.checked) {
            localStorage.setItem('theme', 'dark-mode'); 
        } else {
            localStorage.setItem('theme', 'light-mode');
        }

        // Se o radar já existe, redesenha com nova paleta
        if (
            typeof drawRadarChart === 'function' &&
            window.myRadarChart &&
            window.currentChartData
        ) {
            drawRadarChart(window.currentChartData);
        }
    });
});
