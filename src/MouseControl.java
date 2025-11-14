import java.awt.*;
import java.awt.event.InputEvent;

/***
 * <c>MouseControl</c> class defines the mouse controlling logic.
 */
public class MouseControl
{
    /**
     * <c>cursorMove()</c> moves the Mouse Cursor with the declared value.
     * @param robot is the object for controlling the mouse cursor.
     * @param moveX is the pixel amount to move the mouse cursor by.
     * @param moveY is the pixel amount to move the mouse cursor by.
     */
    public void cursorMove(Robot robot, int moveX, int moveY)
    {
        // Obtain current cursor location
        Point currentCursorLocation = MouseInfo.getPointerInfo().getLocation();
        int currentX = (int) currentCursorLocation.getX();
        int currentY = (int) currentCursorLocation.getY();

        // Set destination based on current location
        int destinationX = currentX + moveX;
        int destinationY = currentY + moveY;

        // Move the Cursor to the clamped destination
        robot.mouseMove(destinationX, destinationY);
    }

    /***
     * <c>cursorClick()</c> executes the mouse clicking action for both left and right click.
     * @param robot is the object for controlling the mouse action.
     * @param leftClick confirms if the selected action is left click or not.
     */
    public void cursorClick(Robot robot, boolean leftClick)
    {
        int clickMode = leftClick ? InputEvent.BUTTON1_DOWN_MASK : InputEvent.BUTTON3_DOWN_MASK;

        robot.mousePress(clickMode);
        robot.mouseRelease(clickMode);
    }

    /***
     * <c>mouseScroll()</c> executes the mouse wheel scrolling action for both scrolling up and down.
     * @param robot is the object for controlling the mouse wheel action.
     * @param value stores the scrolling value. Scroll up if negative. Scroll down if positive.
     */
    public void mouseScroll(Robot robot, int value)
    {
        robot.mouseWheel(value);
    }
}