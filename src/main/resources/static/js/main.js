document.addEventListener('DOMContentLoaded', function() {
    
    // Funcionalidade 1: Formulário dinâmico (Seu código original, sem alterações)
    const modalidadeSelect = document.getElementById('modalidade');
    if (modalidadeSelect) {
        
        const updateMetricFieldsVisibility = () => {
            const selectedSport = modalidadeSelect.value;
            const normalizedSport = selectedSport ? selectedSport.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase() : '';
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

    // Funcionalidade 2: Gráfico interativo (Configuração inicial)
    const metricCards = document.querySelectorAll('.metric-card');
    const chartPlaceholder = document.getElementById('chart-placeholder');
    const chartContainer = document.getElementById('radarChart');

    metricCards.forEach(card => {
        card.addEventListener('click', function() {
            try {
                const labels = JSON.parse(this.dataset.labels);
                const values = JSON.parse(this.dataset.values);
                const sportData = { labels: labels, data: values };
                
                drawRadarChart(sportData);

                if(chartPlaceholder) chartPlaceholder.style.display = 'none';
                if(chartContainer) chartContainer.style.display = 'block';

            } catch (e) {
                console.error("Erro ao processar dados do card para o gráfico:", e);
            }
        });
    });

    if (chartContainer) {
        chartContainer.style.display = 'none';
    }
});


function drawRadarChart(sportData) {
    const ctx = document.getElementById('radarChart');
    if (!ctx || !sportData) return;

    if (window.myRadarChart) {
        window.myRadarChart.destroy();
    }
    
    window.currentChartData = sportData;

    const isDarkMode = document.documentElement.classList.contains('dark-mode');

    // ### INÍCIO DA CORREÇÃO (Cores e Layout) ###

    // Paletas de cores ajustadas para melhor contraste
    const pointLabelColor = isDarkMode ? 'rgba(226, 232, 240, 0.9)' : 'rgba(52, 58, 64, 1)';
    const gridColor       = isDarkMode ? 'rgba(74, 85, 104, 0.7)' : 'rgba(200, 200, 200, 1)'; // Grade um pouco mais visível
    const bgColor         = isDarkMode ? 'rgba(99, 179, 237, 0.4)'  : 'rgba(54, 162, 235, 0.5)';  // Área preenchida mais forte no modo claro
    const borderColor     = isDarkMode ? 'rgb(99, 179, 237)'        : 'rgb(41, 128, 185)';         // Borda mais escura no modo claro
    const pointBorderColor= isDarkMode ? '#2d3748' : '#fff';

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
                borderWidth: 2, // Deixa a linha da borda um pouco mais grossa
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
                // Datalabels (os números) agora ficam com um fundo mais sutil
                datalabels: {
                    backgroundColor: 'rgba(0, 0, 0, 0.7)',
                    borderRadius: 4,
                    color: 'white',
                    font: { weight: 'bold' },
                    padding: 6,
                }
            },
            scales: {
                r: { 
                    pointLabels: {
                        font: { size: 14, weight: 'bold' },
                        color: pointLabelColor,
                        // Adiciona um espaçamento para o gráfico não sobrepor o texto
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
                    // Define um valor máximo para a escala, o que também ajuda a criar espaço
                    suggestedMax: Math.max(...sportData.data, 10) + 5 
                }
            }
        }
    });
    // ### FIM DA CORREÇÃO ###
}