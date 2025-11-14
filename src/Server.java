import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.TimeUnit;

/***
 * <c>Server</c> class setups the server to receive incoming commands to process, and execute corresponding execution.
 */
public class Server
{
    private final MouseControl MOUSE_CONTROL = new MouseControl();
    private static final int LOCAL_PORT = 42069;
    private static final String FIREWALL_RULE_NAME = "Touch Pad Server";

    /***
     * <c>setupServer()</c> receives command sent from the client side application,
     * and control cursor movements.
     * @param robot acts as the computer mouse to virtually control the mouse movement.
     */
    public void setupServer(Robot robot)
    {
        try (DatagramSocket datagramSocket = new DatagramSocket(LOCAL_PORT))
        {
            System.out.println("Server listening on port 42069");

            while (true)
            {
                byte[] buffer = new byte[1024];
                DatagramPacket datagramPacket=  new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(datagramPacket);

                String receivedCommand = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
                parseCommand(robot, receivedCommand);
            }
        }

        catch (IOException e) {throw new RuntimeException(e);}
    }

    /***
     * <c>parseCommand()</c> parse the received command and converts to values
     * that the robot object understands to convert to mouse movements.
     * @param receivedCommand is the string value that sent by the client side mobile application
     */
    public void parseCommand(Robot robot, String receivedCommand)
    {
        receivedCommand = receivedCommand.trim();

        // If mouse is moving the cursor
        if (receivedCommand.contains(","))
        {
            String[] cursorMovements = receivedCommand.split(",");
            if (cursorMovements.length >= 2)
            {
                int moveX = tryParseToInt(cursorMovements[0]);
                int moveY = tryParseToInt(cursorMovements[1]);

                if (moveX != 0 || moveY != 0)
                    MOUSE_CONTROL.cursorMove(robot, moveX, moveY);
            }
        }

        // If mouse is doing actions other than moving cursor
        else
        {
            String[] mouseActions = receivedCommand.split(" ");
            String extractedCommand = mouseActions[0];

            switch (extractedCommand)
            {
                case "left_click" -> MOUSE_CONTROL.cursorClick(robot, true);
                case "right_click" -> MOUSE_CONTROL.cursorClick(robot, false);
                case "scroll"  ->
                {
                    if (mouseActions.length >= 2)
                        MOUSE_CONTROL.mouseScroll(robot, tryParseToInt(mouseActions[1]));
                    else System.out.println("Invalid command: " + receivedCommand);
                }
                default -> System.out.println("Unknown command: " + receivedCommand);
            }
        }
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
    private static boolean runCommand(String[] command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();

            // We must read the output streams, or the process might hang
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            // Read output (optional, but good for debugging)
            // String s;
            // while ((s = stdInput.readLine()) != null) { System.out.println(s); }
            // while ((s = stdError.readLine()) != null) { System.err.println(s); }

            process.waitFor(5, TimeUnit.SECONDS); // Wait for 5 seconds max
            return process.exitValue() == 0; // 0 = success

        } catch (Exception e) {
            System.err.println("Command execution error: " + e.getMessage());
            return false;
        }
    }

    // --- NEW METHOD 2: The main firewall logic ---
    /**
     * Checks if the firewall rule exists, and if not, tries to add it.
     */
    public static void checkAndAddFirewallRule() {
        System.out.println("Checking firewall rules...");

        // Step 1: Check if the rule already exists
        String[] checkCommand = {
                "netsh", "advfirewall", "firewall", "show", "rule", "name=" + FIREWALL_RULE_NAME
        };

        if (runCommand(checkCommand)) {
            System.out.println("Firewall rule '" + FIREWALL_RULE_NAME + "' already exists. Good to go.");
            return; // Rule exists, we're done.
        }

        // Step 2: Rule doesn't exist, so try to add it.
        System.out.println("Firewall rule not found. Attempting to add...");
        String[] addCommand = {
                "netsh", "advfirewall", "firewall", "add", "rule",
                "name=" + FIREWALL_RULE_NAME,
                "dir=in",
                "action=allow",
                "protocol=UDP",
                "localport=" + LOCAL_PORT
        };

        if (runCommand(addCommand)) {
            System.out.println("Successfully added firewall rule!");
        } else {
            // This is the most likely failure point
            System.err.println("\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.err.println("Failed to add firewall rule. This is likely a permission error.");
            System.err.println("Please re-run this program as an Administrator.");
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n");
            // We can choose to exit here, or just warn the user. Let's warn.
        }
    }
}