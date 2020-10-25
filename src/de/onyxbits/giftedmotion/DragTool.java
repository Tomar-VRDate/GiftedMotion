package de.onyxbits.giftedmotion;

import java.awt.*;

public class DragTool
				extends TransformTool {

	@Override
	public void transform(SingleFrame img,
	                      Point mousePos) {
		img.getPosition().x = mousePos.x - offset.x;
		img.getPosition().y = mousePos.y - offset.y;
	}

	@Override
	public void beginTransform(SingleFrame img,
	                           Point mousePos) {
	}

	@Override
	public void endTransform(SingleFrame img,
	                         Point mousePos) {
	}

	@Override
	public String getStatus(SingleFrame img) {
		Integer[] status = {img.getPosition().x,
		                    img.getPosition().y};
		return Translations.get("core.imagedragged",
		                        status);
	}
}