import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/***
 * <c>ServerGUI</c> creates the main window for the server,
 * displays the local IP address, and shows a log of server messages.
 */
public class ServerGUI
{
    private JTextArea logTextArea;

    /**
     * Creates and displays the main GUI window.
     */
    public void initialize()
    {
        // 1. Create the main window
        JFrame frame = new JFrame("Touchpad Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(800, 600));
        frame.setLayout(new BorderLayout());

        // 2. Create the IP address label
        String ipAddress = getLocalIpAddress();
        JLabel ipLabel = new JLabel("  Connect your phone to:     " + ipAddress);
        ipLabel.setFont(new Font("Arial", Font.BOLD, 35));
        ipLabel.setOpaque(true);
        ipLabel.setBackground(Color.DARK_GRAY);
        ipLabel.setForeground(Color.WHITE);
        frame.add(ipLabel, BorderLayout.NORTH);

        // 3. Create the scrolling log area
        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        logTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logTextArea.setBackground(Color.BLACK);
        logTextArea.setForeground(Color.GREEN);
        logTextArea.setMargin(new Insets(5, 5, 5, 5));
        JScrollPane scrollPane = new JScrollPane(logTextArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        // 4. Redirect System.out to the JTextArea
        redirectSystemOut();

        // 5. Show the window
        frame.setLocationRelativeTo(null); // Center on screen
        frame.setVisible(true);
    }

    /**
     * Finds the best local IPv4 address for the server.
     * @return A string (e.g., "192.168.1.100") or "Not Found".
     */
    private String getLocalIpAddress()
    {
        try
        {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements())
            {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp())
                    continue; // Skip loopback and down interfaces

                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while(addresses.hasMoreElements())
                {
                    InetAddress inetAddress = addresses.nextElement();
                    // We want a site-local, non-loopback, IPv4 address
                    if (inetAddress.isSiteLocalAddress() && !inetAddress.isLoopbackAddress() && inetAddress.getAddress().length == 4)
                        return inetAddress.getHostAddress();
                }
            }
        }
        // Log error to the (original) console
        catch (Exception e) {e.printStackTrace();}
        return "IP Not Found";
    }

    /**
     * Redirects all System.out.println messages to the logTextArea.
     * This is thread-safe.
     */
    private void redirectSystemOut()
    {
        OutputStream outputStream = new OutputStream()
        {
            @Override
            public void write(int b) throws IOException
            {
                // This is how you safely update a Swing component from any thread
                SwingUtilities.invokeLater(() ->
                {
                    logTextArea.append(String.valueOf((char) b));
                    // Auto-scroll to the bottom
                    logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
                });
            }
        };

        System.setOut(new PrintStream(outputStream, true));
        System.setErr(new PrintStream(outputStream, true));
    }
}