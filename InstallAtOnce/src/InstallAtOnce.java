import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class InstallAtOnce extends JFrame {
    private final JTextField sourceFolderField;
    private final JTextField installFolderField;
    private final JTextArea logArea;

    public InstallAtOnce() {
        setTitle("Install All of them");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        JPanel panel = new JPanel(new GridLayout(4, 1));

        //TitleLabnel
        JLabel titleLabel = new JLabel("Sequential Installer", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(titleLabel);

        //SourcePanel and SourceButton
        JPanel sourcePanel = new JPanel(new FlowLayout());
        sourcePanel.add(new JLabel("Source Folder:"));
        sourceFolderField = new JTextField(20);
        JButton sourceButton = new JButton("Browse");
        sourceButton.addActionListener(e -> selectFolder(sourceFolderField));
        sourcePanel.add(sourceFolderField);
        sourcePanel.add(sourceButton);
        panel.add(sourcePanel);

        //InstallPanel and InstallButton
        JPanel installPanel = new JPanel(new FlowLayout());
        installPanel.add(new JLabel("Install Folder:"));
        installFolderField = new JTextField(20);
        JButton installButton = new JButton("Browse");
        installButton.addActionListener(e -> selectFolder(installFolderField));
        installPanel.add(installFolderField);
        installPanel.add(installButton);
        panel.add(installPanel);

        //Installation Execution Button
        JButton installExeButton = new JButton("Start Installation");
        installExeButton.addActionListener(e -> startInstallation());
        panel.add(installExeButton);

        add(panel, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    //Folder Selector - Operator
    private void selectFolder(JTextField textField) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnValue = chooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            textField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    //Initialization
    private void startInstallation() {
        String sourceFolder = sourceFolderField.getText();
        String installFolder = installFolderField.getText();

        if (sourceFolder.isEmpty() || installFolder.isEmpty()) {
            logArea.append("Please select both source and install folders.\n");
            return;
        }

        File folder = new File(sourceFolder);
        if (!folder.exists() || !folder.isDirectory()) {
            logArea.append("Invalid source folder.\n");
            return;
        }

        File[] files = folder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".exe") || name.toLowerCase().endsWith(".msi")
        );

        if (files == null || files.length == 0) {
            logArea.append("No .exe or .msi files found in the directory.\n");
            return;
        }

        //Configuration at here!!
        int maxExecutables = 10;
        int count = 0;

        for (File file : files) {
            if (count >= maxExecutables) break;

            try {
                logArea.append("Installing: " + file.getName() + "\n");

                //eh dee min gwon han nae nua
                ProcessBuilder processBuilder;
                if (file.getName().toLowerCase().endsWith(".exe")) {
                    processBuilder = new ProcessBuilder("powershell", "-Command", "Start-Process", file.getAbsolutePath(), "-Verb", "RunAs");
                } else {
                    processBuilder = new ProcessBuilder("powershell", "-Command", "Start-Process", "msiexec", "-ArgumentList", "'/i " + file.getAbsolutePath() + " INSTALLDIR=" + installFolder + "'", "-Verb", "RunAs");
                }

                processBuilder.inheritIO();
                Process process = processBuilder.start();
                process.waitFor();

                logArea.append("Completed: " + file.getName() + "\n");
                count++;
            } catch (IOException | InterruptedException e) {
                logArea.append("Failed to install: " + file.getName() + "\n");
                e.printStackTrace();
            }
        }

        logArea.append("Installation process completed.\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(InstallAtOnce::new);
    }
}
