/**
 * Este script cuida de duas coisas principais:
 * 1. Exibir os campos corretos de métricas conforme o esporte escolhido.
 * 2. Deixar cada card de métrica clicável: quando clica,
 *    - atualiza o gráfico radar
 *    - destaca visualmente o card selecionado
 *    - esconde a mensagem "Nenhuma métrica selecionada"
 */

document.addEventListener('DOMContentLoaded', function() {

    /* ==========================================================
     * BLOCO 1: FORMULÁRIO DINÂMICO DE "ADICIONAR NOVA MÉTRICA"
     * Mostra apenas os campos do esporte selecionado (futebol/basquete/vôlei).
     * Esse bloco só roda nas páginas que têm o <select id="modalidade">,
     * então não quebra outras telas.
     * ========================================================== */
    const modalidadeSelect = document.getElementById('modalidade');

    if (modalidadeSelect) {

        /**
         * Mostra o grupo de inputs correspondente ao esporte escolhido e
         * esconde os outros.
         */
        const updateMetricFieldsVisibility = () => {
            const selectedSport = modalidadeSelect.value;

            // Normaliza o texto pra gerar o ID dos blocos
            // Ex: "Vôlei" -> "volei"
            const normalizedSport = selectedSport
                ? selectedSport
                    .normalize('NFD')
                    .replace(/[\u0300-\u036f]/g, '') // remove acentos
                    .toLowerCase()
                : '';

            // Ex: "metricas_volei", "metricas_futebol"
            const targetId = 'metricas_' + normalizedSport;

            // Esconde todos os blocos e mostra só o correspondente
            document.querySelectorAll('.metric-fields').forEach(div => {
                if (div.id === targetId) {
                    div.style.display = 'block';
                } else {
                    div.style.display = 'none';
                }
            });
        };

        // Atualiza quando troca o select
        modalidadeSelect.addEventListener('change', updateMetricFieldsVisibility);

        // Atualiza já no carregamento (caso já venha preenchido em edição, por ex.)
        updateMetricFieldsVisibility();
    }

    /* ==========================================================
     * BLOCO 2: INTERAÇÃO DOS CARDS DE MÉTRICA + GRÁFICO RADAR
     * - Quando o usuário clica em um card:
     *   -> Lê os dados do card (labels/values)
     *   -> Desenha o gráfico radar (drawRadarChart)
     *   -> Marca visualmente esse card como "selecionado"
     *   -> Mostra o gráfico e esconde o placeholder
     * ========================================================== */

    const metricCards = document.querySelectorAll('.metric-card'); // todos os cards
    const chartPlaceholder = document.getElementById('chart-placeholder'); // texto "nenhuma métrica selecionada"
    const chartCanvas = document.getElementById('radarChart'); // o <canvas> do gráfico

    // Garante que o canvas começa oculto, e só aparece depois do 1º clique
    if (chartCanvas) {
        chartCanvas.style.display = 'none';
    }

    // Adiciona clique em cada card
    metricCards.forEach(card => {
        card.addEventListener('click', function() {
            try {
                // 1. Pega dados do card clicado
                const labels = JSON.parse(this.dataset.labels);   // ex: ["Gols","Assistências","Desarmes"]
                const values = JSON.parse(this.dataset.values);   // ex: [2,1,5]

                const sportData = {
                    labels: labels,
                    data: values
                };

                // 2. Atualiza o gráfico radar com esses dados
                drawRadarChart(sportData);

                // 3. Marca visualmente este card como "selecionado" e
                //    remove o destaque dos outros.
                metricCards.forEach(c => c.classList.remove('selected'));
                this.classList.add('selected');

                // 4. Esconde o placeholder e mostra o gráfico
                if (chartPlaceholder) {
                    chartPlaceholder.style.display = 'none';
                }
                if (chartCanvas) {
                    chartCanvas.style.display = 'block';
                }

            } catch (e) {
                console.error("Erro ao processar dados do card para o gráfico:", e);
            }
        });
    });
});


/* ==========================================================
 * FUNÇÃO GLOBAL: drawRadarChart
 * Desenha ou atualiza o gráfico de radar usando Chart.js.
 * Também ajusta as cores de acordo com o tema atual (claro/escuro).
 * É chamada sempre que o usuário clica em um card de métrica.
 * ========================================================== */
function drawRadarChart(sportData) {
    const ctx = document.getElementById('radarChart');
    if (!ctx || !sportData) return;

    // Se já existe um gráfico desenhado, destrói antes de redesenhar
    if (window.myRadarChart) {
        window.myRadarChart.destroy();
    }

    // Guarda os dados do gráfico atual globalmente
    // (isso é usado quando o usuário troca o tema claro/escuro)
    window.currentChartData = sportData;

    // Checa se está no modo escuro (html.dark-mode)
    const isDarkMode = document.documentElement.classList.contains('dark-mode');

    // Paleta de cores dinâmica por tema
    const pointLabelColor = isDarkMode
        ? 'rgba(226, 232, 240, 0.9)'
        : 'rgba(52, 58, 64, 1)';

    const gridColor = isDarkMode
        ? 'rgba(74, 85, 104, 0.7)'
        : 'rgba(200, 200, 200, 1)';

    const bgColor = isDarkMode
        ? 'rgba(99, 179, 237, 0.4)'
        : 'rgba(54, 162, 235, 0.5)';

    const borderColor = isDarkMode
        ? 'rgb(99, 179, 237)'
        : 'rgb(41, 128, 185)';

    const pointBorderColor = isDarkMode
        ? '#2d3748'
        : '#fff';

    // Registra o plugin de datalabels, se estiver disponível
    if (typeof ChartDataLabels !== 'undefined') {
        Chart.register(ChartDataLabels);
    }

    // Cria o radar chart
    window.myRadarChart = new Chart(ctx, {
        type: 'radar',
        data: {
            labels: sportData.labels,
            datasets: [{
                data: sportData.data,
                fill: true,
                backgroundColor: bgColor,
                borderColor: borderColor,
                borderWidth: 2,
                pointBackgroundColor: borderColor,
                pointBorderColor: pointBorderColor,
                pointHoverBackgroundColor: '#fff',
                pointHoverBorderColor: borderColor,
                pointBorderWidth: 2,
                pointRadius: 4
            }]
        },
        options: {
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false }, // esconde legenda "Dataset 1"
                datalabels: {
                    backgroundColor: 'rgba(0, 0, 0, 0.7)',
                    borderRadius: 4,
                    color: 'white',
                    font: { weight: 'bold' },
                    padding: 6
                }
            },
            scales: {
                r: {
                    pointLabels: {
                        // nomes dos eixos (ex: Gols, Assistências...)
                        font: { size: 14, weight: 'bold' },
                        color: pointLabelColor,
                        padding: 30
                    },
                    grid: {
                        color: gridColor
                    },
                    ticks: {
                        display: false, // some com os números 0,5,10...
                        backdropColor: 'transparent'
                    },
                    suggestedMin: 0,
                    // define limite máximo da escala com uma folguinha
                    suggestedMax: Math.max(...sportData.data, 10) + 5
                }
            }
        }
    });
}
