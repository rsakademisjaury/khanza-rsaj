package simrskhanza;

import fungsi.koneksiDB;
import java.awt.AWTEvent;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JWindow;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import simrskhanza.BaseToast;

/**
 * Chat IT Service Desk untuk frmUtama dengan pemicu pada status bar bawah.
 *
 * Polling dan interaksi pengguna memakai daemon executor yang terpisah. EDT hanya
 * dipakai untuk menggambar dan memperbarui komponen Swing agar klik antrean tidak
 * menunggu siklus polling atau refresh daftar.
 *
 * V23: toast ditempatkan pada JWindow milik frmUtama agar selalu berada di atas
 * seluruh JInternalFrame. Listener klik-luar hanya aktif saat toast terlihat.
 *
 * V25: polling adaptif 2/4 detik dan sinkronisasi komentar incremental. Pesan baru
 * ditambahkan langsung pada percakapan aktif tanpa keluar-masuk daftar tiket.
 */
public final class ITServiceChatOverlay {

    private static final int POLL_INITIAL_DELAY_SECONDS = 1;
    private static final int POLL_INTERVAL_SECONDS = 2;
    private static final long BACKGROUND_POLL_INTERVAL_MS = 4_000L;
    private static final int CONNECT_TIMEOUT_MS = 2200;
    private static final int READ_TIMEOUT_MS = 3200;
    private static final int MAX_RESPONSE_CHARS = 600_000;
    private static final int CHAT_WIDTH = 430;
    private static final int CHAT_HEIGHT = 590;
    private static final int STATUS_BAR_BOTTOM_GAP = 42;
    private static final int STATUS_ICON_WIDTH = 34;
    private static final int STATUS_ICON_HEIGHT = 25;

    private static final Color BLUE = new Color(15, 102, 211);
    private static final Color BLUE_DARK = new Color(8, 73, 159);
    private static final Color BLUE_LIGHT = new Color(232, 243, 255);
    private static final Color TEXT = new Color(29, 43, 65);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Color BORDER = new Color(218, 227, 239);
    private static final Color FOCUS_BLUE = new Color(37, 99, 235);
    private static final Color PANEL_BG = new Color(240, 247, 250);
    private static final Color USER_BUBBLE = new Color(255, 255, 255);
    private static final Color IT_BUBBLE = new Color(220, 248, 210);
    private static final Color RED = new Color(239, 68, 68);
    private static final Color GREEN = new Color(25, 160, 91);

    private final JFrame owner;
    private final Connection connection;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean requestBusy = new AtomicBoolean(false);
    private final AtomicBoolean listLoading = new AtomicBoolean(false);
    private final AtomicBoolean historyLoading = new AtomicBoolean(false);
    private final AtomicBoolean soundPlaying = new AtomicBoolean(false);

    private ScheduledExecutorService scheduler;
    private ExecutorService interactionExecutor;
    private String khanzaUser = "";
    private String localIp = "";
    private String hostname = "";
    private String apiUrl = "";
    private String apiSecret = "";
    private volatile String apiSessionToken = "";
    private String detailBaseUrl = "";
    private volatile boolean configurationLoaded = false;
    private volatile int cycle = 0;
    private volatile int consecutiveFailures = 0;
    private volatile long nextPollAllowedAt = 0L;
    private volatile Actor actor;
    private volatile int currentTicketId = 0;
    private volatile int unreadCount = 0;
    private volatile int lastNotifiedEventId = 0;
    private volatile int lastMessageId = 0;
    private volatile long lastBackgroundPollAt = 0L;

    private JLabel statusBarTrigger;
    private MouseAdapter statusBarTriggerListener;
    private StatusBarChatIcon statusBarChatIcon;
    private Timer statusPulseTimer;
    private float statusPulse = 0f;
    private JPanel chatPanel;
    private JWindow chatWindow;
    private AWTEventListener outsideClickListener;
    private boolean outsideClickListenerInstalled = false;
    private boolean suppressOutsideClickAutoMinimize = false;
    private boolean chatOpenedOnce = false;
    private volatile String activeView = "LIST";
    private JPanel dialogRoot;
    private CardLayout bodyCards;
    private JPanel bodyPanel;
    private JLabel headerActorLabel;
    private JLabel ticketNoLabel;
    private JLabel ticketTitleLabel;
    private JLabel ticketMetaLabel;
    private JLabel connectionLabel;
    private JPanel messagesPanel;
    private JScrollPane messagesScroll;
    private JTextArea replyArea;
    private JButton sendButton;
    private JButton detailButton;
    private JButton finishButton;
    private DefaultListModel<Ticket> ticketListModel;
    private JList<Ticket> ticketList;
    private JLabel listEmptyLabel;
    private JLabel ticketListTitle;
    private JButton newTicketButton;
    private JButton newTicketFooterButton;
    private JButton emptyNewTicketButton;
    private JComboBox<String> newTypeCombo;
    private JComboBox<String> newCategoryCombo;
    private JComboBox<String> newPriorityCombo;
    private JTextField newTitleField;
    private JTextField newSubcategoryField;
    private JTextField newLocationField;
    private JTextArea newDetailArea;
    private JButton createTicketButton;
    private final Map<Integer, Ticket> ticketCache = new LinkedHashMap<Integer, Ticket>();
    private final Map<Integer, Boolean> renderedMessageIds = new LinkedHashMap<Integer, Boolean>();
    private Ticket currentTicket;
    private ComponentAdapter ownerMoveListener;

    public ITServiceChatOverlay(JFrame owner, Connection connection) {
        if (owner == null) {
            throw new IllegalArgumentException("Owner frmUtama tidak boleh null.");
        }
        this.owner = owner;
        this.connection = connection;
        resolveComputerIdentity();
    }

    /**
     * Memakai slot JLabel pada status bar frmUtama sebagai pemicu chat. Pemanggilan
     * ini hanya memasang listener dan ikon lokal; tidak menjalankan query database,
     * koneksi HTTP, maupun thread tambahan sehingga tidak menambah beban startup.
     */
    public void setStatusBarTrigger(final JLabel trigger) {
        if (trigger == null) return;
        statusBarTrigger = trigger;
        // Sebelum login cukup simpan referensi. Ikon baru dipasang setelah start()
        // sehingga tidak ada pekerjaan tambahan pada fase startup frmUtama.
        if (!running.get()) return;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ensureStatusBarTriggerReady();
            }
        });
    }

    public synchronized void start(String username) {
        stop();
        khanzaUser = safe(username);
        if (khanzaUser.length() == 0 || "Username".equalsIgnoreCase(khanzaUser)) {
            return;
        }
        running.set(true);
        configurationLoaded = false;
        apiSessionToken = "";
        cycle = 0;
        consecutiveFailures = 0;
        nextPollAllowedAt = 0L;
        unreadCount = 0;
        lastNotifiedEventId = 0;
        lastMessageId = 0;
        lastBackgroundPollAt = 0L;
        synchronized (renderedMessageIds) { renderedMessageIds.clear(); }
        ensureUiAsync();
        interactionExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "IT-Service-Chat-Interaction");
                t.setDaemon(true);
                t.setPriority(Thread.NORM_PRIORITY);
                return t;
            }
        });
        scheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "IT-Service-Chat-Poller");
                t.setDaemon(true);
                t.setPriority(Thread.MIN_PRIORITY);
                return t;
            }
        });
        scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                pollCycle();
            }
        }, POLL_INITIAL_DELAY_SECONDS, POLL_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    public synchronized void stop() {
        running.set(false);
        requestBusy.set(false);
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        if (interactionExecutor != null) {
            interactionExecutor.shutdownNow();
            interactionExecutor = null;
        }
        listLoading.set(false);
        historyLoading.set(false);
        actor = null;
        apiSessionToken = "";
        currentTicketId = 0;
        unreadCount = 0;
        lastNotifiedEventId = 0;
        lastMessageId = 0;
        lastBackgroundPollAt = 0L;
        currentTicket = null;
        ticketCache.clear();
        synchronized (renderedMessageIds) { renderedMessageIds.clear(); }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                destroyUi();
            }
        });
    }

    private void pollCycle() {
        final long now = System.currentTimeMillis();
        if (!running.get() || now < nextPollAllowedAt) return;

        // Saat toast diminimalkan polling tetap berjalan, tetapi cukup setiap 4
        // detik. Ketika toast terbuka scheduler 2 detik dipakai penuh agar pesan
        // pada percakapan aktif terasa realtime tanpa menambah thread baru.
        final boolean foreground = isDialogVisible();
        if (!foreground && now - lastBackgroundPollAt < BACKGROUND_POLL_INTERVAL_MS) return;
        if (!requestBusy.compareAndSet(false, true)) return;
        if (!foreground) lastBackgroundPollAt = now;

        try {
            if (!configurationLoaded) {
                loadConfiguration();
            }
            if (!isConfigured()) {
                updateConnectionState(false, "Konfigurasi Chat IT belum lengkap.");
                updateActorStatusAsync("Chat belum terhubung: konfigurasi API belum lengkap");
                return;
            }

            final int polledTicketId = currentTicketId;
            Map<String, String> pollParams = new LinkedHashMap<String, String>();
            pollParams.put("after_message_id", polledTicketId > 0 ? String.valueOf(lastMessageId) : "0");
            pollParams.put("after_event_id", String.valueOf(lastNotifiedEventId));
            pollParams.put("conversation_active", isDialogVisible() && "CHAT".equals(activeView) ? "1" : "0");
            ApiResponse response = callApi("poll", polledTicketId, 0, "", pollParams);
            if (!response.ok) {
                markPollFailure();
                updateConnectionState(false, response.error);
                updateActorStatusAsync("Belum terhubung: " + safe(response.error));
                return;
            }
            markPollSuccess();
            actor = response.actor;
            ensureUiAsync();
            updateActorUiAsync();
            updateConnectionState(true, "Terhubung ke Diskusi & Grup WA IT");

            if (response.revokedTicketId > 0) {
                handleRevokedTicketAsync(response.revokedTicketId, response.revokedAssignee);
            }

            Event latestNewEvent = null;
            int newestEventId = lastNotifiedEventId;
            for (Event event : response.events) {
                if (event.id > lastNotifiedEventId && (latestNewEvent == null || event.id > latestNewEvent.id)) {
                    latestNewEvent = event;
                }
                if (event.id > newestEventId) newestEventId = event.id;
            }
            if (newestEventId > lastNotifiedEventId) lastNotifiedEventId = newestEventId;

            if (!response.tickets.isEmpty()) {
                mergeTickets(response.tickets);
                updateTicketListAsync(snapshotTickets());
            }

            if (response.unreadCount >= 0) unreadCount = response.unreadCount;
            updateBadgeAsync(unreadCount);

            // Endpoint poll V25 mengirim metadata tiket aktif dan hanya komentar
            // baru setelah lastMessageId. Bubble ditambahkan langsung pada card
            // CHAT, tanpa reload history dan tanpa menghapus teks balasan user.
            if (polledTicketId > 0 && polledTicketId == currentTicketId
                    && (!response.messages.isEmpty() || !response.tickets.isEmpty())
                    && !historyLoading.get()) {
                final ApiResponse live = response;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (!running.get() || polledTicketId != currentTicketId) return;
                        applyIncrementalPoll(live, polledTicketId);
                    }
                });
            }

            cycle++;
            // Data tiket dari event sudah ikut dibawa oleh response poll. Daftar
            // lengkap hanya disinkronkan saat awal, saat tiket direbut petugas
            // lain, atau berkala ketika halaman Daftar benar-benar terlihat.
            boolean refreshList = response.revokedTicketId > 0
                    || cycle == 1 || (cycle % 15 == 0 && isTicketListVisible());
            if (refreshList && listLoading.compareAndSet(false, true)) {
                try {
                    ApiResponse list = callApi("list", 0, 0, "");
                    if (list.ok) {
                        replaceTicketCache(list.tickets);
                        updateTicketListAsync(list.tickets);
                        unreadCount = list.unreadCount >= 0 ? list.unreadCount : sumUnread(list.tickets);
                        updateBadgeAsync(unreadCount);
                    }
                } finally {
                    listLoading.set(false);
                }
            }

            if (latestNewEvent != null) {
                final Event notice = latestNewEvent;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (!running.get()) return;
                        pulseFloatingButton();
                        showIncomingNotice(notice);
                    }
                });
                playNotificationSound();
            }
        } catch (Throwable ex) {
            markPollFailure();
            updateConnectionState(false, safe(ex.getMessage()));
        } finally {
            requestBusy.set(false);
        }
    }

    private void applyIncrementalPoll(ApiResponse response, int ticketId) {
        for (Ticket ticket : response.tickets) {
            if (ticket.id == ticketId) {
                currentTicket = ticket;
                break;
            }
        }
        updateTicketHeader();
        setComposerEnabled(currentTicket != null && currentTicket.canReply
                && !isFinalStatus(currentTicket.status));
        appendMessagesIncrementally(response.messages);
        updateTicketListModel(snapshotTickets());
    }

    private int countOtherTicketEvents(List<Event> events, int currentId) {
        int count = 0;
        for (Event event : events) {
            if (event.ticketId != currentId) count++;
        }
        return count;
    }

    private void markPollSuccess() {
        consecutiveFailures = 0;
        nextPollAllowedAt = 0L;
    }

    private void markPollFailure() {
        consecutiveFailures = Math.min(8, consecutiveFailures + 1);
        long backoffSeconds = Math.min(120L, 15L * consecutiveFailures);
        nextPollAllowedAt = System.currentTimeMillis() + backoffSeconds * 1000L;
    }

    private boolean isDialogVisible() {
        return chatWindow != null && chatWindow.isVisible();
    }

    private boolean isTicketListVisible() {
        return isDialogVisible() && ticketList != null && ticketList.isShowing();
    }

    private void loadConfiguration() {
        String url = "";
        String secret = "";
        if (connection != null) {
            synchronized (connection) {
                String sql = "SELECT setting_key,setting_value FROM it_settings "
                        + "WHERE setting_key IN('khanza_chat_api_url','khanza_chat_api_secret')";
                try (PreparedStatement ps = connection.prepareStatement(sql);
                        ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String key = safe(rs.getString(1));
                        String value = safe(rs.getString(2));
                        if ("khanza_chat_api_url".equals(key)) url = value;
                        if ("khanza_chat_api_secret".equals(key)) secret = value;
                    }
                } catch (Exception ex) {
                    // Modul belum di-install atau tabel belum tersedia: tetap diam,
                    // tidak mengganggu SIMRS dan akan dicoba lagi pada siklus berikutnya.
                }
            }
        }
        if (url.length() == 0) {
            url = buildFallbackApiUrl();
        }
        apiUrl = trimTrailingSlash(url);
        apiSecret = secret;
        detailBaseUrl = deriveDetailBaseUrl(apiUrl);
        configurationLoaded = true;
    }

    private String buildFallbackApiUrl() {
        try {
            String host = safe(koneksiDB.HOSTHYBRIDWEB());
            String port = safe(koneksiDB.PORTWEB());
            if (host.length() == 0) return "";
            StringBuilder url = new StringBuilder("http://").append(host);
            if (port.length() > 0 && !"80".equals(port)) {
                url.append(':').append(port);
            }
            url.append("/sipegawai/it_service/api/khanza_chat.php");
            return url.toString();
        } catch (Throwable ex) {
            return "";
        }
    }

    private boolean isConfigured() {
        // Secret HMAC bersifat opsional. Bila kosong, client meminta sesi API
        // sementara yang terikat ke IP/hostname dari endpoint bootstrap.
        return apiUrl.length() > 0;
    }

    private void resolveComputerIdentity() {
        try {
            InetAddress local = InetAddress.getLocalHost();
            localIp = safe(local.getHostAddress());
            hostname = safe(local.getHostName());
        } catch (Exception ex) {
            localIp = "";
            hostname = "";
        }
    }

    private ApiResponse callApi(String action, int ticketId, int eventId, String message) {
        return callApi(action, ticketId, eventId, message, Collections.<String, String>emptyMap());
    }

    private ApiResponse callApi(String action, int ticketId, int eventId, String message,
            Map<String, String> extraParams) {
        return callApiInternal(action, ticketId, eventId, message, extraParams, true);
    }

    private ApiResponse callApiInternal(String action, int ticketId, int eventId, String message,
            Map<String, String> extraParams, boolean allowSessionRetry) {
        if (!running.get()) return ApiResponse.error("Service dihentikan.");

        // Instalasi lama sering tidak memiliki kunci HMAC pada database SIMRS.
        // Dalam kondisi itu gunakan token sesi singkat dari server SiPegawai.
        if (apiSecret.length() == 0 && apiSessionToken.length() == 0) {
            ApiResponse bootstrap = bootstrapApiSession();
            if (!bootstrap.ok) return bootstrap;
        }

        HttpURLConnection http = null;
        try {
            long ts = System.currentTimeMillis() / 1000L;
            String nonce = UUID.randomUUID().toString().replace("-", "");
            Map<String, String> params = new LinkedHashMap<String, String>();
            params.put("action", action);
            params.put("ts", String.valueOf(ts));
            params.put("nonce", nonce);
            params.put("khanza_user", khanzaUser);
            params.put("client_ip", localIp);
            params.put("hostname", hostname);
            params.put("ticket_id", ticketId > 0 ? String.valueOf(ticketId) : "");
            params.put("event_id", eventId > 0 ? String.valueOf(eventId) : "");
            params.put("after_message_id", "0");
            params.put("after_event_id", "0");
            params.put("conversation_active", "0");
            params.put("message", safe(message));
            params.put("request_type", "");
            params.put("title", "");
            params.put("category", "");
            params.put("subcategory", "");
            params.put("location", "");
            params.put("priority", "");
            if (extraParams != null) {
                for (Map.Entry<String, String> entry : extraParams.entrySet()) {
                    if (entry.getKey() != null && params.containsKey(entry.getKey())) {
                        params.put(entry.getKey(), safe(entry.getValue()));
                    }
                }
            }
            params.put("session_token", apiSessionToken);
            params.put("sig", apiSecret.length() > 0 ? sign(params) : "");

            byte[] payload = encodeForm(params).getBytes(StandardCharsets.UTF_8);
            http = openPostConnection(payload.length);
            try (java.io.OutputStream out = http.getOutputStream()) {
                out.write(payload);
                out.flush();
            }

            int code = http.getResponseCode();
            InputStream stream = code >= 400 ? http.getErrorStream() : http.getInputStream();
            String body = readLimited(stream);
            ApiResponse response = parseResponse(body);
            if (!response.ok && response.error.length() == 0) {
                response.error = "HTTP " + code;
            }

            if (!response.ok && allowSessionRetry && apiSecret.length() == 0
                    && isSessionAuthError(response.error)) {
                apiSessionToken = "";
                ApiResponse bootstrap = bootstrapApiSession();
                if (bootstrap.ok) {
                    return callApiInternal(action, ticketId, eventId, message, extraParams, false);
                }
                return bootstrap;
            }
            return response;
        } catch (Throwable ex) {
            return ApiResponse.error(safe(ex.getMessage()));
        } finally {
            if (http != null) http.disconnect();
        }
    }

    private HttpURLConnection openPostConnection(int payloadLength) throws Exception {
        HttpURLConnection http = (HttpURLConnection) new URL(apiUrl).openConnection();
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        http.setUseCaches(false);
        http.setConnectTimeout(CONNECT_TIMEOUT_MS);
        http.setReadTimeout(READ_TIMEOUT_MS);
        http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        http.setRequestProperty("Accept", "text/plain");
        http.setRequestProperty("User-Agent", "SIMRS-Khanza-ITServiceChat/1.4");
        http.setFixedLengthStreamingMode(payloadLength);
        return http;
    }

    private ApiResponse bootstrapApiSession() {
        HttpURLConnection http = null;
        try {
            Map<String, String> params = new LinkedHashMap<String, String>();
            params.put("action", "bootstrap");
            params.put("ts", String.valueOf(System.currentTimeMillis() / 1000L));
            params.put("nonce", UUID.randomUUID().toString().replace("-", ""));
            params.put("khanza_user", khanzaUser);
            params.put("client_ip", localIp);
            params.put("hostname", hostname);
            params.put("ticket_id", "");
            params.put("event_id", "");
            params.put("after_message_id", "0");
            params.put("after_event_id", "0");
            params.put("conversation_active", "0");
            params.put("message", "");
            params.put("request_type", "");
            params.put("title", "");
            params.put("category", "");
            params.put("subcategory", "");
            params.put("location", "");
            params.put("priority", "");
            params.put("session_token", "");
            params.put("sig", "");

            byte[] payload = encodeForm(params).getBytes(StandardCharsets.UTF_8);
            http = openPostConnection(payload.length);
            try (java.io.OutputStream out = http.getOutputStream()) {
                out.write(payload);
                out.flush();
            }
            int code = http.getResponseCode();
            InputStream stream = code >= 400 ? http.getErrorStream() : http.getInputStream();
            ApiResponse response = parseResponse(readLimited(stream));
            if (response.ok && response.sessionToken.length() > 0) {
                apiSessionToken = response.sessionToken;
                if (response.actor != null) actor = response.actor;
                return response;
            }
            if (response.error.length() == 0) {
                response.error = code >= 400 ? "Bootstrap API gagal (HTTP " + code + ")."
                        : "Server tidak mengirim token sesi API.";
            }
            return response;
        } catch (Throwable ex) {
            return ApiResponse.error(safe(ex.getMessage()));
        } finally {
            if (http != null) http.disconnect();
        }
    }

    private boolean isSessionAuthError(String error) {
        String value = safe(error).toLowerCase();
        return value.contains("signature") || value.contains("sesi api")
                || value.contains("token sesi") || value.contains("kedaluwarsa");
    }

    private String sign(Map<String, String> params) throws Exception {
        if (apiSecret == null || apiSecret.trim().length() == 0) return "";
        String data = safe(params.get("action")) + "|"
                + safe(params.get("ts")) + "|"
                + safe(params.get("nonce")) + "|"
                + safe(params.get("khanza_user")) + "|"
                + safe(params.get("client_ip")) + "|"
                + safe(params.get("hostname")) + "|"
                + safe(params.get("ticket_id")) + "|"
                + safe(params.get("request_type")) + "|"
                + safe(params.get("title")) + "|"
                + safe(params.get("category")) + "|"
                + safe(params.get("subcategory")) + "|"
                + safe(params.get("location")) + "|"
                + safe(params.get("priority")) + "|"
                + sha256(safe(params.get("message")));
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return toHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    private static String sha256(String value) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return toHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }

    private static String encodeForm(Map<String, String> params) throws Exception {
        StringBuilder out = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (out.length() > 0) out.append('&');
            out.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            out.append('=');
            out.append(URLEncoder.encode(safe(entry.getValue()), "UTF-8"));
        }
        return out.toString();
    }

    private static String readLimited(InputStream stream) throws Exception {
        if (stream == null) return "";
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            char[] buffer = new char[4096];
            int read;
            while ((read = reader.read(buffer)) >= 0) {
                if (body.length() + read > MAX_RESPONSE_CHARS) {
                    throw new IllegalStateException("Respons API terlalu besar.");
                }
                body.append(buffer, 0, read);
            }
        }
        return body.toString();
    }

    private ApiResponse parseResponse(String body) {
        ApiResponse response = new ApiResponse();
        if (body == null || body.trim().length() == 0) {
            return ApiResponse.error("Respons API kosong.");
        }
        String[] lines = body.split("\\r?\\n");
        for (String line : lines) {
            if (line.trim().length() == 0) continue;
            String[] p = line.split("\\t", -1);
            String type = p[0];
            try {
                if ("ERR".equals(type)) {
                    response.ok = false;
                    response.error = p.length > 1 ? b64(p[1]) : "Kesalahan API.";
                } else if ("OK".equals(type)) {
                    response.ok = true;
                } else if ("SESSION".equals(type) && p.length >= 2) {
                    response.sessionToken = b64(p[1]);
                } else if ("ACTOR".equals(type) && p.length >= 7) {
                    response.actor = new Actor(b64(p[1]), b64(p[2]), b64(p[3]), b64(p[4]), b64(p[5]), b64(p[6]));
                } else if ("EVENT".equals(type) && p.length >= 11) {
                    response.events.add(new Event(parseInt(p[1]), parseInt(p[2]), b64(p[3]), b64(p[4]), b64(p[5]), b64(p[6]), b64(p[7]), b64(p[8]), b64(p[9]), b64(p[10])));
                } else if ("TICKET".equals(type) && p.length >= 17) {
                    response.tickets.add(new Ticket(
                            parseInt(p[1]), b64(p[2]), b64(p[3]), b64(p[4]), b64(p[5]), b64(p[6]),
                            b64(p[7]), b64(p[8]), b64(p[9]), b64(p[10]), parseInt(p[11]),
                            b64(p[12]), b64(p[13]), parseInt(p[14]) == 1, parseInt(p[15]) == 1, b64(p[16])));
                } else if ("REVOKE".equals(type) && p.length >= 3) {
                    response.revokedTicketId = parseInt(p[1]);
                    response.revokedAssignee = b64(p[2]);
                } else if ("UNREAD".equals(type) && p.length >= 2) {
                    response.unreadCount = parseInt(p[1]);
                } else if ("CREATED".equals(type) && p.length >= 3) {
                    response.createdTicketId = parseInt(p[1]);
                    response.createdTicketNo = b64(p[2]);
                } else if ("MESSAGE".equals(type) && p.length >= 8) {
                    response.messages.add(new Message(parseInt(p[1]), b64(p[2]), b64(p[3]), b64(p[4]), b64(p[5]), b64(p[6]), parseInt(p[7]) == 1));
                }
            } catch (Throwable ignore) {
                // Abaikan satu baris rusak, tetapi baris lain tetap dapat dipakai.
            }
        }
        if (!response.ok && response.error.length() == 0) {
            response.error = "Format respons API tidak dikenali.";
        }
        return response;
    }

    private void ensureUiAsync() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (running.get()) ensureUi();
            }
        });
    }

    private void ensureUi() {
        ensureStatusBarTriggerReady();

        if (ownerMoveListener == null) {
            ownerMoveListener = new ComponentAdapter() {
                @Override public void componentResized(ComponentEvent e) { repositionUi(); }
                @Override public void componentMoved(ComponentEvent e) { repositionUi(); }
                @Override public void componentShown(ComponentEvent e) { repositionUi(); }
                @Override public void componentHidden(ComponentEvent e) { minimizeChatDialog(); }
            };
            owner.addComponentListener(ownerMoveListener);
        }
        repositionUi();
        updateActorUi();
    }

    /**
     * Menyiapkan slot ikon chat pada status bar. comments.png dibaca dari resource
     * lokal sekali saja. Bila resource belum tersedia, painter fallback tetap
     * menampilkan ikon percakapan sehingga fungsi chat tidak ikut gagal.
     */
    private void ensureStatusBarTriggerReady() {
        if (statusBarTrigger == null) return;

        if (statusBarTriggerListener == null) {
            statusBarTriggerListener = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e) && running.get()) {
                        toggleChatDialog();
                    }
                }
            };
            statusBarTrigger.addMouseListener(statusBarTriggerListener);
        }

        if (statusBarChatIcon == null) {
            Image image = null;
            try {
                URL resource = ITServiceChatOverlay.class.getResource("/picture/comments.png");
                if (resource == null) {
                    resource = ITServiceChatOverlay.class.getResource("/picture/conversation.png");
                }
                if (resource != null) image = new ImageIcon(resource).getImage();
            } catch (Throwable ignore) {
                image = null;
            }
            statusBarChatIcon = new StatusBarChatIcon(image);
        }

        statusBarTrigger.setText("");
        statusBarTrigger.setIcon(statusBarChatIcon);
        statusBarTrigger.setToolTipText("Chat Pengaduan IT");
        statusBarTrigger.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        statusBarTrigger.setHorizontalAlignment(SwingConstants.CENTER);
        statusBarTrigger.setVerticalAlignment(SwingConstants.CENTER);
        statusBarTrigger.setPreferredSize(new Dimension(STATUS_ICON_WIDTH, STATUS_ICON_HEIGHT));
        statusBarTrigger.setMinimumSize(new Dimension(STATUS_ICON_WIDTH, STATUS_ICON_HEIGHT));
        statusBarTrigger.setMaximumSize(new Dimension(STATUS_ICON_WIDTH, STATUS_ICON_HEIGHT));
        statusBarTrigger.setEnabled(running.get());
        statusBarTrigger.setVisible(running.get());
        statusBarChatIcon.setBadge(unreadCount);
        statusBarChatIcon.setPulse(statusPulse);
        statusBarTrigger.revalidate();
        statusBarTrigger.repaint();
    }

    private void buildDialog() {
        if (chatPanel != null) return;

        dialogRoot = new RoundedPanel(18, Color.WHITE);
        dialogRoot.setLayout(new BorderLayout());
        dialogRoot.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(177, 206, 239)),
                new EmptyBorder(0, 0, 0, 0)));

        dialogRoot.add(buildHeader(), BorderLayout.NORTH);
        bodyCards = new CardLayout();
        bodyPanel = new JPanel(bodyCards);
        bodyPanel.setOpaque(false);
        bodyPanel.add(buildChatBody(), "CHAT");
        bodyPanel.add(buildTicketBody(), "LIST");
        bodyPanel.add(buildNewTicketBody(), "NEW");
        dialogRoot.add(bodyPanel, BorderLayout.CENTER);

        // JWindow yang dimiliki frmUtama selalu berada di atas seluruh
        // JInternalFrame, tetapi tidak dibuat always-on-top terhadap aplikasi lain.
        // Komponen dialog tidak dibuang saat minimize sehingga tiket, scroll, teks
        // balasan, dan halaman terakhir tetap utuh ketika ikon dibuka kembali.
        chatPanel = dialogRoot;
        chatWindow = new JWindow(owner);
        chatWindow.setFocusableWindowState(true);
        chatWindow.setAlwaysOnTop(false);
        try {
            chatWindow.setBackground(new Color(0, 0, 0, 0));
        } catch (Throwable ignore) {
            // Transparansi tidak wajib; painter panel tetap dapat digunakan.
        }
        chatWindow.getContentPane().setLayout(new BorderLayout());
        chatWindow.getContentPane().add(chatPanel, BorderLayout.CENTER);
        chatWindow.setVisible(false);
        ensureStatusBarTriggerReady();
        repositionUi();
        updateActorUi();
    }

    private JPanel buildHeader() {
        JPanel header = new RoundedTopPanel(18, BLUE_DARK);
        header.setLayout(new BorderLayout(8, 4));
        header.setBorder(new EmptyBorder(12, 15, 11, 10));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Chat Pengaduan IT");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        headerActorLabel = new JLabel("Menghubungkan...");
        headerActorLabel.setForeground(new Color(215, 232, 255));
        headerActorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(headerActorLabel);
        header.add(titlePanel, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 0));
        actions.setOpaque(false);
        actions.setPreferredSize(new Dimension(72, 30));
        JButton minimize = new HeaderIconButton(false, "Minimalkan");
        minimize.addActionListener(e -> minimizeChatDialog());
        JButton close = new HeaderIconButton(true, "Tutup");
        close.addActionListener(e -> minimizeChatDialog());
        actions.add(minimize);
        actions.add(close);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private JPanel buildChatBody() {
        JPanel chat = new JPanel(new BorderLayout());
        chat.setBackground(Color.WHITE);

        JPanel ticketHead = new JPanel();
        ticketHead.setBackground(Color.WHITE);
        ticketHead.setLayout(new BoxLayout(ticketHead, BoxLayout.Y_AXIS));
        ticketHead.setBorder(new EmptyBorder(12, 15, 10, 15));
        ticketNoLabel = new JLabel("Pilih tiket pengaduan");
        ticketNoLabel.setForeground(BLUE_DARK);
        ticketNoLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        ticketTitleLabel = new JLabel("Belum ada percakapan yang dipilih");
        ticketTitleLabel.setForeground(TEXT);
        ticketTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        ticketMetaLabel = new JLabel(" ");
        ticketMetaLabel.setForeground(MUTED);
        ticketMetaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        ticketHead.add(ticketNoLabel);
        ticketHead.add(Box.createVerticalStrut(4));
        ticketHead.add(ticketTitleLabel);
        ticketHead.add(Box.createVerticalStrut(3));
        ticketHead.add(ticketMetaLabel);
        ticketHead.add(Box.createVerticalStrut(9));
        ticketHead.add(new JSeparator());
        chat.add(ticketHead, BorderLayout.NORTH);

        messagesPanel = new JPanel();
        messagesPanel.setBackground(PANEL_BG);
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setBorder(new EmptyBorder(8, 10, 8, 10));
        messagesScroll = new JScrollPane(messagesPanel);
        messagesScroll.setBorder(null);
        messagesScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        messagesScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        messagesScroll.getViewport().setBackground(PANEL_BG);
        messagesScroll.getVerticalScrollBar().setUnitIncrement(18);
        chat.add(messagesScroll, BorderLayout.CENTER);

        chat.add(buildComposer(), BorderLayout.SOUTH);
        showEmptyMessages("Pilih tiket dari tombol Daftar untuk membuka percakapan.");
        return chat;
    }

    private JPanel buildComposer() {
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(PANEL_BG);
        bottom.setBorder(new EmptyBorder(5, 10, 8, 10));

        connectionLabel = new JLabel("\u25cf Menunggu koneksi Diskusi/Klarifikasi");
        connectionLabel.setForeground(MUTED);
        connectionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        connectionLabel.setBorder(new EmptyBorder(0, 3, 5, 3));
        bottom.add(connectionLabel, BorderLayout.NORTH);

        JPanel compose = new JPanel(new BorderLayout(7, 0));
        compose.setOpaque(false);

        RoundedInputPanel inputShell = new RoundedInputPanel();
        inputShell.setLayout(new BorderLayout());
        inputShell.setBorder(new EmptyBorder(2, 5, 2, 5));

        replyArea = new PlaceholderTextArea("Tulis pesan...");
        replyArea.setRows(2);
        replyArea.setColumns(20);
        replyArea.setLineWrap(true);
        replyArea.setWrapStyleWord(true);
        replyArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        replyArea.setBorder(new EmptyBorder(7, 8, 7, 8));
        replyArea.setOpaque(false);
        replyArea.setToolTipText("Enter untuk mengirim. Shift+Enter untuk baris baru.");
        replyArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // Perilaku seperti WhatsApp: Enter mengirim, Shift+Enter membuat baris baru.
                if (e.getKeyCode() == KeyEvent.VK_ENTER && !e.isShiftDown()) {
                    e.consume();
                    sendCurrentMessage();
                }
            }
        });
        JScrollPane replyScroll = new JScrollPane(replyArea);
        replyScroll.setBorder(null);
        replyScroll.setOpaque(false);
        replyScroll.getViewport().setOpaque(false);
        replyScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        replyScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        replyScroll.setPreferredSize(new Dimension(300, 46));
        inputShell.add(replyScroll, BorderLayout.CENTER);
        compose.add(inputShell, BorderLayout.CENTER);

        sendButton = new SendIconButton();
        sendButton.setPreferredSize(new Dimension(48, 48));
        sendButton.setMinimumSize(new Dimension(48, 48));
        sendButton.setMaximumSize(new Dimension(48, 48));
        sendButton.setToolTipText("Kirim balasan");
        sendButton.addActionListener(e -> sendCurrentMessage());
        compose.add(sendButton, BorderLayout.EAST);
        bottom.add(compose, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 7));
        footer.setOpaque(false);
        newTicketFooterButton = capsuleButton("Pengaduan Baru", BLUE, Color.WHITE);
        newTicketFooterButton.addActionListener(e -> showNewTicketForm());
        newTicketFooterButton.setVisible(false);

        JButton listButton = capsuleButton("Daftar", new Color(37, 99, 235), Color.WHITE);
        listButton.setToolTipText("Tampilkan daftar pengaduan");
        listButton.addActionListener(e -> showTicketList());

        detailButton = capsuleButton("Lihat Detail", new Color(8, 145, 178), Color.WHITE);
        detailButton.setToolTipText("Buka detail tiket di SiPegawai");
        detailButton.addActionListener(e -> openCurrentDetail());

        finishButton = capsuleButton("✓ Selesai", new Color(22, 163, 74), Color.WHITE);
        finishButton.setToolTipText("Tandai masalah selesai dan tutup percakapan");
        finishButton.setVisible(false);
        finishButton.addActionListener(e -> finishCurrentTicket());

        JButton hideButton = capsuleButton("Minimalkan", new Color(100, 116, 139), Color.WHITE);
        hideButton.setToolTipText("Minimalkan panel chat");
        hideButton.addActionListener(e -> minimizeChatDialog());

        footer.add(newTicketFooterButton);
        footer.add(listButton);
        footer.add(detailButton);
        footer.add(finishButton);
        footer.add(hideButton);
        bottom.add(footer, BorderLayout.SOUTH);
        setComposerEnabled(false);
        return bottom;
    }

    private JPanel buildTicketBody() {
        JPanel listBody = new JPanel(new BorderLayout());
        listBody.setBackground(Color.WHITE);
        JPanel top = new JPanel(new BorderLayout(8, 0));
        top.setBackground(Color.WHITE);
        top.setBorder(new EmptyBorder(12, 14, 10, 14));
        ticketListTitle = new JLabel("Riwayat Pengaduan");
        ticketListTitle.setForeground(TEXT);
        ticketListTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);
        newTicketButton = modernButton("+ Pengaduan", BLUE, Color.WHITE);
        // Ditampilkan sejak awal untuk user. Penentuan role tetap divalidasi server
        // ketika pengaduan dikirim, sehingga aman meski koneksi akun belum selesai.
        newTicketButton.setVisible(true);
        newTicketButton.addActionListener(e -> showNewTicketForm());
        actions.add(newTicketButton);
        top.add(ticketListTitle, BorderLayout.WEST);
        top.add(actions, BorderLayout.EAST);
        listBody.add(top, BorderLayout.NORTH);

        ticketListModel = new DefaultListModel<Ticket>();
        ticketList = new JList<Ticket>(ticketListModel);
        ticketList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ticketList.setCellRenderer(new TicketRenderer());
        ticketList.setFixedCellHeight(82);
        ticketList.setBackground(PANEL_BG);
        ticketList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // Satu klik = satu request. Kondisi >=1 sebelumnya membuat
                // double-click mengantrekan request histori dua kali.
                if (e.getClickCount() == 1 && SwingUtilities.isLeftMouseButton(e)) {
                    Ticket selected = ticketList.getSelectedValue();
                    if (selected != null) loadTicket(selected.id, true);
                }
            }
        });
        JScrollPane scroll = new JScrollPane(ticketList);
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        listEmptyLabel = new JLabel("Belum ada tiket yang dapat ditampilkan.", SwingConstants.CENTER);
        listEmptyLabel.setForeground(MUTED);
        listEmptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        listEmptyLabel.setBorder(new EmptyBorder(8, 8, 10, 8));

        JPanel emptyState = new JPanel();
        emptyState.setOpaque(false);
        emptyState.setLayout(new BoxLayout(emptyState, BoxLayout.Y_AXIS));
        listEmptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        emptyNewTicketButton = modernButton("Buat Pengaduan Sekarang", BLUE, Color.WHITE);
        emptyNewTicketButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        emptyNewTicketButton.addActionListener(e -> showNewTicketForm());
        emptyState.add(Box.createVerticalStrut(8));
        emptyState.add(listEmptyLabel);
        emptyState.add(Box.createVerticalStrut(4));
        emptyState.add(emptyNewTicketButton);
        emptyState.add(Box.createVerticalStrut(10));

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(PANEL_BG);
        center.add(scroll, BorderLayout.CENTER);
        center.add(emptyState, BorderLayout.SOUTH);
        listBody.add(center, BorderLayout.CENTER);

        JLabel hint = new JLabel("<html><div style='text-align:center'>Gangguan dan layanan IT melalui tombol chat. Pengembangan SIMRS melalui menu <b>IT Support</b>.</div></html>", SwingConstants.CENTER);
        hint.setForeground(MUTED);
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        hint.setBorder(new EmptyBorder(8, 15, 10, 15));
        listBody.add(hint, BorderLayout.SOUTH);
        return listBody;
    }

    private JPanel buildNewTicketBody() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.setBorder(new EmptyBorder(9, 14, 8, 14));
        JLabel title = new JLabel("Buat Pengaduan Baru");
        title.setForeground(TEXT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        JButton back = modernButton("Kembali", Color.WHITE, BLUE_DARK);
        back.setBorder(BorderFactory.createLineBorder(BORDER));
        back.addActionListener(e -> showBodyCard("LIST"));
        top.add(title, BorderLayout.WEST);
        top.add(back, BorderLayout.EAST);
        root.add(top, BorderLayout.NORTH);

        // Form tetap tanpa JScrollPane. Field yang saling berkaitan disusun
        // dua kolom agar lebih ringkas, sedangkan fungsi dan nilai field tidak
        // berubah.
        JPanel form = new JPanel();
        form.setBackground(PANEL_BG);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                new EmptyBorder(10, 14, 12, 14)));

        newTypeCombo = new JComboBox<String>(new String[]{"Gangguan", "Permintaan Layanan IT"});
        styleComboBox(newTypeCombo);

        newCategoryCombo = new JComboBox<String>();
        styleComboBox(newCategoryCombo);

        newTypeCombo.addActionListener(e -> updateNewCategoryOptions());
        form.add(createTwoColumnFormRow(
                "Jenis Pengaduan *", newTypeCombo,
                "Kategori *", newCategoryCombo));

        newTitleField = new PlaceholderTextField("ketik judul di sini");
        styleTextField(newTitleField);
        form.add(createFormField("Judul Singkat *", newTitleField));

        newSubcategoryField = new PlaceholderTextField("ketik perangkat atau menu terkait");
        styleTextField(newSubcategoryField);
        form.add(createFormField("Perangkat / Menu Terkait", newSubcategoryField));

        newLocationField = new PlaceholderTextField("ketik lokasi atau ruangan");
        styleTextField(newLocationField);

        newPriorityCombo = new JComboBox<String>(new String[]{"Rendah", "Sedang", "Tinggi", "Kritis"});
        newPriorityCombo.setSelectedIndex(1);
        styleComboBox(newPriorityCombo);

        form.add(createTwoColumnFormRow(
                "Lokasi / Ruangan", newLocationField,
                "Urgensi Menurut Pemohon *", newPriorityCombo));

        newDetailArea = new PlaceholderTextArea("jelaskan keluhan atau kebutuhan di sini");
        newDetailArea.setRows(3);
        newDetailArea.setColumns(24);
        newDetailArea.setLineWrap(true);
        newDetailArea.setWrapStyleWord(true);
        newDetailArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        newDetailArea.setBorder(new EmptyBorder(6, 8, 6, 8));
        newDetailArea.setBackground(Color.WHITE);
        newDetailArea.setOpaque(false);
        newDetailArea.setToolTipText("Enter untuk mengirim pengaduan. Shift+Enter untuk baris baru.");
        newDetailArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // Sama dengan tombol Kirim Pengaduan; Shift+Enter tetap untuk newline.
                if (e.getKeyCode() == KeyEvent.VK_ENTER && !e.isShiftDown()) {
                    e.consume();
                    if (createTicketButton == null || createTicketButton.isEnabled()) {
                        sendNewTicket();
                    }
                }
            }
        });
        JScrollPane detailScroll = new JScrollPane(newDetailArea);
        detailScroll.setBorder(null);
        detailScroll.setOpaque(false);
        detailScroll.getViewport().setOpaque(false);
        detailScroll.getViewport().setBorder(null);
        detailScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        detailScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        detailScroll.setPreferredSize(new Dimension(340, 76));
        detailScroll.setMinimumSize(new Dimension(120, 76));
        detailScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));
        form.add(createFormField("Jelaskan Keluhan / Kebutuhan *", detailScroll));

        JLabel note = new JLabel("<html><div style='width:340px'>Langsung masuk ke antrean Tim IT dan Grup Pengaduan IT. Pengembangan SIMRS melalui menu <b>IT Support</b>.</div></html>");
        note.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        note.setForeground(MUTED);
        note.setBorder(new EmptyBorder(1, 2, 5, 2));
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(note);

        createTicketButton = modernButton("Kirim Pengaduan", BLUE, Color.WHITE);
        createTicketButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        createTicketButton.setPreferredSize(new Dimension(340, 36));
        createTicketButton.setMinimumSize(new Dimension(120, 36));
        createTicketButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        createTicketButton.addActionListener(e -> sendNewTicket());
        form.add(createTicketButton);

        root.add(form, BorderLayout.CENTER);
        updateNewCategoryOptions();
        return root;
    }

    /**
     * Membuat dua field dalam satu baris tanpa mengubah referensi komponen
     * aslinya. Grid hanya mengatur layout visual; proses baca/kirim data tetap
     * menggunakan field yang sama.
     */
    private JPanel createTwoColumnFormRow(
            String leftLabel, javax.swing.JComponent leftComponent,
            String rightLabel, javax.swing.JComponent rightComponent) {
        JPanel row = new JPanel(new java.awt.GridLayout(1, 2, 8, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        row.add(createFormField(leftLabel, leftComponent));
        row.add(createFormField(rightLabel, rightComponent));
        return row;
    }

    private JPanel createFormField(String labelText, javax.swing.JComponent component) {
        final boolean isScroll = component instanceof JScrollPane;
        final int controlHeight = isScroll ? 76 : 32;

        JPanel field = new JPanel();
        field.setOpaque(false);
        field.setLayout(new BoxLayout(field, BoxLayout.Y_AXIS));
        // Ruang bawah dibuat lebih lega agar label dan field berikutnya tidak
        // tampak berhimpitan, tanpa mengubah urutan maupun fungsi input.
        field.setBorder(new EmptyBorder(0, 0, 8, 0));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, isScroll ? 106 : 62));

        JLabel label = new JLabel(labelText);
        label.setForeground(TEXT);
        label.setFont(new Font("Segoe UI", Font.BOLD, 10));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        component.setPreferredSize(new Dimension(160, controlHeight));
        component.setMinimumSize(new Dimension(80, controlHeight));
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, controlHeight));

        RoundedFormControl control = new RoundedFormControl(component, 14);
        control.setPreferredSize(new Dimension(160, controlHeight));
        control.setMinimumSize(new Dimension(80, controlHeight));
        control.setMaximumSize(new Dimension(Integer.MAX_VALUE, controlHeight));
        control.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.add(label);
        field.add(Box.createVerticalStrut(5));
        field.add(control);
        return field;
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setBorder(new EmptyBorder(0, 8, 0, 8));
        field.setBackground(Color.WHITE);
        field.setOpaque(false);
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        combo.setBorder(null);
        combo.setBackground(Color.WHITE);
        combo.setOpaque(false);
        combo.setFocusable(true);
        // Painter bawaan Windows/Nimbus menggambar kotak dan tombol panah
        // sendiri sehingga sudut rounded wrapper tampak terpotong. UI ringan
        // ini membiarkan RoundedFormControl menggambar seluruh badan combo dan
        // hanya menggambar chevron pada sisi kanan.
        combo.setUI(new RoundedComboBoxUI());
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                label.setBorder(new EmptyBorder(0, 9, 0, 5));
                label.setForeground(TEXT);
                if (index < 0) {
                    // Nilai terpilih digambar transparan di atas wrapper putih.
                    // Jangan memakai warna selection Look & Feel karena dapat
                    // membuat teks tampak kosong saat combo masih fokus.
                    label.setOpaque(false);
                } else {
                    label.setOpaque(true);
                    label.setBackground(isSelected ? BLUE_LIGHT : Color.WHITE);
                }
                return label;
            }
        });
        combo.addActionListener(e -> {
            combo.repaint();
            if (combo.getParent() != null) combo.getParent().repaint();
        });
    }

    private void updateNewCategoryOptions() {
        if (newCategoryCombo == null || newTypeCombo == null) return;
        Object selected = newCategoryCombo.getSelectedItem();
        newCategoryCombo.removeAllItems();
        String[] values;
        if (newTypeCombo.getSelectedIndex() == 0) {
            values = new String[]{"SIMRS / Aplikasi", "Komputer / Laptop", "Printer / Scanner", "Jaringan / Internet", "Akun / Login", "Perangkat Lain"};
        } else {
            values = new String[]{"Instalasi Software", "Konfigurasi Perangkat", "Pembuatan / Perubahan Akses", "Pemasangan Jaringan", "Pemindahan Perangkat", "Permintaan Perangkat", "Lainnya"};
        }
        for (String value : values) newCategoryCombo.addItem(value);
        if (selected != null) newCategoryCombo.setSelectedItem(selected);
    }

    private JButton modernButton(String text, Color background, Color foreground) {
        // Jangan mengandalkan painter JButton dari Look & Feel Khanza. Pada
        // beberapa tema Windows/Nimbus, background custom diabaikan sehingga
        // tombol menjadi putih sedangkan teksnya tetap putih. SolidButton
        // menggambar sendiri background dan teks agar hasilnya konsisten.
        JButton button = new SolidButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(8, 12, 8, 12));
        return button;
    }

    /**
     * Tombol kapsul khusus aksi chat. Painter digambar sendiri agar warna dan
     * bentuknya konsisten pada Nimbus, Windows, maupun Look & Feel Khanza.
     */
    private JButton capsuleButton(String text, Color background, Color foreground) {
        JButton button = new CapsuleButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 10));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(6, 11, 6, 11));
        button.setPreferredSize(new Dimension(
                Math.max(58, button.getFontMetrics(button.getFont()).stringWidth(text) + 24), 29));
        return button;
    }

    private void toggleChatDialog() {
        ensureUi();
        buildDialog();
        if (isDialogVisible()) {
            minimizeChatDialog();
            return;
        }

        // Saat dibuka kembali setelah minimize, jangan mengubah card, tiket, posisi
        // scroll, atau teks yang belum dikirim. Ini juga menghindari request list /
        // history tambahan hanya karena pengguna berpindah ke form lain.
        if (chatOpenedOnce) {
            showBodyCard(activeView);
            showChatDialog();
            return;
        }

        chatOpenedOnce = true;
        boolean knownIT = actor != null && "IT".equalsIgnoreCase(actor.role);
        showBodyCard(knownIT ? "LIST" : "CHAT");
        showChatDialog();

        // Pemohon langsung dibawa ke percakapan tiket terbarunya. Tim IT tetap
        // dibawa ke antrean agar dapat memilih tiket yang akan ditangani.
        List<Ticket> cached = snapshotTickets();
        if (knownIT) {
            updateTicketListModel(cached);
            refreshTicketList();
        } else {
            int cachedId = newestUnfinishedTicketId(cached);
            if (cachedId <= 0 && !cached.isEmpty()) cachedId = cached.get(0).id;
            if (cachedId > 0) {
                loadTicket(cachedId, true);
                refreshTicketList();
            } else {
                refreshListAndOpenDefault();
            }
        }
    }

    private void showChatDialog() {
        ensureUi();
        buildDialog();
        repositionUi();
        if (chatWindow == null) return;
        chatWindow.setVisible(true);
        chatWindow.toFront();
        installOutsideClickListener();
        chatPanel.revalidate();
        chatPanel.repaint();
        // Tidak menjalankan poll tambahan di sini agar pembukaan ulang instan.
    }

    private void minimizeChatDialog() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override public void run() { minimizeChatDialog(); }
            });
            return;
        }
        uninstallOutsideClickListener();
        if (chatWindow != null) chatWindow.setVisible(false);
    }

    private void showBodyCard(String name) {
        String normalized = safe(name).toUpperCase();
        if (!"CHAT".equals(normalized) && !"LIST".equals(normalized) && !"NEW".equals(normalized)) {
            normalized = "LIST";
        }
        activeView = normalized;
        if (bodyCards != null && bodyPanel != null) bodyCards.show(bodyPanel, normalized);
    }

    private void installOutsideClickListener() {
        if (outsideClickListenerInstalled) return;
        if (outsideClickListener == null) {
            outsideClickListener = new AWTEventListener() {
                @Override
                public void eventDispatched(AWTEvent event) {
                    if (!(event instanceof MouseEvent) || !isDialogVisible()
                            || suppressOutsideClickAutoMinimize) return;
                    MouseEvent mouse = (MouseEvent) event;
                    if (mouse.getID() != MouseEvent.MOUSE_PRESSED) return;
                    Object source = mouse.getSource();
                    if (!(source instanceof Component)) return;
                    Component component = (Component) source;

                    // Klik pada toast, popup combobox milik toast, dan ikon status
                    // bukan klik-luar. Klik ikon tetap ditangani oleh toggle biasa.
                    if (chatPanel != null && SwingUtilities.isDescendingFrom(component, chatPanel)) return;
                    if (statusBarTrigger != null
                            && (component == statusBarTrigger
                            || SwingUtilities.isDescendingFrom(component, statusBarTrigger))) return;
                    Window sourceWindow = SwingUtilities.getWindowAncestor(component);
                    if (sourceWindow == chatWindow || isOwnedBy(sourceWindow, chatWindow)) return;

                    // Jangan consume event. Form yang diklik tetap menerima kliknya,
                    // sedangkan toast diminimalkan sesudah event saat ini selesai.
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override public void run() {
                            if (isDialogVisible() && !suppressOutsideClickAutoMinimize) {
                                minimizeChatDialog();
                            }
                        }
                    });
                }
            };
        }
        try {
            Toolkit.getDefaultToolkit().addAWTEventListener(
                    outsideClickListener, AWTEvent.MOUSE_EVENT_MASK);
            outsideClickListenerInstalled = true;
        } catch (Throwable ignore) {
            outsideClickListenerInstalled = false;
        }
    }

    private void uninstallOutsideClickListener() {
        if (!outsideClickListenerInstalled || outsideClickListener == null) return;
        try {
            Toolkit.getDefaultToolkit().removeAWTEventListener(outsideClickListener);
        } catch (Throwable ignore) {
        } finally {
            outsideClickListenerInstalled = false;
        }
    }

    private boolean isOwnedBy(Window candidate, Window expectedOwner) {
        Window cursor = candidate;
        while (cursor != null) {
            if (cursor == expectedOwner) return true;
            cursor = cursor.getOwner();
        }
        return false;
    }

    private void triggerImmediatePoll() {
        nextPollAllowedAt = 0L;
        ScheduledExecutorService localScheduler = scheduler;
        if (localScheduler != null && !localScheduler.isShutdown()) {
            try {
                localScheduler.execute(new Runnable() {
                    @Override public void run() { pollCycle(); }
                });
            } catch (Throwable ignore) {}
        }
    }

    private int newestUnfinishedTicketId(List<Ticket> tickets) {
        if (tickets == null) return 0;
        for (Ticket ticket : tickets) {
            if (ticket != null && !isFinalStatus(ticket.status)) {
                return ticket.id;
            }
        }
        return 0;
    }

    private void showTicketList() {
        buildDialog();
        showBodyCard("LIST");
        refreshTicketList();
    }

    private void showNewTicketForm() {
        buildDialog();
        if (actor != null && "IT".equalsIgnoreCase(actor.role)) {
            showErrorToast("Pengaduan baru dibuat dari akun pengguna. Komputer Tim IT menampilkan antrean penanganan.");
            return;
        }
        clearNewTicketForm();
        showBodyCard("NEW");
        if (newTitleField != null) newTitleField.requestFocusInWindow();
    }

    private void refreshListAndOpenDefault() {
        if (!listLoading.compareAndSet(false, true)) return;
        executeBackground(new Runnable() {
            @Override
            public void run() {
                try {
                    ApiResponse list = callApi("list", 0, 0, "");
                    if (!list.ok) {
                        updateConnectionState(false, list.error);
                        return;
                    }
                    if (list.actor != null) {
                        actor = list.actor;
                        updateActorUiAsync();
                    }
                    replaceTicketCache(list.tickets);
                    updateTicketListAsync(list.tickets);

                    boolean isIT = actor != null && "IT".equalsIgnoreCase(actor.role);
                    if (isIT) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override public void run() {
                                if (bodyCards != null && bodyPanel != null) showBodyCard("LIST");
                            }
                        });
                        return;
                    }

                    // Buka langsung percakapan terbaru yang belum selesai. Daftar
                    // pengaduan hanya ditampilkan ketika user menekan tombol Daftar.
                    int openId = newestUnfinishedTicketId(list.tickets);
                    if (openId <= 0 && currentTicketId > 0 && currentTicket != null
                            && !isFinalStatus(currentTicket.status)) {
                        openId = currentTicketId;
                    }
                    if (openId <= 0 && !list.tickets.isEmpty()) {
                        openId = list.tickets.get(0).id;
                    }
                    if (openId > 0) {
                        loadTicketInCurrentThread(openId, true);
                    } else {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override public void run() {
                                showBodyCard("CHAT");
                                currentTicket = null;
                                currentTicketId = 0;
                                updateTicketHeader();
                                showEmptyMessages("Belum ada pengaduan. Gunakan tombol Pengaduan Baru untuk mulai menghubungi Tim IT.");
                                setComposerEnabled(false);
                            }
                        });
                    }
                } finally {
                    listLoading.set(false);
                }
            }
        });
    }

    private void refreshTicketList() {
        if (!listLoading.compareAndSet(false, true)) return;
        executeBackground(new Runnable() {
            @Override
            public void run() {
                try {
                    ApiResponse list = callApi("list", 0, 0, "");
                    if (list.ok) {
                        replaceTicketCache(list.tickets);
                        updateTicketListAsync(list.tickets);
                        unreadCount = list.unreadCount >= 0 ? list.unreadCount : sumUnread(list.tickets);
                        updateBadgeAsync(unreadCount);
                    } else {
                        updateConnectionState(false, list.error);
                    }
                } finally {
                    listLoading.set(false);
                }
            }
        });
    }

    private void loadTicket(final int ticketId, final boolean switchToChat) {
        if (ticketId <= 0 || !historyLoading.compareAndSet(false, true)) return;
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                if (switchToChat && bodyCards != null) showBodyCard("CHAT");
                showEmptyMessages("Memuat percakapan...");
            }
        });
        executeBackground(new Runnable() {
            @Override
            public void run() {
                try {
                    loadTicketInCurrentThread(ticketId, switchToChat);
                } finally {
                    historyLoading.set(false);
                }
            }
        });
    }

    private void loadTicketInCurrentThread(int ticketId, boolean switchToChat) {
        // History sudah mengembalikan data tiket terbaru sekaligus menandai event
        // tiket tersebut dibaca. Request list kedua yang sebelumnya dilakukan di
        // sini membuat setiap klik menunggu dua koneksi HTTP secara berurutan.
        ApiResponse history = callApi("history", ticketId, 0, "");
        if (!history.ok) {
            updateConnectionState(false, history.error);
            return;
        }
        int previousUnread = reconcileHistoryCache(history);
        unreadCount = Math.max(0, unreadCount - previousUnread);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (!running.get()) return;
                applyHistory(history);
                updateBadge(unreadCount);
                if (switchToChat && bodyCards != null) showBodyCard("CHAT");
            }
        });
    }

    private synchronized int reconcileHistoryCache(ApiResponse history) {
        int previousUnread = 0;
        if (history != null && !history.tickets.isEmpty()) {
            Ticket fresh = history.tickets.get(0);
            Ticket old = ticketCache.get(fresh.id);
            if (old != null) previousUnread = Math.max(0, old.unread);
            boolean itActor = actor != null && "IT".equalsIgnoreCase(actor.role);
            if (itActor && isFinalStatus(fresh.status)) {
                ticketCache.remove(fresh.id);
            } else {
                ticketCache.put(fresh.id, fresh);
            }
        }
        return previousUnread;
    }

    private void applyHistory(ApiResponse history) {
        if (!history.tickets.isEmpty()) {
            currentTicket = history.tickets.get(0);
            currentTicketId = currentTicket.id;
            synchronized (this) {
                boolean itActor = actor != null && "IT".equalsIgnoreCase(actor.role);
                if (itActor && isFinalStatus(currentTicket.status)) {
                    ticketCache.remove(currentTicket.id);
                } else {
                    ticketCache.put(currentTicket.id, currentTicket);
                }
            }
        } else {
            synchronized (this) {
                currentTicket = ticketCache.get(currentTicketId);
            }
        }
        updateTicketHeader();
        renderMessages(history.messages);
        setComposerEnabled(currentTicket != null && currentTicket.canReply && !isFinalStatus(currentTicket.status));
        updateTicketListModel(snapshotTickets());
    }

    private void updateTicketHeader() {
        if (ticketNoLabel == null) return;
        if (currentTicket == null) {
            ticketNoLabel.setText("Pilih tiket pengaduan");
            ticketTitleLabel.setText("Belum ada percakapan yang dipilih");
            ticketMetaLabel.setText(" ");
            return;
        }
        ticketNoLabel.setText(currentTicket.noTicket + "   " + currentTicket.type + "   " + currentTicket.priority);
        ticketTitleLabel.setText(ellipsize(currentTicket.title, 54));
        String assignment;
        if (isFinalStatus(currentTicket.status)) {
            assignment = "Pengaduan selesai • percakapan ditutup";
        } else if (actor != null && "IT".equalsIgnoreCase(actor.role)) {
            if (currentTicket.assignedNik.length() == 0) {
                assignment = "Belum ditangani \u2022 balasan pertama otomatis mengambil tiket";
            } else if (currentTicket.mineAssignment) {
                assignment = "Ditangani oleh Anda: " + currentTicket.assignedName;
            } else {
                assignment = "Ditangani oleh " + currentTicket.assignedName + " \u2022 mode hanya baca";
            }
        } else {
            assignment = currentTicket.assignedName.length() == 0
                    ? "Menunggu Tim IT"
                    : "Ditangani oleh " + currentTicket.assignedName;
        }
        ticketMetaLabel.setText("<html>" + html(currentTicket.requester + " / " + currentTicket.unit
                + "  \u2022  " + currentTicket.time) + "<br><b>" + html(assignment) + "</b></html>");
    }

    private void renderMessages(List<Message> messages) {
        if (messagesPanel == null) return;
        resetRenderedMessageTracking(messages);
        messagesPanel.removeAll();
        if (messages.isEmpty()) {
            showEmptyMessages("Belum ada pesan dalam Diskusi/Klarifikasi.");
        } else {
            for (int i = 0; i < messages.size(); i++) {
                messagesPanel.add(createMessageRow(messages.get(i)));
                if (i < messages.size() - 1) {
                    messagesPanel.add(Box.createVerticalStrut(4));
                }
            }
        }
        messagesPanel.revalidate();
        messagesPanel.repaint();
        Timer timer = new Timer(90, e -> messagesScroll.getVerticalScrollBar().setValue(
                messagesScroll.getVerticalScrollBar().getMaximum()));
        timer.setRepeats(false);
        timer.start();
    }

    private void resetRenderedMessageTracking(List<Message> messages) {
        int newest = 0;
        synchronized (renderedMessageIds) {
            renderedMessageIds.clear();
            if (messages != null) {
                for (Message message : messages) {
                    if (message.id > 0) {
                        renderedMessageIds.put(message.id, Boolean.TRUE);
                        if (message.id > newest) newest = message.id;
                    }
                }
            }
        }
        lastMessageId = newest;
    }

    private boolean registerRenderedMessage(Message message) {
        if (message == null || message.id <= 0) return false;
        synchronized (renderedMessageIds) {
            if (renderedMessageIds.containsKey(message.id)) return false;
            renderedMessageIds.put(message.id, Boolean.TRUE);
            if (message.id > lastMessageId) lastMessageId = message.id;
            return true;
        }
    }

    private void appendMessagesIncrementally(List<Message> messages) {
        if (messagesPanel == null || messages == null || messages.isEmpty()) return;
        boolean nearBottom = true;
        if (messagesScroll != null) {
            javax.swing.JScrollBar bar = messagesScroll.getVerticalScrollBar();
            nearBottom = bar.getMaximum() - (bar.getValue() + bar.getVisibleAmount()) <= 48;
        }

        int added = 0;
        for (Message message : messages) {
            if (!registerRenderedMessage(message)) continue;
            if (messagesPanel.getComponentCount() > 0) {
                messagesPanel.add(Box.createVerticalStrut(4));
            }
            messagesPanel.add(createMessageRow(message));
            added++;
        }
        if (added == 0) return;

        messagesPanel.revalidate();
        messagesPanel.repaint();
        if (nearBottom && messagesScroll != null) {
            Timer timer = new Timer(70, e -> messagesScroll.getVerticalScrollBar().setValue(
                    messagesScroll.getVerticalScrollBar().getMaximum()));
            timer.setRepeats(false);
            timer.start();
        }
    }

    private Component createMessageRow(Message message) {
        final boolean systemMessage = "SYSTEM".equalsIgnoreCase(message.role)
                || "SYSTEM".equalsIgnoreCase(message.source);
        final boolean itMessage = "IT".equalsIgnoreCase(message.role);

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (systemMessage) {
            JLabel system = new JLabel("<html><div style='text-align:center;width:300px'>"
                    + htmlMultiline(message.text)
                    + "<br><span style='font-size:8px;color:#64748b'>"
                    + html(message.time) + "</span></div></html>", SwingConstants.CENTER);
            system.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            system.setForeground(MUTED);
            system.setBorder(new EmptyBorder(3, 10, 3, 10));
            JPanel holder = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            holder.setOpaque(false);
            holder.add(system);
            int systemHeight = Math.max(28, system.getPreferredSize().height + 4);
            row.setPreferredSize(new Dimension(CHAT_WIDTH - 30, systemHeight));
            row.setMinimumSize(new Dimension(120, systemHeight));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, systemHeight));
            row.add(holder, BorderLayout.CENTER);
            return row;
        }

        final int bubbleWidth = preferredBubbleWidth(message);
        final int contentWidth = Math.max(145, bubbleWidth - 32);
        final Font senderFont = new Font("Segoe UI", Font.BOLD, 10);
        final Font bodyFont = new Font("Segoe UI", Font.PLAIN, 12);
        final Font timeFont = new Font("Segoe UI", Font.PLAIN, 9);

        ChatBubblePanel bubble = new ChatBubblePanel(itMessage,
                itMessage ? IT_BUBBLE : USER_BUBBLE);
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
        bubble.setBorder(new EmptyBorder(8, itMessage ? 10 : 16,
                7, itMessage ? 16 : 10));

        /*
         * Jangan lagi mengandalkan CSS width pada JLabel HTML. Swing HTML
         * sering tetap menghitung satu baris panjang, lalu komponen dipaksa
         * masuk ke lebar bubble sehingga teks dan tanggal terpotong.
         *
         * Setiap bagian pesan sekarang dibungkus berdasarkan lebar piksel
         * FontMetrics dan digambar per baris. Dengan begitu tinggi bubble
         * benar-benar mengikuti jumlah baris yang tampil.
         */
        JPanel senderBlock = createWrappedTextBlock(
                message.sender + roleSuffix(message.role, message.source),
                senderFont,
                itMessage ? new Color(13, 116, 75) : BLUE_DARK,
                contentWidth,
                itMessage);

        JPanel bodyBlock = createWrappedTextBlock(
                message.text,
                bodyFont,
                TEXT,
                contentWidth,
                itMessage);

        JPanel timeBlock = createWrappedTextBlock(
                message.time,
                timeFont,
                MUTED,
                contentWidth,
                itMessage);

        bubble.add(senderBlock);
        bubble.add(Box.createVerticalStrut(3));
        bubble.add(bodyBlock);
        bubble.add(Box.createVerticalStrut(3));
        bubble.add(timeBlock);

        int senderHeight = senderBlock.getPreferredSize().height;
        int bodyHeight = bodyBlock.getPreferredSize().height;
        int timeHeight = timeBlock.getPreferredSize().height;
        int bubbleHeight = 8 + senderHeight + 3 + bodyHeight + 3 + timeHeight + 7;

        bubble.setPreferredSize(new Dimension(bubbleWidth, bubbleHeight));
        bubble.setMinimumSize(new Dimension(bubbleWidth, bubbleHeight));
        bubble.setMaximumSize(new Dimension(bubbleWidth, bubbleHeight));

        int rowHeight = bubbleHeight + 1;
        row.setPreferredSize(new Dimension(CHAT_WIDTH - 30, rowHeight));
        row.setMinimumSize(new Dimension(120, rowHeight));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, rowHeight));
        row.add(bubble, itMessage ? BorderLayout.EAST : BorderLayout.WEST);
        return row;
    }

    /**
     * Membuat blok teks multi-baris dengan ukuran pasti. Setiap JLabel diberi
     * lebar penuh agar alignment kiri/kanan benar-benar berlaku dan tidak ada
     * karakter yang dipotong oleh BoxLayout.
     */
    private JPanel createWrappedTextBlock(String text, Font font, Color color,
            int maxWidth, boolean alignRight) {
        JPanel block = new JPanel();
        block.setOpaque(false);
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        block.setAlignmentX(alignRight ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);

        List<String> lines = wrapTextByPixels(text, font, maxWidth);
        FontMetrics metrics = owner.getFontMetrics(font);
        int lineHeight = Math.max(font.getSize() + 4, metrics.getHeight());
        int totalHeight = 0;

        for (String line : lines) {
            JLabel label = new JLabel(html(line.length() == 0 ? " " : line));
            label.setFont(font);
            label.setForeground(color);
            label.setHorizontalAlignment(alignRight ? SwingConstants.RIGHT : SwingConstants.LEFT);
            label.setAlignmentX(alignRight ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
            Dimension size = new Dimension(maxWidth, lineHeight);
            label.setPreferredSize(size);
            label.setMinimumSize(size);
            label.setMaximumSize(size);
            block.add(label);
            totalHeight += lineHeight;
        }

        if (lines.isEmpty()) {
            totalHeight = lineHeight;
        }
        Dimension blockSize = new Dimension(maxWidth, totalHeight);
        block.setPreferredSize(blockSize);
        block.setMinimumSize(blockSize);
        block.setMaximumSize(blockSize);
        return block;
    }

    /** Membungkus teks berdasarkan lebar piksel, termasuk kata/URL panjang. */
    private List<String> wrapTextByPixels(String text, Font font, int maxWidth) {
        List<String> result = new ArrayList<String>();
        FontMetrics metrics = owner.getFontMetrics(font);
        String normalized = safe(text).replace("\r", "");
        String[] paragraphs = normalized.split("\n", -1);

        for (String paragraph : paragraphs) {
            if (paragraph.length() == 0) {
                result.add("");
                continue;
            }

            String[] words = paragraph.trim().split("\\s+");
            StringBuilder line = new StringBuilder();
            for (String word : words) {
                if (word.length() == 0) continue;
                String candidate = line.length() == 0 ? word : line.toString() + " " + word;
                if (metrics.stringWidth(candidate) <= maxWidth) {
                    line.setLength(0);
                    line.append(candidate);
                    continue;
                }

                if (line.length() > 0) {
                    result.add(line.toString());
                    line.setLength(0);
                }

                if (metrics.stringWidth(word) <= maxWidth) {
                    line.append(word);
                } else {
                    List<String> pieces = splitLongTokenByPixels(word, metrics, maxWidth);
                    for (int i = 0; i < pieces.size(); i++) {
                        String piece = pieces.get(i);
                        if (i < pieces.size() - 1) {
                            result.add(piece);
                        } else {
                            line.append(piece);
                        }
                    }
                }
            }
            if (line.length() > 0) {
                result.add(line.toString());
            }
        }

        if (result.isEmpty()) result.add("");
        return result;
    }

    private List<String> splitLongTokenByPixels(String token, FontMetrics metrics,
            int maxWidth) {
        List<String> pieces = new ArrayList<String>();
        StringBuilder part = new StringBuilder();
        for (int i = 0; i < token.length(); i++) {
            char ch = token.charAt(i);
            String candidate = part.toString() + ch;
            if (part.length() > 0 && metrics.stringWidth(candidate) > maxWidth) {
                pieces.add(part.toString());
                part.setLength(0);
            }
            part.append(ch);
        }
        if (part.length() > 0) pieces.add(part.toString());
        return pieces;
    }

    private int preferredBubbleWidth(Message message) {
        String body = safe(message.text);
        String senderText = safe(message.sender) + roleSuffix(message.role, message.source);
        String timeText = safe(message.time);
        int longest = Math.max(Math.max(longestLineLength(body), senderText.length()),
                timeText.length());
        int estimated = 44 + longest * 7;
        if (body.length() > 55) estimated = Math.max(estimated, 270);
        // Lebar minimum 190 memastikan tanggal lengkap selalu muat.
        return Math.max(190, Math.min(330, estimated));
    }

    private int longestLineLength(String text) {
        int longest = 0;
        for (String line : safe(text).replace("\r", "").split("\n", -1)) {
            longest = Math.max(longest, line.length());
        }
        return longest;
    }

    private String roleSuffix(String role, String source) {
        String src = "";
        if ("WA_PRIVATE".equalsIgnoreCase(source)) src = " \u2022 WhatsApp Pribadi";
        else if ("WA_GROUP".equalsIgnoreCase(source)) src = " \u2022 WhatsApp Grup";
        else if ("TOAST".equalsIgnoreCase(source)) src = " \u2022 Toast Khanza";
        else if ("WEB".equalsIgnoreCase(source)) src = " \u2022 Web";
        String roleText = "IT".equalsIgnoreCase(role) ? " - Tim IT" : "";
        return roleText + src;
    }

    private void showEmptyMessages(String text) {
        if (messagesPanel == null) return;
        messagesPanel.removeAll();
        JLabel label = new JLabel("<html><div style='text-align:center;width:260px'>" + html(text) + "</div></html>", SwingConstants.CENTER);
        label.setForeground(MUTED);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setBorder(new EmptyBorder(45, 10, 10, 10));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        messagesPanel.add(label);
        messagesPanel.revalidate();
        messagesPanel.repaint();
    }

    private void clearNewTicketForm() {
        if (newTypeCombo != null) newTypeCombo.setSelectedIndex(0);
        if (newTitleField != null) newTitleField.setText("");
        if (newSubcategoryField != null) newSubcategoryField.setText("");
        if (newLocationField != null) newLocationField.setText("");
        if (newPriorityCombo != null) newPriorityCombo.setSelectedIndex(1);
        if (newDetailArea != null) newDetailArea.setText("");
        updateNewCategoryOptions();
    }

    private String selectedPriorityCode() {
        if (newPriorityCombo == null) return "MEDIUM";
        switch (newPriorityCombo.getSelectedIndex()) {
            case 0: return "LOW";
            case 2: return "HIGH";
            case 3: return "CRITICAL";
            default: return "MEDIUM";
        }
    }

    private void sendNewTicket() {
        if (actor != null && "IT".equalsIgnoreCase(actor.role)) {
            showErrorToast("Komputer Tim IT tidak digunakan untuk membuat pengaduan pengguna.");
            return;
        }
        final String type = newTypeCombo != null && newTypeCombo.getSelectedIndex() == 1 ? "SERVICE" : "INCIDENT";
        final String title = newTitleField == null ? "" : safe(newTitleField.getText()).trim();
        final String category = newCategoryCombo == null || newCategoryCombo.getSelectedItem() == null
                ? "" : safe(String.valueOf(newCategoryCombo.getSelectedItem())).trim();
        final String subcategory = newSubcategoryField == null ? "" : safe(newSubcategoryField.getText()).trim();
        final String location = newLocationField == null ? "" : safe(newLocationField.getText()).trim();
        final String priority = selectedPriorityCode();
        final String detail = newDetailArea == null ? "" : safe(newDetailArea.getText()).trim();

        if (title.length() == 0) {
            showErrorToast("Judul pengaduan wajib diisi.");
            if (newTitleField != null) newTitleField.requestFocusInWindow();
            return;
        }
        if (detail.length() == 0) {
            showErrorToast("Detail keluhan atau kebutuhan wajib diisi.");
            if (newDetailArea != null) newDetailArea.requestFocusInWindow();
            return;
        }
        if (title.length() > 200 || detail.length() > 5000 || location.length() > 180) {
            showErrorToast("Judul maksimal 200 karakter, lokasi 180 karakter, dan detail 5.000 karakter.");
            return;
        }

        setCreating(true);
        executeBackground(new Runnable() {
            @Override
            public void run() {
                Map<String, String> extra = new LinkedHashMap<String, String>();
                extra.put("request_type", type);
                extra.put("title", title);
                extra.put("category", category);
                extra.put("subcategory", subcategory);
                extra.put("location", location);
                extra.put("priority", priority);
                ApiResponse created = callApi("create", 0, 0, detail, extra);
                if (created.ok && created.createdTicketId > 0) {
                    currentTicketId = created.createdTicketId;
                    if (!created.tickets.isEmpty()) mergeTickets(created.tickets);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            clearNewTicketForm();
                            BaseToast.showSuccessNoIcon(owner,
                                    "<html><b>Pengaduan " + html(created.createdTicketNo)
                                    + " berhasil dikirim.</b><br>Pengaduan sudah masuk ke antrean komputer Tim IT.<br>Notifikasi WhatsApp diproses terpisah dan tidak memengaruhi tiket.</html>",
                                    4500, null, null);
                        }
                    });
                    loadTicketInCurrentThread(created.createdTicketId, true);
                } else {
                    showErrorToast(created.error.length() == 0 ? "Pengaduan belum dapat dikirim." : created.error);
                }
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        setCreating(false);
                    }
                });
            }
        });
    }

    private void setCreating(boolean creating) {
        if (createTicketButton != null) {
            createTicketButton.setEnabled(!creating);
            createTicketButton.setText(creating ? "Mengirim..." : "Kirim Pengaduan");
        }
        if (newTypeCombo != null) newTypeCombo.setEnabled(!creating);
        if (newCategoryCombo != null) newCategoryCombo.setEnabled(!creating);
        if (newPriorityCombo != null) newPriorityCombo.setEnabled(!creating);
        if (newTitleField != null) newTitleField.setEnabled(!creating);
        if (newSubcategoryField != null) newSubcategoryField.setEnabled(!creating);
        if (newLocationField != null) newLocationField.setEnabled(!creating);
        if (newDetailArea != null) newDetailArea.setEnabled(!creating);
    }

    private void sendCurrentMessage() {
        if (currentTicketId <= 0 || replyArea == null) return;
        final String message = safe(replyArea.getText()).trim();
        if (message.length() == 0) {
            Toolkit.getDefaultToolkit().beep();
            replyArea.requestFocus();
            return;
        }
        if (message.length() > 5000) {
            BaseToast.showDangerNoIcon(owner, "<html>Pesan maksimal 5.000 karakter.</html>", 4000, null, null);
            return;
        }
        setSending(true);
        executeBackground(new Runnable() {
            @Override
            public void run() {
                ApiResponse sent = callApi("send", currentTicketId, 0, message);
                if (sent.ok) {
                    if (!sent.tickets.isEmpty()) mergeTickets(sent.tickets);
                    final int sentTicketId = currentTicketId;
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (replyArea != null) replyArea.setText("");
                            for (Ticket ticket : sent.tickets) {
                                if (ticket.id == sentTicketId) {
                                    currentTicket = ticket;
                                    break;
                                }
                            }
                            updateTicketHeader();
                            setComposerEnabled(currentTicket != null && currentTicket.canReply
                                    && !isFinalStatus(currentTicket.status));
                            appendMessagesIncrementally(sent.messages);
                            updateTicketListModel(snapshotTickets());
                        }
                    });
                    // Fallback untuk server lama: bila response SEND belum memuat
                    // MESSAGE, history tetap disegarkan satu kali seperti V24.
                    if (sent.messages.isEmpty()) {
                        loadTicketInCurrentThread(sentTicketId, false);
                    }
                } else {
                    showErrorToast(sent.error);
                    if (actor != null && "IT".equalsIgnoreCase(actor.role)) {
                        refreshListAndOpenDefault();
                    }
                }
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        setSending(false);
                    }
                });
            }
        });
    }

    private boolean canFinishCurrentTicket() {
        return actor != null
                && "IT".equalsIgnoreCase(actor.role)
                && currentTicket != null
                && currentTicket.canReply
                && !isFinalStatus(currentTicket.status);
    }

    private void finishCurrentTicket() {
        if (!canFinishCurrentTicket() || currentTicketId <= 0) return;
        int answer;
        suppressOutsideClickAutoMinimize = true;
        try {
            answer = javax.swing.JOptionPane.showConfirmDialog(
                    owner,
                    "Yakin permasalahan ini sudah selesai?\n\n"
                    + "Setelah ditutup, Pemohon dan Tim IT tidak dapat mengirim pesan lagi.",
                    "Selesaikan Pengaduan",
                    javax.swing.JOptionPane.YES_NO_OPTION,
                    javax.swing.JOptionPane.QUESTION_MESSAGE);
        } finally {
            suppressOutsideClickAutoMinimize = false;
        }
        if (answer != javax.swing.JOptionPane.YES_OPTION) return;

        if (finishButton != null) finishButton.setEnabled(false);
        if (sendButton != null) sendButton.setEnabled(false);
        if (replyArea != null) replyArea.setEnabled(false);

        final int ticketId = currentTicketId;
        executeBackground(new Runnable() {
            @Override
            public void run() {
                ApiResponse result = callApi("finish", ticketId, 0, "");
                if (result.ok) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            BaseToast.showSuccessNoIcon(owner,
                                    "<html><b>Pengaduan telah selesai.</b><br/>Percakapan sudah ditutup.</html>",
                                    4500, null, null);
                        }
                    });
                    loadTicketInCurrentThread(ticketId, true);
                } else {
                    showErrorToast(result.error);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            updateFinishButtonState();
                            setComposerEnabled(currentTicket != null
                                    && currentTicket.canReply
                                    && !isFinalStatus(currentTicket.status));
                        }
                    });
                }
            }
        });
    }

    private void updateFinishButtonState() {
        if (finishButton == null) return;
        boolean visible = actor != null && "IT".equalsIgnoreCase(actor.role)
                && currentTicket != null && !isFinalStatus(currentTicket.status);
        finishButton.setVisible(visible);
        finishButton.setEnabled(visible && currentTicket.canReply);
        if (visible && !currentTicket.canReply && currentTicket.assignedName.length() > 0) {
            finishButton.setToolTipText("Hanya " + currentTicket.assignedName + " yang dapat menyelesaikan tiket ini.");
        } else {
            finishButton.setToolTipText("Tandai masalah selesai dan tutup percakapan");
        }
        java.awt.Container parent = finishButton.getParent();
        if (parent != null) {
            parent.revalidate();
            parent.repaint();
        }
    }

    private void setSending(boolean sending) {
        boolean allowed = currentTicket != null && currentTicket.canReply && !isFinalStatus(currentTicket.status);
        if (sendButton != null) {
            sendButton.setEnabled(!sending && allowed);
            sendButton.setToolTipText(sending ? "Mengirim balasan..." : composerButtonText());
        }
        if (replyArea != null) replyArea.setEnabled(!sending && allowed);
        if (finishButton != null) finishButton.setEnabled(!sending && canFinishCurrentTicket());
    }

    private String composerButtonText() {
        if (actor != null && "IT".equalsIgnoreCase(actor.role)
                && currentTicket != null && currentTicket.assignedNik.length() == 0) {
            return "Tangani & Kirim";
        }
        return "Kirim";
    }

    private void setComposerEnabled(boolean enabled) {
        if (replyArea != null) {
            replyArea.setEnabled(enabled);
            String tooltip = "Tiket sudah final atau belum dipilih.";
            if (currentTicket != null && !currentTicket.canReply && currentTicket.assignedName.length() > 0) {
                tooltip = "Tiket sedang ditangani oleh " + currentTicket.assignedName + ".";
            } else if (enabled) {
                tooltip = "Enter untuk mengirim. Shift+Enter untuk baris baru.";
            }
            replyArea.setToolTipText(tooltip);
        }
        if (sendButton != null) {
            sendButton.setEnabled(enabled);
            sendButton.setToolTipText(composerButtonText());
        }
        if (detailButton != null) detailButton.setEnabled(currentTicket != null);
        updateFinishButtonState();
        if (connectionLabel != null && currentTicket != null && isFinalStatus(currentTicket.status)) {
            connectionLabel.setText("✓ Pengaduan selesai • Percakapan ditutup");
            connectionLabel.setForeground(GREEN);
        }
    }

    private boolean isFinalStatus(String status) {
        String s = safe(status).toUpperCase();
        return "CLOSED".equals(s) || "CANCELLED".equals(s) || "REJECTED_UNIT".equals(s) || "REJECTED_FINAL".equals(s);
    }

    private void openCurrentDetail() {
        if (currentTicketId <= 0 || detailBaseUrl.length() == 0) return;
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(detailBaseUrl + "/detail.php?id=" + currentTicketId));
            }
        } catch (Exception ex) {
            showErrorToast("Halaman detail tidak dapat dibuka: " + safe(ex.getMessage()));
        }
    }

    private void updateActorUiAsync() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                updateActorUi();
            }
        });
    }

    private void updateActorUi() {
        if (actor == null) {
            if (headerActorLabel != null) headerActorLabel.setText("Menghubungkan akun...");
            if (ticketListTitle != null) ticketListTitle.setText("Riwayat Pengaduan");
            if (newTicketButton != null) newTicketButton.setVisible(true);
            if (newTicketFooterButton != null) newTicketFooterButton.setVisible(true);
            if (emptyNewTicketButton != null) emptyNewTicketButton.setVisible(true);
            if (listEmptyLabel != null) {
                listEmptyLabel.setText("Belum ada pengaduan. Anda tetap dapat membuat pengaduan baru.");
            }
            return;
        }
        boolean isIT = "IT".equalsIgnoreCase(actor.role);
        String role = isIT ? "Tim IT" : "Pemohon";
        if (headerActorLabel != null) {
            headerActorLabel.setText("Sebagai " + actor.name + " (" + role + ") • " + localIp);
        }
        if (ticketListTitle != null) ticketListTitle.setText(isIT ? "Antrean Pengaduan IT" : "Pengaduan Saya");
        if (newTicketButton != null) newTicketButton.setVisible(!isIT);
        if (newTicketFooterButton != null) newTicketFooterButton.setVisible(!isIT);
        if (emptyNewTicketButton != null) emptyNewTicketButton.setVisible(!isIT);
        if (listEmptyLabel != null) {
            listEmptyLabel.setText(isIT
                    ? "Belum ada pengaduan baru dalam antrean."
                    : "Belum ada pengaduan. Klik tombol di bawah untuk mulai mengadu.");
        }
    }

    private void updateActorStatusAsync(final String status) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                if (actor == null && headerActorLabel != null) {
                    headerActorLabel.setText(ellipsize(status, 58));
                    headerActorLabel.setToolTipText(status);
                }
            }
        });
    }

    private void updateConnectionState(boolean connected, String text) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (connectionLabel != null) {
                    connectionLabel.setText((connected ? "\u25cf " : "\u25cf ") + (connected ? "Terhubung ke Diskusi & Grup WA IT" : "Koneksi tertunda"));
                    connectionLabel.setForeground(connected ? GREEN : MUTED);
                    connectionLabel.setToolTipText(safe(text));
                }
            }
        });
    }

    private void updateBadgeAsync(final int count) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                updateBadge(count);
            }
        });
    }

    private void updateBadge(int count) {
        if (statusBarChatIcon != null) statusBarChatIcon.setBadge(count);
        if (statusBarTrigger != null) statusBarTrigger.repaint();
    }

    private int sumUnread(List<Ticket> tickets) {
        int total = 0;
        if (tickets != null) {
            for (Ticket ticket : tickets) total += Math.max(0, ticket.unread);
        }
        return total;
    }

    private void pulseFloatingButton() {
        if (statusBarTrigger == null || statusBarChatIcon == null) return;
        if (statusPulseTimer != null) statusPulseTimer.stop();
        statusPulse = 1f;
        statusBarChatIcon.setPulse(statusPulse);
        statusPulseTimer = new Timer(90, null);
        statusPulseTimer.addActionListener(e -> {
            statusPulse -= 0.12f;
            if (statusPulse <= 0f) {
                statusPulse = 0f;
                statusPulseTimer.stop();
            }
            statusBarChatIcon.setPulse(statusPulse);
            statusBarTrigger.repaint();
        });
        statusPulseTimer.start();
    }

    private void showIncomingNotice(Event event) {
        if (event == null) return;
        boolean itActor = actor != null && "IT".equalsIgnoreCase(actor.role);
        String heading = itActor ? "Pengaduan / pesan baru" : "Balasan baru dari Tim IT";
        String sender = safe(event.sender).length() == 0 ? event.type : event.sender;
        BaseToast.showInfoNoIcon(owner,
                "<html><b>" + html(heading) + "</b><br>" + html(event.noTicket + " \u2022 " + event.title)
                + "<br><span style='color:#52657a'>" + html(sender + ": " + ellipsize(event.message, 105))
                + "</span><br><b>Klik ikon chat di status bar bawah.</b></html>",
                4800, null, null);
    }

    private void updateTicketListAsync(final List<Ticket> tickets) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                updateTicketListModel(tickets);
            }
        });
    }

    private void updateTicketListModel(List<Ticket> tickets) {
        if (ticketListModel == null) return;
        List<Ticket> copy = new ArrayList<Ticket>(tickets);
        Collections.sort(copy, new Comparator<Ticket>() {
            @Override
            public int compare(Ticket a, Ticket b) {
                return Integer.compare(b.id, a.id);
            }
        });
        ticketListModel.clear();
        for (Ticket ticket : copy) ticketListModel.addElement(ticket);
        if (listEmptyLabel != null) listEmptyLabel.setVisible(copy.isEmpty());
        if (emptyNewTicketButton != null) {
            boolean isIT = actor != null && "IT".equalsIgnoreCase(actor.role);
            emptyNewTicketButton.setVisible(copy.isEmpty() && !isIT);
        }
        if (ticketList != null && currentTicketId > 0) {
            for (int i = 0; i < ticketListModel.size(); i++) {
                if (ticketListModel.get(i).id == currentTicketId) {
                    ticketList.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private synchronized void replaceTicketCache(List<Ticket> tickets) {
        ticketCache.clear();
        for (Ticket ticket : tickets) ticketCache.put(ticket.id, ticket);
    }

    private synchronized void mergeTickets(List<Ticket> tickets) {
        for (Ticket ticket : tickets) ticketCache.put(ticket.id, ticket);
    }

    private synchronized List<Ticket> snapshotTickets() {
        return new ArrayList<Ticket>(ticketCache.values());
    }

    private void handleRevokedTicketAsync(final int ticketId, final String assignee) {
        synchronized (this) {
            ticketCache.remove(ticketId);
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (currentTicketId == ticketId) {
                    currentTicketId = 0;
                    currentTicket = null;
                    updateTicketHeader();
                    showEmptyMessages("Tiket ini sudah diambil petugas lain. Sistem akan menampilkan pengaduan berikutnya.");
                    setComposerEnabled(false);
                }
                updateTicketListModel(snapshotTickets());
                String who = safe(assignee).length() == 0 ? "petugas IT lain" : assignee;
                BaseToast.showInfoNoIcon(owner,
                        "<html><b>Tiket sudah ditangani oleh " + html(who)
                        + ".</b><br>Pengaduan tersebut dikeluarkan dari antrean komputer ini.</html>",
                        4200, null, null);
            }
        });
    }

    private void repositionUi() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::repositionUi);
            return;
        }
        if (chatPanel == null || chatWindow == null || !owner.isShowing()) return;
        try {
            JLayeredPane layered = owner.getLayeredPane();
            int availableWidth = Math.max(300, layered.getWidth() - 30);
            int availableHeight = Math.max(380, layered.getHeight() - 100);
            int width = Math.min(CHAT_WIDTH, availableWidth);
            int height = Math.min(CHAT_HEIGHT, availableHeight);
            Point origin = layered.getLocationOnScreen();
            int x = origin.x + Math.max(10, layered.getWidth() - width - 24);
            int y = origin.y + Math.max(52, layered.getHeight() - height - STATUS_BAR_BOTTOM_GAP);
            chatWindow.setBounds(x, y, width, height);
            chatPanel.revalidate();
            chatPanel.repaint();
        } catch (java.awt.IllegalComponentStateException ignore) {
            // Owner belum benar-benar tampil; listener componentShown akan mencoba lagi.
        }
    }

    private void destroyUi() {
        uninstallOutsideClickListener();
        if (ownerMoveListener != null) {
            owner.removeComponentListener(ownerMoveListener);
            ownerMoveListener = null;
        }
        if (statusPulseTimer != null) {
            statusPulseTimer.stop();
            statusPulseTimer = null;
        }
        statusPulse = 0f;
        if (statusBarTrigger != null) {
            if (statusBarTriggerListener != null) {
                statusBarTrigger.removeMouseListener(statusBarTriggerListener);
                statusBarTriggerListener = null;
            }
            if (statusBarChatIcon != null) {
                statusBarChatIcon.setBadge(0);
                statusBarChatIcon.setPulse(0f);
            }
            statusBarTrigger.setEnabled(false);
            statusBarTrigger.setVisible(false);
            statusBarTrigger.repaint();
        }
        if (chatWindow != null) {
            chatWindow.setVisible(false);
            chatWindow.getContentPane().removeAll();
            chatWindow.dispose();
            chatWindow = null;
        }
        chatPanel = null;
        chatOpenedOnce = false;
        activeView = "LIST";
        suppressOutsideClickAutoMinimize = false;
        dialogRoot = null;
        bodyPanel = null;
        bodyCards = null;
        messagesPanel = null;
        messagesScroll = null;
        replyArea = null;
        sendButton = null;
        detailButton = null;
        finishButton = null;
        ticketListModel = null;
        ticketList = null;
        listEmptyLabel = null;
        ticketListTitle = null;
        newTicketButton = null;
        newTicketFooterButton = null;
        emptyNewTicketButton = null;
        newTypeCombo = null;
        newCategoryCombo = null;
        newPriorityCombo = null;
        newTitleField = null;
        newSubcategoryField = null;
        newLocationField = null;
        newDetailArea = null;
        createTicketButton = null;
    }

    private void executeBackground(Runnable task) {
        ExecutorService service = interactionExecutor;
        if (service != null && running.get() && !service.isShutdown()) {
            try {
                service.execute(task);
            } catch (Throwable ignore) {}
        }
    }

    private void playNotificationSound() {
        if (!soundPlaying.compareAndSet(false, true)) return;
        Thread soundThread = new Thread(new Runnable() {
            @Override
            public void run() {
                SourceDataLine line = null;
                try {
                    AudioFormat format = new AudioFormat(16000f, 16, 1, true, false);
                    line = AudioSystem.getSourceDataLine(format);
                    line.open(format, 16000);
                    line.start();
                    writeTone(line, 880.0, 180, 0.32);
                    writeSilence(line, 75);
                    writeTone(line, 659.25, 260, 0.34);
                    line.drain();
                } catch (Throwable ex) {
                    try { Toolkit.getDefaultToolkit().beep(); } catch (Throwable ignore) {}
                } finally {
                    try { if (line != null) line.close(); } catch (Throwable ignore) {}
                    soundPlaying.set(false);
                }
            }
        }, "IT-Service-Ting-Tong");
        soundThread.setDaemon(true);
        soundThread.setPriority(Thread.MIN_PRIORITY);
        soundThread.start();
    }

    private void writeTone(SourceDataLine line, double frequency, int durationMs, double volume) {
        final int sampleRate = 16000;
        int samples = Math.max(1, sampleRate * durationMs / 1000);
        byte[] data = new byte[samples * 2];
        int fade = Math.min(samples / 4, sampleRate / 80);
        for (int i = 0; i < samples; i++) {
            double envelope = 1.0;
            if (fade > 0 && i < fade) envelope = (double) i / fade;
            if (fade > 0 && i >= samples - fade) envelope = Math.min(envelope, (double) (samples - i - 1) / fade);
            short value = (short) (Math.sin(2.0 * Math.PI * frequency * i / sampleRate)
                    * 32767.0 * volume * Math.max(0.0, envelope));
            data[i * 2] = (byte) (value & 0xff);
            data[i * 2 + 1] = (byte) ((value >>> 8) & 0xff);
        }
        line.write(data, 0, data.length);
    }

    private void writeSilence(SourceDataLine line, int durationMs) {
        byte[] silence = new byte[Math.max(2, 16000 * durationMs / 1000 * 2)];
        line.write(silence, 0, silence.length);
    }

    private void showErrorToast(String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                BaseToast.showDangerNoIcon(owner,
                        "<html>Chat IT Service belum dapat diproses.<br/>" + html(ellipsize(message, 180)) + "</html>",
                        5000, null, null);
            }
        });
    }

    private static String deriveDetailBaseUrl(String url) {
        if (url == null) return "";
        String marker = "/api/khanza_chat.php";
        int index = url.indexOf(marker);
        return index >= 0 ? url.substring(0, index) : trimTrailingSlash(url);
    }

    private static String trimTrailingSlash(String value) {
        String v = safe(value).trim();
        while (v.endsWith("/")) v = v.substring(0, v.length() - 1);
        return v;
    }

    private static int parseInt(String value) {
        try { return Integer.parseInt(safe(value)); } catch (Exception ex) { return 0; }
    }

    private static String b64(String value) {
        try {
            return new String(Base64.getDecoder().decode(safe(value)), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            return "";
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String ellipsize(String value, int max) {
        String v = safe(value).replace('\n', ' ').replace('\r', ' ').trim();
        return v.length() <= max ? v : v.substring(0, Math.max(0, max - 3)) + "...";
    }

    private static String html(String value) {
        return safe(value).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static String htmlMultiline(String value) {
        return html(value).replace("\r", "").replace("\n", "<br>");
    }

    private static final class Actor {
        final String role, nik, name, workstationId, targetType, targetKey;
        Actor(String role, String nik, String name, String workstationId, String targetType, String targetKey) {
            this.role = role; this.nik = nik; this.name = name; this.workstationId = workstationId;
            this.targetType = targetType; this.targetKey = targetKey;
        }
    }

    private static final class Event {
        final int id, ticketId;
        final String noTicket, type, priority, status, title, sender, message, time;
        Event(int id, int ticketId, String noTicket, String type, String priority, String status,
                String title, String sender, String message, String time) {
            this.id = id; this.ticketId = ticketId; this.noTicket = noTicket; this.type = type;
            this.priority = priority; this.status = status; this.title = title; this.sender = sender;
            this.message = message; this.time = time;
        }
    }

    private static final class Ticket {
        final int id, unread;
        final String noTicket, type, priority, status, title, requester, unit, location, time;
        final String assignedNik, assignedName, assignedSource;
        final boolean canReply, mineAssignment;
        Ticket(int id, String noTicket, String type, String priority, String status, String title,
                String requester, String unit, String location, String time, int unread,
                String assignedNik, String assignedName, boolean canReply,
                boolean mineAssignment, String assignedSource) {
            this.id = id; this.noTicket = noTicket; this.type = type; this.priority = priority;
            this.status = status; this.title = title; this.requester = requester; this.unit = unit;
            this.location = location; this.time = time; this.unread = unread;
            this.assignedNik = safe(assignedNik); this.assignedName = safe(assignedName);
            this.canReply = canReply; this.mineAssignment = mineAssignment;
            this.assignedSource = safe(assignedSource);
        }
        @Override public String toString() { return noTicket + " - " + title; }
    }

    private static final class Message {
        final int id;
        final String sender, role, source, text, time;
        final boolean mine;
        Message(int id, String sender, String role, String source, String text, String time, boolean mine) {
            this.id = id; this.sender = sender; this.role = role; this.source = source;
            this.text = text; this.time = time; this.mine = mine;
        }
    }

    private static final class ApiResponse {
        boolean ok = false;
        String error = "";
        String sessionToken = "";
        Actor actor;
        int revokedTicketId = 0;
        String revokedAssignee = "";
        int unreadCount = -1;
        int createdTicketId = 0;
        String createdTicketNo = "";
        final List<Event> events = new ArrayList<Event>();
        final List<Ticket> tickets = new ArrayList<Ticket>();
        final List<Message> messages = new ArrayList<Message>();
        static ApiResponse error(String message) {
            ApiResponse response = new ApiResponse();
            response.error = safe(message);
            return response;
        }
    }


    /** Tombol kapsul berwarna untuk aksi ringkas di bagian bawah chat. */
    private static final class CapsuleButton extends JButton {
        CapsuleButton(String text) {
            super(text);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setRolloverEnabled(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            Color base = getBackground() == null ? BLUE : getBackground();
            Color fill = base;
            if (!isEnabled()) {
                fill = SolidButton.mix(base, Color.WHITE, 0.48f);
            } else if (getModel().isPressed()) {
                fill = SolidButton.mix(base, Color.BLACK, 0.15f);
            } else if (getModel().isRollover()) {
                fill = SolidButton.mix(base, Color.BLACK, 0.08f);
            }

            int arc = Math.max(18, getHeight() - 1);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, Math.max(0, getWidth() - 1),
                    Math.max(0, getHeight() - 1), arc, arc);

            String value = getText() == null ? "" : getText();
            FontMetrics fm = g2.getFontMetrics(getFont());
            int tx = Math.max(5, (getWidth() - fm.stringWidth(value)) / 2);
            int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            Color fg = getForeground() == null ? Color.WHITE : getForeground();
            if (!isEnabled()) fg = SolidButton.mix(fg, fill, 0.48f);
            g2.setFont(getFont());
            g2.setColor(fg);
            g2.drawString(value, tx, ty);
            g2.dispose();
        }
    }

    /**
     * Tombol berwarna yang tidak bergantung pada painter Look & Feel.
     * Ini penting karena tema Khanza dapat mengabaikan setBackground() pada
     * JButton dan menghasilkan teks putih di atas latar putih.
     */
    private static final class SolidButton extends JButton {
        SolidButton(String text) {
            super(text);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(true);
            setRolloverEnabled(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            Color base = getBackground() == null ? BLUE : getBackground();
            Color fill = base;
            if (!isEnabled()) {
                fill = mix(base, Color.WHITE, 0.48f);
            } else if (getModel().isPressed()) {
                fill = mix(base, Color.BLACK, 0.14f);
            } else if (getModel().isRollover()) {
                fill = mix(base, Color.BLACK, 0.07f);
            }

            int arc = Math.min(12, Math.max(8, getHeight() / 3));
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, Math.max(0, getWidth() - 1),
                    Math.max(0, getHeight() - 1), arc, arc);

            String text = getText() == null ? "" : getText();
            FontMetrics fm = g2.getFontMetrics(getFont());
            int tx = Math.max(4, (getWidth() - fm.stringWidth(text)) / 2);
            int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            Color fg = getForeground() == null ? Color.WHITE : getForeground();
            if (!isEnabled()) fg = mix(fg, fill, 0.48f);
            g2.setFont(getFont());
            g2.setColor(fg);
            g2.drawString(text, tx, ty);
            g2.dispose();
        }

        private static Color mix(Color a, Color b, float ratio) {
            float r = Math.max(0f, Math.min(1f, ratio));
            int red = Math.round(a.getRed() * (1f - r) + b.getRed() * r);
            int green = Math.round(a.getGreen() * (1f - r) + b.getGreen() * r);
            int blue = Math.round(a.getBlue() * (1f - r) + b.getBlue() * r);
            int alpha = Math.round(a.getAlpha() * (1f - r) + b.getAlpha() * r);
            return new Color(red, green, blue, alpha);
        }
    }

    /**
     * Tombol kirim transparan yang memakai resource /picture/paper-plane.png.
     * Jika resource belum tersedia, fallback lama tetap digambar agar tombol tidak hilang.
     */
    private static final class SendIconButton extends JButton {
        private static final int SEND_ICON_SIZE = 44;

        SendIconButton() {
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setHorizontalAlignment(SwingConstants.CENTER);
            setVerticalAlignment(SwingConstants.CENTER);

            try {
                URL resource = ITServiceChatOverlay.class.getResource("/picture/paper-plane.png");
                if (resource != null) {
                    ImageIcon raw = new ImageIcon(resource);
                    if (raw.getIconWidth() > 0 && raw.getIconHeight() > 0) {
                        int sourceW = raw.getIconWidth();
                        int sourceH = raw.getIconHeight();
                        double ratio = Math.min((double) SEND_ICON_SIZE / sourceW,
                                (double) SEND_ICON_SIZE / sourceH);
                        int targetW = Math.max(1, (int) Math.round(sourceW * ratio));
                        int targetH = Math.max(1, (int) Math.round(sourceH * ratio));
                        Image scaled = raw.getImage().getScaledInstance(
                                targetW, targetH, Image.SCALE_SMOOTH);
                        setIcon(new ImageIcon(scaled));
                    }
                }
            } catch (Throwable ignore) {
                // Fallback paint di bawah menjaga tombol tetap dapat digunakan.
            }
        }

        @Override protected void paintComponent(Graphics g) {
            // Jika icon custom tersedia, biarkan JButton hanya menggambar icon tersebut.
            // Tidak ada lingkaran/background tambahan dari source Java.
            if (getIcon() != null) {
                super.paintComponent(g);
                return;
            }

            // Fallback lama apabila paper-plane.png belum ikut terpasang di resource.
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill;
            if (!isEnabled()) fill = new Color(148, 163, 184);
            else if (getModel().isPressed()) fill = BLUE_DARK;
            else if (getModel().isRollover()) fill = new Color(10, 88, 190);
            else fill = BLUE;
            int diameter = Math.max(0, Math.min(getWidth(), getHeight()) - 2);
            int ox = (getWidth() - diameter) / 2;
            int oy = (getHeight() - diameter) / 2;
            g2.setColor(fill);
            g2.fillOval(ox, oy, diameter, diameter);

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            int[] planeX = {cx - 13, cx + 14, cx + 5, cx + 1, cx - 5, cx - 2};
            int[] planeY = {cy - 7, cy - 13, cy + 13, cy + 4, cy + 9, cy + 1};
            g2.setColor(Color.WHITE);
            g2.fillPolygon(planeX, planeY, planeX.length);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(15, 102, 211));
            g2.drawLine(cx - 2, cy + 1, cx + 7, cy - 7);
            g2.dispose();
        }
    }

    /**
     * Ikon header digambar dengan Graphics2D, bukan karakter Unicode. Ini
     * menghindari ikon minimize/close berubah menjadi titik-titik pada font
     * atau Look & Feel tertentu di Windows.
     */
    private static final class HeaderIconButton extends JButton {
        private final boolean closeIcon;
        HeaderIconButton(boolean closeIcon, String tooltip) {
            this.closeIcon = closeIcon;
            setToolTipText(tooltip);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(30, 28));
            setMinimumSize(new Dimension(30, 28));
            setMaximumSize(new Dimension(30, 28));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (getModel().isRollover()) {
                g2.setColor(new Color(255, 255, 255, 35));
                g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 8, 8);
            }
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            if (closeIcon) {
                g2.drawLine(cx - 5, cy - 5, cx + 5, cy + 5);
                g2.drawLine(cx + 5, cy - 5, cx - 5, cy + 5);
            } else {
                g2.drawLine(cx - 6, cy + 4, cx + 6, cy + 4);
            }
            g2.dispose();
        }
    }

    private static final class StatusBarChatIcon implements Icon {
        private final Image commentsImage;
        private int badge = 0;
        private float pulse = 0f;

        StatusBarChatIcon(Image commentsImage) {
            this.commentsImage = commentsImage;
        }

        void setBadge(int value) {
            badge = Math.max(0, value);
        }

        void setPulse(float value) {
            pulse = Math.max(0f, Math.min(1f, value));
        }

        @Override
        public int getIconWidth() {
            return STATUS_ICON_WIDTH;
        }

        @Override
        public int getIconHeight() {
            return STATUS_ICON_HEIGHT;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (pulse > 0f) {
                int alpha = Math.max(0, Math.min(90, Math.round(pulse * 90f)));
                g2.setColor(new Color(37, 99, 235, alpha));
                g2.fillRoundRect(x, y + 1, STATUS_ICON_WIDTH - 1, STATUS_ICON_HEIGHT - 2, 10, 10);
            }

            int imageX = x + 3;
            int imageY = y + 2;
            int imageSize = 21;
            if (commentsImage != null) {
                g2.drawImage(commentsImage, imageX, imageY, imageSize, imageSize, null);
            } else {
                // Fallback ringan agar tombol tetap terlihat bila comments.png belum ada.
                g2.setColor(BLUE);
                g2.fillRoundRect(imageX, imageY + 2, 21, 16, 7, 7);
                int[] px = {imageX + 5, imageX + 3, imageX + 9};
                int[] py = {imageY + 17, imageY + 22, imageY + 18};
                g2.fillPolygon(px, py, 3);
                g2.setColor(Color.WHITE);
                g2.fillOval(imageX + 5, imageY + 9, 3, 3);
                g2.fillOval(imageX + 10, imageY + 9, 3, 3);
                g2.fillOval(imageX + 15, imageY + 9, 3, 3);
            }

            if (badge > 0) {
                String text = badge > 99 ? "99+" : String.valueOf(badge);
                int d = badge > 9 ? 17 : 15;
                int bx = x + STATUS_ICON_WIDTH - d;
                int by = y;
                g2.setColor(RED);
                g2.fillOval(bx, by, d, d);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, badge > 99 ? 8 : 9));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text, bx + (d - fm.stringWidth(text)) / 2,
                        by + (d + fm.getAscent() - fm.getDescent()) / 2);
            }
            g2.dispose();
        }
    }

    /** Bubble chat dengan ekor kecil seperti aplikasi pesan modern. */
    private static final class ChatBubblePanel extends JPanel {
        private final boolean right;
        private final Color fill;
        ChatBubblePanel(boolean right, Color fill) {
            this.right = right;
            this.fill = fill;
            setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int tail = 7;
            int bodyX = right ? 0 : tail;
            int bodyWidth = Math.max(0, getWidth() - tail);
            g2.setColor(fill);
            g2.fillRoundRect(bodyX, 0, Math.max(0, bodyWidth - 1),
                    Math.max(0, getHeight() - 1), 16, 16);
            int baseY = Math.max(18, getHeight() - 18);
            if (right) {
                int[] x = {getWidth() - tail - 4, getWidth(), getWidth() - tail};
                int[] y = {baseY - 4, baseY + 4, baseY + 6};
                g2.fillPolygon(x, y, 3);
            } else {
                int[] x = {tail + 4, 0, tail};
                int[] y = {baseY - 4, baseY + 4, baseY + 6};
                g2.fillPolygon(x, y, 3);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * ComboBox transparan dengan tombol chevron sederhana. Background dan
     * border seluruh kontrol tetap digambar oleh RoundedFormControl sehingga
     * sudut kanan/kiri terlihat utuh pada semua Look & Feel Khanza.
     */
    private static final class RoundedComboBoxUI extends BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {
            JButton arrow = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(MUTED);
                    g2.setStroke(new BasicStroke(1.5f,
                            BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    int cx = getWidth() / 2;
                    int cy = getHeight() / 2;
                    g2.drawLine(cx - 4, cy - 2, cx, cy + 2);
                    g2.drawLine(cx, cy + 2, cx + 4, cy - 2);
                    g2.dispose();
                }
            };
            arrow.setOpaque(false);
            arrow.setContentAreaFilled(false);
            arrow.setBorderPainted(false);
            arrow.setFocusable(false);
            arrow.setPreferredSize(new Dimension(32, 32));
            arrow.setMinimumSize(new Dimension(32, 32));
            arrow.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return arrow;
        }

        @Override
        protected void installDefaults() {
            super.installDefaults();
            if (comboBox != null) {
                comboBox.setOpaque(false);
                comboBox.setBorder(null);
            }
        }

        @Override
        public void paintCurrentValueBackground(
                Graphics g, java.awt.Rectangle bounds, boolean hasFocus) {
            // Sengaja kosong: RoundedFormControl sudah menggambar background.
        }

        @Override
        @SuppressWarnings({"rawtypes", "unchecked"})
        public void paintCurrentValue(
                Graphics g, java.awt.Rectangle bounds, boolean hasFocus) {
            // BasicComboBoxUI dari beberapa Look & Feel dapat memberi warna
            // selection yang sama dengan background ketika combo fokus. Nilai
            // terpilih digambar eksplisit agar tidak pernah tampak kosong.
            javax.swing.ListCellRenderer renderer = comboBox.getRenderer();
            Component component = renderer.getListCellRendererComponent(
                    listBox, comboBox.getSelectedItem(), -1, false, false);
            component.setFont(comboBox.getFont());
            component.setForeground(TEXT);
            if (component instanceof javax.swing.JComponent) {
                ((javax.swing.JComponent) component).setOpaque(false);
            }
            currentValuePane.paintComponent(g, component, comboBox,
                    bounds.x, bounds.y, bounds.width, bounds.height, true);
        }
    }

    /**
     * Bingkai field form dengan sudut melengkung ringan. Child di-clip mengikuti
     * bentuk rounded rectangle agar JTextField, JComboBox, dan JScrollPane tidak
     * kembali terlihat kotak akibat Look & Feel Windows/Nimbus.
     */
    private static final class RoundedFormControl extends JPanel {
        private final int radius;
        private boolean focused;

        RoundedFormControl(javax.swing.JComponent child, int radius) {
            super(new BorderLayout());
            this.radius = radius;
            setOpaque(false);
            setBorder(new EmptyBorder(1, 1, 1, 1));
            add(child, BorderLayout.CENTER);
            installFocusTracking(child);
        }

        private void installFocusTracking(Component component) {
            component.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    focused = true;
                    repaint();
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            focused = hasFocusedDescendant(RoundedFormControl.this);
                            repaint();
                        }
                    });
                }
            });
            if (component instanceof java.awt.Container) {
                Component[] children = ((java.awt.Container) component).getComponents();
                for (Component child : children) installFocusTracking(child);
            }
        }

        private static boolean hasFocusedDescendant(java.awt.Container container) {
            Component focusOwner = java.awt.KeyboardFocusManager
                    .getCurrentKeyboardFocusManager().getFocusOwner();
            return focusOwner != null && SwingUtilities.isDescendingFrom(focusOwner, container);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, Math.max(0, getWidth() - 1),
                    Math.max(0, getHeight() - 1), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        protected void paintChildren(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            // Sisakan ruang dua piksel agar child/viewport tidak menutupi
            // outline wrapper, terutama pada JTextArea di dalam JScrollPane.
            g2.setClip(new java.awt.geom.RoundRectangle2D.Float(
                    2, 2, Math.max(0, getWidth() - 4),
                    Math.max(0, getHeight() - 4), radius - 2, radius - 2));
            super.paintChildren(g2);
            g2.dispose();
        }

        @Override
        public void paint(Graphics g) {
            // Gambar isi dan child terlebih dahulu, lalu outline paling akhir.
            // Dengan urutan ini garis detail keluhan tidak mungkin tertutup
            // viewport JScrollPane maupun komponen Look & Feel.
            super.paint(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(focused ? FOCUS_BLUE : new Color(203, 213, 225));
            g2.setStroke(new BasicStroke(focused ? 1.7f : 1.1f));
            int inset = focused ? 1 : 0;
            g2.drawRoundRect(inset, inset,
                    Math.max(0, getWidth() - 1 - (inset * 2)),
                    Math.max(0, getHeight() - 1 - (inset * 2)),
                    radius, radius);
            g2.dispose();
        }
    }

    /** Bingkai input membulat yang tidak terpengaruh Look & Feel Khanza. */
    private static final class RoundedInputPanel extends JPanel {
        RoundedInputPanel() { setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, Math.max(0, getWidth() - 1),
                    Math.max(0, getHeight() - 1), 26, 26);
            g2.setColor(BORDER);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, Math.max(0, getWidth() - 1),
                    Math.max(0, getHeight() - 1), 26, 26);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * JTextField dengan placeholder visual. Placeholder tidak pernah dimasukkan
     * ke nilai field sehingga validasi dan proses kirim tetap membaca string
     * kosong sampai user benar-benar mengetik.
     */
    private static final class PlaceholderTextField extends JTextField {
        private final String placeholder;

        PlaceholderTextField(String placeholder) {
            this.placeholder = safe(placeholder);
            setOpaque(false);
            getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { repaint(); }
                @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { repaint(); }
                @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { repaint(); }
            });
            addFocusListener(new java.awt.event.FocusAdapter() {
                @Override public void focusGained(java.awt.event.FocusEvent e) { repaint(); }
                @Override public void focusLost(java.awt.event.FocusEvent e) { repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(new Color(148, 163, 184));
                Font base = getFont();
                g2.setFont(base.deriveFont(Font.ITALIC, 12f));
                Insets insets = getInsets();
                FontMetrics fm = g2.getFontMetrics();
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(placeholder, insets.left, y);
                g2.dispose();
            }
        }
    }

    /** JTextArea dengan placeholder ringan tanpa mengubah isi pesan. */
    private static final class PlaceholderTextArea extends JTextArea {
        private final String placeholder;
        PlaceholderTextArea(String placeholder) {
            this.placeholder = safe(placeholder);
            setOpaque(false);
            getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { repaint(); }
                @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { repaint(); }
                @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { repaint(); }
            });
            addFocusListener(new java.awt.event.FocusAdapter() {
                @Override public void focusGained(java.awt.event.FocusEvent e) { repaint(); }
                @Override public void focusLost(java.awt.event.FocusEvent e) { repaint(); }
            });
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().length() == 0) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(new Color(148, 163, 184));
                Font base = getFont();
                g2.setFont(base.deriveFont(Font.ITALIC, 12f));
                Insets insets = getInsets();
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(placeholder, insets.left,
                        insets.top + fm.getAscent());
                g2.dispose();
            }
        }
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color background;
        RoundedPanel(int radius, Color background) {
            this.radius = radius;
            this.background = background;
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(background);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static final class RoundedTopPanel extends JPanel {
        private final int radius;
        private final Color background;
        RoundedTopPanel(int radius, Color background) {
            this.radius = radius;
            this.background = background;
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(background);
            g2.fillRoundRect(0, 0, getWidth(), getHeight() + radius, radius, radius);
            g2.fillRect(0, radius, getWidth(), getHeight() - radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static final class TicketRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setOpaque(true);
            panel.setBackground(isSelected ? BLUE_LIGHT : Color.WHITE);
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                    new EmptyBorder(9, 12, 8, 12)));
            Ticket ticket = (Ticket) value;
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0; c.gridy = 0; c.weightx = 1; c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.HORIZONTAL;
            JLabel no = new JLabel(ticket.noTicket + (ticket.unread > 0 ? "   (" + ticket.unread + " baru)" : ""));
            no.setFont(new Font("Segoe UI", Font.BOLD, 11));
            no.setForeground(BLUE_DARK);
            panel.add(no, c);
            c.gridy++;
            JLabel title = new JLabel(ellipsize(ticket.title, 52));
            title.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            title.setForeground(TEXT);
            panel.add(title, c);
            c.gridy++;
            String owner = ticket.assignedName.length() == 0
                    ? "Belum ditangani"
                    : (ticket.mineAssignment ? "Milik Anda" : "Ditangani " + ticket.assignedName);
            JLabel meta = new JLabel(ticket.type + "  \u2022  " + owner + "  \u2022  " + ticket.time);
            meta.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            meta.setForeground(MUTED);
            panel.add(meta, c);
            return panel;
        }
    }
}
