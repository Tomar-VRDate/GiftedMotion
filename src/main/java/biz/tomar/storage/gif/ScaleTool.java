package biz.tomar.storage.gif;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ScaleTool
				extends TransformTool {

	private float lastScaleX;
	private float lastScaleY;

	@Override
	public void transform(SingleFrame img,
	                      Point mousePos) {
		float scaleX = mousePos.x - img.getPosition().x - lastScaleX;
		img.setScaleX(scaleX);
		if (!shiftPressed) {
			float scaleY = mousePos.y - img.getPosition().y - lastScaleY;
			img.setScaleY(scaleY);
		} else {
			BufferedImage bufferedImage = img.getBufferedImage();
			int           width         = bufferedImage.getWidth();
			int           height        = bufferedImage.getHeight();
			float         scale         = img.getScaleX() / width;
			float         scaleY        = scale * height;
			img.setScaleY(scaleY);
		}
	}

	@Override
	public String getStatus(SingleFrame img) {
		Integer[] status = {(int) img.getScaleX(),
		                    (int) Math.floor(img.getScaleY())};
		return Translations.get("core.componentresized",
		                        status);
	}

	@Override
	public void beginTransform(SingleFrame img,
	                           Point mousePos) {
		lastScaleX = img.getScaleX();
		lastScaleY = img.getScaleY();
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
		lastScaleX = img.getScaleX();
		lastScaleY = img.getScaleY();
	}
}
