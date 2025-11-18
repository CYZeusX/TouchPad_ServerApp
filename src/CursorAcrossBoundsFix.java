import java.awt.*;
import java.util.*;
import java.util.List;

/***
 * <c>CursorAcrossBoundsFix</c> class fixes the hardware side bug
 * where there is a gap in the virtual boundaries of the monitors.
 */
public class CursorAcrossBoundsFix
{
    private static final List<Rectangle> monitorRects = new ArrayList<>();
    private static final int GAP_SIZE = 600;
    private static Rectangle monitorBound = new Rectangle();

    /***
     * <c>getScreenBound()</c> obtains the virtual bounding of the existing connected monitors.
     */
    public void getMonitorBound()
    {
        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (GraphicsDevice screen : graphicsEnvironment.getScreenDevices())
        {
            Rectangle bounds = screen.getDefaultConfiguration().getBounds();
            monitorRects.add(screen.getDefaultConfiguration().getBounds());
            monitorBound = monitorBound.union(bounds);
        }
    }

    /***
     * <c>isPointValid()</c> checks if a point is inside ANY of the monitor rectangles bounds.
     * @param point the point object for the cursor.
     * @return true if cursor destination is within the valid monitor bounds.
     */
    public boolean isPointValid(Point point)
    {
        for (Rectangle rect : monitorRects)
            if (rect.contains(point))
                return true;
        return false;
    }

    /***
     * <c>isWithinBound()</c> checks if cursor is inside the monitor bounds.
     * @param point the point object for the cursor.
     * @return true if the cursor is within the monitor bounds.
     */
    public boolean isWithinBound(Point point)
    {
        return monitorBound.contains(point);
    }

    /***
     * <c>setWithinBound()</c> stops the cursor from going off-screen.
     * @param point the point object for the cursor.
     * @return new destination to go for the cursor.
     */
    public Point setWithinBound(Point point)
    {
        int x = Math.max(monitorBound.x, Math.min(point.x, monitorBound.x + monitorBound.width - 1));
        int y = Math.max(monitorBound.y, Math.min(point.y, monitorBound.y + monitorBound.height - 1));
        return new Point(x, y);
    }

    /**
     * <c>setAcrossBound()</c> calculates the correct "jump" destination based on the direction of travel.
     * @param current The cursor's current, valid position.
     * @param invalidDest The "void" coordinate where the cursor was trying to go.
     * @return A new Point with the correct point location to get across the monitor bound
     */
    public Point setAcrossBound(Point current, Point invalidDest)
    {
        // get the *direction* of travel
        int deltaX = invalidDest.x - current.x;
        int deltaY = invalidDest.y - current.y;

        // get the "step" direction
        int stepX = Integer.signum(deltaX);
        int stepY = Integer.signum(deltaY);

        if (stepX == 0 && stepY == 0)
            return current;

        // scanning valid pixels from the current location
        Point scanPoint = new Point(current.x, current.y);
        for (int i = 0; i < GAP_SIZE; i++)
        {
            // Move one "step" in the correct direction
            scanPoint.x += stepX;
            scanPoint.y += stepY;

            // check if this new point is on a monitor
            if (isPointValid(scanPoint))
                return scanPoint;

            // clamp if scan goes outside the bounds
            if (!isWithinBound(scanPoint))
                return setWithinBound(scanPoint);
        }

        // if scanned nth pixels and found nothing, clamp to the edge to be safe
        return setWithinBound(invalidDest);
    }
}