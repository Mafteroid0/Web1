package ru.mafteroid;

import com.fastcgi.FCGIInterface;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Objects;

class Main {
    public static void main(String[] args) {
        // Устанавливаем порт через системное свойство (можно передать через аргументы)
        System.setProperty("FCGI_PORT", "1337");
        Main server = new Main();
        server.run();


        System.out.println("Starting FastCGI server...");


    }

    private void run(){
        FCGIInterface fcgiInterface = new FCGIInterface();
        Validator validator = new Validator();
        HitChecker checker = new HitChecker();

        while (true) {
            try {
                // Принимаем запрос через FCGIaccept()
                int acceptResult = fcgiInterface.FCGIaccept();

                if (acceptResult < 0) {
                    Thread.sleep(100);
                    continue;
                }

                // Теперь request должен быть инициализирован
                String method = FCGIInterface.request.params.getProperty("REQUEST_METHOD");
                if (method == null) {
                    sendError("Неподдерживаемый метод HTTP");
                    continue;
                }

                if (method.equals("GET")) {
                    long time = System.nanoTime();
                    String req = FCGIInterface.request.params.getProperty("QUERY_STRING");

                    if (req == null || req.isEmpty()) {
                        sendError("Запрос Пустой!");
                        continue;
                    }

                    LinkedHashMap<String, String> m;
                    try {
                        m = getValues(req);
                    } catch (Exception e) {
                        sendError("Похоже кто-то пытается поломать запрос");
                        continue;
                    }

                    boolean isShot;
                    boolean isValid;
                    try {
                        if (m.size() != 3) {
                            throw new RuntimeException("Проверьте, что в вашем запросе только x,y,r");
                        }

                        String xStr = m.get("x");
                        String yStr = m.get("y");
                        String rStr = m.get("r");

                        // Валидация через Validator
                        String validationError = validator.validate(xStr, yStr, rStr);
                        if (validationError != null) {
                            throw new RuntimeException(validationError);
                        }

                        // Конвертируем в float для checker.hit()
                        float x = Float.parseFloat(xStr);
                        float y = Float.parseFloat(yStr);
                        float r = Float.parseFloat(rStr);

                        isValid = true;
                        isShot = checker.hit(x, y, r);

                    } catch (NumberFormatException e) {
                        sendError("В данных обнаружены недопустимые символы");
                        continue;
                    } catch (RuntimeException e) {
                        sendError(e.getMessage());
                        continue;
                    } catch (Exception e) {
                        sendError("Неизвестная ошибка");
                        continue;
                    }

                    if (isValid) {
                        sendResponse(isShot, m.get("x"), m.get("y"), m.get("r"), time);
                    }
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }


    private boolean isSimpleValidNumber(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        // Проверяем базовый формат числа
        if (!value.matches("-?\\d+(\\.\\d+)?")) {
            return false;
        }

        // Отсекаем значения вроде 4.000000000000000000000000001
        // Если после точки больше 6 знаков - считаем избыточной точностью
        if (value.contains(".")) {
            String decimalPart = value.split("\\.")[1];
            if (decimalPart.length() > 6) {
                return false;
            }
        }

        return true;
    }

    private LinkedHashMap<String, String> getValues(String queryString) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        String[] pairs = queryString.split("&");

        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                String key = keyValue[0];
                String value = keyValue[1]; // Просто берем значение как есть

                params.put(key, value);
            }
        }

        return params;
    }

    private boolean isValidNumberFormat(String value) {
        // Разрешаем: целые числа, числа с плавающей точкой, отрицательные числа
        // Запрещаем: научную нотацию, ведущие нули, избыточную точность
        return value.matches("-?(?:0|[1-9]\\d*)(?:\\.\\d{1,15})?");
    }

    private void sendResponse(boolean isShoot, String x, String y, String r, long wt) {
        String content = """
                {"result":"%s","x":"%s","y":"%s","r":"%s","time":"%s","workTime":"%s"}
                """.formatted(isShoot, x, y, r, (double)(System.nanoTime() - wt) / 10000000,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));

        System.out.println("Content-Type: application/json; charset=utf-8");
        System.out.println("Status: 200 OK");
        System.out.println();
        System.out.println(content);
        System.out.flush();
    }

    private void sendError(String msg) {
        String content = """
                {"error":"%s"}
                """.formatted(msg);

        System.out.println("Content-Type: application/json; charset=utf-8");
        System.out.println("Status: 400 Bad Request");
        System.out.println();
        System.out.println(content);
        System.out.flush();
    }
}