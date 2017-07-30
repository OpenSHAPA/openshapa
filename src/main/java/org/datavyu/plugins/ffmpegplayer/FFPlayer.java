package org.datavyu.plugins.ffmpegplayer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.sampled.AudioFormat;
import javax.swing.*;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.io.IOException;


public class FFPlayer extends JPanel {
	
	/** Identifier for object serialization */
	private static final long serialVersionUID = 5109839668203738974L;

	/** The logger for this class */
	private static Logger logger = LogManager.getLogger(FFPlayer.class);
	
	/** The requested color space */
	private final ColorSpace reqColorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);
	
	/** The requested audio format */
	private final AudioFormat reqAudioFormat = AudioStream.MONO_FORMAT;
	
	/** The movie stream for this movie player */
	private MovieStreamProvider movieStreamProvider;

	/** Play back speed for this player */
	private float playbackSpeed = 1f;
	
	/** Set to true if this player is in stepping mode */
	private boolean isStepping = false;

	/** This is the audio sound stream listener */
	private AudioSoundStreamListener audioSound = null;

	/**
	 * Construct an FFPlayer by creating the underlying movie stream provider
	 * and registering stream listeners for the video and audio. The stream
	 * listener for the video will show the image in this JPanel.
	 */
	public FFPlayer() {
		setLayout(new BorderLayout());
		movieStreamProvider = new MovieStreamProvider();
		audioSound = new AudioSoundStreamListener(movieStreamProvider);
		// Add the audio sound listener
		movieStreamProvider.addAudioStreamListener(audioSound);
		// Add video display
		movieStreamProvider.addVideoStreamListener(
				new VideoDisplayStreamListener(movieStreamProvider, this, BorderLayout.CENTER, reqColorSpace));
	}

	/**
	 * Open a file with the fileName.
	 *
	 * @param fileName The filename.
	 */
	protected void openFile(String fileName) {
		movieStreamProvider.stop();
		// Assign a new movie file.
		try {
			// The input audio format will be manipulated by the open method!
			AudioFormat input = new AudioFormat(
					reqAudioFormat.getEncoding(), 
					reqAudioFormat.getSampleRate(),
					reqAudioFormat.getSampleSizeInBits(),
					reqAudioFormat.getChannels(),
					reqAudioFormat.getFrameSize(),
					reqAudioFormat.getFrameRate(),
					reqAudioFormat.isBigEndian());
			
			// Open the stream
	        movieStreamProvider.open(fileName, "0.0.1", reqColorSpace, input);
	        
	        // Load and display first frame.
	        movieStreamProvider.nextImageFrame();
		} catch (IOException io) {
			io.printStackTrace();
		}        
	}
	
	/**
	 * Returns true if values are set for the video to be played back at 1x 
	 * speed.
	 *  
	 * @return True if we play forward at 1x; otherwise false.
	 */
	private boolean playsAtForward1x() {
		double speedValue = Math.abs(playbackSpeed);
		int speedSign = (int) Math.signum(playbackSpeed);
		return Math.abs(speedValue - 1.0) <= Math.ulp(1.0) && speedSign == 1 && !isStepping;
	}

	/**
	 * Get the duration of the opened video/audio stream of this player in seconds.
	 *
	 * @return Duration of the opened stream.
	 */
	public double getDuration() {
		return movieStreamProvider.getDuration();
	}

	/**
	 * Get the original stream size (not the size when a viewing window is used).
	 *
	 * @return Original stream size: width, height.
	 */
	public Dimension getOriginalVideoSize() {
		return new Dimension(movieStreamProvider.getWidthOfStream(),
							 movieStreamProvider.getHeightOfStream());
	}

	/**
	 * Get the current time in seconds.
	 *
	 * @return Current time in seconds.
	 */
	public double getCurrentTime() {
		return movieStreamProvider.getCurrentTime();
	}

	/**
	 * Seek to the position.
	 *
	 * @param position Position in seconds.
	 */
	public void seek(double position) {
		logger.info("Seeking position: " + position);
		// TODO: May have to stop the video here!
		movieStreamProvider.seek(position);
		movieStreamProvider.dropImageFrame();
		movieStreamProvider.startVideoListeners();
		movieStreamProvider.nextImageFrame();
		// TODO: May have to start the video here!
	}

	/**
	 * Clean up the player before closing.
	 */
	public void cleanUp() {
		movieStreamProvider.stop();
	}

	/**
	 * Set the play back speed for this player.
	 *
	 * @param playbackSpeed The play back speed.
	 */
	public void setPlaybackSpeed(float playbackSpeed) {
		this.playbackSpeed = playbackSpeed;

		// Need to set speed first so that the reverse is set correctly!!!
		movieStreamProvider.setSpeed(playbackSpeed);
		// Then we can start/stop the audio
		if (playsAtForward1x()) {
			movieStreamProvider.startAudio();
		} else {
			movieStreamProvider.stopAudio();
		}
	}

	/**
	 * Play the video/audio.
	 */
	public void play() {
		// Must set is stepping to false BEFORE calling playsAtForward1x
		isStepping = false;
		// Play sound only if we are in forward direction
		if (playsAtForward1x()) {
			logger.info("Starting playing audio.");
			movieStreamProvider.startAudio();
		}
		// Play video
		logger.info("Starting playing video");
		movieStreamProvider.startVideo();
	}

	/**
	 * Stop the video/audio.
	 */
	public void stop() {
		logger.info("Stopping the video.");
		movieStreamProvider.stop();
	}

	/**
	 * Rewind to the start/end of file depending if we play forward or backward, respectively.
	 */
	public void rewind() {
		movieStreamProvider.reset();
	}

	/**
	 * Instead of playing a sequence of frames just step by one frame.
	 */
	public void step() {
		if (!isStepping) {
			// Set natively no sound (otherwise the audio buffer will block the video stream)
			movieStreamProvider.setPlaySound(false);
			// Stops all stream providers
			movieStreamProvider.stop();
			// Enables stepping to display the frames without starting the video thread
			movieStreamProvider.startVideoListeners();
			// We are stepping, set isStepping
			isStepping = true;
		}
		movieStreamProvider.nextImageFrame();
	}

	/**
	 * Reset the streams to their original state after opening the file.
	 */
	public void reset() {
		try {
			// Stop the player before changing the window
			movieStreamProvider.stop();
			int w = movieStreamProvider.getWidthOfStream();
			int h = movieStreamProvider.getHeightOfStream();
			movieStreamProvider.setView(0, 0, w, h);
		} catch (IndexOutOfBoundsException iob) {
			System.err.println("Could not reset view: " + iob.getMessage());
			iob.printStackTrace();
		} finally {
			movieStreamProvider.start();
		}
	}

	/**
	 * Set the audio volume.
	 *
	 * @param volume New volume to set.
	 */
	public void setVolume(float volume) {
		audioSound.setVolume(volume);
	}
}
