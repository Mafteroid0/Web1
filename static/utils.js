export function showNotification(message, isError = true) {
    let notificationContainer = document.getElementById('notification-container');
    if (!notificationContainer) {
        notificationContainer = document.createElement('div');
        notificationContainer.id = 'notification-container';
        notificationContainer.style.position = 'fixed';
        notificationContainer.style.bottom = '20px';
        notificationContainer.style.left = '20px';
        notificationContainer.style.zIndex = '1000';
        notificationContainer.style.display = 'flex';
        notificationContainer.style.flexDirection = 'column';
        notificationContainer.style.gap = '10px';
        document.body.appendChild(notificationContainer);
    }

    const notification = document.createElement('div');

    // Базовая стилизация уведомления
    notification.style.display = 'flex';
    notification.style.alignItems = 'center';
    notification.style.padding = '15px 20px';
    notification.style.borderRadius = '8px';
    notification.style.color = '#2d3748';
    notification.style.backgroundColor = isError ? '#ffe6e6' : '#e6ffe6';
    notification.style.boxShadow = '0 2px 10px rgba(0,0,0,0.1)';
    notification.style.border = `1px solid ${isError ? '#ff9999' : '#99ff99'}`;
    notification.style.maxWidth = '350px';
    notification.style.minWidth = '280px';
    notification.style.fontFamily = 'system-ui, -apple-system, sans-serif';
    notification.style.fontSize = '14px';
    notification.style.lineHeight = '1.4';
    notification.style.opacity = '0';
    notification.style.transform = 'translateX(-20px)';
    notification.style.transition = 'all 0.3s ease';

    // Создаем иконку (предполагаем, что картинки лежат в папке resources)
    const icon = document.createElement('img');
    icon.style.width = '20px';
    icon.style.height = '20px';
    icon.style.marginRight = '12px';
    icon.style.flexShrink = '0';

    // Указываем локальные пути к картинкам
    if (isError) {
        icon.alt = "Ошибка";
        icon.src = "./resources/error.webp";
    } else {
        icon.alt = "Успех";
        icon.src = "./resources/success.webp";
    }

    // Создаем текст
    const text = document.createElement('div');
    text.textContent = message;
    text.style.flex = '1';

    // Кнопка закрытия
    const closeBtn = document.createElement('button');
    closeBtn.innerHTML = '&times;';
    closeBtn.style.background = 'none';
    closeBtn.style.border = 'none';
    closeBtn.style.fontSize = '18px';
    closeBtn.style.cursor = 'pointer';
    closeBtn.style.marginLeft = '10px';
    closeBtn.style.color = '#666';
    closeBtn.style.padding = '0';
    closeBtn.style.width = '20px';
    closeBtn.style.height = '20px';
    closeBtn.style.display = 'flex';
    closeBtn.style.alignItems = 'center';
    closeBtn.style.justifyContent = 'center';

    // Собираем уведомление
    notification.appendChild(icon);
    notification.appendChild(text);
    notification.appendChild(closeBtn);
    notificationContainer.appendChild(notification);

    // Плавное появление
    setTimeout(() => {
        notification.style.opacity = '1';
        notification.style.transform = 'translateX(0)';
    }, 10);

    // Функция для плавного закрытия
    const closeNotification = () => {
        notification.style.opacity = '0';
        notification.style.transform = 'translateX(-20px)';
        setTimeout(() => {
            if (notification.parentNode) {
                notification.remove();
            }
            // Удаляем контейнер если нет уведомлений
            if (notificationContainer.children.length === 0) {
                notificationContainer.remove();
            }
        }, 300);
    };

    // Закрытие по кнопке
    closeBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        closeNotification();
    });

    // Автоматическое закрытие через 5 секунд
    const autoCloseTimeout = setTimeout(closeNotification, 5000);

    // Закрытие по клику на уведомление (кроме кнопки закрытия)
    notification.addEventListener('click', (e) => {
        if (e.target !== closeBtn) {
            clearTimeout(autoCloseTimeout);
            closeNotification();
        }
    });
}
