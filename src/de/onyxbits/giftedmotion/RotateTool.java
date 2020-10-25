package de.onyxbits.giftedmotion;

import java.awt.*;

public class RotateTool
				extends TransformTool {
	private double lastRotationDeg;

	@Override
	public void transform(SingleFrame img,
	                      Point mousePos) {
		float x = mousePos.x - img.getPosition().x - (img.getScaleX() / 2);
		float y = mousePos.y - img.getPosition().y - (img.getScaleY() / 2);
		if (x != 0) {
			double rotationDegrees = Math.atan2(y,
			                                    x) - lastRotationDeg;
			img.setRotationDegrees(rotationDegrees);
		}
	}

	@Override
	public String getStatus(SingleFrame img) {
		Double[] status = {Math.toDegrees(img.getRotationDegrees()) % 360};
		return Translations.get("core.imagerotated",
		                        status);
	}

	@Override
	public void beginTransform(SingleFrame img,
	                           Point mousePos) {
		lastRotationDeg = img.getRotationDegrees();
		transform(img,
		          mousePos);
		endTransform(img,
		             mousePos);
		transform(img,
		          mousePos);
	}

	@Override
	public void endTransform(SingleFrame img,
	                         Point mousePos) {
		lastRotationDeg = img.getRotationDegrees();
	}
}