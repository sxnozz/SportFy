/**
 * Adiciona um "ouvinte" que espera o documento HTML ser completamente
 * carregado antes de executar qualquer script. É a entrada principal
 * do script da página.
 */
document.addEventListener('DOMContentLoaded', function() {

    // --- Bloco 1: Funcionalidade do Formulário Dinâmico ---

    const modalidadeSelect = document.getElementById('modalidade');

    // Verifica se o <select> de modalidade existe na página atual
    if (modalidadeSelect) {

        /**
         * Função responsável por exibir/ocultar os campos de métrica
         * com base no esporte selecionado no dropdown.
         */
        const updateMetricFieldsVisibility = () => {
            const selectedSport = modalidadeSelect.value;
            
            // Normaliza o valor (remove acentos, converte para minúsculas)
            // para criar um ID de destino confiável.
            // Ex: "Futebol" -> "futebol"
            const normalizedSport = selectedSport ? selectedSport.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase() : '';
            
            // Constrói o ID do <div> que deve ser exibido (ex: "metricas_futebol")
            const targetId = 'metricas_' + normalizedSport;

            // Itera sobre todos os <div> com a classe .metric-fields
            document.querySelectorAll('.metric-fields').forEach(div => {
                // Se o ID do <div> for igual ao ID de destino, mostra.
                if (div.id === targetId) {
                    div.style.display = 'block';
                } else {
                    // Se não for, oculta.
                    div.style.display = 'none';
                }
            });
        };

        // Adiciona o evento "change" ao dropdown
        modalidadeSelect.addEventListener('change', updateMetricFieldsVisibility);
        
        // Executa a função uma vez no carregamento da página
        // para garantir que o estado inicial do formulário esteja correto.
        updateMetricFieldsVisibility();
    }

    // --- Bloco 2: Funcionalidade do Gráfico Interativo ---

    const metricCards = document.querySelectorAll('.metric-card');
    const chartPlaceholder = document.getElementById('chart-placeholder');
    const chartContainer = document.getElementById('radarChart');

    // Adiciona um evento de clique para cada "card de métrica"
    metricCards.forEach(card => {
        card.addEventListener('click', function() {
            try {
                // Tenta extrair os dados (labels e valores) dos atributos 'data-' do card clicado
                const labels = JSON.parse(this.dataset.labels);
                const values = JSON.parse(this.dataset.values);
                const sportData = { labels: labels, data: values };

                // Chama a função para desenhar/atualizar o gráfico com os novos dados
                drawRadarChart(sportData);

                // Oculta o placeholder (ex: "Clique em um card")
                if (chartPlaceholder) chartPlaceholder.style.display = 'none';
                // Exibe o <canvas> do gráfico
                if (chartContainer) chartContainer.style.display = 'block';

            } catch (e) {
                // Captura erros caso os dados JSON nos atributos 'data-' estejam mal formatados
                console.error("Erro ao processar dados do card para o gráfico:", e);
            }
        });
    });

    // Garante que o container do gráfico comece oculto
    if (chartContainer) {
        chartContainer.style.display = 'none';
    }
});


/**
 * Função principal que desenha ou atualiza o gráfico de radar (Radar Chart).
 * Utiliza a biblioteca Chart.js.
 * @param {object} sportData - Um objeto contendo { labels: [], data: [] }
 */
function drawRadarChart(sportData) {
    const ctx = document.getElementById('radarChart');
    if (!ctx || !sportData) return; // Aborta se o <canvas> ou os dados não existirem

    // Verifica se um gráfico já existe na variável global (window)
    // Se sim, destrói a instância anterior para evitar sobreposição
    if (window.myRadarChart) {
        window.myRadarChart.destroy();
    }

    // Armazena os dados atuais globalmente (útil para redimensionamento, etc.)
    window.currentChartData = sportData;

    // Verifica se o modo escuro está ativo no HTML
    const isDarkMode = document.documentElement.classList.contains('dark-mode');

    // --- Definição das Paletas de Cores (Dark/Light Mode) ---
    // Define cores diferentes para os elementos do gráfico com base no tema
    const pointLabelColor = isDarkMode ? 'rgba(226, 232, 240, 0.9)' : 'rgba(52, 58, 64, 1)';
    const gridColor = isDarkMode ? 'rgba(74, 85, 104, 0.7)' : 'rgba(200, 200, 200, 1)';
    const bgColor = isDarkMode ? 'rgba(99, 179, 237, 0.4)' : 'rgba(54, 162, 235, 0.5)';
    const borderColor = isDarkMode ? 'rgb(99, 179, 237)' : 'rgb(41, 128, 185)';
    const pointBorderColor = isDarkMode ? '#2d3748' : '#fff';

    // Verifica se o plugin 'ChartDataLabels' (para mostrar valores no gráfico) foi carregado
    if (typeof ChartDataLabels !== 'undefined') {
        // Registra o plugin no Chart.js
        Chart.register(ChartDataLabels);
    }

    // Cria a nova instância do gráfico e a armazena na variável global
    window.myRadarChart = new Chart(ctx, {
        type: 'radar',
        data: {
            labels: sportData.labels, // Nomes dos eixos (ex: "Força", "Velocidade")
            datasets: [{
                data: sportData.data, // Valores numéricos
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
            maintainAspectRatio: false, // Permite que o gráfico preencha o container
            plugins: {
                // Oculta a legenda padrão do Chart.js
                legend: { display: false },
                
                // Configura o plugin 'datalabels' (os números em cima dos pontos)
                datalabels: {
                    backgroundColor: 'rgba(0, 0, 0, 0.7)',
                    borderRadius: 4,
                    color: 'white',
                    font: { weight: 'bold' },
                    padding: 6,
                }
            },
            // Configuração dos eixos (escala 'r' = radial)
            scales: {
                r: {
                    // Configura os rótulos dos eixos (ex: "Força", "Velocidade")
                    pointLabels: {
                        font: { size: 14, weight: 'bold' },
                        color: pointLabelColor,
                        padding: 30 // Adiciona espaço para o texto não sobrepor o gráfico
                    },
                    // Configura as linhas da grade
                    grid: {
                        color: gridColor
                    },
                    // Configura os "ticks" (marcações de escala, ex: 0, 5, 10)
                    ticks: {
                        display: false, // Oculta os números da escala
                        backdropColor: 'transparent'
                    },
                    suggestedMin: 0, // Garante que a escala comece em 0
                    
                    // Define dinamicamente o valor máximo da escala
                    // Pega o maior valor dos dados (ou 10) e soma 5,
                    // para o gráfico não ficar "colado" na borda.
                    suggestedMax: Math.max(...sportData.data, 10) + 5
                }
            }
        }
    });
}