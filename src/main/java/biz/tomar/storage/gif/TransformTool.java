package biz.tomar.storage.gif;

import java.awt.*;

public abstract class TransformTool {
	protected Point   offset;
	protected boolean shiftPressed;

	public void setOffset(Point mousePos) {
		this.offset = mousePos;
	}

	public void setShiftPressed(boolean shi) {
		shiftPressed = shi;
	}

	public abstract void beginTransform(SingleFrame img,
	                                    Point mousePos);

	public abstract void transform(SingleFrame img,
	                               Point mousePos);

	public abstract void endTransform(SingleFrame img,
	                                  Point mousePos);

	public abstract String getStatus(SingleFrame img);
}
