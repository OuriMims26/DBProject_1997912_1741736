import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Main {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/LogisticsDB";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "26Zakai26!!";

    private static final Color GREEN = new Color(48, 166, 94);
    private static final Color GREEN_DARK = new Color(35, 134, 74);
    private static final Color GREEN_SOFT = new Color(232, 248, 239);
    private static final Color BLUE = new Color(56, 116, 255);
    private static final Color BLUE_SOFT = new Color(232, 239, 255);
    private static final Color RED = new Color(232, 66, 92);
    private static final Color RED_SOFT = new Color(255, 237, 241);
    private static final Color AMBER = new Color(217, 125, 0);
    private static final Color AMBER_SOFT = new Color(255, 246, 229);
    private static final Color DARK = new Color(18, 25, 40);
    private static final Color TEXT = new Color(75, 85, 105);
    private static final Color MUTED = new Color(132, 143, 161);
    private static final Color BORDER = new Color(224, 230, 239);
    private static final Color BACKGROUND = new Color(246, 248, 251);
    private static final Color WHITE = Color.WHITE;

    private final JFrame frame = new JFrame("OuriLogistic - LogisticsDB");
    private final JPanel content = new JPanel(new CardLayout());
    private final JPanel sidebar = new JPanel();
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Swing falls back to its default look and feel.
            }
            new Main().show();
        });
    }

    private static Connection openConnection() throws Exception {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private void show() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1280, 780));
        frame.setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BACKGROUND);
        root.add(createSidebar(), BorderLayout.WEST);

        content.setBackground(BACKGROUND);
        content.add(createDashboard(), "Dashboard");
        content.add(createDataScreen("Fleet Overview", "Vehicles with readable depot names", "Vehicles", vehiclesSql()), "Fleet");
        content.add(createDataScreen("Routes", "Route planning with depot names", "Delivery Routes", routesSql()), "Routes");
        content.add(createDataScreen("Active Deliveries", "Deliveries with depot, rate and zone names", "Deliveries", deliveriesSql()), "Deliveries");
        content.add(createDataScreen("Depots", "Depot names, locations and storage capacity", "Depots", depotsSql()), "Depots");
        content.add(createDataScreen("Delivery Zones", "Zones and postal coverage", "Delivery Zones", zonesSql()), "Zones");
        content.add(createDataScreen("Delivery Rates", "Rates with zone names instead of zone ids", "Delivery Rates", ratesSql()), "Rates");
        content.add(createDataScreen("Vehicle Assignments", "Livreur to vehicle assignments with vehicle plates", "Vehicle Assignments", assignmentsSql()), "Assignments");
        content.add(createDataScreen("Route Stops", "Route stops with route names and order references", "Route Stops", stopsSql()), "Stops");
        content.add(createDataScreen("Status History", "Delivery status history by order reference", "Status History", historySql()), "History");
        content.add(createDataScreen("Incident Alerts", "Incidents connected to orders, depots and zones", "Delivery Incidents", incidentsSql()), "Incidents");
        content.add(createReportsScreen(), "Reports");

        root.add(content, BorderLayout.CENTER);
        frame.setContentPane(root);
        selectScreen("Dashboard");
        frame.pack();
        frame.setVisible(true);
    }

    private JPanel createSidebar() {
        sidebar.setPreferredSize(new Dimension(292, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(WHITE);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER));

        JPanel logoPanel = new JPanel(new BorderLayout(14, 0));
        logoPanel.setOpaque(false);
        logoPanel.setBorder(new EmptyBorder(18, 22, 18, 20));
        JLabel logo = new CircleLogo("OL");
        logo.setForeground(WHITE);
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 17));
        logo.setPreferredSize(new Dimension(54, 54));
        JLabel brand = new JLabel("<html><b>OuriLogistic</b><br><span style='font-size:10px;color:#8490a1'>Fleet Operations</span></html>");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 21));
        brand.setForeground(DARK);
        logoPanel.add(logo, BorderLayout.WEST);
        logoPanel.add(brand, BorderLayout.CENTER);
        sidebar.add(logoPanel);
        sidebar.add(Box.createVerticalStrut(18));

        JPanel navigation = new JPanel();
        navigation.setOpaque(false);
        navigation.setLayout(new BoxLayout(navigation, BoxLayout.Y_AXIS));
        navigation.setBorder(new EmptyBorder(0, 22, 0, 22));
        navigation.setAlignmentX(Component.CENTER_ALIGNMENT);
        addNavButton(navigation, "Dashboard");
        addNavButton(navigation, "Fleet");
        addNavButton(navigation, "Routes");
        addNavButton(navigation, "Deliveries");
        addNavButton(navigation, "Depots");
        addNavButton(navigation, "Zones");
        addNavButton(navigation, "Rates");
        addNavButton(navigation, "Assignments");
        addNavButton(navigation, "Stops");
        addNavButton(navigation, "History");
        addNavButton(navigation, "Incidents");
        addNavButton(navigation, "Reports");
        sidebar.add(navigation);

        sidebar.add(Box.createVerticalGlue());
        JPanel statusWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 22));
        statusWrap.setOpaque(false);
        RoundedPanel statusCard = new RoundedPanel(16, new Color(249, 251, 253));
        statusCard.setLayout(new BorderLayout());
        statusCard.setPreferredSize(new Dimension(240, 84));
        statusCard.setBorder(new EmptyBorder(13, 16, 13, 16));
        JLabel status = new JLabel("<html><span style='font-size:9px;color:#8490a1'>SYSTEM STATUS</span><br><b style='color:#121928'>All systems operational</b><br><span style='color:#30a65e'>Connected</span> to LogisticsDB</html>");
        status.setForeground(TEXT);
        status.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusCard.add(status, BorderLayout.CENTER);
        statusWrap.add(statusCard);
        sidebar.add(statusWrap);
        return sidebar;
    }

    private void addNavButton(JPanel navigation, String name) {
        JButton button = new NavButton(name);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setPreferredSize(new Dimension(248, 48));
        button.setMaximumSize(new Dimension(248, 48));
        button.setMinimumSize(new Dimension(248, 48));
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setBorder(new EmptyBorder(0, 0, 0, 0));
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBackground(WHITE);
        button.setForeground(MUTED);
        button.addActionListener(event -> selectScreen(name));
        navButtons.put(name, button);
        navigation.add(button);
        navigation.add(Box.createVerticalStrut(8));
    }

    private void selectScreen(String name) {
        CardLayout layout = (CardLayout) content.getLayout();
        layout.show(content, name);
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            boolean active = entry.getKey().equals(name);
            entry.getValue().putClientProperty("active", active);
            entry.getValue().setBackground(active ? GREEN_SOFT : WHITE);
            entry.getValue().setForeground(active ? GREEN : MUTED);
            entry.getValue().repaint();
        }
    }

    private JPanel createDashboard() {
        JPanel screen = createScreenShell("Dashboard", "Logistics overview for deliveries, fleet and incidents");
        JPanel dashboardBody = new JPanel(new GridBagLayout());
        dashboardBody.setOpaque(false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(0, 0, 18, 0);

        JPanel stats = new JPanel(new GridLayout(1, 4, 18, 18));
        stats.setOpaque(false);
        stats.add(createMetricCard("Total Deliveries", count("deliveries")));
        stats.add(createMetricCard("Active Fleet", count("vehicles")));
        stats.add(createMetricCard("Open Routes", countWhere("delivery_routes", "status <> 'Terminee'")));
        stats.add(createMetricCard("Incident Alerts", count("delivery_incidents")));
        constraints.gridy = 0;
        constraints.weighty = 0;
        dashboardBody.add(stats, constraints);

        JPanel middle = new JPanel(new GridLayout(1, 2, 18, 18));
        middle.setOpaque(false);
        middle.add(createRouteMapCard());
        middle.add(createDashboardTable("Route Sequence Preview", routeSequenceSql()));
        middle.setPreferredSize(new Dimension(900, 300));
        middle.setMinimumSize(new Dimension(900, 260));
        constraints.gridy = 1;
        constraints.weighty = 0.45;
        dashboardBody.add(middle, constraints);

        JPanel bottom = new JPanel(new GridLayout(1, 2, 18, 18));
        bottom.setOpaque(false);
        bottom.add(createDashboardTable("Recent Incident Alerts", recentIncidentsSql()));
        bottom.add(createDashboardTable("Delivery Status Mix", deliveryStatusSql()));
        bottom.setPreferredSize(new Dimension(900, 330));
        bottom.setMinimumSize(new Dimension(900, 280));
        constraints.gridy = 2;
        constraints.weighty = 0.55;
        constraints.insets = new Insets(0, 0, 0, 0);
        dashboardBody.add(bottom, constraints);

        JScrollPane dashboardScroll = new JScrollPane(dashboardBody);
        dashboardScroll.setBorder(null);
        dashboardScroll.getViewport().setBackground(BACKGROUND);
        dashboardScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        screen.add(dashboardScroll, BorderLayout.CENTER);
        return screen;
    }

    private JPanel createScreenShell(String title, String subtitle) {
        JPanel outer = new JPanel(new BorderLayout(0, 22));
        outer.setBackground(BACKGROUND);
        outer.setBorder(new EmptyBorder(30, 34, 30, 34));

        JPanel header = new JPanel(new BorderLayout(18, 0));
        header.setOpaque(false);

        JPanel titleStack = new JPanel();
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        titleStack.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        titleLabel.setForeground(DARK);
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(MUTED);
        titleStack.add(titleLabel);
        titleStack.add(Box.createVerticalStrut(4));
        titleStack.add(subtitleLabel);

        header.add(titleStack, BorderLayout.WEST);
        header.add(createTopBar(), BorderLayout.EAST);
        outer.add(header, BorderLayout.NORTH);
        return outer;
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        topBar.setOpaque(false);

        JLabel search = new JLabel("Search fleet, routes, incidents...");
        search.setForeground(MUTED);
        search.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        search.setBorder(new RoundedBorder(BORDER, 18, new Insets(10, 18, 10, 120)));
        search.setOpaque(true);
        search.setBackground(WHITE);

        JLabel date = new JLabel("Apr 12, 2026");
        date.setForeground(TEXT);
        date.setFont(new Font("Segoe UI", Font.BOLD, 13));
        date.setBorder(new RoundedBorder(BORDER, 14, new Insets(10, 16, 10, 16)));
        date.setOpaque(true);
        date.setBackground(WHITE);

        JLabel user = new JLabel("<html><b>Dave Zafr</b><br><span style='color:#8490a1'>Fleet Manager</span></html>");
        user.setForeground(DARK);
        user.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        user.setBorder(new EmptyBorder(0, 10, 0, 0));

        topBar.add(search);
        topBar.add(date);
        topBar.add(user);
        return topBar;
    }

    private JPanel createMetricCard(String label, String value) {
        RoundedPanel card = new ElevatedPanel(18, WHITE);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(24, 24, 24, 24));
        card.setPreferredSize(new Dimension(210, 128));

        JLabel labelView = new JLabel(label.toUpperCase());
        labelView.setFont(new Font("Segoe UI", Font.BOLD, 12));
        labelView.setForeground(MUTED);
        JLabel valueView = new JLabel(value);
        valueView.setFont(new Font("Segoe UI", Font.BOLD, 34));
        valueView.setForeground(DARK);

        card.add(labelView, BorderLayout.NORTH);
        card.add(valueView, BorderLayout.CENTER);
        return card;
    }

    private JPanel createDashboardTable(String title, String sql) {
        RoundedPanel panel = new ElevatedPanel(18, WHITE);
        panel.setLayout(new BorderLayout(0, 14));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setMinimumSize(new Dimension(420, 250));
        panel.setPreferredSize(new Dimension(520, 290));

        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 19));
        label.setForeground(DARK);

        JTable table = new JTable(loadTableModel(sql));
        styleTable(table);
        panel.add(label, BorderLayout.NORTH);
        panel.add(createScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createDataScreen(String title, String subtitle, String tableName, String sql) {
        JPanel screen = createScreenShell(title, subtitle);
        RoundedPanel panel = new ElevatedPanel(18, WHITE);
        panel.setLayout(new BorderLayout(0, 16));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel tools = new JPanel(new BorderLayout(12, 0));
        tools.setOpaque(false);
        JTextField search = new JTextField();
        search.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        search.setPreferredSize(new Dimension(380, 42));
        search.setMargin(new Insets(0, 14, 0, 14));
        search.setBorder(new RoundedBorder(BORDER, 13, new Insets(9, 14, 9, 14)));
        search.setToolTipText("Search " + tableName);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton refresh = createActionButton("Refresh", false);
        JButton add = createActionButton("+ Add", true);
        JButton edit = createActionButton("Edit", false);
        JButton delete = createActionButton("Delete", false);
        actions.add(refresh);
        actions.add(add);
        actions.add(edit);
        actions.add(delete);
        tools.add(search, BorderLayout.CENTER);
        tools.add(actions, BorderLayout.EAST);

        DefaultTableModel model = loadTableModel(sql);
        JTable table = new JTable(model);
        styleTable(table);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        search.getDocument().addDocumentListener(new SimpleDocumentListener(() ->
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(search.getText())))
        ));

        refresh.addActionListener(event -> {
            DefaultTableModel refreshed = loadTableModel(sql);
            table.setModel(refreshed);
            styleTable(table);
            hideTechnicalColumns(table);
            table.setRowSorter(new TableRowSorter<>(refreshed));
            showToast(tableName + " refreshed", GREEN);
        });
        if ("Vehicles".equals(tableName)) {
            add.addActionListener(event -> {
                if (showVehicleDialog(null)) {
                    reloadTable(table, sql);
                    showToast("Vehicle added successfully", GREEN);
                }
            });
            edit.addActionListener(event -> {
                Integer id = selectedId(table);
                if (id == null) {
                    showSelectRowMessage();
                    return;
                }
                if (showVehicleDialog(id)) {
                    reloadTable(table, sql);
                    showToast("Vehicle updated successfully", GREEN);
                }
            });
            delete.addActionListener(event -> {
                Integer id = selectedId(table);
                if (id == null) {
                    showSelectRowMessage();
                    return;
                }
                if (deleteVehicle(id)) {
                    reloadTable(table, sql);
                    showToast("Vehicle deleted successfully", GREEN);
                }
            });
        } else if ("Depots".equals(tableName)) {
            add.addActionListener(event -> {
                if (showDepotDialog(null)) {
                    reloadTable(table, sql);
                    showToast("Depot added successfully", GREEN);
                }
            });
            edit.addActionListener(event -> {
                Integer id = selectedId(table);
                if (id == null) {
                    showSelectRowMessage("Select a depot row first.");
                    return;
                }
                if (showDepotDialog(id)) {
                    reloadTable(table, sql);
                    showToast("Depot updated successfully", GREEN);
                }
            });
            delete.addActionListener(event -> {
                Integer id = selectedId(table);
                if (id == null) {
                    showSelectRowMessage("Select a depot row first.");
                    return;
                }
                if (deleteDepot(id)) {
                    reloadTable(table, sql);
                    showToast("Depot deleted successfully", GREEN);
                }
            });
        } else if ("Delivery Routes".equals(tableName)) {
            add.addActionListener(event -> {
                if (showRouteDialog(null)) {
                    reloadTable(table, sql);
                    showToast("Route added successfully", GREEN);
                }
            });
            edit.addActionListener(event -> {
                Integer id = selectedId(table);
                if (id == null) {
                    showSelectRowMessage("Select a route row first.");
                    return;
                }
                if (showRouteDialog(id)) {
                    reloadTable(table, sql);
                    showToast("Route updated successfully", GREEN);
                }
            });
            delete.addActionListener(event -> {
                Integer id = selectedId(table);
                if (id == null) {
                    showSelectRowMessage("Select a route row first.");
                    return;
                }
                if (deleteRoute(id)) {
                    reloadTable(table, sql);
                    showToast("Route deleted successfully", GREEN);
                }
            });
        } else if ("Deliveries".equals(tableName)) {
            add.addActionListener(event -> {
                if (showDeliveryDialog(null)) {
                    reloadTable(table, sql);
                    showToast("Delivery added successfully", GREEN);
                }
            });
            edit.addActionListener(event -> {
                Integer id = selectedId(table);
                if (id == null) {
                    showSelectRowMessage("Select a delivery row first.");
                    return;
                }
                if (showDeliveryDialog(id)) {
                    reloadTable(table, sql);
                    showToast("Delivery updated successfully", GREEN);
                }
            });
            delete.addActionListener(event -> {
                Integer id = selectedId(table);
                if (id == null) {
                    showSelectRowMessage("Select a delivery row first.");
                    return;
                }
                if (deleteDelivery(id)) {
                    reloadTable(table, sql);
                    showToast("Delivery deleted successfully", GREEN);
                }
            });
        } else if ("Delivery Incidents".equals(tableName)) {
            add.addActionListener(event -> {
                if (showIncidentDialog(null)) {
                    reloadTable(table, sql);
                    showToast("Incident added successfully", GREEN);
                }
            });
            edit.addActionListener(event -> {
                Integer id = selectedId(table);
                if (id == null) {
                    showSelectRowMessage("Select an incident row first.");
                    return;
                }
                if (showIncidentDialog(id)) {
                    reloadTable(table, sql);
                    showToast("Incident updated successfully", GREEN);
                }
            });
            delete.addActionListener(event -> {
                Integer id = selectedId(table);
                if (id == null) {
                    showSelectRowMessage("Select an incident row first.");
                    return;
                }
                if (deleteIncident(id)) {
                    reloadTable(table, sql);
                    showToast("Incident deleted successfully", GREEN);
                }
            });
        } else if ("Delivery Zones".equals(tableName)) {
            add.addActionListener(event -> {
                if (showZoneDialog(null)) {
                    reloadTable(table, sql);
                    showToast("Zone added successfully", GREEN);
                }
            });
            edit.addActionListener(event -> {
                Integer id = selectedId(table);
                if (id == null) {
                    showSelectRowMessage("Select a zone row first.");
                    return;
                }
                if (showZoneDialog(id)) {
                    reloadTable(table, sql);
                    showToast("Zone updated successfully", GREEN);
                }
            });
            delete.addActionListener(event -> {
                Integer id = selectedId(table);
                if (id == null) {
                    showSelectRowMessage("Select a zone row first.");
                    return;
                }
                if (deleteSimple("delivery_zones", "zoneid", id, "Zone")) {
                    reloadTable(table, sql);
                    showToast("Zone deleted successfully", GREEN);
                }
            });
        } else if ("Delivery Rates".equals(tableName)) {
            add.addActionListener(event -> {
                if (showRateDialog(null)) {
                    reloadTable(table, sql);
                    showToast("Rate added successfully", GREEN);
                }
            });
            edit.addActionListener(event -> {
                Integer id = selectedId(table);
                if (id == null) {
                    showSelectRowMessage("Select a rate row first.");
                    return;
                }
                if (showRateDialog(id)) {
                    reloadTable(table, sql);
                    showToast("Rate updated successfully", GREEN);
                }
            });
            delete.addActionListener(event -> {
                Integer id = selectedId(table);
                if (id == null) {
                    showSelectRowMessage("Select a rate row first.");
                    return;
                }
                if (deleteSimple("delivery_rates", "rateid", id, "Rate")) {
                    reloadTable(table, sql);
                    showToast("Rate deleted successfully", GREEN);
                }
            });
        } else if ("Vehicle Assignments".equals(tableName)) {
            add.addActionListener(event -> {
                if (showAssignmentDialog(null)) {
                    reloadTable(table, sql);
                    showToast("Assignment added successfully", GREEN);
                }
            });
            edit.addActionListener(event -> {
                Integer id = selectedId(table);
                if (id == null) {
                    showSelectRowMessage("Select an assignment row first.");
                    return;
                }
                if (showAssignmentDialog(id)) {
                    reloadTable(table, sql);
                    showToast("Assignment updated successfully", GREEN);
                }
            });
            delete.addActionListener(event -> {
                Integer id = selectedId(table);
                if (id == null) {
                    showSelectRowMessage("Select an assignment row first.");
                    return;
                }
                if (deleteSimple("vehicle_assignments", "assignmentid", id, "Assignment")) {
                    reloadTable(table, sql);
                    showToast("Assignment deleted successfully", GREEN);
                }
            });
        } else if ("Route Stops".equals(tableName)) {
            add.addActionListener(event -> {
                if (showStopDialog(null)) {
                    reloadTable(table, sql);
                    showToast("Stop added successfully", GREEN);
                }
            });
            edit.addActionListener(event -> {
                Integer id = selectedId(table);
                if (id == null) {
                    showSelectRowMessage("Select a stop row first.");
                    return;
                }
                if (showStopDialog(id)) {
                    reloadTable(table, sql);
                    showToast("Stop updated successfully", GREEN);
                }
            });
            delete.addActionListener(event -> {
                Integer id = selectedId(table);
                if (id == null) {
                    showSelectRowMessage("Select a stop row first.");
                    return;
                }
                if (deleteSimple("route_stops", "stopid", id, "Route Stop")) {
                    reloadTable(table, sql);
                    showToast("Stop deleted successfully", GREEN);
                }
            });
        } else if ("Status History".equals(tableName)) {
            add.addActionListener(event -> {
                if (showHistoryDialog(null)) {
                    reloadTable(table, sql);
                    showToast("Status history added successfully", GREEN);
                }
            });
            edit.addActionListener(event -> {
                Integer id = selectedId(table);
                if (id == null) {
                    showSelectRowMessage("Select a history row first.");
                    return;
                }
                if (showHistoryDialog(id)) {
                    reloadTable(table, sql);
                    showToast("Status history updated successfully", GREEN);
                }
            });
            delete.addActionListener(event -> {
                Integer id = selectedId(table);
                if (id == null) {
                    showSelectRowMessage("Select a history row first.");
                    return;
                }
                if (deleteSimple("delivery_status_history", "statushistoryid", id, "Status History")) {
                    reloadTable(table, sql);
                    showToast("Status history deleted successfully", GREEN);
                }
            });
        } else {
            add.addActionListener(event -> notReady("Add", tableName));
            edit.addActionListener(event -> notReady("Edit", tableName));
            delete.addActionListener(event -> notReady("Delete", tableName));
        }

        panel.add(tools, BorderLayout.NORTH);
        panel.add(createScrollPane(table), BorderLayout.CENTER);
        hideTechnicalColumns(table);
        screen.add(panel, BorderLayout.CENTER);
        return screen;
    }

    private JPanel createReportsScreen() {
        JPanel screen = createScreenShell("Reports", "Stage B queries and Stage D programs");
        JPanel panel = new JPanel(new GridLayout(2, 2, 18, 18));
        panel.setOpaque(false);
        panel.add(createReportButton(
                "April Transit Incidents",
                "Stage B query: incidents for deliveries in transit",
                () -> showReportResults("April Transit Incidents", stageBIncidentQuery())
        ));
        panel.add(createReportButton(
                "Route Planning Details",
                "Stage B query: detailed stops for route 1",
                () -> showReportResults("Route Planning Details", stageBRoutePlanningQuery())
        ));
        panel.add(createReportButton(
                "Depot Workload Function",
                "Stage D function: fn_depot_workload",
                this::runDepotWorkloadFunction
        ));
        panel.add(createReportButton(
                "Close Route Procedure",
                "Stage D procedure: prc_close_route",
                this::runCloseRouteProcedure
        ));
        screen.add(panel, BorderLayout.CENTER);
        return screen;
    }

    private JButton createReportButton(String title, String subtitle, Runnable action) {
        JButton button = new JButton("<html><b>" + title + "</b><br><span style='color:#8490a1'>" + subtitle + "</span></html>");
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        button.setBackground(WHITE);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorder(new RoundedBorder(BORDER, 18, new Insets(18, 22, 18, 22)));
        button.addActionListener(event -> action.run());
        return button;
    }

    private JButton createActionButton(String text, boolean primary) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(primary ? GREEN : WHITE);
        button.setForeground(primary ? WHITE : DARK);
        button.setBorder(new RoundedBorder(primary ? GREEN : BORDER, 12, new Insets(10, 18, 10, 18)));
        return button;
    }

    private void showReportResults(String title, String sql) {
        JTable table = new JTable(loadTableModel(sql));
        styleTable(table);

        JDialog dialog = createStyledDialog(title);
        JPanel root = createDialogRoot(title, "Results from LogisticsDB.", GREEN);
        JScrollPane scrollPane = createScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(860, 420));
        root.add(scrollPane, BorderLayout.CENTER);
        boolean[] accepted = {false};
        root.add(createDialogActions(dialog, accepted, "Close", null, GREEN), BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private void runDepotWorkloadFunction() {
        Integer depotId = promptInteger("Depot Workload Function", "Enter depot id to analyze:", "1");
        if (depotId == null) {
            return;
        }
        showReportResults("Depot Workload - Depot " + depotId, "SELECT * FROM fn_depot_workload(" + depotId + ")");
    }

    private void runCloseRouteProcedure() {
        Integer routeId = promptInteger("Close Route Procedure", "Enter route id to close:", "1");
        if (routeId == null) {
            return;
        }
        if (!showConfirm("Close Route", "Close route " + routeId + " and mark its deliveries as delivered?", "Close Route")) {
            return;
        }
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement("CALL prc_close_route(?)")) {
            statement.setInt(1, routeId);
            statement.execute();
            showToast("Route " + routeId + " closed successfully", GREEN);
        } catch (Exception exception) {
            showError("Close Route Error", friendlyDatabaseError(exception));
        }
    }

    private Integer promptInteger(String title, String label, String defaultValue) {
        JTextField valueField = new JTextField(defaultValue);
        styleInput(valueField);
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(10, 0, 4, 0));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        addFormRow(form, constraints, 0, label, valueField);

        boolean accepted = showFormDialog(title, "Provide the required id, then run the operation.", form, "Run");
        if (!accepted) {
            return null;
        }
        try {
            return parseInteger(valueField.getText(), label);
        } catch (Exception exception) {
            showError(title, exception.getMessage());
            return null;
        }
    }

    private JPanel createRouteMapCard() {
        RoundedPanel card = new ElevatedPanel(18, WHITE);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        card.setMinimumSize(new Dimension(420, 250));
        card.setPreferredSize(new Dimension(520, 290));
        JLabel title = new JLabel("<html><b>Live Route Tracking</b><br><span style='color:#8490a1'>Real-time route simulation</span></html>");
        title.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        title.setForeground(DARK);
        card.add(title, BorderLayout.NORTH);
        card.add(new RouteCanvas(), BorderLayout.CENTER);
        return card;
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(46);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setForeground(MUTED);
        table.getTableHeader().setBackground(new Color(250, 252, 255));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        table.setGridColor(new Color(242, 245, 249));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setSelectionBackground(GREEN_SOFT);
        table.setSelectionForeground(DARK);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                           boolean focused, int row, int column) {
                Component component = super.getTableCellRendererComponent(table, value, selected, focused, row, column);
                setBorder(new EmptyBorder(0, 14, 0, 14));
                String text = value == null ? "" : value.toString();
                setText(text);
                setFont(new Font("Segoe UI", column == 0 ? Font.BOLD : Font.PLAIN, 14));
                if (!selected) {
                    component.setBackground(row % 2 == 0 ? WHITE : new Color(250, 252, 255));
                    component.setForeground(colorForCell(text, column == 0));
                }
                return component;
            }
        });
    }

    private void reloadTable(JTable table, String sql) {
        DefaultTableModel refreshed = loadTableModel(sql);
        table.setModel(refreshed);
        styleTable(table);
        hideTechnicalColumns(table);
        table.setRowSorter(new TableRowSorter<>(refreshed));
    }

    private void hideTechnicalColumns(JTable table) {
        for (int i = table.getColumnModel().getColumnCount() - 1; i >= 0; i--) {
            String name = table.getColumnModel().getColumn(i).getHeaderValue().toString();
            if (name.startsWith("_")) {
                table.getColumnModel().removeColumn(table.getColumnModel().getColumn(i));
            }
        }
    }

    private Integer selectedId(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        if (!(table.getModel() instanceof DefaultTableModel model)) {
            return null;
        }
        int idColumn = model.findColumn("_id");
        if (idColumn < 0) {
            return null;
        }
        Object value = model.getValueAt(modelRow, idColumn);
        return value instanceof Number number ? number.intValue() : Integer.parseInt(value.toString());
    }

    private void showSelectRowMessage() {
        showSelectRowMessage("Select a vehicle row first.");
    }

    private void showSelectRowMessage(String message) {
        showInfo("Selection Required", message);
    }

    private Color colorForCell(String text, boolean firstColumn) {
        String lower = text.toLowerCase();
        if (lower.contains("incident") || lower.contains("echou") || lower.contains("failed")) {
            return RED;
        }
        if (lower.contains("transit") || lower.contains("active") || lower.contains("route")) {
            return BLUE;
        }
        if (lower.contains("attente") || lower.contains("pending") || lower.contains("maintenance")) {
            return AMBER;
        }
        if (lower.contains("livr") || lower.contains("delivered") || lower.contains("terminee")) {
            return GREEN_DARK;
        }
        return firstColumn ? DARK : TEXT;
    }

    private JScrollPane createScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(235, 239, 246)));
        scrollPane.getViewport().setBackground(WHITE);
        return scrollPane;
    }

    private DefaultTableModel loadTableModel(String sql) {
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        try (Connection connection = openConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            ResultSetMetaData metadata = resultSet.getMetaData();
            int columnCount = metadata.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                model.addColumn(metadata.getColumnLabel(i));
            }
            while (resultSet.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = resultSet.getObject(i);
                }
                model.addRow(row);
            }
        } catch (Exception exception) {
            model.addColumn("Error");
            model.addRow(new Object[]{exception.getMessage()});
        }
        return model;
    }

    private String count(String tableName) {
        return scalar("SELECT COUNT(*) FROM " + tableName);
    }

    private String countWhere(String tableName, String condition) {
        return scalar("SELECT COUNT(*) FROM " + tableName + " WHERE " + condition);
    }

    private String scalar(String sql) {
        try (Connection connection = openConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            return resultSet.next() ? String.valueOf(resultSet.getObject(1)) : "0";
        } catch (Exception exception) {
            return "-";
        }
    }

    private void notReady(String action, String target) {
        showInfo("Next Step", action + " for " + target + " will be connected in the CRUD step.");
    }

    private boolean showVehicleDialog(Integer vehicleId) {
        boolean editMode = vehicleId != null;
        VehicleData existing = editMode ? loadVehicle(vehicleId) : null;
        if (editMode && existing == null) {
            showError("Vehicles", "Vehicle not found.");
            return false;
        }

        List<DepotOption> depots = loadDepotOptions();
        JComboBox<DepotOption> depotBox = new JComboBox<>(depots.toArray(new DepotOption[0]));
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"Camionnette", "Scooter", "Velo", "Camion"});
        JTextField plateField = new JTextField();
        JTextField volumeField = new JTextField();
        JTextField weightField = new JTextField();
        JTextField maintenanceField = new JTextField();

        if (existing != null) {
            selectDepot(depotBox, existing.depotId);
            typeBox.setSelectedItem(existing.type);
            plateField.setText(existing.licensePlate);
            volumeField.setText(String.valueOf(existing.capacityVolume));
            weightField.setText(String.valueOf(existing.capacityWeight));
            maintenanceField.setText(existing.lastMaintenanceDate == null ? "" : existing.lastMaintenanceDate.toString());
        }

        styleInput(plateField);
        styleInput(volumeField);
        styleInput(weightField);
        styleInput(maintenanceField);
        depotBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        typeBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JPanel form = createVehicleForm(depotBox, typeBox, plateField, volumeField, weightField, maintenanceField);
        boolean accepted = showFormDialog(
                editMode ? "Edit Vehicle" : "Add Vehicle",
                editMode ? "Update vehicle details and save the changes." : "Create a new vehicle in the fleet.",
                form,
                editMode ? "Save Changes" : "Add Vehicle"
        );
        if (!accepted) {
            return false;
        }

        try {
            DepotOption depot = (DepotOption) depotBox.getSelectedItem();
            if (depot == null) {
                throw new IllegalArgumentException("Depot is required.");
            }
            VehicleData vehicle = new VehicleData(
                    editMode ? vehicleId : nextId("vehicles", "vehicleid"),
                    depot.id,
                    typeBox.getSelectedItem().toString(),
                    plateField.getText().trim(),
                    parseDouble(volumeField.getText(), "Capacity Volume"),
                    parseDouble(weightField.getText(), "Capacity Weight"),
                    parseDateOrNull(maintenanceField.getText().trim())
            );
            if (vehicle.licensePlate.isBlank()) {
                throw new IllegalArgumentException("License Plate is required.");
            }
            if (editMode) {
                updateVehicle(vehicle);
            } else {
                insertVehicle(vehicle);
            }
            return true;
        } catch (Exception exception) {
            showError("Vehicle Save Error", exception.getMessage());
            return false;
        }
    }

    private boolean showDepotDialog(Integer depotId) {
        boolean editMode = depotId != null;
        DepotData existing = editMode ? loadDepot(depotId) : null;
        if (editMode && existing == null) {
            showError("Depots", "Depot not found.");
            return false;
        }

        JTextField nameField = new JTextField();
        JTextField locationField = new JTextField();
        JTextField capacityField = new JTextField();
        styleInput(nameField);
        styleInput(locationField);
        styleInput(capacityField);

        if (existing != null) {
            nameField.setText(existing.name);
            locationField.setText(existing.location);
            capacityField.setText(String.valueOf(existing.capacity));
        }

        JPanel form = createDepotForm(nameField, locationField, capacityField);
        boolean accepted = showFormDialog(
                editMode ? "Edit Depot" : "Add Depot",
                editMode ? "Update depot details and storage capacity." : "Create a new logistics depot.",
                form,
                editMode ? "Save Changes" : "Add Depot"
        );
        if (!accepted) {
            return false;
        }

        try {
            DepotData depot = new DepotData(
                    editMode ? depotId : nextId("depots", "depotid"),
                    nameField.getText().trim(),
                    locationField.getText().trim(),
                    parseDouble(capacityField.getText(), "Storage Capacity")
            );
            if (depot.name.isBlank()) {
                throw new IllegalArgumentException("Depot name is required.");
            }
            if (depot.location.isBlank()) {
                throw new IllegalArgumentException("Location is required.");
            }
            if (depot.capacity < 0) {
                throw new IllegalArgumentException("Storage Capacity cannot be negative.");
            }
            if (editMode) {
                updateDepot(depot);
            } else {
                insertDepot(depot);
            }
            return true;
        } catch (Exception exception) {
            showError("Depot Save Error", friendlyDatabaseError(exception));
            return false;
        }
    }

    private boolean showRouteDialog(Integer routeId) {
        boolean editMode = routeId != null;
        RouteData existing = editMode ? loadRoute(routeId) : null;
        if (editMode && existing == null) {
            showError("Routes", "Route not found.");
            return false;
        }

        List<DepotOption> depots = loadDepotOptions();
        JComboBox<DepotOption> depotBox = new JComboBox<>(depots.toArray(new DepotOption[0]));
        JComboBox<String> statusBox = new JComboBox<>(new String[]{"Planifiée", "Active", "Terminée", "Annulée"});
        JTextField nameField = new JTextField();
        JTextField dateField = new JTextField();
        styleInput(nameField);
        styleInput(dateField);
        depotBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        if (existing != null) {
            selectDepot(depotBox, existing.depotId);
            statusBox.setSelectedItem(existing.status);
            nameField.setText(existing.name);
            dateField.setText(existing.scheduledStartDate.toString());
        }

        JPanel form = createRouteForm(nameField, depotBox, dateField, statusBox);
        boolean accepted = showFormDialog(
                editMode ? "Edit Route" : "Add Route",
                editMode ? "Update route planning details." : "Create a new delivery route.",
                form,
                editMode ? "Save Changes" : "Add Route"
        );
        if (!accepted) {
            return false;
        }

        try {
            DepotOption depot = (DepotOption) depotBox.getSelectedItem();
            if (depot == null) {
                throw new IllegalArgumentException("Depot is required.");
            }
            RouteData route = new RouteData(
                    editMode ? routeId : nextId("delivery_routes", "routeid"),
                    depot.id,
                    nameField.getText().trim(),
                    parseRequiredDate(dateField.getText().trim(), "Scheduled Start Date"),
                    statusBox.getSelectedItem().toString()
            );
            if (route.name.isBlank()) {
                throw new IllegalArgumentException("Route name is required.");
            }
            if (editMode) {
                updateRoute(route);
            } else {
                insertRoute(route);
            }
            return true;
        } catch (Exception exception) {
            showError("Route Save Error", friendlyDatabaseError(exception));
            return false;
        }
    }

    private boolean showDeliveryDialog(Integer deliveryId) {
        boolean editMode = deliveryId != null;
        DeliveryData existing = editMode ? loadDelivery(deliveryId) : null;
        if (editMode && existing == null) {
            showError("Deliveries", "Delivery not found.");
            return false;
        }

        List<DepotOption> depots = loadDepotOptions();
        List<RateOption> rates = loadRateOptions();
        JComboBox<DepotOption> depotBox = new JComboBox<>(depots.toArray(new DepotOption[0]));
        JComboBox<RateOption> rateBox = new JComboBox<>(rates.toArray(new RateOption[0]));
        JComboBox<String> statusBox = new JComboBox<>(new String[]{"En attente", "En transit", "Livrée", "Échouée", "Incident"});
        JTextField orderField = new JTextField();
        JTextField livreurField = new JTextField();
        JTextField actualDateField = new JTextField();
        styleInput(orderField);
        styleInput(livreurField);
        styleInput(actualDateField);
        depotBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        rateBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        if (existing != null) {
            orderField.setText(String.valueOf(existing.externalOrderId));
            livreurField.setText(existing.externalPrimaryLivreurId == null ? "" : String.valueOf(existing.externalPrimaryLivreurId));
            selectDepot(depotBox, existing.depotId);
            selectRate(rateBox, existing.rateId);
            actualDateField.setText(existing.actualDeliveryDate == null ? "" : existing.actualDeliveryDate.toString());
            statusBox.setSelectedItem(existing.status);
        }

        JPanel form = createDeliveryForm(orderField, livreurField, depotBox, rateBox, actualDateField, statusBox);
        boolean accepted = showFormDialog(
                editMode ? "Edit Delivery" : "Add Delivery",
                editMode ? "Update delivery details and status." : "Create a new delivery record.",
                form,
                editMode ? "Save Changes" : "Add Delivery"
        );
        if (!accepted) {
            return false;
        }

        try {
            DepotOption depot = (DepotOption) depotBox.getSelectedItem();
            RateOption rate = (RateOption) rateBox.getSelectedItem();
            if (depot == null || rate == null) {
                throw new IllegalArgumentException("Depot and rate are required.");
            }
            DeliveryData delivery = new DeliveryData(
                    editMode ? deliveryId : nextId("deliveries", "deliveryid"),
                    parseInteger(orderField.getText(), "External Order ID"),
                    parseOptionalInteger(livreurField.getText(), "External Livreur ID"),
                    depot.id,
                    rate.id,
                    parseDateOrNull(actualDateField.getText().trim()),
                    statusBox.getSelectedItem().toString()
            );
            if (editMode) {
                updateDelivery(delivery);
            } else {
                insertDelivery(delivery);
            }
            return true;
        } catch (Exception exception) {
            showError("Delivery Save Error", friendlyDatabaseError(exception));
            return false;
        }
    }

    private boolean showIncidentDialog(Integer incidentId) {
        boolean editMode = incidentId != null;
        IncidentData existing = editMode ? loadIncident(incidentId) : null;
        if (editMode && existing == null) {
            showError("Incidents", "Incident not found.");
            return false;
        }

        List<DeliveryOption> deliveries = loadDeliveryOptions();
        JComboBox<DeliveryOption> deliveryBox = new JComboBox<>(deliveries.toArray(new DeliveryOption[0]));
        JTextField livreurField = new JTextField();
        JTextField typeField = new JTextField();
        JTextField dateField = new JTextField();
        JTextField descriptionField = new JTextField();
        styleInput(livreurField);
        styleInput(typeField);
        styleInput(dateField);
        styleInput(descriptionField);
        deliveryBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        if (existing != null) {
            selectDelivery(deliveryBox, existing.deliveryId);
            livreurField.setText(existing.externalLivreurId == null ? "" : String.valueOf(existing.externalLivreurId));
            typeField.setText(existing.type);
            dateField.setText(existing.date.toString());
            descriptionField.setText(existing.description);
        }

        JPanel form = createIncidentForm(deliveryBox, livreurField, typeField, dateField, descriptionField);
        boolean accepted = showFormDialog(
                editMode ? "Edit Incident" : "Add Incident",
                editMode ? "Update incident details." : "Report a new delivery incident.",
                form,
                editMode ? "Save Changes" : "Add Incident"
        );
        if (!accepted) {
            return false;
        }

        try {
            DeliveryOption delivery = (DeliveryOption) deliveryBox.getSelectedItem();
            if (delivery == null) {
                throw new IllegalArgumentException("Delivery is required.");
            }
            IncidentData incident = new IncidentData(
                    editMode ? incidentId : nextId("delivery_incidents", "incidentid"),
                    delivery.id,
                    parseOptionalInteger(livreurField.getText(), "External Livreur ID"),
                    typeField.getText().trim(),
                    parseRequiredDate(dateField.getText().trim(), "Incident Date"),
                    descriptionField.getText().trim()
            );
            if (incident.type.isBlank()) {
                throw new IllegalArgumentException("Incident type is required.");
            }
            if (incident.description.isBlank()) {
                throw new IllegalArgumentException("Description is required.");
            }
            if (editMode) {
                updateIncident(incident);
            } else {
                insertIncident(incident);
            }
            return true;
        } catch (Exception exception) {
            showError("Incident Save Error", friendlyDatabaseError(exception));
            return false;
        }
    }

    private JPanel createIncidentForm(
            JComboBox<DeliveryOption> deliveryBox,
            JTextField livreurField,
            JTextField typeField,
            JTextField dateField,
            JTextField descriptionField
    ) {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(10, 0, 4, 0));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        addFormRow(form, constraints, 0, "Delivery", deliveryBox);
        addFormRow(form, constraints, 1, "External Livreur ID", livreurField);
        addFormRow(form, constraints, 2, "Incident Type", typeField);
        addFormRow(form, constraints, 3, "Incident Date", dateField);
        addFormRow(form, constraints, 4, "Description", descriptionField);
        JLabel hint = new JLabel("Date format: yyyy-mm-dd. Livreur may be empty.");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint.setForeground(MUTED);
        constraints.gridx = 1;
        constraints.gridy = 5;
        form.add(hint, constraints);
        return form;
    }

    private IncidentData loadIncident(int incidentId) {
        String sql = """
                SELECT incidentid, deliveryid, externallivreurid, incidenttype, incidentdate, description
                FROM delivery_incidents
                WHERE incidentid = ?
                """;
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, incidentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                Object livreur = resultSet.getObject("externallivreurid");
                return new IncidentData(
                        resultSet.getInt("incidentid"),
                        resultSet.getInt("deliveryid"),
                        livreur == null ? null : ((Number) livreur).intValue(),
                        resultSet.getString("incidenttype"),
                        resultSet.getDate("incidentdate"),
                        resultSet.getString("description")
                );
            }
        } catch (Exception exception) {
            showError("Incident Load Error", exception.getMessage());
            return null;
        }
    }

    private void insertIncident(IncidentData incident) throws Exception {
        String sql = """
                INSERT INTO delivery_incidents
                    (incidentid, deliveryid, externallivreurid, incidenttype, incidentdate, description)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, incident.id);
            statement.setInt(2, incident.deliveryId);
            if (incident.externalLivreurId == null) {
                statement.setNull(3, java.sql.Types.INTEGER);
            } else {
                statement.setInt(3, incident.externalLivreurId);
            }
            statement.setString(4, incident.type);
            statement.setDate(5, incident.date);
            statement.setString(6, incident.description);
            statement.executeUpdate();
        }
    }

    private void updateIncident(IncidentData incident) throws Exception {
        String sql = """
                UPDATE delivery_incidents
                SET deliveryid = ?, externallivreurid = ?, incidenttype = ?, incidentdate = ?, description = ?
                WHERE incidentid = ?
                """;
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, incident.deliveryId);
            if (incident.externalLivreurId == null) {
                statement.setNull(2, java.sql.Types.INTEGER);
            } else {
                statement.setInt(2, incident.externalLivreurId);
            }
            statement.setString(3, incident.type);
            statement.setDate(4, incident.date);
            statement.setString(5, incident.description);
            statement.setInt(6, incident.id);
            statement.executeUpdate();
        }
    }

    private boolean deleteIncident(int incidentId) {
        if (!showConfirm("Delete Incident", "Delete selected incident? This action cannot be undone.", "Delete")) {
            return false;
        }
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM delivery_incidents WHERE incidentid = ?")) {
            statement.setInt(1, incidentId);
            statement.executeUpdate();
            return true;
        } catch (Exception exception) {
            showError("Incident Delete Error", friendlyDatabaseError(exception));
            return false;
        }
    }

    private boolean showZoneDialog(Integer zoneId) {
        boolean editMode = zoneId != null;
        ZoneData existing = editMode ? loadZone(zoneId) : null;
        if (editMode && existing == null) {
            showError("Zones", "Zone not found.");
            return false;
        }

        JTextField nameField = new JTextField();
        JTextField postalField = new JTextField();
        styleInput(nameField);
        styleInput(postalField);
        if (existing != null) {
            nameField.setText(existing.name);
            postalField.setText(existing.postalCodes);
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(10, 0, 4, 0));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        addFormRow(form, constraints, 0, "Zone Name", nameField);
        addFormRow(form, constraints, 1, "Postal Codes Covered", postalField);

        boolean accepted = showFormDialog(editMode ? "Edit Zone" : "Add Zone",
                editMode ? "Update delivery zone details." : "Create a new delivery zone.",
                form,
                editMode ? "Save Changes" : "Add Zone");
        if (!accepted) {
            return false;
        }

        try {
            ZoneData zone = new ZoneData(
                    editMode ? zoneId : nextId("delivery_zones", "zoneid"),
                    nameField.getText().trim(),
                    postalField.getText().trim()
            );
            if (zone.name.isBlank() || zone.postalCodes.isBlank()) {
                throw new IllegalArgumentException("Zone name and postal codes are required.");
            }
            if (editMode) {
                executeUpdate("UPDATE delivery_zones SET zonename = ?, postalcodescovered = ? WHERE zoneid = ?",
                        zone.name, zone.postalCodes, zone.id);
            } else {
                executeUpdate("INSERT INTO delivery_zones (zoneid, zonename, postalcodescovered) VALUES (?, ?, ?)",
                        zone.id, zone.name, zone.postalCodes);
            }
            return true;
        } catch (Exception exception) {
            showError("Zone Save Error", friendlyDatabaseError(exception));
            return false;
        }
    }

    private ZoneData loadZone(int zoneId) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT zoneid, zonename, postalcodescovered FROM delivery_zones WHERE zoneid = ?")) {
            statement.setInt(1, zoneId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return new ZoneData(resultSet.getInt("zoneid"), resultSet.getString("zonename"), resultSet.getString("postalcodescovered"));
            }
        } catch (Exception exception) {
            showError("Zone Load Error", exception.getMessage());
            return null;
        }
    }

    private boolean showRateDialog(Integer rateId) {
        boolean editMode = rateId != null;
        RateData existing = editMode ? loadRate(rateId) : null;
        if (editMode && existing == null) {
            showError("Rates", "Rate not found.");
            return false;
        }

        JComboBox<ZoneOption> zoneBox = new JComboBox<>(loadZoneOptions().toArray(new ZoneOption[0]));
        JTextField weightField = new JTextField();
        JTextField amountField = new JTextField();
        JTextField dateField = new JTextField();
        styleInput(weightField);
        styleInput(amountField);
        styleInput(dateField);
        zoneBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        if (existing != null) {
            selectZone(zoneBox, existing.zoneId);
            weightField.setText(existing.weightClass);
            amountField.setText(String.valueOf(existing.amount));
            dateField.setText(existing.effectiveDate.toString());
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(10, 0, 4, 0));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        addFormRow(form, constraints, 0, "Zone", zoneBox);
        addFormRow(form, constraints, 1, "Weight Class", weightField);
        addFormRow(form, constraints, 2, "Rate Amount", amountField);
        addFormRow(form, constraints, 3, "Effective Date", dateField);

        boolean accepted = showFormDialog(editMode ? "Edit Rate" : "Add Rate",
                editMode ? "Update delivery rate details." : "Create a new delivery rate.",
                form,
                editMode ? "Save Changes" : "Add Rate");
        if (!accepted) {
            return false;
        }

        try {
            ZoneOption zone = (ZoneOption) zoneBox.getSelectedItem();
            if (zone == null) {
                throw new IllegalArgumentException("Zone is required.");
            }
            RateData rate = new RateData(
                    editMode ? rateId : nextId("delivery_rates", "rateid"),
                    zone.id,
                    weightField.getText().trim(),
                    parseDouble(amountField.getText(), "Rate Amount"),
                    parseRequiredDate(dateField.getText().trim(), "Effective Date")
            );
            if (rate.weightClass.isBlank()) {
                throw new IllegalArgumentException("Weight class is required.");
            }
            if (editMode) {
                executeUpdate("UPDATE delivery_rates SET zoneid = ?, weightclass = ?, rateamount = ?, effectivedate = ? WHERE rateid = ?",
                        rate.zoneId, rate.weightClass, rate.amount, rate.effectiveDate, rate.id);
            } else {
                executeUpdate("INSERT INTO delivery_rates (rateid, zoneid, weightclass, rateamount, effectivedate) VALUES (?, ?, ?, ?, ?)",
                        rate.id, rate.zoneId, rate.weightClass, rate.amount, rate.effectiveDate);
            }
            return true;
        } catch (Exception exception) {
            showError("Rate Save Error", friendlyDatabaseError(exception));
            return false;
        }
    }

    private RateData loadRate(int rateId) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT rateid, zoneid, weightclass, rateamount, effectivedate FROM delivery_rates WHERE rateid = ?")) {
            statement.setInt(1, rateId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return new RateData(resultSet.getInt("rateid"), resultSet.getInt("zoneid"), resultSet.getString("weightclass"), resultSet.getDouble("rateamount"), resultSet.getDate("effectivedate"));
            }
        } catch (Exception exception) {
            showError("Rate Load Error", exception.getMessage());
            return null;
        }
    }

    private boolean showAssignmentDialog(Integer assignmentId) {
        boolean editMode = assignmentId != null;
        AssignmentData existing = editMode ? loadAssignment(assignmentId) : null;
        if (editMode && existing == null) {
            showError("Assignments", "Assignment not found.");
            return false;
        }

        JComboBox<VehicleOption> vehicleBox = new JComboBox<>(loadVehicleOptions().toArray(new VehicleOption[0]));
        JTextField livreurField = new JTextField();
        JTextField startField = new JTextField();
        JTextField endField = new JTextField();
        styleInput(livreurField);
        styleInput(startField);
        styleInput(endField);
        vehicleBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        if (existing != null) {
            selectVehicle(vehicleBox, existing.vehicleId);
            livreurField.setText(String.valueOf(existing.livreurId));
            startField.setText(existing.startDate.toString());
            endField.setText(existing.endDate == null ? "" : existing.endDate.toString());
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(10, 0, 4, 0));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        addFormRow(form, constraints, 0, "Livreur Ref", livreurField);
        addFormRow(form, constraints, 1, "Vehicle", vehicleBox);
        addFormRow(form, constraints, 2, "Start Date", startField);
        addFormRow(form, constraints, 3, "End Date", endField);

        boolean accepted = showFormDialog(editMode ? "Edit Assignment" : "Add Assignment",
                editMode ? "Update vehicle assignment details." : "Assign a vehicle to a livreur.",
                form,
                editMode ? "Save Changes" : "Add Assignment");
        if (!accepted) {
            return false;
        }

        try {
            VehicleOption vehicle = (VehicleOption) vehicleBox.getSelectedItem();
            if (vehicle == null) {
                throw new IllegalArgumentException("Vehicle is required.");
            }
            AssignmentData assignment = new AssignmentData(
                    editMode ? assignmentId : nextId("vehicle_assignments", "assignmentid"),
                    parseInteger(livreurField.getText(), "Livreur Ref"),
                    vehicle.id,
                    parseRequiredDate(startField.getText().trim(), "Start Date"),
                    parseDateOrNull(endField.getText().trim())
            );
            if (editMode) {
                executeUpdate("UPDATE vehicle_assignments SET externallivreurid = ?, vehicleid = ?, startdate = ?, enddate = ? WHERE assignmentid = ?",
                        assignment.livreurId, assignment.vehicleId, assignment.startDate, assignment.endDate, assignment.id);
            } else {
                executeUpdate("INSERT INTO vehicle_assignments (assignmentid, externallivreurid, vehicleid, startdate, enddate) VALUES (?, ?, ?, ?, ?)",
                        assignment.id, assignment.livreurId, assignment.vehicleId, assignment.startDate, assignment.endDate);
            }
            return true;
        } catch (Exception exception) {
            showError("Assignment Save Error", friendlyDatabaseError(exception));
            return false;
        }
    }

    private AssignmentData loadAssignment(int assignmentId) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT assignmentid, externallivreurid, vehicleid, startdate, enddate FROM vehicle_assignments WHERE assignmentid = ?")) {
            statement.setInt(1, assignmentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return new AssignmentData(resultSet.getInt("assignmentid"), resultSet.getInt("externallivreurid"), resultSet.getInt("vehicleid"), resultSet.getDate("startdate"), resultSet.getDate("enddate"));
            }
        } catch (Exception exception) {
            showError("Assignment Load Error", exception.getMessage());
            return null;
        }
    }

    private boolean showStopDialog(Integer stopId) {
        boolean editMode = stopId != null;
        StopData existing = editMode ? loadStop(stopId) : null;
        if (editMode && existing == null) {
            showError("Route Stops", "Stop not found.");
            return false;
        }

        JComboBox<RouteOption> routeBox = new JComboBox<>(loadRouteOptions().toArray(new RouteOption[0]));
        JComboBox<DeliveryOption> deliveryBox = new JComboBox<>(loadDeliveryOptions().toArray(new DeliveryOption[0]));
        JTextField sequenceField = new JTextField();
        styleInput(sequenceField);
        routeBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        deliveryBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        if (existing != null) {
            selectRoute(routeBox, existing.routeId);
            selectDelivery(deliveryBox, existing.deliveryId);
            sequenceField.setText(String.valueOf(existing.sequence));
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(10, 0, 4, 0));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        addFormRow(form, constraints, 0, "Route", routeBox);
        addFormRow(form, constraints, 1, "Delivery", deliveryBox);
        addFormRow(form, constraints, 2, "Stop Sequence", sequenceField);

        boolean accepted = showFormDialog(editMode ? "Edit Route Stop" : "Add Route Stop",
                editMode ? "Update route stop details." : "Add a delivery stop to a route.",
                form,
                editMode ? "Save Changes" : "Add Stop");
        if (!accepted) {
            return false;
        }

        try {
            RouteOption route = (RouteOption) routeBox.getSelectedItem();
            DeliveryOption delivery = (DeliveryOption) deliveryBox.getSelectedItem();
            if (route == null || delivery == null) {
                throw new IllegalArgumentException("Route and delivery are required.");
            }
            StopData stop = new StopData(
                    editMode ? stopId : nextId("route_stops", "stopid"),
                    route.id,
                    delivery.id,
                    parseInteger(sequenceField.getText(), "Stop Sequence")
            );
            if (editMode) {
                executeUpdate("UPDATE route_stops SET routeid = ?, deliveryid = ?, stopsequence = ? WHERE stopid = ?",
                        stop.routeId, stop.deliveryId, stop.sequence, stop.id);
            } else {
                executeUpdate("INSERT INTO route_stops (stopid, routeid, deliveryid, stopsequence) VALUES (?, ?, ?, ?)",
                        stop.id, stop.routeId, stop.deliveryId, stop.sequence);
            }
            return true;
        } catch (Exception exception) {
            showError("Route Stop Save Error", friendlyDatabaseError(exception));
            return false;
        }
    }

    private StopData loadStop(int stopId) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT stopid, routeid, deliveryid, stopsequence FROM route_stops WHERE stopid = ?")) {
            statement.setInt(1, stopId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return new StopData(resultSet.getInt("stopid"), resultSet.getInt("routeid"), resultSet.getInt("deliveryid"), resultSet.getInt("stopsequence"));
            }
        } catch (Exception exception) {
            showError("Route Stop Load Error", exception.getMessage());
            return null;
        }
    }

    private boolean showHistoryDialog(Integer historyId) {
        boolean editMode = historyId != null;
        HistoryData existing = editMode ? loadHistory(historyId) : null;
        if (editMode && existing == null) {
            showError("Status History", "Status history row not found.");
            return false;
        }

        JComboBox<DeliveryOption> deliveryBox = new JComboBox<>(loadDeliveryOptions().toArray(new DeliveryOption[0]));
        JComboBox<String> statusBox = new JComboBox<>(new String[]{"En attente", "En transit", "Livrée", "Échouée", "Incident"});
        JTextField changedField = new JTextField();
        styleInput(changedField);
        deliveryBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        if (existing != null) {
            selectDelivery(deliveryBox, existing.deliveryId);
            statusBox.setSelectedItem(existing.status);
            changedField.setText(existing.changedDate.toString());
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(10, 0, 4, 0));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        addFormRow(form, constraints, 0, "Delivery", deliveryBox);
        addFormRow(form, constraints, 1, "Status", statusBox);
        addFormRow(form, constraints, 2, "Changed Date", changedField);
        JLabel hint = new JLabel("Timestamp format: yyyy-mm-dd hh:mm:ss");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint.setForeground(MUTED);
        constraints.gridx = 1;
        constraints.gridy = 3;
        form.add(hint, constraints);

        boolean accepted = showFormDialog(editMode ? "Edit Status History" : "Add Status History",
                editMode ? "Update delivery status history." : "Add a status history event.",
                form,
                editMode ? "Save Changes" : "Add History");
        if (!accepted) {
            return false;
        }

        try {
            DeliveryOption delivery = (DeliveryOption) deliveryBox.getSelectedItem();
            if (delivery == null) {
                throw new IllegalArgumentException("Delivery is required.");
            }
            HistoryData history = new HistoryData(
                    editMode ? historyId : nextId("delivery_status_history", "statushistoryid"),
                    delivery.id,
                    statusBox.getSelectedItem().toString(),
                    parseTimestamp(changedField.getText().trim(), "Changed Date")
            );
            if (editMode) {
                executeUpdate("UPDATE delivery_status_history SET deliveryid = ?, status = ?, changeddate = ? WHERE statushistoryid = ?",
                        history.deliveryId, history.status, history.changedDate, history.id);
            } else {
                executeUpdate("INSERT INTO delivery_status_history (statushistoryid, deliveryid, status, changeddate) VALUES (?, ?, ?, ?)",
                        history.id, history.deliveryId, history.status, history.changedDate);
            }
            return true;
        } catch (Exception exception) {
            showError("Status History Save Error", friendlyDatabaseError(exception));
            return false;
        }
    }

    private HistoryData loadHistory(int historyId) {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT statushistoryid, deliveryid, status, changeddate FROM delivery_status_history WHERE statushistoryid = ?")) {
            statement.setInt(1, historyId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return new HistoryData(resultSet.getInt("statushistoryid"), resultSet.getInt("deliveryid"), resultSet.getString("status"), resultSet.getTimestamp("changeddate"));
            }
        } catch (Exception exception) {
            showError("Status History Load Error", exception.getMessage());
            return null;
        }
    }

    private JPanel createDeliveryForm(
            JTextField orderField,
            JTextField livreurField,
            JComboBox<DepotOption> depotBox,
            JComboBox<RateOption> rateBox,
            JTextField actualDateField,
            JComboBox<String> statusBox
    ) {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(10, 0, 4, 0));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        addFormRow(form, constraints, 0, "External Order ID", orderField);
        addFormRow(form, constraints, 1, "Primary Livreur ID", livreurField);
        addFormRow(form, constraints, 2, "Depot", depotBox);
        addFormRow(form, constraints, 3, "Rate", rateBox);
        addFormRow(form, constraints, 4, "Actual Delivery Date", actualDateField);
        addFormRow(form, constraints, 5, "Status", statusBox);
        JLabel hint = new JLabel("Date format: yyyy-mm-dd. Livreur and actual date may be empty.");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint.setForeground(MUTED);
        constraints.gridx = 1;
        constraints.gridy = 6;
        form.add(hint, constraints);
        return form;
    }

    private DeliveryData loadDelivery(int deliveryId) {
        String sql = """
                SELECT deliveryid, externalorderid, externalprimarylivreurid, depotid, rateid, actualdeliverydate, status
                FROM deliveries
                WHERE deliveryid = ?
                """;
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, deliveryId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                Object livreur = resultSet.getObject("externalprimarylivreurid");
                return new DeliveryData(
                        resultSet.getInt("deliveryid"),
                        resultSet.getInt("externalorderid"),
                        livreur == null ? null : ((Number) livreur).intValue(),
                        resultSet.getInt("depotid"),
                        resultSet.getInt("rateid"),
                        resultSet.getDate("actualdeliverydate"),
                        resultSet.getString("status")
                );
            }
        } catch (Exception exception) {
            showError("Delivery Load Error", exception.getMessage());
            return null;
        }
    }

    private void insertDelivery(DeliveryData delivery) throws Exception {
        String sql = """
                INSERT INTO deliveries
                    (deliveryid, externalorderid, externalprimarylivreurid, depotid, rateid, actualdeliverydate, status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillDeliveryStatement(statement, delivery, true);
            statement.executeUpdate();
        }
    }

    private void updateDelivery(DeliveryData delivery) throws Exception {
        String sql = """
                UPDATE deliveries
                SET externalorderid = ?, externalprimarylivreurid = ?, depotid = ?, rateid = ?, actualdeliverydate = ?, status = ?
                WHERE deliveryid = ?
                """;
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, delivery.externalOrderId);
            if (delivery.externalPrimaryLivreurId == null) {
                statement.setNull(2, java.sql.Types.INTEGER);
            } else {
                statement.setInt(2, delivery.externalPrimaryLivreurId);
            }
            statement.setInt(3, delivery.depotId);
            statement.setInt(4, delivery.rateId);
            statement.setDate(5, delivery.actualDeliveryDate);
            statement.setString(6, delivery.status);
            statement.setInt(7, delivery.id);
            statement.executeUpdate();
        }
    }

    private void fillDeliveryStatement(PreparedStatement statement, DeliveryData delivery, boolean includeId) throws Exception {
        int index = 1;
        if (includeId) {
            statement.setInt(index++, delivery.id);
        }
        statement.setInt(index++, delivery.externalOrderId);
        if (delivery.externalPrimaryLivreurId == null) {
            statement.setNull(index++, java.sql.Types.INTEGER);
        } else {
            statement.setInt(index++, delivery.externalPrimaryLivreurId);
        }
        statement.setInt(index++, delivery.depotId);
        statement.setInt(index++, delivery.rateId);
        statement.setDate(index++, delivery.actualDeliveryDate);
        statement.setString(index, delivery.status);
    }

    private boolean deleteDelivery(int deliveryId) {
        int references = countDeliveryReferences(deliveryId);
        if (references > 0) {
            showError(
                    "Delivery Cannot Be Deleted",
                    "This delivery is still used by " + references + " related row(s).\n\n"
                            + "Delete route stops, status history or incidents first."
            );
            return false;
        }
        if (!showConfirm("Delete Delivery", "Delete selected delivery? This action cannot be undone.", "Delete")) {
            return false;
        }
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM deliveries WHERE deliveryid = ?")) {
            statement.setInt(1, deliveryId);
            statement.executeUpdate();
            return true;
        } catch (Exception exception) {
            showError("Delivery Delete Error", friendlyDatabaseError(exception));
            return false;
        }
    }

    private int countDeliveryReferences(int deliveryId) {
        String sql = """
                SELECT
                    (SELECT COUNT(*) FROM route_stops WHERE deliveryid = ?) +
                    (SELECT COUNT(*) FROM delivery_status_history WHERE deliveryid = ?) +
                    (SELECT COUNT(*) FROM delivery_incidents WHERE deliveryid = ?)
                """;
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, deliveryId);
            statement.setInt(2, deliveryId);
            statement.setInt(3, deliveryId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        } catch (Exception exception) {
            return 0;
        }
    }

    private JPanel createRouteForm(
            JTextField nameField,
            JComboBox<DepotOption> depotBox,
            JTextField dateField,
            JComboBox<String> statusBox
    ) {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(10, 0, 4, 0));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        addFormRow(form, constraints, 0, "Route Name", nameField);
        addFormRow(form, constraints, 1, "Depot", depotBox);
        addFormRow(form, constraints, 2, "Scheduled Start Date", dateField);
        addFormRow(form, constraints, 3, "Status", statusBox);
        JLabel hint = new JLabel("Date format: yyyy-mm-dd");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint.setForeground(MUTED);
        constraints.gridx = 1;
        constraints.gridy = 4;
        form.add(hint, constraints);
        return form;
    }

    private RouteData loadRoute(int routeId) {
        String sql = """
                SELECT routeid, depotid, routename, scheduledstartdate, status
                FROM delivery_routes
                WHERE routeid = ?
                """;
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, routeId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return new RouteData(
                        resultSet.getInt("routeid"),
                        resultSet.getInt("depotid"),
                        resultSet.getString("routename"),
                        resultSet.getDate("scheduledstartdate"),
                        resultSet.getString("status")
                );
            }
        } catch (Exception exception) {
            showError("Route Load Error", exception.getMessage());
            return null;
        }
    }

    private void insertRoute(RouteData route) throws Exception {
        String sql = """
                INSERT INTO delivery_routes (routeid, depotid, routename, scheduledstartdate, status)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, route.id);
            statement.setInt(2, route.depotId);
            statement.setString(3, route.name);
            statement.setDate(4, route.scheduledStartDate);
            statement.setString(5, route.status);
            statement.executeUpdate();
        }
    }

    private void updateRoute(RouteData route) throws Exception {
        String sql = """
                UPDATE delivery_routes
                SET depotid = ?, routename = ?, scheduledstartdate = ?, status = ?
                WHERE routeid = ?
                """;
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, route.depotId);
            statement.setString(2, route.name);
            statement.setDate(3, route.scheduledStartDate);
            statement.setString(4, route.status);
            statement.setInt(5, route.id);
            statement.executeUpdate();
        }
    }

    private boolean deleteRoute(int routeId) {
        int stops = countRouteStops(routeId);
        if (stops > 0) {
            showError(
                    "Route Cannot Be Deleted",
                    "This route still contains " + stops + " stop(s).\n\n"
                            + "Delete or move the route stops first, then try again."
            );
            return false;
        }
        if (!showConfirm("Delete Route", "Delete selected route? This action cannot be undone.", "Delete")) {
            return false;
        }
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM delivery_routes WHERE routeid = ?")) {
            statement.setInt(1, routeId);
            statement.executeUpdate();
            return true;
        } catch (Exception exception) {
            showError("Route Delete Error", friendlyDatabaseError(exception));
            return false;
        }
    }

    private int countRouteStops(int routeId) {
        String sql = "SELECT COUNT(*) FROM route_stops WHERE routeid = ?";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, routeId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        } catch (Exception exception) {
            return 0;
        }
    }

    private JPanel createDepotForm(JTextField nameField, JTextField locationField, JTextField capacityField) {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(10, 0, 4, 0));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        addFormRow(form, constraints, 0, "Depot Name", nameField);
        addFormRow(form, constraints, 1, "Location Address", locationField);
        addFormRow(form, constraints, 2, "Storage Capacity", capacityField);
        return form;
    }

    private DepotData loadDepot(int depotId) {
        String sql = """
                SELECT depotid, depotname, locationaddress, storagecapacity
                FROM depots
                WHERE depotid = ?
                """;
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, depotId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return new DepotData(
                        resultSet.getInt("depotid"),
                        resultSet.getString("depotname"),
                        resultSet.getString("locationaddress"),
                        resultSet.getDouble("storagecapacity")
                );
            }
        } catch (Exception exception) {
            showError("Depot Load Error", exception.getMessage());
            return null;
        }
    }

    private void insertDepot(DepotData depot) throws Exception {
        String sql = """
                INSERT INTO depots (depotid, depotname, locationaddress, storagecapacity)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, depot.id);
            statement.setString(2, depot.name);
            statement.setString(3, depot.location);
            statement.setDouble(4, depot.capacity);
            statement.executeUpdate();
        }
    }

    private void updateDepot(DepotData depot) throws Exception {
        String sql = """
                UPDATE depots
                SET depotname = ?, locationaddress = ?, storagecapacity = ?
                WHERE depotid = ?
                """;
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, depot.name);
            statement.setString(2, depot.location);
            statement.setDouble(3, depot.capacity);
            statement.setInt(4, depot.id);
            statement.executeUpdate();
        }
    }

    private boolean deleteDepot(int depotId) {
        int references = countDepotReferences(depotId);
        if (references > 0) {
            showError(
                    "Depot Cannot Be Deleted",
                    "This depot is still used by " + references + " related row(s).\n\n"
                            + "Move or delete the related vehicles, routes and deliveries first."
            );
            return false;
        }
        if (!showConfirm("Delete Depot", "Delete selected depot? This action cannot be undone.", "Delete")) {
            return false;
        }
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM depots WHERE depotid = ?")) {
            statement.setInt(1, depotId);
            statement.executeUpdate();
            return true;
        } catch (Exception exception) {
            showError("Depot Delete Error", friendlyDatabaseError(exception));
            return false;
        }
    }

    private int countDepotReferences(int depotId) {
        String sql = """
                SELECT
                    (SELECT COUNT(*) FROM vehicles WHERE depotid = ?) +
                    (SELECT COUNT(*) FROM delivery_routes WHERE depotid = ?) +
                    (SELECT COUNT(*) FROM deliveries WHERE depotid = ?)
                """;
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, depotId);
            statement.setInt(2, depotId);
            statement.setInt(3, depotId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        } catch (Exception exception) {
            return 0;
        }
    }

    private VehicleData loadVehicle(int vehicleId) {
        String sql = """
                SELECT vehicleid, depotid, vehicletype, licenseplate, capacityvolume, capacityweight, lastmaintenancedate
                FROM vehicles
                WHERE vehicleid = ?
                """;
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, vehicleId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return new VehicleData(
                        resultSet.getInt("vehicleid"),
                        resultSet.getInt("depotid"),
                        resultSet.getString("vehicletype"),
                        resultSet.getString("licenseplate"),
                        resultSet.getDouble("capacityvolume"),
                        resultSet.getDouble("capacityweight"),
                        resultSet.getDate("lastmaintenancedate")
                );
            }
        } catch (Exception exception) {
            showError("Vehicle Load Error", exception.getMessage());
            return null;
        }
    }

    private void insertVehicle(VehicleData vehicle) throws Exception {
        String sql = """
                INSERT INTO vehicles
                    (vehicleid, depotid, vehicletype, licenseplate, capacityvolume, capacityweight, lastmaintenancedate)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillVehicleStatement(statement, vehicle, true);
            statement.executeUpdate();
        }
    }

    private void updateVehicle(VehicleData vehicle) throws Exception {
        String sql = """
                UPDATE vehicles
                SET depotid = ?, vehicletype = ?, licenseplate = ?, capacityvolume = ?, capacityweight = ?, lastmaintenancedate = ?
                WHERE vehicleid = ?
                """;
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, vehicle.depotId);
            statement.setString(2, vehicle.type);
            statement.setString(3, vehicle.licensePlate);
            statement.setDouble(4, vehicle.capacityVolume);
            statement.setDouble(5, vehicle.capacityWeight);
            statement.setDate(6, vehicle.lastMaintenanceDate);
            statement.setInt(7, vehicle.vehicleId);
            statement.executeUpdate();
        }
    }

    private void fillVehicleStatement(PreparedStatement statement, VehicleData vehicle, boolean includeId) throws Exception {
        int index = 1;
        if (includeId) {
            statement.setInt(index++, vehicle.vehicleId);
        }
        statement.setInt(index++, vehicle.depotId);
        statement.setString(index++, vehicle.type);
        statement.setString(index++, vehicle.licensePlate);
        statement.setDouble(index++, vehicle.capacityVolume);
        statement.setDouble(index++, vehicle.capacityWeight);
        statement.setDate(index, vehicle.lastMaintenanceDate);
    }

    private boolean deleteVehicle(int vehicleId) {
        int assignmentCount = countVehicleAssignments(vehicleId);
        if (assignmentCount > 0) {
            showError(
                    "Vehicle Cannot Be Deleted",
                    "This vehicle is still used in " + assignmentCount + " vehicle assignment(s).\n\n"
                            + "Delete or reassign those rows first, then try again."
            );
            return false;
        }
        if (!showConfirm("Delete Vehicle", "Delete selected vehicle? This action cannot be undone.", "Delete")) {
            return false;
        }
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM vehicles WHERE vehicleid = ?")) {
            statement.setInt(1, vehicleId);
            statement.executeUpdate();
            return true;
        } catch (Exception exception) {
            showError("Vehicle Delete Error", friendlyDatabaseError(exception));
            return false;
        }
    }

    private List<DepotOption> loadDepotOptions() {
        List<DepotOption> depots = new ArrayList<>();
        try (Connection connection = openConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT depotid, depotname FROM depots ORDER BY depotname")) {
            while (resultSet.next()) {
                depots.add(new DepotOption(resultSet.getInt("depotid"), resultSet.getString("depotname")));
            }
        } catch (Exception exception) {
            showError("Depot Load Error", exception.getMessage());
        }
        return depots;
    }

    private List<RateOption> loadRateOptions() {
        List<RateOption> rates = new ArrayList<>();
        String sql = """
                SELECT dr.rateid, dz.zonename, dr.weightclass, dr.rateamount
                FROM delivery_rates dr
                JOIN delivery_zones dz ON dz.zoneid = dr.zoneid
                ORDER BY dz.zonename, dr.weightclass, dr.rateamount
                """;
        try (Connection connection = openConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                rates.add(new RateOption(
                        resultSet.getInt("rateid"),
                        resultSet.getString("zonename"),
                        resultSet.getString("weightclass"),
                        resultSet.getDouble("rateamount")
                ));
            }
        } catch (Exception exception) {
            showError("Rate Load Error", exception.getMessage());
        }
        return rates;
    }

    private List<DeliveryOption> loadDeliveryOptions() {
        List<DeliveryOption> deliveries = new ArrayList<>();
        String sql = """
                SELECT d.deliveryid, d.externalorderid, dep.depotname, d.status
                FROM deliveries d
                JOIN depots dep ON dep.depotid = d.depotid
                ORDER BY d.deliveryid
                """;
        try (Connection connection = openConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                deliveries.add(new DeliveryOption(
                        resultSet.getInt("deliveryid"),
                        resultSet.getInt("externalorderid"),
                        resultSet.getString("depotname"),
                        resultSet.getString("status")
                ));
            }
        } catch (Exception exception) {
            showError("Delivery Load Error", exception.getMessage());
        }
        return deliveries;
    }

    private List<ZoneOption> loadZoneOptions() {
        List<ZoneOption> zones = new ArrayList<>();
        try (Connection connection = openConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT zoneid, zonename FROM delivery_zones ORDER BY zonename")) {
            while (resultSet.next()) {
                zones.add(new ZoneOption(resultSet.getInt("zoneid"), resultSet.getString("zonename")));
            }
        } catch (Exception exception) {
            showError("Zone Load Error", exception.getMessage());
        }
        return zones;
    }

    private List<VehicleOption> loadVehicleOptions() {
        List<VehicleOption> vehicles = new ArrayList<>();
        String sql = """
                SELECT v.vehicleid, v.licenseplate, v.vehicletype, dep.depotname
                FROM vehicles v
                JOIN depots dep ON dep.depotid = v.depotid
                ORDER BY v.licenseplate
                """;
        try (Connection connection = openConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                vehicles.add(new VehicleOption(
                        resultSet.getInt("vehicleid"),
                        resultSet.getString("licenseplate"),
                        resultSet.getString("vehicletype"),
                        resultSet.getString("depotname")
                ));
            }
        } catch (Exception exception) {
            showError("Vehicle Load Error", exception.getMessage());
        }
        return vehicles;
    }

    private List<RouteOption> loadRouteOptions() {
        List<RouteOption> routes = new ArrayList<>();
        String sql = """
                SELECT r.routeid, r.routename, dep.depotname, r.status
                FROM delivery_routes r
                JOIN depots dep ON dep.depotid = r.depotid
                ORDER BY r.routename
                """;
        try (Connection connection = openConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                routes.add(new RouteOption(
                        resultSet.getInt("routeid"),
                        resultSet.getString("routename"),
                        resultSet.getString("depotname"),
                        resultSet.getString("status")
                ));
            }
        } catch (Exception exception) {
            showError("Route Load Error", exception.getMessage());
        }
        return routes;
    }

    private JPanel createVehicleForm(
            JComboBox<DepotOption> depotBox,
            JComboBox<String> typeBox,
            JTextField plateField,
            JTextField volumeField,
            JTextField weightField,
            JTextField maintenanceField
    ) {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(10, 0, 4, 0));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(7, 0, 7, 0);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        addFormRow(form, constraints, 0, "Depot", depotBox);
        addFormRow(form, constraints, 1, "Vehicle Type", typeBox);
        addFormRow(form, constraints, 2, "License Plate", plateField);
        addFormRow(form, constraints, 3, "Capacity Volume", volumeField);
        addFormRow(form, constraints, 4, "Capacity Weight", weightField);
        addFormRow(form, constraints, 5, "Last Maintenance Date", maintenanceField);
        JLabel hint = new JLabel("Date format: yyyy-mm-dd");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint.setForeground(MUTED);
        constraints.gridx = 1;
        constraints.gridy = 6;
        form.add(hint, constraints);
        return form;
    }

    private void addFormRow(JPanel form, GridBagConstraints constraints, int row, String labelText, Component field) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT);
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.weightx = 0;
        constraints.insets = new Insets(7, 0, 7, 16);
        form.add(label, constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.insets = new Insets(7, 0, 7, 0);
        form.add(field, constraints);
    }

    private void styleInput(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(260, 38));
        field.setMargin(new Insets(0, 12, 0, 12));
        field.setBorder(new RoundedBorder(BORDER, 12, new Insets(8, 12, 8, 12)));
    }

    private boolean showFormDialog(String title, String subtitle, JPanel form, String primaryAction) {
        final boolean[] accepted = {false};
        JDialog dialog = createStyledDialog(title);
        JPanel root = createDialogRoot(title, subtitle, GREEN);
        root.add(form, BorderLayout.CENTER);
        root.add(createDialogActions(dialog, accepted, primaryAction, "Cancel", GREEN), BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
        return accepted[0];
    }

    private void showInfo(String title, String message) {
        showMessage(title, message, GREEN, "OK");
    }

    private void showError(String title, String message) {
        showMessage(title, message, RED, "OK");
    }

    private void showMessage(String title, String message, Color accent, String actionText) {
        final boolean[] accepted = {false};
        JDialog dialog = createStyledDialog(title);
        JPanel root = createDialogRoot(title, message, accent);
        root.add(createDialogActions(dialog, accepted, actionText, null, accent), BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private void showToast(String message, Color accent) {
        JDialog toast = new JDialog(frame);
        toast.setUndecorated(true);
        toast.setAlwaysOnTop(true);
        toast.setFocusableWindowState(false);

        JPanel card = new RoundedPanel(18, WHITE);
        card.setLayout(new BorderLayout(12, 0));
        card.setBorder(new EmptyBorder(12, 16, 12, 18));
        JLabel badge = new BadgeLabel("OK", accent);
        badge.setPreferredSize(new Dimension(34, 34));
        JLabel text = new JLabel(message);
        text.setFont(new Font("Segoe UI", Font.BOLD, 13));
        text.setForeground(DARK);
        card.add(badge, BorderLayout.WEST);
        card.add(text, BorderLayout.CENTER);

        toast.setContentPane(card);
        toast.pack();
        int x = frame.getX() + frame.getWidth() - toast.getWidth() - 36;
        int y = frame.getY() + 34;
        toast.setLocation(x, y);
        toast.setVisible(true);

        Timer timer = new Timer(2200, event -> toast.dispose());
        timer.setRepeats(false);
        timer.start();
    }

    private boolean showConfirm(String title, String message, String primaryAction) {
        final boolean[] accepted = {false};
        JDialog dialog = createStyledDialog(title);
        JPanel root = createDialogRoot(title, message, RED);
        root.add(createDialogActions(dialog, accepted, primaryAction, "Cancel", RED), BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
        return accepted[0];
    }

    private JDialog createStyledDialog(String title) {
        JDialog dialog = new JDialog(frame, title, true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);
        return dialog;
    }

    private JPanel createDialogRoot(String title, String subtitle, Color accent) {
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(WHITE);
        root.setBorder(new EmptyBorder(22, 24, 20, 24));

        JPanel header = new JPanel(new BorderLayout(14, 0));
        header.setOpaque(false);
        JLabel icon = new BadgeLabel(accent == RED ? "!" : "OL", accent);
        icon.setPreferredSize(new Dimension(42, 42));

        JLabel text = new JLabel("<html><b style='font-size:15px;color:#121928'>" + escapeHtml(title)
                + "</b><br><span style='color:#4b5569'>" + escapeHtml(subtitle).replace("\n", "<br>") + "</span></html>");
        text.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        text.setPreferredSize(new Dimension(370, text.getPreferredSize().height));
        header.add(icon, BorderLayout.WEST);
        header.add(text, BorderLayout.CENTER);
        root.add(header, BorderLayout.NORTH);
        return root;
    }

    private JPanel createDialogActions(JDialog dialog, boolean[] accepted, String primaryText, String secondaryText, Color primaryColor) {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        if (secondaryText != null) {
            JButton secondary = createActionButton(secondaryText, false);
            secondary.addActionListener(event -> dialog.dispose());
            actions.add(secondary);
        }
        JButton primary = createActionButton(primaryText, true);
        primary.setBackground(primaryColor);
        primary.setBorder(new RoundedBorder(primaryColor, 12, new Insets(10, 18, 10, 18)));
        primary.addActionListener(event -> {
            accepted[0] = true;
            dialog.dispose();
        });
        actions.add(primary);
        return actions;
    }

    private String escapeHtml(String value) {
        return value == null ? "" : value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private int countVehicleAssignments(int vehicleId) {
        String sql = "SELECT COUNT(*) FROM vehicle_assignments WHERE vehicleid = ?";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, vehicleId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        } catch (Exception exception) {
            return 0;
        }
    }

    private String friendlyDatabaseError(Exception exception) {
        String message = exception.getMessage();
        if (message != null && message.toLowerCase().contains("foreign key")) {
            return "This row is still referenced by another table. Remove the related rows first, then try again.";
        }
        return message == null ? "Database operation failed." : message;
    }

    private void selectDepot(JComboBox<DepotOption> comboBox, int depotId) {
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            if (comboBox.getItemAt(i).id == depotId) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectRate(JComboBox<RateOption> comboBox, int rateId) {
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            if (comboBox.getItemAt(i).id == rateId) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectDelivery(JComboBox<DeliveryOption> comboBox, int deliveryId) {
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            if (comboBox.getItemAt(i).id == deliveryId) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectZone(JComboBox<ZoneOption> comboBox, int zoneId) {
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            if (comboBox.getItemAt(i).id == zoneId) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectVehicle(JComboBox<VehicleOption> comboBox, int vehicleId) {
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            if (comboBox.getItemAt(i).id == vehicleId) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectRoute(JComboBox<RouteOption> comboBox, int routeId) {
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            if (comboBox.getItemAt(i).id == routeId) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private void executeUpdate(String sql, Object... values) throws Exception {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                statement.setObject(i + 1, values[i]);
            }
            statement.executeUpdate();
        }
    }

    private boolean deleteSimple(String tableName, String idColumn, int id, String label) {
        if (!showConfirm("Delete " + label, "Delete selected " + label.toLowerCase() + "? This action cannot be undone.", "Delete")) {
            return false;
        }
        try {
            executeUpdate("DELETE FROM " + tableName + " WHERE " + idColumn + " = ?", id);
            return true;
        } catch (Exception exception) {
            showError(label + " Delete Error", friendlyDatabaseError(exception));
            return false;
        }
    }

    private int nextId(String tableName, String idColumn) throws Exception {
        String sql = "SELECT COALESCE(MAX(" + idColumn + "), 0) + 1 FROM " + tableName;
        try (Connection connection = openConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    private double parseDouble(String value, String fieldName) {
        try {
            return Double.parseDouble(value.trim());
        } catch (Exception exception) {
            throw new IllegalArgumentException(fieldName + " must be a number.");
        }
    }

    private int parseInteger(String value, String fieldName) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception exception) {
            throw new IllegalArgumentException(fieldName + " must be an integer.");
        }
    }

    private Integer parseOptionalInteger(String value, String fieldName) {
        if (value == null || value.trim().isBlank()) {
            return null;
        }
        return parseInteger(value, fieldName);
    }

    private Date parseDateOrNull(String value) {
        if (value.isBlank()) {
            return null;
        }
        try {
            return Date.valueOf(value);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Last Maintenance Date must use yyyy-mm-dd format.");
        }
    }

    private Date parseRequiredDate(String value, String fieldName) {
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        try {
            return Date.valueOf(value);
        } catch (Exception exception) {
            throw new IllegalArgumentException(fieldName + " must use yyyy-mm-dd format.");
        }
    }

    private Timestamp parseTimestamp(String value, String fieldName) {
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        try {
            return Timestamp.valueOf(value);
        } catch (Exception exception) {
            throw new IllegalArgumentException(fieldName + " must use yyyy-mm-dd hh:mm:ss format.");
        }
    }

    private String vehiclesSql() {
        return """
                SELECT
                    v.vehicleid AS "_id",
                    v.licenseplate AS "License Plate",
                    v.vehicletype AS "Type",
                    dep.depotname AS "Depot",
                    v.capacityvolume AS "Volume Capacity",
                    v.capacityweight AS "Weight Capacity",
                    v.lastmaintenancedate AS "Last Maintenance"
                FROM vehicles v
                JOIN depots dep ON dep.depotid = v.depotid
                ORDER BY v.vehicleid
                """;
    }

    private String routesSql() {
        return """
                SELECT
                    r.routeid AS "_id",
                    r.routename AS "Route Name",
                    dep.depotname AS "Depot",
                    r.scheduledstartdate AS "Scheduled Start",
                    r.status AS "Status",
                    COUNT(rs.stopid) AS "Stops"
                FROM delivery_routes r
                JOIN depots dep ON dep.depotid = r.depotid
                LEFT JOIN route_stops rs ON rs.routeid = r.routeid
                GROUP BY r.routeid, r.routename, dep.depotname, r.scheduledstartdate, r.status
                ORDER BY r.scheduledstartdate DESC, r.routename
                """;
    }

    private String deliveriesSql() {
        return """
                SELECT
                    d.deliveryid AS "_id",
                    d.externalorderid AS "Order Ref",
                    dep.depotname AS "Depot",
                    dz.zonename AS "Zone",
                    dr.weightclass AS "Weight Class",
                    dr.rateamount AS "Rate",
                    d.actualdeliverydate AS "Actual Delivery",
                    d.status AS "Status"
                FROM deliveries d
                JOIN depots dep ON dep.depotid = d.depotid
                JOIN delivery_rates dr ON dr.rateid = d.rateid
                JOIN delivery_zones dz ON dz.zoneid = dr.zoneid
                ORDER BY d.deliveryid
                """;
    }

    private String depotsSql() {
        return """
                SELECT
                    depotid AS "_id",
                    depotname AS "Depot",
                    locationaddress AS "Location",
                    storagecapacity AS "Storage Capacity"
                FROM depots
                ORDER BY depotname
                """;
    }

    private String incidentsSql() {
        return """
                SELECT
                    di.incidentid AS "_id",
                    d.externalorderid AS "Order Ref",
                    di.incidenttype AS "Incident Type",
                    di.incidentdate AS "Incident Date",
                    dep.depotname AS "Depot",
                    dz.zonename AS "Zone",
                    di.description AS "Description"
                FROM delivery_incidents di
                JOIN deliveries d ON d.deliveryid = di.deliveryid
                JOIN depots dep ON dep.depotid = d.depotid
                JOIN delivery_rates dr ON dr.rateid = d.rateid
                JOIN delivery_zones dz ON dz.zoneid = dr.zoneid
                ORDER BY di.incidentdate DESC, di.incidentid DESC
                """;
    }

    private String zonesSql() {
        return """
                SELECT
                    zoneid AS "_id",
                    zonename AS "Zone",
                    postalcodescovered AS "Postal Codes Covered"
                FROM delivery_zones
                ORDER BY zonename
                """;
    }

    private String ratesSql() {
        return """
                SELECT
                    dr.rateid AS "_id",
                    dz.zonename AS "Zone",
                    dr.weightclass AS "Weight Class",
                    dr.rateamount AS "Rate Amount",
                    dr.effectivedate AS "Effective Date"
                FROM delivery_rates dr
                JOIN delivery_zones dz ON dz.zoneid = dr.zoneid
                ORDER BY dz.zonename, dr.weightclass, dr.effectivedate DESC
                """;
    }

    private String assignmentsSql() {
        return """
                SELECT
                    va.assignmentid AS "_id",
                    va.externallivreurid AS "Livreur Ref",
                    v.licenseplate AS "Vehicle Plate",
                    v.vehicletype AS "Vehicle Type",
                    dep.depotname AS "Depot",
                    va.startdate AS "Start Date",
                    va.enddate AS "End Date"
                FROM vehicle_assignments va
                JOIN vehicles v ON v.vehicleid = va.vehicleid
                JOIN depots dep ON dep.depotid = v.depotid
                ORDER BY va.startdate DESC, va.assignmentid
                """;
    }

    private String stopsSql() {
        return """
                SELECT
                    rs.stopid AS "_id",
                    r.routename AS "Route",
                    rs.stopsequence AS "Stop Sequence",
                    d.externalorderid AS "Order Ref",
                    d.status AS "Delivery Status",
                    dep.depotname AS "Depot"
                FROM route_stops rs
                JOIN delivery_routes r ON r.routeid = rs.routeid
                JOIN deliveries d ON d.deliveryid = rs.deliveryid
                JOIN depots dep ON dep.depotid = d.depotid
                ORDER BY r.routename, rs.stopsequence
                """;
    }

    private String historySql() {
        return """
                SELECT
                    h.statushistoryid AS "_id",
                    d.externalorderid AS "Order Ref",
                    h.status AS "Status",
                    h.changeddate AS "Changed Date",
                    dep.depotname AS "Depot"
                FROM delivery_status_history h
                JOIN deliveries d ON d.deliveryid = h.deliveryid
                JOIN depots dep ON dep.depotid = d.depotid
                ORDER BY h.changeddate DESC, h.statushistoryid DESC
                """;
    }

    private String recentIncidentsSql() {
        return """
                SELECT
                    d.externalorderid AS "Order Ref",
                    di.incidenttype AS "Type",
                    di.incidentdate AS "Date",
                    dep.depotname AS "Depot"
                FROM delivery_incidents di
                JOIN deliveries d ON d.deliveryid = di.deliveryid
                JOIN depots dep ON dep.depotid = d.depotid
                ORDER BY di.incidentdate DESC
                LIMIT 8
                """;
    }

    private String routeSequenceSql() {
        return """
                SELECT
                    r.routename AS "Route",
                    rs.stopsequence AS "Stop",
                    d.externalorderid AS "Order Ref",
                    d.status AS "Status"
                FROM route_stops rs
                JOIN delivery_routes r ON r.routeid = rs.routeid
                JOIN deliveries d ON d.deliveryid = rs.deliveryid
                ORDER BY r.routeid, rs.stopsequence
                LIMIT 8
                """;
    }

    private String deliveryStatusSql() {
        return """
                SELECT
                    status AS "Status",
                    COUNT(*) AS "Deliveries"
                FROM deliveries
                GROUP BY status
                ORDER BY COUNT(*) DESC
                """;
    }

    private String stageBIncidentQuery() {
        return """
                SELECT
                    di.incidenttype AS "Incident Type",
                    di.incidentdate AS "Incident Date",
                    d.externalorderid AS "Order Ref",
                    dep.depotname AS "Depot",
                    dz.zonename AS "Destination Zone",
                    dr.weightclass AS "Weight Class"
                FROM delivery_incidents di
                JOIN deliveries d ON d.deliveryid = di.deliveryid
                JOIN depots dep ON dep.depotid = d.depotid
                JOIN delivery_rates dr ON dr.rateid = d.rateid
                JOIN delivery_zones dz ON dz.zoneid = dr.zoneid
                WHERE EXTRACT(MONTH FROM di.incidentdate) = 4
                  AND EXTRACT(YEAR FROM di.incidentdate) = 2026
                  AND EXISTS (
                      SELECT 1
                      FROM deliveries d_sub
                      WHERE d_sub.deliveryid = di.deliveryid
                        AND d_sub.status = 'En transit'
                  )
                ORDER BY di.incidentdate DESC
                """;
    }

    private String stageBRoutePlanningQuery() {
        return """
                SELECT
                    rs.stopsequence AS "Stop Number",
                    d.externalorderid AS "Order Ref",
                    d.status AS "Delivery Status",
                    dr.weightclass AS "Weight Class",
                    dr.rateamount AS "Rate Amount",
                    dz.zonename AS "Destination Zone",
                    dep.depotname AS "Route Depot",
                    r.scheduledstartdate AS "Route Date"
                FROM route_stops rs
                JOIN delivery_routes r ON r.routeid = rs.routeid
                JOIN depots dep ON dep.depotid = r.depotid
                JOIN deliveries d ON d.deliveryid = rs.deliveryid
                JOIN delivery_rates dr ON dr.rateid = d.rateid
                JOIN delivery_zones dz ON dz.zoneid = dr.zoneid
                WHERE rs.routeid = 1
                ORDER BY rs.stopsequence ASC
                """;
    }

    private interface ChangeHandler {
        void onChange();
    }

    private record DepotOption(int id, String name) {
        @Override
        public String toString() {
            return name;
        }
    }

    private record ZoneOption(int id, String name) {
        @Override
        public String toString() {
            return name;
        }
    }

    private record RateOption(int id, String zoneName, String weightClass, double amount) {
        @Override
        public String toString() {
            return zoneName + " / " + weightClass + " / " + amount;
        }
    }

    private record DeliveryOption(int id, int externalOrderId, String depotName, String status) {
        @Override
        public String toString() {
            return "Order " + externalOrderId + " / " + depotName + " / " + status;
        }
    }

    private record VehicleOption(int id, String licensePlate, String type, String depotName) {
        @Override
        public String toString() {
            return licensePlate + " / " + type + " / " + depotName;
        }
    }

    private record RouteOption(int id, String name, String depotName, String status) {
        @Override
        public String toString() {
            return name + " / " + depotName + " / " + status;
        }
    }

    private record VehicleData(
            int vehicleId,
            int depotId,
            String type,
            String licensePlate,
            double capacityVolume,
            double capacityWeight,
            Date lastMaintenanceDate
    ) {
    }

    private record DepotData(
            int id,
            String name,
            String location,
            double capacity
    ) {
    }

    private record RouteData(
            int id,
            int depotId,
            String name,
            Date scheduledStartDate,
            String status
    ) {
    }

    private record DeliveryData(
            int id,
            int externalOrderId,
            Integer externalPrimaryLivreurId,
            int depotId,
            int rateId,
            Date actualDeliveryDate,
            String status
    ) {
    }

    private record IncidentData(
            int id,
            int deliveryId,
            Integer externalLivreurId,
            String type,
            Date date,
            String description
    ) {
    }

    private record ZoneData(
            int id,
            String name,
            String postalCodes
    ) {
    }

    private record RateData(
            int id,
            int zoneId,
            String weightClass,
            double amount,
            Date effectiveDate
    ) {
    }

    private record AssignmentData(
            int id,
            int livreurId,
            int vehicleId,
            Date startDate,
            Date endDate
    ) {
    }

    private record StopData(
            int id,
            int routeId,
            int deliveryId,
            int sequence
    ) {
    }

    private record HistoryData(
            int id,
            int deliveryId,
            String status,
            Timestamp changedDate
    ) {
    }

    private static class SimpleDocumentListener implements javax.swing.event.DocumentListener {
        private final ChangeHandler handler;

        SimpleDocumentListener(ChangeHandler handler) {
            this.handler = handler;
        }

        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent event) {
            handler.onChange();
        }

        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent event) {
            handler.onChange();
        }

        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent event) {
            handler.onChange();
        }
    }

    private static class NavButton extends JButton {
        NavButton(String text) {
            super(text);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            boolean active = Boolean.TRUE.equals(getClientProperty("active"));
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (active) {
                g2.setColor(GREEN_SOFT);
                g2.fillRoundRect(0, 2, getWidth(), getHeight() - 4, 16, 16);
                g2.setColor(GREEN);
                g2.fillRoundRect(0, 12, 5, getHeight() - 24, 5, 5);
            }
            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    private static class CircleLogo extends JLabel {
        CircleLogo(String text) {
            super(text);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = Math.min(getWidth(), getHeight());
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;
            g2.setColor(new Color(48, 166, 94, 45));
            g2.fillOval(x + 4, y + 6, size - 8, size - 8);
            g2.setColor(GREEN);
            g2.fillOval(x, y, size - 6, size - 6);
            g2.setColor(new Color(255, 255, 255, 55));
            g2.fillOval(x + 8, y + 7, size / 3, size / 3);
            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    private static class BadgeLabel extends JLabel {
        private final Color color;

        BadgeLabel(String text, Color color) {
            super(text, SwingConstants.CENTER);
            this.color = color;
            setOpaque(false);
            setForeground(WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 15));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = Math.min(getWidth(), getHeight()) - 2;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 45));
            g2.fillOval(x + 3, y + 4, size, size);
            g2.setColor(color);
            g2.fillOval(x, y, size, size);
            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    private static class RouteCanvas extends JPanel {
        RouteCanvas() {
            setOpaque(false);
            setPreferredSize(new Dimension(420, 260));
            setBorder(new RoundedBorder(new Color(235, 239, 246), 16, new Insets(10, 10, 10, 10)));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            g2.setColor(new Color(249, 251, 254));
            g2.fillRoundRect(0, 0, width - 1, height - 1, 16, 16);

            g2.setColor(new Color(232, 237, 245));
            for (int x = 30; x < width; x += 55) {
                g2.drawLine(x, 18, x, height - 18);
            }
            for (int y = 30; y < height; y += 48) {
                g2.drawLine(18, y, width - 18, y);
            }

            int roadY = height / 2;
            g2.setStroke(new BasicStroke(30, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(222, 227, 234));
            g2.drawLine(35, roadY, width - 45, roadY);
            g2.drawLine(width / 2, 45, width / 2, height - 42);

            g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(121, 202, 158));
            g2.drawLine(78, roadY, width / 2, roadY);
            g2.drawLine(width / 2, roadY, width / 2, 70);
            g2.drawLine(width / 2, 70, width - 86, 70);

            drawPoint(g2, 78, roadY, GREEN, "D");
            drawPoint(g2, width / 2, roadY, BLUE, "1");
            drawPoint(g2, width / 2, 70, AMBER, "2");
            drawPoint(g2, width - 86, 70, RED, "!");

            drawLegend(g2, 26, height - 48, GREEN, "Active");
            drawLegend(g2, 116, height - 48, AMBER, "Delayed");
            drawLegend(g2, 218, height - 48, RED, "Incident");
            g2.dispose();
        }

        private void drawPoint(Graphics2D g2, int x, int y, Color color, String label) {
            g2.setColor(WHITE);
            g2.fillOval(x - 18, y - 18, 36, 36);
            g2.setColor(color);
            g2.fillOval(x - 14, y - 14, 28, 28);
            g2.setColor(WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g2.drawString(label, x - 4, y + 5);
        }

        private void drawLegend(Graphics2D g2, int x, int y, Color color, String label) {
            g2.setColor(color);
            g2.fillOval(x, y, 10, 10);
            g2.setColor(TEXT);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.drawString(label, x + 16, y + 10);
        }
    }

    private static class RoundedPanel extends JPanel {
        protected final int radius;
        protected final Color background;

        RoundedPanel(int radius, Color background) {
            this.radius = radius;
            this.background = background;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(background);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.setColor(BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    private static class ElevatedPanel extends RoundedPanel {
        ElevatedPanel(int radius, Color background) {
            super(radius, background);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(16, 24, 40, 18));
            g2.fillRoundRect(4, 6, getWidth() - 8, getHeight() - 8, radius, radius);
            g2.setColor(background);
            g2.fillRoundRect(0, 0, getWidth() - 8, getHeight() - 10, radius, radius);
            g2.setColor(BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 8, getHeight() - 10, radius, radius);
            g2.dispose();
        }
    }

    private static class RoundedBorder extends AbstractBorder {
        private final Color color;
        private final int radius;
        private final Insets insets;

        RoundedBorder(Color color, int radius, Insets insets) {
            this.color = color;
            this.radius = radius;
            this.insets = insets;
        }

        @Override
        public void paintBorder(Component component, Graphics graphics, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1));
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component component) {
            return insets;
        }

        @Override
        public Insets getBorderInsets(Component component, Insets targetInsets) {
            targetInsets.top = insets.top;
            targetInsets.left = insets.left;
            targetInsets.bottom = insets.bottom;
            targetInsets.right = insets.right;
            return targetInsets;
        }
    }
}
