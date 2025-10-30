package WeatherApp;

import javax.swing.*;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.CardLayout;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import org.json.JSONObject;

public class WeatherAppFull extends JFrame {

    private static final String API_KEY = "928d43f568ff6fa31ccf4019744789bb";
    private static final Path USER_FILE = Paths.get("users.txt");

    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel = new JPanel(cardLayout);

    // Login / Register components
    private JTextField loginUserField = new JTextField(15);
    private JPasswordField loginPassField = new JPasswordField(15);
    private JTextField regUserField = new JTextField(15);
    private JPasswordField regPassField = new JPasswordField(15);

    // Weather components
    private JTextField cityField = new JTextField(15);
    private JTextArea resultArea = new JTextArea(6, 20);
    private String currentUser = null;

    private Map<String, String> users = new LinkedHashMap<>();

    public WeatherAppFull() {
        setTitle("🌦️ Weather App");
        setSize(450, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        loadUsers();
        setupLoginPanel();
        setupRegisterPanel();
        setupWeatherPanel();

        add(mainPanel);
        cardLayout.show(mainPanel, "login");
    }

    // -------------------- Panels --------------------
    private void setupLoginPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JLabel title = new JLabel("Giriş Yap");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel userLabel = new JLabel("Kullanıcı Adı:");
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginUserField.setMaximumSize(new Dimension(300,30));

        JLabel passLabel = new JLabel("Şifre:");
        passLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginPassField.setMaximumSize(new Dimension(300,30));

        JButton loginButton = new JButton("Giriş");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.addActionListener(e -> doLogin());

        JButton toRegButton = new JButton("Kayıt Ol");
        toRegButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        toRegButton.addActionListener(e -> cardLayout.show(mainPanel, "register"));

        panel.add(title);
        panel.add(Box.createVerticalStrut(15));
        panel.add(userLabel);
        panel.add(loginUserField);
        panel.add(Box.createVerticalStrut(10));
        panel.add(passLabel);
        panel.add(loginPassField);
        panel.add(Box.createVerticalStrut(15));
        panel.add(loginButton);
        panel.add(Box.createVerticalStrut(10));
        panel.add(toRegButton);

        mainPanel.add(panel, "login");
    }

    private void setupRegisterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JLabel title = new JLabel("Kayıt Ol");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel userLabel = new JLabel("Kullanıcı Adı (İsim Soyisim):");
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        regUserField.setMaximumSize(new Dimension(300,30));

        JLabel passLabel = new JLabel("Şifre (sadece rakam, 4+ hane):");
        passLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        regPassField.setMaximumSize(new Dimension(300,30));

        JButton regButton = new JButton("Kayıt Ol");
        regButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        regButton.addActionListener(e -> doRegister());

        JButton toLoginButton = new JButton("Girişe Dön");
        toLoginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        toLoginButton.addActionListener(e -> cardLayout.show(mainPanel, "login"));

        panel.add(title);
        panel.add(Box.createVerticalStrut(15));
        panel.add(userLabel);
        panel.add(regUserField);
        panel.add(Box.createVerticalStrut(10));
        panel.add(passLabel);
        panel.add(regPassField);
        panel.add(Box.createVerticalStrut(15));
        panel.add(regButton);
        panel.add(Box.createVerticalStrut(10));
        panel.add(toLoginButton);

        mainPanel.add(panel, "register");
    }

    private void setupWeatherPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20,40,20,40));

        JLabel title = new JLabel("Hava Durumu Sorgulama");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        cityField.setMaximumSize(new Dimension(300,30));
        cityField.setHorizontalAlignment(JTextField.CENTER);

        JButton fetchButton = new JButton("Sorgula");
        fetchButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        fetchButton.addActionListener(e -> fetchWeather());

        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(resultArea);

        JButton logoutButton = new JButton("Çıkış");
        logoutButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutButton.addActionListener(e -> {
            currentUser = null;
            loginUserField.setText("");
            loginPassField.setText("");
            cardLayout.show(mainPanel, "login");
        });

        panel.add(title);
        panel.add(Box.createVerticalStrut(15));
        panel.add(cityField);
        panel.add(Box.createVerticalStrut(10));
        panel.add(fetchButton);
        panel.add(Box.createVerticalStrut(10));
        panel.add(scrollPane);
        panel.add(Box.createVerticalStrut(10));
        panel.add(logoutButton);

        mainPanel.add(panel, "weather");
    }

    // -------------------- User Methods --------------------
    private void loadUsers() {
        try {
            if (!Files.exists(USER_FILE)) Files.createFile(USER_FILE);
            List<String> lines = Files.readAllLines(USER_FILE);
            for(String line: lines){
                if(line.trim().isEmpty()) continue;
                String[] parts = line.split(",",2);
                if(parts.length==2) users.put(parts[0].trim(), parts[1].trim());
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void saveUsers() {
        try(BufferedWriter writer = Files.newBufferedWriter(USER_FILE, StandardOpenOption.TRUNCATE_EXISTING)){
            for(Map.Entry<String,String> e: users.entrySet()){
                writer.write(e.getKey()+","+e.getValue());
                writer.newLine();
            }
        } catch(IOException e){ e.printStackTrace(); }
    }

    private void doRegister() {
        String username = regUserField.getText().trim();
        String password = new String(regPassField.getPassword()).trim();

        if(!isValidName(username)){
            JOptionPane.showMessageDialog(this,"Hatalı isim! İsim ve soyisim büyük harf ile başlamalı ve en az 2 harf olmalı.");
            return;
        }
        if(!password.matches("\\d{4,}")){
            JOptionPane.showMessageDialog(this,"Şifre kuralına uymuyor (sadece rakam, 4+ hane).");
            return;
        }
        if(users.containsKey(username)){
            JOptionPane.showMessageDialog(this,"Bu kullanıcı zaten kayıtlı.");
            return;
        }
        users.put(username,password);
        saveUsers();
        JOptionPane.showMessageDialog(this,"Kayıt başarılı! Giriş yapabilirsiniz.");
        regUserField.setText("");
        regPassField.setText("");
        cardLayout.show(mainPanel,"login");
    }

    private void doLogin() {
        String username = loginUserField.getText().trim();
        String password = new String(loginPassField.getPassword()).trim();

        if(!users.containsKey(username)){
            JOptionPane.showMessageDialog(this,"Böyle bir kullanıcı yok.");
            return;
        }
        if(!users.get(username).equals(password)){
            JOptionPane.showMessageDialog(this,"Şifre yanlış.");
            return;
        }
        currentUser = username;
        JOptionPane.showMessageDialog(this,"Giriş başarılı: "+currentUser);
        cityField.setText("");
        resultArea.setText("");
        cardLayout.show(mainPanel,"weather");
    }

    private boolean isValidName(String name){
        if(!name.contains(" ")) return false;
        String[] parts = name.trim().split("\\s+");
        if(parts.length !=2) return false;
        for(String p: parts){
            if(p.length()<2) return false;
            if(!Character.isUpperCase(p.charAt(0))) return false;
        }
        return true;
    }

    // -------------------- Weather API --------------------
    private void fetchWeather() {
        String city = cityField.getText().trim();
        if(city.isEmpty()){
            JOptionPane.showMessageDialog(this,"Şehir boş olamaz!");
            return;
        }
        try{
            String urlStr = "https://api.openweathermap.org/data/2.5/weather?q="+city+
                    "&appid="+API_KEY+"&units=metric&lang=tr";
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while((line=reader.readLine())!=null) sb.append(line);
            reader.close();

            JSONObject obj = new JSONObject(sb.toString());
            JSONObject main = obj.getJSONObject("main");
            JSONObject weather = obj.getJSONArray("weather").getJSONObject(0);

            double temp = main.getDouble("temp");
            int hum = main.getInt("humidity");
            String desc = weather.getString("description");
            String name = obj.getString("name");

            resultArea.setText("📍 Şehir: "+name+
                    "\n🌡️ Sıcaklık: "+temp+" °C"+
                    "\n💧 Nem: "+hum+"%"+
                    "\n☁️ Durum: "+desc);

        } catch(Exception e){
            resultArea.setText("Hava durumu alınamadı: "+e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WeatherAppFull().setVisible(true));
    }
}