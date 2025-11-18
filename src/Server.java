import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.TimeUnit;

/***
 * <c>Server</c> class setups the server to receive incoming commands to process, and execute corresponding execution.
 */
public class Server
{
    private final TouchPadControl TOUCH_PAD_CONTROL = new TouchPadControl();
    private final ServerGUI serverGUI = new ServerGUI();
    private static final int LOCAL_PORT = 42069;
    private static final String FIREWALL_RULE_NAME = "Touch Pad Server";
    private String[] mouseActions;
    private String[] parameters;

    /***
     * <c>setupServer()</c> receives command sent from the client side application,
     * and control cursor movements.
     */
    public void setupServer(Robot robot, CursorAcrossBoundsFix fix)
    {
        serverGUI.initialize();
        TOUCH_PAD_CONTROL.setup(robot, fix);

        try (DatagramSocket datagramSocket = new DatagramSocket(LOCAL_PORT))
        {
            System.out.println("Server listening on port 42069");

            while (true)
            {
                byte[] buffer = new byte[1024];
                DatagramPacket datagramPacket=  new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(datagramPacket);

                Point currentCursorLocation = MouseInfo.getPointerInfo().getLocation();
                int cursorX = currentCursorLocation.x;
                int cursorY = currentCursorLocation.y;

                String receivedCommand = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
                serverGUI.getLogTextArea().setText("Received: "+ receivedCommand +"\nCursor: "+ cursorX +", "+ cursorY);
                extractCommandAndParameters(receivedCommand);
                assignCommand();
            }
        }

        catch (IOException e) {throw new RuntimeException(e);}
    }

    /***
     * <c>assignCommand()</c> assigns the received command to corresponding mouse movements.
     */
    public void assignCommand()
    {
        assert mouseActions != null;

        String extractedCommand = mouseActions[0];
        switch (extractedCommand)
        {
            // Click
            case "left_click" -> TOUCH_PAD_CONTROL.cursorClick(true);
            case "right_click" -> TOUCH_PAD_CONTROL.cursorClick(false);

            // --- Tap and Drag (Select) ---
            case "mouse_down" -> TOUCH_PAD_CONTROL.cursorPress();
            case "mouse_up" -> TOUCH_PAD_CONTROL.cursorRelease();

            // --- 1F Drag (Move) ---
            case "drag" ->
            {
                if (parameters != null && parameters.length >= 2)
                    TOUCH_PAD_CONTROL.cursorMove(tryParseToInt(parameters[0]), tryParseToInt(parameters[1]));
            }

            // --- 2F Drag (Scroll) ---
            case "scrollX" ->
            {
                if (parameters != null && parameters.length >= 1)
                    TOUCH_PAD_CONTROL.mouseScrollHorizontally(tryParseToInt(parameters[0]));
            }

            case "scrollY" ->
            {
                if (parameters != null && parameters.length >= 1)
                    TOUCH_PAD_CONTROL.mouseScrollVertically(tryParseToInt(parameters[0]));
            }

            // --- 3F Swipe (Ctrl + Tabs) ---
            case "ctrl_shift_tab" -> TOUCH_PAD_CONTROL.keyCombination(KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT, KeyEvent.VK_TAB);
            case "ctrl_tab" -> TOUCH_PAD_CONTROL.keyCombination(KeyEvent.VK_CONTROL, KeyEvent.VK_TAB);

            // --- 4F Swipe (Alt + Tab) ---
            case "alt_tab" -> TOUCH_PAD_CONTROL.keyCombination(KeyEvent.VK_ALT, KeyEvent.VK_TAB);

            // --- 5F Swipe (Ctrl T / W) ---
            case "ctrl_t" -> TOUCH_PAD_CONTROL.keyCombination(KeyEvent.VK_CONTROL, KeyEvent.VK_T);
            case "ctrl_w" -> TOUCH_PAD_CONTROL.keyCombination(KeyEvent.VK_CONTROL, KeyEvent.VK_W);

            default -> System.out.println("Unknown command: " + extractedCommand);
        }
    }

    /***
     * <c>extractCommandAndParameters()</c> extracts the received data from the datagram packet, <br></br>
     * then assigns the command and the parameters separately.
     * @param receivedCommand is the string value that sent by the client side mobile application
     */
    private void extractCommandAndParameters(String receivedCommand)
    {
        receivedCommand = receivedCommand.trim();
        mouseActions = receivedCommand.split(" ");
        parameters = null;

        if (mouseActions.length >= 2)
            parameters = mouseActions[1].split(",");
    }

    /***
     * <c>tryParseToInt()</c> converts the string values to numeric.
     * @param receivedCommandPart is one of the section from the received commands.
     * @return numerical values for cursor movements.
     */
    private int tryParseToInt(String receivedCommandPart)
    {
        try { return Integer.parseInt(receivedCommandPart); }
        catch (Exception e) {return 0;}
    }

    /**
     * Executes a system command and returns its exit code.
     * @param command The command to run.
     * @return true if the command succeeded (exit code 0), false otherwise.
     */
    private static boolean runCommand(String[] command)
    {
        try
        {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();

            process.waitFor(5, TimeUnit.SECONDS);
            return process.exitValue() == 0;
        }

        catch (Exception e)
        {
            System.err.println("Command execution error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if the firewall rule exists, and if not, tries to add it.
     */
    public static void checkAndAddFirewallRule()
    {
        System.out.println("Checking firewall rules...");

        // Check if the rule already exists
        String[] checkCommand = {"netsh", "advfirewall", "firewall", "show", "rule", "name=" + FIREWALL_RULE_NAME};

        if (runCommand(checkCommand))
        {
            System.out.println("Firewall rule '" + FIREWALL_RULE_NAME + "' already exists. Good to go.");
            return;
        }

        // Rule doesn't exist, try to add it
        System.out.println("Firewall rule not found. Attempting to add...");
        String[] addCommand =
                {
                    "netsh", "advfirewall", "firewall", "add", "rule",
                    "name=" + FIREWALL_RULE_NAME,
                    "dir=in",
                    "action=allow",
                    "protocol=UDP",
                    "localport=" + LOCAL_PORT
                };

        if (runCommand(addCommand))
            System.out.println("Successfully added firewall rule!");

        else
        {
            String waring =
                    """
                    !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    Failed to add firewall rule. This is likely a permission error.
                    Please re-run this program as an Administrator.
                    !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    """;
            System.out.println(waring);
        }
    }
}