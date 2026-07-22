// Controla visibilidade dos campos por esporte e interação dos cards de métrica
document.addEventListener('DOMContentLoaded', function() {

    // Formulário dinâmico: mostra apenas campos do esporte selecionado
    const modalidadeSelect = document.getElementById('modalidade');

    if (modalidadeSelect) {

        const updateMetricFieldsVisibility = () => {
            const selectedSport = modalidadeSelect.value;

            const normalizedSport = selectedSport
                ? selectedSport
                    .normalize('NFD')
                    .replace(/[\u0300-\u036f]/g, '')
                    .toLowerCase()
                : '';

            const targetId = 'metricas_' + normalizedSport;

            document.querySelectorAll('.metric-fields').forEach(div => {
                if (div.id === targetId) {
                    div.style.display = 'block';
                } else {
                    div.style.display = 'none';
                }
            });
        };

        modalidadeSelect.addEventListener('change', updateMetricFieldsVisibility);
        updateMetricFieldsVisibility();
    }

    // Interação dos cards: clique atualiza gráfico e destaca o card
    const metricCards = document.querySelectorAll('.metric-card');
    const chartPlaceholder = document.getElementById('chart-placeholder');
    const chartCanvas = document.getElementById('radarChart');

    if (chartCanvas) {
        chartCanvas.style.display = 'none';
    }

    metricCards.forEach(card => {
        card.addEventListener('click', function() {
            try {
                const labels = JSON.parse(this.dataset.labels);
                const values = JSON.parse(this.dataset.values);

                const sportData = {
                    labels: labels,
                    data: values
                };

                drawRadarChart(sportData);

                metricCards.forEach(c => c.classList.remove('selected'));
                this.classList.add('selected');

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

// Desenha/atualiza o gráfico radar (Chart.js), com cores por tema
function drawRadarChart(sportData) {
    const ctx = document.getElementById('radarChart');
    if (!ctx || !sportData) return;

    if (window.myRadarChart) {
        window.myRadarChart.destroy();
    }

    window.currentChartData = sportData;

    const isDarkMode = document.documentElement.classList.contains('dark-mode');

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

    if (typeof ChartDataLabels !== 'undefined') {
        Chart.register(ChartDataLabels);
    }

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
                legend: { display: false },
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
                        font: { size: 14, weight: 'bold' },
                        color: pointLabelColor,
                        padding: 30
                    },
                    grid: {
                        color: gridColor
                    },
                    ticks: {
                        display: false,
                        backdropColor: 'transparent'
                    },
                    suggestedMin: 0,
                    suggestedMax: Math.max(...sportData.data, 10) + 5
                }
            }
        }
    });
}
