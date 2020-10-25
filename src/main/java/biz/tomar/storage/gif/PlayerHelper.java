package biz.tomar.storage.gif;

class PlayerHelper
				implements Runnable {

	private final FrameSequence frameSequence;

	public PlayerHelper(FrameSequence frameSequence) {
		this.frameSequence = frameSequence;
	}

	public void run() {
		frameSequence.fireDataChanged();
	}
}