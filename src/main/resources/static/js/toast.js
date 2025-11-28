// Exibe um toast (notificação temporária)
function showToast(message, type = 'info', duration = 5000) {
    const container = document.getElementById('toast-container');
    if (!container) {
        console.warn('showToast: #toast-container não encontrado.');
        return;
    }

    let iconChar = 'ℹ';
    if (type === 'success') iconChar = '✔';
    if (type === 'warning') iconChar = '⚠';
    if (type === 'error')   iconChar = '✖';

    const toastEl = document.createElement('div');
    toastEl.className = `toast toast--${type} toast-enter`;

    toastEl.innerHTML = `
        <div class="toast__icon">${iconChar}</div>
        <div class="toast__body">${message}</div>
        <button class="toast__close" aria-label="Fechar">&times;</button>
        <div class="toast__progress"></div>
    `;

    const progressBar = toastEl.querySelector('.toast__progress');
    if (progressBar) {
        progressBar.style.animationDuration = duration + 'ms';
    }

    const closeBtn = toastEl.querySelector('.toast__close');
    closeBtn.addEventListener('click', () => {
        fecharToast(toastEl);
    });

    container.appendChild(toastEl);

    const autoRemove = setTimeout(() => {
        fecharToast(toastEl);
    }, duration);

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
