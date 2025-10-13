document.addEventListener('DOMContentLoaded', function() {
    
    // ### INÍCIO DA CORREÇÃO ###
    // Funcionalidade 1: Formulário dinâmico (Versão Corrigida)
    const modalidadeSelect = document.getElementById('modalidade');
    if (modalidadeSelect) {
        
        const updateMetricFieldsVisibility = () => {
            const selectedSport = modalidadeSelect.value;
            // Normaliza o valor para criar o ID de destino (ex: 'metricas_futebol')
            const normalizedSport = selectedSport ? selectedSport.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase() : '';
            const targetId = 'metricas_' + normalizedSport;

            // Itera sobre todos os campos de métrica
            document.querySelectorAll('.metric-fields').forEach(div => {
                // Se o ID da div for o alvo, mostra. Senão, esconde.
                if (div.id === targetId) {
                    div.style.display = 'block';
                } else {
                    div.style.display = 'none';
                }
            });
        };

        // Adiciona o evento que chama a função sempre que o usuário mudar a seleção
        modalidadeSelect.addEventListener('change', updateMetricFieldsVisibility);
        
        // Chama a função uma vez no carregamento da página para definir o estado inicial correto
        updateMetricFieldsVisibility();
    }
    // ### FIM DA CORREÇÃO ###

    // Funcionalidade 2: Gráfico interativo (Seu código original, sem alterações)
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

    // O ChartDataLabels já deve estar registrado globalmente, mas para garantir:
    // Se estiver usando Chart.js v3+, você pode precisar registrar plugins assim.
    // Se der erro, pode remover esta linha.
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
                backgroundColor: 'rgba(54, 162, 235, 0.2)',
                borderColor: 'rgb(54, 162, 235)',
                pointBackgroundColor: 'rgb(54, 162, 235)',
                pointBorderColor: '#fff',
            }]
        },
        options: {
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                // A configuração de datalabels deve estar dentro de 'plugins'
                datalabels: {
                    backgroundColor: 'rgba(54, 162, 235, 0.8)',
                    borderRadius: 4,
                    color: 'white',
                    font: { weight: 'bold' },
                    padding: 6,
                    anchor: 'end',
                    align: 'end',
                    offset: 8
                }
            },
            scales: {
                r: { 
                    pointLabels: {
                        font: { size: 16, weight: 'bold' },
                        color: '#333',
                        padding: 40
                    },
                    grid: { 
                        color: '#d3d3d3'
                    },
                    ticks: { display: false },
                    suggestedMin: 0,
                    suggestedMax: 15
                }
            }
        }
    });
}