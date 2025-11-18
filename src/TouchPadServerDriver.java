import java.awt.*;

/***
 * <c>TouchPadServer</c> class is the application protocol
 * that receives the commands sent from the client side.
 * mobile application to control mouse movements.
 */
class TouchPadServerDriver
{
    /***
     * The main method to start the server and stores the <c>robot</c> object to control the cursor and mouse action.
     * @throws AWTException is for the case to log error from the robot object.
     */
    public static void main(String[] args) throws AWTException
    {
        CursorAcrossBoundsFix fix = new CursorAcrossBoundsFix();
        Robot robot = new Robot();
        Server server = new Server();
        Server.checkAndAddFirewallRule();

        new Thread(() ->
        {
            try {server.setupServer(robot, fix);}
            catch (Exception exception) {exception.printStackTrace();}
        }).start();
    }
}