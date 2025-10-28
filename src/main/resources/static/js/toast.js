/**
 * Exibe um toast (notificação temporária no canto inferior direito).
 *
 * @param {string} message Texto a exibir.
 * @param {string} type    "success" | "info" | "warning" | "error"
 * @param {number} duration ms até sumir sozinho (default 5000ms)
 */
function showToast(message, type = 'info', duration = 5000) {
    // container onde ficam empilhados os toasts
    const container = document.getElementById('toast-container');
    if (!container) {
        console.warn('showToast: #toast-container não encontrado.');
        return;
    }

    // ícone simples por tipo
    let iconChar = 'ℹ';
    if (type === 'success') iconChar = '✔';
    if (type === 'warning') iconChar = '⚠';
    if (type === 'error')   iconChar = '✖';

    // cria o elemento raiz com classes que batem com o CSS (.toast e .toast--{type})
    const toastEl = document.createElement('div');
    toastEl.className = `toast toast--${type} toast-enter`;

    // estrutura interna compatível com o CSS:
    //   .toast__icon        -> ícone à esquerda
    //   .toast__body        -> texto
    //   .toast__close       -> botão fechar (x)
    //   .toast__progress    -> barrinha de tempo
    toastEl.innerHTML = `
        <div class="toast__icon">${iconChar}</div>
        <div class="toast__body">${message}</div>
        <button class="toast__close" aria-label="Fechar">&times;</button>
        <div class="toast__progress"></div>
    `;

    // pega barra de progresso pra ajustar a duração da animação
    const progressBar = toastEl.querySelector('.toast__progress');
    if (progressBar) {
        progressBar.style.animationDuration = duration + 'ms';
    }

    // botão X → fecha manualmente
    const closeBtn = toastEl.querySelector('.toast__close');
    closeBtn.addEventListener('click', () => {
        fecharToast(toastEl);
    });

    // coloca o toast no DOM (agora aparece na tela)
    container.appendChild(toastEl);

    // auto remover após 'duration'
    const autoRemove = setTimeout(() => {
        fecharToast(toastEl);
    }, duration);

    // anima saída, remove do DOM depois de ~200ms
    function fecharToast(el) {
        if (!el.classList.contains('toast-exit')) {
            el.classList.remove('toast-enter');
            el.classList.add('toast-exit');
            clearTimeout(autoRemove);

            setTimeout(() => {
                if (el && el.parentNode) {
                    el.parentNode.removeChild(el);
                }
            }, 220);
        }
    }
}
