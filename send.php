<?php
// ===== НАСТРОЙКИ =====
$to = 't.zheleznowa@yandex.ru';                    // Куда отправлять
$subject = 'Новая заявка — DriveBit';
$from_email = 'noreply@' . $_SERVER['HTTP_HOST']; // От кого (автоматически)

// ===== ЗАЩИТА =====
header('Content-Type: application/json; charset=utf-8');

// Защита от прямого доступа (только POST)
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['success' => false, 'error' => 'Method not allowed']);
    exit;
}

// Простая защита от спама — проверка реферера
$allowed_host = $_SERVER['HTTP_HOST'];
$referer = isset($_SERVER['HTTP_REFERER']) ? parse_url($_SERVER['HTTP_REFERER'], PHP_URL_HOST) : '';
if ($referer !== $allowed_host) {
    // Мягкая проверка — логируем, но не блокируем жёстко
    // можно раскомментировать для строгой проверки:
    // echo json_encode(['success' => false, 'error' => 'Invalid referer']);
    // exit;
}

// ===== ПОЛУЧЕНИЕ И ОЧИСТКА ДАННЫХ =====
$name  = isset($_POST['name'])  ? trim(strip_tags($_POST['name']))  : '';
$phone = isset($_POST['phone']) ? trim(strip_tags($_POST['phone'])) : '';
$car   = isset($_POST['car'])   ? trim(strip_tags($_POST['car']))   : '';

// Валидация
$errors = [];

if (empty($name) || mb_strlen($name) < 2) {
    $errors[] = 'Укажите имя';
}

if (empty($phone) || mb_strlen($phone) < 6) {
    $errors[] = 'Укажите корректный номер телефона';
}

if (empty($car) || mb_strlen($car) < 2) {
    $errors[] = 'Укажите марку и модель автомобиля';
}

// Если есть ошибки — возвращаем
if (!empty($errors)) {
    echo json_encode([
        'success' => false,
        'error'   => implode(', ', $errors)
    ]);
    exit;
}

// ===== ФОРМИРОВАНИЕ ПИСЬМА =====
$date = date('d.m.Y H:i:s');
$ip   = $_SERVER['REMOTE_ADDR'];
$ua   = $_SERVER['HTTP_USER_AGENT'] ?? 'Не определён';
$page = $_SERVER['HTTP_REFERER'] ?? 'Не определена';

// HTML-версия письма
$message_html = "
<!DOCTYPE html>
<html>
<head>
    <meta charset='utf-8'>
    <style>
        body { font-family: Arial, sans-serif; background: #f4f5f7; margin: 0; padding: 20px; }
        .email-wrapper { max-width: 600px; margin: 0 auto; }
        .email-header {
            background: #09052b;
            color: #ffffff;
            padding: 28px 32px;
            border-radius: 16px 16px 0 0;
        }
        .email-header h1 {
            margin: 0;
            font-size: 22px;
            font-weight: 700;
        }
        .email-header p {
            margin: 8px 0 0;
            color: rgba(255,255,255,0.6);
            font-size: 14px;
        }
        .email-body {
            background: #ffffff;
            padding: 32px;
            border-radius: 0 0 16px 16px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.08);
        }
        .field {
            margin-bottom: 20px;
            padding-bottom: 20px;
            border-bottom: 1px solid #eef0f4;
        }
        .field:last-of-type {
            border-bottom: none;
            margin-bottom: 0;
            padding-bottom: 0;
        }
        .field-label {
            font-size: 12px;
            color: #8993a4;
            text-transform: uppercase;
            letter-spacing: 1px;
            font-weight: 600;
            margin-bottom: 6px;
        }
        .field-value {
            font-size: 18px;
            color: #09052b;
            font-weight: 600;
        }
        .field-value.phone {
            color: #2962ff;
        }
        .meta {
            margin-top: 24px;
            padding-top: 20px;
            border-top: 2px solid #eef0f4;
            font-size: 12px;
            color: #8993a4;
            line-height: 1.8;
        }
    </style>
</head>
<body>
    <div class='email-wrapper'>
        <div class='email-header'>
            <h1>🚗 Новая заявка на сайте</h1>
            <p>DriveBit — {$date}</p>
        </div>
        <div class='email-body'>
            <div class='field'>
                <div class='field-label'>Имя</div>
                <div class='field-value'>" . htmlspecialchars($name) . "</div>
            </div>
            <div class='field'>
                <div class='field-label'>Телефон</div>
                <div class='field-value phone'>" . htmlspecialchars($phone) . "</div>
            </div>
            <div class='field'>
                <div class='field-label'>Автомобиль</div>
                <div class='field-value'>" . htmlspecialchars($car) . "</div>
            </div>
            <div class='meta'>
                <strong>Дата:</strong> {$date}<br>
                <strong>IP:</strong> {$ip}<br>
                <strong>Страница:</strong> " . htmlspecialchars($page) . "<br>
                <strong>Браузер:</strong> " . htmlspecialchars($ua) . "
            </div>
        </div>
    </div>
</body>
</html>
";

// Текстовая версия (запасная)
$message_text = "
Новая заявка — DriveBit
============================
Имя: {$name}
Телефон: {$phone}
Автомобиль: {$car}
----------------------------
Дата: {$date}
IP: {$ip}
Страница: {$page}
";

// ===== ЗАГОЛОВКИ ПИСЬМА =====
$boundary = md5(uniqid(time()));

$headers  = "From: DriveBit <{$from_email}>\r\n";
$headers .= "Reply-To: {$from_email}\r\n";
$headers .= "MIME-Version: 1.0\r\n";
$headers .= "Content-Type: multipart/alternative; boundary=\"{$boundary}\"\r\n";
$headers .= "X-Mailer: PHP/" . phpversion() . "\r\n";

// Тело письма (multipart — текст + HTML)
$body  = "--{$boundary}\r\n";
$body .= "Content-Type: text/plain; charset=utf-8\r\n";
$body .= "Content-Transfer-Encoding: 8bit\r\n\r\n";
$body .= $message_text . "\r\n\r\n";
$body .= "--{$boundary}\r\n";
$body .= "Content-Type: text/html; charset=utf-8\r\n";
$body .= "Content-Transfer-Encoding: 8bit\r\n\r\n";
$body .= $message_html . "\r\n\r\n";
$body .= "--{$boundary}--\r\n";

// ===== ОТПРАВКА =====
$sent = mail($to, "=?utf-8?B?" . base64_encode($subject) . "?=", $body, $headers);

// ===== ЛОГИРОВАНИЕ (опционально) =====
$persistent_log = '/var/www/drivebit-clients/leads.log';
$log_file = (file_exists($persistent_log) && is_writable($persistent_log))
    ? $persistent_log
    : __DIR__ . '/leads.log';
$log_entry = "[{$date}] {$name} | {$phone} | {$car} | IP: {$ip} | Sent: " . ($sent ? 'YES' : 'NO') . "\n";
file_put_contents($log_file, $log_entry, FILE_APPEND | LOCK_EX);

// ===== ОТВЕТ =====
if ($sent) {
    echo json_encode(['success' => true, 'message' => 'Заявка отправлена']);
} else {
    echo json_encode(['success' => false, 'error' => 'Ошибка отправки письма']);
}
?>