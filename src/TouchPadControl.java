import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/***
 * <c>TouchPadControl</c> class defines the mouse controlling logic.
 */
public class TouchPadControl
{
    private Robot robot;
    private CursorAcrossBoundsFix fix;

    /***
     * <c>setUp()</c> configures the robot object to control mouse movements.
     * @param robot is the object for controlling the mouse cursor.
     */
    public void setup(Robot robot, CursorAcrossBoundsFix fix)
    {
        this.robot = robot;
        this.fix = fix;
        fix.getMonitorBound();
    }

    /**
     * <c>cursorMove()</c> moves the Mouse Cursor with the declared value.
     * @param moveX is the pixel amount to move the mouse cursor by.
     * @param moveY is the pixel amount to move the mouse cursor by.
     */
    public void cursorMove(int moveX, int moveY)
    {
        // Obtain current cursor location, and move cursor based on the current's
        Point currentCursorLocation = MouseInfo.getPointerInfo().getLocation();
        int destinationX = moveX + currentCursorLocation.x;
        int destinationY = moveY + currentCursorLocation.y;
        Point destinationCursorLocation = new Point(destinationX, destinationY);

        // Case 1: VALID. - Expected cursor movement
        if (fix.isPointValid(destinationCursorLocation))
            robot.mouseMove(destinationX, destinationY);

        // Case 2: EXTERNAL_EDGE. - Cursor would fall to Case 3 if unable to lock the bounds
        else if (!fix.isWithinBound(destinationCursorLocation))
        {
            Point clampedDestination = fix.setWithinBound(destinationCursorLocation);
            robot.mouseMove(clampedDestination.x, clampedDestination.y);
        }

        // Case 3: INTERNAL_GAP. - Cursor would jump if out of monitor bounds
        else
        {
            Point acrossedBound = fix.setAcrossBound(currentCursorLocation, destinationCursorLocation);
            robot.mouseMove(acrossedBound.x, acrossedBound.y);
        }
    }

    /***
     * <c>cursorClick()</c> executes the mouse clicking action for both left and right click.
     * @param leftClick confirms if the selected action is left click or not.
     */
    public void cursorClick(boolean leftClick)
    {
        int clickMode = leftClick ? InputEvent.BUTTON1_DOWN_MASK : InputEvent.BUTTON3_DOWN_MASK;
        robot.mousePress(clickMode);
        robot.mouseRelease(clickMode);
    }

    /***
     * <c>mouseScrollVertically()</c> executes the mouse wheel scrolling action for both scrolling up and down.
     * @param value stores the scrolling value. Scroll up if negative. Scroll down if positive.
     */
    public void mouseScrollVertically(int value)
    {
        robot.mouseWheel(value);
    }

    /***
     * <c>mouseScrollHorizontally()</c> executes the mouse wheel scrolling action for both scrolling left and right. <br></br>
     * Horizontal scrolling is done with holding shift + scrolling vertically.
     * @param value stores the scrolling value. Scroll left if negative. Scroll right if positive.
     */
    public void mouseScrollHorizontally(int value)
    {
        robot.keyPress(KeyEvent.VK_SHIFT);
        robot.mouseWheel(value);
        robot.keyRelease(KeyEvent.VK_SHIFT);
    }

    /***
     * <c>cursorPress()</c> presses and holds the left mouse button
     */
    public void cursorPress()
    {
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
    }

    /***
     * <c>cursorRelease()</c> releases the left mouse button
     */
    public void cursorRelease()
    {
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    /***
     * <c>keyCombination()</c> presses and releases a sequence of keys.
     * @param keyCodes is the varargs list of keys to press
     */
    public void keyCombination(int... keyCodes)
    {
        if (keyCodes.length == 0)
            return;

        // Press all keys in order
        for (int keyCode : keyCodes)
            robot.keyPress(keyCode);

        // Release all keys in reverse order
        for (int i = keyCodes.length - 1; i >= 0; i--)
            robot.keyRelease(keyCodes[i]);
    }
}