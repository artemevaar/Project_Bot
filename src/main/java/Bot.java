
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Bot extends TelegramLongPollingBot {
    public static void main(String[] args) throws TelegramApiException, SQLException {
        DBfunc dBfunc = new DBfunc();
        dBfunc.connect("jdbc:postgresql://localhost:5432/bot", "postgres", "DGM3Afkn5v");
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(new Bot());
    }
    private static boolean isAdmin = false;
    private String SaveUsername;
    private String SavePassword;
    @Override
    public String getBotUsername() {
        return "Bot";
    }
    @Override
    public String getBotToken() {
        return "6079220171:AAEJIEUnzx0k9oo-etQK70WlvDkAD6Z4aA8";
    }

    int variant;
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = Long.parseLong(update.getMessage().getChatId().toString());
            String messageText = update.getMessage().getText();
            if (variant == 1) {
                String[] inputValues = messageText.split("/");
                if (inputValues.length == 2) {
                    String username = inputValues[0];
                    String password = inputValues[1];
                    saveLoginAndPasswordToPostgreSQL(username, password);
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(String.valueOf(chatId));
                    sendMessage.setText("Вы зарегистрированы.");
                    sendMessageToTelegram(sendMessage);
                    SendMessage sendNewMessage = sendNewMessage(update.getMessage().getChatId());
                    SavePassword = password;
                    SaveUsername = username;
                    try {
                        execute(sendNewMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } else {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(String.valueOf(chatId));
                    sendMessage.setText("Неверный ввод данных. Повторите попытку.");
                    sendMessageToTelegram(sendMessage);
                }
            }
            if (variant == 2) {
                String[] inputValues = messageText.split("/");
                if (inputValues.length == 2) {
                    String username = inputValues[0];
                    String password = inputValues[1];
                    SavePassword = password;
                    SaveUsername = username;
                    if (SaveUsername.equals("admin") && SavePassword.equals("admin123")) {
                        isAdmin = true;
                    }
                    if (checkUserExistenceInPostgreSQL(username, password)) {
                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(String.valueOf(chatId));
                        sendMessage.setText("Вы успешно вошли в систему.");
                        sendMessageToTelegram(sendMessage);
                        SendMessage sendNewMessage = sendNewMessage(update.getMessage().getChatId());
                        try {
                            execute(sendNewMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    } else {
                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(String.valueOf(chatId));
                        sendMessage.setText("Неверные имя пользователя или пароль. Повторите попытку.");
                        sendMessageToTelegram(sendMessage);
                    }
                } else {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(String.valueOf(chatId));
                    sendMessage.setText("Неверный ввод данных. Повторите попытку.");
                    sendMessageToTelegram(sendMessage);
                }
            }
            if (variant == 4) {
                String searchQuery = update.getMessage().getText();
                searchDatabaseContents(chatId, searchQuery, SaveUsername, SavePassword);
            }
            if (variant == 7) {
                String[] inputValues = messageText.split("/");
                String rowname = inputValues[0];
                String description = inputValues[1];
                String application = inputValues[2];
                String rowgroup = inputValues[3];
                addNewProduct(rowname, description, application, rowgroup, chatId);
            }
            if (variant == 8) {
                String productName = messageText;
                deleteProductByName(productName, chatId);
            }
            if (variant == 9) {
                String[] inputValues = messageText.split("/");
                if (inputValues.length == 3) {
                    String productName = inputValues[0].trim();
                    String choice = inputValues[1].trim();
                    String newValue = inputValues[2];
                    updateProduct(productName, Integer.parseInt(choice), newValue,chatId);
                } else {
                    SendMessage errorMessage = new SendMessage();
                    errorMessage.setChatId(String.valueOf(chatId));
                    errorMessage.setText("Некорректный формат ввода. Изменение отменено.");
                    sendMessageToTelegram(errorMessage);
                }
            }
            if (variant == 10 || variant == 11) {
                CallbackQuery callbackQuery = update.getCallbackQuery();
                String productName = callbackQuery.getData();
                SendMessage sendMessage = getProductInformation(chatId, productName);
                sendMessageToTelegram(sendMessage);
            }
            if (update.getMessage().getText().equals("Hello")) {
                SendMessage sendInlineKeyBoardMessage = sendInlineKeyBoardMessage(update.getMessage().getChatId());
                try {
                    sendPhoto(chatId);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                try {
                    execute(sendInlineKeyBoardMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }

        else if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String data = callbackQuery.getData();
            long chatId = callbackQuery.getMessage().getChatId();
            if (data.equals("1")) {
                variant = 1;
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(String.valueOf(chatId));
                sendMessage.setText("Введите логин и пароль для регистрации через '/':");
                sendMessageToTelegram(sendMessage);
            }
            if (data.equals("2")) {
                variant = 2;
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(String.valueOf(chatId));
                sendMessage.setText("Введите логин и пароль (через '/') для входа в систему:");
                sendMessageToTelegram(sendMessage);
            }
            if (data.equals("3")){
                SendMessage Sections = Sections(Long.parseLong(String.valueOf(callbackQuery.getMessage().getChatId())));
                try {
                    execute(Sections);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
            if (data.equals("4")){
                variant = 4;
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(String.valueOf(chatId));
                sendMessage.setText("Введите название инструмента, которое желаете найти");
                sendMessageToTelegram(sendMessage);
            }
            if (data.equals("5")){
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(String.valueOf(chatId));
                sendMessage.setText("История поиска: ");
                sendMessageToTelegram(sendMessage);
                viewSearchHistory(chatId,SaveUsername,SavePassword);
            }
            if (data.equals("6")){
                SendMessage sendInlineKeyBoardMessage = sendInlineKeyBoardMessage(Long.parseLong(String.valueOf(callbackQuery.getMessage().getChatId())));
                try {
                    execute(sendInlineKeyBoardMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
            if (data.equals("7")) {
                variant = 7;
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(String.valueOf(chatId));
                sendMessage.setText("Добавление нового товара. \nВведите через '/' \n(название/описание/способ применения/группа(1 - столярные, 2 - слесарные) ). ");
                sendMessageToTelegram(sendMessage);
            }
            if (data.equals("8")) {
                variant=8;
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(String.valueOf(chatId));
                sendMessage.setText("Удаление инструмента из каталога."+ "\n"+ "Ведите название инструмента, которое желаете удалить");
                sendMessageToTelegram(sendMessage);
            }
            if (data.equals("9")){
                variant=9;
                SendMessage sendMessage=new SendMessage();
                sendMessage.setChatId(String.valueOf(chatId));
                sendMessage.setText("* Введите название товара, который желаете изменить." +
                        "\n* Выберите, что именно хотите изменить:" +
                        "\n   1 - название\n   2 - описание\n   3 - безопасное применение\n   4 - группа\n* Введите новое изменение.\n" +
                        "* Все это сделайте через '/'\n* (Пример: Пила/1/Дрель)");
                sendMessageToTelegram(sendMessage);
            }

            if (data.equals("10")) {
                SendMessage sendMessage = getCatalogButtons(chatId, "1");
                sendMessageToTelegram(sendMessage);
            } else if (data.equals("11")) {
                SendMessage sendMessage = getCatalogButtons(chatId, "2");
                sendMessageToTelegram(sendMessage);
            } else if (data.startsWith("catalog_")) {
                String productName = data.substring(8);
                SendMessage sendMessage = getProductInformation(chatId, productName);
                sendMessageToTelegram(sendMessage);
            }
            if (data.equals("12")) {
                SendMessage sendMessage = sendNewMessage(Long.parseLong(String.valueOf(callbackQuery.getMessage().getChatId())));
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }

        }
    }
    public static SendMessage sendInlineKeyBoardMessage(long chatId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("Регистрация");
        inlineKeyboardButton1.setCallbackData("1");
        inlineKeyboardButton2.setText("Вход");
        inlineKeyboardButton2.setCallbackData("2");
        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow2 = new ArrayList<>();
        keyboardButtonsRow1.add(inlineKeyboardButton1);
        keyboardButtonsRow2.add(inlineKeyboardButton2);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);
        rowList.add(keyboardButtonsRow2);
        inlineKeyboardMarkup.setKeyboard(rowList);
        SendMessage sendMessage=new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("Привет! Я готов к работе. \nДля начала тебе необходимо выполнить вход в систему");
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        return  sendMessage;
    }
   public static SendMessage sendNewMessage(long chatId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton3 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton4 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton5 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton6 = new InlineKeyboardButton();
        inlineKeyboardButton3.setText("Каталог");
        inlineKeyboardButton3.setCallbackData("3");
        inlineKeyboardButton4.setText("Поиск");
        inlineKeyboardButton4.setCallbackData("4");
        inlineKeyboardButton5.setText("История поиска");
        inlineKeyboardButton5.setCallbackData("5");
        inlineKeyboardButton6.setText("Выйти из системы");
        inlineKeyboardButton6.setCallbackData("6");
        List<InlineKeyboardButton> keyboardButtonsRow3 = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow4 = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow5 = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow6 = new ArrayList<>();
        keyboardButtonsRow3.add(inlineKeyboardButton3);
        keyboardButtonsRow4.add(inlineKeyboardButton4);
        keyboardButtonsRow5.add(inlineKeyboardButton5);
        keyboardButtonsRow6.add(inlineKeyboardButton6);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow3);
        rowList.add(keyboardButtonsRow4);
        rowList.add(keyboardButtonsRow5);
        rowList.add(keyboardButtonsRow6);
        inlineKeyboardMarkup.setKeyboard(rowList);
       if (isAdmin) {
           InlineKeyboardButton inlineKeyboardButton7 = new InlineKeyboardButton();
           InlineKeyboardButton inlineKeyboardButton8 = new InlineKeyboardButton();
           InlineKeyboardButton inlineKeyboardButton9 = new InlineKeyboardButton();
           inlineKeyboardButton7.setText("Добавление нового инструмента");
           inlineKeyboardButton7.setCallbackData("7");
           inlineKeyboardButton8.setText("Удаление инструмента из каталога");
           inlineKeyboardButton8.setCallbackData("8");
           inlineKeyboardButton9.setText("Изменение товара");
           inlineKeyboardButton9.setCallbackData("9");
           List<InlineKeyboardButton> keyboardButtonsRow7 = new ArrayList<>();
           List<InlineKeyboardButton> keyboardButtonsRow8 = new ArrayList<>();
           List<InlineKeyboardButton> keyboardButtonsRow9 = new ArrayList<>();
           keyboardButtonsRow7.add(inlineKeyboardButton7);
           keyboardButtonsRow8.add(inlineKeyboardButton8);
           keyboardButtonsRow9.add(inlineKeyboardButton9);
           rowList.add(keyboardButtonsRow7);
           rowList.add(keyboardButtonsRow8);
           rowList.add(keyboardButtonsRow9);
       }
           SendMessage sendMessage = new SendMessage();
           sendMessage.setChatId(String.valueOf(chatId));
           sendMessage.setText("Поиск по системе:");
           sendMessage.setReplyMarkup(inlineKeyboardMarkup);
           return sendMessage;
       }



    public static SendMessage Sections(long chatId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton10 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton11 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton12 = new InlineKeyboardButton();

        inlineKeyboardButton10.setText("Столярные инструменты");
        inlineKeyboardButton10.setCallbackData("10");
        inlineKeyboardButton11.setText("Слесарные инструменты");
        inlineKeyboardButton11.setCallbackData("11");
        inlineKeyboardButton12.setText("Назад к основному меню");
        inlineKeyboardButton12.setCallbackData("12");
        List<InlineKeyboardButton> keyboardButtonsRow10 = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow11 = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow12 = new ArrayList<>();
        keyboardButtonsRow10.add(inlineKeyboardButton10);
        keyboardButtonsRow11.add(inlineKeyboardButton11);
        keyboardButtonsRow12.add(inlineKeyboardButton12);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow10);
        rowList.add(keyboardButtonsRow11);
        rowList.add(keyboardButtonsRow12);
        inlineKeyboardMarkup.setKeyboard(rowList);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("Содержимое каталога:");
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        return sendMessage;
    }

    public static SendMessage getCatalogButtons(long chatId, String rowGroup) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        try {
            Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/bot", "postgres", "DGM3Afkn5v");
            String selectSql = "SELECT rowname FROM catalog WHERE rowgroup = ?";
            PreparedStatement selectStmt = conn.prepareStatement(selectSql);
            selectStmt.setString(1, rowGroup);
            ResultSet resultSet = selectStmt.executeQuery();
            while (resultSet.next()) {
                String rowname = resultSet.getString("rowname");
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(rowname);
                button.setCallbackData("catalog_" + rowname); // Добавление callback данных, соответствующих названию кнопки
                List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
                keyboardButtonsRow.add(button);
                rowList.add(keyboardButtonsRow);
            }
            resultSet.close();
            selectStmt.close();
            conn.close();

        } catch (SQLException e) {
            System.out.println("Ошибка при получении каталога: " + e.getMessage());
        }
        inlineKeyboardMarkup.setKeyboard(rowList);

        // Добавление кнопки "Назад в основное меню"
        InlineKeyboardButton backButton = new InlineKeyboardButton();
        backButton.setText("Назад в основное меню");
        backButton.setCallbackData("12");
        List<InlineKeyboardButton> backButtonRow = new ArrayList<>();
        backButtonRow.add(backButton);
        rowList.add(backButtonRow);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("Содержимое каталога. \nНажмите на название инструмента, чтобы просмотреть детальную информацию о нем.");

        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        return sendMessage;
    }
    public static SendMessage getProductInformation(long chatId, String productName) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/bot", "postgres", "DGM3Afkn5v");
            String selectSql = "SELECT description, application FROM catalog WHERE rowname = ?";
            PreparedStatement selectStmt = conn.prepareStatement(selectSql);
            selectStmt.setString(1, productName);
            ResultSet resultSet = selectStmt.executeQuery();
            if (resultSet.next()) {
                String description = resultSet.getString("description");
                String application = resultSet.getString("application");

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                InlineKeyboardButton backButton = new InlineKeyboardButton();
                backButton.setText("Назад в основное меню");
                backButton.setCallbackData("12");
                List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
                keyboardButtonsRow.add(backButton);
                List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
                rowList.add(keyboardButtonsRow);
                inlineKeyboardMarkup.setKeyboard(rowList);

                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(String.valueOf(chatId));
                sendMessage.setText("Описание: " + "\n" + description + "\n"+  "\nБезопасное применение: " + application);
                sendMessage.setReplyMarkup(inlineKeyboardMarkup);
                resultSet.close();
                selectStmt.close();
                conn.close();
                return sendMessage;
            } else {
                // Если товара нет кнопку "Назад в основное меню"
                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                InlineKeyboardButton backButton = new InlineKeyboardButton();
                backButton.setText("Назад в основное меню");
                backButton.setCallbackData("12");
                List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
                keyboardButtonsRow.add(backButton);
                List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
                rowList.add(keyboardButtonsRow);
                inlineKeyboardMarkup.setKeyboard(rowList);

                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(String.valueOf(chatId));
                sendMessage.setText("Товар не найден.");
                sendMessage.setReplyMarkup(inlineKeyboardMarkup);
                resultSet.close();
                selectStmt.close();
                conn.close();
                return sendMessage;
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при получении информации о товаре: " + e.getMessage());
            return null;
        }
    }
    public void saveLoginAndPasswordToPostgreSQL(String login, String password) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/bot", "postgres", "DGM3Afkn5v");
            String sql = "INSERT INTO users(rowname,login) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            SendMessage sendMessage = new SendMessage();
            pstmt.setString(1, login);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
            pstmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    // проверка логина пароля
    public boolean checkUserExistenceInPostgreSQL(String login, String password) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/bot", "postgres", "DGM3Afkn5v");
            String sql = "SELECT COUNT(*) FROM users WHERE rowname=? AND login=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, login);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            rs.close();
            pstmt.close();
            conn.close();
            return count > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    // для получения айдишника
    public int readLogin(Connection conn, String login, String pass){
        try {
            String query = String.format("SELECT * FROM users WHERE rowname = '%s' AND login = '%s'", login, pass);
            Statement statement = conn.createStatement();
            ResultSet rs=statement.executeQuery(query);
            if (rs.next()) {
                return rs.getInt("rowid");
            }
        }
        catch (Exception e){
            System.out.println(e);
        }
        return 0;
    }
    // поиск
    public void searchDatabaseContents(long chatId, String searchQuery, String login, String password) {
        try {
            // Сохранение запроса в базе данных истории поиска
            Connection connHistory = DriverManager.getConnection("jdbc:postgresql://localhost:5432/bot", "postgres", "DGM3Afkn5v");
            String sqlHistory = "INSERT INTO search_history (user_id,search_query, search_timestamp) VALUES (?, ?, ?)";
            PreparedStatement pstmtHistory = connHistory.prepareStatement(sqlHistory);
            pstmtHistory.setLong(1, readLogin(connHistory, login, password));
            pstmtHistory.setString(2, searchQuery);
            pstmtHistory.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            pstmtHistory.executeUpdate();
            pstmtHistory.close();
            connHistory.close();
            // Поиск по базе данных каталога
            Connection connCatalog = DriverManager.getConnection("jdbc:postgresql://localhost:5432/bot", "postgres", "DGM3Afkn5v");
            String sqlCatalog = "SELECT * FROM catalog WHERE LOWER(rowname) LIKE LOWER(?)";
            PreparedStatement pstmtCatalog = connCatalog.prepareStatement(sqlCatalog);
            pstmtCatalog.setString(1, "%" + searchQuery + "%");
            ResultSet rs = pstmtCatalog.executeQuery();
            if (rs.next()) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(String.valueOf(chatId));
                sendMessage.setText("Результаты поиска:");
                sendMessageToTelegram(sendMessage);
                do {
                    String rowname = rs.getString("rowname");
                    String description = rs.getString("description");
                    String application = rs.getString("application");
                    String resultText = "Название инструмента: " + rowname + "\n\nОписание: " + description + "\n\nСпособ применения: " + application;

                    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                    InlineKeyboardButton backButton = new InlineKeyboardButton();
                    backButton.setText("Назад в основное меню");
                    backButton.setCallbackData("12");
                    List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
                    keyboardButtonsRow.add(backButton);
                    List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
                    rowList.add(keyboardButtonsRow);
                    inlineKeyboardMarkup.setKeyboard(rowList);
                    sendMessage.setReplyMarkup(inlineKeyboardMarkup);
                    sendMessage.setText(resultText);
                    sendMessageToTelegram(sendMessage);
                } while (rs.next());
            } else {
                // Если ничего не найдено, отправляем сообщение об этом
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(String.valueOf(chatId));
                sendMessage.setText("По вашему запросу ничего не найдено.");

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                InlineKeyboardButton backButton = new InlineKeyboardButton();
                backButton.setText("Назад в основное меню");
                backButton.setCallbackData("12");
                List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
                keyboardButtonsRow.add(backButton);
                List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
                rowList.add(keyboardButtonsRow);
                inlineKeyboardMarkup.setKeyboard(rowList);
                sendMessage.setReplyMarkup(inlineKeyboardMarkup);
                sendMessageToTelegram(sendMessage);
            }
            rs.close();
            pstmtCatalog.close();
            connCatalog.close();
        } catch (SQLException e) {
            System.out.println("Error searching database contents: " + e.getMessage());
        }
    }
    // вывод истории поиска
    public void viewSearchHistory(long chatId, String login, String password) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/bot", "postgres", "DGM3Afkn5v");
            String sql = "SELECT * FROM search_history WHERE user_id = ? ORDER BY search_timestamp DESC";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, readLogin(conn, login, password));
            ResultSet rs = pstmt.executeQuery();
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(chatId));
            if (rs.next()) {
                sendMessage.setText("История поиска:");
                do {
                    String searchQuery = rs.getString("search_query");
                    Timestamp searchTimestamp = rs.getTimestamp("search_timestamp");
                    sendMessage.setText(searchQuery + " (" + searchTimestamp + ")");
                    sendMessageToTelegram(sendMessage);
                } while (rs.next());
            } else {
                sendMessage.setText("История поиска пуста.");
                sendMessageToTelegram(sendMessage);
            }
            rs.close();
            pstmt.close();
            conn.close();
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            InlineKeyboardButton backButton = new InlineKeyboardButton();
            backButton.setText("Назад в основное меню");
            backButton.setCallbackData("12");
            List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
            keyboardButtonsRow.add(backButton);
            List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
            rowList.add(keyboardButtonsRow);
            inlineKeyboardMarkup.setKeyboard(rowList);
            sendMessage.setReplyMarkup(inlineKeyboardMarkup);
            sendMessageToTelegram(sendMessage);
        } catch (SQLException e) {
            System.out.println("Error listing search history: " + e.getMessage());
        }
    }

    // добавление нового товара
    public void addNewProduct(String rowname, String description, String application, String rowgroup, long chatId) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/bot", "postgres", "DGM3Afkn5v");
            String sql = "INSERT INTO catalog (rowname, description, application, rowgroup) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, rowname);
            pstmt.setString(2, description);
            pstmt.setString(3, application);
            pstmt.setString(4, rowgroup);
            pstmt.executeUpdate();
            pstmt.close();
            conn.close();
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(chatId));
            sendMessage.setText("Инструмент успешно добавлен в каталог.");

            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            InlineKeyboardButton backButton = new InlineKeyboardButton();
            backButton.setText("Назад в основное меню");
            backButton.setCallbackData("12");
            List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
            keyboardButtonsRow.add(backButton);
            List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
            rowList.add(keyboardButtonsRow);
            inlineKeyboardMarkup.setKeyboard(rowList);

            sendMessage.setReplyMarkup(inlineKeyboardMarkup);
            sendMessageToTelegram(sendMessage);
        } catch (SQLException e) {
            System.out.println("Ошибка при добавлении инструмента в каталог: " + e.getMessage());
        }
    }

    // изменение товара
    public void updateProduct(String productName, int choice, String newValue, long chatId) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/bot", "postgres", "DGM3Afkn5v");

            // Запрос для проверки наличия товара
            String selectSql = "SELECT * FROM catalog WHERE rowname = ?";
            PreparedStatement selectStmt = conn.prepareStatement(selectSql);
            selectStmt.setString(1, productName);
            ResultSet resultSet = selectStmt.executeQuery();

            if (resultSet.next()) {
                // Получение текущих значений полей товара
                String currentRowname = resultSet.getString("rowname");
                String currentDescription = resultSet.getString("description");
                String currentApplication = resultSet.getString("application");
                String currentRowgroup = resultSet.getString("rowgroup");

                // Обновление соответствующего  товара
                switch (choice) {
                    case 1:
                        currentRowname = newValue;
                        break;
                    case 2:
                        currentDescription = newValue;
                        break;
                    case 3:
                        currentApplication = newValue;
                        break;
                    case 4:
                        currentRowgroup = newValue;
                        break;
                    default:
                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(String.valueOf(chatId));
                        sendMessage.setText("Некорректный выбор. Изменение отменено.");
                        sendMessageToTelegram(sendMessage);
                        resultSet.close();
                        selectStmt.close();
                        conn.close();
                        return;
                }
                // Обновление записи в базе данных
                String updateSql = "UPDATE catalog SET rowname = ?, description = ?, application = ?, rowgroup = ? WHERE rowname = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setString(1, currentRowname);
                updateStmt.setString(2, currentDescription);
                updateStmt.setString(3, currentApplication);
                updateStmt.setString(4, currentRowgroup);
                updateStmt.setString(5, productName);
                updateStmt.executeUpdate();
                updateStmt.close();
                // Отправка сообщения об успешном изменении товара
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(String.valueOf(chatId));
                sendMessage.setText("Изменения сохранены.");

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                InlineKeyboardButton backButton = new InlineKeyboardButton();
                backButton.setText("Назад в основное меню");
                backButton.setCallbackData("12");
                List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
                keyboardButtonsRow.add(backButton);
                List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
                rowList.add(keyboardButtonsRow);
                inlineKeyboardMarkup.setKeyboard(rowList);

                sendMessage.setReplyMarkup(inlineKeyboardMarkup);
                sendMessageToTelegram(sendMessage);
            } else {
                // Отправка сообщения об ошибке при поиске товара
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(String.valueOf(chatId));
                sendMessage.setText("Товар не найден.");

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                InlineKeyboardButton backButton = new InlineKeyboardButton();
                backButton.setText("Назад в основное меню");
                backButton.setCallbackData("12");
                List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
                keyboardButtonsRow.add(backButton);
                List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
                rowList.add(keyboardButtonsRow);
                inlineKeyboardMarkup.setKeyboard(rowList);

                sendMessage.setReplyMarkup(inlineKeyboardMarkup);
                sendMessageToTelegram(sendMessage);
            }
            resultSet.close();
            selectStmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println("Ошибка при изменении товара: " + e.getMessage());
        }
    }

    public void deleteProductByName(String productName, long chatId) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/bot", "postgres", "DGM3Afkn5v");
            String sql = "DELETE FROM catalog WHERE rowname = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, productName);
            SendMessage sendMessage = new SendMessage();
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                sendMessage.setText("Товар успешно удален из каталога.");
                sendMessageToTelegram1111(chatId, sendMessage);
            } else {
                sendMessage.setText("Товар с указанным названием не найден в каталоге.");
                sendMessageToTelegram1111(chatId, sendMessage);
            }
            pstmt.close();
            conn.close();

            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            InlineKeyboardButton backButton = new InlineKeyboardButton();
            backButton.setText("Назад в основное меню");
            backButton.setCallbackData("12");
            List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
            keyboardButtonsRow.add(backButton);
            List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
            rowList.add(keyboardButtonsRow);
            inlineKeyboardMarkup.setKeyboard(rowList);

            sendMessage.setChatId(String.valueOf(chatId));
            sendMessage.setReplyMarkup(inlineKeyboardMarkup);
            sendMessageToTelegram1111(chatId, sendMessage);

        } catch (SQLException e) {
            System.out.println("Error deleting product from catalog: " + e.getMessage());
        }
    }

    public void sendMessageToTelegram1111(long chatId, SendMessage message) {
        message.setChatId(String.valueOf(chatId));
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageToTelegram(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public  void sendPhoto(Long chatId) throws TelegramApiException {
        SendPhoto message = new SendPhoto();
        message.setChatId(chatId);
     message.setCaption("");
        InputFile photo=new InputFile(new File("C:\\Users\\дом\\Desktop\\инструментыjpg.jpg"));
     message.setPhoto(photo);

        execute(message);
    }
}
