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

        FCGIInterface fcgiInterface = new FCGIInterface();
        Validator validator = new Validator();
        HitChecker checker = new HitChecker();

        System.out.println("Starting FastCGI server...");

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

                    if (req == null || req.equals("")) {
                        sendError("Запрос Пустой!");
                        continue;
                    }

                    LinkedHashMap<String, String> m = null;
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
                        isValid = validator.validation(Float.parseFloat(m.get("x")), Float.parseFloat(m.get("y")),
                                Float.parseFloat(m.get("r")));
                        isShot = checker.hit(Float.parseFloat(m.get("x")), Float.parseFloat(m.get("y")), Float.parseFloat(m.get("r")));
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
                    } else {
                        sendError("Невалидные данные!");
                    }
                } else {
                    sendError("Данные должны отправляться GET запросом!");
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

    private LinkedHashMap<String, String> getValues(String inpString) {
        String[] args = inpString.split("&");
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        for (String s : args) {
            String[] arg = s.split("=");
            if (arg.length == 2) {
                map.put(arg[0], arg[1]);
            }
        }
        return map;
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