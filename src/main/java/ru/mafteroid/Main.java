package ru.mafteroid;

import com.fastcgi.FCGIInterface;
import ru.mafteroid.validator.Validator;
import ru.mafteroid.validator.HitChecker;
import ru.mafteroid.parser.ParameterParser;
import ru.mafteroid.parser.impl.*;
import ru.mafteroid.parser.decorator.*;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Set;

class Main {
    private ParameterParser<LinkedHashMap<String, String>> getParser;
    private ParameterParser<Set<BigDecimal>> putXParser;
    private ParameterParser<BigDecimal[]> putYParser;
    private ParameterParser<BigDecimal[]> putRParser;
    private Validator validator;
    private HitChecker checker;
    private volatile boolean running = true;

    public static void main(String[] args) {
        Main server = new Main();
        System.out.println("Starting FastCGI server...");
        server.run();
    }

    public void stop() {
        running = false;
    }

    private void initialize() {
        initializeParsers();
        this.validator = new Validator();
        this.checker = new HitChecker();
    }

    private void initializeParsers() {
        // GET парсер для /calculate с цепочкой декораторов
        ParameterParser<LinkedHashMap<String, String>> baseGetParser = new QueryStringParser();
        ParameterParser<LinkedHashMap<String, String>> injectionCheckedParser = new InjectionCheckDecorator(baseGetParser);
        this.getParser = new QueryStringValidationDecorator(injectionCheckedParser);

        // PUT парсер для /validX с декоратором валидации
        ParameterParser<Set<BigDecimal>> basePutXParser = new JsonArrayParser();
        this.putXParser = new JsonArrayValidationDecorator(basePutXParser);

        // PUT парсеры для /validY и /validR с декораторами валидации
        ParameterParser<BigDecimal[]> baseRangeParser = new JsonRangeParser();
        this.putYParser = new RangeValidationDecorator(baseRangeParser);
        this.putRParser = new RangeValidationDecorator(baseRangeParser);
    }

    private void run() {
        initialize();
        FCGIInterface fcgiInterface = new FCGIInterface();

        while (fcgiInterface.FCGIaccept() >= 0) {
            try {

                String method = FCGIInterface.request.params.getProperty("REQUEST_METHOD");
                if (method == null) {
                    sendError("Неподдерживаемый метод HTTP");
                    continue;
                }

                if ("GET".equals(method)) {
                    processGetRequest();
                } else if ("PUT".equals(method)) {
                    processPutRequest();
                } else {
                    sendError("Метод не поддерживается: " + method, 405);
                }

            } catch (Exception e) {
                handleException(e);
            }
        }
    }

    private void processGetRequest() {
        String queryString = FCGIInterface.request.params.getProperty("QUERY_STRING");

        if (queryString == null || queryString.isEmpty()) {
            sendError("Запрос Пустой!");
            return;
        }

        try {
            LinkedHashMap<String, String> params = getParser.parse(queryString);
            processCoordinates(params.get("x"), params.get("y"), params.get("r"));

        } catch (IllegalArgumentException e) {
            sendError(e.getMessage());
        } catch (Exception e) {
            sendError("Ошибка обработки запроса " + e);
        }
    }

    private void processPutRequest() {
        String path = FCGIInterface.request.params.getProperty("REQUEST_URI");
        String body = readRequestBody();

        if (body.isEmpty()) {
            sendError("Тело запроса пустое");
            return;
        }

        try {
            if ("/validX".equals(path)) {
                Set<BigDecimal> values = putXParser.parse(body);
                validator.setValidX(values);
                sendSuccess("Допустимые значения X обновлены: " + values);

            } else if ("/validY".equals(path)) {
                BigDecimal[] range = putYParser.parse(body);
                validator.setValidYRange(range[0], range[1]);
                sendSuccess(String.format("Допустимый диапазон Y обновлен: [%s, %s]", range[0], range[1]));

            } else if ("/validR".equals(path)) {
                BigDecimal[] range = putRParser.parse(body);
                validator.setValidRRange(range[0], range[1]);
                sendSuccess(String.format("Допустимый диапазон R обновлен: [%s, %s]", range[0], range[1]));

            } else {
                sendError("Неизвестный endpoint: " + path, 404);
            }

        } catch (IllegalArgumentException e) {
            sendError("Неверный запрос: " + e.getMessage());
        } catch (Exception e) {
            sendError("Ошибка обработки PUT запроса");
        }
    }

    private void processCoordinates(String x, String y, String r) {
        long time = System.nanoTime();

        String validationError = validator.validate(x, y, r);
        if (validationError != null) {
            throw new RuntimeException(validationError);
        }

        try {
            float xFloat = Float.parseFloat(x);
            float yFloat = Float.parseFloat(y);
            float rFloat = Float.parseFloat(r);

            boolean isShot = checker.hit(xFloat, yFloat, rFloat);
            sendResponse(isShot, x, y, r, time);

        } catch (NumberFormatException e) {
            throw new RuntimeException("В данных обнаружены недопустимые символы");
        } catch (Exception e) {
            throw new RuntimeException("Неизвестная ошибка при обработке координат");
        }
    }

    private String readRequestBody() {
        try {
            String contentLengthStr = FCGIInterface.request.params.getProperty("CONTENT_LENGTH");
            if (contentLengthStr == null || contentLengthStr.isEmpty()) {
                return "";
            }

            int contentLength = Integer.parseInt(contentLengthStr);
            if (contentLength <= 0) {
                return "";
            }

            InputStream inputStream = System.in;
            byte[] buffer = new byte[contentLength];
            int totalRead = 0;

            while (totalRead < contentLength) {
                int bytesRead = inputStream.read(buffer, totalRead, contentLength - totalRead);
                if (bytesRead == -1) {
                    break;
                }
                totalRead += bytesRead;
            }

            if (totalRead > 0) {
                return new String(buffer, 0, totalRead, StandardCharsets.UTF_8);
            }

        } catch (NumberFormatException e) {
            System.err.println("Неверный формат CONTENT_LENGTH");
        } catch (IOException e) {
            System.err.println("Ошибка чтения тела запроса: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Неизвестная ошибка при чтении тела запроса: " + e.getMessage());
        }

        return "";
    }

    private void handleException(Exception e) {
        e.printStackTrace();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
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

    private void sendSuccess(String message) {
        String content = """
                {"success":"%s"}
                """.formatted(message);

        System.out.println("Content-Type: application/json; charset=utf-8");
        System.out.println("Status: 200 OK");
        System.out.println();
        System.out.println(content);
        System.out.flush();
    }

    private void sendError(String msg) {
        sendError(msg, 400);
    }

    private void sendError(String msg, int status) {
        String content = """
                {"error":"%s"}
                """.formatted(msg);

        System.out.println("Content-Type: application/json; charset=utf-8");
        System.out.println("Status: " + status + " " + getStatusText(status));
        System.out.println();
        System.out.println(content);
        System.out.flush();
    }

    private String getStatusText(int status) {
        return switch (status) {
            case 400 -> "Bad Request";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            case 500 -> "Internal Server Error";
            default -> "Error";
        };
    }
}