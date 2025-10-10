package ru.mafteroid;

import com.fastcgi.FCGIInterface;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;

class Main {
    public static void main(String[] args) {
        Main server = new Main();
        System.out.println("Starting FastCGI server...");
        server.run();
    }

    private void run(){
        FCGIInterface fcgiInterface = new FCGIInterface();
        Validator validator = new Validator();
        HitChecker checker = new HitChecker();

        while (true) {
            try {
                int acceptResult = fcgiInterface.FCGIaccept();

                if (acceptResult < 0) {
                    Thread.sleep(100);
                    continue;
                }


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

                    try {
                        if (m.size() != 3) {
                            throw new RuntimeException("Проверьте, что в вашем запросе только x,y,r");
                        }

                        String xStr = m.get("x");
                        String yStr = m.get("y");
                        String rStr = m.get("r");

                        String validationError = validator.validate(xStr, yStr, rStr);
                        if (validationError != null) {
                            throw new RuntimeException(validationError);
                        }

                        float x = Float.parseFloat(xStr);
                        float y = Float.parseFloat(yStr);
                        float r = Float.parseFloat(rStr);

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

                    sendResponse(isShot, m.get("x"), m.get("y"), m.get("r"), time);
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

    private LinkedHashMap<String, String> getValues(String queryString){
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        String[] pairs = queryString.split("&");

        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                String key = keyValue[0];
                String value = keyValue[1];

                params.put(key, value);
            }
        }

        return params;
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